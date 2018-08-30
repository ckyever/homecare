package com.example.sayyaf.homecare;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import org.webrtc.AudioSource;
import org.webrtc.AudioTrack;
import org.webrtc.MediaConstraints;
import org.webrtc.PeerConnectionFactory;

public class VoiceCallActivity extends AppCompatActivity implements View.OnClickListener{

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice_call);

        //StartVoiceCall();
    }

    // onclick method: start voice call
    /*public void StartVoiceCall() {

        PeerConnectionFactory.InitializationOptions initializationOptions =
                PeerConnectionFactory.InitializationOptions.
                        builder(this).
                        createInitializationOptions();

        PeerConnectionFactory.initialize(initializationOptions);
        PeerConnectionFactory peerConnectionFactory = PeerConnectionFactory.builder().createPeerConnectionFactory();

        //Create MediaConstraints
        MediaConstraints constraints = new MediaConstraints();

        //create an AudioSource instance
        AudioSource audioSource = peerConnectionFactory.createAudioSource(constraints);
        AudioTrack localAudioTrack = peerConnectionFactory.createAudioTrack("101", audioSource);

        //peerConnectionFactory.createPeerConnection()

    }*/



    @Override
    public void onClick(View v) {
        int button_id = v.getId();

        switch (button_id) {
            case R.id.endCall:
                Intent goToChat = new Intent(VoiceCallActivity.this, ChatActivity.class);
                startActivity(goToChat);
                finish();
                break;
        }
    }

    @Override
    public void onBackPressed() {
        Intent goToChat = new Intent(VoiceCallActivity.this, ChatActivity.class);
        startActivity(goToChat);
        finish();
    }
}
