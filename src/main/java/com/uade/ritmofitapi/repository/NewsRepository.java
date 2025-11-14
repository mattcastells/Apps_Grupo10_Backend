package com.uade.ritmofitapi.repository;

import com.uade.ritmofitapi.model.News;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface NewsRepository extends MongoRepository<News, String> {
    List<News> findAllByOrderByPublishedDateDesc();
}
