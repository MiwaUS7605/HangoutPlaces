package com.groupb.locationsharing.Model;

public class Comment {
    private String comments;
    private String publisher;
    private String commentId;

    public Comment(String comment, String publisher, String commentId) {
        this.comments = comment;
        this.publisher = publisher;
        this.commentId = commentId;
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

    public String getCommentId() {
        return commentId;
    }

    public void setCommentId(String commentId) {
        this.commentId = commentId;
    }
}
