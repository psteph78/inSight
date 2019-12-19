package com.example.insight.entity;

public class User {
    private String id;
    private String username;
    private String email;
    private Integer points;
    private String profileImgName;
    private String profileImgUrl;

    public User() {
    }

    public User(String id, String username, String email, Integer points, String profileImgName, String profileImgUrl) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.points = points;
        this.profileImgName = profileImgName;
        this.profileImgUrl = profileImgUrl;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Integer getPoints() {
        return points;
    }

    public void setPoints(Integer points) {
        this.points = points;
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
