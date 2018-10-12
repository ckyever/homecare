package com.example.sayyaf.homecare.options;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.example.sayyaf.homecare.R;

public class UpdatePasswordActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_password);
    }


/*
    public void updatePassword() {
        FirebaseUser user;
        user = FirebaseAuth.getInstance().getCurrentUser();
        final String email = user.getEmail();
        AuthCredential credential = EmailAuthProvider.getCredential(email,oldpass);

        user.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    user.updatePassword(newPass).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(!task.isSuccessful()){
                                Toast.makeText(UpdatePasswordActivity.this,
                                        "Something went wrong. Please try again later", Toast.LENGTH_SHORT).show();
                            }else {
                                Toast.makeText(UpdatePasswordActivity.this,
                                        "Password Successfully Modified", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }else {

                    Toast.makeText(UpdatePasswordActivity.this,
                            "Authentication Failed", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    */
}
