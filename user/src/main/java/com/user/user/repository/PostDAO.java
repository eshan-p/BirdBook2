package com.user.user.repository;

import com.user.user.client.PostClient;
import com.user.user.models.Post;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Repository adapter for Post microservice
 * Wraps Feign client to provide DAO-like interface
 */
@Repository
public class PostDAO {
    
    private final PostClient postClient;
    
    public PostDAO(PostClient postClient) {
        this.postClient = postClient;
    }
    
    public Optional<Post> findById(ObjectId id) {
        try {
            Post post = postClient.getPostById(id.toHexString());
            return Optional.ofNullable(post);
        } catch (Exception e) {
            return Optional.empty();
        }
    }
    
    public List<Post> findAllById(List<ObjectId> ids) {
        try {
            List<String> hexIds = ids.stream()
                    .map(ObjectId::toHexString)
                    .collect(Collectors.toList());
            return postClient.getPostsByIds(hexIds);
        } catch (Exception e) {
            return List.of();
        }
    }
    
    public List<Post> findByUserId(ObjectId userId) {
        try {
            return postClient.getPostsByUserId(userId.toHexString());
        } catch (Exception e) {
            return List.of();
        }
    }
}
