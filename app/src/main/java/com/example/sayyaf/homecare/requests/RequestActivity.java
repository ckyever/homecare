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

public class RequestActivity extends AppCompatActivity {

    private DatabaseReference ref;
    private User this_device;
    private User friend;

    private EditText textInputs;
    private Button searchUser;
    private Button refreshList;
    private ArrayList<User> friends;
    private ListView requestsView;
    private RequestUserListAdapter requestUserListAdapter;
    private HashMap<String, String> requests;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_requests);

        textInputs = (EditText) findViewById(R.id.requestName);
        searchUser = (Button) findViewById(R.id.searchUserRequest);
        refreshList = (Button) findViewById(R.id.refreshRequestList);
        requestsView = (ListView) findViewById(R.id.requestsView);

        ref = FirebaseDatabase.getInstance().getReference();
        friends = new ArrayList<User>();

        //wait for database fetch complete
        getCurrentUser(new RequestsUserListCallback() {
            @Override
            public void onRequestsCallback(User currentUser) {
                getRequests(currentUser, new FriendsUserListCallback(){
                    @Override
                    public void onFriendsCallback(ArrayList<User> requests){
                        requestUserListAdapter =
                                new RequestUserListAdapter(RequestActivity.this, R.layout.request_block,
                                        requests, currentUser, ref);

                        requestsView.setAdapter(requestUserListAdapter);

                    }

                });

            }
        });

    }



    public void getCurrentUser(RequestsUserListCallback requestsUserListCallback) {
        Query query = ref.child("User").orderByChild("id")
                .equalTo(FirebaseAuth.getInstance().getCurrentUser().getUid());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot datasnapshot) {
                for (DataSnapshot s : datasnapshot.getChildren()) {
                    if (s.exists()) {
                        this_device = s.getValue(User.class);
                        requestsUserListCallback.onRequestsCallback(this_device);
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError arg0) {

            }
        });
    }

    public void getRequests(User currentUser, FriendsUserListCallback friendsUserListCallback){
        getRequests(currentUser, friendsUserListCallback, null);
    }

    public void getRequests(User currentUser, FriendsUserListCallback friendsUserListCallback, String starter){
        ref.child("User").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){

                    friends = new ArrayList<User>();

                    for (DataSnapshot s : dataSnapshot.getChildren()) {
                        if(s.exists()){
                            User fd = s.getValue(User.class);

                            if (currentUser.getRequests() != null){
                                if(currentUser.getRequests().containsKey(fd.getId())){
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
                        Toast.makeText(RequestActivity.this,
                                "No result",
                                Toast.LENGTH_SHORT).show();
                    }

                   friendsUserListCallback.onFriendsCallback(friends);
                }
                else{
                    Toast.makeText(RequestActivity.this,
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
