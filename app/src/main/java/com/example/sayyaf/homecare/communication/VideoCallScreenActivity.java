package com.example.sayyaf.homecare.communication;

import com.example.sayyaf.homecare.R;
import com.sinch.android.rtc.AudioController;
import com.sinch.android.rtc.PushPair;
import com.sinch.android.rtc.calling.Call;
import com.sinch.android.rtc.calling.CallEndCause;
import com.sinch.android.rtc.calling.CallState;
import com.sinch.android.rtc.video.VideoCallListener;
import com.sinch.android.rtc.video.VideoController;

import android.media.AudioManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Activity to handle video calling within the application. Uses WebRTC for calling
 * and runs on the Sinch backend
 * Adapted from Sinch SDK sample
 */
public class VideoCallScreenActivity extends BaseActivity {

    static final String TAG = VideoCallScreenActivity.class.getSimpleName();
    static final String ADDED_LISTENER = "addedListener";

    private AudioPlayer mAudioPlayer;
    private Timer mTimer;
    private UpdateCallDurationTask mDurationTask;
    private Call call;


    private String mCallId;
    private TextView mCallDuration;
    private TextView mCallState;
    private TextView mCallerName;
    private boolean mAddedListener = false;
    private boolean mLocalVideoViewAdded = false;
    private boolean mRemoteVideoViewAdded = false;

    /**
     * Task to continuously update how long the call has been going
     */
    private class UpdateCallDurationTask extends TimerTask {

        @Override
        public void run() {
            VideoCallScreenActivity.this.runOnUiThread(new Runnable() {
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
        setContentView(R.layout.activity_video_call_screen);

        mAudioPlayer = new AudioPlayer(this);
        mCallDuration = (TextView) findViewById(R.id.callDuration);
        mCallerName = (TextView) findViewById(R.id.remoteUser);
        mCallState = (TextView) findViewById(R.id.callState);
        Button endCallButton = (Button) findViewById(R.id.hangupButton);

        endCallButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                endCall();
            }
        });

        mCallId = getIntent().getStringExtra(SinchService.CALL_ID);
    }


    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        //
        super.onSaveInstanceState(savedInstanceState);
        //
        savedInstanceState.putBoolean(ADDED_LISTENER, mAddedListener);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        mAddedListener = savedInstanceState.getBoolean(ADDED_LISTENER);
    }


    /**
     * When the sinch service is connected to the activity, sets the neccessary listeners
     * to find out if call is accepted or rejected
     */
    @Override
    public void onServiceConnected() {
        call = getSinchServiceInterface().getCall(mCallId);
        if (call != null) {
            call.addCallListener(new SinchCallListener());
            mAddedListener = true;
            String callerName= call.getRemoteUserId().split(",")[1];
            mCallerName.setText(callerName);
            mCallState.setText(call.getState().toString());
        } else {
            Log.e(TAG, "Started with invalid callId, aborting.");
            finish();
        }
    }

    /**
     * Asynchronous method to handle what happens if the call is paused
     */
    @Override
    public void onPause() {
        super.onPause();
        mDurationTask.cancel();
        mTimer.cancel();
    }


    /**
     * Asynchronous method to handle what happens if the call is resumed
     */
    @Override
    public void onResume() {
        super.onResume();
        mTimer = new Timer();
        mDurationTask = new UpdateCallDurationTask();
        mTimer.schedule(mDurationTask, 0, 500);
    }

   //Don't want to allow user to exit call screen without ending it
    @Override
    public void onBackPressed() {
    }

    // avoid call continue after swipe
    @Override
    protected void onDestroy(){
        // mAudioPlayer.stopProgressTone();
        Call call = getSinchServiceInterface().getCall(mCallId);
        if (call != null) {
            call.hangup();
        }
        super.onDestroy();
    }

    /**
     * Handles call ending. Hangs up the call and stops the ringing tone if necessary
     */
    private void endCall() {
        /*mAudioPlayer.stopProgressTone();
        Call call = getSinchServiceInterface().getCall(mCallId);
        if (call != null) {
            call.hangup();
        }*/
        finish();
    }


    /**
     * Pauses the call. Sets the video stream views to invisible
     */
    private void pauseCall() {
        if(call!=null) {
            call.pauseVideo();

        }
    }

    /**
     * Resumes the paused call. Redefines the video stream views
     */
    private void resumeCall() {
        if(call!=null) {
            call.resumeVideo();
           addLocalView();
           addRemoteView();
        }
    }

    private void updateUI() {
        if (getSinchServiceInterface() == null) {
            return; // early
        }

        Call call = getSinchServiceInterface().getCall(mCallId);
        if (call != null) {
            mCallState.setText(call.getState().toString());
            if (call.getDetails().isVideoOffered()) {
                addLocalView();
                if (call.getState() == CallState.ESTABLISHED) {
                    addRemoteView();
                }
            }
        }
    }


    /**
     * Adds the video stream of the local user to the screen
     */
    private void addLocalView() {
        if (mLocalVideoViewAdded || getSinchServiceInterface() == null) {
            return; //early
        }
        final VideoController vc = getSinchServiceInterface().getVideoController();
        if (vc != null) {
            RelativeLayout localView = (RelativeLayout) findViewById(R.id.localVideo);
            localView.addView(vc.getLocalView());
            mLocalVideoViewAdded = true;
            localView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    vc.toggleCaptureDevicePosition();
                }
            });
        }
    }

    /**
     * Adds the video stream of the call receiver user to the screen once call has been
     * established
     */
    private void addRemoteView() {
        if (mRemoteVideoViewAdded || getSinchServiceInterface() == null) {
            return; //early
        }
        final VideoController vc = getSinchServiceInterface().getVideoController();
        if (vc != null) {
            RelativeLayout view = (RelativeLayout) findViewById(R.id.remoteVideo);
            view.addView(vc.getRemoteView());
        }
    }


    //
    private void removeLocalView() {
        if (mRemoteVideoViewAdded || getSinchServiceInterface() == null) {
            return; //early
        }
        final VideoController vc = getSinchServiceInterface().getVideoController();
        if (vc != null) {
            RelativeLayout view = (RelativeLayout) findViewById(R.id.localVideo);
            view.removeView(vc.getLocalView());
            mRemoteVideoViewAdded = false;
        }
    }
    //


    private void removeRemoteView() {
        if (mRemoteVideoViewAdded || getSinchServiceInterface() == null) {
            return; //early
        }
        final VideoController vc = getSinchServiceInterface().getVideoController();
        if (vc != null) {
            RelativeLayout view = (RelativeLayout) findViewById(R.id.remoteVideo);
            view.removeView(vc.getRemoteView());
            mRemoteVideoViewAdded = false;
        }
    }

    /**
     * Formats the time to minutes and seconds
     * @param totalSeconds the number of seconds the call has run
     * @return The formatted time
     */
    private String formatTimespan(int totalSeconds) {
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;
        return String.format(Locale.US, "%02d:%02d", minutes, seconds);
    }


    /**
     * Updates the ongoing call duration on teh screen
     */
    private void updateCallDuration() {
        Call call = getSinchServiceInterface().getCall(mCallId);
        if (call != null) {
            mCallDuration.setText(formatTimespan(call.getDetails().getDuration()));
        }
    }



    private class SinchCallListener implements VideoCallListener {
        /**
         * Asynchronous method that handles the call ending
         * @param call call object to represent call taking place
         */
        @Override
        public void onCallEnded(Call call) {
            CallEndCause cause = call.getDetails().getEndCause();
            Log.d(TAG, "Call ended. Reason: " + cause.toString());
            mAudioPlayer.stopProgressTone();
            setVolumeControlStream(AudioManager.USE_DEFAULT_STREAM_TYPE);
            String endMsg = "Call ended";

            removeRemoteView();
            removeLocalView();

            if(cause.toString().equals("TIMEOUT")) {
                endMsg+= " because " + call.getRemoteUserId().split(",")[1] + " is unavailable";
                Toast.makeText(VideoCallScreenActivity.this, endMsg, Toast.LENGTH_LONG).show();
            }

            else if(cause.toString().equals("ENDED")) {
                Toast.makeText(VideoCallScreenActivity.this, endMsg, Toast.LENGTH_LONG).show();
            }

            else if(cause.toString().equals("DENIED")) {
                Toast.makeText(VideoCallScreenActivity.this, endMsg + " " + " because user is busy. " +
                        "Please try again later", Toast.LENGTH_LONG).show();
            }

            else if(cause.toString().equals("HUNG_UP") || cause.toString().equals("CANCELLED")) {

            }

            else {
                Toast.makeText(VideoCallScreenActivity.this, endMsg + " " + cause.toString(), Toast.LENGTH_LONG).show();
            }
            endCall();
        }


        /**
         * Method used when call has been established. Sets the volume control,
         * stops the ringing tone and enables both users to hear each other
         * @param call
         */
        @Override
        public void onCallEstablished(Call call) {
            Log.d(TAG, "Call established");
            mAudioPlayer.stopProgressTone();
            mCallState.setText(call.getState().toString());
            setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);
            AudioController audioController = getSinchServiceInterface().getAudioController();
            audioController.enableSpeaker();
            Log.d(TAG, "Call offered video: " + call.getDetails().isVideoOffered());
        }

        /**
         * Method called when call is waiting to be picked up
         * @param call
         */
        @Override
        public void onCallProgressing(Call call) {
            Log.d(TAG, "Call progressing");
            mAudioPlayer.playProgressTone();
        }

        @Override
        public void onShouldSendPushNotification(Call call, List<PushPair> pushPairs) {
        }


        /**
         * Method that handles adding the video streams of the users to the screen
         * @param call
         */
        @Override
        public void onVideoTrackAdded(Call call) {
                Log.d(TAG, "Video track added");
            addLocalView();
            addRemoteView();
        }

        @Override
        public void onVideoTrackPaused(Call call) {

        }

        @Override
        public void onVideoTrackResumed(Call call) {

        }
    }
}

