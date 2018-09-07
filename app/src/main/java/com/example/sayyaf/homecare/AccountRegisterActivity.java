package com.example.sayyaf.homecare;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.UUID;


/** Activity controller for the Account Registration Activity. Handles validation of sign up inputs
 * and registration to the Firebase Authentication Service and the Firebase Real Time Database
 *
 */
public class AccountRegisterActivity extends AppCompatActivity implements View.OnClickListener {

    Button mCreateUserButton;
    EditText mNameEditText;
    EditText mEmailEditText;
    EditText mPasswordEditText;
    EditText mConfirmPasswordEditText;
    TextView mLoginTextView;
    RadioButton mCaregiver;
    RadioButton mAssistedPerson;
    private FirebaseUser fbUser =null;
    public static final String TAG = AccountRegisterActivity.class.getSimpleName();
    private FirebaseAuth mAuth;

    /* Textwatcher works off asynchronous calls, tracking when the input fields are changed
        to check if they meet necessary sign up validation */
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

    /* Sets standards for minimum requirements  before the sign up button becomes available to user */
    private void validateFields() {

        if(mNameEditText.getText().length()>0 && mEmailEditText.getText().length()>0 &&
                mPasswordEditText.getText().length()>=6 &&
                mConfirmPasswordEditText.getText().length()>0 && (mCaregiver.isChecked() ||
                mAssistedPerson.isChecked())){
            mCreateUserButton.setEnabled(true);
        }else{
            mCreateUserButton.setEnabled(false);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mCreateUserButton = (Button) findViewById(R.id.createUserButton);
        mNameEditText = (EditText) findViewById(R.id.nameEditText);
        mEmailEditText = (EditText)findViewById(R.id.emailEditText);
        mPasswordEditText = (EditText)findViewById(R.id.passwordEditText);
        mConfirmPasswordEditText = (EditText)findViewById(R.id.confirmPasswordEditText);
        mLoginTextView = (TextView) findViewById(R.id.loginTextView);
        mCaregiver = (RadioButton) findViewById(R.id.caregiver);
        mAssistedPerson = (RadioButton) findViewById(R.id.assistedPerson);
        mAuth = FirebaseAuth.getInstance();

        mLoginTextView.setOnClickListener(this);
        mCreateUserButton.setOnClickListener(this);
        mAssistedPerson.setOnClickListener(
                new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validateFields();
            }
        });

        mCaregiver.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        validateFields();
                    }
                });

        mCreateUserButton.setEnabled(false);

        mNameEditText.addTextChangedListener(textWatcher);
        mEmailEditText.addTextChangedListener(textWatcher);
        mPasswordEditText.addTextChangedListener(textWatcher);
        mConfirmPasswordEditText.addTextChangedListener(textWatcher);
    }

    /* Handles activity changes based on view clicked */
    @Override
    public void onClick(View view) {

        Intent intent = new Intent(AccountRegisterActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        if (view == mLoginTextView) {
            startActivity(intent);
            finish();
        }

        if (view == mCreateUserButton) {
            if(createNewUser()) {
                startActivity(intent);
                finish();
            }
        }

    }


    /* Method responsible for creating a new user. Only gets called upon basic validation
    * of all input fields. Then checks for valid email addresses, before attempting to
    * register the user with the Firebase Authentication Service. If that is successful,
    * links the user to the Real time database*/
    private boolean createNewUser() {
        final String name = mNameEditText.getText().toString().trim();
        final String email = mEmailEditText.getText().toString().trim();
        String password = mPasswordEditText.getText().toString().trim();
        String confirmPassword = mConfirmPasswordEditText.getText().toString().trim();
        //Flag to check if user is caregiver or assisted person
        final boolean isCaregiver = mCaregiver.isChecked();
        final DatabaseReference  myRef;
        final User newUser;

        /*Creates the specifiec user instance */
        if(isCaregiver) {
            newUser = new Caregiver(name, email, isCaregiver);
            myRef= FirebaseDatabase.getInstance().getReference("caregiver");
        }

        else {
            newUser = new Assisted(name, email, isCaregiver);
            myRef = FirebaseDatabase.getInstance().getReference("Assisted");

        }

        /* Checks password and confirm password match */
        if(!password.equals(confirmPassword)) {
            Toast.makeText(AccountRegisterActivity.this, "Password and confirm password don't match",
                    Toast.LENGTH_SHORT).show();
            return false;
        }

        /* Checks that the email address given follows the requisite pattern */
        if(!isValidEmail(email)) {
            Toast.makeText(AccountRegisterActivity.this, "Email is invalid",
                    Toast.LENGTH_SHORT).show();
            return false;
        }

        /* If email is valid, adds user to the Authentication service. If that succeeds, adds them
            to the Real Time database
         */
        if(isValidEmail(email)) {
            mAuth.createUserWithEmailAndPassword(email, password).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                        @Override
                        public void onSuccess(AuthResult auth) {
                                Log.d(TAG, "Authentication successful");
                                fbUser =FirebaseAuth.getInstance().getCurrentUser();
                                String userId = fbUser.getUid();
                                myRef.child(userId);
                                myRef.child(userId).setValue(newUser);

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(AccountRegisterActivity.this, "Authentication failed. Please try again later",
                            Toast.LENGTH_SHORT).show();
                }
            });

        }
        return true;
    }

    public final static boolean isValidEmail(CharSequence target) {
        if (TextUtils.isEmpty(target)) {
            return false;
        } else {
            return android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
        }
    }


    boolean isValidPassword(String password, String confirm) {

        if(password.length() >=6 && password.equals(confirm)) {
            return true;
        }
        return false;
    }


}
