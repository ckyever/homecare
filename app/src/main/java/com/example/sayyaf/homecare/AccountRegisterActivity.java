package com.example.sayyaf.homecare;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import java.util.UUID;


public class AccountRegisterActivity extends AppCompatActivity implements View.OnClickListener {

    Button mCreateUserButton;
    EditText mNameEditText;
    EditText mEmailEditText;
    EditText mPasswordEditText;
    EditText mConfirmPasswordEditText;
    TextView mLoginTextView;
    public static final String TAG = AccountRegisterActivity.class.getSimpleName();
    private FirebaseAuth mAuth;
    String uniqueID = UUID.randomUUID().toString();
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

        private void validateFields() {

            if(mNameEditText.getText().length()>0 && mEmailEditText.getText().length()>0 &&
                    mPasswordEditText.getText().length()>0 &&
                    mConfirmPasswordEditText.getText().length()>0){
                mCreateUserButton.setEnabled(true);
            }else{
                mCreateUserButton.setEnabled(false);
            }
        }
    };

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
        mAuth = FirebaseAuth.getInstance();

        mLoginTextView.setOnClickListener(this);
        mCreateUserButton.setOnClickListener(this);

        mCreateUserButton.setEnabled(false);

        mNameEditText.addTextChangedListener(textWatcher);
        mEmailEditText.addTextChangedListener(textWatcher);
        mPasswordEditText.addTextChangedListener(textWatcher);
        mConfirmPasswordEditText.addTextChangedListener(textWatcher);
    }


    @Override
    public void onClick(View view) {

        if (view == mLoginTextView) {
            Intent intent = new Intent(AccountRegisterActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }

        if (view == mCreateUserButton) {
            createNewUser();
        }

    }


    private void createNewUser() {
        final String name = mNameEditText.getText().toString().trim();
        final String email = mEmailEditText.getText().toString().trim();
        String password = mPasswordEditText.getText().toString().trim();
        String confirmPassword = mConfirmPasswordEditText.getText().toString().trim();

        //if(password.equals(confirmPassword)) {
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(AccountRegisterActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                Log.d(TAG, "Authentication successful");
                            } else {
                                Toast.makeText(AccountRegisterActivity.this, "Authentication failed.",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
       // }
    }


}
