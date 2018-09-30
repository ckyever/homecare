package com.example.sayyaf.homecare.communication;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import android.widget.ListView;
import android.widget.TextView;
import android.widget.EditText;

import com.example.sayyaf.homecare.R;
import com.example.sayyaf.homecare.accounts.User;
import com.example.sayyaf.homecare.notifications.EmergencyCallActivity;
import com.example.sayyaf.homecare.requests.RequestActivity;
import com.google.firebase.database.DatabaseReference;

public class ChatActivity extends AppCompatActivity implements View.OnClickListener{

    private static boolean setUpChatController = false;
    private static ChatController chatController;
    //private ChatController chatController;

    private EditText textMsg;
    private TextView contactName;
    private ListView msgView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // look for the UI elements
        textMsg = (EditText) findViewById(R.id.textInputs);
        msgView = (ListView) findViewById(R.id.msgView);
        contactName = (TextView) findViewById(R.id.contactName);

        /*chatController =
                    (ChatController) getIntent().getSerializableExtra("Chat Controller");*/

        // show the chat peer name
        chatController.displayReceiverName(contactName);

        // setup listener between chat database and the UI elements
        chatController.initialiseContentUpdateAdapter(this,
                R.layout.msg_block_sender, R.layout.msg_block);

        // setup listener between chat database and user view
        chatController.setContentUpdateAdapter(msgView);

        // activatily listen to chat database change
        chatController.listenToChatChanges();

    }

    // set up chat pair from the account list
    public static void setUpChatController(User this_device, User contact_person, DatabaseReference chatDB){
        chatController = new ChatController(this_device, contact_person, chatDB);
    }

    /*public static void listenToAdded(Context context, NotificationManager notificationManager, DatabaseReference chatDB){
        chatController.listenToAdded(context, notificationManager, chatDB);
    }

    public static void cancelAddListening(DatabaseReference chatDB){
        chatController.cancelAddListening(chatDB);
    }*/

    @Override
    public void onClick(View v) {
       int button_id = v.getId();

        switch (button_id) {

            case R.id.sendMsg:
                chatController.sendMsg(this, textMsg);
                break;

        }
    }

    @Override
    protected void onStart() {
        // listen to chat database changes and update the view
        super.onStart();
        chatController.listenToChatChanges();
    }

    @Override
    protected void onPause(){
        // stop listen to chat database and update view
        chatController.stopListenToChatChanges();
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        // go back to the contact page
        chatController.returnToMenu(this);
    }

}
