package com.example.sayyaf.homecare.notifications;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

public class NotificationChannels extends Application {
    private static String chatNotificationCH = "chat notification";
    private static String foregroundCH = "foreground keeper";
    private static String emergencyCH = "emergency notification";

    @Override
    public void onCreate(){
        super.onCreate();
        createNotificationChannels();
    }

    public static String getChatNotificationCH(){
        return chatNotificationCH;
    }
    public static String getForegroundCH() { return foregroundCH; }
    public static String getEmergencyCH(){ return emergencyCH; }

    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            NotificationChannel channel1 = new NotificationChannel(
                    chatNotificationCH,
                    "CHAT",
                    NotificationManager.IMPORTANCE_HIGH);

            channel1.setDescription("This is a chat notification channal");

            NotificationChannel channel3 = new NotificationChannel(
                    foregroundCH,
                    "KEEP",
                    NotificationManager.IMPORTANCE_LOW);

            channel3.setDescription("This is a foreground keeper");

            NotificationChannel channel2 = new NotificationChannel(
                    emergencyCH,
                    "EMERGENCY",
                    NotificationManager.IMPORTANCE_HIGH);

            channel2.setDescription("This is an emergency notification channal");

            channel2.enableLights(true);
            channel2.setLightColor(0xffff0000);

            channel2.setLockscreenVisibility(NotificationCompat.VISIBILITY_PUBLIC);
            channel2.enableVibration(true);

            NotificationManager manager =
                    (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

            manager.createNotificationChannel(channel1);
            manager.createNotificationChannel(channel2);
            manager.createNotificationChannel(channel3);

        }
    }
}
