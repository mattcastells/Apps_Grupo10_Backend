// GymClassRepository.java
package com.uade.ritmofitapi.repository;

import com.uade.ritmofitapi.model.ScheduledClass;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ScheduledClassRepository extends MongoRepository<ScheduledClass, String> {
    List<ScheduledClass> findByProfessor(String professor);
    List<ScheduledClass> findAllByDateTimeBetween(LocalDateTime startDateTime, LocalDateTime endDateTime);
}
