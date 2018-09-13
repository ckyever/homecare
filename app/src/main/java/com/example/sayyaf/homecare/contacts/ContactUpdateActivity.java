package com.example.sayyaf.homecare.contacts;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.sayyaf.homecare.MainActivity;
import com.example.sayyaf.homecare.R;
import com.example.sayyaf.homecare.accounts.User;
import com.example.sayyaf.homecare.requests.RequestController;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

/**
 *  Activity to handle the sending of friend requests and the removal of friends
 **/
public class ContactUpdateActivity extends AppCompatActivity implements View.OnClickListener {

    private Button mAddUserButton;
    private EditText mUserEmail;
    private DatabaseReference ref;
    private FirebaseUser currentUserAuth;
    private User currentUser;
    private String uid;
    private Button mRemoveUserButton;
    public static final String TAG =ContactUpdateActivity.class.getSimpleName();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_update);
        mAddUserButton = (Button) findViewById(R.id.addContact);
        mUserEmail = (EditText) findViewById(R.id.contactEmail);
        mRemoveUserButton = (Button) findViewById(R.id.removeContact);

        currentUserAuth = FirebaseAuth.getInstance().getCurrentUser();
        ref = FirebaseDatabase.getInstance().getReference();
        uid = currentUserAuth.getUid();
        getCurrentUser();
        mAddUserButton.setOnClickListener(this);
        mRemoveUserButton.setOnClickListener(this);
    }


    @Override
    public void onClick(View view) {

        final String email = mUserEmail.getText().toString().trim();
        //Call the requisite protocol based on view clicked
        if(view == mAddUserButton) {
            addFriend(email);
        }

        else if(view == mRemoveUserButton) {
            removeFriend(email);
        }
    }

    //Returns the user currently signed into the app
    public void getCurrentUser() {
        //Finds the user in the realtime database based on ID stored in Firebase Authentication Service
        Query query = ref.child("User").orderByChild("id").equalTo(uid);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot datasnapshot) {

                for (DataSnapshot snapshot : datasnapshot.getChildren()) {
                    if (snapshot.exists()) {
                        currentUser = snapshot.getValue(User.class);

                        if (currentUser == null) {
                            Toast.makeText(ContactUpdateActivity.this, "NULL user",
                                    Toast.LENGTH_SHORT).show();
                            Toast.makeText(ContactUpdateActivity.this, "User doesn't exist",
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }

                                Log.d(TAG, "Current User id: " + currentUser.getId());
                                //Log.d(TAG, "SNAPSHOT : " + snapshot.getValue());
                                //Log.d(TAG, "USER: " + user.getId());
                        return;
                    }
                }

            }
            @Override
            public void onCancelled(DatabaseError arg0) {

            }
        });
    }


    public void removeFriend(String email) {
        if(isValidEmail(email)){
            Query query = ref.child("User").orderByChild("email").equalTo(email);
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot datasnapshot) {

                    for (DataSnapshot snapshot : datasnapshot.getChildren()) {
                        if (snapshot.exists()) {
                            User user = snapshot.getValue(User.class);

                            if (user == null ||
                                    currentUser.getChatDatabase() == null
                                    || !currentUser.getChatDatabase().containsKey(user.getId())) {
                                Toast.makeText(ContactUpdateActivity.this, "User doesn't exist",
                                        Toast.LENGTH_SHORT).show();
                                return;
                            }

                            if(currentUser.getRequestsSent() != null) {
                                if(currentUser.getRequestsSent().containsKey(user.getId())) {
                                    ref.child("User").child(uid)
                                            .child("requestsSent")
                                            .child(user.getId())
                                            .removeValue();

                                    ref.child("User").child(user.getId())
                                            .child("requests")
                                            .child(uid)
                                            .removeValue();
                                    return;
                                }
                            }

                            RequestController.removeUser(ref, user.getId(), uid);
                            backToMenu();
                            return;
                        }
                    }
                }
                @Override
                public void onCancelled(DatabaseError arg0) {

                }
            });
        }

        else {
            Toast.makeText(ContactUpdateActivity.this, "Invalid Email Entered",
                    Toast.LENGTH_SHORT).show();
        }
    }

    public void addFriend(String email) {
        if(isValidEmail(email)){
            Query query = ref.child("User").orderByChild("email").equalTo(email);
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot datasnapshot) {

                    for (DataSnapshot snapshot : datasnapshot.getChildren()) {
                        if (snapshot.exists()) {
                            User user = snapshot.getValue(User.class);

                            if (user == null || user.isCaregiver() == currentUser.isCaregiver()) {
                                Toast.makeText(ContactUpdateActivity.this, "User doesn't exist",
                                        Toast.LENGTH_SHORT).show();
                                return;
                            }

                            else if(currentUser.getFriends() != null
                                && currentUser.getFriends().containsKey(user.getId())) {
                                Toast.makeText(ContactUpdateActivity.this, "Already Friend",
                                        Toast.LENGTH_SHORT).show();
                                return;
                            }

                            else if(currentUser.getRequestsSent() != null
                                    && currentUser.getRequestsSent().containsKey(user.getId())) {
                                Toast.makeText(ContactUpdateActivity.this, "Request Already Sent",
                                        Toast.LENGTH_SHORT).show();
                                return;
                            }
                           
                            RequestController.addSentRequest(ref.child("User"), user.getId(), user.getEmail(), uid);
                            RequestController.addReceiverRequest(ref.child("User"), user.getId(), currentUser.getEmail(), uid);

                            Toast.makeText(ContactUpdateActivity.this, "Request Sent",
                                    Toast.LENGTH_SHORT).show();

                            backToMenu();

                            return;
                        }
                    }

                }
                @Override
                public void onCancelled(DatabaseError arg0) {

                }
            });
        } else {
            Toast.makeText(ContactUpdateActivity.this, "Invalid Email Entered",
                    Toast.LENGTH_SHORT).show();
        }

    }

    public static boolean isValidEmail(CharSequence target) {
        if (TextUtils.isEmpty(target)) {

            return false;
        } else {
            return android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
        }
    }


    private void backToMenu(){
        Intent goToMenu = new Intent(ContactUpdateActivity.this, MainActivity.class);
        goToMenu.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(goToMenu);
        finish();
    }

    @Override
    public void onBackPressed() {
        backToMenu();
    }

}
