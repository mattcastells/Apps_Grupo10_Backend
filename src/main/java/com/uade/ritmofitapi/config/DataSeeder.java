package com.uade.ritmofitapi.config;

import com.uade.ritmofitapi.model.ClassTemplate;
import com.uade.ritmofitapi.model.ScheduledClass;
import com.uade.ritmofitapi.model.User;
import com.uade.ritmofitapi.repository.BookingRepository;
import com.uade.ritmofitapi.repository.ClassTemplateRepository;
import com.uade.ritmofitapi.repository.ScheduledClassRepository;
import com.uade.ritmofitapi.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
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

    public DataSeeder(UserRepository userRepository, ClassTemplateRepository classTemplateRepository, ScheduledClassRepository scheduledClassRepository, BookingRepository bookingRepository) {
        this.userRepository = userRepository;
        this.classTemplateRepository = classTemplateRepository;
        this.scheduledClassRepository = scheduledClassRepository;
        this.bookingRepository = bookingRepository;
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
        User user1 = new User("mattcastells@uade.edu.ar");
        user1.setId("6502251846b9a22a364b9011"); // Fijamos un ID para pruebas
        User user2 = new User("otro.usuario@uade.edu.ar");
        userRepository.saveAll(List.of(user1, user2));
        log.info("-> Usuarios mock creados.");

        // --- Crear Plantillas de Clases ---
        ClassTemplate yogaTemplate = new ClassTemplate();
        yogaTemplate.setName("Yoga Matutino");
        yogaTemplate.setDiscipline("Yoga");
        yogaTemplate.setProfessor("Ana López");
        yogaTemplate.setDurationMinutes(60);
        yogaTemplate.setCapacity(20);
        yogaTemplate.setDayOfWeek(DayOfWeek.MONDAY);
        yogaTemplate.setTime(LocalTime.of(9, 0));

        ClassTemplate funcionalTemplate = new ClassTemplate();
        funcionalTemplate.setName("Funcional Intenso");
        funcionalTemplate.setDiscipline("Funcional");
        funcionalTemplate.setProfessor("Carlos Ruiz");
        funcionalTemplate.setDurationMinutes(45);
        funcionalTemplate.setCapacity(15);
        funcionalTemplate.setDayOfWeek(DayOfWeek.WEDNESDAY);
        funcionalTemplate.setTime(LocalTime.of(18, 30));

        classTemplateRepository.saveAll(List.of(yogaTemplate, funcionalTemplate));
        log.info("-> Plantillas de clases creadas.");

        // --- Generar Clases Agendadas para las próximas 2 semanas ---
        LocalDate today = LocalDate.now();
        for (int i = 0; i < 14; i++) {
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
        log.info("-> Clases agendadas para las próximas 2 semanas generadas.");
        log.info("----- DATOS MOCK CARGADOS CORRECTAMENTE -----");
    }
}