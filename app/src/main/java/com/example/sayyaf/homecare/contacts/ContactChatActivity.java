package com.example.sayyaf.homecare.contacts;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.example.sayyaf.homecare.MainActivity;
import com.example.sayyaf.homecare.R;
import com.example.sayyaf.homecare.accounts.User;
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

        // look for the UI elements
        textInputs = (EditText) findViewById(R.id.textInputs);
        searchUser = (Button) findViewById(R.id.searchUser);
        refreshList = (Button) findViewById(R.id.refreshList);
        contactView = (ListView) findViewById(R.id.contactView);

        ref = FirebaseDatabase.getInstance().getReference();

        getCurrentUser();
        friends = new ArrayList<User>();

        // get all added contacts
        getFriends(new ContactUserListCallback(){

            // wait for database fetch complete and update the listing
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

        // search user by name or email (added contacts)
        if(v == searchUser){

            // ignore empty input
            if(starter == null || starter.isEmpty()) return;

            getFriends(new ContactUserListCallback(){
                @Override
                public void onCallback(ArrayList<User> friends){

                    // wait for database fetch complete and update the listing
                    contactUserListAdapter =
                            new ContactUserListAdapter(ContactChatActivity.this, R.layout.contact_block,
                                    friends, this_device, ref);

                    contactView.setAdapter(contactUserListAdapter);

                }
            }, starter, true);
        }

        // refresh the contact listing (show all added contacts)
        if(v == refreshList){
            getFriends(new ContactUserListCallback(){
                @Override
                public void onCallback(ArrayList<User> friends){

                    // wait for database fetch complete and update the listing
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

    // get user of this device
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

    // get all added contacts
    public void getFriends(ContactUserListCallback contactUserListCallback){
        getFriends(contactUserListCallback, null, false);
    }

    // get added contacts base on the search
    public void getFriends(ContactUserListCallback contactUserListCallback, String starter, boolean showResult){
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
                                        // add all friends
                                        friends.add(fd);
                                    else if(fd.getName().startsWith(starter)
                                            || fd.getEmail().startsWith(starter))
                                        // 
                                        friends.add(fd);
                                }
                            }
                        }

                    }

                    // no friends match the starting input letters
                    if(friends.isEmpty() && showResult){
                        Toast.makeText(ContactChatActivity.this,
                                "No result",
                                Toast.LENGTH_SHORT).show();
                    }

                    contactUserListCallback.onCallback(friends);
                }
                else{
                    // user do not have a added friend
                    if(showResult)
                        Toast.makeText(ContactChatActivity.this,
                                "No added contacts",
                                Toast.LENGTH_SHORT).show();

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onBackPressed() {
        // back to menu page
        Intent goToMenu = new Intent(ContactChatActivity.this, MainActivity.class);
        goToMenu.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(goToMenu);
        finish();
    }
}
