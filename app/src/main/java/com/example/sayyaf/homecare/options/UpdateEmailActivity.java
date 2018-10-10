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

import com.example.sayyaf.homecare.R;
import com.example.sayyaf.homecare.accounts.AccountRegisterActivity;
import com.example.sayyaf.homecare.accounts.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class UpdateEmailActivity extends AppCompatActivity implements View.OnClickListener {

    private Button changeEmailButton;
    private EditText changeEmailText;
    private FirebaseAuth mAuth;
    public static final String TAG = UpdateEmailActivity.class.getSimpleName();
    private final DatabaseReference myRef= FirebaseDatabase.getInstance().getReference();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_email);
        changeEmailButton = (Button) findViewById(R.id.updateEmail);
        changeEmailText = (EditText) findViewById(R.id.newEmail);
    }


    @Override
    public void onClick(View v) {

        if(v == changeEmailButton) {
            updateEmail();
        }
    }

    public void updateEmail() {
        final String email = changeEmailText.toString().trim();
        if(!isValidEmail(email)) {
            mAuth.getCurrentUser().updateEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "User email address updated.");
                        String userId = mAuth.getCurrentUser().getUid();
                        myRef.child("User").
                                child(userId).
                                child("email").
                                setValue(email);

                    }
                }
            });
        }
    }



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
