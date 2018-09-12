package com.example.sayyaf.homecare.requests;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
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

public class RequestActivity extends AppCompatActivity implements RequestsUserListCallback,
    FriendsUserListCallback {

    private DatabaseReference ref;
    private User currentUser;

    private Button refreshRequestList;
    private ListView requestsView;
    private RequestUserListAdapter requestUserListAdapter;

    private ArrayList<User> friends;
    private HashMap<String, String> requests;
    private String uid;

    public static final String TAG = RequestActivity.class.getSimpleName();


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_requests);

        refreshRequestList = (Button) findViewById(R.id.refreshRequestList);
        requestsView = (ListView) findViewById(R.id.requestsView);

        ref = FirebaseDatabase.getInstance().getReference();
        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        friends = new ArrayList<User>();
        requests = new HashMap<>();

        getCurrentUser();
    }

        @Override
        public void onRequestsCallback(HashMap<String, String> requestsStored, String id) {
            getrequests(requestsStored);

        }

    @Override
    public void onFriendsCallback(ArrayList<User> requests){
        requestUserListAdapter =
                new RequestUserListAdapter(RequestActivity.this, R.layout.request_block,
                        requests, currentUser, ref);

        requestsView.setAdapter(requestUserListAdapter);

    }


    public void getCurrentUser() {
        Query query = ref.child("User")
                .orderByChild("id")
                .equalTo(uid);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot datasnapshot) {
                for (DataSnapshot s : datasnapshot.getChildren()) {
                    if (s.exists()) {
                        currentUser = s.getValue(User.class);
                        requests = currentUser.getRequests();
                        onRequestsCallback(currentUser.getRequests(),
                                currentUser.getId());
                    }

                }
            }
            @Override
            public void onCancelled(DatabaseError arg0) {

            }
        });
    }

    public void getrequests(HashMap<String, String> requests){
        ref.child("User").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    friends = new ArrayList<User>();
                    for (DataSnapshot s : dataSnapshot.getChildren()) {
                        if (s.exists()) {
                            User fd = s.getValue(User.class);
                            if (requests!=null && requests.containsKey(fd.getId())) {
                                friends.add(fd);
                            }
                        }
                    }
                    Log.d(TAG, "Friends before" +friends);
                    if(!friends.isEmpty()) {
                        onFriendsCallback(friends);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    

    @Override
    public void onBackPressed() {
        Intent goToMenu = new Intent(RequestActivity.this, MainActivity.class);
        goToMenu.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(goToMenu);
        finish();
    }



}
