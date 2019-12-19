package com.example.insight.entity;

public class Exchange {
    private String id;
    private Integer points;
    private String description;
    private String title;
    private String qrCode;

    public Exchange() {}

    public Exchange( String title,String description, Integer points,  String qrCode) {
        this.points = points;
        this.description = description;
        this.title = title;
        this.qrCode = qrCode;
    }

    public Exchange(String id, String title, String description, Integer points, String qrCode) {
        this.id = id;
        this.points = points;
        this.description = description;
        this.title = title;
        this.qrCode = qrCode;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getPoints() {
        return points;
    }

    public void setPoints(Integer points) {
        this.points = points;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getQrCode() {
        return qrCode;
    }

    public void setQrCode(String qrCode) {
        this.qrCode = qrCode;
    }
}
