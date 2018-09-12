package com.example.sayyaf.homecare.notifications;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

public class NotificationService extends Service {

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        /*NotificationCompat.Builder notificationbulider = null;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationbulider =
                    new NotificationCompat.Builder(this, NotificationChannals.getTestForegroundCH())
                            .setSmallIcon(R.drawable.ic_launcher_background)
                            .setContentTitle("background keeper")
                            .setContentText("")
                            .setDefaults(Notification.DEFAULT_ALL)
                            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                            .setOngoing(true);
        }
        else{
            notificationbulider =
                    new NotificationCompat.Builder(this)
                            .setSmallIcon(R.drawable.ic_launcher_background)
                            .setContentTitle("background keeper")
                            .setContentText("")
                            .setDefaults(Notification.DEFAULT_ALL)
                            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                            .setOngoing(true);
        }

        ChatActivity.listenToAdded(this, (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE));

        startForeground(3, notificationbulider.build());*/
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
