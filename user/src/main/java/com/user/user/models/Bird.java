package com.user.user.models;

import org.bson.types.ObjectId;

/**
 * DTO for Bird data from Bird microservice
 */
public class Bird {
    private ObjectId id;
    private String commonName;
    private String scientificName;
    private String imageURL;

    public Bird() {}

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public String getCommonName() {
        return commonName;
    }

    public void setCommonName(String commonName) {
        this.commonName = commonName;
    }

    public String getScientificName() {
        return scientificName;
    }

    public void setScientificName(String scientificName) {
        this.scientificName = scientificName;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }
}
