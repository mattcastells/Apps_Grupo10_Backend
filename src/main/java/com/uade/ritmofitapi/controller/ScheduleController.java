package com.uade.ritmofitapi.controller;

import com.uade.ritmofitapi.dto.request.CreateScheduledClassRequest;
import com.uade.ritmofitapi.dto.response.ScheduledClassDto;
import com.uade.ritmofitapi.model.ScheduledClass;
import com.uade.ritmofitapi.service.ScheduleService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/schedule")
public class ScheduleController {

    private final ScheduleService scheduleService;

    public ScheduleController(ScheduleService scheduleService) {
        this.scheduleService = scheduleService;
    }

    @GetMapping("/weekly")
    public ResponseEntity<List<ScheduledClassDto>> getWeeklySchedule() {
        List<ScheduledClassDto> schedule = scheduleService.getWeeklySchedule();
        return ResponseEntity.ok(schedule);
    }

    /**
     * Endpoint unificado con filtros opcionales
     * GET /api/v1/schedule?location={id}&discipline={name}&from={dd-MM-yyyy}&to={dd-MM-yyyy}
     * Todos los parámetros son opcionales
     */
    @GetMapping
    public ResponseEntity<List<ScheduledClassDto>> getFilteredSchedule(
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String discipline,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to
    ) {
        try {
            List<ScheduledClassDto> schedule = scheduleService.getFilteredSchedule(location, discipline, from, to);
            return ResponseEntity.ok(schedule);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @GetMapping("/{classId}")
    public ResponseEntity<ScheduledClassDto> getClassDetail(@PathVariable String classId) {
        ScheduledClassDto classDetail = scheduleService.getClassDetail(classId);
        return ResponseEntity.ok(classDetail);
    }

    @PostMapping
    public ResponseEntity<ScheduledClassDto> createScheduledClass(@Valid @RequestBody CreateScheduledClassRequest request) {
        log.info("📥 Recibiendo solicitud de creación de clase: {}", request);
        ScheduledClass createdClass = scheduleService.createScheduledClass(request);
        log.info("✅ Clase creada exitosamente con ID: {}", createdClass.getId());

        ScheduledClassDto responseDto = new ScheduledClassDto(
                createdClass.getId(),
                createdClass.getProfessor(),
                createdClass.getDiscipline(),
                createdClass.getLocation(),
                null, // locationAddress - not stored in ScheduledClass model
                createdClass.getDateTime(),
                createdClass.getDurationMinutes(),
                createdClass.getCapacity() - createdClass.getEnrolledCount(),
                createdClass.getCapacity(),
                createdClass.getDescription()
        );

        return ResponseEntity.created(URI.create("/api/v1/schedule/" + createdClass.getId()))
                .body(responseDto);
    }

    @GetMapping("/professor/{professorName}")
    public ResponseEntity<List<ScheduledClassDto>> getClassesByProfessor(@PathVariable String professorName) {
        List<ScheduledClassDto> classes = scheduleService.getClassesByProfessor(professorName);
        return ResponseEntity.ok(classes);
    }

    @PutMapping("/{classId}")
    public ResponseEntity<ScheduledClassDto> updateScheduledClass(
            @PathVariable String classId,
            @Valid @RequestBody CreateScheduledClassRequest request) {

        ScheduledClass updatedClass = scheduleService.updateScheduledClass(classId, request);

        ScheduledClassDto responseDto = new ScheduledClassDto(
                updatedClass.getId(),
                updatedClass.getProfessor(),
                updatedClass.getDiscipline(),
                updatedClass.getLocation(),
                null, // locationAddress - not stored in ScheduledClass model
                updatedClass.getDateTime(),
                updatedClass.getDurationMinutes(),
                updatedClass.getCapacity() - updatedClass.getEnrolledCount(),
                updatedClass.getCapacity(),
                updatedClass.getDescription()
        );

        return ResponseEntity.ok(responseDto);
    }

    @DeleteMapping("/{classId}")
    public ResponseEntity<Void> deleteScheduledClass(@PathVariable String classId) {
        scheduleService.cancelClass(classId);
        return ResponseEntity.noContent().build();
    }
}