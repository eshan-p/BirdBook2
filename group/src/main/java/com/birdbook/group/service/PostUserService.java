package com.birdbook.group.service;

import com.birdbook.group.client.UserClient;
import com.birdbook.group.models.PostUser;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;
import java.util.Map;

@Service
public class PostUserService {
    private final UserClient userClient;

    public PostUserService(UserClient userClient) {
        this.userClient = userClient;
    }

    public PostUser buildPostUser(ObjectId userId) {
        try {
            Map<String, Object> userData = userClient.getUser(userId.toHexString());
            
            PostUser postUser = new PostUser();
            postUser.setUserId(userId);
            postUser.setUsername((String) userData.get("username"));
            postUser.setProfilePic((String) userData.get("profilePic"));
            
            return postUser;
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to fetch user data: " + e.getMessage());
        }
    }
}
