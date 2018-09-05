package com.example.sayyaf.homecare;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseListAdapter;
import com.firebase.ui.database.FirebaseListOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;
import java.util.Date;

public class ChatController {

    private User this_device, contact_person;
    private String chatDB;
    private FirebaseListAdapter<ChatMessage> contentUpdateAdapter;

    private ChildEventListener chatAddedListener;
    private long lastMsgTime;


    /*  set up chat control for user-contact person pair (on user adding)
     *  this_device: user of this device
     *  contact_person: chosen contact person to the user
     *  chatDB: database for the chatting pair
     */
    public ChatController(User this_device, User contact_person, String chatDB){

        this.this_device = this_device;
        this.contact_person = contact_person;
        this.chatDB = chatDB;

        chatAddedListener = null;
        lastMsgTime = 0;

    }

    /*  initialise ContentUpdateAdapter from database contents
     *  msg_block_this_device: message block for contents are send by this user
     *  msg_block_contact_person: message block for contents are send by your peer
     */
    public void initialiseContentUpdateAdapter(
            ChatActivity chatActivity, int msg_block_this_device, int msg_block_contact_person){

        FirebaseListOptions<ChatMessage> options = new FirebaseListOptions.Builder<ChatMessage>()
                .setQuery(FirebaseDatabase.getInstance().getReference(
                        "testChatDB"), ChatMessage.class).setLayout(msg_block_this_device).build();

        contentUpdateAdapter = new FirebaseListAdapter<ChatMessage>(options){
            @Override
            public View getView(int position, View v, ViewGroup viewGroup) {

                ChatMessage ct = this.getItem(position);

                if(ct.getMessageSender().equals(/*this_device.getName()*/
                        FirebaseAuth.getInstance().getCurrentUser().getEmail())){
                    v = LayoutInflater.from(chatActivity.getApplicationContext()).
                            inflate(msg_block_this_device/*R.layout.msg_block_sender*/, viewGroup, false);
                }
                else{
                    v = LayoutInflater.from(chatActivity.getApplicationContext()).
                            inflate(msg_block_contact_person/*R.layout.msg_block*/, viewGroup, false);

                }

                // Call out to subclass to marshall this model into the provided view
                populateView(v, ct, position);
                return v;
            }

            @Override
            protected void populateView(android.view.View v, ChatMessage ct, int position) {

                // referencing text views
                TextView messageText = (TextView) v.findViewById(R.id.messageText);
                TextView messageUser = (TextView) v.findViewById(R.id.username);
                TextView messageTime = (TextView) v.findViewById(R.id.sendTime);

                // Set their text
                messageText.setText(ct.getMessageText());
                messageUser.setText(ct.getMessageSender());

                // Format the date before showing it
                messageTime.setText(DateFormat.format("d/M/yy (h:mm a)",
                        ct.getMessageTime()));
            }

        };
    }

    // set ContentUpdateAdapter for the view
    public void setContentUpdateAdapter(ListView msgView){
        msgView.setAdapter(contentUpdateAdapter);
    }

    // start listen to the chat database (for updating view)
    public void listenToChatChanges(){
        contentUpdateAdapter.startListening();
    }

    /*  post message to the chat database
     *  textMsg: holds text input
     */
    public void sendMsg(ChatActivity chatActivity, EditText textMsg){
        // handle click on sending when there is non-space texts
        if(vaildateMsgContent(textMsg)){

            // upload msg to db

            ChatMessage ct = new ChatMessage(trimmedContent(textMsg),
                    FirebaseAuth.getInstance().getCurrentUser().getEmail()
                    /*this_device.getName()*/);

            // update time that user interacted with message
            lastMsgTime = ct.getMessageTime();

            FirebaseDatabase.getInstance().getReference("testChatDB")
                    .push().setValue(ct);

            cleanup(textMsg);

        }
    }

    // get rid of spaces before and after string
    private String trimmedContent(EditText textMsg){
        return textMsg.getText().toString().trim();
    }

    // return true for a non empty string
    private boolean vaildateMsgContent(EditText textMsg){
        // handle click on sending when there is non-space texts
        return trimmedContent(textMsg).length() != 0;
    }

    // blank out input field
    private void cleanup(EditText textMsg){
        // clear out text message input
        textMsg.setText("");
    }

    // listen to new message sent to user (may move to notification base controller)
    public void listenToAdded(Activity activity, NotificationManager notificationManager){

        //if(chatAddedListener == null)
            chatAddedListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    if(dataSnapshot.exists()){
                        String username = dataSnapshot.child("messageSender").getValue().toString();
                        long sendTime = (long) dataSnapshot.child("messageTime").getValue();

                        if(sendTime > lastMsgTime &&
                                !username.equals(FirebaseAuth.getInstance().getCurrentUser().getEmail())){
                            newIncomingNotification(activity, notificationManager,
                                    username,
                                    dataSnapshot.child("messageText").getValue().toString());

                            lastMsgTime = sendTime;
                        }
                    }
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {

                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            };

        FirebaseDatabase.getInstance().getReference(
                "testChatDB").orderByKey().limitToLast(1).addChildEventListener(chatAddedListener);
    }

    // pause listening to new message sent to user (may move to notification base controller)
    public void cancelAddListening(){
        FirebaseDatabase.getInstance().getReference(
                "testChatDB").removeEventListener(chatAddedListener);
    }

    // fire the notification (may move to notification base controller)
    private void newIncomingNotification(
            Activity activity, NotificationManager notificationManager,
            String sender, String contents){

        NotificationCompat.Builder notificationbulider =
                new NotificationCompat.Builder(activity, NotificationChannals.getNotificationCH())
                        .setSmallIcon(R.drawable.ic_launcher_background)
                        .setContentTitle("New message from: " + sender)
                        .setContentText(contents)
                        .setDefaults(Notification.DEFAULT_ALL)
                        .setCategory(NotificationCompat.CATEGORY_MESSAGE);

        Intent intent = new Intent(activity, MainActivity.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                activity, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        notificationbulider.setContentIntent(pendingIntent);

        /*NotificationManager nM =
                (NotificationManager) activity.getSystemService(Context.NOTIFICATION_SERVICE);*/

        if(notificationManager != null)
            notificationManager.notify(1, notificationbulider.build());
    }

    // show peer's user name
    public void displayReceiverName(TextView contactName) {
        contactName.setText(contact_person.getName());
    }

    // move to voice chat
    public void startVoiceChat(ChatActivity chatActivity){
        Intent goToCallPage = new Intent(chatActivity, VoiceCallActivity.class);
        chatActivity.startActivity(goToCallPage);
        chatActivity.finish();
    }

    // move to menu
    public void returnToMenu(ChatActivity chatActivity){
        Intent goToMenu = new Intent(chatActivity, MainActivity.class);
        chatActivity.startActivity(goToMenu);
        chatActivity.finish();
    }


}
