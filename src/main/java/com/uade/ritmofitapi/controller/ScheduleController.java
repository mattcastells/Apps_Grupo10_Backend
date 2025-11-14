package com.uade.ritmofitapi.controller;

import com.uade.ritmofitapi.dto.response.ScheduledClassDto;
import com.uade.ritmofitapi.service.ScheduleService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
}