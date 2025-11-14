package com.uade.ritmofitapi.config;

import com.uade.ritmofitapi.model.ClassTemplate;
import com.uade.ritmofitapi.model.Location;
import com.uade.ritmofitapi.model.ScheduledClass;
import com.uade.ritmofitapi.model.User;
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

        // Clean slate
        bookingRepository.deleteAll();
        scheduledClassRepository.deleteAll();
        classTemplateRepository.deleteAll();
        userRepository.deleteAll();
        locationRepository.deleteAll();

        // --- Create Locations ---
        Location sedeBelgrano = new Location("Sede Belgrano", "Av. Cabildo 1234");
        Location sedePalermo = new Location("Sede Palermo", "Av. Santa Fe 5678");
        Location sedeCaballito = new Location("Sede Caballito", "Av. Rivadavia 4900");
        locationRepository.saveAll(List.of(sedeBelgrano, sedePalermo, sedeCaballito));
        log.info("-> Sedes creadas.");

        // --- Create Users ---
        User user1 = new User("Matias", "matias@uade.edu.ar", "1234", 25, "Masculino");
        user1.setId("6502251846b9a22a364b9011"); // Fixed ID for testing
        User user2 = new User("Franco", "franco@uade.edu.ar", "1234", 29, "Masculino");
        User user3 = new User("Horacio", "horacio@uade.edu.ar", "1234", 37, "Masculino");
        User user4 = new User("Antonio", "antonio@uade.edu.ar", "1234", 24, "Masculino");

        List<User> users = new ArrayList<>(List.of(user1, user2, user3, user4));
        for (User user : users) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            user.setVerified(true);
        }
        userRepository.saveAll(users);
        log.info("-> Usuarios mock creados.");

        // --- Create Class Templates ---
        createClassTemplates(sedeBelgrano, sedePalermo, sedeCaballito);
        log.info("-> Plantillas de clases creadas.");

        // --- Generate Scheduled Classes for the last 4 weeks and next 4 weeks ---
        generateScheduledClasses();
        log.info("-> Clases agendadas generadas.");

        // --- User Bookings are NOT pre-created ---
        // Users should create their own bookings through the app
        log.info("-> No se crean reservas automáticas. Los usuarios deben reservar desde la app.");

        log.info("----- DATOS MOCK CARGADOS CORRECTAMENTE -----");
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
}
