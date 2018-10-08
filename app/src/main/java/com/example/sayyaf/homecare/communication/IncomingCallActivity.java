package com.example.sayyaf.homecare.communication;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.sayyaf.homecare.R;
import com.google.android.gms.tasks.OnSuccessListener;
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
 */
public class IncomingCallActivity extends BaseActivity {

    static final String TAG = IncomingCallActivity.class.getSimpleName();
    private String mCallId;
    private AudioPlayer mAudioPlayer;
    private ImageView profilePic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incoming_call);

        Button answer = (Button) findViewById(R.id.answerButton);
        answer.setOnClickListener(mClickListener);
        Button decline = (Button) findViewById(R.id.declineButton);
        profilePic = (ImageView) findViewById(R.id.profileImageIncoming);
        decline.setOnClickListener(mClickListener);

        mAudioPlayer = new AudioPlayer(this);
        //play ringing tone of phone
        mAudioPlayer.playRingtone();
        mCallId = getIntent().getStringExtra(SinchService.CALL_ID);
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
            //Break the caller id to get the name of the call sender
            String callerName= call.getRemoteUserId().split(",")[1];
            remoteUser.setText(callerName);
            FirebaseStorage.getInstance()
                    .getReference("UserProfileImage")
                    .child(call.getRemoteUserId().split(",")[0])
                    .getDownloadUrl()
                    .addOnSuccessListener(onDownloadSuccess(profilePic));
        } else {
            Log.e(TAG, "Started with invalid callId, aborting");
            finish();
        }
    }


    /**
     * The method called when the call has been accepted. Sends to appropriate activity
     * depending on type of call
     */
    private void answerClicked() {
        mAudioPlayer.stopRingtone();
        Call call = getSinchServiceInterface().getCall(mCallId);
        if (call != null) {
            try {
                call.answer();
                if(call.getDetails().isVideoOffered()) {
                    Intent intent = new Intent(this, VideoCallScreenActivity.class);
                    intent.putExtra(SinchService.CALL_ID, mCallId);
                    startActivity(intent);
                }

                else {
                    Intent intent = new Intent(this, VoiceCallScreenActivity.class);
                    intent.putExtra(SinchService.CALL_ID, mCallId);
                    startActivity(intent);
                }
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
    private void declineClicked() {
        /*mAudioPlayer.stopRingtone();
        Call call = getSinchServiceInterface().getCall(mCallId);
        if (call != null) {
            call.hangup();
        }*/
        finish();
    }

    @Override
    public void onBackPressed() {
        declineClicked();
    }

    // avoid call continue after swipe
    @Override
    protected void onDestroy(){
        mAudioPlayer.stopRingtone();
        Call call = getSinchServiceInterface().getCall(mCallId);
        if (call != null) {
            call.hangup();
        }
        super.onDestroy();
    }

    /**
     * OnClickListener listens for whether call was accepted or rejected
     */
    private OnClickListener mClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.answerButton:
                    answerClicked();
                    break;
                case R.id.declineButton:
                    declineClicked();
                    break;
            }
        }
    };

    public class SinchCallListener implements CallListener {

        /**
         * Asynchronous method that handles the call ending
         * @param call
         */
        @Override
        public void onCallEnded(Call call) {
            CallEndCause cause = call.getDetails().getEndCause();
            Log.d(TAG, "Call ended, cause: " + cause.toString());
            mAudioPlayer.stopRingtone();
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

    private OnSuccessListener<Uri> onDownloadSuccess(ImageView userImage){
        return new OnSuccessListener<Uri>(){
            @Override
            public void onSuccess(Uri userImagePath) {

                Glide.with(IncomingCallActivity.this)
                        .load(userImagePath.toString())
                        .apply(new RequestOptions()
                                .override(100, 100) // resize image in pixel
                                .centerCrop()
                                .dontAnimate())
                        .into(userImage);

            }
        };
    }
}