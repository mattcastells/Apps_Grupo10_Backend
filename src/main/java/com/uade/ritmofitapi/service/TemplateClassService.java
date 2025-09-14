package com.uade.ritmofitapi.service;

import com.uade.ritmofitapi.model.ClassTemplate;
import com.uade.ritmofitapi.repository.ClassTemplateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TemplateClassService {

    private final ClassTemplateRepository classTemplateRepository;

    @Autowired
    public TemplateClassService(ClassTemplateRepository classTemplateRepository) {
        this.classTemplateRepository = classTemplateRepository;
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
}