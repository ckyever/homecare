package com.example.sayyaf.homecare.options;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.example.sayyaf.homecare.MainActivity;
import com.example.sayyaf.homecare.R;

public class OptionActivity extends AppCompatActivity implements View.OnClickListener {

    private FloatingActionButton changeImageButton;
    private TextView changeImageText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_options);

        changeImageButton = (FloatingActionButton) findViewById(R.id.changeImageButton);
        changeImageButton.setOnClickListener(this);

        changeImageText = (TextView) findViewById(R.id.changeImage);
        changeImageText.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        // go to image upload
        if(v == changeImageButton || v == changeImageText){
            Intent goToProfileImage = new Intent(OptionActivity.this, ProfileImageActivity.class);
            goToProfileImage.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(goToProfileImage);
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
