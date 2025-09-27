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
        
        // Filter only confirmed bookings for past classes
        List<UserBooking> pastBookings = bookings.stream()
            .filter(booking -> booking.getStatus() == BookingStatus.CONFIRMED)
            .filter(booking -> booking.getClassDateTime().isBefore(LocalDateTime.now()))
            .collect(Collectors.toList());
        
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
        } else {
            response.setTeacher("Profesor");
            response.setDurationMinutes(60);
        }
        
        response.setSite("Sede Centro"); // TODO: Get from configuration
        response.setLocation("Av. ... 123"); // TODO: Get from configuration
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
        } else {
            response.setTeacher("Profesor");
            response.setDurationMinutes(60);
        }
        
        response.setSite("Sede Centro"); // TODO: Get from configuration
        response.setLocation("Av. ... 123"); // TODO: Get from configuration
        response.setStartDateTime(booking.getClassDateTime().format(dateTimeFormatter));
        response.setAttendanceStatus(booking.getStatus().name());
        response.setUserReview(null); // TODO: Implement review system
        
        return response;
    }
}
