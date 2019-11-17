package com.example.insight.activity.mapActivity;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.example.insight.R;
import com.example.insight.entity.Location;
import com.example.insight.entity.enums.LocationType;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
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

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private List<Location> locations;
    private List<Marker> markers;
    private DatabaseReference database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.locations = new ArrayList<>();
        this.markers = new ArrayList<>();
        this.database = FirebaseDatabase.getInstance().getReference("locations");
        setContentView(R.layout.activity_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.mMap = googleMap;
        Log.d("db", "reading");
        database.orderByChild("id").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                locations = new ArrayList<>();
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

                    locations.add(toBuild);
                }

                mMap.clear();

                for (Location location : locations) {
                    System.out.println(location.toString());
                    LatLng coordinates = location.getCoordinates();

                    Marker newMarker = mMap.addMarker(new MarkerOptions()
                            .position(coordinates)
                            .title(location.getTitle()));
                    newMarker.setTag(0);
                    markers.add(newMarker);
                }
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
}
