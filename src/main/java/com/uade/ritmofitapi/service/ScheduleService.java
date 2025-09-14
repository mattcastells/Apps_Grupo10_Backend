package com.uade.ritmofitapi.service;

import com.uade.ritmofitapi.dto.response.ScheduledClassDto;
import com.uade.ritmofitapi.model.ScheduledClass;
import com.uade.ritmofitapi.repository.ScheduledClassRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ScheduleService {

    private final ScheduledClassRepository scheduledClassRepository;

    public ScheduleService(ScheduledClassRepository scheduledClassRepository) {
        this.scheduledClassRepository = scheduledClassRepository;
    }

    public List<ScheduledClassDto> getWeeklySchedule() {
        LocalDateTime startOfWeek = LocalDate.now().atStartOfDay();
        LocalDateTime endOfWeek = startOfWeek.plusDays(7);

        List<ScheduledClass> classes = scheduledClassRepository.findAllByDateTimeBetween(startOfWeek, endOfWeek);

        return classes.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    private ScheduledClassDto mapToDto(ScheduledClass scheduledClass) {
        int availableSlots = scheduledClass.getCapacity() - scheduledClass.getEnrolledCount();
        return new ScheduledClassDto(
                scheduledClass.getId(),
                scheduledClass.getName(),
                scheduledClass.getProfessor(),
                scheduledClass.getDateTime(),
                scheduledClass.getDurationMinutes(),
                availableSlots
        );
    }
}