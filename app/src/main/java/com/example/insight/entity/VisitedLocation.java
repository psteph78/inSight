package com.example.insight.entity;

public class VisitedLocation {
    private String id;
    private String userEmail;
    private String locationName;

    public VisitedLocation() {
    }

    public VisitedLocation(String id, String userEmail, String locationName) {
        this.id = id;
        this.userEmail = userEmail;
        this.locationName = locationName;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }
}
