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
import com.example.sayyaf.homecare.contacts.ContactChatActivity;
import com.example.sayyaf.homecare.notifications.EmergencyCallActivity;
import com.example.sayyaf.homecare.notifications.NetworkConnection;

public class OptionActivity extends AppCompatActivity implements View.OnClickListener {

    private FloatingActionButton changeImageButton;
    private TextView changeImageText;

    private Button helpButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_options);

        changeImageButton = (FloatingActionButton) findViewById(R.id.changeImageButton);
        changeImageText = (TextView) findViewById(R.id.changeImage);
        helpButton = (Button) findViewById(R.id.optionHelp);

        // activate help button on assisted person version
        UserAppVersionController.getUserAppVersionController().resetButton(helpButton);
    }

    @Override
    public void onClick(View v) {
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

        if(v == helpButton){
            EmergencyCallActivity.setBackToActivity(OptionActivity.class);

            Intent intent = new Intent(OptionActivity.this, EmergencyCallActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }

    }



    @Override
    public void onBackPressed() {
        // back to menu page
        Intent goToMenu = new Intent(OptionActivity.this, MainActivity.class);
        goToMenu.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(goToMenu);
        finish();
    }
}
