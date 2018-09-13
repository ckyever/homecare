package com.example.sayyaf.homecare.mapping;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.example.sayyaf.homecare.R;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.android.ui.IconGenerator;

import java.util.HashMap;

public class TrackingActivity extends AppCompatActivity implements OnMapReadyCallback {

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

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        getAllLocations();
    }

    /**
     * Iterate through all friends of the current user and calls subscribeToLocation on each one.
     */
    private void getAllLocations() {
        // Get current user's list of assisted persons
        String path = "User/" + uid + "/friends";

        // Gets reference to list of friends for current user
        DatabaseReference mRef = FirebaseDatabase.getInstance().getReference(path);
        mRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Check current user has friends first
                if (dataSnapshot.exists()) {
                    for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                        // Gets their uid to find database path to their location
                        String assistedUid = userSnapshot.getKey();
                        String locationPath = "User/" + assistedUid + "/location";
                        subscribeToLocation(locationPath, assistedUid);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "Failed to get friends", databaseError.toException());
            }
        });
    }

    /**
     * Given a location path and a Uid, gets a snapshot of the location data for corresponding user
     * and then passes on that information to the method getMarkerName.
     */
    private void subscribeToLocation(String locationPath, String uid) {
        DatabaseReference mRef = FirebaseDatabase.getInstance().getReference(locationPath);
        mRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d(TAG, "Getting marker name: " + locationPath);
                if (dataSnapshot.exists())
                    getMarkerName(dataSnapshot, uid);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.d(TAG, "Failed to read location.", error.toException());
            }
        });
    }

    /**
     * Takes a snapshot of some user's location data and their Uid, and gets the name of the
     * corresponding user and calls setMarker.
     */
    // Retrieves name to be used to identify the marker
    private void getMarkerName(DataSnapshot locationSnapshot, String uid) {
        String namePath = "User/" + uid + "/name";
        DatabaseReference mRef = FirebaseDatabase.getInstance().getReference(namePath);
        mRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    String name = (String) dataSnapshot.getValue();
                    Log.d(TAG, "Setting marker for: " + name);
                    setMarker(locationSnapshot, uid, name);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, "Failed to read name.");
            }
        });
    }

    /**
     * Adds a new marker to the map for the given assisted person, as identified by their Uid (key).
     * If a marker already exists then update the position.
     */
    private void setMarker(DataSnapshot dataSnapshot, String key, String name) {
        // Use a HashMap to hold a LatLng object for each assisted person. The key is their Uid and
        // value is a LatLng object representing their location
        HashMap<String, Object> value = (HashMap<String, Object>) dataSnapshot.getValue();
        double lat = Double.parseDouble(value.get("latitude").toString());
        double lng = Double.parseDouble(value.get("longitude").toString());
        LatLng location = new LatLng(lat, lng);

        // Icon factory for custom marker icons and ability to show multiple info windows at once
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

        // Build boundaries so we can show all markers at once on upon starting the activity
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (Marker marker : mMarkers.values()) {
            builder.include(marker.getPosition());
        }
        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 300));
    }
}
