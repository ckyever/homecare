package com.example.sayyaf.homecare.requests;

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
import com.example.sayyaf.homecare.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class RequestActivity extends AppCompatActivity implements View.OnClickListener {

    private DatabaseReference ref;
    private User currentUser;

    private EditText requestSearch;
    private Button searchUserRequest;
    private Button refreshRequestList;
    private ArrayList<User> requests;
    private ListView requestsView;
    private RequestUserListAdapter requestUserListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_requests);

        requestSearch = (EditText) findViewById(R.id.requestSearch);
        searchUserRequest = (Button) findViewById(R.id.searchUserRequest);
        refreshRequestList = (Button) findViewById(R.id.refreshRequestList);
        requestsView = (ListView) findViewById(R.id.requestsView);

        ref = FirebaseDatabase.getInstance().getReference();

        getCurrentUser();
        requests = new ArrayList<User>();

        //wait for database fetch complete
        getrequests(new RequestsUserListCallback(){
            @Override
            public void onCallback(ArrayList<User> requests){
                requestUserListAdapter =
                        new RequestUserListAdapter(RequestActivity.this, R.layout.request_block,
                                requests, currentUser, ref);

                requestsView.setAdapter(requestUserListAdapter);

            }
        });

    }

    @Override
    public void onClick(View v) {

        String starter = requestSearch.getText().toString().trim();

        if(v == requestSearch){
            //checkUser(email);
            if(starter == null || starter.isEmpty()) return;

            getrequests(new RequestsUserListCallback(){
                @Override
                public void onCallback(ArrayList<User> requests){

                    requestUserListAdapter =
                            new RequestUserListAdapter(RequestActivity.this, R.layout.request_block,
                                    requests, currentUser, ref);

                    requestsView.setAdapter(requestUserListAdapter);

                }
            }, starter);
        }

        if(v == refreshRequestList){
            getrequests(new RequestsUserListCallback(){
                @Override
                public void onCallback(ArrayList<User> requests){
                    requestUserListAdapter =
                            new RequestUserListAdapter(RequestActivity.this, R.layout.request_block,
                                    requests, currentUser, ref);

                    requestsView.setAdapter(requestUserListAdapter);

                }
            });
        }

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
            for(User u : requests) System.out.println(u.getName());
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
                        currentUser = s.getValue(User.class);
                    }
                }

            }
            @Override
            public void onCancelled(DatabaseError arg0) {

            }
        });
    }

    public void getrequests(RequestsUserListCallback requestsUserListCallback){
        getrequests(requestsUserListCallback, null);
    }

    public void getrequests(RequestsUserListCallback requestsUserListCallback, String starter){
        ref.child("User").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){

                    requests = new ArrayList<>();

                    for (DataSnapshot s : dataSnapshot.getChildren()) {
                        if(s.exists()){
                            User fd = s.getValue(User.class);

                            if (fd.getRequests() != null){
                                if(fd.getRequests().containsKey(currentUser.getId())){
                                    if(starter == null)
                                        requests.add(fd);
                                    else if(fd.getName().startsWith(starter)
                                            || fd.getEmail().startsWith(starter))
                                        requests.add(fd);
                                }
                            }
                        }

                    }

                    if(requests.isEmpty()){
                        Toast.makeText(RequestActivity.this,
                                "No result",
                                Toast.LENGTH_SHORT).show();
                    }

                    requestsUserListCallback.onCallback(requests);
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
    

    @Override
    public void onBackPressed() {
        Intent goToMenu = new Intent(RequestActivity.this, MainActivity.class);
        goToMenu.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(goToMenu);
        finish();
    }



}
