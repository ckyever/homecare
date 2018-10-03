package com.example.sayyaf.homecare.notifications;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import com.example.sayyaf.homecare.MainActivity;
import com.example.sayyaf.homecare.R;
import com.example.sayyaf.homecare.contacts.ContactChatActivity;
import com.example.sayyaf.homecare.mapping.TrackingActivity;

public class EmergencyAttentionActivity extends AppCompatActivity implements View.OnClickListener {

    private Button done;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency_attention);

        done = (Button) findViewById(R.id.done);

        getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

    }

    @Override
    public void onClick(View v) {
        if(v == done){
            // back to menu page
            finish();
        }
    }


}
