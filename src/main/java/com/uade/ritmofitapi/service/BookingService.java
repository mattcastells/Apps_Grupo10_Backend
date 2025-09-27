package com.uade.ritmofitapi.service;

import com.uade.ritmofitapi.dto.response.BookingResponse;
import com.uade.ritmofitapi.dto.request.BookingRequest;

import com.uade.ritmofitapi.dto.response.UserBookingDto;
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

    public BookingService(BookingRepository bookingRepository,
                          ScheduledClassRepository scheduledClassRepository,
                          UserRepository userRepository) {
        this.bookingRepository = bookingRepository;
        this.scheduledClassRepository = scheduledClassRepository;
        this.userRepository = userRepository;
    }

    public BookingResponse create(BookingRequest bookingRequest, String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + userId));

        ScheduledClass scheduledClass = scheduledClassRepository.findById(bookingRequest.getScheduledClassId())
                .orElseThrow(() -> new RuntimeException("Clase agendada no encontrada con ID: " + bookingRequest.getScheduledClassId()));

        if (scheduledClass.getEnrolledCount() >= scheduledClass.getCapacity()) {
            throw new RuntimeException("No hay cupos disponibles para esta clase.");
        }

        if (bookingRepository.existsByUserIdAndScheduledClassId(userId, scheduledClass.getId())) {
            throw new RuntimeException("El usuario ya tiene una reserva para esta clase.");
        }

        scheduledClass.setEnrolledCount(scheduledClass.getEnrolledCount() + 1);
        scheduledClassRepository.save(scheduledClass);

        UserBooking newBooking = new UserBooking();
        newBooking.setUserId(userId);
        newBooking.setScheduledClassId(scheduledClass.getId());
        newBooking.setCreationDate(LocalDateTime.now());
        newBooking.setStatus(BookingStatus.CONFIRMED);
        newBooking.setClassName(scheduledClass.getName());
        newBooking.setClassDateTime(scheduledClass.getDateTime());

        UserBooking savedBooking = bookingRepository.save(newBooking);

        return mapToBookingResponse(savedBooking);
    }

    public List<UserBookingDto> getAllByUserId(String userId) {
        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("Usuario no encontrado con ID: " + userId);
        }

        List<UserBooking> bookings = bookingRepository.findAllByUserId(userId);

        // Filtrar solo reservas futuras y confirmadas
        LocalDateTime now = LocalDateTime.now();
        return bookings.stream()
                .filter(booking -> booking.getClassDateTime().isAfter(now))
                .filter(booking -> booking.getStatus() == BookingStatus.CONFIRMED)
                .map(this::mapToUserBookingDto)
                .collect(Collectors.toList());
    }

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

        // Actualizamos el contador y el estado de la reserva
        scheduledClass.setEnrolledCount(scheduledClass.getEnrolledCount() - 1);
        booking.setStatus(BookingStatus.CANCELLED);

        scheduledClassRepository.save(scheduledClass);
        bookingRepository.save(booking);
    }

    private UserBookingDto mapToUserBookingDto(UserBooking booking) {
        // Obtener el profesor desde la clase agendada
        String professor = scheduledClassRepository.findById(booking.getScheduledClassId())
                .map(ScheduledClass::getProfessor)
                .orElse("Profesor no disponible");
        
        return new UserBookingDto(
                booking.getId(),
                booking.getClassName(),
                booking.getClassDateTime(),
                professor,
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