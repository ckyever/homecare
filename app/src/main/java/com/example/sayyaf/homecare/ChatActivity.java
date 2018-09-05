package com.example.sayyaf.homecare;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.support.v4.app.NotificationCompat;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.Button;

import com.firebase.ui.database.FirebaseListAdapter;
import com.firebase.ui.database.FirebaseListOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Date;

public class ChatActivity extends AppCompatActivity implements View.OnClickListener{

    private static boolean setUpChatController = false;
    public static ChatController chatController;
    //private ChatController chatController;

    private EditText textMsg;
    private TextView contactName;
    private ListView msgView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        textMsg = (EditText) findViewById(R.id.textInputs);
        msgView = (ListView) findViewById(R.id.msgView);
        contactName = (TextView) findViewById(R.id.contactName);

        /*chatController =
                    (ChatController) getIntent().getSerializableExtra("Chat Controller");*/

        chatController.displayReceiverName(contactName);

        chatController.initialiseContentUpdateAdapter(this,
                R.layout.msg_block_sender, R.layout.msg_block);

        chatController.setContentUpdateAdapter(msgView);
        chatController.listenToChatChanges();

    }

    @Override
    public void onClick(View v) {
       int button_id = v.getId();

        switch (button_id) {

            case R.id.voiceCall:
                // start a voice call
                chatController.startVoiceChat(this);
                break;

            case R.id.sendMsg:

                chatController.sendMsg(this, textMsg);
                break;

        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        chatController.listenToChatChanges();
    }

    @Override
    public void onBackPressed() {
        chatController.returnToMenu(this);
    }

}
