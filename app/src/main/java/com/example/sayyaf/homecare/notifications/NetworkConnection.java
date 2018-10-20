package com.example.sayyaf.homecare.notifications;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import com.example.sayyaf.homecare.MainActivity;
import com.example.sayyaf.homecare.R;

import java.io.IOException;
import java.net.InetAddress;

import static android.content.Context.NOTIFICATION_SERVICE;

/* This class keep track on whether user's device is connected to the internet
 * https://stackoverflow.com/questions/6169059/android-event-for-internet-connectivity-state-change
 */
public class NetworkConnection extends BroadcastReceiver {

    private static final int connectionID = 20;

    private final String STATE_CONNECTED = "HomeCare is connected to the internet";
    private final String STATE_NOT_CONNECTED = "HomeCare is not connected to the internet";

    private final String CONTENT_CONNECTED = "Services are functioning";
    private final String CONTENT_NOT_CONNECTED = "Some of the services may not functioning";

    private final int COLOR_CONNECTED = 0xff00ff00;
    private final int COLOR_NOT_CONNECTED = 0xff0000ff;

    private static boolean connection;

    // default as not connected
    public NetworkConnection() { connection = false; }

    public static boolean getConnection(){ return connection; }

    @Override
    public void onReceive(Context context, Intent intent) {

        // default as not connected to the internet
        boolean current_connection_state = false;

        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();

        // device is connected to the internet
        if (activeNetwork != null && activeNetwork.isConnected())
            current_connection_state = true;

        // detect state change
        if(current_connection_state != connection){

            String state, content;
            int color;

            if(current_connection_state){
                state = STATE_CONNECTED;
                content = CONTENT_CONNECTED;
                color = COLOR_CONNECTED;
            }
            else{
                state = STATE_NOT_CONNECTED;
                content = CONTENT_NOT_CONNECTED;
                color = COLOR_NOT_CONNECTED;
            }

            // inform user about the connection change
            showStateChange(context, state, content, color);

            // set to detected connection state
            connection = current_connection_state;

        }

    }

    /* notification to inform user about the connection change
     * state: either connected or not connected to the internet
     * content: description of services under current connection state
     */
    private void showStateChange(Context context, String state, String content, int color){
        NotificationCompat.Builder notificationbulider = null;

        notificationbulider =
                new NotificationCompat.Builder(context,
                        NotificationChannels.getChatNotificationCH())
                        .setSmallIcon(R.drawable.ic_dialog_info)
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

    // block access to avoid crash or not synced content sent
    public static void requestNetworkConnection(Context context){
        Toast.makeText(context,
                "Wait for Network connection", Toast.LENGTH_SHORT).show();
    }

}
