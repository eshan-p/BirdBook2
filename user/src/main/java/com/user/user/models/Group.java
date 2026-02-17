package com.user.user.models;

import org.bson.types.ObjectId;

/**
 * DTO for Group data from Group microservice
 */
public class Group {
    private ObjectId id;
    private String name;
    private String description;

    public Group() {}

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
