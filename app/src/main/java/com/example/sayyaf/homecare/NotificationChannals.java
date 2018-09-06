package com.example.sayyaf.homecare;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

public class NotificationChannals extends Application {
    private static String chatNotificationCH = "chat notification";
    // private static String testForegroundCH = "foreground keeper";
    // private static String emergencyCH = "emergency notification";

    @Override
    public void onCreate(){
        super.onCreate();
        createNotificationChannels();
    }

    public static String getChatNotificationCH(){
        return chatNotificationCH;
    }
    //public static String getTestForegroundCH() { return testForegroundCH; }
    // public static String getEmergencyCH(){ return emergencyCH; }

    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            NotificationChannel channel1 = new NotificationChannel(
                    chatNotificationCH,
                    "CHAT",
                    NotificationManager.IMPORTANCE_HIGH);

            channel1.setDescription("This is a chat notification channal");

            /*NotificationChannel channel3 = new NotificationChannel(
                    testForegroundCH,
                    "KEEP",
                    NotificationManager.IMPORTANCE_HIGH);

            channel3.setDescription("This is a foreground keeper");*/

            NotificationManager manager =
                    (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

            manager.createNotificationChannel(channel1);
            //manager.createNotificationChannel(channel3);


            /*NotificationChannel channel2 = new NotificationChannel(
                    chatNotificationCH,
                    "EMERGENCY",
                    NotificationManager.IMPORTANCE_HIGH);

            channel2.setDescription("This is an emergency notification channal");

            NotificationManager manager =
                    (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            manager.createNotificationChannel(channel2);*/

        }
    }
}
