package com.example.post.models;

import com.example.post.serializers.ObjectIdDeserializer;
import com.example.post.serializers.ObjectIdSerializer;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Document(collection = "posts")
public class Post {

    @Id
    @JsonSerialize(using = ObjectIdSerializer.class)
    @JsonDeserialize(using = ObjectIdDeserializer.class)
    private ObjectId id;

    private PostUser user;

    @NotBlank(message = "Header cannot be blank")
    @Size(max = 100, message = "Header cannot exceed 280 characters.")
    private String header;

    private Map<String, String> tags;

    @JsonSerialize(using = ObjectIdSerializer.class)
    @JsonDeserialize(using = ObjectIdDeserializer.class)
    private ObjectId bird;

    private Boolean flagged = false;

    @JsonSerialize(using = ObjectIdSerializer.class)
    @JsonDeserialize(using = ObjectIdDeserializer.class)
    private ObjectId group;
    
    private Boolean help = false;

    @JsonSerialize(contentUsing = ObjectIdSerializer.class)
    @JsonDeserialize(contentUsing = ObjectIdDeserializer.class)
    private List<ObjectId> likes = new ArrayList<>();

    private String image;

    @NotBlank(message = "Description cannot be blank")
    @Size(max = 280, message = "Description cannot exceed 280 characters.")
    private String textBody;

    private Date timestamp = new Date();
    private List<Comment> comments = new ArrayList<>();

    // Required by Spring Data
    public Post() {}

    // Optional convenience constructor
    public Post(String header, ObjectId bird, String textBody) {
        this.header = header;
        this.bird = bird;
        this.textBody = textBody;
        this.timestamp = new Date();
    }

    // Getters and Setters

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public PostUser getUser() {
        return user;
    }

    /*
    public ObjectId getUserId(){
        return user.getUserId();
    }*/
    
    public void setUser(PostUser user) {
        this.user = user;
    }

    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public Map<String, String> getTags() {
        return tags;
    }

    public void setTags(Map<String, String> tags) {
        this.tags = tags;
    }

    public ObjectId getBird() {
        return bird;
    }

    public void setBird(ObjectId bird) {
        this.bird = bird;
    }

    public Boolean getFlagged() {
        return flagged;
    }

    public void setFlagged(Boolean flagged) {
        this.flagged = flagged;
    }

    public ObjectId getGroup() {
        return group;
    }

    public void setGroup(ObjectId group) {
        this.group = group;
    }

    public Boolean getHelp() {
        return help;
    }

    public void setHelp(Boolean help) {
        this.help = help;
    }

    public List<ObjectId> getLikes() {
        return likes;
    }

    public void setLikes(List<ObjectId> likes) {
        this.likes = likes;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getTextBody() {
        return textBody;
    }

    public void setTextBody(String textBody) {
        this.textBody = textBody;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public List<Comment> getComments() {
        return comments;
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }

    public Boolean isFlagged() {
        return flagged;
    }

    public Boolean isHelp() {
        return help;
    }

}
