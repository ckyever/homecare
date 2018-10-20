package com.example.sayyaf.homecare.options;

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

import com.example.sayyaf.homecare.accounts.UserAppVersionController;
import com.example.sayyaf.homecare.notifications.EmergencyCallActivity;
import com.example.sayyaf.homecare.notifications.NetworkConnection;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Activity to allow users to update the email address registered to their account
 */

public class UpdateEmailActivity extends AppCompatActivity implements View.OnClickListener {

    private Button changeEmailButton;
    private EditText changeEmailText;
    private FirebaseAuth mAuth;
    private Button optionsMenu;
    private Button helpButton;
    public static final String TAG = UpdateEmailActivity.class.getSimpleName();
    private final DatabaseReference myRef= FirebaseDatabase.getInstance().getReference();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_email);
        changeEmailButton = (Button) findViewById(R.id.updateEmailButton);
        changeEmailText = (EditText) findViewById(R.id.newEmail);
        optionsMenu = (Button) findViewById(R.id.optionMenuUpdateEmail);
        helpButton = (Button) findViewById(R.id.optionHelpUpdateEmail);
        changeEmailButton.setOnClickListener(this);

        // activate help button on assisted person version
        UserAppVersionController.getUserAppVersionController().resetButton(helpButton);

    }


    @Override
    public void onClick(View v) {


        if(v == optionsMenu){
            Intent intent = new Intent(UpdateEmailActivity.this, OptionActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }

        // block actions those require internet connection
        if(!NetworkConnection.getConnection()){
            NetworkConnection.requestNetworkConnection(UpdateEmailActivity.this);
            return;
        }

        if(v == changeEmailButton) {
            updateEmail();
        }

        if(v == helpButton) {
        EmergencyCallActivity.setBackToActivity(UpdateEmailActivity.class);

        Intent intent = new Intent(UpdateEmailActivity.this, EmergencyCallActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
        }
    }

    /**
     * Attempts to update user email. Checks the entered email for correct format
     * followed by attempted account update.
     */
    public void updateEmail() {
        final String email = changeEmailText.getText().toString().trim();
        mAuth = FirebaseAuth.getInstance();

        //Ensures entered email address follows requisite email string pattern
        if(isValidEmail(email)) {
            //Calls the Firebase Authentication user email update method
            mAuth.getCurrentUser().updateEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    //If successfully updated in Firebase Authentication, update in Real Time Database
                    if (task.isSuccessful()) {
                        Log.d(TAG, "User email address updated.");
                        String userId = mAuth.getCurrentUser().getUid();
                        myRef.child("User").
                                child(userId).
                                child("email").
                                setValue(email);

                        Toast.makeText(UpdateEmailActivity.this,
                                "Email successfully updated", Toast.LENGTH_SHORT).show();
                    }

                    //Something went wrong in the update
                    else {
                        try {
                            if (task.getException() != null) {
                                throw task.getException();
                            }
                            else{
                                Toast.makeText(UpdateEmailActivity.this,
                                        "Email update unsuccessful  please try again", Toast.LENGTH_SHORT).show();
                            }

                         //Entered email registered to a user already
                        }catch(FirebaseAuthUserCollisionException e) {
                            Toast.makeText(UpdateEmailActivity.this, "Account already exists with this email address. " +
                                            "Please try again",
                                    Toast.LENGTH_SHORT).show();
                        } catch (Exception e) {
                            Toast.makeText(UpdateEmailActivity.this,
                                    "Email update unsuccessful  please try again", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });
        }

        else {
            Toast.makeText(UpdateEmailActivity.this,
                    "Email format incorrect, please try again", Toast.LENGTH_SHORT).show();
        }
    }


    /**
     * Method to test pattern of user entered email addresses
     * @param target target character sequence to be pattern matched
     * @return
     */
    public static boolean isValidEmail(CharSequence target) {
        return !TextUtils.isEmpty(target) &&
                android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }

    public void onBackPressed() {
        Intent intent = new Intent(UpdateEmailActivity.this, OptionActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }


}
