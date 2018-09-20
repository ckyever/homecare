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

    private static final int connectionID = 20;

    private boolean connection;

    /*public NetworkConnection(boolean connection){
        this.connection = connection;
    }*/

    // default as not connected
    public NetworkConnection() { connection = false; }

    public boolean getConnection(){ return connection; }

    @Override
    public void onReceive(Context context, Intent intent) {

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

        // detect state change
        if(current_connection_state != connection){

            String state, content;
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

            // inform user about the connection change
            showStateChange(context, state, content, color);

            // set to detected connection state
            connection = current_connection_state;

        }

    }

    /* inform user about the connection change
     * state: either connected or not connected to the internet
     * content: description of services under current connection state
     */
    private void showStateChange(Context context, String state, String content, int color){
        NotificationCompat.Builder notificationbulider = null;

        notificationbulider =
                new NotificationCompat.Builder(context,
                        NotificationChannels.getChatNotificationCH())
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

    }

}
