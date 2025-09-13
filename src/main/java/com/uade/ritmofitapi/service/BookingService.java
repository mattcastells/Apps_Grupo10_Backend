package com.uade.ritmofitapi.service;

import com.uade.ritmofitapi.dto.BookingResponse;
import com.uade.ritmofitapi.dto.BookingRequest;

import java.util.List;

import com.uade.ritmofitapi.model.GymClass;
import com.uade.ritmofitapi.model.booking.Booking;
import com.uade.ritmofitapi.model.booking.BookingStatus;
import com.uade.ritmofitapi.repository.BookingRepository;
import com.uade.ritmofitapi.repository.GymClassRepository;
import com.uade.ritmofitapi.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final GymClassRepository gymClassRepository;
    private final UserRepository userRepository;

    public BookingService(BookingRepository bookingRepository,
                              GymClassRepository gymClassRepository,
                              UserRepository userRepository) {
        this.bookingRepository = bookingRepository;
        this.gymClassRepository = gymClassRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public BookingResponse create(BookingRequest bookingRequest, String userId) {

        // 1. Validacion de Existencia
        GymClass gymClass = gymClassRepository.findById(bookingRequest.getClaseId())
                .orElseThrow(() -> new RuntimeException("Clase no encontrada"));

        userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // 2. Validaciones Funcionales
        // a) El usuario no puede inscribirse dos veces a la misma clase
        boolean alreadyBooked = bookingRepository.existsByClaseIdAndUsuarioIdAndFechaHoraClase(
                gymClass.getId(), userId, bookingRequest.getFechaHoraClase()
        );
        if (alreadyBooked) {
            throw new RuntimeException("Ya tienes una reserva para esta gymClass.");
        }
        // a) Validar cupo máximo
        long currentBookings = bookingRepository.countByClaseIdAndFechaHoraClaseAndEstado(
                gymClass.getId(), bookingRequest.getFechaHoraClase(), Booking.EstadoBooking.CONFIRMADA
        );
        if (currentBookings >= gymClass.getCupoMaximo()) {
            throw new RuntimeException("No hay cupos disponibles para esta clase.");
        }


        // 3. Creación de la Reserva
        Booking newBooking = new Booking();
        newBooking.setUserId(userId);
        newBooking.setClassId(classId);
        newBooking.setFechaHoraClase(bookingRequest.getFechaHoraClase());
        newBooking.setBookingStatus(BookingStatus.CONFIRMED);
        newBooking.setCreationDate(LocalDateTime.now());
        Booking savedBooking = bookingRepository.save(newBooking);

        return mapToBookingResponse(savedBooking, gymClass);
    }


    @Transactional
    public void cancel(String bookingId, String userId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Reserva no encontrada"));

        // ¡Validación de seguridad crítica!
        // Un usuario solo puede cancelar sus propias reservas.
        if (!booking.getUsuarioId().equals(userId)) {
            throw new SecurityException("No tienes permiso para cancelar esta reserva.");
        }

        booking.setEstado(Booking.EstadoBooking.CANCELADA);
        bookingRepository.save(booking);
    }

    @Override
    public List<BookingResponse> findUpcomingByUser(String userId) {
        // Lógica para buscar reservas futuras y confirmadas
        List<Booking> bookings = bookingRepository.findByUsuarioIdAndEstadoAndFechaHoraClaseAfter(
                userId, Booking.EstadoBooking.CONFIRMADA, LocalDateTime.now()
        );

        return bookings.stream()
                .map(booking -> {
                    // Podemos enriquecer la respuesta consultando la clase
                    Clase clase = gymClassRepository.findById(booking.getClaseId()).orElse(null);
                    return mapToBookingResponse(booking, clase);
                })
                .collect(Collectors.toList());
    }

    // Método helper para no repetir el código de mapeo
    private BookingResponse mapToBookingResponse(Booking booking, Clase clase) {
        BookingResponse dto = new BookingResponse();
        dto.setId(booking.getId());
        dto.setFechaHoraClase(booking.getFechaHoraClase());
        dto.setEstado(booking.getEstado().toString());
        if (clase != null) {
            dto.setClaseNombre(clase.getNombre());
            // Aquí podríamos buscar el nombre de la sede también si fuera necesario
            // dto.setSedeNombre(...); 
        }
        return dto;
    }
}