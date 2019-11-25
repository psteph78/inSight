package com.example.insight.entity.enums;

/**
 * class used to store and read pictures uploaded
 * by users for locations
 */
public class UserPictureForLocation {
    private String id;
    private String locationName;
    private String profileImgName;
    private String profileImgUrl;

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

    public String getProfileImgName() {
        return profileImgName;
    }

    public void setProfileImgName(String profileImgName) {
        this.profileImgName = profileImgName;
    }

    public String getProfileImgUrl() {
        return profileImgUrl;
    }

    public void setProfileImgUrl(String profileImgUrl) {
        this.profileImgUrl = profileImgUrl;
    }
}
