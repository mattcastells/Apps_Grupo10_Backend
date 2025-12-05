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
        LocalDateTime endOfMonth = now.plusDays(30);

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

        LocalDateTime dateTime = LocalDateTime.parse(request.getDateTime());
        log.info("📅 Fecha parseada: {}", dateTime);

        // Validate minimum 1 hour separation
        boolean hasConflict = scheduledClassRepository.findAll().stream()
                .filter(c -> c.getProfessor() != null && c.getProfessor().equalsIgnoreCase(request.getProfessor()))
                .anyMatch(existingClass -> {
                    LocalDateTime existingStart = existingClass.getDateTime();
                    long hoursDiff = Math.abs(java.time.Duration.between(existingStart, dateTime).toHours());
                    boolean tooClose = hoursDiff < 1;

                    if (tooClose) {
                        log.warn(
                                "⚠️ Conflicto detectado con clase existente ID: {} (inicia: {}). Nueva clase: {}. Diferencia: {} horas",
                                existingClass.getId(), existingStart, dateTime, hoursDiff);
                    }

                    return tooClose;
                });

        if (hasConflict) {
            log.error("❌ El profesor {} ya tiene una clase dentro del bloque de 1 hora", request.getProfessor());
            throw new RuntimeException(
                    "Ya tenés una clase programada dentro del bloque de 1 hora. Debe haber al menos 1 hora de separación entre los horarios de inicio.");
        }

        Location location = locationRepository.findById(request.getLocationId())
                .orElseThrow(() -> {
                    log.error("❌ Sede no encontrada con ID: {}", request.getLocationId());
                    return new RuntimeException("Sede no encontrada con ID: " + request.getLocationId());
                });
        log.info("📍 Sede encontrada: {}", location.getName());

        ScheduledClass scheduledClass = new ScheduledClass();
        scheduledClass.setName(request.getDiscipline() + " con " + request.getProfessor());
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
        ScheduledClass existingClass = scheduledClassRepository.findById(classId)
                .orElseThrow(() -> new RuntimeException("Clase no encontrada con ID: " + classId));

        LocalDateTime dateTime = LocalDateTime.parse(request.getDateTime());

        // Validate minimum 1 hour separation (excluding current class)
        boolean hasConflict = scheduledClassRepository.findAll().stream()
                .filter(c -> !c.getId().equals(classId))
                .filter(c -> c.getProfessor() != null && c.getProfessor().equalsIgnoreCase(request.getProfessor()))
                .anyMatch(existingOtherClass -> {
                    LocalDateTime existingStart = existingOtherClass.getDateTime();
                    long hoursDiff = Math.abs(java.time.Duration.between(existingStart, dateTime).toHours());
                    boolean tooClose = hoursDiff < 1;

                    if (tooClose) {
                        log.warn(
                                "⚠️ Conflicto detectado con clase existente ID: {} (inicia: {}). Clase editada: {}. Diferencia: {} horas",
                                existingOtherClass.getId(), existingStart, dateTime, hoursDiff);
                    }

                    return tooClose;
                });

        if (hasConflict) {
            log.error("❌ El profesor {} ya tiene una clase dentro del bloque de 1 hora", request.getProfessor());
            throw new RuntimeException(
                    "Ya tenés una clase programada dentro del bloque de 1 hora. Debe haber al menos 1 hora de separación entre los horarios de inicio.");
        }

        Location location = locationRepository.findById(request.getLocationId())
                .orElseThrow(() -> new RuntimeException("Sede no encontrada con ID: " + request.getLocationId()));

        // Track changes for notifications
        StringBuilder changesDescription = new StringBuilder();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        boolean hasChanges = false;

        if (!Objects.equals(existingClass.getDateTime(), dateTime)) {
            changesDescription.append(String.format("Nuevo horario: %s. ", dateTime.format(formatter)));
            hasChanges = true;
        }

        if (!Objects.equals(existingClass.getLocationId(), request.getLocationId())) {
            changesDescription.append(String.format("Nueva sede: %s. ", location.getName()));
            hasChanges = true;
        }

        if (!Objects.equals(existingClass.getDiscipline(), request.getDiscipline())) {
            changesDescription.append(String.format("Nueva disciplina: %s. ", request.getDiscipline()));
            hasChanges = true;
        }

        if (!Objects.equals(existingClass.getProfessor(), request.getProfessor())) {
            changesDescription.append(String.format("Nuevo profesor: %s. ", request.getProfessor()));
            hasChanges = true;
        }

        if (!Objects.equals(existingClass.getDurationMinutes(), request.getDurationMinutes())) {
            changesDescription.append(String.format("Nueva duración: %d minutos. ", request.getDurationMinutes()));
            hasChanges = true;
        }

        // Update all fields
        existingClass.setName(request.getDiscipline() + " con " + request.getProfessor());
        existingClass.setDiscipline(request.getDiscipline());
        existingClass.setProfessor(request.getProfessor());
        existingClass.setDurationMinutes(request.getDurationMinutes());
        existingClass.setCapacity(request.getCapacity());
        existingClass.setLocationId(request.getLocationId());
        existingClass.setLocation(location.getName());
        existingClass.setDateTime(dateTime);
        existingClass.setDescription(request.getDescription());

        ScheduledClass saved = scheduledClassRepository.save(existingClass);

        // Send notifications if there are significant changes
        if (hasChanges) {
            List<UserBooking> activeBookings = bookingRepository.findAllByScheduledClassIdAndStatus(
                    classId, com.uade.ritmofitapi.model.booking.BookingStatus.CONFIRMED);

            log.info("📝 Class {} updated. Notifying {} users about changes", classId, activeBookings.size());

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
                        null);
            }
        }

        return saved;
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
                scheduledClass.getName(),
                scheduledClass.getProfessor(),
                scheduledClass.getDiscipline(),
                locationName,
                locationAddress,
                scheduledClass.getDateTime(),
                scheduledClass.getDurationMinutes(),
                availableSlots,
                scheduledClass.getCapacity(),
                scheduledClass.getDescription());
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
                    null);
        }

        return scheduledClass;
    }
}
