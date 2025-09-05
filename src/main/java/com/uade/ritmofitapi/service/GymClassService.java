package com.uade.ritmofitapi.service;

import com.uade.ritmofitapi.model.GymClass;
import com.uade.ritmofitapi.repository.GymClassRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class GymClassService {

    private final GymClassRepository gymClassRepository;

    @Autowired
    public GymClassService(GymClassRepository gymClassRepository) {
        this.gymClassRepository = gymClassRepository;
    }

    public List<GymClass> getAllClasses(String discipline, String professor) {
        if (discipline != null && !discipline.isEmpty()) {
            return gymClassRepository.findByDiscipline(discipline);
        }
        if (professor != null && !professor.isEmpty()) {
            return gymClassRepository.findByProfessor(professor);
        }
        return gymClassRepository.findAll();
    }

    public Optional<GymClass> getClassById(String id) {
        return gymClassRepository.findById(id);
    }
}