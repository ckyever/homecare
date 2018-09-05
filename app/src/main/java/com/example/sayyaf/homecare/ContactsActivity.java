package com.example.sayyaf.homecare;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.FirebaseError;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ContactsActivity extends AppCompatActivity implements View.OnClickListener {

    private Button mAddUserButton;
    private EditText mUserEmail;
    private DatabaseReference userRef;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private User user;
    private String uid;
    public static final String TAG =ContactsActivity.class.getSimpleName();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);
        mAddUserButton = (Button) findViewById(R.id.addUser);
        mUserEmail = (EditText) findViewById(R.id.contactEmail);
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        userRef = FirebaseDatabase.getInstance().getReference("User");
        String email = mUserEmail.getText().toString().trim();
        uid = currentUser.getUid();
        //User caregiverUser = caregiverRef.orderByChild("email").equalTo(email);

        mAddUserButton.setOnClickListener(this);
    }


    @Override
    public void onClick(View view) {

        final String email = mUserEmail.getText().toString().trim();
        if(view == mAddUserButton) {
            if(isValidEmail(email)){
                userRef.child("Users").orderByChild("email")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Log.i(TAG, snapshot.getValue().toString());
                        for(DataSnapshot shot : snapshot.getChildren()) {
                            if (shot.getValue() != null) {
                                User user = shot.getValue(User.class);
                                if(user!=null) {
                                    System.out.println(user.getName());
                                }
                                if(user.getEmail().equals(email))
                                //mUserEmail.setText("HELLO FRIENDS");
                                Toast.makeText(ContactsActivity.this, "YES CONTACT",
                                        Toast.LENGTH_SHORT).show();
                                return;
                            }

                        }
                        Toast.makeText(ContactsActivity.this, "NO CONTACT",
                                Toast.LENGTH_SHORT).show();
                    }
                    @Override
                    public void onCancelled(DatabaseError arg0) {

                    }
                });


                Toast.makeText(ContactsActivity.this, "Invalid Email",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    public final static boolean isValidEmail(CharSequence target) {
        if (TextUtils.isEmpty(target)) {
            return false;
        } else {
            return android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
        }
    }

}
