package com.example.sayyaf.homecare.notifications;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.text.format.DateFormat;

import com.example.sayyaf.homecare.R;
import com.example.sayyaf.homecare.communication.ChatMessage;
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
    private final int max_emergency_notifications = 5;

    private FirebaseUser user_this_device;

    public EmergencyMsgListener(){
        super("EmergencyMsgListener");
        emergencyID = 10;
        user_this_device = FirebaseAuth.getInstance().getCurrentUser();
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        listenToEmergencyMsg();
    }

    private void listenToEmergencyMsg(){
        String uid = user_this_device.getUid();

        Query emergencyRef = FirebaseDatabase.getInstance().getReference("EmergencyMsg").child(uid);

        emergencyRef.addValueEventListener(new ValueEventListener() {
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
                            .child(uid).removeValue();

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void setNotification(String name, long time){
        NotificationCompat.Builder notificationbulider = null;

        notificationbulider =
                new NotificationCompat.Builder(EmergencyMsgListener.this,
                        NotificationChannels.getEmergencyCH())
                        .setSmallIcon(R.drawable.ic_launcher_background)
                        .setContentTitle("Emergency: " + name)
                        .setContentText(DateFormat.format("d/M/yy (h:mm a)",
                                time))
                        .setColor(0xffff0000)
                        .setLights(0xffff0000, 250, 10000)
                        .setDefaults(Notification.DEFAULT_ALL)
                        .setCategory(NotificationCompat.CATEGORY_MESSAGE);

        NotificationManager manager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        manager.notify(emergencyID, notificationbulider.build());

        if(emergencyID <= max_emergency_notifications){
            emergencyID = 10;
        }
        else
            emergencyID--;
    }


}
