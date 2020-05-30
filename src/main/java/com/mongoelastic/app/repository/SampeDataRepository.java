package com.mongoelastic.app.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.mongoelastic.app.entity.SampleData;

@Repository
public interface SampeDataRepository extends MongoRepository<SampleData, Long> {

}
