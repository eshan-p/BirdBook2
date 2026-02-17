package com.user.user.repository;

import com.user.user.client.BirdClient;
import com.user.user.models.Bird;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Repository;
import java.util.Optional;

/**
 * Repository adapter for Bird microservice
 * Wraps Feign client to provide DAO-like interface
 */
@Repository
public class BirdDAO {
    
    private final BirdClient birdClient;
    
    public BirdDAO(BirdClient birdClient) {
        this.birdClient = birdClient;
    }
    
    public Optional<Bird> findById(ObjectId id) {
        try {
            Bird bird = birdClient.getBirdById(id.toHexString());
            return Optional.ofNullable(bird);
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
