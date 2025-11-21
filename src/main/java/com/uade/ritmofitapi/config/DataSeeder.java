package com.uade.ritmofitapi.config;

import com.uade.ritmofitapi.model.ClassTemplate;
import com.uade.ritmofitapi.model.ClassRating;
import com.uade.ritmofitapi.model.Location;
import com.uade.ritmofitapi.model.News;
import com.uade.ritmofitapi.model.ScheduledClass;
import com.uade.ritmofitapi.model.User;
import com.uade.ritmofitapi.model.booking.BookingStatus;
import com.uade.ritmofitapi.model.booking.UserBooking;
import com.uade.ritmofitapi.repository.UserRepository;
import com.uade.ritmofitapi.repository.ClassTemplateRepository;
import com.uade.ritmofitapi.repository.ScheduledClassRepository;
import com.uade.ritmofitapi.repository.BookingRepository;
import com.uade.ritmofitapi.repository.LocationRepository;
import com.uade.ritmofitapi.repository.NewsRepository;
import com.uade.ritmofitapi.repository.ClassRatingRepository;
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
import java.util.Collections;

@Slf4j
@Component
public class DataSeeder implements CommandLineRunner {

    private final Boolean skip = true;
    private final UserRepository userRepository;
    private final ClassTemplateRepository classTemplateRepository;
    private final ScheduledClassRepository scheduledClassRepository;
    private final BookingRepository bookingRepository;
    private final PasswordEncoder passwordEncoder;
    private final LocationRepository locationRepository;
    private final NewsRepository newsRepository;
    private final ClassRatingRepository ratingRepository;

    public DataSeeder(UserRepository userRepository, ClassTemplateRepository classTemplateRepository,
                      ScheduledClassRepository scheduledClassRepository, BookingRepository bookingRepository,
                      PasswordEncoder passwordEncoder, LocationRepository locationRepository,
                      NewsRepository newsRepository, ClassRatingRepository ratingRepository) {
        this.userRepository = userRepository;
        this.classTemplateRepository = classTemplateRepository;
        this.scheduledClassRepository = scheduledClassRepository;
        this.bookingRepository = bookingRepository;
        this.passwordEncoder = passwordEncoder;
        this.locationRepository = locationRepository;
        this.newsRepository = newsRepository;
        this.ratingRepository = ratingRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        if (skip) {
            log.info("Skipping DataSeeder...");
            return;
        }

        // ===== LIMPIEZA COMPLETA DE LA BASE DE DATOS =====
        // Esto asegura que en cada arranque partimos de cero
        // Evita problemas con hashes BCrypt de ejecuciones anteriores
        log.info("Limpiando base de datos...");
        bookingRepository.deleteAll();
        scheduledClassRepository.deleteAll();
        classTemplateRepository.deleteAll();
        userRepository.deleteAll();
        locationRepository.deleteAll();
        newsRepository.deleteAll();
        ratingRepository.deleteAll();
        log.info("Base de datos limpiada completamente.");

        // --- Create Locations ---
        log.info("Creando sedes...");
        Location sedeBelgrano = new Location("Sede Belgrano", "Av. Cabildo 1234");
        Location sedePalermo = new Location("Sede Palermo", "Av. Santa Fe 5678");
        Location sedeCaballito = new Location("Sede Caballito", "Av. Rivadavia 4900");
        locationRepository.saveAll(List.of(sedeBelgrano, sedePalermo, sedeCaballito));
        log.info("3 sedes creadas.");

        // --- Create Users ---
        log.info("Creando usuarios mock...");
        User user1 = new User("Matias", "matias@uade.edu.ar", "12345678", 25, "Masculino");
        user1.setId("6502251846b9a22a364b9011"); // Fixed ID for testing
        User user2 = new User("Franco", "franco@uade.edu.ar", "12345678", 29, "Masculino");
        User user3 = new User("Horacio", "horacio@uade.edu.ar", "12345678", 37, "Masculino");
        User user4 = new User("Antonio", "antonio@uade.edu.ar", "12345678", 24, "Masculino");

        List<User> users = new ArrayList<>(List.of(user1, user2, user3, user4));

        // Hashear passwords y marcar como verificados
        for (User user : users) {
            String plainPassword = user.getPassword();
            String hashedPassword = passwordEncoder.encode(plainPassword);
            user.setPassword(hashedPassword);
            user.setVerified(true);
            log.info("Creating user: {} | Hash starts with: {}",
                     user.getEmail(), hashedPassword.substring(0, 20) + "...");
        }

        userRepository.saveAll(users);
        log.info("{} usuarios guardados en BD.", users.size());

        // VERIFICACIÓN CRÍTICA: Comprobar que los usuarios se guardaron correctamente
        log.info("VERIFICACIÓN DE PASSWORDS:");
        for (User user : users) {
            User saved = userRepository.findByEmail(user.getEmail()).orElseThrow();
            boolean testMatch = passwordEncoder.matches("12345678", saved.getPassword());

            if (testMatch) {
                log.info("{} - Password test: PASS", user.getEmail());
            } else {
                log.error("{} - Password test: FAIL", user.getEmail());
                log.error("Hash en BD: {}", saved.getPassword());
                log.error("ESTO ES UN ERROR CRÍTICO - EL LOGIN NO FUNCIONARÁ");
            }
        }

        // --- Create Class Templates ---
        log.info("Creando plantillas de clases...");
        createClassTemplates(sedeBelgrano, sedePalermo, sedeCaballito);
        log.info("Plantillas de clases creadas.");

        // --- Generate Scheduled Classes for the last 4 weeks and next 4 weeks ---
        log.info("Generando clases agendadas (4 semanas pasadas + 4 futuras)...");
        generateScheduledClasses();
        log.info("Clases agendadas generadas.");

        // --- Create history bookings for user1 (past attended classes) ---
        createHistoryBookings(user1);
        log.info("-> Reservas históricas creadas para el usuario de prueba.");

        // --- Create News ---
        createNews();
        log.info("-> Noticias creadas.");

        // --- Create Rating Mocked (con reseña) ---
        createRatingMocked();
        log.info("-> Reseña mock creada.");
        
        // --- Create Class Without Rating Mocked (sin reseña, para que se cree manualmente) ---
        createClassWithoutRatingMocked();
        log.info("-> Clase sin reseña mock creada.");


        log.info("===== DATOS MOCK CARGADOS CORRECTAMENTE =====");
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

    private void createHistoryBookings(User user) {
        LocalDate today = LocalDate.now();
        List<ScheduledClass> allClasses = scheduledClassRepository.findAll();
        
        // Filter classes from the past (within last 30 days)
        List<ScheduledClass> pastClasses = allClasses.stream()
            .filter(sc -> sc.getDateTime().isBefore(LocalDateTime.now()))
            .filter(sc -> sc.getDateTime().isAfter(today.minusDays(30).atStartOfDay()))
            .toList();
        
        if (pastClasses.isEmpty()) {
            log.warn("No hay clases pasadas disponibles para crear historial");
            return;
        }
        
        // Shuffle the list to get random classes
        List<ScheduledClass> shuffledClasses = new ArrayList<>(pastClasses);
        Collections.shuffle(shuffledClasses);
        
        // Create exactly 2 history bookings: one ATTENDED and one ABSENT
        if (shuffledClasses.size() >= 2) {
            // First booking: ATTENDED (present)
            ScheduledClass class1 = shuffledClasses.get(0);
            int daysBeforeClass1 = 3 + new Random().nextInt(7);
            createHistoryBooking(user, class1, daysBeforeClass1, BookingStatus.ATTENDED);
            
            // Second booking: ABSENT (ausente)
            ScheduledClass class2 = shuffledClasses.get(1);
            int daysBeforeClass2 = 3 + new Random().nextInt(7);
            createHistoryBooking(user, class2, daysBeforeClass2, BookingStatus.ABSENT);
            
            log.info("Creadas 2 reservas históricas para usuario {}: 1 ATTENDED, 1 ABSENT", user.getEmail());
        } else {
            log.warn("No hay suficientes clases pasadas para crear historial completo");
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

    private void createNews() {
        List<News> newsList = new ArrayList<>();

        News news1 = new News();
        news1.setTitle("Nueva Sede en Recoleta");
        news1.setContent("¡Estamos emocionados de anunciar la apertura de nuestra nueva sede en Recoleta! Con equipamiento de última generación y un equipo de profesionales altamente capacitados.");
        news1.setImageUrl("https://res.cloudinary.com/do7lo4pkj/image/upload/v1763109160/nueva_sede_f4slx4.jpg");
        news1.setPublishedDate(LocalDateTime.now().minusDays(2));
        news1.setCreatedAt(LocalDateTime.now().minusDays(2));

        News news2 = new News();
        news2.setTitle("Horarios Especiales - Feriados");
        news2.setContent("Durante los próximos feriados, el gimnasio tendrá horarios especiales. Consulta en recepción o en nuestra app los nuevos horarios disponibles.");
        news2.setImageUrl("https://res.cloudinary.com/do7lo4pkj/image/upload/v1763109160/feriados_k0o7d2.jpg");
        news2.setPublishedDate(LocalDateTime.now().minusDays(5));
        news2.setCreatedAt(LocalDateTime.now().minusDays(5));

        News news3 = new News();
        news3.setTitle("Desafío RitmoFit 30 Días");
        news3.setContent("¡Únete al Desafío RitmoFit de 30 días! Participa asistiendo a 20 clases en un mes y obtén un mes gratis. Inscripciones abiertas hasta fin de mes.");
        news3.setImageUrl("https://res.cloudinary.com/do7lo4pkj/image/upload/v1763109162/desafio_cx2j6j.jpg");
        news3.setPublishedDate(LocalDateTime.now().minusDays(8));
        news3.setCreatedAt(LocalDateTime.now().minusDays(8));

        News news4 = new News();
        news4.setTitle("Nuevas Clases de Yoga Aéreo");
        news4.setContent("A partir de la próxima semana incorporamos clases de Yoga Aéreo. Una experiencia única que combina flexibilidad, fuerza y diversión. Cupos limitados.");
        news4.setImageUrl("https://res.cloudinary.com/do7lo4pkj/image/upload/v1763109160/yoga_aereo_npdtua.jpg");
        news4.setPublishedDate(LocalDateTime.now().minusDays(12));
        news4.setCreatedAt(LocalDateTime.now().minusDays(12));

        newsList.add(news1);
        newsList.add(news2);
        newsList.add(news3);
        newsList.add(news4);

        newsRepository.saveAll(newsList);
    }


    private void createRatingMocked() {
        // Buscar el primer usuario disponible
        List<User> users = userRepository.findAll();
        if (users.isEmpty()) {
            log.warn("No hay usuarios disponibles para crear la reseña mock");
            return;
        }
        User user = users.get(0);

        // Crear una clase que haya terminado hace 2 horas (dentro de las 24 horas)
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime classEndTime = now.minusHours(2); // Hace 2 horas
        LocalDateTime classStartTime = classEndTime.minusMinutes(60); // Clase de 60 minutos

        // Buscar una plantilla de clase para usar como base
        List<ClassTemplate> templates = classTemplateRepository.findAll();
        if (templates.isEmpty()) {
            log.warn("No hay plantillas de clase disponibles para crear la reseña");
            return;
        }

        ClassTemplate template = templates.get(0);
        
        // Crear una clase programada que ya terminó
        ScheduledClass pastClass = new ScheduledClass();
        pastClass.setTemplateId(template.getId());
        pastClass.setLocationId(template.getLocation() != null ? template.getLocation().getId() : null);
        pastClass.setDiscipline(template.getDiscipline());
        pastClass.setDateTime(classStartTime);
        pastClass.setCapacity(template.getCapacity());
        pastClass.setName(template.getName());
        pastClass.setProfessor(template.getProfessor());
        pastClass.setDurationMinutes(template.getDurationMinutes());
        pastClass.setLocation(template.getLocation() != null ? template.getLocation().getName() : "Sede Centro");
        pastClass.setEnrolledCount(1);
        pastClass = scheduledClassRepository.save(pastClass);
        log.info("Clase pasada creada para reseña: {} - {}", pastClass.getName(), pastClass.getDateTime());

        // Crear un booking para esa clase con status ATTENDED
        UserBooking booking = new UserBooking();
        booking.setUserId(user.getId());
        booking.setScheduledClassId(pastClass.getId());
        booking.setClassName(pastClass.getName());
        booking.setClassDateTime(pastClass.getDateTime());
        booking.setLocation(pastClass.getLocation());
        booking.setDurationMinutes(pastClass.getDurationMinutes());
        booking.setStatus(BookingStatus.ATTENDED);
        booking.setCreationDate(classStartTime.minusDays(2)); // Reservó hace 2 días
        booking = bookingRepository.save(booking);
        log.info("Booking creado para reseña: {}", booking.getId());

        // Crear la reseña (calificación)
        ClassRating rating = new ClassRating(
                user.getId(),
                booking.getId(),
                5, // 5 estrellas
                "Excelente clase! El profesor fue muy claro y la intensidad fue perfecta. Definitivamente volveré."
        );
        rating = ratingRepository.save(rating);
        log.info("Reseña creada: {} estrellas para booking {} (usuario: {})", rating.getRating(), booking.getId(), user.getEmail());
    }

    private void createClassWithoutRatingMocked() {
        // Buscar el primer usuario disponible
        List<User> users = userRepository.findAll();
        if (users.isEmpty()) {
            log.warn("No hay usuarios disponibles para crear la clase sin reseña mock");
            return;
        }
        User user = users.get(0);

        // Crear una clase que haya terminado hace 1 hora (dentro de las 24 horas, sin reseña)
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime classEndTime = now.minusHours(1); // Hace 1 hora
        LocalDateTime classStartTime = classEndTime.minusMinutes(45); // Clase de 45 minutos

        // Buscar una plantilla de clase diferente para usar como base
        List<ClassTemplate> templates = classTemplateRepository.findAll();
        if (templates.size() < 2) {
            log.warn("No hay suficientes plantillas de clase disponibles");
            return;
        }

        ClassTemplate template = templates.get(1); // Usar una plantilla diferente
        
        // Crear una clase programada que ya terminó
        ScheduledClass pastClass = new ScheduledClass();
        pastClass.setTemplateId(template.getId());
        pastClass.setLocationId(template.getLocation() != null ? template.getLocation().getId() : null);
        pastClass.setDiscipline(template.getDiscipline());
        pastClass.setDateTime(classStartTime);
        pastClass.setCapacity(template.getCapacity());
        pastClass.setName(template.getName());
        pastClass.setProfessor(template.getProfessor());
        pastClass.setDurationMinutes(template.getDurationMinutes());
        pastClass.setLocation(template.getLocation() != null ? template.getLocation().getName() : "Sede Centro");
        pastClass.setEnrolledCount(1);
        pastClass = scheduledClassRepository.save(pastClass);
        log.info("Clase pasada creada (sin reseña): {} - {}", pastClass.getName(), pastClass.getDateTime());

        // Crear un booking para esa clase con status ATTENDED (sin reseña)
        UserBooking booking = new UserBooking();
        booking.setUserId(user.getId());
        booking.setScheduledClassId(pastClass.getId());
        booking.setClassName(pastClass.getName());
        booking.setClassDateTime(pastClass.getDateTime());
        booking.setLocation(pastClass.getLocation());
        booking.setDurationMinutes(pastClass.getDurationMinutes());
        booking.setStatus(BookingStatus.ATTENDED);
        booking.setCreationDate(classStartTime.minusDays(1)); // Reservó hace 1 día
        booking = bookingRepository.save(booking);
        log.info("Booking creado (sin reseña) para usuario {}: {} - Clase terminó hace 1 hora", user.getEmail(), booking.getId());
        log.info("Esta clase puede ser calificada desde la app (dentro de las 24 horas)");
    }
}
