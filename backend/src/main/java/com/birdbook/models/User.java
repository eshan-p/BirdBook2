package com.birdbook.models;

import com.birdbook.models.Role;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Document(collection = "users")
public class User {

    @Id
    private ObjectId id;

    @NotBlank(message = "Username cannot be blank.")
    @Pattern(
        regexp = "^[a-zA-Z0-9]{5,20}$",
        message = "Username must be 5 to 20 characters with no special characters"
    )
    private String username;

    @NotBlank(message = "Password cannot be blank.")
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{4,12}$",
        message = "Password must be 4–12 chars with uppercase, lowercase, digit, and special character"
    )
    private String password;

    private Role role;

    private String profilePic;
    private String firstName;
    private String lastName;
    private String location;
    private Boolean onboardingComplete = false;

    private ObjectId[] friends;
    private ObjectId[] posts;
    private ObjectId[] groups;

    public User() {
    }

    public User(String username, String password) {
        this.id = new ObjectId();
        this.username = username;
        this.password = password;
        this.role = (role == null) ? Role.BASIC_USER : role;
        this.profilePic = "/profile_pictures/default_pfp.jpg";
        this.firstName = null;
        this.lastName = null;
        this.location = null;
        this.onboardingComplete = false;
        this.friends = new ObjectId[0];
        this.posts = new ObjectId[0];
        this.groups = new ObjectId[0];
    }

    public User(
        ObjectId id,
        String username,
        String password,
        Role role,
        String profilePic,
        String firstName,
        String lastName,
        String location,
        Boolean onboardingComplete,
        ObjectId[] friends,
        ObjectId[] posts,
        ObjectId[] groups
    ) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.role = role;
        this.profilePic = profilePic;
        this.firstName = firstName;
        this.lastName = lastName;
        this.location = location;
        this.onboardingComplete = onboardingComplete;
        this.friends = friends;
        this.posts = posts;
        this.groups = groups;
    }

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

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
}
