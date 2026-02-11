package com.birdbook.models;

import org.bson.types.ObjectId;

import com.birdbook.serializers.ObjectIdSerializer;
import com.birdbook.serializers.ObjectIdDeserializer;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

public class PostUser {

    @JsonSerialize(using = ObjectIdSerializer.class)
    @JsonDeserialize(using = ObjectIdDeserializer.class)
    private ObjectId userId;

    private String username;

    private String profilePic; 

    public PostUser() {}

    public PostUser(ObjectId userId, String username) {
        this.userId = userId;
        this.username = username;
    }

    public PostUser(ObjectId userId, String username, String profilePic) {
        this.userId = userId;
        this.username = username;
        this.profilePic = profilePic;
    }

    public ObjectId getUserId() {
        return userId;
    }

    public void setUserId(ObjectId userId) {
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