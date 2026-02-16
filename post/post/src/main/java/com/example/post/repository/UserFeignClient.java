package com.example.post.repository;
import com.example.post.models.User;
import org.bson.types.ObjectId;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;

import java.util.List;

@FeignClient(name = "user", url = "http://localhost:8761")
public interface UserFeignClient {
    @GetMapping("/users/{id}")
    User getUserById(@PathVariable("id") String id);

    @GetMapping("/users/{id}/friends")
    List<User> getAllById(@PathVariable("id") List<ObjectId> id);

    @PutMapping("/users/{id}/posts/{postId}")
    void addPostToUser(
            @PathVariable("id") String id,
            @PathVariable("postId") String postId
    );
}






