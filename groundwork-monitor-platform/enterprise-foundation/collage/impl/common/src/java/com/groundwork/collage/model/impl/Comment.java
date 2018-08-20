package com.groundwork.collage.model.impl;

import java.io.Serializable;
import java.util.Date;

public class Comment implements Serializable, com.groundwork.collage.model.Comment {

    private static final long serialVersionUID = 1;

    private Integer commentId;
    private String notes;
    private String author;
    private Date createdOn;
    private Integer hostId;
    private Integer serviceStatusId;

    public Comment() {
    }

    public Integer getCommentId() {
        return commentId;
    }

    public void setCommentId(Integer commentId) {
        this.commentId = commentId;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public Date getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(Date createdOn) {
        this.createdOn = createdOn;
    }

    public Integer getHostId() {
        return hostId;
    }

    public void setHostId(Integer hostId) {
        this.hostId = hostId;
    }

    public Integer getServiceStatusId() {
        return serviceStatusId;
    }

    public void setServiceStatusId(Integer serviceStatusId) {
        this.serviceStatusId = serviceStatusId;
    }

    @Override
    public String toString() {
        return String.format("Comment [id=%d author=%s createdOn=%s]: %s", commentId, author, createdOn, notes);
    }
}
