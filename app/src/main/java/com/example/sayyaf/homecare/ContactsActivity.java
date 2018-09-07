package com.example.sayyaf.homecare;

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
        mAddUserButton = (Button) findViewById(R.id.addUser);
        mUserEmail = (EditText) findViewById(R.id.contactEmail);
        mRemoveUserButton = (Button) findViewById(R.id.removeContact);

        currentUserAuth = FirebaseAuth.getInstance().getCurrentUser();
        userRef = FirebaseDatabase.getInstance().getReference("User");
        String email = mUserEmail.getText().toString().trim();
        uid = currentUserAuth.getUid();
        //User caregiverUser = caregiverRef.orderByChild("email").equalTo(email);
        getCurrentUser();
        mAddUserButton.setOnClickListener(this);
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
                            return;
                        }

                                Log.d(TAG, "Current User id: " + currentUser.getId());
                                //Log.d(TAG, "SNAPSHOT : " + snapshot.getValue());
                                //Log.d(TAG, "USER: " + user.getId());
                    }
                }

                Toast.makeText(ContactsActivity.this, "User doesn't exist",
                        Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onCancelled(DatabaseError arg0) {

            }
        });
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

    public void removeFriend(String email) {
        if(isValidEmail(email)){
            Query query = userRef.orderByChild("email").equalTo(email);
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot datasnapshot) {

                    for (DataSnapshot snapshot : datasnapshot.getChildren()) {
                        if (snapshot.exists()) {
                            User user = snapshot.getValue(User.class);

                            if (user == null) {
                                Toast.makeText(ContactsActivity.this, "NULL user",
                                        Toast.LENGTH_SHORT).show();
                                continue;
                            }


                            userRef.child(uid)
                                    .child("friends")
                                    .child(user.getId())
                                    .removeValue();

                            return;
                        }
                    }

                    Toast.makeText(ContactsActivity.this, "User doesn't exist",
                            Toast.LENGTH_SHORT).show();
                }
                @Override
                public void onCancelled(DatabaseError arg0) {

                }
            });
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
                                Toast.makeText(ContactsActivity.this, "NULL user",
                                        Toast.LENGTH_SHORT).show();
                                continue;
                            }

                            if(user.isCaregiver() == currentUser.isCaregiver()) {
                                continue;
                            }

                            if(currentUser.getFriends().containsKey(user.getId())) {
                                Toast.makeText(ContactsActivity.this, "Already Friend",
                                        Toast.LENGTH_SHORT).show();
                            }

                            userRef.child(uid).child("friends").push();

                            userRef.child(uid)
                                    .child("friends")
                                    .child(user.getId())
                                    .setValue(user.getEmail());

                            return;
                        }
                    }

                    Toast.makeText(ContactsActivity.this, "User doesn't exist",
                            Toast.LENGTH_SHORT).show();
                }
                @Override
                public void onCancelled(DatabaseError arg0) {

                }
            });
        }
    }

    public static boolean isValidEmail(CharSequence target) {
        if (TextUtils.isEmpty(target)) {
            return false;
        } else {
            return android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
        }
    }

}
