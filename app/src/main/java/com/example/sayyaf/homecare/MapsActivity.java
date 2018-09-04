package com.example.sayyaf.homecare;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMyLocationButtonClickListener;
import com.google.android.gms.maps.GoogleMap.OnMyLocationClickListener;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMyLocationButtonClickListener,
        OnMyLocationClickListener, OnMapReadyCallback {

    private static final String TAG = "MapsActivity";
    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_REQUEST_CODE = 1000;
    private static final float STREET_ZOOM = 15;

    private GoogleMap mMap;
    private Boolean mLocationPermissionsGranted = false;
    private FusedLocationProviderClient mFusedLocationClient;
    private Location currentLocation;
    private LatLng currentLatLng;

    private EditText mInputSearchEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        mInputSearchEditText = (EditText) findViewById(R.id.inputSearch);

        // Get location permissions then initialise the map
        getLocationPermission();

    }

    /**
     * Initialises the map fragment
     */
    private void initMap() {
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    /**
     * Attempts to get location permission of the device then initialise the map, if not a
     * permission request is sent and appears on the user's screen
     */
    private void getLocationPermission() {
        String[] permissions = {FINE_LOCATION, COARSE_LOCATION};

        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(this.getApplicationContext(),COARSE_LOCATION) ==
                    PackageManager.PERMISSION_GRANTED) {
                mLocationPermissionsGranted = true;

                // All permissions granted so initialise the map
                initMap();
            }
            else {
                ActivityCompat.requestPermissions(this, permissions, LOCATION_REQUEST_CODE);
            }
        }
        else {
            ActivityCompat.requestPermissions(this, permissions, LOCATION_REQUEST_CODE);
        }
    }

    /**
     * Handles the result of the permission request, initialising the map if the user allows the
     * request
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        mLocationPermissionsGranted = false;

        if (requestCode == LOCATION_REQUEST_CODE) {
            if (permissions.length == 1 && permissions[0] == FINE_LOCATION &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                mLocationPermissionsGranted = true;

                // So now we can initialise the map
                initMap();
            }
        }
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

        getDeviceLocation();
        mMap.getUiSettings().setZoomControlsEnabled(true);

        initialiseSearch();
    }

    /**
     * Gets the current location of the device and represents it as a blue circle on the map.
     * Also enables a "My Location" button which recentres the camera on the current location.
     */
    private void getDeviceLocation() {

        // Ensure permissions have been granted before enable location data
        try {
            if(mLocationPermissionsGranted) {

                // Move and zoom camera to current location on startup
                mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
                final Task location = mFusedLocationClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        // Successfully found device location
                        if (task.isSuccessful()) {
                            currentLocation = (Location) location.getResult();
                            currentLatLng = new LatLng(currentLocation.getLatitude(),
                                    currentLocation.getLongitude());
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng,
                                    STREET_ZOOM));
                        }
                        // Unable to get device's location
                        else {
                            Toast.makeText(MapsActivity.this, "Unable to get current location",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                mMap.setMyLocationEnabled(true);

                // Enable "My Location" button
                mMap.setOnMyLocationButtonClickListener(this);
                mMap.setOnMyLocationClickListener(this);
            }
        }
        catch (SecurityException e){
            Log.e(TAG, "getDeviceLocation: SecurityException: " + e.getMessage());
        }
    }

    @Override
    public void onMyLocationClick(@NonNull Location location) {
        Toast.makeText(this, "Current location:\n" +
                location, Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onMyLocationButtonClick() {
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false;
    }

    private void initialiseSearch() {

        mInputSearchEditText.setImeActionLabel("Search", EditorInfo.IME_ACTION_SEARCH);
        mInputSearchEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    geoLocate();
                }
                return false;
            }
        });
    }

    private void geoLocate() {

        String searchString = mInputSearchEditText.getText().toString();

        Geocoder geocoder = new Geocoder(MapsActivity.this);
        List<Address> list = new ArrayList<>();
        try {
            list = geocoder.getFromLocationName(searchString, 1);
        } catch (IOException e) {
            // e.printStackTrace();
            Log.e(TAG, "geoLocation: IOException" + e.getMessage());
        }

        if (list.size() > 0) {
            Address address = list.get(0);

            Log.d(TAG, "geoLocate: found a location: " + address.toString());

            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(address.getLatitude(), address.getLongitude()), STREET_ZOOM));
            MarkerOptions markerOptions = new MarkerOptions().position(new LatLng(address.getLatitude(), address.getLongitude())).title(address.getAddressLine(0));
            mMap.addMarker(markerOptions);
        }
    }
}
