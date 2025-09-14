package com.uade.ritmofitapi.controller;

import com.uade.ritmofitapi.dto.request.CreateClassTemplateRequest;
import com.uade.ritmofitapi.dto.response.ClassTemplateDto;
import com.uade.ritmofitapi.model.ClassTemplate;
import com.uade.ritmofitapi.service.ClassTemplateService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@Slf4j
@RestController
@RequestMapping("/api/v1/class-templates")
public class TemplateController {

    private final ClassTemplateService classTemplateService;

    public TemplateController(ClassTemplateService classTemplateService) {
        this.classTemplateService = classTemplateService;
    }

    @PostMapping
    public ResponseEntity<ClassTemplateDto> createClassTemplate(@Valid @RequestBody CreateClassTemplateRequest request) {
        // El servicio ahora recibe el request DTO
        ClassTemplate createdClass = classTemplateService.createClassTemplate(request);

        // Mapeamos la entidad a un DTO de respuesta
        ClassTemplateDto responseDto = new ClassTemplateDto(
                createdClass.getId(),
                createdClass.getName(),
                createdClass.getDiscipline(),
                createdClass.getDurationMinutes(),
                createdClass.getCapacity()
        );

        // Devolvemos un 201 Created con la ubicación del nuevo recurso
        return ResponseEntity.created(URI.create("/api/v1/class-templates/" + createdClass.getId()))
                .body(responseDto);
    }
}
