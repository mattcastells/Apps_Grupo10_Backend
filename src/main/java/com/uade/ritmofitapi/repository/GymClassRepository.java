// GymClassRepository.java
package com.uade.ritmofitapi.repository;

import com.uade.ritmofitapi.model.GymClass;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface GymClassRepository extends MongoRepository<GymClass, String> {
    List<GymClass> findByProfessor(String professor);
    List<GymClass> findByDiscipline(String discipline);
}
