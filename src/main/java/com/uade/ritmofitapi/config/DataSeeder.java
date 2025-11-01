package com.uade.ritmofitapi.config;

import com.uade.ritmofitapi.model.ClassTemplate;
import com.uade.ritmofitapi.model.ScheduledClass;
import com.uade.ritmofitapi.model.User;
import com.uade.ritmofitapi.model.booking.BookingStatus;
import com.uade.ritmofitapi.model.booking.UserBooking;
import com.uade.ritmofitapi.repository.BookingRepository;
import com.uade.ritmofitapi.repository.ClassTemplateRepository;
import com.uade.ritmofitapi.repository.ScheduledClassRepository;
import com.uade.ritmofitapi.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Slf4j
@Component
public class DataSeeder implements CommandLineRunner {

    private final Boolean skip = true;

    private final UserRepository userRepository;
    private final ClassTemplateRepository classTemplateRepository;
    private final ScheduledClassRepository scheduledClassRepository;
    private final BookingRepository bookingRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserRepository userRepository, ClassTemplateRepository classTemplateRepository,
                      ScheduledClassRepository scheduledClassRepository, BookingRepository bookingRepository,
                      PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.classTemplateRepository = classTemplateRepository;
        this.scheduledClassRepository = scheduledClassRepository;
        this.bookingRepository = bookingRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {

        if (skip) {
            log.info("Skipping DataSeeder...");
            return;
        }

        // Limpiar datos para evitar duplicados en cada reinicio
        bookingRepository.deleteAll();
        scheduledClassRepository.deleteAll();
        classTemplateRepository.deleteAll();
        userRepository.deleteAll();

        // --- Crear Usuarios ---

        User user1 = new User("Matias", "matias@uade.edu.ar", "1234", 25, "Masculino");
        User user2 = new User("Franco", "franco@uade.edu.ar", "1234", 35, "Masculino");
        User user3 = new User("Horacio", "horacio@uade.edu.ar", "1234", 37, "Masculino");
        User user4 = new User("Antonio", "antonio@uade.edu.ar", "1234", 24, "Masculino");

        List<User> users = List.of(user1, user2, user3, user4);

        for (User user : users) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            user.setVerified(true);
        }
        user1.setId("6502251846b9a22a364b9011"); // Fijamos un ID para pruebas
        userRepository.saveAll(List.of(user1, user2));
        log.info("-> Usuarios mock creados.");

        // --- Crear Plantillas de Clases ---
        ClassTemplate yoga = new ClassTemplate();
        yoga.setId("6502251846b9a22a364b9010");
        yoga.setName("Yoga Matutino");
        yoga.setDiscipline("Yoga");
        yoga.setProfessor("Ana López");
        yoga.setDurationMinutes(60);
        yoga.setCapacity(20);
        yoga.setDayOfWeek(DayOfWeek.MONDAY);
        yoga.setTime(LocalTime.of(9, 0));

        ClassTemplate funcional = new ClassTemplate();
        funcional.setId("6502251846b9a22a364b9011");
        funcional.setName("Funcional Intenso");
        funcional.setDiscipline("Funcional");
        funcional.setProfessor("Carlos Ruiz");
        funcional.setDurationMinutes(45);
        funcional.setCapacity(15);
        funcional.setDayOfWeek(DayOfWeek.WEDNESDAY);
        funcional.setTime(LocalTime.of(18, 30));

        ClassTemplate spinning = new ClassTemplate();
        spinning.setId("6502251846b9a22a364b9013");
        spinning.setName("Spinning de Alta Intensidad");
        spinning.setDiscipline("Spinning");
        spinning.setProfessor("Jorge Franco");
        spinning.setDurationMinutes(45);
        spinning.setCapacity(15);
        spinning.setDayOfWeek(DayOfWeek.TUESDAY);
        spinning.setTime(LocalTime.of(10, 0));

        ClassTemplate boxeo = new ClassTemplate();
        boxeo.setId("6502251846b9a22a364b9012");
        boxeo.setName("Boxeo Recreativo");
        boxeo.setDiscipline("Boxeo");
        boxeo.setProfessor("Pablo Pujol");
        boxeo.setDurationMinutes(75);
        boxeo.setCapacity(12);
        boxeo.setDayOfWeek(DayOfWeek.FRIDAY);
        boxeo.setTime(LocalTime.of(20, 0));

        classTemplateRepository.saveAll(List.of(yoga, funcional, spinning, boxeo));
        log.info("-> Plantillas de clases creadas.");

        // --- Generar Clases Agendadas para las últimas 2 semanas y próximas 2 semanas ---
        LocalDate today = LocalDate.now();
        for (int i = -14; i < 14; i++) { // Cambiar a -14 para incluir fechas pasadas
            LocalDate date = today.plusDays(i);
            for (ClassTemplate template : classTemplateRepository.findAll()) {
                if (date.getDayOfWeek() == template.getDayOfWeek()) {
                    ScheduledClass scheduledClass = new ScheduledClass();
                    scheduledClass.setTemplateId(template.getId());
                    scheduledClass.setDateTime(LocalDateTime.of(date, template.getTime()));
                    scheduledClass.setCapacity(template.getCapacity());
                    scheduledClass.setDurationMinutes(template.getDurationMinutes());
                    scheduledClass.setName(template.getName());
                    scheduledClass.setProfessor(template.getProfessor());
                    scheduledClass.setEnrolledCount(0);
                    scheduledClassRepository.save(scheduledClass);
                }
            }
        }
        log.info("-> Clases agendadas para las últimas 2 semanas y próximas 2 semanas generadas.");

        // --- Crear Reservas de Usuarios ---
        createUserBookings(user1, user2, user3);
        log.info("-> Reservas de usuarios creadas.");
        
        log.info("----- DATOS MOCK CARGADOS CORRECTAMENTE -----");
    }

    private void createUserBookings(User user1, User user2, User user3) {
        // Obtener todas las clases agendadas
        List<ScheduledClass> allClasses = scheduledClassRepository.findAll();
        
        // Matias se anota a Yoga y Funcional
        createBookingForUser(user1, "Yoga Matutino", allClasses);
        createBookingForUser(user1, "Funcional Intenso", allClasses);
        
        // Franco se anota a Spinning y Boxeo
        createBookingForUser(user2, "Spinning de Alta Intensidad", allClasses);
        createBookingForUser(user2, "Boxeo Recreativo", allClasses);
        
        // Horacio se anota a Yoga y Spinning
        createBookingForUser(user3, "Yoga Matutino", allClasses);
        createBookingForUser(user3, "Spinning de Alta Intensidad", allClasses);
    }

    private void createBookingForUser(User user, String className, List<ScheduledClass> allClasses) {
        // Buscar la primera clase disponible con el nombre especificado
        ScheduledClass targetClass = allClasses.stream()
                .filter(clazz -> clazz.getName().equals(className))
                .filter(clazz -> clazz.getEnrolledCount() < clazz.getCapacity())
                .findFirst()
                .orElse(null);

        if (targetClass != null) {
            // Crear la reserva
            UserBooking booking = new UserBooking();
            booking.setUserId(user.getId());
            booking.setScheduledClassId(targetClass.getId());
            booking.setClassName(targetClass.getName());
            booking.setClassDateTime(targetClass.getDateTime());
            booking.setCreationDate(LocalDateTime.now());
            booking.setStatus(BookingStatus.CONFIRMED);
            
            bookingRepository.save(booking);
            
            // Actualizar el contador de inscritos
            targetClass.setEnrolledCount(targetClass.getEnrolledCount() + 1);
            scheduledClassRepository.save(targetClass);
            
            log.info("Reserva creada: {} para {} el {}", user.getName(), className, targetClass.getDateTime());
        } else {
            log.warn("No se pudo crear reserva para {} en {} - clase no disponible", user.getName(), className);
        }
    }
}