package com.uade.ritmofitapi.repository;

import com.uade.ritmofitapi.model.Location;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LocationRepository extends MongoRepository<Location, String> {

}