package com.uade.ritmofitapi.service;

import com.uade.ritmofitapi.dto.request.CreateScheduledClassRequest;
import com.uade.ritmofitapi.dto.response.ScheduledClassDto;
import com.uade.ritmofitapi.model.Location;
import com.uade.ritmofitapi.model.Notification;
import com.uade.ritmofitapi.model.ScheduledClass;
import com.uade.ritmofitapi.model.booking.UserBooking;
import com.uade.ritmofitapi.repository.BookingRepository;
import com.uade.ritmofitapi.repository.LocationRepository;
import com.uade.ritmofitapi.repository.ScheduledClassRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ScheduleService {

    private final ScheduledClassRepository scheduledClassRepository;
    private final LocationRepository locationRepository;
    private final BookingRepository bookingRepository;
    private final NotificationService notificationService;

    public ScheduleService(ScheduledClassRepository scheduledClassRepository,
                           LocationRepository locationRepository,
                           BookingRepository bookingRepository,
                           NotificationService notificationService) {
        this.scheduledClassRepository = scheduledClassRepository;
        this.locationRepository = locationRepository;
        this.bookingRepository = bookingRepository;
        this.notificationService = notificationService;
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
        scheduledClass.setDescription(request.getDescription());
        
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

        StringBuilder changesDescription = new StringBuilder();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        LocalDateTime newDateTime = LocalDateTime.parse(request.getDateTime());
        if (!Objects.equals(existingClass.getDateTime(), newDateTime)) {
            changesDescription.append(String.format("Nuevo horario: %s. ", newDateTime.format(formatter)));
            existingClass.setDateTime(newDateTime);
        }

        if (!Objects.equals(existingClass.getDiscipline(), request.getDiscipline())) {
            changesDescription.append(String.format("Nueva disciplina: %s. ", request.getDiscipline()));
            existingClass.setDiscipline(request.getDiscipline());
        }

        if (!Objects.equals(existingClass.getProfessor(), request.getProfessor())) {
            changesDescription.append(String.format("Nuevo profesor: %s. ", request.getProfessor()));
            existingClass.setProfessor(request.getProfessor());
        }

        if (!Objects.equals(existingClass.getDurationMinutes(), request.getDurationMinutes())) {
            changesDescription.append(String.format("Nueva duración: %d minutos. ", request.getDurationMinutes()));
            existingClass.setDurationMinutes(request.getDurationMinutes());
        }

        if (!Objects.equals(existingClass.getCapacity(), request.getCapacity())) {
            changesDescription.append(String.format("Nueva capacidad: %d. ", request.getCapacity()));
            existingClass.setCapacity(request.getCapacity());
        }

        if (!Objects.equals(existingClass.getLocationId(), request.getLocationId())) {
            Location location = locationRepository.findById(request.getLocationId())
                    .orElseThrow(() -> new RuntimeException("Sede no encontrada con ID: " + request.getLocationId()));
            changesDescription.append(String.format("Nueva sede: %s. ", location.getName()));
            existingClass.setLocationId(request.getLocationId());
            existingClass.setLocation(location.getName());
        }

        if (!Objects.equals(existingClass.getDescription(), request.getDescription())) {
            changesDescription.append("Descripción actualizada. ");
            existingClass.setDescription(request.getDescription());
        }

        if (changesDescription.length() > 0) {
            scheduledClassRepository.save(existingClass);
            List<UserBooking> activeBookings = bookingRepository.findAllByScheduledClassIdAndStatus(
                    classId, com.uade.ritmofitapi.model.booking.BookingStatus.CONFIRMED);

            log.info("📝 Class {} updated. Notifying {} users", classId, activeBookings.size());

            for (UserBooking booking : activeBookings) {
                String title = "⚠️ Cambio en tu Clase";
                String message = String.format(
                        "La clase de %s ha sido modificada. %s",
                        existingClass.getDiscipline(),
                        changesDescription.toString());

                notificationService.createNotification(
                        booking.getUserId(),
                        Notification.NotificationType.CLASS_CHANGED,
                        title,
                        message,
                        LocalDateTime.now(),
                        booking.getId(),
                        null
                );
            }
        }

        return existingClass;
    }

    public ScheduledClassDto getClassDetail(String classId) {
        Optional<ScheduledClass> scheduledClass = scheduledClassRepository.findById(classId);

        if (scheduledClass.isEmpty()) {
            throw new RuntimeException("Clase no encontrada con ID: " + classId);
        }

        return mapToDto(scheduledClass.get());
    }

    public List<ScheduledClassDto> getFilteredSchedule(String locationId, String discipline, String fromDate,
                                                       String toDate) {
        LocalDateTime startDate = LocalDate.now().atStartOfDay();
        LocalDateTime endDate = startDate.plusDays(30);

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

        List<ScheduledClass> classes = scheduledClassRepository.findAllByDateTimeBetween(startDate, endDate);

        return classes.stream()
                .filter(c -> locationId == null || locationId.isBlank() || locationId.equals(c.getLocationId()))
                .filter(c -> discipline == null || discipline.isBlank()
                        || discipline.equalsIgnoreCase(c.getDiscipline()))
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    private ScheduledClassDto mapToDto(ScheduledClass scheduledClass) {
        int availableSlots = scheduledClass.getCapacity() - scheduledClass.getEnrolledCount();

        String locationName = "Sede no disponible";
        String locationAddress = null;

        if (scheduledClass.getLocationId() != null) {
            var location = locationRepository.findById(scheduledClass.getLocationId());
            if (location.isPresent()) {
                locationName = location.get().getName();
                locationAddress = location.get().getAddress();
            }
        }

        return new ScheduledClassDto(
                scheduledClass.getId(),
                scheduledClass.getProfessor(),
                scheduledClass.getDiscipline(),
                locationName,
                locationAddress,
                scheduledClass.getDateTime(),
                scheduledClass.getDurationMinutes(),
                availableSlots,
                scheduledClass.getCapacity(),
                scheduledClass.getDescription()
        );
    }

    public ScheduledClass cancelClass(String classId) {
        ScheduledClass scheduledClass = scheduledClassRepository.findById(classId)
                .orElseThrow(() -> new RuntimeException("Clase no encontrada"));

        scheduledClass.setCancelled(true);
        scheduledClassRepository.save(scheduledClass);

        List<UserBooking> activeBookings = bookingRepository.findAllByScheduledClassIdAndStatus(
                classId,
                com.uade.ritmofitapi.model.booking.BookingStatus.CONFIRMED);

        log.info("🚫 Class {} cancelled. Notifying {} users", classId, activeBookings.size());

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        for (UserBooking booking : activeBookings) {
            String title = "❌ Clase Cancelada";
            String message = String.format(
                    "La clase de %s programada para el %s ha sido cancelada. Por favor revisa tu agenda.",
                    scheduledClass.getDiscipline(),
                    scheduledClass.getDateTime().format(formatter));

            notificationService.createNotification(
                    booking.getUserId(),
                    Notification.NotificationType.BOOKING_CANCELLED,
                    title,
                    message,
                    LocalDateTime.now(),
                    booking.getId(),
                    null
            );
        }

        return scheduledClass;
    }
}
