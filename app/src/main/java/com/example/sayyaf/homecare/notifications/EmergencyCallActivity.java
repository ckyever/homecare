package com.example.sayyaf.homecare.notifications;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sayyaf.homecare.ActivityKeeper;
import com.example.sayyaf.homecare.MainActivity;
import com.example.sayyaf.homecare.R;
import com.example.sayyaf.homecare.accounts.User;
import com.example.sayyaf.homecare.accounts.UserAppVersionController;
import com.example.sayyaf.homecare.communication.ChatMessage;
import com.example.sayyaf.homecare.contacts.ContactChatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

// This class handles emergency call from assisted person
public class EmergencyCallActivity extends AppCompatActivity implements View.OnClickListener  {

    private static ActivityKeeper backToActivity;

    private CountDownTimer timer;
    private Button cancelCall;
    private TextView timeLeft;

    private User this_device;

    private Vibrator misLaunchAttention;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency);

        cancelCall = (Button) findViewById(R.id.cancelCall);
        timeLeft = (TextView) findViewById(R.id.timeLeft);

        cancelCall.setOnClickListener(this);

        misLaunchAttention = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);

        this_device = null;
        timer = null;

    }

    @Override
    protected void onStart(){
        super.onStart();

        // vibration notifier to avoid misLaunch Emergency call
        misLaunchAttention.vibrate(500);

        timer = setTime(this, 5);
        timer.start();

    }

    // set up returning path for this activity (since there are help buttons on any other pages)
    public static void setBackToActivity(Class entryActivity){
        backToActivity = new ActivityKeeper(entryActivity);
    }

    @Override
    protected void onPause(){
        // clear up timer
        if(timer != null){
            misLaunchAttention.cancel();

            timer.cancel();
            timer = null;
        }
        super.onPause();
    }

    @Override
    public void onClick(View v) {
        if(v == cancelCall){
            cancelEmergencyCall();
        }
    }

    @Override
    public void onBackPressed() {
        cancelEmergencyCall();
    }

    private CountDownTimer setTime(Context context, long seconds){
        return new CountDownTimer(seconds * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                // show the remaining time for notification to send
                timeLeft.setText("" + millisUntilFinished / 1000);
            }

            @Override
            public void onFinish() {
                // send emergency contents
                fireEmergencyNotification();
            }
        };
    }

    private void backToLastActivity(){
        backToActivity.returnToActivity(this);
    }

    private void cancelEmergencyCall(){
        // clear up timer
        if(timer != null){

            misLaunchAttention.cancel();

            timer.cancel();
            timer = null;
        }

        Toast.makeText(EmergencyCallActivity.this,
                "Emergency Notification is cancelled", Toast.LENGTH_SHORT).show();

        backToLastActivity();
    }

    private void fireEmergencyNotification(){
        // block actions those require internet connection
        if(!NetworkConnection.getConnection()){
            NetworkConnection.requestNetworkConnection(EmergencyCallActivity.this);
            backToLastActivity();
            return;
        }

        Query userRef = FirebaseDatabase.getInstance()
                .getReference("User")
                .child(UserAppVersionController
                        .getUserAppVersionController().getCurrentUserId());

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                        this_device = dataSnapshot.getValue(User.class);

                        if(checkHasFriends()) {
                            EmergencyMsg msg = new EmergencyMsg(
                                    UserAppVersionController.getUserAppVersionController()
                                            .getCurrentUserId(), this_device.getName());

                            // send emergency contents to all friends
                            for (String friendId : this_device.getFriends().keySet()) {
                                sendNotification(friendId, msg);
                            }

                            Toast.makeText(EmergencyCallActivity.this,
                                    "Emergency Notification is sent", Toast.LENGTH_SHORT).show();

                        }
                        else{
                            Toast.makeText(EmergencyCallActivity.this,
                                    "User need at least one added caregiver", Toast.LENGTH_SHORT).show();
                        }

                }

                backToLastActivity();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                backToLastActivity();
            }

        });
    }

    // check if the assisted person has a caregiver
    private boolean checkHasFriends(){
        return (this_device != null
                && this_device.getFriends() != null
                && !this_device.getFriends().isEmpty());
    }

    /* send the notification to the caregiver
     * friendId: the user id of the caregiver
     * msg: contains sender name and send time
     */
    private void sendNotification(String friendId, EmergencyMsg msg){
        FirebaseDatabase.getInstance().getReference("EmergencyMsg")
                .child(friendId).push().setValue(msg);
    }
}
