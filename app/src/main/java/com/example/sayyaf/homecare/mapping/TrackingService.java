package com.example.sayyaf.homecare.mapping;

import android.Manifest;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.example.sayyaf.homecare.accounts.UserAppVersionController;
import com.example.sayyaf.homecare.notifications.EmergencyMsgListener;
import com.example.sayyaf.homecare.notifications.NotificationChannels;
import com.example.sayyaf.homecare.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class TrackingService extends Service {
    private static final String TAG = "TrackingService";

    FusedLocationProviderClient mClient;
    LocationCallback mLocationCallback;
    LocationRequest mRequest;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        buildNotification();
        requestLocationUpdates();
    }

    /**
     * Creates a persistent notification that tells the user that they are being tracked.
     */
    private void buildNotification() {
        String stop = "stop";
        registerReceiver(stopReceiver, new IntentFilter(stop));
        PendingIntent broadcastIntent = PendingIntent.getBroadcast(
                this, 0, new Intent(stop), PendingIntent.FLAG_UPDATE_CURRENT);
        // Create the persistent notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this,
                NotificationChannels.getChatNotificationCH())
                .setContentTitle(getString(R.string.tracking_title))
                .setContentText(getString(R.string.tracking_notification))
                .setOngoing(true)
                .setContentIntent(broadcastIntent)
                .setSmallIcon(R.drawable.ic_tracking);
        startForeground(1, builder.build());
    }

    /**
     * Stops the tracking service if user taps the notification.
     */
    protected BroadcastReceiver stopReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "received stop broadcast");
            // Stop the service when the notification is tapped
            if (stopReceiver != null) {
                unregisterReceiver(stopReceiver);
            }
            stopSelf();
        }
    };

    /**
     * Checks location permissions then creates a request to receive location updates with
     * FusedLocationProvider, and upon receiving a new location, we store it on the Firebase
     * Realtime Database.
     */
    private void requestLocationUpdates() {
        mRequest = new LocationRequest();
        mRequest.setInterval(10000);
        mRequest.setFastestInterval(5000);
        mRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mClient = LocationServices.getFusedLocationProviderClient(this);
        int permission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);

        // Get current user's uid and set path as a locations subtree within user's database path
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String uid = user.getUid();
        final String path = "User/" + uid + "/location";

        if (permission == PackageManager.PERMISSION_GRANTED) {
            // Request location updates and when an update is
            // received, store it under the database path User/uid/location
            mClient.requestLocationUpdates(mRequest, mLocationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {

                    FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
                    DatabaseReference mRef = mDatabase.getReference(path);
                    Location location = locationResult.getLastLocation();

                    if (location != null) {
                        Log.d(TAG, "location update " + location);
                        mRef.setValue(location);
                        sendBroadcast(location);
                    }
                }
            }, null);
        }
    }

    // Allows the MapsActivity to add a listener for this service and obtain real time latlng values

    /**
     * Allows the MapsActivity to add a listener for this service and obtain real time latlng
     * values via Intent extras holding the individual Latitude and Longitude values.
     */
    private void sendBroadcast (Location location) {
        Intent intent = new Intent (TAG);
        intent.putExtra("Latitude", location.getLatitude());
        intent.putExtra("Longitude", location.getLongitude());
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    // Ensure that location updates have been stopped
    @Override
    public void onDestroy() {
        if (mClient != null) {
            mClient.removeLocationUpdates(mLocationCallback);
        }
        stopSelf();
        super.onDestroy();
    }

}
