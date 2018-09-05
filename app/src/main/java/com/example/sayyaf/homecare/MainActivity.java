package com.example.sayyaf.homecare;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;


public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    // set up chat controllers for once
    private static boolean setUpChatControllers = false;
    //private ChatController chatController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(!setUpChatControllers){
            ChatActivity.chatController =
                    new ChatController(new User("", "", false),
                            new User("Fake name", "", false), "");

            /*chatController = new ChatController(new User("", "", false),
                    new User("Fake name", "", false), "");*/

            setUpChatControllers = true;
        }


    }

    @Override
    public void onClick(View v) {
        int button_id = v.getId();
        Log.i(v.toString(), v.toString());

        switch (button_id){
            case R.id.optionContacts:
                Intent intent = new Intent(MainActivity.this, ChatActivity.class);
                //intent.putExtra("Chat Controller", chatController);
                startActivity(intent);
                finish();
                break;
        }
    }

    @Override
    protected void onStart(){
        super.onStart();
        ChatActivity.chatController.listenToAdded(this, (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE));
        //chatController.listenToAdded(this, (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE));

    }

    @Override
    protected void onPause(){
        ChatActivity.chatController.cancelAddListening();
        //chatController.cancelAddListening();
        super.onPause();
    }

}
