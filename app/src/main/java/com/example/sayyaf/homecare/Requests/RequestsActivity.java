package com.example.sayyaf.homecare.Requests;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.example.sayyaf.homecare.ContactUserListAdapter;
import com.example.sayyaf.homecare.ContactUserListCallback;
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

public class RequestsActivity extends AppCompatActivity implements View.OnClickListener {

    private DatabaseReference ref;
    private User this_device;

    private EditText textInputs;
    private Button searchUser;
    private Button refreshList;
    private ArrayList<User> requests;
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
        requests = new ArrayList<User>();

        //wait for database fetch complete
        getrequests(new ContactUserListCallback(){
            @Override
            public void onCallback(ArrayList<User> requests){
                contactUserListAdapter =
                        new ContactUserListAdapter(RequestsActivity.this, R.layout.contact_block,
                                requests, this_device, ref);

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

            getrequests(new ContactUserListCallback(){
                @Override
                public void onCallback(ArrayList<User> requests){

                    contactUserListAdapter =
                            new ContactUserListAdapter(RequestsActivity.this, R.layout.contact_block,
                                    requests, this_device, ref);

                    contactView.setAdapter(contactUserListAdapter);

                }
            }, starter);
        }

        if(v == refreshList){
            getrequests(new ContactUserListCallback(){
                @Override
                public void onCallback(ArrayList<User> requests){
                    contactUserListAdapter =
                            new ContactUserListAdapter(RequestsActivity.this, R.layout.contact_block,
                                    requests, this_device, ref);

                    contactView.setAdapter(contactUserListAdapter);

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
                        this_device = s.getValue(User.class);
                    }
                }

            }
            @Override
            public void onCancelled(DatabaseError arg0) {

            }
        });
    }

    public void getrequests(ContactUserListCallback contactUserListCallback){
        getrequests(contactUserListCallback, null);
    }

    public void getrequests(ContactUserListCallback contactUserListCallback, String starter){
        ref.child("User").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){

                    requests = new ArrayList<User>();

                    for (DataSnapshot s : dataSnapshot.getChildren()) {
                        if(s.exists()){
                            User fd = s.getValue(User.class);

                            if (fd.getChatDatabase() != null){
                                if(fd.getChatDatabase().containsKey(this_device.getId())){
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
                        Toast.makeText(RequestsActivity.this,
                                "No result",
                                Toast.LENGTH_SHORT).show();
                    }

                    contactUserListCallback.onCallback(requests);
                }
                else{
                    Toast.makeText(RequestsActivity.this,
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
        Intent goToMenu = new Intent(RequestsActivity.this, MainActivity.class);
        goToMenu.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(goToMenu);
        finish();
    }
}
