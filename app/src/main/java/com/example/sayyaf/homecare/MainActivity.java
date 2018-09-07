package com.example.sayyaf.homecare;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    Button mMapButton;
    Button mContacts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mMapButton = findViewById(R.id.mapButton);
        mMapButton.setOnClickListener(this);

        mContacts = (Button) findViewById(R.id.button3);
        mContacts.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if(view == mMapButton) {
            Intent intent = new Intent(MainActivity.this, MapsActivity.class);
            startActivity(intent);
        }

        if (view == mContacts) {
            Intent intent = new Intent(MainActivity.this, ContactsActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
    }
}
