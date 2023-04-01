package com.groupb.locationsharing.Model;

public class User {
    private String id;
    private String username;
    private String imageUrl;
    private String status;

    public User(String id, String username, String imageUrl, String status) {
        this.id = id;
        this.username = username;
        this.imageUrl = imageUrl;
        this.status = status;
    }

    public User(){

    }

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

    public String getImageUrl() {
        return imageUrl;
    }
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
