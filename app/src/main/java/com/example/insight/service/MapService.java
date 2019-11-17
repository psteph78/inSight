package com.example.insight.service;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.insight.R;
import com.example.insight.activity.mapActivity.MapsActivity;
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
import java.util.Map;

public class MapService {
    private List<Location> locations;
    private List<Marker> markers;
    private DatabaseReference database;
    private GoogleMap map;

    private Dialog locationDialog;

    public List<Location> getLocations() {
        return locations;
    }

    public MapService(GoogleMap map, Context context) {

        locationDialog = new Dialog(context);

        this.locations = new ArrayList<>();
        this.markers = new ArrayList<>();
        this.database = FirebaseDatabase.getInstance().getReference("locations");
        this.map = map;
        //initDatabase();
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

                map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(Marker marker) {
                        Log.d("Clicked location", "LOCATION MARKER CLICKED");
                        ShowPopUpLocation(marker);
                        return true;
                    }
                });
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
        locations.add(new Location(new LatLng(46.7695133,23.5898073), "Matei Corvin Statue", "Description", LocationType.MONUMENT, 25));
        locations.add(new Location(new LatLng(46.769334,23.589890), "'Unirii' Main Square", "Description", LocationType.MAIN_SQUARE, 30));
        locations.add(new Location(new LatLng(46.769977,23.589429), "Romano-Catholic Church 'Sfantul Mihail'", "Description", LocationType.CHURCH, 45));
        locations.add(new Location(new LatLng(46.767882,23.591431), "'Babes-Bolyai' Univeristy", "Description", LocationType.UNIVERSITY, 20));
        locations.add(new Location(new LatLng(46.770564,23.590454), "Art Museum", "Description", LocationType.MUSEUM, 25));
        locations.add(new Location(new LatLng(46.772052,23.596693), "Mithropolitan Cathedral 'Adormirea Maicii Domnului'", "Description", LocationType.CHURCH, 40));
        locations.add(new Location(new LatLng(46.771122,23.597104), "Avram Iancu Statue", "Description", LocationType.MONUMENT, 25));
        locations.add(new Location(new LatLng(46.769924,23.597887), "National Operahouse", "Description", LocationType.THATRE, 50));
        locations.add(new Location(new LatLng(46.769419,23.597950), "Operahouse Park", "Description", LocationType.PARK, 35));

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

    public void ShowPopUpLocation(Marker marker){
        TextView locationName;
        TextView locationPoints;
        ImageView locationType;

        locationDialog.setContentView(R.layout.pop_up_location);

        locationName = locationDialog.findViewById(R.id.location_name);
        locationPoints = locationDialog.findViewById(R.id.location_points);
        locationType = locationDialog.findViewById(R.id.location_type_img);

        //sets title of location
        locationName.setText(marker.getTitle());

        //sets points of location
//        Spannable nrPoints = new SpannableString(getPointsOfLocation(marker.getTitle()).toString());
//        nrPoints.setSpan(new ForegroundColorSpan(Color.rgb(23,104,120)), 0, nrPoints.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//        nrPoints.setSpan(new RelativeSizeSpan(1.4f), 0, nrPoints.length(),
//                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//        nrPoints.setSpan(new StyleSpan(Typeface.BOLD), 0, nrPoints.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//
//        locationPoints.setText(nrPoints);
//        locationPoints.append("\n");
//
//        Spannable points = new SpannableString("total points");
//        points.setSpan(new ForegroundColorSpan(Color.rgb(128,128,128)), 0, points.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//        points.setSpan(new RelativeSizeSpan(0.5f), 0, points.length(),
//                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//        locationPoints.append(points);
//
//        String locationTypeEnum = getTypeOfLocation(marker.getTitle());

        //String uri = "@drawable/".concat(locationTypeEnum);
//        int imageResource = getResources().getIdentifier(uri, null, getPackageName());
//        Drawable res = getResources().getDrawable(imageResource);
//        locationType.setImageDrawable(res);

        locationDialog.show();
    }

    private Integer getPointsOfLocation(String locationName){
        for(Location location: this.getLocations()){
            if (location.getTitle().equals(locationName)){
                return location.getRewardPoints();
            }
        }
        return null;
    }

    private String getTypeOfLocation(String locationName){
        for(Location location: this.getLocations()){
            if (location.getTitle().equals(locationName)){
                return location.getType().toString().toLowerCase();
            }
        }
        return null;
    }
}
