package com.uade.ritmofitapi.service;

import com.uade.ritmofitapi.dto.request.CreateClassTemplateRequest;
import com.uade.ritmofitapi.model.ClassTemplate;
import com.uade.ritmofitapi.repository.ClassTemplateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ClassTemplateService {

    private final ClassTemplateRepository classTemplateRepository;

    @Autowired
    public ClassTemplateService(ClassTemplateRepository classTemplateRepository) {
        this.classTemplateRepository = classTemplateRepository;
    }

    public ClassTemplate createClassTemplate(CreateClassTemplateRequest request) {
        // 1. Mapeo del DTO a la entidad de dominio
        ClassTemplate classTemplate = new ClassTemplate();
        classTemplate.setName(request.getName());
        classTemplate.setDiscipline(request.getDiscipline());
        classTemplate.setDurationMinutes(request.getDurationMinutes());
        classTemplate.setProfessor(request.getProfessor());
        classTemplate.setCapacity(request.getCapacity());

        // 2. Persistencia en la base de datos
        return classTemplateRepository.save(classTemplate);
    }

    public List<ClassTemplate> getAllClasses(String discipline, String professor) {
        if (discipline != null && !discipline.isEmpty()) {
            return classTemplateRepository.findByDiscipline(discipline);
        }
        if (professor != null && !professor.isEmpty()) {
            return classTemplateRepository.findByProfessor(professor);
        }
        return classTemplateRepository.findAll();
    }

    public Optional<ClassTemplate> getClassById(String id) {
        return classTemplateRepository.findById(id);
    }

    public List<String> getAllDisciplines() {
        return classTemplateRepository.findDistinctDiscipline();
    }
}