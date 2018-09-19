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
import com.example.sayyaf.homecare.contacts.ContactChatActivity;

public class NotificationService extends Service {

    private boolean connection = false;
    private BroadcastReceiver connectivityreceiver;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        super.onStartCommand(intent, flags, startId);

        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        serviceKeeper();

        internetStateMonitor();
        // stopListenToEmergencyMsg(this);
        listenToEmergencyMsg(this);

    }


    private void serviceKeeper(){
        NotificationCompat.Builder notificationbulider = null;

        Intent intent = new Intent(this, ContactChatActivity.class);
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

    private void internetStateMonitor(){

        connectivityreceiver = new NetworkConnection(connection);

        registerReceiver(connectivityreceiver,
                new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));

        connection = ((NetworkConnection) connectivityreceiver).getConnection();
    }

    private void listenToEmergencyMsg(Context context){

        Intent intent = new Intent(context, EmergencyMsgListener.class);
        context.startService(intent);
    }

    private void stopListenToEmergencyMsg(Context context){
        Intent intent = new Intent(context, EmergencyMsgListener.class);
        context.stopService(intent);
    }

    @Override
    public void onDestroy() {

        if(connectivityreceiver != null){
            ((NetworkConnection) connectivityreceiver).cancelNotification(this);
            unregisterReceiver(connectivityreceiver);
        }

        stopListenToEmergencyMsg(this);
        EmergencyMsgListener.getEmergencyRef().removeEventListener(EmergencyMsgListener.getNotificationListener());

        NotificationManager manager =
                (NotificationManager) this.getSystemService(NOTIFICATION_SERVICE);

        manager.cancelAll();

        stopSelf();
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
