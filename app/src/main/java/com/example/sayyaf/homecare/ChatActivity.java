package com.example.sayyaf.homecare;

import android.content.Intent;
import android.graphics.Color;
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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.firebase.ui.database.FirebaseListAdapter;
import com.firebase.ui.database.FirebaseListOptions;

public class ChatActivity extends AppCompatActivity implements View.OnClickListener{

    //private User sender, receiver = null;
    //private String directedConnectionDB = null;
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

        // temp username
        tempUsername = FirebaseAuth.getInstance().getCurrentUser().getEmail();

        textMsg = (EditText) findViewById(R.id.textInputs);
        msgView = (ListView) findViewById(R.id.msgView);

        textMsgViewAdapter = setTextMsgViewAdapter();
        msgView.setAdapter(textMsgViewAdapter);
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
                                    new ChatMessage(textMsg.getText().toString().trim(), tempUsername));

                    // clear out text message input
                    textMsg.setText("");
                }
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

}
