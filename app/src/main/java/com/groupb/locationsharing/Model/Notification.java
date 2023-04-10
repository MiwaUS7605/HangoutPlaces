package com.groupb.locationsharing.Model;

public class Notification {
    private String userId;
    private String text;
    private String postId;
    private String isPost;

    public Notification(String userId, String text, String postId, String isPost) {
        this.userId = userId;
        this.text = text;
        this.postId = postId;
        this.isPost = isPost;
    }

    public Notification() {
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public String getIsPost() {
        return isPost;
    }

    public void setIsPost(String isPost) {
        this.isPost = isPost;
    }
}
