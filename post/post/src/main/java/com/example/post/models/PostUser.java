package com.example.post.models;



import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

public class PostUser {


    private String userId;

    private String username;

    private String profilePic; 

    public PostUser() {}

    public PostUser(String userId, String username) {
        this.userId = userId;
        this.username = username;
    }

    public PostUser(String userId, String username, String profilePic) {
        this.userId = userId;
        this.username = username;
        this.profilePic = profilePic;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getProfilePic() {
        return profilePic;
    }

    public void setProfilePic(String profilePic) {
        this.profilePic = profilePic;
    }
}