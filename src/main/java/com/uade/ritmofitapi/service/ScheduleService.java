package com.uade.ritmofitapi.service;

import com.uade.ritmofitapi.dto.response.ScheduledClassDto;
import com.uade.ritmofitapi.model.ScheduledClass;
import com.uade.ritmofitapi.repository.ScheduledClassRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
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

    public ScheduledClassDto getClassDetail(String classId) {
        Optional<ScheduledClass> scheduledClass = scheduledClassRepository.findById(classId);

        if (scheduledClass.isEmpty()) {
            throw new RuntimeException("Clase no encontrada con ID: " + classId);
        }

        return mapToDto(scheduledClass.get());
    }

    /**
     * Obtener clases con filtros opcionales
     * @param locationId ID de la sede (opcional)
     * @param discipline Nombre de la disciplina (opcional)
     * @param fromDate Fecha desde en formato dd-MM-yyyy (opcional)
     * @param toDate Fecha hasta en formato dd-MM-yyyy (opcional)
     * @return Lista de clases filtradas
     */
    public List<ScheduledClassDto> getFilteredSchedule(String locationId, String discipline, String fromDate, String toDate) {
        // Establecer rango de fechas por defecto
        LocalDateTime startDate = LocalDate.now().atStartOfDay();
        LocalDateTime endDate = startDate.plusDays(30); // Por defecto próximos 30 días

        // Parsear fechas si se proporcionan (formato dd-MM-yyyy)
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        if (fromDate != null && !fromDate.isBlank()) {
            try {
                startDate = LocalDate.parse(fromDate, formatter).atStartOfDay();
            } catch (Exception e) {
                throw new IllegalArgumentException("Formato de fecha 'from' inválido. Use dd-MM-yyyy");
            }
        }
        if (toDate != null && !toDate.isBlank()) {
            try {
                endDate = LocalDate.parse(toDate, formatter).atTime(23, 59, 59);
            } catch (Exception e) {
                throw new IllegalArgumentException("Formato de fecha 'to' inválido. Use dd-MM-yyyy");
            }
        }

        // Obtener todas las clases en el rango de fechas
        List<ScheduledClass> classes = scheduledClassRepository.findAllByDateTimeBetween(startDate, endDate);

        // Aplicar filtros opcionales
        return classes.stream()
                .filter(c -> locationId == null || locationId.isBlank() || locationId.equals(c.getLocationId()))
                .filter(c -> discipline == null || discipline.isBlank() || discipline.equalsIgnoreCase(c.getDiscipline()))
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