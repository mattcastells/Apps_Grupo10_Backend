package com.uade.ritmofitapi.service;

import com.uade.ritmofitapi.dto.response.HistoryDetailResponse;
import com.uade.ritmofitapi.dto.response.HistoryItemResponse;
import com.uade.ritmofitapi.model.ScheduledClass;
import com.uade.ritmofitapi.model.booking.BookingStatus;
import com.uade.ritmofitapi.model.booking.UserBooking;
import com.uade.ritmofitapi.repository.BookingRepository;
import com.uade.ritmofitapi.repository.ScheduledClassRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class HistoryService {

    private final BookingRepository bookingRepository;
    private final ScheduledClassRepository scheduledClassRepository;
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    public List<HistoryItemResponse> getUserHistory(String userId, String fromDate, String toDate) {
        log.info("Getting history for user {} from {} to {}", userId, fromDate, toDate);
        
        // Parse dates
        LocalDate from = LocalDate.parse(fromDate);
        LocalDate to = LocalDate.parse(toDate);
        
        // Get user bookings within date range
        List<UserBooking> bookings = bookingRepository.findByUserIdAndClassDateTimeBetween(
            userId, 
            from.atStartOfDay(), 
            to.atTime(23, 59, 59)
        );
        
        log.info("Found {} bookings in date range for user {}", bookings.size(), userId);
        
        // Filter bookings for past classes with attendance status
        // Valid statuses: CONFIRMED, EXPIRED (not yet marked), ATTENDED (presente), ABSENT (ausente)
        List<UserBooking> pastBookings = bookings.stream()
            .filter(booking -> {
                boolean isValidStatus = booking.getStatus() == BookingStatus.CONFIRMED 
                                     || booking.getStatus() == BookingStatus.EXPIRED
                                     || booking.getStatus() == BookingStatus.ATTENDED
                                     || booking.getStatus() == BookingStatus.ABSENT;
                boolean isPast = booking.getClassDateTime().isBefore(LocalDateTime.now());
                log.info("Booking {}: class={}, dateTime={}, status={}, isPast={}, isValidStatus={}", 
                    booking.getId(), booking.getClassName(), booking.getClassDateTime(), 
                    booking.getStatus(), isPast, isValidStatus);
                return isValidStatus && isPast;
            })
            .collect(Collectors.toList());
        
        log.info("Filtered to {} past bookings (all attendance statuses)", pastBookings.size());
        
        // Convert to response DTOs
        return pastBookings.stream()
            .map(this::convertToHistoryItemResponse)
            .collect(Collectors.toList());
    }

    public HistoryDetailResponse getAttendanceDetail(String attendanceId) {
        log.info("Getting attendance detail for {}", attendanceId);
        
        UserBooking booking = bookingRepository.findById(attendanceId)
            .orElseThrow(() -> new RuntimeException("Booking not found"));
        
        return convertToHistoryDetailResponse(booking);
    }

    private HistoryItemResponse convertToHistoryItemResponse(UserBooking booking) {
        HistoryItemResponse response = new HistoryItemResponse();
        response.setId(booking.getId());
        response.setDiscipline(booking.getClassName());
        
        // Obtener datos de la clase programada
        ScheduledClass scheduledClass = scheduledClassRepository.findById(booking.getScheduledClassId()).orElse(null);
        if (scheduledClass != null) {
            response.setTeacher(scheduledClass.getProfessor());
            response.setDurationMinutes(scheduledClass.getDurationMinutes());
            response.setSite(scheduledClass.getLocation());
            response.setLocation(scheduledClass.getLocation());
        } else {
            response.setTeacher("Profesor");
            response.setDurationMinutes(60);
            response.setSite(booking.getLocation() != null ? booking.getLocation() : "Sede Centro");
            response.setLocation(booking.getLocation() != null ? booking.getLocation() : "Sede Centro");
        }
        
        response.setStartDateTime(booking.getClassDateTime().format(dateTimeFormatter));
        
        return response;
    }

    private HistoryDetailResponse convertToHistoryDetailResponse(UserBooking booking) {
        HistoryDetailResponse response = new HistoryDetailResponse();
        response.setId(booking.getId());
        response.setDiscipline(booking.getClassName());
        
        // Obtener datos de la clase programada
        ScheduledClass scheduledClass = scheduledClassRepository.findById(booking.getScheduledClassId()).orElse(null);
        if (scheduledClass != null) {
            response.setTeacher(scheduledClass.getProfessor());
            response.setDurationMinutes(scheduledClass.getDurationMinutes());
            response.setSite(scheduledClass.getLocation());
            response.setLocation(scheduledClass.getLocation());
        } else {
            response.setTeacher("Profesor");
            response.setDurationMinutes(60);
            response.setSite(booking.getLocation() != null ? booking.getLocation() : "Sede Centro");
            response.setLocation(booking.getLocation() != null ? booking.getLocation() : "Sede Centro");
        }
        
        response.setStartDateTime(booking.getClassDateTime().format(dateTimeFormatter));
        response.setAttendanceStatus(booking.getStatus().name());
        
        // Buscar calificación si existe
        ClassRating rating = ratingRepository.findByBookingId(booking.getId()).orElse(null);
        if (rating != null) {
            HistoryDetailResponse.Review review = new HistoryDetailResponse.Review();
            review.setRating(rating.getRating());
            review.setComment(rating.getComment());
            review.setCreatedAt(rating.getCreatedAt() != null ? rating.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null);
            response.setUserReview(review);
        } else {
            response.setUserReview(null);
        }
        
        return response;
    }
}
