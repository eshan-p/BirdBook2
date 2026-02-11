package com.birdbook.repository;

import java.util.List;
import java.util.Optional;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import com.birdbook.models.User;

@Repository
public interface UserDAO extends MongoRepository<User, ObjectId>{
    @Query("{\"username\": ?0}")
    Optional<User> findByUsername(String username);

    @Query("{ 'username': { $regex: ?0, $options: 'i' } }")
    List<User> findByUsernameContaining(String username);
}
