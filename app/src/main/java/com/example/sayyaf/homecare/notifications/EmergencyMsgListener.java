package com.example.sayyaf.homecare.notifications;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.text.format.DateFormat;

import com.example.sayyaf.homecare.MainActivity;
import com.example.sayyaf.homecare.R;
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

    private int emergencyID;
    private ArrayList<Integer> emergencyIDs;
    private static Query emergencyRef;
    private static ValueEventListener notificationListener;

    public EmergencyMsgListener(){
        super("EmergencyMsgListener");
        emergencyID = 20;

        emergencyRef = FirebaseDatabase.getInstance()
                .getReference("EmergencyMsg")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        notificationListener = setUpnotificationListener();

        emergencyIDs = new ArrayList<Integer>();
    }

    public static Query getEmergencyRef(){
        return emergencyRef;
    }

    public static ValueEventListener getNotificationListener(){
        return notificationListener;
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


    private void listenToEmergencyMsg(){

        emergencyRef = FirebaseDatabase.getInstance()
                .getReference("EmergencyMsg")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        notificationListener = setUpnotificationListener();

        emergencyRef.addValueEventListener(notificationListener);

    }

    private ValueEventListener setUpnotificationListener(){
        return new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){

                    ArrayList<String> senders = new ArrayList<String>();
                    ArrayList<Long> times = new ArrayList<Long>();

                    for(DataSnapshot s : dataSnapshot.getChildren()){

                        ChatMessage ct = s.getValue(ChatMessage.class);

                        //setNotification(ct.getMessageSender(), ct.getMessageTime());

                        senders.add(ct.getMessageSender());
                        times.add(ct.getMessageTime());

                    }

                    for(int i = 0; i < senders.size(); i++){
                        setNotification(senders.get(i), times.get(i));
                    }

                    FirebaseDatabase.getInstance()
                            .getReference("EmergencyMsg")
                            .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                            .removeValue();

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
    }

    private void setNotification(String name, long time){
        NotificationCompat.Builder notificationbulider = null;

        Intent goToContact = new Intent(this, ContactChatActivity.class);
        PendingIntent pendingGoToContact = PendingIntent.getActivity(this, 0, goToContact, 0);

        Intent goToTracking = new Intent(this, TrackingActivity.class);
        PendingIntent pendingGoToTracking = PendingIntent.getActivity(this, 0, goToTracking, 0);


        notificationbulider =
                new NotificationCompat.Builder(EmergencyMsgListener.this,
                        NotificationChannels.getEmergencyCH())
                        .setSmallIcon(R.drawable.ic_launcher_background)
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

        emergencyIDs.add(emergencyID);

        emergencyID++;
    }


}
