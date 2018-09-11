package com.example.sayyaf.homecare;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ContactChatActivity extends AppCompatActivity implements View.OnClickListener {

    private DatabaseReference ref;
    private User this_device;
    private User friend;

    private EditText textInputs;
    private Button searchUser;
    private Button refreshList;
    private ArrayList<User> friends;
    private ListView contactView;
    private ContactUserListAdapter contactUserListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);

        textInputs = (EditText) findViewById(R.id.textInputs);
        searchUser = (Button) findViewById(R.id.searchUser);
        refreshList = (Button) findViewById(R.id.refreshList);
        contactView = (ListView) findViewById(R.id.contactView);

        ref = FirebaseDatabase.getInstance().getReference();

        getCurrentUser();
        friends = new ArrayList<User>();

        //wait for database fetch complete
        getFriends(new ContactUserListCallback(){
            @Override
            public void onCallback(ArrayList<User> friends){
                contactUserListAdapter =
                        new ContactUserListAdapter(ContactChatActivity.this, R.layout.contact_block,
                                friends, this_device, ref);

                contactView.setAdapter(contactUserListAdapter);

            }
        });

    }

    @Override
    public void onClick(View v) {

        String starter = textInputs.getText().toString().trim();

        if(v == searchUser){
            //checkUser(email);
            if(starter == null || starter.isEmpty()) return;

            getFriends(new ContactUserListCallback(){
                @Override
                public void onCallback(ArrayList<User> friends){

                    contactUserListAdapter =
                            new ContactUserListAdapter(ContactChatActivity.this, R.layout.contact_block,
                                    friends, this_device, ref);

                    contactView.setAdapter(contactUserListAdapter);

                }
            }, starter);
        }

        if(v == refreshList){
            getFriends(new ContactUserListCallback(){
                @Override
                public void onCallback(ArrayList<User> friends){
                    contactUserListAdapter =
                            new ContactUserListAdapter(ContactChatActivity.this, R.layout.contact_block,
                                    friends, this_device, ref);

                    contactView.setAdapter(contactUserListAdapter);

                }
            });
        }

    }

    // debug getting data
    public void showFriends(){
        if(friends == null){
            System.out.println("null");
        }
        else if(friends.isEmpty()) {
            System.out.println("empty");
        }
        else{
            for(User u : friends) System.out.println(u.getName());
        }
    }

    public void getCurrentUser() {
        Query query = ref.child("User").orderByChild("id")
                .equalTo(FirebaseAuth.getInstance().getCurrentUser().getUid());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot datasnapshot) {
                for (DataSnapshot s : datasnapshot.getChildren()) {
                    if (s.exists()) {
                        this_device = s.getValue(User.class);
                    }
                }

            }
            @Override
            public void onCancelled(DatabaseError arg0) {

            }
        });
    }

    public void getFriends(ContactUserListCallback contactUserListCallback){
        getFriends(contactUserListCallback, null);
    }

    public void getFriends(ContactUserListCallback contactUserListCallback, String starter){
        ref.child("User").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){

                    friends = new ArrayList<User>();

                    for (DataSnapshot s : dataSnapshot.getChildren()) {
                        if(s.exists()){
                            User fd = s.getValue(User.class);

                            if (fd.getChatDatabase() != null){
                                if(fd.getChatDatabase().containsKey(this_device.getId())){
                                    if(starter == null)
                                        friends.add(fd);
                                    else if(fd.getName().startsWith(starter)
                                            || fd.getEmail().startsWith(starter))
                                        friends.add(fd);
                                }
                            }
                        }

                    }

                    if(friends.isEmpty()){
                        Toast.makeText(ContactChatActivity.this,
                                "No result",
                                Toast.LENGTH_SHORT).show();
                    }

                    contactUserListCallback.onCallback(friends);
                }
                else{
                    Toast.makeText(ContactChatActivity.this,
                            "No result",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void checkUser(String starter) {


        /*Query query = ref.child("User").orderByChild("email")
                .equalTo(email);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot s : dataSnapshot.getChildren()) {
                    if (s.exists()) {
                        friend = s.getValue(User.class);

                        if(friend.getChatDatabase() == null
                                || !friend.getChatDatabase().containsKey(this_device.getId())) {
                            Toast.makeText(ContactChatActivity.this,
                                    "This person is not added as a friend",
                                    Toast.LENGTH_SHORT).show();
                        }else{

                            goToChat(friend.getChatDatabase().get(this_device.getId()));
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });*/
    }


    public void goToChat(String chatDB){

        ChatActivity.setUpChatController(this_device,
                friend,
                ref.child("chatDB").child(chatDB));

        Intent intent = new Intent(ContactChatActivity.this, ChatActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        Intent goToMenu = new Intent(ContactChatActivity.this, MainActivity.class);
        goToMenu.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(goToMenu);
        finish();
    }
}
