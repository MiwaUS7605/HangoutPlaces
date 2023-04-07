package com.groupb.locationsharing.Model;

public class Post {
    private String postDescription;
    private String postId;
    private String postImage;
    private String publisher;

    public Post() {
    }

    public Post(String postDescription, String postId, String postImage, String publisher) {
        this.postDescription = postDescription;
        this.postId = postId;
        this.postImage = postImage;
        this.publisher = publisher;
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
}
