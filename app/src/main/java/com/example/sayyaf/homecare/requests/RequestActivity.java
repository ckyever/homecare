package com.example.sayyaf.homecare.requests;

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

import com.example.sayyaf.homecare.MainActivity;
import com.example.sayyaf.homecare.R;
import com.example.sayyaf.homecare.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class RequestActivity extends AppCompatActivity implements RequestsUserListCallback {

    private DatabaseReference ref;
    private User this_device;

    private ArrayList<User> friends;
    private ListView requestsView;
    private RequestUserListAdapter requestUserListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_requests);

        requestsView = (ListView) findViewById(R.id.requestsView);

        ref = FirebaseDatabase.getInstance().getReference();
        friends = new ArrayList<User>();
        getCurrentUser(null);

    }

    @Override
    public void onRequestsCallback(User currentUser) {
        getRequests(currentUser);
    }

    @Override
    public void onFriendsCallback(ArrayList<User> requests, User currentUser){
        requestUserListAdapter =
                new RequestUserListAdapter(RequestActivity.this, R.layout.request_block,
                        requests, currentUser, ref);
        requestsView.setAdapter(requestUserListAdapter);

    }


    public void getCurrentUser(String starter) {
        Query query = ref.child("User").orderByChild("id")
                .equalTo(FirebaseAuth.getInstance().getCurrentUser().getUid());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot datasnapshot) {
                for (DataSnapshot s : datasnapshot.getChildren()) {
                    if (s.exists()) {
                        this_device = s.getValue(User.class);
                        onRequestsCallback(this_device);
                    }
                }
            }


            @Override
            public void onCancelled(DatabaseError arg0) {

            }
        });
    }


    public void getRequests(User currentUser){
        ref.child("User").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){

                    friends = new ArrayList<User>();

                    for (DataSnapshot s : dataSnapshot.getChildren()) {
                        if(s.exists()){
                            User fd = s.getValue(User.class);

                            if (currentUser.getRequests() != null &&
                                    currentUser.getRequests().containsKey(fd.getId())){

                                    friends.add(fd);
                            }
                        }

                    }

                    if(!friends.isEmpty()){
                        onFriendsCallback(friends, currentUser);
                    } else{
                        Toast.makeText(RequestActivity.this,
                                "No Pending Requests",
                                Toast.LENGTH_SHORT).show();
                    }
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
                            Toast.makeText(RequestActivity.this,
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



    @Override
    public void onBackPressed() {
        Intent goToMenu = new Intent(RequestActivity.this, MainActivity.class);
        goToMenu.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(goToMenu);
        finish();
    }
}
