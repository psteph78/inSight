package com.example.insight.activity.builder;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MarkerBuilder {
    private LatLng coordinates;
    private String title;
    private BitmapDescriptor icon;

    public MarkerBuilder(LatLng coordinates, String title, BitmapDescriptor icon) {
        this.coordinates = coordinates;
        this.title = title;
        this.icon = icon;
    }

    public MarkerBuilder setCoordinates(LatLng coordinates){
        this.coordinates = coordinates;
        return this;
    }

    public MarkerBuilder setTitle(String title){
        this.title = title;
        return this;
    }

    public MarkerBuilder setIcon(BitmapDescriptor bitmapDescriptor){
        this.icon = bitmapDescriptor;
        return this;
    }

    public MarkerOptions build(){
        return new MarkerOptions().title(this.title).position(this.coordinates).icon(this.icon);
    }
}
