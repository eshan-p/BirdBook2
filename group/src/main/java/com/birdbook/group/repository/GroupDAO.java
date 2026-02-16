package com.birdbook.group.repository;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.birdbook.group.models.Group;

@Repository
public interface GroupDAO extends MongoRepository<Group, ObjectId> {

}
