package com.uade.ritmofitapi.service;

import com.uade.ritmofitapi.dto.request.CreateRatingRequest;
import com.uade.ritmofitapi.dto.response.RatingResponse;
import com.uade.ritmofitapi.model.ClassRating;
import com.uade.ritmofitapi.model.booking.UserBooking;
import com.uade.ritmofitapi.repository.BookingRepository;
import com.uade.ritmofitapi.repository.ClassRatingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class RatingService {

    private final ClassRatingRepository ratingRepository;
    private final BookingRepository bookingRepository;

    public RatingResponse createRating(String userId, String bookingId, CreateRatingRequest request) {
        // Validar que el booking existe
        UserBooking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Reserva no encontrada con ID: " + bookingId));

        // Validar que el booking pertenece al usuario
        if (!booking.getUserId().equals(userId)) {
            throw new RuntimeException("No tienes permiso para calificar esta reserva.");
        }

        // Validar que la clase ya terminó (status ATTENDED o EXPIRED después de la fecha)
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime classEndTime = booking.getClassDateTime().plusMinutes(
                booking.getDurationMinutes() != null ? booking.getDurationMinutes() : 60
        );

        if (classEndTime.isAfter(now)) {
            throw new RuntimeException("Solo puedes calificar clases que ya finalizaron.");
        }

        // Validar que no pasaron más de 24 horas desde el final de la clase
        LocalDateTime ratingDeadline = classEndTime.plusHours(24);
        if (now.isAfter(ratingDeadline)) {
            throw new RuntimeException("El plazo para calificar esta clase ha expirado. Solo puedes calificar dentro de las 24 horas posteriores al final de la clase.");
        }

        // Validar que no existe ya una calificación para este booking
        if (ratingRepository.existsByBookingId(bookingId)) {
            throw new RuntimeException("Ya has calificado esta clase.");
        }

        // Crear la calificación
        ClassRating rating = new ClassRating(
                userId,
                bookingId,
                request.getRating(),
                request.getComment()
        );

        ClassRating savedRating = ratingRepository.save(rating);
        log.info("Calificación creada para booking {} por usuario {}", bookingId, userId);

        return mapToRatingResponse(savedRating);
    }

    public RatingResponse getRatingByBookingId(String bookingId) {
        ClassRating rating = ratingRepository.findByBookingId(bookingId)
                .orElse(null);

        if (rating == null) {
            return null;
        }

        return mapToRatingResponse(rating);
    }

    private RatingResponse mapToRatingResponse(ClassRating rating) {
        RatingResponse response = new RatingResponse();
        response.setId(rating.getId());
        response.setRating(rating.getRating());
        response.setComment(rating.getComment());
        response.setCreatedAt(rating.getCreatedAt());
        return response;
    }
}

