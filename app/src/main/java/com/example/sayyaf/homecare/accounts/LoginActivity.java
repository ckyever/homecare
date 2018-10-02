package com.example.sayyaf.homecare.accounts;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sayyaf.homecare.MainActivity;
import com.example.sayyaf.homecare.R;
import com.example.sayyaf.homecare.notifications.NotificationService;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

/** Class for handling user login to app
 */
public class LoginActivity  extends AppCompatActivity implements View.OnClickListener{
    TextView mRegisterTextView;
    EditText mEmailEditText;
    EditText mPasswordEditText;
    Button mLoginButton;
    private FirebaseAuth mAuth;

    private ProgressBar progressBar;
    private TextView progressBarMsg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mRegisterTextView = (TextView) findViewById(R.id.registerTextView);
        mEmailEditText = (EditText) findViewById(R.id.emailEditText);
        mPasswordEditText = (EditText) findViewById(R.id.passwordEditText);
        mLoginButton = (Button) findViewById(R.id.loginButton);
        mAuth = FirebaseAuth.getInstance();

        mLoginButton.setEnabled(false);
        mRegisterTextView.setOnClickListener(this);
        mLoginButton.setOnClickListener(this);

        mEmailEditText.addTextChangedListener(textWatcher);
        mPasswordEditText.addTextChangedListener(textWatcher);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBarMsg = (TextView) findViewById(R.id.progressBarMsg);

    }

    //Asynchronous method which watches for changes to EditText View
    TextWatcher textWatcher = new TextWatcher() {

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count,
                                      int after) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            validateFields();
        }

    };

    //Checks that a user has entered values for both username and password
    private void validateFields() {

        if(mEmailEditText.getText().length() > 0 && mPasswordEditText.getText().length() > 0) {
            mLoginButton.setEnabled(true);
        }
        else {
            mLoginButton.setEnabled(false);
        }
    }

    @Override
    public void onClick(View view) {

        //Redirects user to the account registration activity
        if(view == mRegisterTextView) {
            Intent intent = new Intent(LoginActivity.this, AccountRegisterActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }

        //Authenticates the given user details in preparation for signin
        else {
            authenticateUser();
        }
    }

    /** Method exists to authenticate the user details. If authentication successful, user is
     * signed in
     */
    private void authenticateUser() {
        String email = mEmailEditText.getText().toString();
        String password = mPasswordEditText.getText().toString();

        showProgress();

        //Firebase Authentication service use to check for successful sign in
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(this, new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult auth) {
                        // Sign in success, update UI with the signed-in user's information
                        FirebaseUser user = mAuth.getCurrentUser();

                        Query userTypeRef = FirebaseDatabase.getInstance().getReference("User")
                                .child(user.getUid())
                                .child("caregiver");

                        userTypeRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if(dataSnapshot.exists()){

                                    // set up user info for app version controll
                                    UserAppVersionController
                                            .getUserAppVersionController()
                                            .setUser(user.getUid(), dataSnapshot.getValue(Boolean.class));

                                    endProgress();

                                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);

                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);
                                    finish();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });


                    }
                }).addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(LoginActivity.this, "Emaill Address or Password is Incorrect",
                        Toast.LENGTH_SHORT).show();

                endProgress();
            }
        });

    }

    // show either auth progress
    private void showProgress(){
        progressBar.setVisibility(View.VISIBLE);
        progressBarMsg.setVisibility(View.VISIBLE);
    }

    // remove progress bar after finish
    private void endProgress(){
        progressBar.setVisibility(View.GONE);
        progressBarMsg.setVisibility(View.GONE);
    }

    public void onBackPressed() {
        Intent intent = new Intent(LoginActivity.this, AccountRegisterActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}