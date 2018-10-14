package com.example.sayyaf.homecare.requests;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.example.sayyaf.homecare.MainActivity;
import com.example.sayyaf.homecare.R;
import com.example.sayyaf.homecare.accounts.User;
import com.example.sayyaf.homecare.accounts.UserAppVersionController;
import com.example.sayyaf.homecare.contacts.ContactChatActivity;
import com.example.sayyaf.homecare.notifications.EmergencyCallActivity;
import com.example.sayyaf.homecare.notifications.NetworkConnection;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


/** Class for handling the acceptance or rejection of received friend requests
 */
public class RequestActivity extends AppCompatActivity implements RequestsUserListCallback, View.OnClickListener {

    private DatabaseReference ref;
    private User currentUser;

    private ArrayList<User> friends;
    private ListView requestsView;
    private RequestUserListAdapter requestUserListAdapter;

    private Button helpButton;
    private Button homeButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_requests);
        requestsView = (ListView) findViewById(R.id.requestsView);

        helpButton = (Button) findViewById(R.id.optionHelp);

        homeButton = (Button) findViewById(R.id.optionMenu);

        // activate help button on assisted person version
        UserAppVersionController.getUserAppVersionController().resetButton(helpButton);

        ref = FirebaseDatabase.getInstance().getReference();
        friends = new ArrayList<>();
        getCurrentUser();

    }

    @Override
    public void onRequestsCallback(User currentUser) {
        getRequests(currentUser);
    }

    /** If requests found, send the list of senders into a list adapter to be readied for
     * use by the ListView
     * @param requests a list of users who have sent requests to the current user
     * @param currentUser the user currently signed in
     */
    @Override
    public void onFriendsCallback(ArrayList<User> requests, User currentUser){
        requestUserListAdapter =
                new RequestUserListAdapter(RequestActivity.this, R.layout.request_block,
                        requests, currentUser, ref);
        requestsView.setAdapter(requestUserListAdapter);

    }

    //Assign User attributes of the user currently signed into the app
    public void getCurrentUser() {
        Query query = ref.child("User").orderByChild("id")
                .equalTo(FirebaseAuth.getInstance().getCurrentUser().getUid());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot datasnapshot) {
                for (DataSnapshot s : datasnapshot.getChildren()) {
                    if (s.exists()) {
                        currentUser = s.getValue(User.class);
                        onRequestsCallback(currentUser);
                    }
                }
            }


            @Override
            public void onCancelled(DatabaseError arg0) {
            }
        });
    }

    /**
     * Get a list of the users who have sent friend requests to the current user
     * @param currentUser the user currently signed into the app
     */
    public void getRequests(User currentUser){
        ref.child("User").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){

                    //Arraylist to store the request senders
                    friends = new ArrayList<>();
                    for (DataSnapshot s : dataSnapshot.getChildren()) {
                        if(s.exists()){
                            User fd = s.getValue(User.class);

                            /*Check if the user has requests, and the user fd from database
                            sent a request
                             */
                            if (currentUser.getRequests() != null &&
                                    currentUser.getRequests().containsKey(fd.getId())) {
                                    friends.add(fd);
                            }
                        }

                    }

                    /* If there are any requests found, create a callback */
                    if(!friends.isEmpty()){
                        onFriendsCallback(friends, currentUser);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onClick(View view) {

        if(view == homeButton){
            goToMenu();
        }

        // block actions those require internet connection
        if(!NetworkConnection.getConnection()){
            NetworkConnection.requestNetworkConnection(RequestActivity.this);
            return;
        }

        if(view == helpButton){
            EmergencyCallActivity.setBackToActivity(RequestActivity.class);

            Intent intent = new Intent(RequestActivity.this, EmergencyCallActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        goToMenu();
    }

    private void goToMenu(){
        // back to menu page
        Intent goToMenu = new Intent(RequestActivity.this, MainActivity.class);
        goToMenu.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(goToMenu);
        finish();
    }
}
