package com.example.sayyaf.homecare;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.sayyaf.homecare.requests.RequestActivity;
import com.example.sayyaf.homecare.accounts.LoginActivity;
import com.example.sayyaf.homecare.contacts.ContactChatActivity;
import com.example.sayyaf.homecare.contacts.ContactsActivity;
import com.example.sayyaf.homecare.mapping.MapsActivity;
import com.example.sayyaf.homecare.mapping.TrackingActivity;
import com.google.firebase.auth.FirebaseAuth;


public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    // set up chat controllers for once
    private static boolean setUpChatControllers = false;
    //private ChatController chatController;
    Button mMapButton;
    Button mContacts;
    Button mContactsUpdate;
    Button mFriendRequests;
    Button mTrackingButton;
    Button logoutButton;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

            /*if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {

                this.startService(new Intent(this, NotificationService.class));
            }
            else {
                this.startForegroundService(new Intent(this, NotificationService.class));
            }*/


            /*chatController = new ChatController(new User("", "", false),
                    new User("Fake name", "", false), "");*/

        mMapButton = findViewById(R.id.mapButton);
        mMapButton.setOnClickListener(this);

        mContacts = (Button) findViewById(R.id.optionContacts);
        mContacts.setOnClickListener(this);
        mContactsUpdate = (Button) findViewById(R.id.contactsUpdate);
        mContactsUpdate.setOnClickListener(this);

        mTrackingButton = findViewById(R.id.trackingButton);
        mTrackingButton.setOnClickListener(this);

        logoutButton = findViewById(R.id.logoutButton);
        logoutButton.setOnClickListener(this);

        mFriendRequests = (Button) findViewById(R.id.friendRequests);
        mFriendRequests.setOnClickListener(this);

    }


    @Override
    public void onClick(View view) {
        if(view == mMapButton) {
            Intent intent = new Intent(MainActivity.this, MapsActivity.class);
            startActivity(intent);
        }

        if (view == mContacts) {
            Intent intent = new Intent(MainActivity.this, ContactChatActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }

        if (view == mContactsUpdate) {
            Intent intent = new Intent(MainActivity.this, ContactsActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }

        if(view == mTrackingButton) {
            Intent intent = new Intent(MainActivity.this, TrackingActivity.class);
            startActivity(intent);
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
    }

    private void logout(){
        // logout from firebase
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
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
        finish();
    }

}
