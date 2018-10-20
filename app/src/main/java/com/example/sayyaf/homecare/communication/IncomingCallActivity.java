package com.example.sayyaf.homecare.communication;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.sayyaf.homecare.ImageLoader;
import com.example.sayyaf.homecare.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.sinch.android.rtc.MissingPermissionException;
import com.sinch.android.rtc.PushPair;
import com.sinch.android.rtc.calling.Call;
import com.sinch.android.rtc.calling.CallEndCause;
import com.sinch.android.rtc.calling.CallListener;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;


/**
 * Activity used to handle an incoming call to the app
 * Adapted from Sinch SDK Sample 
 */
public class IncomingCallActivity extends BaseActivity implements View.OnClickListener{

    static final String TAG = IncomingCallActivity.class.getSimpleName();
    private String mCallId;
    private AudioPlayer mAudioPlayer;
    private ImageView profilePic;
    private Button answer;
    private Button decline;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incoming_call);

        answer = (Button) findViewById(R.id.answerButton);
        answer.setOnClickListener(this);
       decline = (Button) findViewById(R.id.declineButton);
        profilePic = (ImageView) findViewById(R.id.profileImageIncoming);
        decline.setOnClickListener(this);

        mAudioPlayer = new AudioPlayer(this);
        //play ringing tone of phone
        mAudioPlayer.playRingtone();
        mCallId = getIntent().getStringExtra(SinchService.CALL_ID);

    }
    
    
    @Override
    public void onClick(View v) {
        
        if(v == answer) {
            callAccepted();
        }
        
        else if (v == decline) {
            callDeclined();
        }
    }
    

    /**
     * When the sinch service is connected to the activity, this method is used to
     * show the incoming call to the user with the call sender name
     */
    @Override
    protected void onServiceConnected() {
        Call call = getSinchServiceInterface().getCall(mCallId);
        if (call != null) {
            call.addCallListener(new SinchCallListener());
            TextView remoteUser = (TextView) findViewById(R.id.remoteUser);
            //Split the caller ID to get the name of the call sender. 
            //An index of 0 would obtain the remote user's account ID
            String callerName= call.getRemoteUserId().split(",")[1];
            remoteUser.setText(callerName);

            // speed up image download, old method try to download url twice
            Query profilePicUriRef =  FirebaseDatabase.getInstance() .getReference("User")
                    .child(call.getRemoteUserId().split(",")[0])
                    .child("profileImage");

            profilePicUriRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists()) {
                        String userImageUriString = dataSnapshot.getValue(String.class);

                        if(userImageUriString.equals("no Image"))
                            return;

                        ImageLoader.getImageLoader().loadContactImageToView(
                                IncomingCallActivity.this,
                                profilePic, userImageUriString);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

        } else {
            Log.e(TAG, "Started with invalid callId, aborting");
            finish();
        }
    }


    /**
     * The method called when the call has been accepted. Sends to appropriate activity
     * depending on type of call
     */
    private void callAccepted() {
        mAudioPlayer.stopRingtone();
        Call call = getSinchServiceInterface().getCall(mCallId);
        if (call != null) {
            try {
                call.answer();
                //Check if video call or voice call and start the appropriate activity
                if(call.getDetails().isVideoOffered()) {
                    Intent intent = new Intent(this, IncomingCallActivity.class);
                    intent.putExtra(SinchService.CALL_ID, mCallId);
                    startActivity(intent);
                }

                else {
                    Intent intent = new Intent(this, VoiceCallScreenActivity.class);
                    intent.putExtra(SinchService.CALL_ID, mCallId);
                    startActivity(intent);
                }
                
                //Checks if missing permissions to make the call. 
            } catch (MissingPermissionException e) {
                ActivityCompat.requestPermissions(this, new String[]{e.getRequiredPermission()}, 0);
            }
        } else {
            finish();
        }
    }

    /**
     * Requests required permissions from the user
     * @param requestCode
     * @param permissions Permissions Required
     * @param grantResults Permissions Granted
     */
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "You may now answer the call", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "This application needs permission to use your microphone to function properly.", Toast
                    .LENGTH_LONG).show();
        }
    }

    /**
     * The method called when the call has been declined. Hangs up the call
     */
    private void callDeclined() {
        mAudioPlayer.stopRingtone();
        Call call = getSinchServiceInterface().getCall(mCallId);
        if (call != null) {
            call.hangup();
        }
        finish();
    }

    
    //Method used to avoid the call continuing if back pressed
    @Override
    public void onBackPressed() {
        callDeclined();
    }


    /**
     * Ensures that if activity destryoyed, call ends with it
     */
    @Override
    protected void onDestroy(){
        mAudioPlayer.stopRingtone();
        Call call = getSinchServiceInterface().getCall(mCallId);
        if (call != null) {
            call.hangup();
        }
        super.onDestroy();
    }
    

    public class SinchCallListener implements CallListener {

        /**
         * Asynchronous method that handles the call ending
         * @param call
         */
        public void onCallEnded(Call call) {
            CallEndCause cause = call.getDetails().getEndCause();
            Log.d(TAG, "Call ended. Reason: " + cause.toString());
            mAudioPlayer.stopRingtone();
            String endMsg = "Call ended";

            if(cause.toString().equals("TIMEOUT")) {
                endMsg+= " because " + call.getRemoteUserId().split(",")[1] + " is unavailable";
                Toast.makeText(IncomingCallActivity.this, endMsg, Toast.LENGTH_LONG).show();
            }

            else if(cause.toString().equals("ENDED")) {
                Toast.makeText(IncomingCallActivity.this, endMsg, Toast.LENGTH_LONG).show();
            }

            else if(cause.toString().equals("DENIED")) {
                Toast.makeText(IncomingCallActivity.this, endMsg + " " + " because user is busy. " +
                        "Please try again later", Toast.LENGTH_LONG).show();
            }

            else if(cause.toString().equals("HUNG_UP") || cause.toString().equals("CANCELLED")) {

            }

            else {
                Toast.makeText(IncomingCallActivity.this, endMsg + " " + cause.toString(), Toast.LENGTH_LONG).show();
            }
            finish();
        }

        //Logs call establishment
        @Override
        public void onCallEstablished(Call call) {
            Log.d(TAG, "Call established");
        }

        //Logs call progression
        @Override
        public void onCallProgressing(Call call) {
            Log.d(TAG, "Call progressing");
        }


        @Override
        public void onShouldSendPushNotification(Call call, List<PushPair> pushPairs) {
        }

    }

}