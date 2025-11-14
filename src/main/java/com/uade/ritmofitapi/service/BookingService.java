package com.uade.ritmofitapi.service;

import com.uade.ritmofitapi.dto.response.BookingResponse;
import com.uade.ritmofitapi.dto.request.BookingRequest;

import com.uade.ritmofitapi.dto.response.UserBookingDto;
import com.uade.ritmofitapi.exception.AlreadyBookedException;
import com.uade.ritmofitapi.model.ScheduledClass;
import com.uade.ritmofitapi.model.booking.UserBooking;
import com.uade.ritmofitapi.model.booking.BookingStatus;
import com.uade.ritmofitapi.repository.BookingRepository;
import com.uade.ritmofitapi.repository.ScheduledClassRepository;
import com.uade.ritmofitapi.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final ScheduledClassRepository scheduledClassRepository;
    private final UserRepository userRepository;

    public BookingService(BookingRepository bookingRepository,
                          ScheduledClassRepository scheduledClassRepository,
                          UserRepository userRepository) {
        this.bookingRepository = bookingRepository;
        this.scheduledClassRepository = scheduledClassRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public BookingResponse create(BookingRequest bookingRequest, String userId) {
        // Validar que el usuario existe
        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("Usuario no encontrado con ID: " + userId);
        }

        ScheduledClass scheduledClass = scheduledClassRepository.findById(bookingRequest.getScheduledClassId())
                .orElseThrow(() -> new RuntimeException("Clase agendada no encontrada con ID: " + bookingRequest.getScheduledClassId()));

        // Validar cupo disponible
        if (scheduledClass.getEnrolledCount() >= scheduledClass.getCapacity()) {
            throw new RuntimeException("No hay cupos disponibles para esta clase.");
        }

        // Validar que no tenga una reserva CONFIRMADA para esta misma clase
        if (bookingRepository.existsByUserIdAndScheduledClassIdAndStatus(userId, scheduledClass.getId(), BookingStatus.CONFIRMED)) {
            throw new AlreadyBookedException("La clase ya fue reservada por este usuario.");
        }

        // Validar horarios solapados: buscar reservas confirmadas del usuario
        LocalDateTime classStart = scheduledClass.getDateTime();
        LocalDateTime classEnd = classStart.plusMinutes(scheduledClass.getDurationMinutes());

        List<UserBooking> userConfirmedBookings = bookingRepository.findAllByUserIdAndStatus(userId, BookingStatus.CONFIRMED);
        
        for (UserBooking existingBooking : userConfirmedBookings) {
            LocalDateTime existingStart = existingBooking.getClassDateTime();
            LocalDateTime existingEnd = existingStart.plusMinutes(existingBooking.getDurationMinutes());
            
            // Verificar si hay solapamiento
            if (classStart.isBefore(existingEnd) && classEnd.isAfter(existingStart)) {
                throw new RuntimeException("Ya tenés una clase reservada en este horario. No podés reservar clases que se solapen.");
            }
        }

        // Incrementar el contador de inscriptos
        scheduledClass.setEnrolledCount(scheduledClass.getEnrolledCount() + 1);
        scheduledClassRepository.save(scheduledClass);

        // Crear la nueva reserva
        UserBooking newBooking = new UserBooking();
        newBooking.setUserId(userId);
        newBooking.setScheduledClassId(scheduledClass.getId());
        newBooking.setCreationDate(LocalDateTime.now());
        newBooking.setStatus(BookingStatus.CONFIRMED);
        newBooking.setClassName(scheduledClass.getName());
        newBooking.setClassDateTime(scheduledClass.getDateTime());
        newBooking.setLocation(scheduledClass.getLocation());
        newBooking.setDurationMinutes(scheduledClass.getDurationMinutes());

        UserBooking savedBooking = bookingRepository.save(newBooking);

        return mapToBookingResponse(savedBooking);
    }

    public List<UserBookingDto> getAllByUserId(String userId) {
        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("Usuario no encontrado con ID: " + userId);
        }

        // Actualizar reservas expiradas antes de consultar
        updateExpiredBookings(userId);

        List<UserBooking> bookings = bookingRepository.findAllByUserId(userId);

        // Filtrar solo reservas futuras y confirmadas
        LocalDateTime now = LocalDateTime.now();
        List<UserBookingDto> result = bookings.stream()
                .filter(booking -> booking.getClassDateTime().isAfter(now))
                .filter(booking -> booking.getStatus() == BookingStatus.CONFIRMED)
                .map(this::mapToUserBookingDto)
                .collect(Collectors.toList());
        
        System.out.println("===== getAllByUserId =====");
        System.out.println("UserId: " + userId);
        System.out.println("Total bookings from DB: " + bookings.size());
        System.out.println("Filtered bookings (future + confirmed): " + result.size());
        result.forEach(dto -> System.out.println("  - " + dto.getBookingId() + " | " + dto.getClassName() + " | " + dto.getClassDateTime()));
        System.out.println("==========================");
        
        return result;
    }

    public List<UserBookingDto> getHistoryByUserId(String userId) {
        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("Usuario no encontrado con ID: " + userId);
        }

        // Actualizar reservas expiradas antes de consultar
        updateExpiredBookings(userId);

        List<UserBooking> bookings = bookingRepository.findAllByUserId(userId);

        // Retornar todas las reservas pasadas o canceladas/expiradas
        LocalDateTime now = LocalDateTime.now();
        return bookings.stream()
                .filter(booking -> 
                    booking.getClassDateTime().isBefore(now) || 
                    booking.getStatus() == BookingStatus.CANCELLED || 
                    booking.getStatus() == BookingStatus.EXPIRED
                )
                .sorted((a, b) -> b.getClassDateTime().compareTo(a.getClassDateTime())) // Más recientes primero
                .map(this::mapToUserBookingDto)
                .collect(Collectors.toList());
    }

    @Transactional
    protected void updateExpiredBookings(String userId) {
        LocalDateTime now = LocalDateTime.now();
        List<UserBooking> confirmedBookings = bookingRepository.findAllByUserIdAndStatus(userId, BookingStatus.CONFIRMED);
        
        for (UserBooking booking : confirmedBookings) {
            // Si la clase ya pasó, marcar como expirada
            if (booking.getClassDateTime().isBefore(now)) {
                booking.setStatus(BookingStatus.EXPIRED);
                bookingRepository.save(booking);
            }
        }
    }

    @Transactional
    public void cancel(String bookingId, String userId) {
        UserBooking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Reserva no encontrada con ID: " + bookingId));

        // Validaciones
        if (!booking.getUserId().equals(userId)) {
            throw new RuntimeException("Acceso denegado. No tienes permiso para cancelar esta reserva.");
        }

        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new RuntimeException("Solo se pueden cancelar reservas confirmadas.");
        }

        // Buscamos la clase agendada
        ScheduledClass scheduledClass = scheduledClassRepository.findById(booking.getScheduledClassId())
                .orElseThrow(() -> new RuntimeException("La clase agendada asociada a la reserva no fue encontrada."));

        if (scheduledClass.getEnrolledCount() > 0) {
            scheduledClass.setEnrolledCount(scheduledClass.getEnrolledCount() - 1);
        }

        booking.setStatus(BookingStatus.CANCELLED);

        scheduledClassRepository.save(scheduledClass);
        bookingRepository.save(booking);
    }

    private UserBookingDto mapToUserBookingDto(UserBooking booking) {
        // Obtener información adicional desde la clase agendada si es necesario
        ScheduledClass scheduledClass = scheduledClassRepository.findById(booking.getScheduledClassId())
                .orElse(null);
        
        String professor = booking.getClassName(); // Fallback
        String location = booking.getLocation();
        Integer durationMinutes = booking.getDurationMinutes();
        
        if (scheduledClass != null) {
            professor = scheduledClass.getProfessor();
            if (location == null) {
                location = scheduledClass.getLocation();
            }
            if (durationMinutes == null) {
                durationMinutes = scheduledClass.getDurationMinutes();
            }
        }
        
        return new UserBookingDto(
                booking.getId(),
                booking.getScheduledClassId(),
                booking.getClassName(),
                booking.getClassDateTime(),
                professor != null ? professor : "Profesor no disponible",
                location != null ? location : "Sede no disponible",
                durationMinutes != null ? durationMinutes : 60,
                booking.getStatus().toString()
        );
    }

    private BookingResponse mapToBookingResponse(UserBooking booking) {
        BookingResponse dto = new BookingResponse();
        dto.setId(booking.getId());
        dto.setClassDateTime(booking.getClassDateTime());
        dto.setStatus(booking.getStatus().toString());
        dto.setClassName(booking.getClassName());
        return dto;
    }
}