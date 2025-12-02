package com.uade.ritmofitapi.service;

import com.uade.ritmofitapi.dto.response.BookingResponse;
import com.uade.ritmofitapi.dto.request.BookingRequest;

import com.uade.ritmofitapi.dto.response.UserBookingDto;
import com.uade.ritmofitapi.exception.AlreadyBookedException;
import com.uade.ritmofitapi.exception.UserNotFoundException;
import com.uade.ritmofitapi.model.ScheduledClass;
import com.uade.ritmofitapi.model.User;
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
    private final EmailService emailService;
    private final com.uade.ritmofitapi.repository.LocationRepository locationRepository;
    private final NotificationService notificationService;

    public BookingService(BookingRepository bookingRepository,
            ScheduledClassRepository scheduledClassRepository,
            UserRepository userRepository,
            EmailService emailService,
            com.uade.ritmofitapi.repository.LocationRepository locationRepository,
            NotificationService notificationService) {
        this.bookingRepository = bookingRepository;
        this.scheduledClassRepository = scheduledClassRepository;
        this.userRepository = userRepository;
        this.emailService = emailService;
        this.locationRepository = locationRepository;
        this.notificationService = notificationService;
    }

    @Transactional
    public BookingResponse create(BookingRequest bookingRequest, String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado con ID: " + userId));

        ScheduledClass scheduledClass = scheduledClassRepository.findById(bookingRequest.getScheduledClassId())
                .orElseThrow(() -> new RuntimeException(
                        "Clase agendada no encontrada con ID: " + bookingRequest.getScheduledClassId()));

        LocalDateTime now = LocalDateTime.now();

        // VALIDACIÓN 1: No permitir reservar clases del pasado
        if (scheduledClass.getDateTime().isBefore(now)) {
            throw new IllegalArgumentException("No se puede reservar una clase que ya pasó.");
        }

        // VALIDACIÓN 2: Mínimo 1 hora de anticipación
        LocalDateTime minimumBookingTime = now.plusHours(1);
        if (scheduledClass.getDateTime().isBefore(minimumBookingTime)) {
            throw new IllegalArgumentException("Debes reservar con al menos 1 hora de anticipación.");
        }

        // VALIDACIÓN 3: Verificar capacidad
        if (scheduledClass.getEnrolledCount() >= scheduledClass.getCapacity()) {
            throw new IllegalStateException("No hay cupos disponibles para esta clase.");
        }

        // VALIDACIÓN 4: Solo bloquear si ya tiene una reserva CONFIRMADA
        if (bookingRepository.existsByUserIdAndScheduledClassIdAndStatus(userId, scheduledClass.getId(),
                BookingStatus.CONFIRMED)) {
            throw new AlreadyBookedException("La clase ya fue reservada por este usuario.");
        }

        // VALIDACIÓN 5: Validar horarios solapados: buscar reservas confirmadas del
        // usuario
        LocalDateTime classStart = scheduledClass.getDateTime();
        LocalDateTime classEnd = classStart.plusMinutes(scheduledClass.getDurationMinutes());

        List<UserBooking> userConfirmedBookings = bookingRepository.findAllByUserIdAndStatus(userId,
                BookingStatus.CONFIRMED);

        for (UserBooking existingBooking : userConfirmedBookings) {
            LocalDateTime existingStart = existingBooking.getClassDateTime();
            LocalDateTime existingEnd = existingStart.plusMinutes(existingBooking.getDurationMinutes());

            // Verificar si hay solapamiento
            if (classStart.isBefore(existingEnd) && classEnd.isAfter(existingStart)) {
                throw new RuntimeException(
                        "Ya tenés una clase reservada en este horario. No podés reservar clases que se solapen.");
            }
        }

        // Incrementar el contador de inscriptos
        scheduledClass.setEnrolledCount(scheduledClass.getEnrolledCount() + 1);
        scheduledClassRepository.save(scheduledClass);

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

        // Crear notificación de recordatorio 1h antes de la clase
        try {
            notificationService.createBookingReminder(
                    userId,
                    savedBooking.getId(),
                    scheduledClass.getId(), // scheduledClassId
                    scheduledClass.getName(),
                    scheduledClass.getDateTime());
        } catch (Exception e) {
            System.err.println("Error creando notificación de recordatorio: " + e.getMessage());
        }

        // Enviar email de confirmación
        try {
            String subject = "Reserva confirmada - " + scheduledClass.getName();
            String body = String.format(
                    "Hola %s,\n\n" +
                            "Tu reserva ha sido confirmada exitosamente.\n\n" +
                            "Detalles de la clase:\n" +
                            "- Clase: %s\n" +
                            "- Profesor: %s\n" +
                            "- Fecha y hora: %s\n" +
                            "- Duración: %d minutos\n\n" +
                            "¡Te esperamos!\n\n" +
                            "RitmoFit",
                    user.getName(),
                    scheduledClass.getName(),
                    scheduledClass.getProfessor(),
                    scheduledClass.getDateTime().toString(),
                    scheduledClass.getDurationMinutes());
            emailService.sendEmail(user.getEmail(), subject, body);
        } catch (Exception e) {
            // Log error pero no fallar la reserva si el email falla
            System.err.println("Error enviando email de confirmación: " + e.getMessage());
        }

        return mapToBookingResponse(savedBooking);
    }

    public List<UserBookingDto> getAllByUserId(String userId) {
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException("Usuario no encontrado con ID: " + userId);
        }

        // Actualizar reservas expiradas antes de consultar
        updateExpiredBookings(userId);

        List<UserBooking> bookings = bookingRepository.findAllByUserId(userId);

        // Filtrar solo reservas futuras y confirmadas
        LocalDateTime now = LocalDateTime.now();
        return bookings.stream()
                .filter(booking -> booking.getClassDateTime().isAfter(now))
                .filter(booking -> booking.getStatus() == BookingStatus.CONFIRMED)
                .map(this::mapToUserBookingDto)
                .collect(Collectors.toList());
    }

    public List<UserBookingDto> getHistoryByUserId(String userId) {
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException("Usuario no encontrado con ID: " + userId);
        }

        // Actualizar reservas expiradas antes de consultar
        updateExpiredBookings(userId);

        List<UserBooking> bookings = bookingRepository.findAllByUserId(userId);

        // Retornar todas las reservas pasadas o canceladas/expiradas
        LocalDateTime now = LocalDateTime.now();
        return bookings.stream()
                .filter(booking -> booking.getClassDateTime().isBefore(now) ||
                        booking.getStatus() == BookingStatus.CANCELLED ||
                        booking.getStatus() == BookingStatus.EXPIRED)
                .sorted((a, b) -> b.getClassDateTime().compareTo(a.getClassDateTime())) // Más recientes primero
                .map(this::mapToUserBookingDto)
                .collect(Collectors.toList());
    }

    @Transactional
    protected void updateExpiredBookings(String userId) {
        LocalDateTime now = LocalDateTime.now();
        List<UserBooking> confirmedBookings = bookingRepository.findAllByUserIdAndStatus(userId,
                BookingStatus.CONFIRMED);

        for (UserBooking booking : confirmedBookings) {
            // Si la clase ya pasó, marcar como expirada
            if (booking.getClassDateTime().isBefore(now)) {
                booking.setStatus(BookingStatus.EXPIRED);
                bookingRepository.save(booking);
            }
        }
    }

    /**
     * Obtener historial completo de reservas (pasadas, futuras, canceladas, etc.)
     */
    public List<UserBookingDto> getAllBookingsHistory(String userId) {
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException("Usuario no encontrado con ID: " + userId);
        }

        List<UserBooking> bookings = bookingRepository.findAllByUserId(userId);

        // Retornar TODAS las reservas sin filtrar
        return bookings.stream()
                .map(this::mapToUserBookingDto)
                .collect(Collectors.toList());
    }

    /**
     * Obtener IDs de clases ya reservadas por el usuario (solo confirmadas y
     * futuras)
     * Para que el frontend pueda filtrar/marcar las clases en el catálogo
     */
    public List<String> getBookedClassIds(String userId) {
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException("Usuario no encontrado con ID: " + userId);
        }

        List<UserBooking> bookings = bookingRepository.findAllByUserId(userId);
        LocalDateTime now = LocalDateTime.now();

        return bookings.stream()
                .filter(booking -> booking.getStatus() == BookingStatus.CONFIRMED)
                .filter(booking -> booking.getClassDateTime().isAfter(now))
                .map(UserBooking::getScheduledClassId)
                .collect(Collectors.toList());
    }

    @Transactional
    public void cancel(String bookingId, String userId) {
        UserBooking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Reserva no encontrada con ID: " + bookingId));

        // VALIDACIÓN 1: Ownership
        if (!booking.getUserId().equals(userId)) {
            throw new RuntimeException("Acceso denegado. No tienes permiso para cancelar esta reserva.");
        }

        // VALIDACIÓN 2: Solo cancelar reservas confirmadas
        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new RuntimeException("Solo se pueden cancelar reservas confirmadas.");
        }

        // Buscamos la clase agendada
        ScheduledClass scheduledClass = scheduledClassRepository.findById(booking.getScheduledClassId())
                .orElseThrow(() -> new RuntimeException("La clase agendada asociada a la reserva no fue encontrada."));

        // VALIDACIÓN 3: No permitir cancelar después de que empiece la clase
        LocalDateTime now = LocalDateTime.now();
        if (scheduledClass.getDateTime().isBefore(now)) {
            throw new RuntimeException("No se puede cancelar una reserva de una clase que ya empezó.");
        }

        if (scheduledClass.getEnrolledCount() > 0) {
            scheduledClass.setEnrolledCount(scheduledClass.getEnrolledCount() - 1);
        }

        booking.setStatus(BookingStatus.CANCELLED);

        scheduledClassRepository.save(scheduledClass);
        bookingRepository.save(booking);

        // Crear notificación de cancelación
        try {
            notificationService.createBookingCancellation(
                    userId,
                    bookingId,
                    scheduledClass.getName());
        } catch (Exception e) {
            System.err.println("Error creando notificación de cancelación: " + e.getMessage());
        }

        // Eliminar notificaciones de recordatorio pendientes para esta reserva
        try {
            notificationService.deleteNotificationsByBooking(bookingId);
        } catch (Exception e) {
            System.err.println("Error eliminando notificaciones pendientes: " + e.getMessage());
        }
    }

    private UserBookingDto mapToUserBookingDto(UserBooking booking) {
        // Obtener datos completos desde la clase agendada
        ScheduledClass scheduledClass = scheduledClassRepository.findById(booking.getScheduledClassId())
                .orElse(null);

        String professor = "Profesor no disponible";
        String location = booking.getLocation() != null ? booking.getLocation() : "Sede no disponible";
        Integer durationMinutes = booking.getDurationMinutes() != null ? booking.getDurationMinutes() : 60;

        if (scheduledClass != null) {
            professor = scheduledClass.getProfessor();
            // Obtener la ubicación desde la clase programada si no está en el booking
            if (booking.getLocation() == null && scheduledClass.getLocationId() != null) {
                location = locationRepository.findById(scheduledClass.getLocationId())
                        .map(loc -> loc.getName())
                        .orElse("Sede no disponible");
            }
            if (booking.getDurationMinutes() == null) {
                durationMinutes = scheduledClass.getDurationMinutes();
            }
        }

        return new UserBookingDto(
                booking.getId(),
                booking.getScheduledClassId(),
                booking.getClassName(),
                booking.getClassDateTime(),
                professor,
                location,
                durationMinutes,
                booking.getStatus().toString());
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