package com.example.sayyaf.homecare.mapping;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sayyaf.homecare.MainActivity;
import com.example.sayyaf.homecare.R;
import com.example.sayyaf.homecare.accounts.AccountRegisterActivity;
import com.example.sayyaf.homecare.accounts.LoginActivity;
import com.example.sayyaf.homecare.accounts.UserAppVersionController;
import com.example.sayyaf.homecare.notifications.EmergencyCallActivity;
import com.example.sayyaf.homecare.notifications.NetworkConnection;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.AutocompleteFilter;
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
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.PendingResult;
import com.google.maps.internal.PolylineEncoding;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;
import com.google.maps.model.TravelMode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback,
        View.OnClickListener, GoogleMap.OnCameraMoveStartedListener {

    // Constants
    private static final String TAG = "MapsActivity";
    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_REQUEST_CODE = 1;
    private static final int STREET_ZOOM = 17;
    private static final LatLngBounds LAT_LNG_BOUNDS =
            new LatLngBounds(new LatLng(-90, -180), new LatLng(90, 180));

    // Map variables
    private GoogleMap mMap;
    private Boolean mLocationPermissionsGranted = false;

    // Location variables
    private FusedLocationProviderClient mFusedLocationClient;
    private Location initialLocation;
    private LatLng initialLatLng;
    private LatLng mLatLng;

    // Autocomplete variables
    private PlaceAutocompleteAdapter mPlaceAutocompleteAdapter;
    private GeoDataClient mGeoDataClient;
    private PlaceInfo mPlace;
    private Marker locationSearched;

    // Directions variables
    private GeoApiContext mGeoApiContext;
    private Polyline polyline;
    private TravelMode travelMode;
    private AutocompleteFilter filter;

    // Buttons
    private ImageView mLocationButton;
    private Boolean isLocationButtonOn = false;
    private AutoCompleteTextView mInputSearchTextView;
    private Button mDirectionsButton;
    private Button helpButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Setup buttons
        mLocationButton = findViewById(R.id.ic_mylocation);
        mInputSearchTextView = findViewById(R.id.inputSearch);
        mDirectionsButton = findViewById(R.id.directions);
        mDirectionsButton.setOnClickListener(this);
        helpButton = findViewById(R.id.optionHelp);

        // Makes help button visible on assisted person's screen
        UserAppVersionController.getUserAppVersionController().resetButton(helpButton);

        // Get location permissions then initialise the map
        getLocationPermission();

        locationSearched = null;
        polyline = null;

        if (mGeoApiContext == null) {
            mGeoApiContext = new GeoApiContext.Builder()
                    .apiKey(getString(R.string.google_maps_key))
                    .build();
        }

        filter = new AutocompleteFilter.Builder().setCountry("AU").build();
    }



    @Override
    public void onClick(View view) {
        if(!NetworkConnection.getConnection()){
            NetworkConnection.requestNetworkConnection(MapsActivity.this);
            return;
        }

        if(view == helpButton){
            EmergencyCallActivity.setBackToActivity(MapsActivity.class);
            Intent intent = new Intent(MapsActivity.this,
                    EmergencyCallActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }

        if (view == mDirectionsButton) {
            if (locationSearched != null) {
                // pop up window to select mode of transport
                transportModePopUp();
            } else {
                Toast.makeText(MapsActivity.this, R.string.directions_error,
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Initialises the map fragment.
     */
    private void initMap() {
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        mapFragment.setRetainInstance(true);

    }

    /**
     * Starts the tracking service.
     */
    private void startTrackingService() {
        startService(new Intent(this,
                com.example.sayyaf.homecare.mapping.TrackingService.class));
    }

    // Broadcast receiver that listens to latlng updates from TrackingService and moves the camera
    // to the new LatLng if applicable.
    protected BroadcastReceiver bReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "Getting intent extras");
            Bundle intentExtras = intent.getExtras();

            // LatLng is stored in intent extras by TrackingService
            if (intentExtras != null) {
                double latitude = intentExtras.getDouble("Latitude");
                double longitude = intentExtras.getDouble("Longitude");
                mLatLng = new LatLng(latitude, longitude);
                // Move camera to new position if applicable
                cameraFollow();
            }
        }
    };

    /**
     * Dynamically adds broadcast receiver to TrackingService.
     */
    protected void onResume(){
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(bReceiver,
                new IntentFilter("TrackingService"));
    }

    /**
     * Dynamically removes broadcast receiver from TrackingService.
     */
    protected void onPause (){
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(bReceiver);
    }

    /**
     * Attempts to get location permission of the device then initialise the map, if not a
     * permission request is sent and appears on the user's screen.
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
     * request.
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
     * Here the behaviour is to enable the zoom assist buttons, add the current users location on
     * the map, enable the "My Location" button which can center and follow the users real time
     * location as they move, initialise the location search bar, and create listener for camera
     * movements initiated by user.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setMapToolbarEnabled(false);

        getDeviceLocation();
        enableMyLocationButton();
        initialiseSearch();
        mMap.setOnCameraMoveStartedListener(this);
    }

    /**
     * Gets the current location of the device after checking for permissions
     * and represents it as a blue circle on the map. We hide the default "My Location" button
     * to implement a custom one with the ability to stay following the users location.
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
                            Toast.makeText(MapsActivity.this,
                                    R.string.get_device_location_error, Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                mMap.setMyLocationEnabled(true);
                // Hide the default "My Location" button
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
            }
        }
        catch (SecurityException e){
            Log.e(TAG, "getDeviceLocation: SecurityException: " + e.getMessage());
        }
    }

    /**
     * Listens to clicks on the "My Location" image view and imitates a toggleable button, changing
     * the boolean isLocationButtonOn, which dictates whether the camera should be following the
     * users current location via the method cameraFollow().
     */
    private void enableMyLocationButton() {
        mLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Change image view to indicate button is toggled on
                if (!isLocationButtonOn && mLatLng != null) {
                    mLocationButton.setImageResource(R.drawable.ic_mylocationon);
                    isLocationButtonOn = true;
                    // Centre and zoom camera on location after pressing
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mLatLng, STREET_ZOOM));
                }
                // Change image view to indicate button is toggled off
                else if (isLocationButtonOn) {
                    mLocationButton.setImageResource(R.drawable.ic_mylocationoff);
                    isLocationButtonOn = false;
                }
            }
        });
    }

    /**
     * If the user performs a gesture that changes the map camera and the "my location" button is
     * toggled on, turn it off to allow the user to move freely again
     */
    @Override
    public void onCameraMoveStarted(int reason) {
        if (reason == GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE &&
                isLocationButtonOn == true) {
            mLocationButton.performClick();
        }
    }

    /**
     * When called checks the "My Location" button is toggled on and moves the camera to the current
     * location.
     */
    private void cameraFollow() {
        if (isLocationButtonOn) {
            mMap.animateCamera(CameraUpdateFactory.newLatLng(mLatLng));
        }
        else {
            // Do nothing
        }
    }

    // Place marker and move camera to location
    private void placeMarker(LatLng latlng, String title) {
        // remove old route
        removePolyline();

        // remove old marker
        if (locationSearched != null) {
            locationSearched.remove();
            locationSearched = null;
        }

        // place new marker
        if (locationSearched == null) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng, STREET_ZOOM));
            MarkerOptions markerOptions = new MarkerOptions().position(latlng).title(title);
            locationSearched = mMap.addMarker(markerOptions);
        }
    }

    // Hide keyboard
    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)
                    getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    // Search the location that has been input in the search bar
    private void initialiseSearch() {
        mGeoDataClient = Places.getGeoDataClient(this);
        mPlaceAutocompleteAdapter = new PlaceAutocompleteAdapter
                (this, mGeoDataClient, LAT_LNG_BOUNDS, filter);
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

    // Place a marker and move the camera to the location that has been searched
    private void geoLocate() {

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

    private OnCompleteListener<PlaceBufferResponse> mUpdatePlaceDetailsCallback =
            new OnCompleteListener<PlaceBufferResponse>() {
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

    // Calculate directions from current location to marker
    private void calculateDirections(Marker marker, TravelMode travelMode){
        // remove old route
        removePolyline();
        Log.d(TAG, "calculateDirections: calculating directions.");

        com.google.maps.model.LatLng destination = new com.google.maps.model.LatLng(
                marker.getPosition().latitude,
                marker.getPosition().longitude
        );
        DirectionsApiRequest directions = new DirectionsApiRequest(mGeoApiContext);

        // directions.alternatives(true);
        directions.mode(travelMode);
        directions.origin(
                new com.google.maps.model.LatLng(
                        mLatLng.latitude,
                        mLatLng.longitude
                )
        );
        Log.d(TAG, "calculateDirections: destination: " + destination.toString());
        directions.destination(destination).setCallback(new PendingResult.Callback<DirectionsResult>() {
            @Override
            public void onResult(DirectionsResult result) {
                Log.d(TAG, "calculateDirections: routes: " + result.routes[0].toString());
                Log.d(TAG, "calculateDirections: duration: " +
                        result.routes[0].legs[0].duration);
                Log.d(TAG, "calculateDirections: distance: " +
                        result.routes[0].legs[0].distance);
                Log.d(TAG, "calculateDirections: geocodedWayPoints: " +
                        result.geocodedWaypoints[0].toString());

                addPolylineToMap(result);
            }

            @Override
            public void onFailure(Throwable e) {
                Log.e(TAG, "calculateDirections: Failed to get directions: "
                        + e.getMessage() );

            }
        });
    }

    // Add route to map
    private void addPolylineToMap(final DirectionsResult result){
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "run: result routes: " + result.routes.length);

                for(DirectionsRoute route: result.routes){
                    Log.d(TAG, "run: leg: " + route.legs[0].toString());
                    List<com.google.maps.model.LatLng> decodedPath =
                            PolylineEncoding.decode(route.overviewPolyline.getEncodedPath());

                    List<LatLng> newDecodedPath = new ArrayList<>();

                    // This loops through all the LatLng coordinates of ONE polyline.
                    for(com.google.maps.model.LatLng latLng: decodedPath){

                        // Log.d(TAG, "run: latlng: " + latLng.toString());

                        newDecodedPath.add(new LatLng(
                                latLng.lat,
                                latLng.lng
                        ));
                    }

                    // draw route on map
                    polyline = mMap.addPolyline(new PolylineOptions().addAll(newDecodedPath));
                    polyline.setColor(ContextCompat.getColor(getApplicationContext(),
                            R.color.blue));
                    // polyline.setClickable(true);

                    // add trip duration to marker
                    locationSearched.setSnippet("Time: " + route.legs[0].duration.toString());

                    // set camera so current location and destination can be seen
                    LatLngBounds.Builder builder = new LatLngBounds.Builder();
                    builder.include(mLatLng);
                    builder.include(locationSearched.getPosition());
                    mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(),300));

                }
            }
        });
    }

    // Remove route
    private void removePolyline() {
        if (polyline != null) {
            polyline.remove();
        }
    }

    // Pop up window for mode of transport selection
    private void transportModePopUp() {
        String[] transportMode = {"Driving", "Walking"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose mode of transport");
        builder.setItems(transportMode, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                switch (i) {
                    case 0: travelMode = TravelMode.DRIVING;
                    break;
                    case 1: travelMode = TravelMode.WALKING;
                    break;
                }

                calculateDirections(locationSearched, travelMode);
            }
        });
        builder.show();
    }



    @Override
    protected void onStop() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(bReceiver);
        super.onStop();
    }
}
