package com.uade.ritmofitapi.controller;

import com.uade.ritmofitapi.dto.GymClassDto;
import com.uade.ritmofitapi.model.GymClass;
import com.uade.ritmofitapi.service.GymClassService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/gym")
public class GymClassController {

    private final GymClassService gymClassService;

    @Autowired
    public GymClassController(GymClassService gymClassService) {
        this.gymClassService = gymClassService;
    }

    @GetMapping
    public ResponseEntity<List<GymClassDto>> getAllClasses(
            @RequestParam(required = false) String discipline,
            @RequestParam(required = false) String professor) {
        List<GymClass> classes = gymClassService.getAllClasses(discipline, professor);
        List<GymClassDto> classDtos = classes.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(classDtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<GymClassDto> getClassById(@PathVariable String id) {
        return gymClassService.getClassById(id)
                .map(this::convertToDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    private GymClassDto convertToDto(GymClass gymClass) {
        return new GymClassDto(
                gymClass.getId(),
                gymClass.getName(),
                gymClass.getDiscipline(),
                gymClass.getDuration(),
                gymClass.getCapacity(),
                gymClass.getProfessor(),
                gymClass.getDateTime()
        );
    }
}