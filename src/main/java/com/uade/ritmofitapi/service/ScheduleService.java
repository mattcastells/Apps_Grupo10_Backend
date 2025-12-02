package com.uade.ritmofitapi.service;

import com.uade.ritmofitapi.dto.request.UpdateScheduledClassRequest;
import com.uade.ritmofitapi.dto.response.ScheduledClassDto;
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
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
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
     * 
     * @param locationId ID de la sede (opcional)
     * @param discipline Nombre de la disciplina (opcional)
     * @param fromDate   Fecha desde en formato dd-MM-yyyy (opcional)
     * @param toDate     Fecha hasta en formato dd-MM-yyyy (opcional)
     * @return Lista de clases filtradas
     */
    public List<ScheduledClassDto> getFilteredSchedule(String locationId, String discipline, String fromDate,
            String toDate) {
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
                availableSlots);
    }

    /**
     * Cancela una clase programada y notifica a todos los usuarios inscritos
     */
    public ScheduledClass cancelClass(String classId) {
        // 1. Buscar la clase
        ScheduledClass scheduledClass = scheduledClassRepository.findById(classId)
                .orElseThrow(() -> new RuntimeException("Clase no encontrada"));

        // 2. Marcar como cancelada
        scheduledClass.setCancelled(true);
        scheduledClassRepository.save(scheduledClass);

        // 3. Buscar todos los bookings con status CONFIRMED
        List<UserBooking> activeBookings = bookingRepository.findAllByScheduledClassIdAndStatus(
                classId,
                com.uade.ritmofitapi.model.booking.BookingStatus.CONFIRMED);

        log.info("🚫 Class {} cancelled. Notifying {} users", classId, activeBookings.size());

        // 4. Crear notificación para cada usuario
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        for (UserBooking booking : activeBookings) {
            String title = "❌ Clase Cancelada";
            String message = String.format(
                    "La clase de %s programada para el %s ha sido cancelada. Por favor revisa tu agenda.",
                    scheduledClass.getName(),
                    scheduledClass.getDateTime().format(formatter));

            notificationService.createNotification(
                    booking.getUserId(),
                    Notification.NotificationType.BOOKING_CANCELLED,
                    title,
                    message,
                    LocalDateTime.now(), // Enviar inmediatamente
                    booking.getId(),
                    null // No scheduledClassId for cancellations
            );
        }

        return scheduledClass;
    }

    /**
     * Actualiza horario/sede de una clase y notifica a todos los usuarios inscritos
     */
    public ScheduledClass updateClass(String classId, UpdateScheduledClassRequest request) {
        // 1. Buscar la clase
        ScheduledClass scheduledClass = scheduledClassRepository.findById(classId)
                .orElseThrow(() -> new RuntimeException("Clase no encontrada"));

        // Guardar datos originales
        LocalDateTime originalDateTime = scheduledClass.getDateTime();
        String originalLocation = scheduledClass.getLocation();

        // 2. Actualizar campos si vienen en el request
        boolean hasChanges = false;
        StringBuilder changesDescription = new StringBuilder();

        if (request.getNewDateTime() != null && !request.getNewDateTime().equals(originalDateTime)) {
            scheduledClass.setDateTime(request.getNewDateTime());
            hasChanges = true;
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            changesDescription.append(String.format("Nuevo horario: %s", request.getNewDateTime().format(formatter)));
        }

        if (request.getNewLocationId() != null && !request.getNewLocationId().equals(scheduledClass.getLocationId())) {
            scheduledClass.setLocationId(request.getNewLocationId());
            if (request.getNewLocation() != null) {
                scheduledClass.setLocation(request.getNewLocation());
            }
            hasChanges = true;
            if (changesDescription.length() > 0)
                changesDescription.append(". ");
            changesDescription.append(String.format("Nueva sede: %s", request.getNewLocation()));
        }

        if (!hasChanges) {
            throw new IllegalArgumentException("No se proporcionaron cambios para actualizar");
        }

        scheduledClassRepository.save(scheduledClass);

        // 3. Buscar todos los bookings con status CONFIRMED
        List<UserBooking> activeBookings = bookingRepository.findAllByScheduledClassIdAndStatus(
                classId,
                com.uade.ritmofitapi.model.booking.BookingStatus.CONFIRMED);

        log.info("📝 Class {} updated. Notifying {} users", classId, activeBookings.size());

        // 4. Crear notificación para cada usuario
        for (UserBooking booking : activeBookings) {
            String title = "⚠️ Cambio en tu Clase";
            String message = String.format(
                    "La clase de %s ha sido modificada. %s",
                    scheduledClass.getName(),
                    changesDescription.toString());

            notificationService.createNotification(
                    booking.getUserId(),
                    Notification.NotificationType.CLASS_CHANGED,
                    title,
                    message,
                    LocalDateTime.now(), // Enviar inmediatamente
                    booking.getId(),
                    null // No scheduledClassId for class changes
            );
        }

        return scheduledClass;
    }
}