package com.example.sayyaf.homecare.notifications;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.app.NotificationCompat;

import com.example.sayyaf.homecare.R;

import static android.content.Context.NOTIFICATION_SERVICE;

public class NetworkConnection extends BroadcastReceiver {

    private static final int connectionID = 2;

    private boolean connection;

    public NetworkConnection(boolean connection){
        this.connection = connection;
    }

    public boolean getConnection(){ return connection; }

    public void cancelNotification(Context context){
        NotificationManager manager =
                (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);

        manager.cancel(connectionID);
    }

    public void onReceive(Context context, Intent intent) {
        /*String action = intent.getAction();

        if ("android.net.conn.CONNECTIVITY_CHANGE".equals(action)) {*/
        boolean current_connection_state;

        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        if (activeNetwork != null) {
            // connected to the internet
            current_connection_state = true;
        } else {
            // not connected to the internet
            current_connection_state = false;
        }

        // state changed
        if(current_connection_state != connection){

            String state;
            String content;
            int color;

            if(current_connection_state){
                state = "HomeCare is connected to the internet";
                content = "Services are functioning";
                color = 0xff00ff00;
            }
            else{
                state = "HomeCare is not connected to the internet";
                content = "Some of the services may not functioning";
                color = 0xff0000ff;
            }

            NotificationCompat.Builder notificationbulider = null;

            notificationbulider =
                    new NotificationCompat.Builder(context,
                            NotificationChannels.getTestForegroundCH())
                            .setSmallIcon(R.drawable.ic_launcher_background)
                            .setContentTitle(state)
                            .setContentText(content)
                            .setColor(color)
                            .setVisibility(Notification.VISIBILITY_PUBLIC)
                            .setDefaults(Notification.DEFAULT_ALL)
                            .setCategory(NotificationCompat.CATEGORY_MESSAGE);

            NotificationManager manager =
                    (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);

            manager.notify(connectionID, notificationbulider.build());

            connection = current_connection_state;
        }

        //}
    }

}
