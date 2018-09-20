package com.example.sayyaf.homecare;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.example.sayyaf.homecare.notifications.EmergencyCallActivity;

/* This class is used for activity that can be accessed from
 * various other activities, allowing current activity to set
 * itself as the returning activity
 */
public class ActivityKeeper {

    private Class backPressActivityClass;

    // set up the return path before start the activity
    public ActivityKeeper(Class backPressActivityClass){
        this.backPressActivityClass = backPressActivityClass;
    }

    /*public Class getBackPressActivityClass() {
        return backPressActivityClass;
    }*/

    // return to previous activity
    public void returnToActivity(Context context){
        Intent intent = new Intent(context, backPressActivityClass);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
        ((Activity) context).finish();
    }
}
