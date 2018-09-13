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

import com.example.sayyaf.homecare.R;

public class NotificationService extends Service {

    private boolean connection = false;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        /*BroadcastReceiver connectivityreceiver = new NetworkConnection(connection);

        registerReceiver(connectivityreceiver, new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));

        connection = ((NetworkConnection) connectivityreceiver).getConnection();*/

        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        serviceKeeper();
        internetStateMonitor();

    }

    private void serviceKeeper(){
        NotificationCompat.Builder notificationbulider = null;

        notificationbulider =
                new NotificationCompat.Builder(this, NotificationChannels.getTestForegroundCH())
                        .setSmallIcon(R.drawable.ic_launcher_background)
                        .setContentTitle("Welcome to HomeCare")
                        .setContentText("You have logged in")
                        .setDefaults(Notification.DEFAULT_ALL)
                        .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                        .setOngoing(true);

        startForeground(3, notificationbulider.build());
    }

    private void internetStateMonitor(){
        BroadcastReceiver connectivityreceiver = new NetworkConnection(connection);

        registerReceiver(connectivityreceiver,
                new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));

        connection = ((NetworkConnection) connectivityreceiver).getConnection();
    }

    public void onDestroy() {
        //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //stopForeground(true); //true will remove notification
        //}
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
