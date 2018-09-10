package com.example.sayyaf.homecare;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseListAdapter;
import com.firebase.ui.database.FirebaseListOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ContactController {

    private User this_device;
    private DatabaseReference userDB;
    private DatabaseReference friendDB;
    private DatabaseReference chatDB;

    //private FriendListAdapter contentUpdateAdapter;
    //private ArrayList<User> friends;
    //private ArrayList<String> friendIDs;

    public ContactController(User this_device, DatabaseReference userDB,
                             DatabaseReference friendDB, DatabaseReference chatDB){

        this.this_device = this_device;
        this.userDB = userDB;
        this.friendDB = friendDB;
        this.chatDB = chatDB;

        //friends = null;
        //friendIDs = null;
    }


    /*public void setContentUpdateAdapter(ContactChatActivity contactChatActivity, ListView contactView){

        friendDB.orderByValue().equalTo(this_device.getId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot datasnapshot) {
                if(datasnapshot.exists()){
                    ArrayList<String> friendIDs = null;

                    for (DataSnapshot snapshot : datasnapshot.getChildren()) {
                        friendIDs.add(snapshot.getKey().toString());
                    }

                    Log.d("friendIDs", Integer.toString(friendIDs.size()));

                    userDB.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot datasnapshot) {
                            if(datasnapshot.exists()){
                                ArrayList<User> friends = null;

                                if(friendIDs == null) return;

                                for(int i = 0; i < friendIDs.size(); i++){
                                    friends.add(datasnapshot.child(friendIDs.get(i)).getValue(User.class));
                                }

                                Log.d("friends", Integer.toString(friends.size()));

                                FirebaseListOptions<User> options = new FirebaseListOptions.Builder<User>()
                                        .setQuery(userDB, User.class).setLayout(R.layout.contact_block).build();

                                contentUpdateAdapter =
                                        new FriendListAdapter(contactChatActivity,
                                                R.layout.contact_block, friends, this_device);

                                contactView.setAdapter(contentUpdateAdapter);
                            }
                        }
                        @Override
                        public void onCancelled(DatabaseError arg0) {

                        }
                    });
                }

            }
            @Override
            public void onCancelled(DatabaseError arg0) {

            }
        });
    }

    /*public void getFriendsOfCurrentUser(){

        userDB.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot datasnapshot) {
                if(datasnapshot.exists()){
                    ArrayList<User> friends = null;

                    if(friendIDs == null) return;

                    for(int i = 0; i < friendIDs.size(); i++){
                        friends.add(datasnapshot.child(friendIDs.get(i)).getValue(User.class));
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError arg0) {

            }
        });

    }*/

    //  initialise ContentUpdateAdapter from database contents
    /*public void initialiseContentUpdateAdapter(
            ContactChatActivity contactChatActivity){

        contentUpdateAdapter =
                new FriendListAdapter(contactChatActivity, R.layout.contact_block, friends);
    }*/

    // set ContentUpdateAdapter for the view
    /*public void setContentUpdateAdapter(ListView contactView){
        if(friends == null) return;
        contactView.setAdapter(contentUpdateAdapter);
    }*/

    // start listen to the chat database (for updating view)
    // public void listenToChatChanges(){ contentUpdateAdapter.startListening(); }

    // stop listen to the chat database (not updating view)
    // public void stopListenToChatChanges(){ contentUpdateAdapter.stopListening(); }


    // move to menu
    public void returnToMenu(ContactChatActivity contactChatActivity){
        Intent goToMenu = new Intent(contactChatActivity, MainActivity.class);
        contactChatActivity.startActivity(goToMenu);
        contactChatActivity.finish();
    }

}
