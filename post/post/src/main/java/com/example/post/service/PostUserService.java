package com.example.post.service;


import com.example.post.models.PostUser;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

@Service
public class PostUserService {

    private UserService uService;

    public PostUserService (UserService uService){
        this.uService = uService;
    }

    public PostUser buildPostUser(String userId){
        PostUser temp = new PostUser();
        temp.setUserId(userId);
        temp.setUsername(uService.getUserById(String.valueOf(userId)).getUsername());
        temp.setProfilePic(uService.getUserById(String.valueOf(userId)).getProfilePic());
        return temp;
    }

}
