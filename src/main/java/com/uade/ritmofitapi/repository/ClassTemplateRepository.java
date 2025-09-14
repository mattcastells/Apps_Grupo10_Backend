package com.uade.ritmofitapi.repository;

import com.uade.ritmofitapi.model.ClassTemplate;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ClassTemplateRepository extends MongoRepository<ClassTemplate, String> {
    List<ClassTemplate> findByProfessor(String professor);
    List<ClassTemplate> findByDiscipline(String discipline);
}
