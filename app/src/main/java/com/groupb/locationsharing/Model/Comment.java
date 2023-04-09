package com.groupb.locationsharing.Model;

public class Comment {
    private String comments;
    private String publisher;

    public Comment(String comment, String publisher) {
        this.comments = comment;
        this.publisher = publisher;
    }

    public Comment() {
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }
}
