package com.uade.ritmofitapi.controller;

import com.uade.ritmofitapi.dto.response.ScheduledClassDto;
import com.uade.ritmofitapi.service.ScheduleService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    @GetMapping("/{classId}")
    public ResponseEntity<ScheduledClassDto> getClassDetail(@PathVariable String classId) {
        ScheduledClassDto classDetail = scheduleService.getClassDetail(classId);
        return ResponseEntity.ok(classDetail);
    }
}