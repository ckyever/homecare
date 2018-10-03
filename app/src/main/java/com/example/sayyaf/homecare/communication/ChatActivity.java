package com.example.sayyaf.homecare.communication;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.EditText;

import com.example.sayyaf.homecare.R;
import com.example.sayyaf.homecare.accounts.User;
import com.example.sayyaf.homecare.accounts.UserAppVersionController;
import com.example.sayyaf.homecare.contacts.ContactChatActivity;
import com.example.sayyaf.homecare.notifications.EmergencyCallActivity;
import com.example.sayyaf.homecare.notifications.NetworkConnection;
import com.example.sayyaf.homecare.requests.RequestActivity;
import com.google.firebase.database.DatabaseReference;

public class ChatActivity extends AppCompatActivity implements View.OnClickListener{

    private static ChatController chatController;

    private EditText textMsg;
    private TextView contactName;
    private ListView msgView;
    private Button sendMsg;
    private Button helpButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // look for the UI elements
        textMsg = (EditText) findViewById(R.id.textInputs);
        msgView = (ListView) findViewById(R.id.msgView);
        contactName = (TextView) findViewById(R.id.contactName);

        sendMsg = (Button) findViewById(R.id.sendMsg);
        helpButton = (Button) findViewById(R.id.optionHelp);

        // activate help button on assisted person version
        UserAppVersionController.getUserAppVersionController().resetButton(helpButton);

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

    @Override
    public void onClick(View v) {
        if(!NetworkConnection.getConnection()){
            NetworkConnection.requestNetworkConnection(ChatActivity.this);
            return;
        }

        if(v != textMsg){
            // hide keyboard
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        }

       if(v == sendMsg){
           chatController.sendMsg(this, textMsg);
       }

       if(v == helpButton){
           EmergencyCallActivity.setBackToActivity(ChatActivity.class);

           Intent intent = new Intent(ChatActivity.this, EmergencyCallActivity.class);
           intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
           startActivity(intent);
           finish();
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
