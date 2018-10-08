package com.example.sayyaf.homecare.notifications;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.display.DisplayManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.text.format.DateFormat;
import android.view.Display;

import com.example.sayyaf.homecare.MainActivity;
import com.example.sayyaf.homecare.R;
import com.example.sayyaf.homecare.accounts.UserAppVersionController;
import com.example.sayyaf.homecare.communication.ChatMessage;
import com.example.sayyaf.homecare.contacts.ContactChatActivity;
import com.example.sayyaf.homecare.mapping.TrackingActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class EmergencyMsgListener extends IntentService {

    private final int initEmergencyID = 10;

    private int emergencyID;
    private static Query emergencyRef;
    private static ValueEventListener notificationListener;

    public EmergencyMsgListener(){
        super("EmergencyMsgListener");
    }

    // stop tracking emergency message mailbox
    public static void stopListening(){
        emergencyRef.removeEventListener(notificationListener);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        listenToEmergencyMsg();
    }

    @Override
    public void onDestroy() {

        //stop service
        stopSelf();
        super.onDestroy();
    }

    // start tracking emergency message mailbox
    private void listenToEmergencyMsg(){
        emergencyID = initEmergencyID;

        emergencyRef = FirebaseDatabase.getInstance()
                .getReference("EmergencyMsg")
                .child(UserAppVersionController
                        .getUserAppVersionController()
                        .getCurrentUserId());

        notificationListener = setUpNotificationListener();

        emergencyRef.addValueEventListener(notificationListener);
    }

    private ValueEventListener setUpNotificationListener(){
        return new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){

                    for(DataSnapshot s : dataSnapshot.getChildren()){

                        EmergencyMsg msg = s.getValue(EmergencyMsg.class);

                        setNotification(msg.getMessageSender(), msg.getMessageTime());
                        // setNotification(msg.getMessageSender(), msg.getMessageSenderId(), msg.getMessageTime());

                    }

                    DisplayManager dm = (DisplayManager) getSystemService(Context.DISPLAY_SERVICE);

                    for (Display display : dm.getDisplays()) {
                        if (display.getState() == Display.STATE_OFF) {
                            Intent dialogIntent = new Intent(getBaseContext(), EmergencyAttentionActivity.class);
                            dialogIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            getApplication().startActivity(dialogIntent);
                            break;
                        }
                    }

                    // remove calls when user receive the emergency calls
                    FirebaseDatabase.getInstance()
                            .getReference("EmergencyMsg")
                            .child(UserAppVersionController
                                    .getUserAppVersionController().getCurrentUserId())
                            .removeValue();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
    }

    // private void setNotification(String name, String id, long time){
    private void setNotification(String name, long time){
        NotificationCompat.Builder notificationbulider = null;

        Intent goToContact = new Intent(this, ContactChatActivity.class);
        goToContact.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingGoToContact = PendingIntent.getActivity(this, 0, goToContact, 0);

        Intent goToTracking = new Intent(this, TrackingActivity.class);
        goToTracking.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        // PendingIntent pendingGoToTracking = PendingIntent.getActivity(this, 0, goToTracking, 0);
        PendingIntent pendingGoToTracking = PendingIntent.getActivity(this, 0, goToTracking, 0);


        notificationbulider =
                new NotificationCompat.Builder(EmergencyMsgListener.this,
                        NotificationChannels.getEmergencyCH())
                        .setSmallIcon(R.drawable.ic_dialog_alert_holo_light)
                        .setContentTitle("Emergency: " + name)
                        .setContentText(DateFormat.format("d/M/yy (h:mm a)",
                                time))
                        .setColor(0xffff0000)
                        .setLights(0xffff0000, 250, 10000)
                        .setVisibility(Notification.VISIBILITY_PUBLIC)
                        .setDefaults(Notification.DEFAULT_ALL)
                        .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                        .addAction(R.drawable.ic_launcher_background, "Go to Contact", pendingGoToContact)
                        .addAction(R.drawable.ic_launcher_background, "Go to Tracking", pendingGoToTracking);

        NotificationManager manager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        manager.notify(emergencyID, notificationbulider.build());

        // increment id to avoid override previous notification
        if(emergencyID >= Integer.MAX_VALUE){
            emergencyID = initEmergencyID;
        }
        else{
            emergencyID++;
        }

    }

}
