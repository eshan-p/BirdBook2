package com.example.bird.repository;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.example.bird.models.Bird;

@Repository
public interface BirdDAO extends MongoRepository<Bird, ObjectId> {
    Bird findByCommonName(String commonName);
}
