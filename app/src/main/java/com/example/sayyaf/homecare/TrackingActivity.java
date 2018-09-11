package com.example.sayyaf.homecare;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.android.ui.IconGenerator;

import java.util.HashMap;

public class TrackingActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final String TAG = "TrackingActivity";
    private static final String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

    private GoogleMap mMap;
    private HashMap<String, Marker> mMarkers = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracking);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
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
        mMap = googleMap;
        getAllLocations();
    }

    private void getAllLocations() {
        // Get current user's list of assisted persons
        String path = "User/" + uid + "/friends";

        DatabaseReference mRef = FirebaseDatabase.getInstance().getReference(path);
        mRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Iterate through list of assisted persons and subscribe to updates
                // of their location
                for (DataSnapshot userSnapshot: dataSnapshot.getChildren()) {
                    // Gets their uid to find database path to their location
                    String assistedUid = userSnapshot.getKey();
                    String locationPath = "User/" + assistedUid + "/location";
                    subscribeToUpdates(locationPath, assistedUid);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "Failed to get friends", databaseError.toException());
            }
        });
    }

    private void subscribeToUpdates(String locationPath, String uid) {
        DatabaseReference mRef = FirebaseDatabase.getInstance().getReference(locationPath);
        mRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d(TAG, "Getting marker name: " + locationPath);
                getMarkerName(dataSnapshot, uid);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.d(TAG, "Failed to read location.", error.toException());
            }
        });
    }

    // Retrieves name to be used to identify the marker
    private void getMarkerName(DataSnapshot locationSnapshot, String uid) {
        String namePath = "User/" + uid + "/name";
        DatabaseReference mRef = FirebaseDatabase.getInstance().getReference(namePath);
        mRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String name = (String) dataSnapshot.getValue();
                Log.d(TAG, "Setting marker for: " + name);
                setMarker(locationSnapshot, uid, name);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, "Failed to read name.");
            }
        });
    }

    private void setMarker(DataSnapshot dataSnapshot, String key, String name) {
        HashMap<String, Object> value = (HashMap<String, Object>) dataSnapshot.getValue();
        double lat = Double.parseDouble(value.get("latitude").toString());
        double lng = Double.parseDouble(value.get("longitude").toString());
        LatLng location = new LatLng(lat, lng);

        // Icon factory for custom marker icons and ability to show multiple info windows
        IconGenerator iconFactory = new IconGenerator(this);

        // New marker, so place it
        if (!mMarkers.containsKey(key)) {
            Marker mMarker = mMap.addMarker(new MarkerOptions().position(location));
            mMarkers.put(key, mMarker);
            mMarker.setIcon(BitmapDescriptorFactory.fromBitmap(iconFactory.makeIcon(name)));
        }

        // Existing marker, update its location
        else {
            mMarkers.get(key).setPosition(location);
        }

        // Build boundaries so we can show all markers at once
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (Marker marker : mMarkers.values()) {
            builder.include(marker.getPosition());
        }
        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 300));
    }
}
