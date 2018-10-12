package com.example.sayyaf.homecare.options;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sayyaf.homecare.MainActivity;
import com.example.sayyaf.homecare.R;
import com.example.sayyaf.homecare.accounts.UserAppVersionController;
import com.example.sayyaf.homecare.notifications.EmergencyCallActivity;
import com.example.sayyaf.homecare.notifications.NetworkConnection;

public class OptionActivity extends AppCompatActivity implements View.OnClickListener {

    private FloatingActionButton changeImageButton;
    private TextView changeImageText;
    private FloatingActionButton changeEmailButton;
    private TextView changeEmailText;
    private FloatingActionButton changePasswordButton;
    private TextView changePasswordText;

    private Button helpButton;
    private Button homeButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_options);

        changeImageButton = (FloatingActionButton) findViewById(R.id.changeImageButton);
        changeImageText = (TextView) findViewById(R.id.changeImage);
        changeEmailButton = (FloatingActionButton) findViewById(R.id.changeEmailButton);
        changeEmailText = (TextView) findViewById(R.id.changeEmail);
        changePasswordButton = (FloatingActionButton) findViewById(R.id.changePasswordButton);
        //changePasswordText = (TextView) findViewById(R.id.updatePassword);

        helpButton = (Button) findViewById(R.id.optionHelp);
        homeButton = (Button) findViewById(R.id.optionMenu);

        // activate help button on assisted person version
        UserAppVersionController.getUserAppVersionController().resetButton(helpButton);
    }

    @Override
    public void onClick(View v) {

        if(v == homeButton){
            goToMenu();
        }

        if(!NetworkConnection.getConnection()){
            NetworkConnection.requestNetworkConnection(OptionActivity.this);
            return;
        }

        // go to image upload
        if(v == changeImageButton || v == changeImageText){
            if(ProfileImageActivity.isUploading()){
                Toast.makeText(OptionActivity.this,
                        "Wait for upload complete", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent goToProfileImage = new Intent(OptionActivity.this, ProfileImageActivity.class);
            goToProfileImage.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(goToProfileImage);
            finish();
        }

        if(v == changeEmailText || v == changeEmailButton) {
            Intent updateEmail = new Intent(OptionActivity.this, UpdateEmailActivity.class);
            updateEmail.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(updateEmail);
            finish();
        }

        if(v == changePasswordText || v == changePasswordButton) {
            Intent updatePassword = new Intent(OptionActivity.this, UpdatePasswordActivity.class);
            updatePassword.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(updatePassword);
            finish();
        }

        if(v == helpButton){
            EmergencyCallActivity.setBackToActivity(OptionActivity.class);

            Intent intent = new Intent(OptionActivity.this, EmergencyCallActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }

    }

    private void goToMenu(){
        // back to menu page
        Intent goToMenu = new Intent(OptionActivity.this, MainActivity.class);
        goToMenu.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(goToMenu);
        finish();
    }

    @Override
    public void onBackPressed() { goToMenu(); }
}
