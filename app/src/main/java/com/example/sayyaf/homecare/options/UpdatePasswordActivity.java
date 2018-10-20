package com.example.sayyaf.homecare.options;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.sayyaf.homecare.MainActivity;
import com.example.sayyaf.homecare.R;
import com.example.sayyaf.homecare.accounts.UserAppVersionController;
import com.example.sayyaf.homecare.notifications.EmergencyCallActivity;
import com.example.sayyaf.homecare.notifications.NetworkConnection;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class UpdatePasswordActivity extends AppCompatActivity implements View.OnClickListener{

    private Button changePasswordButton;
    private EditText changePasswordText;
    private EditText confirmPasswordText;
    private EditText currentPasswordText;

    private FirebaseAuth mAuth;
    private Button optionsMenu;
    private Button helpButton;
    public static final String TAG = UpdateEmailActivity.class.getSimpleName();
    private final DatabaseReference myRef= FirebaseDatabase.getInstance().getReference();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_password);
        changePasswordButton = (Button) findViewById(R.id.updatePasswordButton);
        changePasswordText = (EditText) findViewById(R.id.newPasswordUpdate);
        currentPasswordText = (EditText) findViewById(R.id.currentPasswordUpdate);

        confirmPasswordText = (EditText) findViewById(R.id.confirmPasswordUpdate);
        optionsMenu = (Button) findViewById(R.id.optionMenuUpdatePassword);
        helpButton = (Button) findViewById(R.id.optionHelpUpdatePassword);
        changePasswordButton.setOnClickListener(this);

        // activate help button on assisted person version
        UserAppVersionController.getUserAppVersionController().resetButton(helpButton);
    }

    @Override
    public void onClick(View v) {

        if(v == optionsMenu){
            Intent intent = new Intent(UpdatePasswordActivity.this, OptionActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }

        // block actions those require internet connection
        if(!NetworkConnection.getConnection()){
            NetworkConnection.requestNetworkConnection(UpdatePasswordActivity.this);
            return;
        }

        if(v == changePasswordButton) {
            updatePassword();
        }

        if(v == helpButton) {
            EmergencyCallActivity.setBackToActivity(UpdatePasswordActivity.class);

            Intent intent = new Intent(UpdatePasswordActivity.this, EmergencyCallActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
    }



    public void updatePassword() {

        final String oldPass = currentPasswordText.getText().toString().trim();
        final String newPass = changePasswordText.getText().toString().trim();
        final String confirmPass = confirmPasswordText.getText().toString().trim();

        FirebaseUser user;
        user = FirebaseAuth.getInstance().getCurrentUser();
        final String email = user.getEmail();

        if(user.getEmail() == null) {
            Toast.makeText(UpdatePasswordActivity.this,
                    "Something went wrong. Please re-sign in and try again", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if(newPass.equals(oldPass)) {
            Toast.makeText(UpdatePasswordActivity.this,
                    "New password cannot be equal to old password. Please try again", Toast.LENGTH_SHORT).show();
            return;
        }

        if(!newPass.equals(confirmPass)) {
            Toast.makeText(UpdatePasswordActivity.this,
                    "Entered passwords do not match. Please try again", Toast.LENGTH_SHORT).show();
            return;
        }

        if(newPass.length() < 6) {
            Toast.makeText(UpdatePasswordActivity.this,
                    "New password is too short. Passwords " +
                            "must be at least 6 characters. Please " +
                            "try again", Toast.LENGTH_SHORT).show();
            return;
        }

        AuthCredential credential = EmailAuthProvider.getCredential(email,oldPass);

        user.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    user.updatePassword(newPass).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (!task.isSuccessful()) {
                                try {

                                    if (task.getException() != null) {
                                        throw task.getException();
                                    } else {
                                        Toast.makeText(UpdatePasswordActivity.this,
                                                "Something went wrong. Please try again later", Toast.LENGTH_SHORT).show();
                                    }
                                } catch (FirebaseAuthWeakPasswordException e) {
                                    Toast.makeText(UpdatePasswordActivity.this, "Password length is too short",
                                            Toast.LENGTH_SHORT).show();
                                } catch (Exception e) {
                                    Toast.makeText(UpdatePasswordActivity.this,
                                            "Something went wrong. Please try again later", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(UpdatePasswordActivity.this,
                                        "Password Successfully Modified", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }else {

                    Toast.makeText(UpdatePasswordActivity.this,
                            "Authentication Failed, incorrect password entered. " +
                                    "Please try again", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void onBackPressed() {
        Intent intent = new Intent(UpdatePasswordActivity.this, OptionActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

}
