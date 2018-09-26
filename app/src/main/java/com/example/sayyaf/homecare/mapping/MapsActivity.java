package com.example.sayyaf.homecare.mapping;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sayyaf.homecare.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBufferResponse;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "MapsActivity";
    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_REQUEST_CODE = 1;
    private static final float STREET_ZOOM = 17;
    private static final LatLngBounds LAT_LNG_BOUNDS =
            new LatLngBounds(new LatLng(-90, -180), new LatLng(90, 180));

    private GoogleMap mMap;
    private Boolean mLocationPermissionsGranted = false;
    private FusedLocationProviderClient mFusedLocationClient;
    private Location initialLocation;
    private LatLng initialLatLng;
    private PlaceAutocompleteAdapter mPlaceAutocompleteAdapter;
    private GeoDataClient mGeoDataClient;
    private PlaceInfo mPlace;
    private LatLng mLatLng;

    private ImageView mLocationButton;
    private AutoCompleteTextView mInputSearchTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        mLocationButton = (ImageView) findViewById(R.id.ic_mylocation);
        mInputSearchTextView = (AutoCompleteTextView) findViewById(R.id.inputSearch);

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
     * Starts the tracking service
     */
    private void startTrackingService() {
        startService(new Intent(this,
                com.example.sayyaf.homecare.mapping.TrackingService.class));
    }

    // Receives latitude and longitude from TrackingService and stores it as mLatLng
    BroadcastReceiver bReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "Getting intent extras");
            Bundle intentExtras = intent.getExtras();

            if (intentExtras != null) {
                double latitude = intentExtras.getDouble("Latitude");
                double longitude = intentExtras.getDouble("Longitude");
                mLatLng = new LatLng(latitude, longitude);
            }
        }
    };

    // Dynamically adds listener to the tracking service
    protected void onResume(){
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(bReceiver,
                new IntentFilter("TrackingService"));
    }

    // Dynamically removes listener from the tracking service
    protected void onPause (){
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(bReceiver);
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

                // All permissions granted so initialise the map and start tracking service
                initMap();
                startTrackingService();
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

                // So now we can initialise the map and initialise tracking service
                initMap();
                startTrackingService();
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
        enableMyLocationButton();

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
                        if (task.isSuccessful() && task.getResult() != null) {
                            initialLocation = (Location) location.getResult();
                            initialLatLng = new LatLng(initialLocation.getLatitude(),
                                    initialLocation.getLongitude());
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(initialLatLng,
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
                // Hide the default my location button
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
            }
        }
        catch (SecurityException e){
            Log.e(TAG, "getDeviceLocation: SecurityException: " + e.getMessage());
        }
    }

    /**
     * When the "My Location" button is toggled center on the device's current location and
     * keep the camera following the location while toggled. When turned off the user can freely
     * move their map camera view.
     */
    private void enableMyLocationButton() {
        mLocationButton.setOnClickListener(new View.OnClickListener() {
            boolean buttonOn = false;
            @Override
            public void onClick(View view) {
                // Change image view to indicate button is toggled on
                if (!buttonOn) {
                    mLocationButton.setImageResource(R.drawable.ic_mylocationon);
                    buttonOn = true;
                }
                // Change image view to indicate button is toggled off
                else if (buttonOn) {
                    mLocationButton.setImageResource(R.drawable.ic_mylocationoff);
                    buttonOn = false;
                }
            }
        });
    }

    // Place marker and move camera to location
    private void placeMarker(LatLng latlng, String title) {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng, STREET_ZOOM));
        MarkerOptions markerOptions = new MarkerOptions().position(latlng).title(title);
        mMap.addMarker(markerOptions);
    }

    // Hide keyboard
    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void initialiseSearch() {
        // Search the location that has been input in the search bar
        mGeoDataClient = Places.getGeoDataClient(this);
        mPlaceAutocompleteAdapter = new PlaceAutocompleteAdapter
                (this, mGeoDataClient, LAT_LNG_BOUNDS, null);
        mInputSearchTextView.setOnItemClickListener(mAutocompleteClickListener);
        mInputSearchTextView.setAdapter(mPlaceAutocompleteAdapter);
        mInputSearchTextView.setImeActionLabel("Search", EditorInfo.IME_ACTION_SEARCH);
        mInputSearchTextView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    hideKeyboard();
                    geoLocate();
                }
                return false;
            }
        });
    }

    private void geoLocate() {
        // Place a marker and move the camera to the location that has been searched

        String searchString = mInputSearchTextView.getText().toString();

        Geocoder geocoder = new Geocoder(MapsActivity.this);
        List<Address> list = new ArrayList<>();
        try {
            list = geocoder.getFromLocationName(searchString, 1);
        } catch (IOException e) {
            // e.printStackTrace();
            Log.e(TAG, "geoLocation: IOException" + e.getMessage());
        }
        // If the location searched returns at least one result
        if (list.size() > 0) {
            Address address = list.get(0);

            Log.d(TAG, "geoLocate: found a location: " + address.toString());

            // Place marker and move camera
            placeMarker(new LatLng(address.getLatitude(), address.getLongitude()),
                    address.getAddressLine(0));
        }
    }

    private AdapterView.OnItemClickListener mAutocompleteClickListener =
            new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    final AutocompletePrediction item = mPlaceAutocompleteAdapter.getItem(position);
                    final String placeId = item.getPlaceId();

                    // Get selected location from autocomplete
                    Task<PlaceBufferResponse> placeResult = mGeoDataClient.getPlaceById(placeId);
                    placeResult.addOnCompleteListener(mUpdatePlaceDetailsCallback);
                    hideKeyboard();

                }
            };

    private OnCompleteListener<PlaceBufferResponse> mUpdatePlaceDetailsCallback = new OnCompleteListener<PlaceBufferResponse>() {
        @Override
        public void onComplete(@NonNull Task<PlaceBufferResponse> task) {
            if (task.isSuccessful()) {
                PlaceBufferResponse places = task.getResult();
                final Place place = places.get(0);

                // Store information about location
                try {
                    mPlace = new PlaceInfo();
                    mPlace.setAddress(place.getAddress().toString());
                    mPlace.setLatlng(place.getLatLng());
                } catch (NullPointerException e){
                    Log.e(TAG, "NullPointerException: " + e.getMessage());
                }

                // Place marker and move camera
                placeMarker(mPlace.getLatlng(), mPlace.getAddress());

                places.release();
            } else {
                Log.e(TAG, "Place not found");
            }
        }
    };

}
