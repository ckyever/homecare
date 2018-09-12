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

public class RequestActivity extends AppCompatActivity {

    private DatabaseReference ref;
    private User currentUser;

    private Button refreshRequestList;
    private ArrayList<User> friends;
    private HashMap<String, String> requests;
    private ListView requestsView;
    private RequestUserListAdapter requestUserListAdapter;
    private String uid;
    public static final String TAG = RequestActivity.class.getSimpleName();


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_requests);

        refreshRequestList = (Button) findViewById(R.id.refreshRequestList);
        requestsView = (ListView) findViewById(R.id.requestsView);

        ref = FirebaseDatabase.getInstance().getReference();
        friends = new ArrayList<User>();
        requests = new HashMap<>();

        getCurrentUser(new RequestsUserListCallback() {
            @Override
            public void onRequestsCallback(HashMap<String, String> requestsStored, String id) {
                requests = requestsStored;
                uid = id;
                getrequests(requests, new FriendsUserListCallback(){
                    @Override
                    public void onFriendsCallback(ArrayList<User> requests){
                        requestUserListAdapter =
                                new RequestUserListAdapter(RequestActivity.this, R.layout.request_block,
                                        requests, currentUser, ref);

                        requestsView.setAdapter(requestUserListAdapter);
<<<<<<< HEAD
                    }
                });
            }
        });

        //wait for database fetch complete
    }


    // debug getting data
    public void showrequests(){
        if(requests == null){
            System.out.println("null");
        }
        else if(requests.isEmpty()) {
            System.out.println("empty");
        }
        else{
            for(User u : friends) System.out.println(u.getName());
        }
    }

=======

                    }

                });

            }
        });
        Log.d(TAG, "Requests after" + requests);

        //wait for database fetch complete
    }


    // debug getting data
    public void showrequests(){
        if(requests == null){
            System.out.println("null");
        }
        else if(requests.isEmpty()) {
            System.out.println("empty");
        }
        else{
            for(User u : friends) System.out.println(u.getName());
        }
    }

>>>>>>> parent of 19dff13... Attempted bug fixing of requestuserlistadapter
    public void getCurrentUser(RequestsUserListCallback requestsUserListCallback) {
        Query query = ref.child("User").orderByChild("id")
                .equalTo(FirebaseAuth.getInstance().getCurrentUser().getUid());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot datasnapshot) {
                for (DataSnapshot s : datasnapshot.getChildren()) {
                    if (s.exists()) {
                        currentUser = s.getValue(User.class);
                        uid = currentUser.getId();
                        requests = currentUser.getRequests();
                        Log.d(TAG, "Requests before" + requests);
                        requestsUserListCallback.onRequestsCallback(currentUser.getRequests(),
                                currentUser.getId());
                    }

                }

            }
            @Override
            public void onCancelled(DatabaseError arg0) {

            }
        });
    }

    public void getrequests(HashMap<String, String> requests, FriendsUserListCallback requestsUserListCallback){
        getrequests(requests, requestsUserListCallback, null);
    }

    public void getrequests(HashMap<String, String> requests, FriendsUserListCallback requestsUserListCallback, String starter){
        ref.child("User").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    friends = new ArrayList<User>();
                    for (DataSnapshot s : dataSnapshot.getChildren()) {
                        if (s.exists()) {
                            User fd = s.getValue(User.class);
<<<<<<< HEAD
                            if (requests!=null && requests.containsKey(fd.getId())) {
=======
                            if (requests.containsKey(fd.getId())) {
                                if (starter == null) {
                                    friends.add(fd);
                                } else if (fd.getName().startsWith(starter)
                                        || fd.getEmail().startsWith(starter))
>>>>>>> parent of 19dff13... Attempted bug fixing of requestuserlistadapter
                                    friends.add(fd);
                            }
                        }
                    }

<<<<<<< HEAD
                    if (!friends.isEmpty()) {
                        requestsUserListCallback.onFriendsCallback(friends);
=======
                    if (friends.isEmpty()) {
                        Toast.makeText(RequestActivity.this,
                                "No result",
                                Toast.LENGTH_SHORT).show();
>>>>>>> parent of 19dff13... Attempted bug fixing of requestuserlistadapter
                    }
                    requestsUserListCallback.onFriendsCallback(friends);
                } else {
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


    @Override
    public void onBackPressed() {
        Intent goToMenu = new Intent(RequestActivity.this, MainActivity.class);
        goToMenu.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(goToMenu);
        finish();
    }



}