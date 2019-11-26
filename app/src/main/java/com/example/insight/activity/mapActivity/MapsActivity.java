package com.example.insight.activity.mapActivity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.method.ScrollingMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.insight.R;
import com.example.insight.activity.UserProfile;
import com.example.insight.entity.CommentForLocation;
import com.example.insight.entity.Location;
import com.example.insight.entity.VisitedLocation;
import com.example.insight.entity.enums.LocationType;
import com.example.insight.entity.UserPictureForLocation;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
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

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.CAMERA;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, LocationListener {

    private GoogleMap mMap;
    private List<Location> locations;
    private List<Marker> markers;
    private DatabaseReference database;
    private LocationManager locationManager;
    private Set<String> visitedLocations;
    private final Integer REWARD_POINTS_FOR_COMMENTS = 15;

    private Button userProfileButton;
    private Button mapViewButton;

    private android.location.Location currentUserLocation;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;

    private Dialog locationDialog;
    private Dialog leaveCommentDialog;

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private String currentMarkerName;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        this.locations = new ArrayList<>();
        this.markers = new ArrayList<>();
        this.visitedLocations = new HashSet<>();
        this.database = FirebaseDatabase.getInstance().getReference("locations");
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        getVisitedLocationsOfUser();
        locationDialog = new Dialog(this);
        leaveCommentDialog = new Dialog(this);
        userProfileButton = findViewById(R.id.userProfileButton);
        mapViewButton = findViewById(R.id.mapViewButton);

        mapViewButton.setVisibility(View.INVISIBLE);

        userProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MapsActivity.this, UserProfile.class));
            }
        });

    }

    /**
     * method makes application wait for location permission to be granted;
     * if the user doesn't allow those permission, he's brought back to the login
     * screen
     * once he allows them, the application continues with the initialization of the map
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {

        int index = 0;
        Map<String, Integer> PermissionsMap = new HashMap<>();
        for (String permission : permissions){
            PermissionsMap.put(permission, grantResults[index]);
            index++;
        }

        if(PermissionsMap.get(ACCESS_FINE_LOCATION) != 0 || PermissionsMap.get(CAMERA) != 0){
            Toast.makeText(this, "Location and camera permissions are a must", Toast.LENGTH_SHORT).show();
            finish();
        }
        else
        {
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.mMap = googleMap;
        mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style));

        getVisitedLocationsOfUser();


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


                //check if necessary permissions have been granted
                //if not, return from the method and stop the initialization
                //of the map until permissions have been granted
                boolean canContinue = requestUserForPermissionsIfNeeded();
                if (!canContinue){
                    return;
                }


                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);

                //TODO start
                //android.location.Location currentLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                //currentUserLocation = currentLocation;
                //mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude())));

                android.location.Location currentLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                currentLocation.setLatitude(46.7695133);
                currentLocation.setLongitude(23.5898073);
                //TODO end -> remove mockup current location and replace with the real one up above

                currentUserLocation = currentLocation;
                mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude())));
                mMap.animateCamera(CameraUpdateFactory.zoomTo(15));

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

        if (ActivityCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
    }

    /**
     * method displays pop-up dialog of location when marker is clicked
     * @param marker
     */
    public void ShowPopUpLocation(final Marker marker){
        TextView locationName;
        TextView locationPoints;
        ImageView locationType;
        Button checkinButton;
        Button leaveCommentButton;
        Button takePictureButton;

        locationDialog.setContentView(R.layout.pop_up_location);

        locationName = locationDialog.findViewById(R.id.location_name);
        locationPoints = locationDialog.findViewById(R.id.location_points);
        locationType = locationDialog.findViewById(R.id.location_type_img);
        checkinButton = locationDialog.findViewById(R.id.check_in_button);
        leaveCommentButton = locationDialog.findViewById(R.id.comment_option_button);
        takePictureButton = locationDialog.findViewById(R.id.camera_option_button);


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


        //checks-in user to the location if he hasn't visited already
        //and if he is in close proximity to the location
        //(stores check-in in db)
        checkinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean canCheckIn = isUserAtLocation(marker, true);
                if (canCheckIn) {
                    locationCheckIn(marker);
                }
            }
        });

        //opens leaveComment dialog only if the user has
        //previously checked in to the location
        //(stores comment of location in db)
        leaveCommentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(visitedLocations.contains(marker.getTitle())){
                    leaveCommentPopUp(marker);
                }
                else{
                    Toast.makeText(MapsActivity.this, "You must check-in before \n leaving a comment!", Toast.LENGTH_LONG).show();
                }
            }
        });


        //we create a new intent to open the camera
        //the taken picture will be stored automatically in the database
        takePictureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(visitedLocations.contains(marker.getTitle()) && isUserAtLocation(marker, false)){
                    currentMarkerName = marker.getTitle();
                    Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                }
                else{
                    Toast.makeText(MapsActivity.this, "You must check-in and be at the location \n to post a picture!", Toast.LENGTH_LONG).show();
                }
            }
        });


        locationDialog.show();
    }

    private boolean requestUserForPermissionsIfNeeded(){
        //check if necessary permission have been granted, if not request them from the user
        //verify if location permission is granted
        if (ContextCompat.checkSelfPermission(MapsActivity.this, ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_DENIED ||
                ContextCompat.checkSelfPermission(MapsActivity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) ==
                        PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(MapsActivity.this, new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.CAMERA},
                    12);
            return false;
        }
        //verify if camera permission is granted
        else if(ContextCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_DENIED){
            ActivityCompat.requestPermissions(MapsActivity.this, new String[]{
                            Manifest.permission.CAMERA},
                    12);
            return false;
        }
        return true;
    }

    /**
     * overridden method catches the result of the camera intent
     * extracts a bitmap out of the returned data (the captured image)
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            encodeBitmapAndSaveToFirebase(imageBitmap);
        }
    }

    /**
     * method encodes a bitmap and creates a new entity of UserPictureForLocation
     * and persists this into the database
     * @param bitmap
     */
    public void encodeBitmapAndSaveToFirebase(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        String imageEncoded = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);

        databaseReference = FirebaseDatabase.getInstance().getReference("locationPictures");
        UserPictureForLocation userPictureForLocation = new UserPictureForLocation();
        userPictureForLocation.setLocationName(this.currentMarkerName);

        userPictureForLocation.setEncodedPicture(imageEncoded);

        String id = databaseReference.push().getKey();
        userPictureForLocation.setId(id);

        databaseReference.child(userPictureForLocation.getId()).setValue(userPictureForLocation);
    }

    /**
     * popUp dialog for user to leave comment on location
     * @param marker
     */
    private void leaveCommentPopUp(final Marker marker){
        Button postCommentButton;
        final EditText commentField;

        leaveCommentDialog.setContentView(R.layout.pop_up_location_comment);
        commentField = leaveCommentDialog.findViewById(R.id.commentField);
        postCommentButton = leaveCommentDialog.findViewById(R.id.postCommentButton);

        postCommentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userComment = commentField.getText().toString();
                if(userComment == null || userComment.equals("")){
                    Toast.makeText(MapsActivity.this, "Comment can't be empty!", Toast.LENGTH_LONG).show();
                }
                else{
                    storeLocationComment(userComment, marker.getTitle());
                    updateRewardPointsOfUser(REWARD_POINTS_FOR_COMMENTS);
                    Toast.makeText(MapsActivity.this,"You won " + REWARD_POINTS_FOR_COMMENTS +
                            " points!", Toast.LENGTH_LONG).show();
                    leaveCommentDialog.dismiss();
                }
            }
        });

        leaveCommentDialog.show();


    }

    /**
     * method creates an instance of CommentForLocation entity
     * and stores it in the database
     * @param userComment the comment typed in the popUp dialog
     * @param locationName the name of the location about which the user
     *                     leaves a comment
     */
    private void storeLocationComment(String userComment, String locationName){
        databaseReference = FirebaseDatabase.getInstance().getReference("locationComments");

        CommentForLocation userCommentLocation = new CommentForLocation();
        userCommentLocation.setUserComment(userComment);
        userCommentLocation.setLocationName(locationName);

        String id = databaseReference.push().getKey();
        userCommentLocation.setId(id);
        databaseReference.child(userCommentLocation.getId()).setValue(userCommentLocation);

    }

    /**
     * method retrieves user points of given location
     * @param locationName
     * @return
     */
    private Integer getPointsOfLocation(String locationName){
        for(Location location: this.locations){
            if (location.getTitle().equals(locationName)){
                return location.getRewardPoints();
            }
        }
        return null;
    }

    /**
     * method retrieves the type of the location
     * @param locationName
     * @return
     */
    private String getTypeOfLocation(String locationName){
        for(Location location: this.locations){
            if (location.getTitle().equals(locationName)){
                return location.getType().toString().toLowerCase();
            }
        }
        return null;
    }

    public AtomicBoolean getAllLocations(final AtomicBoolean done){
        database = FirebaseDatabase.getInstance().getReference("locations");

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
                done.set(true);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("db error", "cancelled", databaseError.toException().getCause());
            }
        });

        return done;
    }


    /**
     * method retrieves all visited locations of current
     * logged in user and stores them in a set
     */
    private void getVisitedLocationsOfUser(){
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        final String email = currentUser.getEmail();
        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("visitedLocations");

        databaseReference.orderByChild("userEmail").equalTo(email).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                visitedLocations = new HashSet<>();
                for (DataSnapshot childDataSnapshot : dataSnapshot.getChildren()){
                    visitedLocations.add(childDataSnapshot.child("locationName").getValue().toString());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    /**
     * method checks if the logged in user is in proximity to the location
     * he desires to check in
     * @param marker location in which the user wants to check-in
     * @return
     */
    private boolean isUserAtLocation(Marker marker, boolean showToasters){
        boolean userIsAtLocation;
        CircleOptions circleOptions = new CircleOptions().center(marker.getPosition()).radius(100000.0);
        float[] distance = new float[2];

        android.location.Location.distanceBetween(currentUserLocation.getLatitude(), currentUserLocation.getLongitude(),
                circleOptions.getCenter().latitude, circleOptions.getCenter().longitude, distance);

        if (visitedLocations.contains(marker.getTitle())){
            if(showToasters){
                Toast.makeText(this,"You have already checked in!", Toast.LENGTH_LONG).show();
            }
        }
        //else{
            if(distance[0] > circleOptions.getRadius()){
                if(showToasters){
                    Toast.makeText(this,"You cannot check in! \n You must be at the location!", Toast.LENGTH_LONG).show();
                }
                userIsAtLocation = false;
            }
            else{
                if(showToasters){
                    Toast.makeText(this,"You won " + getPointsOfLocation(marker.getTitle()).toString() +
                            " points!", Toast.LENGTH_LONG).show();
                }
                userIsAtLocation = true;
        //    }
        }

        return userIsAtLocation;
    }

    /**
     * method checks in user to the location;
     * it updates the marker on the map and calls methods
     * to store the new visited location and update the
     * reward points of the user
     * @param marker
     */
    private void locationCheckIn(Marker marker){
        this.visitedLocations.add(marker.getTitle());
        int height = 100;
        int width = 100;
        BitmapDrawable bitmapdraw = (BitmapDrawable)getResources().getDrawable(R.drawable.applogo);
        Bitmap b=bitmapdraw.getBitmap();
        Bitmap smallMarker = Bitmap.createScaledBitmap(b, width, height, false);
        marker.setIcon(BitmapDescriptorFactory.fromBitmap(smallMarker));

        updateRewardPointsOfUser(getPointsOfLocation(marker.getTitle()));
        storeNewVisitedLocationOfUser(marker.getTitle());
    }

    /**
     * method updates the logged in users' reward points in the db
     * after he has checked in to a location
     * @param rewardPoints
     */
    private void updateRewardPointsOfUser(final Integer rewardPoints) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        final String email = currentUser.getEmail();

        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        databaseReference = database.getReference("users");
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

    /**
     * method creates an instance of class VisitedLocation
     * sets its fields to the current logged in Users information
     * and the location he wants to check in
     * and stores in to the database
     * @param locationName
     */
    private void storeNewVisitedLocationOfUser(String locationName){
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        final String email = currentUser.getEmail();

        databaseReference = FirebaseDatabase.getInstance().getReference("visitedLocations");

        VisitedLocation visitedLocation = new VisitedLocation();
        visitedLocation.setUserEmail(email);
        visitedLocation.setLocationName(locationName);
        visitedLocation.setPoints(getPointsOfLocation(locationName));
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