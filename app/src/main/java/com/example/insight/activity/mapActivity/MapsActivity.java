package com.example.insight.activity.mapActivity;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.content.pm.PackageManager;
import android.graphics.Camera;
import android.location.Location;
import android.os.Bundle;
import android.widget.Toast;

import com.example.insight.R;
import com.example.insight.activity.exception.BusinessException;
import com.example.insight.service.MapService;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener {

    private GoogleMap mMap;
    private MapService mapService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
//        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
//        mapFragment.getMapAsync(this);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.mMap = googleMap;
        this.mapService = new MapService(mMap);
        if (mMap == null){
            Toast.makeText(this,"Error loading map!", Toast.LENGTH_LONG).show();
        }

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        mMap.clear();
        mMap = mapService.getMap();

        //mMap = mapService.setMap(mMap);

        CameraUpdate center = CameraUpdateFactory.newLatLng(mapService.getMarkers().get(0).getPosition());
        CameraUpdate zoom = CameraUpdateFactory.newLatLngZoom(mapService.getMarkers().get(0).getPosition(), 9);
        mMap.moveCamera(center);
        mMap.animateCamera(zoom);

//        LatLng clujNapoca = new LatLng(46.770439,23.591423);
//
//        CameraUpdate center = CameraUpdateFactory.newLatLng(clujNapoca);
//        CameraUpdate zoom = CameraUpdateFactory.newLatLngZoom(clujNapoca, 11);
//
//        mMap.clear();
//        mMap = mapService.setMap(mMap);
//        mMap.moveCamera(center);
//        mMap.animateCamera(zoom);

//
//        MarkerOptions mp = new MarkerOptions();
//        mp.position(clujNapoca);
//        mp.title("Center");
//        mMap.addMarker(mp);
//        mMap.moveCamera(center);
//        mMap.animateCamera(zoom);

//        mMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener(){
//            @Override
//            public void onMyLocationChange(Location location){
//                CameraUpdate center = CameraUpdateFactory.newLatLng(new LatLng(location.getLatitude(), location.getLongitude()));
//                CameraUpdate zoom = CameraUpdateFactory.zoomTo(11);
//                mMap.clear();
//
//                MarkerOptions mp = new MarkerOptions();
//
//                mp.position(new LatLng(location.getLatitude(), location.getLongitude()));
//
//                mp.title("my position");
//
//                mMap.addMarker(mp);
//                mMap.moveCamera(center);
//                mMap.animateCamera(zoom);
//            }
//        });
//
//        // Add a marker in Sydney and move the camera
//        LatLng sydney = new LatLng(-34, 151);
//        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

//        if (googleMap == null){
//            Toast.makeText(getApplicationContext(),"Sorry! Unable to load map!", Toast.LENGTH_LONG).show();
//        }
//
//        mMap = googleMap;
//        LatLng clujNapoca = new LatLng(46.770439,23.591423);
//        mMap.addMarker(new MarkerOptions().position(clujNapoca).title("Suck my Dick"));
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(clujNapoca));
    }

    @Override
    public void onLocationChanged(Location location) {

    }
}
