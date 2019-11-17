package com.example.insight.activity.mapActivity;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LevelListDrawable;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.method.ScrollingMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.insight.R;
import com.example.insight.entity.Location;
import com.example.insight.entity.User;
import com.example.insight.entity.VisitedLocation;
import com.example.insight.entity.enums.LocationType;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener {

    private GoogleMap mMap;
    private List<Location> locations;
    private List<Marker> markers;
    private DatabaseReference database;
    private LocationManager locationManager;
    private Set<String> visitedLocations;

    private android.location.Location currentUserLocation;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;

    private Dialog locationDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        this.locations = new ArrayList<>();
        this.markers = new ArrayList<>();
        this.database = FirebaseDatabase.getInstance().getReference("locations");
        setContentView(R.layout.activity_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        getVisitedLocationsOfUser();
        locationDialog = new Dialog(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.mMap = googleMap;
        mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style));
        Log.d("db", "reading");
        database.orderByChild("id").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                locations = new ArrayList<>();
                for (DataSnapshot entity : dataSnapshot.getChildren()) {
                    Location toBuild = new Location();

                    toBuild.setId(entity.child("id").getValue().toString());
                    toBuild.setTitle(entity.child("title").getValue().toString());
                    toBuild.setDescription(entity.child("description").getValue().toString());
                    toBuild.setType(LocationType.valueOf((String) entity.child("type").getValue()));
                    toBuild.setRewardPoints(Integer.valueOf(entity.child("rewardPoints").getValue().toString()));

                    double x = (Double) entity.child("coordinates/latitude").getValue();
                    double y = (Double) entity.child("coordinates/longitude").getValue();

                    LatLng coordinates = new LatLng(x, y);
                    toBuild.setCoordinates(coordinates);

                    locations.add(toBuild);
                }

                mMap.clear();

                if (ContextCompat.checkSelfPermission(MapsActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) ==
                        PackageManager.PERMISSION_GRANTED &&
                        ContextCompat.checkSelfPermission(MapsActivity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) ==
                                PackageManager.PERMISSION_GRANTED) {
                    mMap.setMyLocationEnabled(true);
                    mMap.getUiSettings().setMyLocationButtonEnabled(true);
                    android.location.Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    currentUserLocation = location;
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 16f));
                } else {
                    ActivityCompat.requestPermissions(MapsActivity.this, new String[] {
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION },
                            12);
                    mMap.setMyLocationEnabled(true);
                    mMap.getUiSettings().setMyLocationButtonEnabled(true);
                    android.location.Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    currentUserLocation = location;
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 16f));
                }

                for (Location location : locations) {
                    System.out.println(location.toString());
                    LatLng coordinates = location.getCoordinates();

                    int height = 100;
                    int width = 100;
                    BitmapDrawable bitmapdraw;

                    //already visited locations will be marked with blue icons
                    if(visitedLocations.contains(location.getTitle())){
                        bitmapdraw=(BitmapDrawable)getResources().getDrawable(R.drawable.applogo);
                    }
                    //unvisited location will be marked with pink icons
                    else{
                        bitmapdraw=(BitmapDrawable)getResources().getDrawable(R.drawable.unvisited);
                    }

                    Bitmap b=bitmapdraw.getBitmap();
                    Bitmap smallMarker = Bitmap.createScaledBitmap(b, width, height, false);

                    Marker newMarker = mMap.addMarker(new MarkerOptions()
                            .position(coordinates)
                            .title(location.getTitle())
                            .anchor(0.5f, 0.5f)
                            .icon(BitmapDescriptorFactory.fromBitmap(smallMarker)));
                    newMarker.setTag(0);
                    markers.add(newMarker);
                }

                mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
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

        if (mMap == null){
            Toast.makeText(this,"Error loading map!", Toast.LENGTH_LONG).show();
        }

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
    }

    public void ShowPopUpLocation(final Marker marker){
        TextView locationName;
        TextView locationPoints;
        ImageView locationType;
        Button checkinButton;

        locationDialog.setContentView(R.layout.pop_up_location);

        locationName = locationDialog.findViewById(R.id.location_name);
        locationPoints = locationDialog.findViewById(R.id.location_points);
        locationType = locationDialog.findViewById(R.id.location_type_img);
        checkinButton = locationDialog.findViewById(R.id.check_in_button);

        //sets title of location
        locationName.setText(marker.getTitle());
        locationName.setMovementMethod(new ScrollingMovementMethod());

        //sets points of location
        Spannable nrPoints = new SpannableString(String.valueOf(getPointsOfLocation(marker.getTitle())));
        nrPoints.setSpan(new ForegroundColorSpan(Color.rgb(23,104,120)), 0, nrPoints.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        nrPoints.setSpan(new RelativeSizeSpan(1.4f), 0, nrPoints.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        nrPoints.setSpan(new StyleSpan(Typeface.BOLD), 0, nrPoints.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        locationPoints.setText(nrPoints);
        locationPoints.append("\n");

        Spannable points = new SpannableString("total points");
        points.setSpan(new ForegroundColorSpan(Color.rgb(128,128,128)), 0, points.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        points.setSpan(new RelativeSizeSpan(0.5f), 0, points.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        locationPoints.append(points);

        String locationTypeEnum = getTypeOfLocation(marker.getTitle());

        String uri = "@drawable/".concat(locationTypeEnum);
        int imageResource = getResources().getIdentifier(uri, null, getPackageName());
        Drawable res = getResources().getDrawable(imageResource);
        locationType.setImageDrawable(res);

        checkinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                verifyCheckIn(marker);
            }
        });

        locationDialog.show();
    }

    private Integer getPointsOfLocation(String locationName){
        for(Location location: this.locations){
            if (location.getTitle().equals(locationName)){
                return location.getRewardPoints();
            }
        }
        return null;
    }

    private String getTypeOfLocation(String locationName){
        for(Location location: this.locations){
            if (location.getTitle().equals(locationName)){
                return location.getType().toString().toLowerCase();
            }
        }
        return null;
    }

    private void getVisitedLocationsOfUser(){
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        final String email = currentUser.getEmail();

        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("visitedLocations");

        visitedLocations = new HashSet<>();
        databaseReference.orderByChild("userEmail").equalTo(email).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot childDataSnapshot : dataSnapshot.getChildren()){
                    visitedLocations.add(childDataSnapshot.child("locationName").getValue().toString());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private boolean verifyCheckIn(Marker marker){
        CircleOptions circleOptions = new CircleOptions().center(marker.getPosition()).radius(100000.0);
        float[] distance = new float[2];

        android.location.Location.distanceBetween(currentUserLocation.getLatitude(), currentUserLocation.getLongitude(),
                circleOptions.getCenter().latitude, circleOptions.getCenter().longitude, distance);


        if (visitedLocations.contains(marker.getTitle())){
            Toast.makeText(this,"You have already checked in!", Toast.LENGTH_LONG).show();
        }
        else{
            if(distance[0] > circleOptions.getRadius()){
                Toast.makeText(this,"You cannot check in! \n You must be at the location!", Toast.LENGTH_LONG).show();
            }
            else{
                locationCheckIn(marker);
                Toast.makeText(this,"You won " + getPointsOfLocation(marker.getTitle()).toString() +
                        " points!", Toast.LENGTH_LONG).show();
            }
        }

        return false;
    }

    private void locationCheckIn(Marker marker){
        this.visitedLocations.add(marker.getTitle());
        int height = 100;
        int width = 100;
        BitmapDrawable bitmapdraw = (BitmapDrawable)getResources().getDrawable(R.drawable.applogo);
        Bitmap b=bitmapdraw.getBitmap();
        Bitmap smallMarker = Bitmap.createScaledBitmap(b, width, height, false);
        marker.setIcon(BitmapDescriptorFactory.fromBitmap(smallMarker));
        addRewardPointsToUser(getPointsOfLocation(marker.getTitle()));
        addUserLocations(marker.getTitle());
    }

    private void addRewardPointsToUser(final Integer rewardPoints) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        final String email = currentUser.getEmail();

        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        databaseReference = database.getReference("users");
        //databaseReference = FirebaseDatabase.getInstance().getReference("users");

        databaseReference.orderByChild("email").equalTo(email).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot childDataSnapshot : dataSnapshot.getChildren()){
                    String key = childDataSnapshot.getKey();
                    Integer points = Integer.valueOf(childDataSnapshot.child("points").getValue().toString());
                    points += rewardPoints;

                    databaseReference = database.getReference("users").child(key);
                    Map<String, Object> map = new HashMap<>();
                    map.put("points", points);
                    databaseReference.updateChildren(map);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void addUserLocations(String locationName){
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        final String email = currentUser.getEmail();

        //firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("visitedLocations");

        VisitedLocation visitedLocation = new VisitedLocation();
        visitedLocation.setUserEmail(email);
        visitedLocation.setLocationName(locationName);
        String id = databaseReference.push().getKey();
        visitedLocation.setId(id);
        databaseReference.child(visitedLocation.getId()).setValue(visitedLocation);
    }








    @Override
    public void onLocationChanged(android.location.Location location) {
        currentUserLocation = location;
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}
