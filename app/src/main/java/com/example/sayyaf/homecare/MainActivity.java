package com.example.sayyaf.homecare;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Build;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.sayyaf.homecare.notifications.EmergencyCallActivity;
import com.example.sayyaf.homecare.notifications.NotificationService;
import com.example.sayyaf.homecare.communication.BaseActivity;
import com.example.sayyaf.homecare.communication.SinchService;
import com.example.sayyaf.homecare.requests.RequestActivity;
import com.example.sayyaf.homecare.accounts.LoginActivity;
import com.example.sayyaf.homecare.contacts.ContactChatActivity;
import com.example.sayyaf.homecare.contacts.ContactUpdateActivity;
import com.example.sayyaf.homecare.mapping.MapsActivity;
import com.example.sayyaf.homecare.mapping.TrackingActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sinch.android.rtc.SinchError;


public class MainActivity extends BaseActivity implements View.OnClickListener,SinchService.StartFailedListener {

    // set up chat controllers for once
    private static boolean setUpChatControllers = false;
    private static final String TAG = "MainActivity";
    //private ChatController chatController;
    Button mMapButton;
    Button mContacts;
    Button mContactsUpdate;
    Button mFriendRequests;
    Button logoutButton;
    Button helpButton;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // start foreground service
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            this.startService(new Intent(this, NotificationService.class));
        }
        else {
            this.startForegroundService(new Intent(this, NotificationService.class));
        }

        mMapButton = findViewById(R.id.mapButton);
        mMapButton.setOnClickListener(this);

        mContacts = (Button) findViewById(R.id.optionContacts);
        mContacts.setOnClickListener(this);
        mContactsUpdate = (Button) findViewById(R.id.contactsUpdate);
        mContactsUpdate.setOnClickListener(this);

        logoutButton = findViewById(R.id.logoutButton);
        logoutButton.setOnClickListener(this);

        mFriendRequests = (Button) findViewById(R.id.friendRequests);
        mFriendRequests.setOnClickListener(this);

        helpButton = (Button) findViewById(R.id.optionHelp);
        helpButton.setOnClickListener(this);

    }

    @Override
    public void onServiceConnected() {
        if (!getSinchServiceInterface().isStarted()) {
            getSinchServiceInterface().startClient(FirebaseAuth.getInstance().getCurrentUser().getUid());
        }
    }

    @Override
    public void onClick(View view) {

        if(view == mMapButton) {
            mapLauncher();
        }

        if (view == mContacts) {
            Intent intent = new Intent(MainActivity.this, ContactChatActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }

        if (view == mContactsUpdate) {
            Intent intent = new Intent(MainActivity.this, ContactUpdateActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }

        if(view == logoutButton){
            logout();
        }

        if (view == mFriendRequests) {
            Intent intent = new Intent(MainActivity.this, RequestActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }

        if(view == helpButton){
            EmergencyCallActivity.setBackToActivity(MainActivity.class);

            Intent intent = new Intent(MainActivity.this, EmergencyCallActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
    }

    private void logout(){
        // logout from firebase
        FirebaseAuth.getInstance().signOut();

        // stop foreground service
        this.stopService(new Intent(this, NotificationService.class));
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);

    }

    /*@Override
    protected void onStart(){
        super.onStart();
        ChatActivity.listenToAdded(this,
                (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE),
                FirebaseDatabase.getInstance().getReference("testChatDB"));
        //chatController.listenToAdded(this, (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE));

    }

    @Override
    protected void onPause(){
        ChatActivity.cancelAddListening(FirebaseDatabase.getInstance().getReference("testChatDB"));
        //chatController.cancelAddListening();
        super.onPause();
    }*/

    /*@Override
    protected void onDestroy(){
        this.stopService(new Intent(this, NotificationService.class));
        super.onDestroy();
    }*/

    public void onBackPressed() {
        super.onBackPressed();
    }

    // Launches the TrackingActivity if current user is a caregiver and the MapsActivity if current
    // user is an assisted person
    private void mapLauncher() {
        String path = "User/" + FirebaseAuth.getInstance().getCurrentUser().getUid() + "/caregiver";
        DatabaseReference mRef = FirebaseDatabase.getInstance().getReference(path);
        mRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Boolean result = (Boolean) dataSnapshot.getValue();
                    // User is a caregiver
                    if (result) {
                        Intent intent = new Intent(MainActivity.this,
                                TrackingActivity.class);
                        startActivity(intent);
                    }
                    // User is an assisted person
                    else {
                        Intent intent = new Intent(MainActivity.this,
                                MapsActivity.class);
                        startActivity(intent);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w(TAG, "Failed to get caregiver boolean", databaseError.toException());
            }
        });

    }

    @Override
    public void onStarted() {
        mContacts.setEnabled(true);
    }

    @Override
    public void onStartFailed(SinchError error) {
        Toast.makeText(this, error.toString(), Toast.LENGTH_LONG).show();
    }

    private boolean readyService(String username) {

        if (getSinchServiceInterface() != null && !getSinchServiceInterface().isStarted()) {
            getSinchServiceInterface().startClient(username);
            return true;
        }
        else {
            return false;
        }
    }

}
