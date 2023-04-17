package com.groupb.locationsharing.Model;

public class Post {
    private String postDescription;
    private String postId;
    private String postImage;
    private String publisher;
    private String time;

    public Post() {
    }

    public Post(String postDescription, String postId, String postImage, String publisher, String time) {
        this.postDescription = postDescription;
        this.postId = postId;
        this.postImage = postImage;
        this.publisher = publisher;
        this.time = time;
    }

    public String getPostDescription() {
        return postDescription;
    }

    public void setPostDescription(String postDescription) {
        this.postDescription = postDescription;
    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public String getPostImage() {
        return postImage;
    }

    public void setPostImage(String postImage) {
        this.postImage = postImage;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
