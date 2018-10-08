package com.example.sayyaf.homecare.notifications;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

import com.example.sayyaf.homecare.MainActivity;
import com.example.sayyaf.homecare.R;
import com.example.sayyaf.homecare.accounts.UserAppVersionController;
import com.example.sayyaf.homecare.contacts.ContactChatActivity;

public class NotificationService extends Service {

    // private boolean connectionState;
    private BroadcastReceiver connectivityReceiver;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        super.onStartCommand(intent, flags, startId);

        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // keep the app on foreground
        serviceKeeper();

        // start tracking network connection state
        internetStateMonitor();

        // start tracking if emergency message sent to caregiver
        if(UserAppVersionController.getUserAppVersionController().getIsCaregiver())
            listenToEmergencyMsg(this);

    }

    // keep the app on foreground, enable services run even with app is closed (if logged in)
    private void serviceKeeper(){
        NotificationCompat.Builder notificationbulider = null;

        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        notificationbulider =
                new NotificationCompat.Builder(this, NotificationChannels.getForegroundCH())
                        .setSmallIcon(R.drawable.ic_launcher_background)
                        .setContentTitle("Welcome to HomeCare")
                        .setContentText("You have logged in")
                        .setDefaults(Notification.DEFAULT_ALL)
                        .setVisibility(Notification.VISIBILITY_PUBLIC)
                        .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                        .setContentIntent(pendingIntent)
                        .setOngoing(true);

        startForeground(3, notificationbulider.build());
    }

    // start tracking network connection state
    private void internetStateMonitor(){

        connectivityReceiver = new NetworkConnection();

        registerReceiver(connectivityReceiver,
                new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));
    }

    // start tracking if emergency message sent to this user
    private void listenToEmergencyMsg(Context context){

        Intent intent = new Intent(context, EmergencyMsgListener.class);
        context.startService(intent);
    }

    // clean up all the notifications
    private void notificationCleanUp(){
        NotificationManager manager =
                (NotificationManager) this.getSystemService(NOTIFICATION_SERVICE);

        manager.cancelAll();
    }

    @Override
    public void onDestroy() {

        // stop tracking network connection state
        if(connectivityReceiver != null) unregisterReceiver(connectivityReceiver);

        // stop tracking if emergency message sent to caregiver
        if(UserAppVersionController.getUserAppVersionController().getIsCaregiver())
            EmergencyMsgListener.stopListening();

        // clean up all the notifications
        notificationCleanUp();

        stopSelf();
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
