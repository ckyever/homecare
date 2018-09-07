package com.example.sayyaf.homecare;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.firebase.database.FirebaseDatabase;

import butterknife.OnClick;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    Button mContacts;
    Button mContactsUpdate;

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
        mContactsUpdate = (Button) findViewById(R.id.contactsUpdate);
        mContactsUpdate.setOnClickListener(this);
    }


    @Override
    public void onClick(View view) {

        if (view == mContacts) {
            Intent intent = new Intent(MainActivity.this, ContactsActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
    }
}
