package com.example.sayyaf.homecare;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

public class NotificationChannals extends Application {
    private static String notificationCH = "chat notification";

    @Override
    public void onCreate(){
        super.onCreate();
        createNotificationChannels();
    }

    public static String getNotificationCH(){
        return notificationCH;
    }

    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel1 = new NotificationChannel(
                    notificationCH,
                    "CH 1",
                    NotificationManager.IMPORTANCE_HIGH);

            channel1.setDescription("This is CH 1");

            NotificationManager manager =
                    (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            manager.createNotificationChannel(channel1);
        }
    }
}
