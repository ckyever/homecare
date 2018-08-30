package com.example.sayyaf.homecare;

import android.app.Notification;
import android.support.v4.app.NotificationCompat;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;

import android.widget.TextView;
import android.widget.EditText;
import android.widget.Button;
import android.widget.ListView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.firebase.ui.database.FirebaseListAdapter;
import com.firebase.ui.database.FirebaseListOptions;

import java.util.Date;

public class ChatActivity extends AppCompatActivity implements View.OnClickListener{

    //private User sender, receiver = null;
    //private String directedConnectionDB = null;
    private static String notificationCH = "ch1";
    private NotificationManagerCompat notificationManager;

    private String tempUsername;

    private Button sendButton;
    private EditText textMsg;
    private TextView contactName;
    private ListView msgView;

    private FirebaseListAdapter<ChatMessage> textMsgViewAdapter;

    /*public ChatActivity(User sender, User receiver){
        //this.sender = sender;
        //this.receiver = receiver;

    }*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        createNotificationChannels();
        notificationManager = NotificationManagerCompat.from(this);

        // temp username
        tempUsername = FirebaseAuth.getInstance().getCurrentUser().getEmail();

        textMsg = (EditText) findViewById(R.id.textInputs);
        msgView = (ListView) findViewById(R.id.msgView);
        contactName = (TextView) findViewById(R.id.contactName);

        // contactName.setText(receiver.getName())

        textMsgViewAdapter = setTextMsgViewAdapter();
        msgView.setAdapter(textMsgViewAdapter);

        // msgView.smoothScrollToPosition(msgView.getAdapter().getCount() - 1);
    }

    @Override
    public void onClick(View v) {
       int button_id = v.getId();

        switch (button_id) {

            case R.id.voiceCall:
                // start a voice call
                Intent goToCallPage = new Intent(ChatActivity.this, VoiceCallActivity.class);
                startActivity(goToCallPage);
                finish();
                break;

            case R.id.sendMsg:

                // handle click on sending when there is non-space texts
                if(textMsg.getText().toString().trim().length() != 0){

                    // upload msg to db
                    FirebaseDatabase.getInstance().getReference(
                            "testChatDB").push().setValue(
                                    new ChatMessage(textMsg.getText().toString().trim(), tempUsername))
                            .addOnFailureListener(uploadListener());

                    // clear out text message input
                    textMsg.setText("");

                    // msgView.smoothScrollToPosition(msgView.getAdapter().getCount() - 1);
                }
                break;

            case R.id.textInputs:
                // msgView.smoothScrollToPosition(msgView.getAdapter().getCount() - 1);
                break;

        }
    }

    private FirebaseListAdapter<ChatMessage> setTextMsgViewAdapter(){

        FirebaseListOptions<ChatMessage> options = new FirebaseListOptions.Builder<ChatMessage>()
                .setQuery(FirebaseDatabase.getInstance().getReference(
                        "testChatDB"), ChatMessage.class)
                .setLayout(R.layout.msg_block)
                .build();

        return new FirebaseListAdapter<ChatMessage>(options){

            protected void populateView(View v, ChatMessage ct, int position) {
                // referencing text views
                TextView messageText = (TextView) v.findViewById(R.id.messageText);
                TextView messageUser = (TextView) v.findViewById(R.id.username);
                TextView messageTime = (TextView) v.findViewById(R.id.sendTime);

                // Set their text
                messageText.setText(ct.getMessageText());
                messageUser.setText(ct.getMessageSender());

                // Format the date before showing it
                messageTime.setText(DateFormat.format("dd-MM-yy (HH:mm)",
                        ct.getMessageTime()));
            }

        };
    }

    @Override
    protected void onStart() {
        super.onStart();
        textMsgViewAdapter.startListening();
    }

    @Override
    public void onBackPressed() {
        Intent goToMenu = new Intent(ChatActivity.this, MainActivity.class);
        startActivity(goToMenu);
        finish();
    }

    public OnFailureListener uploadListener(){
        return new OnFailureListener(){
            @Override
            public void onFailure(@NonNull Exception e) {
                // Write failed
                String dateTimeFail = DateFormat.format("dd-MM-yy (HH:mm)",
                        new Date().getTime()).toString();
                Log.d(dateTimeFail, e.getMessage());
                uploadFailNotification(e.getMessage());
            }
        };
    }

    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel1 = new NotificationChannel(
                    notificationCH,
                    "CH 1",
                    NotificationManager.IMPORTANCE_HIGH);

            channel1.setDescription("This is CH 1");

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel1);
        }
    }

    private void uploadFailNotification(String error_msg){
        // Builds your notification
        Notification notification = new NotificationCompat.Builder(this, notificationCH)
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentTitle("Send Fail")
                .setContentText("Fail to uploaded message to database: "
                        + error_msg + "/nat "
                        + DateFormat.format("dd-MM-yy (HH:mm)",
                        new Date().getTime()).toString())
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .build();

        notificationManager.notify(1, notification);
    }

}
