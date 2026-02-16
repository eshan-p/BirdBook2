package com.example.post.service;


import com.example.post.models.User;
import com.example.post.repository.UserFeignClient;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserFeignClient ufClient;

    @Autowired
    public UserService(UserFeignClient ufClient) {
        this.ufClient = ufClient;
    }

    public User getUserById(String id) {
        // Calls the remote service via Feign
        return ufClient.getUserById(id);
    }

    public List<User> findAllById(List<ObjectId> ids){
        return ufClient.getAllById(ids);
    }

    public void addPostToUser(String userId, String postId){
        ufClient.addPostToUser(userId,postId);
    }
}

