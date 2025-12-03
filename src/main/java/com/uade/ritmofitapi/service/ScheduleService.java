package com.uade.ritmofitapi.service;

import com.uade.ritmofitapi.dto.request.CreateScheduledClassRequest;
import com.uade.ritmofitapi.dto.response.ScheduledClassDto;
import com.uade.ritmofitapi.model.Location;
import com.uade.ritmofitapi.model.ScheduledClass;
import com.uade.ritmofitapi.repository.LocationRepository;
import com.uade.ritmofitapi.repository.ScheduledClassRepository;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ScheduleService {

    private final ScheduledClassRepository scheduledClassRepository;
    private final LocationRepository locationRepository;

    public ScheduleService(ScheduledClassRepository scheduledClassRepository,
                           LocationRepository locationRepository) {
        this.scheduledClassRepository = scheduledClassRepository;
        this.locationRepository = locationRepository;
    }

    public List<ScheduledClassDto> getWeeklySchedule() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endOfMonth = now.plusDays(30); // Mostrar próximos 30 días en lugar de solo 7

        log.info("🔍 Buscando clases entre {} y {}", now, endOfMonth);
        List<ScheduledClass> classes = scheduledClassRepository.findAllByDateTimeBetween(now, endOfMonth);
        log.info("✅ Encontradas {} clases en el rango", classes.size());

        return classes.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }
    
    public ScheduledClass createScheduledClass(CreateScheduledClassRequest request) {
        log.info("🔧 Creando clase programada: discipline={}, professor={}, date={}", 
                 request.getDiscipline(), request.getProfessor(), request.getDateTime());
        
        // Parse datetime
        LocalDateTime dateTime = LocalDateTime.parse(request.getDateTime());
        log.info("📅 Fecha parseada: {}", dateTime);
        
        // Validate minimum 1 hour separation between classes for the same professor
        // The new class cannot start until 1 hour after any existing class starts
        boolean hasConflict = scheduledClassRepository.findAll().stream()
                .filter(c -> c.getProfessor() != null && c.getProfessor().equalsIgnoreCase(request.getProfessor()))
                .anyMatch(existingClass -> {
                    LocalDateTime existingStart = existingClass.getDateTime();
                    
                    // Calculate absolute difference in hours between start times
                    long hoursDiff = Math.abs(java.time.Duration.between(existingStart, dateTime).toHours());
                    
                    boolean tooClose = hoursDiff < 1;
                    
                    if (tooClose) {
                        log.warn("⚠️ Conflicto detectado con clase existente ID: {} (inicia: {}). Nueva clase: {}. Diferencia: {} horas", 
                                existingClass.getId(), existingStart, dateTime, hoursDiff);
                    }
                    
                    return tooClose;
                });
        
        if (hasConflict) {
            log.error("❌ El profesor {} ya tiene una clase dentro del bloque de 1 hora", request.getProfessor());
            throw new RuntimeException("Ya tenés una clase programada dentro del bloque de 1 hora. Debe haber al menos 1 hora de separación entre los horarios de inicio.");
        }
        
        // Get location
        Location location = locationRepository.findById(request.getLocationId())
                .orElseThrow(() -> {
                    log.error("❌ Sede no encontrada con ID: {}", request.getLocationId());
                    return new RuntimeException("Sede no encontrada con ID: " + request.getLocationId());
                });
        log.info("📍 Sede encontrada: {}", location.getName());
        
        // Create scheduled class
        ScheduledClass scheduledClass = new ScheduledClass();
        scheduledClass.setDiscipline(request.getDiscipline());
        scheduledClass.setProfessor(request.getProfessor());
        scheduledClass.setDurationMinutes(request.getDurationMinutes());
        scheduledClass.setCapacity(request.getCapacity());
        scheduledClass.setLocationId(request.getLocationId());
        scheduledClass.setLocation(location.getName());
        scheduledClass.setDateTime(dateTime);
        scheduledClass.setEnrolledCount(0);
        
        ScheduledClass saved = scheduledClassRepository.save(scheduledClass);
        log.info("💾 Clase guardada en BD con ID: {}", saved.getId());
        return saved;
    }
    
    public List<ScheduledClassDto> getClassesByProfessor(String professorName) {
        // Get all future classes for this professor
        LocalDateTime now = LocalDateTime.now();
        List<ScheduledClass> classes = scheduledClassRepository.findAll().stream()
                .filter(c -> c.getProfessor() != null && c.getProfessor().equalsIgnoreCase(professorName))
                .filter(c -> c.getDateTime().isAfter(now))
                .sorted((c1, c2) -> c1.getDateTime().compareTo(c2.getDateTime()))
                .collect(Collectors.toList());
        
        return classes.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }
    
    public ScheduledClass updateScheduledClass(String classId, CreateScheduledClassRequest request) {
        // Find existing class
        ScheduledClass existingClass = scheduledClassRepository.findById(classId)
                .orElseThrow(() -> new RuntimeException("Clase no encontrada con ID: " + classId));
        
        // Parse datetime
        LocalDateTime dateTime = LocalDateTime.parse(request.getDateTime());
        
        // Validate minimum 1 hour separation between classes for the same professor (excluding current class)
        // The class cannot start until 1 hour after any other class starts
        boolean hasConflict = scheduledClassRepository.findAll().stream()
                .filter(c -> !c.getId().equals(classId)) // Exclude current class being edited
                .filter(c -> c.getProfessor() != null && c.getProfessor().equalsIgnoreCase(request.getProfessor()))
                .anyMatch(existingOtherClass -> {
                    LocalDateTime existingStart = existingOtherClass.getDateTime();
                    
                    // Calculate absolute difference in hours between start times
                    long hoursDiff = Math.abs(java.time.Duration.between(existingStart, dateTime).toHours());
                    
                    boolean tooClose = hoursDiff < 1;
                    
                    if (tooClose) {
                        log.warn("⚠️ Conflicto detectado con clase existente ID: {} (inicia: {}). Clase editada: {}. Diferencia: {} horas", 
                                existingOtherClass.getId(), existingStart, dateTime, hoursDiff);
                    }
                    
                    return tooClose;
                });
        
        if (hasConflict) {
            log.error("❌ El profesor {} ya tiene una clase dentro del bloque de 1 hora", request.getProfessor());
            throw new RuntimeException("Ya tenés una clase programada dentro del bloque de 1 hora. Debe haber al menos 1 hora de separación entre los horarios de inicio.");
        }
        
        // Get location
        Location location = locationRepository.findById(request.getLocationId())
                .orElseThrow(() -> new RuntimeException("Sede no encontrada con ID: " + request.getLocationId()));
        
        // Update fields
        existingClass.setDiscipline(request.getDiscipline());
        existingClass.setProfessor(request.getProfessor());
        existingClass.setDurationMinutes(request.getDurationMinutes());
        existingClass.setCapacity(request.getCapacity());
        existingClass.setLocationId(request.getLocationId());
        existingClass.setLocation(location.getName());
        existingClass.setDateTime(dateTime);
        
        return scheduledClassRepository.save(existingClass);
    }
    
    public void deleteScheduledClass(String classId) {
        ScheduledClass existingClass = scheduledClassRepository.findById(classId)
                .orElseThrow(() -> new RuntimeException("Clase no encontrada con ID: " + classId));
        
        scheduledClassRepository.delete(existingClass);
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
                scheduledClass.getProfessor(),
                scheduledClass.getDiscipline(),
                scheduledClass.getLocation(),
                scheduledClass.getDateTime(),
                scheduledClass.getDurationMinutes(),
                availableSlots,
                scheduledClass.getCapacity()
        );
    }
}