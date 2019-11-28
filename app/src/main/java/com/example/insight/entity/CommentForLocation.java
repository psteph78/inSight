package com.example.insight.entity;

/**
 * class used to store and read user comments left on locations
 * an entity contains the name of the location and
 * its corresponding comment
 */
public class CommentForLocation {
    String id;
    String locationName;
    String userComment;
    String userEmail;

    public CommentForLocation() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public String getUserComment() {
        return userComment;
    }

    public void setUserComment(String userComment) {
        this.userComment = userComment;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }
}
