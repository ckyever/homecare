package com.example.sayyaf.homecare.communication;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.sayyaf.homecare.ImageLoader;
import com.example.sayyaf.homecare.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.sinch.android.rtc.PushPair;
import com.sinch.android.rtc.calling.Call;
import com.sinch.android.rtc.calling.CallEndCause;
import com.sinch.android.rtc.calling.CallListener;

import android.media.AudioManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

/**
    * Activity to handle voice calling within the application. Uses WebRTC for calling
    * and runs on the Sinch backend
    *  Adapted from Sinch SDK sample
    */
public class VoiceCallScreenActivity extends BaseActivity {
    static final String TAG = VoiceCallScreenActivity.class.getSimpleName();

    private AudioPlayer mAudioPlayer;
    private Timer mTimer;
    private UpdateCallDurationTask mDurationTask;

    private String mCallId;
    private ImageView profilePic;

    private TextView mCallDuration;
    private TextView mCallState;
    private TextView mCallerName;

    private class UpdateCallDurationTask extends TimerTask {

        /**
         * Task to continuously update how long the call has been going
         */
        @Override
        public void run() {
            VoiceCallScreenActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    updateCallDuration();
                }
            });
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice_call_screen);

        mAudioPlayer = new AudioPlayer(this);
        mCallDuration = (TextView) findViewById(R.id.callDurationVoice);
        mCallerName = (TextView) findViewById(R.id.remoteUserVoice);
        mCallState = (TextView) findViewById(R.id.callStateVoice);
        profilePic = (ImageView) findViewById(R.id.profileImageVoice);
        Button endCallButton = (Button) findViewById(R.id.hangupButtonVoice);

        endCallButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                endCall();
            }
        });
        mCallId = getIntent().getStringExtra(SinchService.CALL_ID);
    }

    /**
     * When the sinch service is connected to the activity, sets the neccessary listeners
     * to find out if call is accepted or rejected
     */
    @Override
    public void onServiceConnected() {
        Call call = getSinchServiceInterface().getCall(mCallId);
        if (call != null) {
            call.addCallListener(new SinchCallListener());
            String callerName= call.getRemoteUserId().split(",")[1];
            mCallerName.setText(callerName);
            mCallState.setText(call.getState().toString());

            // speed up image download, old method try to download url for twice
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
                                VoiceCallScreenActivity.this,
                                profilePic, userImageUriString);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

        } else {
            Log.e(TAG, "Started with invalid callId, aborting.");
            finish();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mDurationTask.cancel();
        mTimer.cancel();
    }

    @Override
    public void onResume() {
        super.onResume();
        mTimer = new Timer();
        mDurationTask = new UpdateCallDurationTask();
        mTimer.schedule(mDurationTask, 0, 500);
    }

    @Override
    public void onBackPressed() {
        // User should exit activity by ending call, not by going back.
    }

    // avoid call continue after swipe
    @Override
    protected void onDestroy(){
        mAudioPlayer.stopProgressTone();
        Call call = getSinchServiceInterface().getCall(mCallId);
        if (call != null) {
            call.hangup();
        }
        super.onDestroy();
    }

    /**
     * Called when call is requested to end
     */
    private void endCall() {
        mAudioPlayer.stopProgressTone();
        Call call = getSinchServiceInterface().getCall(mCallId);
        if (call != null) {
            call.hangup();
        }
        finish();
    }

    /**
     * Sets the format of the timespan of call shown to user
     * @param totalSeconds amount of time call has been established
     * @return
     */
    private String formatTimespan(int totalSeconds) {
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;
        return String.format(Locale.US, "%02d:%02d", minutes, seconds);
    }

    /**
     * Updates the call duration on the users devices
     */
    private void updateCallDuration() {
        Call call = getSinchServiceInterface().getCall(mCallId);
        if (call != null) {
            mCallDuration.setText(formatTimespan(call.getDetails().getDuration()));
        }
    }


    private OnSuccessListener<Uri> onDownloadSuccess(ImageView userImage){
        return new OnSuccessListener<Uri>(){
            @Override
            public void onSuccess(Uri userImagePath) {

                Glide.with(VoiceCallScreenActivity.this)
                        .load(userImagePath.toString())
                        .apply(new RequestOptions()
                                .override(100, 100) // resize image in pixel
                                .centerCrop()
                                .dontAnimate())
                        .into(userImage);

            }
        };
    }

    private class SinchCallListener implements CallListener {

        /**
         * Called when call has been ended. Sets the volume to default, stops the progress
         * tone if still playing and ends the call for the current user.
         * @param call
         */
        @Override
        public void onCallEnded(Call call) {
            CallEndCause cause = call.getDetails().getEndCause();
            Log.d(TAG, "Call ended. Reason: " + cause.toString());
            mAudioPlayer.stopProgressTone();
            setVolumeControlStream(AudioManager.USE_DEFAULT_STREAM_TYPE);
            String endMsg = "Call ended";
            if(cause.toString().equals("TIMEOUT")) {
                endMsg+= " because " + call.getRemoteUserId().split(",")[1] + " is unavailable";
                Toast.makeText(VoiceCallScreenActivity.this, endMsg, Toast.LENGTH_LONG).show();
            }

            else if(cause.toString().equals("ENDED")) {
                Toast.makeText(VoiceCallScreenActivity.this, endMsg, Toast.LENGTH_LONG).show();
            }

            else if(cause.toString().equals("DENIED")) {
                Toast.makeText(VoiceCallScreenActivity.this, endMsg + " " + " because user is busy. " +
                        "Please try again later", Toast.LENGTH_LONG).show();
            }

            else if(cause.toString().equals("HUNG_UP")  || cause.toString().equals("CANCELLED")) {

            }

            else {
                Toast.makeText(VoiceCallScreenActivity.this, endMsg + " " + cause.toString(), Toast.LENGTH_LONG).show();
            }
            endCall();
        }

        /**
         * Called when the call is established.
         * Sets volume control and stops progress tone
         * @param call
         */
        @Override
        public void onCallEstablished(Call call) {
            Log.d(TAG, "Call established");
            mAudioPlayer.stopProgressTone();
            mCallState.setText(call.getState().toString());
            setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);
        }

        @Override
        public void onCallProgressing(Call call) {
            Log.d(TAG, "Call progressing");
            mAudioPlayer.playProgressTone();
        }

        @Override
        public void onShouldSendPushNotification(Call call, List<PushPair> pushPairs) {
        }

    }
}
