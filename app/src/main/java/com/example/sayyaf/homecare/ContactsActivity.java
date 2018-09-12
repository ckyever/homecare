package com.example.sayyaf.homecare;

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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Date;
import java.util.Random;

public class ContactsActivity extends AppCompatActivity implements View.OnClickListener {

    private Button mAddUserButton;
    private EditText mUserEmail;
    private DatabaseReference userRef;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUserAuth;
    private User currentUser;
    private String uid;
    private Button mRemoveUserButton;
    public static final String TAG =ContactsActivity.class.getSimpleName();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_update);
        mAddUserButton = (Button) findViewById(R.id.addContact);
        mUserEmail = (EditText) findViewById(R.id.contactEmail);
        mRemoveUserButton = (Button) findViewById(R.id.removeContact);

        currentUserAuth = FirebaseAuth.getInstance().getCurrentUser();
        userRef = FirebaseDatabase.getInstance().getReference("User");
        String email = mUserEmail.getText().toString().trim();
        uid = currentUserAuth.getUid();
        //User caregiverUser = caregiverRef.orderByChild("email").equalTo(email);
        getCurrentUser();
        mAddUserButton.setOnClickListener(this);
        mRemoveUserButton.setOnClickListener(this);
    }


    @Override
    public void onClick(View view) {

        final String email = mUserEmail.getText().toString().trim();
        if(view == mAddUserButton) {
            addFriend(email);
        }

        else if(view == mRemoveUserButton) {
            removeFriend(email);
        }
    }

    public void getCurrentUser() {
        Query query = userRef.orderByChild("id").equalTo(uid);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot datasnapshot) {

                for (DataSnapshot snapshot : datasnapshot.getChildren()) {
                    if (snapshot.exists()) {
                        currentUser = snapshot.getValue(User.class);

                        if (currentUser == null) {
                            Toast.makeText(ContactsActivity.this, "NULL user",
                                    Toast.LENGTH_SHORT).show();
                            Toast.makeText(ContactsActivity.this, "User doesn't exist",
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
            Query query = userRef.orderByChild("email").equalTo(email);
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot datasnapshot) {

                    for (DataSnapshot snapshot : datasnapshot.getChildren()) {
                        if (snapshot.exists()) {
                            User user = snapshot.getValue(User.class);

                            if (user == null ||
                                    (currentUser.getFriends() == null
                                    && currentUser.getRequestsSent() == null)
                                    || !currentUser.getFriends().containsKey(user.getId())) {
                                Toast.makeText(ContactsActivity.this, "User doesn't exist",
                                        Toast.LENGTH_SHORT).show();
                                return;
                            }

                            if(currentUser.getRequestsSent() != null) {
                                if(currentUser.getRequestsSent().containsKey(user.getId())) {
                                    userRef.child(uid)
                                            .child("requestsSent")
                                            .child(user.getId())
                                            .removeValue();

                                    userRef.child(user.getId())
                                            .child("requests")
                                            .child(uid)
                                            .removeValue();
                                }
                            }

                            else if(email.equals(currentUser.getEmail())) {
                                Toast.makeText(ContactsActivity.this, "Cannot remove request to yourself",
                                        Toast.LENGTH_SHORT).show();
                                return;
                            }

                            userRef.child(uid)
                                    .child("friends")
                                    .child(user.getId())
                                    .removeValue();

                            // remove the common database
                            FirebaseDatabase.getInstance()
                                    .getReference("chatDB")
                                    .child(user.getChatDatabase().get(uid))
                                    .removeValue();

                            userRef.child(uid)
                                    .child("chatDatabase")
                                    .child(user.getId())
                                    .removeValue();

                            userRef.child(user.getId())
                                    .child("chatDatabase")
                                    .child(uid)
                                    .removeValue();
                            //

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
            Toast.makeText(ContactsActivity.this, "Invalid Email Entered",
                    Toast.LENGTH_SHORT).show();
        }
    }

    public void addFriend(String email) {
        if(isValidEmail(email)){
            Query query = userRef.orderByChild("email").equalTo(email);
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot datasnapshot) {

                    for (DataSnapshot snapshot : datasnapshot.getChildren()) {
                        if (snapshot.exists()) {
                            User user = snapshot.getValue(User.class);

                            if (user == null) {
                                Toast.makeText(ContactsActivity.this, "User doesn't exist",
                                        Toast.LENGTH_SHORT).show();
                                return;
                            }

                            if(currentUser.getFriends() != null
                                && currentUser.getFriends().containsKey(user.getId())) {
                                Toast.makeText(ContactsActivity.this, "Already Friend",
                                        Toast.LENGTH_SHORT).show();
                                return;
                            }

                            else if(currentUser.getRequestsSent() != null
                                    && currentUser.getRequestsSent().containsKey(user.getId())) {
                                Toast.makeText(ContactsActivity.this, "Request Already Sent",
                                        Toast.LENGTH_SHORT).show();
                                return;
                            }

                            else if(currentUser.getRequests() != null
                                    && currentUser.getRequests().containsKey(user.getId())) {
                                Toast.makeText(ContactsActivity.this, "User has sent you a pending request",
                                        Toast.LENGTH_SHORT).show();
                                return;
                            }

                            else if(email.equals(currentUser.getEmail())) {
                                Toast.makeText(ContactsActivity.this, "Cannot send a friend request to yourself",
                                        Toast.LENGTH_SHORT).show();
                                return;
                            }

                            userRef.child(uid).child("requestsSent").push();
                            userRef.child(uid)
                                    .child("requestsSent")
                                    .child(user.getId())
                                    .setValue(user.getEmail());

                            userRef.child(user.getId())
                                    .child("requests")
                                    .push();

                            userRef.child(user.getId())
                                    .child("requests")
                                    .child(uid)
                                    .setValue(currentUser.getEmail());


                            Toast.makeText(ContactsActivity.this, "Request Sent",
                                    Toast.LENGTH_SHORT).show();

                            return;
                        }
                    }
                }
                @Override
                public void onCancelled(DatabaseError arg0) {

                }
            });
        } else {
            Toast.makeText(ContactsActivity.this, "Invalid Email Entered",
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

    @Override
    public void onBackPressed() {
        Intent goToMenu = new Intent(ContactsActivity.this, MainActivity.class);
        goToMenu.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(goToMenu);
        finish();
    }

}
