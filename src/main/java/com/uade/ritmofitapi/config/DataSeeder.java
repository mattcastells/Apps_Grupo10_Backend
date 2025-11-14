package com.uade.ritmofitapi.config;

import com.uade.ritmofitapi.model.ClassTemplate;
import com.uade.ritmofitapi.model.Location;
import com.uade.ritmofitapi.model.ScheduledClass;
import com.uade.ritmofitapi.model.User;
import com.uade.ritmofitapi.model.booking.BookingStatus;
import com.uade.ritmofitapi.model.booking.UserBooking;
import com.uade.ritmofitapi.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Slf4j
@Component
public class DataSeeder implements CommandLineRunner {

    private final Boolean skip = false;
    private final UserRepository userRepository;
    private final ClassTemplateRepository classTemplateRepository;
    private final ScheduledClassRepository scheduledClassRepository;
    private final BookingRepository bookingRepository;
    private final PasswordEncoder passwordEncoder;
    private final LocationRepository locationRepository;
    private final Random random = new Random();

    public DataSeeder(UserRepository userRepository, ClassTemplateRepository classTemplateRepository,
                      ScheduledClassRepository scheduledClassRepository, BookingRepository bookingRepository,
                      PasswordEncoder passwordEncoder, LocationRepository locationRepository) {
        this.userRepository = userRepository;
        this.classTemplateRepository = classTemplateRepository;
        this.scheduledClassRepository = scheduledClassRepository;
        this.bookingRepository = bookingRepository;
        this.passwordEncoder = passwordEncoder;
        this.locationRepository = locationRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        if (skip) {
            log.info("Skipping DataSeeder...");
            return;
        }

        // IMPORTANTE: Solo limpiar si realmente queremos resetear TODO
        // Para desarrollo, comentar esto y usar la lógica idempotente de abajo
        // bookingRepository.deleteAll();
        // scheduledClassRepository.deleteAll();
        // classTemplateRepository.deleteAll();
        // userRepository.deleteAll();
        // locationRepository.deleteAll();

        // --- Create Locations (solo si no existen) ---
        if (locationRepository.count() == 0) {
            Location sedeBelgrano = new Location("Sede Belgrano", "Av. Cabildo 1234");
            Location sedePalermo = new Location("Sede Palermo", "Av. Santa Fe 5678");
            Location sedeCaballito = new Location("Sede Caballito", "Av. Rivadavia 4900");
            locationRepository.saveAll(List.of(sedeBelgrano, sedePalermo, sedeCaballito));
            log.info("-> Sedes creadas.");
        } else {
            log.info("-> Sedes ya existen, saltando creación.");
        }

        // --- Create Users (solo si no existen) ---
        List<User> users;
        if (userRepository.count() == 0) {
            log.info("-> Creando usuarios mock...");
            User user1 = new User("Matias", "matias@uade.edu.ar", "12345678", 25, "Masculino");
            user1.setId("6502251846b9a22a364b9011"); // Fixed ID for testing
            User user2 = new User("Franco", "franco@uade.edu.ar", "12345678", 35, "Masculino");
            User user3 = new User("Horacio", "horacio@uade.edu.ar", "12345678", 37, "Masculino");
            User user4 = new User("Antonio", "antonio@uade.edu.ar", "12345678", 24, "Masculino");

            users = new ArrayList<>(List.of(user1, user2, user3, user4));
            for (User user : users) {
                String plainPassword = user.getPassword(); // Guardar antes de hashear
                String hashedPassword = passwordEncoder.encode(plainPassword);
                user.setPassword(hashedPassword);
                user.setVerified(true);

                // LOG PARA VERIFICAR
                log.info("🔐 Creating user: {} with hashed password", user.getEmail());
            }
            userRepository.saveAll(users);

            // VERIFICACIÓN POST-CREACIÓN
            for (User user : users) {
                User saved = userRepository.findByEmail(user.getEmail()).orElseThrow();
                boolean testMatch = passwordEncoder.matches("12345678", saved.getPassword());
                log.info("✅ User {} - Password test: {}", user.getEmail(), testMatch ? "PASS" : "FAIL ❌");
            }

            log.info("-> Usuarios mock creados y verificados.");
        } else {
            log.info("-> Usuarios ya existen, saltando creación.");
            users = userRepository.findAll();

            // VERIFICAR usuarios existentes
            User testUser = userRepository.findByEmail("horacio@uade.edu.ar").orElse(null);
            if (testUser != null) {
                boolean testMatch = passwordEncoder.matches("12345678", testUser.getPassword());
                log.info("🔍 Existing user horacio@uade.edu.ar - Password test: {}", testMatch ? "PASS ✅" : "FAIL ❌");
            }
        }

        // --- Create Class Templates (solo si no existen) ---
        if (classTemplateRepository.count() == 0) {
            List<Location> locations = locationRepository.findAll();
            if (locations.size() >= 3) {
                createClassTemplates(locations.get(0), locations.get(1), locations.get(2));
                log.info("-> Plantillas de clases creadas.");
            } else {
                log.warn("-> No hay suficientes sedes para crear plantillas de clases.");
            }
        } else {
            log.info("-> Plantillas de clases ya existen, saltando creación.");
        }

        // --- Generate Scheduled Classes for the last 4 weeks and next 4 weeks (solo si no existen) ---
        if (scheduledClassRepository.count() == 0) {
            generateScheduledClasses();
            log.info("-> Clases agendadas generadas.");
        } else {
            log.info("-> Clases agendadas ya existen, saltando generación.");
        }

        // --- Create User Bookings (Past and Future) (solo si no existen) ---
        if (bookingRepository.count() == 0) {
            createRealisticUserBookings(users);
            log.info("-> Reservas de usuarios (pasadas y futuras) creadas.");
        } else {
            log.info("-> Reservas ya existen, saltando creación.");
        }

        log.info("----- DATOS MOCK VERIFICADOS/CARGADOS CORRECTAMENTE -----");
    }

    private void createClassTemplates(Location sedeBelgrano, Location sedePalermo, Location sedeCaballito) {
        List<ClassTemplate> templates = new ArrayList<>();

        templates.add(new ClassTemplate("Yoga", "Yoga", "Ana López", 60, 20, DayOfWeek.MONDAY, LocalTime.of(9, 0), sedeBelgrano));
        templates.add(new ClassTemplate("Funcional Intenso", "Funcional", "Carlos Ruiz", 45, 15, DayOfWeek.WEDNESDAY, LocalTime.of(18, 30), sedeCaballito));
        templates.add(new ClassTemplate("Spinning de Alta Intensidad", "Spinning", "Jorge Franco", 45, 15, DayOfWeek.TUESDAY, LocalTime.of(10, 0), sedePalermo));
        templates.add(new ClassTemplate("Boxeo Recreativo", "Boxeo", "Pablo Pujol", 75, 12, DayOfWeek.FRIDAY, LocalTime.of(20, 0), sedeBelgrano));
        templates.add(new ClassTemplate("Pilates Reformer", "Pilates", "Sofía Gómez", 50, 10, DayOfWeek.THURSDAY, LocalTime.of(17, 0), sedePalermo));
        templates.add(new ClassTemplate("Zumba Party", "Zumba", "Valentina Díaz", 60, 25, DayOfWeek.SATURDAY, LocalTime.of(11, 0), sedeCaballito));
        templates.add(new ClassTemplate("CrossFit Avanzado", "CrossFit", "Martín Herrera", 50, 18, DayOfWeek.TUESDAY, LocalTime.of(7, 30), sedeBelgrano));
        templates.add(new ClassTemplate("Yoga", "Yoga", "Ana López", 60, 20, DayOfWeek.THURSDAY, LocalTime.of(19, 0), sedeBelgrano));

        classTemplateRepository.saveAll(templates);
    }

    private void generateScheduledClasses() {
        LocalDate today = LocalDate.now();
        List<ClassTemplate> templates = classTemplateRepository.findAll();
        for (int i = -28; i < 28; i++) { // From 4 weeks ago to 4 weeks in the future
            LocalDate date = today.plusDays(i);
            for (ClassTemplate template : templates) {
                if (date.getDayOfWeek() == template.getDayOfWeek()) {
                    ScheduledClass scheduledClass = new ScheduledClass(template, LocalDateTime.of(date, template.getTime()));
                    scheduledClassRepository.save(scheduledClass);
                }
            }
        }
    }

    private void createRealisticUserBookings(List<User> users) {
        List<ScheduledClass> allScheduledClasses = scheduledClassRepository.findAll();
        LocalDateTime now = LocalDateTime.now();

        List<ScheduledClass> pastClasses = allScheduledClasses.stream()
                .filter(sc -> sc.getDateTime().isBefore(now))
                .collect(Collectors.toList());

        List<ScheduledClass> futureClasses = allScheduledClasses.stream()
                .filter(sc -> sc.getDateTime().isAfter(now))
                .collect(Collectors.toList());

        for (User user : users) {
            // Create 2 to 4 past bookings
            for (int i = 0; i < 2 + random.nextInt(3); i++) {
                if (!pastClasses.isEmpty()) {
                    ScheduledClass randomPastClass = pastClasses.get(random.nextInt(pastClasses.size()));
                    createBooking(user, randomPastClass, BookingStatus.CONFIRMED);
                }
            }

            // Create 2 to 4 future bookings
            for (int i = 0; i < 2 + random.nextInt(3); i++) {
                if (!futureClasses.isEmpty()) {
                    ScheduledClass randomFutureClass = futureClasses.get(random.nextInt(futureClasses.size()));
                    // 80% chance of CONFIRMED, 20% chance of CANCELLED
                    BookingStatus status = random.nextDouble() < 0.8 ? BookingStatus.CONFIRMED : BookingStatus.CANCELLED;
                    createBooking(user, randomFutureClass, status);
                }
            }
        }
    }

    private void createBooking(User user, ScheduledClass scheduledClass, BookingStatus status) {
        // Avoid double booking the same class for the same user
        if (bookingRepository.findByUserIdAndScheduledClassId(user.getId(), scheduledClass.getId()).isPresent()) {
            return;
        }

        // Check capacity only for confirmed bookings
        if (status == BookingStatus.CONFIRMED && scheduledClass.getEnrolledCount() >= scheduledClass.getCapacity()) {
            log.warn("No se pudo crear reserva para {} en {} - Clase llena.", user.getName(), scheduledClass.getName());
            return;
        }

        UserBooking booking = new UserBooking();
        booking.setUserId(user.getId());
        booking.setScheduledClassId(scheduledClass.getId());
        booking.setClassName(scheduledClass.getName());
        booking.setClassDateTime(scheduledClass.getDateTime());
        booking.setCreationDate(LocalDateTime.now().minusDays(random.nextInt(10))); // Simulate booking was made some days ago
        booking.setStatus(status);

        bookingRepository.save(booking);

        // Update enrolled count only if confirmed
        if (status == BookingStatus.CONFIRMED) {
            scheduledClass.setEnrolledCount(scheduledClass.getEnrolledCount() + 1);
            scheduledClassRepository.save(scheduledClass);
        }

        log.info("Reserva creada: {} para {} en {} con estado {}", user.getName(), scheduledClass.getName(), scheduledClass.getDateTime(), status);
    }
}
