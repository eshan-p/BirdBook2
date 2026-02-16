package com.example.bird.models;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

@Document(collection = "birds")
public class Bird {

    @Id
    @JsonProperty("_id")
    private ObjectId _id;

    @NotBlank(message = "Name cannot be blank")
    @Size(min = 2, max = 100, message = "Bird name must be between 2 and 100 characters")
    private String commonName;

    private String scientificName;

    private String imageURL;

    private List<Double> location;

    public Bird() {}

    public Bird(String commonName, String scientificName, String imageURL, List<Double> location) {
        this.commonName = commonName;
        this.scientificName = scientificName;
        this.imageURL = imageURL;
        this.location = location;
    }

    public String getId() {
        return _id != null ? _id.toHexString() : null;
    }

    public ObjectId getObjectId() {
        return _id;
    }

    public void setObjectId(ObjectId _id) {
        this._id = _id;
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

    public List<Double> getLocation() {
        return location;
    }

    public void setLocation(List<Double> location) {
        this.location = location;
    }
}