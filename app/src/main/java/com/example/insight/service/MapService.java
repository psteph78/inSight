package com.example.insight.service;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.insight.entity.Location;
import com.example.insight.entity.enums.LocationType;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MapService {
    private List<Location> locations;
    private List<Marker> markers;
    private DatabaseReference database;
    private GoogleMap map;

    public MapService(GoogleMap map) {
        //initDatabase();
        this.locations = new ArrayList<>();
        this.markers = new ArrayList<>();
        this.database = FirebaseDatabase.getInstance().getReference("locations");
        this.map = map;
        queryLocations();
        //Log.d("constructor", "size of list is: " + locations.size());
        //this.map = setMap(map);
    }

    public GoogleMap getMap() {
        return map;
    }

    private void queryLocations(){
        Log.d("query", "before query");
        database.orderByChild("id").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                locations = new ArrayList<>();
                Log.d("dataChange", "in event listener");
                for (DataSnapshot entity : dataSnapshot.getChildren()){
                    Location toBuild = new Location();

                    toBuild.setId(entity.child("id").getValue().toString());
                    toBuild.setTitle(entity.child("title").toString());
                    toBuild.setDescription(entity.child("description").toString());
                    toBuild.setType(LocationType.valueOf((String)entity.child("type").getValue()));

                    double x = (Double)entity.child("coordinates/latitude").getValue();
                    double y = (Double)entity.child("coordinates/longitude").getValue();

                    LatLng coordinates = new LatLng(x,y);
                    toBuild.setCoordinates(coordinates);

                    Log.d("dataChange", toBuild.toString());

                    locations.add(toBuild);
                }
                Log.d("dataChange", "size of list is: " + locations.size());

                Log.d("setMap","In function");
                Log.d("setMap", "Size of locationsList is: " + locations.size());

                map.clear();

                for (Location location : locations) {
                    Log.d("building","building new location");
                    System.out.println(location.toString());
                    LatLng coordinates = location.getCoordinates();

                    Marker newMarker = map.addMarker(new MarkerOptions()
                            .position(coordinates)
                            .title(location.getTitle()));
                    newMarker.setTag(0);
                    markers.add(newMarker);
                    Log.d("building","added new marker");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("db error", "cancelled", databaseError.toException().getCause());
            }
        });
    }

    public GoogleMap setMap(GoogleMap map){
        Log.d("setMap","In function");
        Log.d("setMap", "Size of locationsList is: " + locations.size());
        for (Location location : locations) {
            Log.d("building","building new location");
            System.out.println(location.toString());
            LatLng coordinates = location.getCoordinates();

            Marker newMarker = map.addMarker(new MarkerOptions()
                    .position(coordinates)
                    .title(location.getTitle()));
            newMarker.setTag(0);
            markers.add(newMarker);
            Log.d("building","added new marker");
        }
        for (Location x: locations){

            Log.d("LOCATION",x.toString());
        }
        return map;
    }

    public List<Marker> getMarkers() {
        return markers;
    }

    private List<Location> initLocationList(){
        List<Location> locations = new ArrayList<>();

        // add below new locations
        locations.add(new Location(new LatLng(46.7695133,23.5898073), "Matei Corvin Statue", "Description", LocationType.MONUMENT));
        locations.add(new Location(new LatLng(46.769334,23.589890), "'Unirii' Main Square", "Description", LocationType.MAIN_SQUARE));
        locations.add(new Location(new LatLng(46.769977,23.589429), "Romano-Catholic Church 'Sfantul Mihail'", "Description", LocationType.CHURCH));
        locations.add(new Location(new LatLng(46.767882,23.591431), "'Babes-Bolyai' Univeristy", "Description", LocationType.UNIVERSITY));
        locations.add(new Location(new LatLng(46.770564,23.590454), "Art Museum", "Description", LocationType.MUSEUM));
        locations.add(new Location(new LatLng(46.772052,23.596693), "Mithropolitan Cathedral 'Adormirea Maicii Domnului'", "Description", LocationType.CHURCH));
        locations.add(new Location(new LatLng(46.771122,23.597104), "Avram Iancu Statue", "Description", LocationType.MONUMENT));
        locations.add(new Location(new LatLng(46.769924,23.597887), "National Operahouse", "Description", LocationType.THATRE));
        locations.add(new Location(new LatLng(46.769419,23.597950), "Operahouse Park", "Description", LocationType.PARK));

        return locations;
    }

    // TODO: Run this only when initializing database! Delete all entities before calling this again!
    private void initDatabase(){
        List<Location> locations = initLocationList();
        for (Location location : locations){
            // get key from database
            String id = database.push().getKey();
            location.setId(id);

            // add entity to database
            database.child(location.getId()).setValue(location);
        }
    }
}
