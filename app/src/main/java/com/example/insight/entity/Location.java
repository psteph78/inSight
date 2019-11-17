package com.example.insight.entity;

import com.example.insight.entity.enums.LocationType;
import com.google.android.gms.maps.model.LatLng;

public class Location {
    private String id;
    private LatLng coordinates;
    private String title;
    private String description;
    private LocationType type;
    private Integer rewardPoints;

    public Location() {
    }

    public Location(LatLng coordinates, String title, String description, LocationType type, Integer rewardPoints) {
        this.coordinates = coordinates;
        this.title = title;
        this.description = description;
        this.type = type;
        this.rewardPoints = rewardPoints;
    }

    public Integer getRewardPoints() {
        return rewardPoints;
    }

    public void setRewardPoints(Integer rewardPoints) {
        this.rewardPoints = rewardPoints;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public LatLng getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(LatLng coordinates) {
        this.coordinates = coordinates;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocationType getType() {
        return type;
    }

    public void setType(LocationType type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "Location{" +
                "id='" + id + '\'' +
                ", coordinates=" + coordinates +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", type=" + type +
                '}';
    }
}
