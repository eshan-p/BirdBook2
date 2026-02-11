package com.birdbook.repository;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.birdbook.models.Group;

@Repository
public interface GroupDAO extends MongoRepository<Group, ObjectId> {

}
