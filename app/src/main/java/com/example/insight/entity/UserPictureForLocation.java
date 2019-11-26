package com.example.insight.entity;

/**
 * class used to store and read pictures uploaded
 * by users for locations
 */
public class UserPictureForLocation {
    private String id;
    private String locationName;
    private String encodedPicture;

    public UserPictureForLocation() {
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

    public String getEncodedPicture() {
        return encodedPicture;
    }

    public void setEncodedPicture(String encodedPicture) {
        this.encodedPicture = encodedPicture;
    }
}
