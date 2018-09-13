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

        //Retrieve the entered email
        final String email = mUserEmail.getText().toString().trim();

        //Call the requisite protocol based on view clicked
        if(view == mAddUserButton) {
            addFriend(email);
        }
        else if(view == mRemoveUserButton) {
            removeFriend(email);
        }
    }

    //Assign User attributes of the user currently signed into the app
    public void getCurrentUser() {
        //Finds the current user in the realtime database based on ID stored in Firebase Authentication Service
        Query query = ref.child("User").orderByChild("id").equalTo(uid);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot datasnapshot) {

                for (DataSnapshot snapshot : datasnapshot.getChildren()) {

                    //If result exists, set the user attributes to the current user
                    if (snapshot.exists()) {
                        currentUser = snapshot.getValue(User.class);
                        Log.d(TAG, "Current User id: " + currentUser.getId());

                        if (currentUser == null) {
                            Toast.makeText(ContactUpdateActivity.this, "User doesn't exist",
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError arg0) {

            }
        });
    }


    /**
     * On removeUserButton clicked, removes either the sent friend request or removes the user from
     * the friends list depending on where the requested user exists (given that they exist)
     * @param email email of user to be removed
     */
    public void removeFriend(String email) {
        if(isValidEmail(email)){
            Query query = ref.child("User").orderByChild("email").equalTo(email);
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot datasnapshot) {

                    for (DataSnapshot snapshot : datasnapshot.getChildren()) {
                        if (snapshot.exists()) {
                            User user = snapshot.getValue(User.class);

                            /*Checks if user exists and if the current user had actually sent a
                            friend request or has any friends
                             */
                            if (user == null || (currentUser.getRequestsSent() == null &&
                                    (currentUser.getFriends() == null ||
                                            !currentUser.getFriends().containsKey(user.getId())))) {
                                Toast.makeText(ContactUpdateActivity.this, "Friend doesn't exist",
                                        Toast.LENGTH_SHORT).show();
                                return;
                            }
                            //Checks if current user sent a friend request to the found user
                            if(currentUser.getRequestsSent()!= null &&
                                    currentUser.getRequestsSent().containsKey(user.getId())) {

                                /* Removes the friend request for the receiver, and removes
                                request sent for the current user */
                                RequestController.removeRequest(ref, currentUser.getId(), user);
                                RequestController.removeSentRequest(ref, currentUser.getId(), user);

                                Toast.makeText(ContactUpdateActivity.this,
                                        "Friend request is removed",
                                        Toast.LENGTH_SHORT).show();
                            }

                            /* Removes the friend relationship between current user and
                                requested user
                             */
                            else if(currentUser.getFriends().containsKey(user.getId())) {
                                RequestController.removeUser(ref, currentUser, user);
                                Toast.makeText(ContactUpdateActivity.this,
                                        "Friend has been removed",
                                        Toast.LENGTH_SHORT).show();
                            }
                            refresh();
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

    /**
     * on addUserButton clicked, sends a friend request to the requested user
     * @param email email of user to send request to
     */
    public void addFriend(String email) {
        if(isValidEmail(email)){
            Query query = ref.child("User").orderByChild("email").equalTo(email);
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot datasnapshot) {

                    for (DataSnapshot snapshot : datasnapshot.getChildren()) {
                        if (snapshot.exists()) {
                            User user = snapshot.getValue(User.class);

                            /* ensures user has been found and the caregiver status of both users
                             is different
                              */
                            if (user == null || user.isCaregiver() == currentUser.isCaregiver()) {
                                Toast.makeText(ContactUpdateActivity.this, "User doesn't exist",
                                        Toast.LENGTH_SHORT).show();
                                return;
                            }

                            //Checks if users are already friends
                            else if(currentUser.getFriends() != null
                                && currentUser.getFriends().containsKey(user.getId())) {
                                Toast.makeText(ContactUpdateActivity.this, "Already Friend",
                                        Toast.LENGTH_SHORT).show();
                                return;
                            }

                            //Checks if a friend request has already been sent to the user
                            else if(currentUser.getRequestsSent() != null
                                    && currentUser.getRequestsSent().containsKey(user.getId())) {
                                Toast.makeText(ContactUpdateActivity.this, "Request Already Sent",
                                        Toast.LENGTH_SHORT).show();
                                return;
                            }
                            /* informs the current user about a sent request, and adds the
                                request to the receiver
                             */
                            RequestController.addSentRequest(ref, user.getId(), user.getEmail(), uid);
                            RequestController.addReceiverRequest(ref, user.getId(), currentUser.getEmail(), uid);

                            Toast.makeText(ContactUpdateActivity.this, "Request Sent",
                                    Toast.LENGTH_SHORT).show();

                            refresh();

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

    /**
     * Checks if provided email is of the right pattern to be considered a valid email
     * @param target characters to be checked
     * @return true if valid, else false
     */
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

    private void refresh(){
        Intent refreshCurrent = new Intent(ContactUpdateActivity.this, ContactUpdateActivity.class);
        refreshCurrent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(refreshCurrent);
        finish();
    }

    @Override
    public void onBackPressed() {
        backToMenu();
    }

}
