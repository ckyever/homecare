package com.example.sayyaf.homecare;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import butterknife.OnClick;

import com.google.firebase.database.FirebaseDatabase;


public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    // set up chat controllers for once
    private static boolean setUpChatControllers = false;
    //private ChatController chatController;
    Button mMapButton;
    Button mContacts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(!setUpChatControllers){
            ChatActivity.setUpChatController(new User("", "", false),
                    new User("Fake name", "", false),
                    FirebaseDatabase.getInstance().getReference("testChatDB"));

            /*if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {

                this.startService(new Intent(this, NotificationService.class));
            }
            else {
                this.startForegroundService(new Intent(this, NotificationService.class));
            }*/


            /*chatController = new ChatController(new User("", "", false),
                    new User("Fake name", "", false), "");*/

            setUpChatControllers = true;
        }

        mMapButton = findViewById(R.id.mapButton);
        mMapButton.setOnClickListener(this);

        mContacts = (Button) findViewById(R.id.optionContacts);
        mContacts.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if(view == mMapButton) {
            Intent intent = new Intent(MainActivity.this, MapsActivity.class);
            startActivity(intent);
        }

        if (view == mContacts) {
            Intent intent = new Intent(MainActivity.this, ContactsActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
    }

    @Override
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
    }

    /*@Override
    protected void onDestroy(){
        this.stopService(new Intent(this, NotificationService.class));
        super.onDestroy();
    }*/

}
