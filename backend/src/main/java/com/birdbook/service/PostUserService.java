package com.birdbook.service;

import com.birdbook.models.PostUser;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

@Service
public class PostUserService {
    private UserService uService;

    public PostUserService (UserService uService){
        this.uService = uService;
    }
    public PostUser buildPostUser(ObjectId userId){
        PostUser temp = new PostUser();
        temp.setUserId(userId);
        temp.setUsername(uService.getUserById(userId).getUsername());
        temp.setProfilePic(uService.getUserById(userId).getProfilePic());
        return temp;
    }
}
