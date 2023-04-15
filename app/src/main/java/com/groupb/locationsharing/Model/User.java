package com.groupb.locationsharing.Model;

public class User {
    private String fullname;
    private String id;
    private String imageUrl;
    private String name;
    private String username;
    private String status;
    private String bio;
    private String findable;
    private String lat;
    private String lon;
    private String city;

    public User(String fullname, String id, String imageUrl, String name, String username, String status, String bio, String findable, String lat, String lon, String city) {
        this.fullname = fullname;
        this.id = id;
        this.imageUrl = imageUrl;
        this.name = name;
        this.username = username;
        this.status = status;
        this.bio = bio;
        this.findable = findable;
        this.lat = lat;
        this.lon = lon;
        this.city = city;
    }

    public User() {
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getFindable() {
        return findable;
    }

    public void setFindable(String findable) {
        this.findable = findable;
    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getLon() {
        return lon;
    }

    public void setLon(String lon) {
        this.lon = lon;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }
}
