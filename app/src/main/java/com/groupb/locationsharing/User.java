package com.groupb.locationsharing;

public class User {

    public String username;
    public String email;
    public String profileUrl;

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(String username, String email) {
        this.username = username;
        this.email = email;

    }

}