package com.uade.ritmofitapi.controller;

import com.uade.ritmofitapi.model.ClassTemplate;
import com.uade.ritmofitapi.model.Location;
import com.uade.ritmofitapi.model.ScheduledClass;
import com.uade.ritmofitapi.model.User;
import com.uade.ritmofitapi.model.booking.BookingStatus;
import com.uade.ritmofitapi.model.booking.UserBooking;
import com.uade.ritmofitapi.repository.BookingRepository;
import com.uade.ritmofitapi.repository.ClassTemplateRepository;
import com.uade.ritmofitapi.repository.LocationRepository;
import com.uade.ritmofitapi.repository.ScheduledClassRepository;
import com.uade.ritmofitapi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/v1/mock")
@RequiredArgsConstructor
public class MockDataController {

    private final UserRepository userRepository;
    private final ScheduledClassRepository scheduledClassRepository;
    private final BookingRepository bookingRepository;
    private final ClassTemplateRepository classTemplateRepository;
    private final LocationRepository locationRepository;

    @PostMapping("/history")
    public ResponseEntity<String> createHistoryData(@RequestParam(required = false) String email) {
        try {
            String userEmail = email != null ? email : "antonio@uade.edu.ar";
            
            // Buscar usuario
            User user = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + userEmail));

            // Eliminar bookings existentes del usuario para empezar limpio
            List<UserBooking> existingBookings = bookingRepository.findAll()
                    .stream()
                    .filter(b -> b.getUserId().equals(user.getId()))
                    .collect(Collectors.toList());
            bookingRepository.deleteAll(existingBookings);
            log.info("Eliminadas {} reservas existentes de {}", existingBookings.size(), userEmail);

            // Crear nuevos datos de historial
            createHistoryBookings(user);

            return ResponseEntity.ok("Datos mock de historial creados para " + userEmail + " exitosamente");
        } catch (Exception e) {
            log.error("Error creando datos mock de historial", e);
            return ResponseEntity.internalServerError()
                    .body("Error: " + e.getMessage());
        }
    }

    private void createHistoryBookings(User user) {
        LocalDate today = LocalDate.now();
        List<ScheduledClass> allClasses = scheduledClassRepository.findAll();
        Random random = new Random();

        // Si es Antonio, crear datos en diferentes rangos de fechas
        if (user.getEmail().equals("antonio@uade.edu.ar")) {
            // Obtener clases pasadas en diferentes rangos
            List<ScheduledClass> weekClasses = allClasses.stream()
                    .filter(sc -> sc.getDateTime().isBefore(LocalDateTime.now()))
                    .filter(sc -> sc.getDateTime().isAfter(today.minusDays(7).atStartOfDay()))
                    .collect(Collectors.toList());

            List<ScheduledClass> monthClasses = allClasses.stream()
                    .filter(sc -> sc.getDateTime().isBefore(LocalDateTime.now()))
                    .filter(sc -> sc.getDateTime().isAfter(today.minusDays(30).atStartOfDay()))
                    .filter(sc -> sc.getDateTime().isBefore(today.minusDays(7).atStartOfDay())) // Excluir la semana
                    .collect(Collectors.toList());

            List<ScheduledClass> threeMonthsClasses = allClasses.stream()
                    .filter(sc -> sc.getDateTime().isBefore(LocalDateTime.now()))
                    .filter(sc -> sc.getDateTime().isAfter(today.minusDays(90).atStartOfDay()))
                    .filter(sc -> sc.getDateTime().isBefore(today.minusDays(30).atStartOfDay())) // Excluir el mes
                    .collect(Collectors.toList());

            log.info("Clases disponibles para {} - Semana: {}, Mes: {}, 3 Meses: {}",
                    user.getEmail(), weekClasses.size(), monthClasses.size(), threeMonthsClasses.size());

            int totalCreated = 0;

            // Crear 3-4 asistencias en la última semana
            Collections.shuffle(weekClasses);
            int weekCount = Math.min(4, weekClasses.size());
            for (int i = 0; i < weekCount; i++) {
                ScheduledClass sc = weekClasses.get(i);
                BookingStatus status = (i % 2 == 0) ? BookingStatus.ATTENDED : BookingStatus.ABSENT;
                createHistoryBooking(user, sc, 2 + random.nextInt(3), status);
                totalCreated++;
            }
            log.info("Creadas {} asistencias para {} en la última semana", weekCount, user.getEmail());

            // Crear 5-6 asistencias en el último mes (excluyendo la semana)
            Collections.shuffle(monthClasses);
            int monthCount = Math.min(6, monthClasses.size());
            for (int i = 0; i < monthCount; i++) {
                ScheduledClass sc = monthClasses.get(i);
                BookingStatus status = (i % 3 == 0) ? BookingStatus.ABSENT : BookingStatus.ATTENDED;
                createHistoryBooking(user, sc, 3 + random.nextInt(5), status);
                totalCreated++;
            }
            log.info("Creadas {} asistencias para {} en el último mes", monthCount, user.getEmail());

            // Crear 8-10 asistencias en los últimos 3 meses (excluyendo el mes)
            Collections.shuffle(threeMonthsClasses);
            int threeMonthsCount = Math.min(10, threeMonthsClasses.size());
            for (int i = 0; i < threeMonthsCount; i++) {
                ScheduledClass sc = threeMonthsClasses.get(i);
                BookingStatus status = (i % 4 == 0) ? BookingStatus.ABSENT : BookingStatus.ATTENDED;
                createHistoryBooking(user, sc, 5 + random.nextInt(10), status);
                totalCreated++;
            }
            log.info("Creadas {} asistencias para {} en los últimos 3 meses", threeMonthsCount, user.getEmail());

            log.info("Total de asistencias creadas para {}: {}", user.getEmail(), totalCreated);
        } else {
            // Para otros usuarios, crear algunas reservas históricas
            List<ScheduledClass> pastClasses = allClasses.stream()
                    .filter(sc -> sc.getDateTime().isBefore(LocalDateTime.now()))
                    .filter(sc -> sc.getDateTime().isAfter(today.minusDays(30).atStartOfDay()))
                    .collect(Collectors.toList());

            if (pastClasses.isEmpty()) {
                log.warn("No hay clases pasadas disponibles para crear historial");
                return;
            }

            Collections.shuffle(pastClasses);
            int count = Math.min(5, pastClasses.size());
            for (int i = 0; i < count; i++) {
                ScheduledClass sc = pastClasses.get(i);
                BookingStatus status = (i % 2 == 0) ? BookingStatus.ATTENDED : BookingStatus.ABSENT;
                createHistoryBooking(user, sc, 2 + random.nextInt(5), status);
            }
            log.info("Creadas {} asistencias para {}", count, user.getEmail());
        }
    }

    private void createHistoryBooking(User user, ScheduledClass scheduledClass, int daysBeforeClass, BookingStatus status) {
        UserBooking booking = new UserBooking();
        booking.setUserId(user.getId());
        booking.setScheduledClassId(scheduledClass.getId());
        booking.setClassName(scheduledClass.getName());
        booking.setClassDateTime(scheduledClass.getDateTime());
        booking.setLocation(scheduledClass.getLocation());
        booking.setDurationMinutes(scheduledClass.getDurationMinutes());
        booking.setStatus(status);
        booking.setCreationDate(scheduledClass.getDateTime().minusDays(daysBeforeClass));
        bookingRepository.save(booking);
    }

    @PostMapping("/test-notification")
    public ResponseEntity<String> createTestNotificationBooking(@RequestParam(required = false) String email) {
        try {
            String userEmail = email != null ? email : "antonio@uade.edu.ar";
            
            // Buscar usuario
            User user = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + userEmail));

            // Obtener o crear una ubicación
            Location location = locationRepository.findAll().stream()
                    .findFirst()
                    .orElseGet(() -> {
                        Location newLocation = new Location("Sede Centro", "Av. Test 123");
                        return locationRepository.save(newLocation);
                    });

            // Obtener o crear un template de clase
            ClassTemplate template = classTemplateRepository.findAll().stream()
                    .findFirst()
                    .orElseGet(() -> {
                        ClassTemplate newTemplate = new ClassTemplate(
                                "Yoga Test",
                                "Yoga",
                                "Profesor Test",
                                60,
                                20,
                                java.time.DayOfWeek.MONDAY,
                                java.time.LocalTime.of(10, 0),
                                location
                        );
                        return classTemplateRepository.save(newTemplate);
                    });

            // Crear clase programada dentro de 1 hora y 10 minutos
            LocalDateTime classDateTime = LocalDateTime.now().plusHours(1).plusMinutes(10);
            ScheduledClass scheduledClass = new ScheduledClass(template, classDateTime);
            scheduledClass.setEnrolledCount(0);
            scheduledClass = scheduledClassRepository.save(scheduledClass);

            log.info("Clase de prueba creada: {} a las {}", scheduledClass.getName(), classDateTime);

            // Crear reserva para el usuario
            UserBooking booking = new UserBooking();
            booking.setUserId(user.getId());
            booking.setScheduledClassId(scheduledClass.getId());
            booking.setClassName(scheduledClass.getName());
            booking.setClassDateTime(scheduledClass.getDateTime());
            booking.setLocation(scheduledClass.getLocation());
            booking.setDurationMinutes(scheduledClass.getDurationMinutes());
            booking.setStatus(BookingStatus.CONFIRMED);
            booking.setCreationDate(LocalDateTime.now());
            booking = bookingRepository.save(booking);

            // Incrementar contador de inscriptos
            scheduledClass.setEnrolledCount(1);
            scheduledClassRepository.save(scheduledClass);

            log.info("Reserva de prueba creada para {} - Clase: {} a las {}", 
                     userEmail, scheduledClass.getName(), classDateTime);
            log.info("La notificación de recordatorio se enviará en aproximadamente 10 minutos (1 hora antes)");

            return ResponseEntity.ok(String.format(
                    "Reserva de prueba creada exitosamente para %s. Clase: %s a las %s. " +
                    "La notificación de recordatorio se enviará en ~10 minutos.",
                    userEmail, scheduledClass.getName(), classDateTime.toString()
            ));
        } catch (Exception e) {
            log.error("Error creando reserva de prueba", e);
            return ResponseEntity.internalServerError()
                    .body("Error: " + e.getMessage());
        }
    }
}

