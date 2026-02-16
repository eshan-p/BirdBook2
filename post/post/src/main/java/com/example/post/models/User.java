package com.example.post.models;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;


public class User {

    private String id;
    private String username;
    private String profilePic;
    private String firstName;
    private String lastName;
    private ObjectId[] friends;
    private ObjectId[] posts;
    private ObjectId[] groups;

    public User() {}


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public ObjectId[] getFriends() {
        return friends;
    }

    public void setFriends(ObjectId[] friends) {
        this.friends = friends;
    }

    public ObjectId[] getPosts() {
        return posts;
    }

    public void setPosts(ObjectId[] posts) {
        this.posts = posts;
    }

    public ObjectId[] getGroups() {
        return groups;
    }

    public void setGroups(ObjectId[] groups) {
        this.groups = groups;
    }

    /*
        public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Boolean getOnboardingComplete() {
        return onboardingComplete;
    }

    public void setOnboardingComplete(Boolean onboardingComplete) {
        this.onboardingComplete = onboardingComplete;
    }


     */
}

