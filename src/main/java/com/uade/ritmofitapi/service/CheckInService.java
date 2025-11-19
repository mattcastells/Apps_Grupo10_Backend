package com.uade.ritmofitapi.service;

import com.uade.ritmofitapi.dto.request.CheckInRequest;
import com.uade.ritmofitapi.dto.response.CheckInResponse;
import com.uade.ritmofitapi.exception.UserNotFoundException;
import com.uade.ritmofitapi.model.ScheduledClass;
import com.uade.ritmofitapi.model.User;
import com.uade.ritmofitapi.model.booking.BookingStatus;
import com.uade.ritmofitapi.model.booking.UserBooking;
import com.uade.ritmofitapi.repository.BookingRepository;
import com.uade.ritmofitapi.repository.ScheduledClassRepository;
import com.uade.ritmofitapi.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class CheckInService {

    private final BookingRepository bookingRepository;
    private final ScheduledClassRepository scheduledClassRepository;
    private final UserRepository userRepository;

    public CheckInService(BookingRepository bookingRepository,
                         ScheduledClassRepository scheduledClassRepository,
                         UserRepository userRepository) {
        this.bookingRepository = bookingRepository;
        this.scheduledClassRepository = scheduledClassRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public CheckInResponse checkIn(CheckInRequest request, String userId) {
        // 1. Verify user exists
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado"));

        // 2. Verify scheduled class exists
        ScheduledClass scheduledClass = scheduledClassRepository.findById(request.getScheduledClassId())
                .orElseThrow(() -> new RuntimeException("Clase no encontrada"));

        // 3. Find user's CONFIRMED booking for this class
        // Check if already attended
        if (bookingRepository.existsByUserIdAndScheduledClassIdAndStatus(
                userId, request.getScheduledClassId(), BookingStatus.ATTENDED)) {
            throw new RuntimeException("ALREADY_CHECKED_IN");
        }

        // Find CONFIRMED booking
        UserBooking booking = bookingRepository
                .findByUserIdAndScheduledClassId(userId, request.getScheduledClassId())
                .filter(b -> b.getStatus() == BookingStatus.CONFIRMED)
                .orElseThrow(() -> new RuntimeException("NO_BOOKING_FOUND"));

        // 5. Verify check-in time window (15 minutes before to 5 minutes after class start)
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime classStart = scheduledClass.getDateTime();
        LocalDateTime earliestCheckIn = classStart.minusMinutes(15);
        LocalDateTime latestCheckIn = classStart.plusMinutes(5);

        if (now.isBefore(earliestCheckIn) || now.isAfter(latestCheckIn)) {
            throw new RuntimeException("INVALID_CHECKIN_TIME");
        }

        // 6. Update booking status to ATTENDED
        booking.setStatus(BookingStatus.ATTENDED);
        bookingRepository.save(booking);

        // 7. Build and return response
        return CheckInResponse.builder()
                .bookingId(booking.getId())
                .className(scheduledClass.getName())
                .classDateTime(scheduledClass.getDateTime())
                .location(scheduledClass.getLocation())
                .professor(scheduledClass.getProfessor())
                .durationMinutes(scheduledClass.getDurationMinutes())
                .status(BookingStatus.ATTENDED.toString())
                .build();
    }
}
