package com.user.user.models;

import org.bson.types.ObjectId;
import java.util.Date;

/**
 * DTO for Post data from Post microservice
 */
public class Post {
    private ObjectId id;
    private ObjectId bird;
    private ObjectId userId;
    private Date timestamp;

    public Post() {}

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public ObjectId getBird() {
        return bird;
    }

    public void setBird(ObjectId bird) {
        this.bird = bird;
    }

    public ObjectId getUserId() {
        return userId;
    }

    public void setUserId(ObjectId userId) {
        this.userId = userId;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}
