package com.birdbook.models;

import com.birdbook.serializers.ObjectIdSerializer;
import com.birdbook.serializers.ObjectIdDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Date;

public class Comment {

    private PostUser user;
    
    @NotBlank(message = "Comment cannot be blank")
    @Size(max = 280, message = "Comment cannot exceed 280 characters.")
    private String textBody;
    private Date timestamp;

    public Comment() {}

    public Comment(PostUser user, String textBody) {
        this.user = user;
        this.textBody = textBody;
        this.timestamp = new Date();
    }

    public PostUser getUser() {
        return user;
    }

    public void setUser(PostUser user) {
        this.user = user;
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
}
