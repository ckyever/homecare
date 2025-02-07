package com.example.sayyaf.homecare.accounts;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Button;

import com.example.sayyaf.homecare.MainActivity;
import com.example.sayyaf.homecare.mapping.MapsActivity;
import com.example.sayyaf.homecare.mapping.TrackingActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

// This class store user details (persistent details) under logged in life time
public class UserAppVersionController {

    private static UserAppVersionController instance = null;
    private String userId;
    private boolean isCaregiver;

    /* new controller creation
     *  (the default empty user id used for clear up login state under any device crashes)
     */
    private UserAppVersionController(){
        userId = "";
        isCaregiver = true;
    }

    public static UserAppVersionController getUserAppVersionController(){
        if(instance == null){
            instance = new UserAppVersionController();
        }

        return instance;
    }

    /* set up user details (under login authentication)
     * userId: string ID get from FirebaseAuth
     * isCaregiver: user type, used for select interface version and service type
     */
    public void setUser(String userId, boolean isCaregiver){
        this.userId = userId;
        this.isCaregiver = isCaregiver;
    }

    // get user type of current user
    public boolean getIsCaregiver(){
        return isCaregiver;
    }

    // get user id of current user
    public String getCurrentUserId(){
        return userId;
    }

    /* enable help buttons on assisted person version
     * helpButton: the help button of the activity
     */
    public void resetButton(Button helpButton){
        if(!isCaregiver){
            helpButton.setVisibility(View.VISIBLE);
            helpButton.setEnabled(true);
        }
    }

    /* enable help buttons and provide a correct version of map page on assisted person version
     * helpButton: the help button of the activity
     * mMapButton: either "Tracking" (for caregiver version) or "Map" (for assisted person version)
     */
    public void resetButton(Button helpButton, Button mMapButton){
        if(!isCaregiver){
            mMapButton.setText("Map");
            helpButton.setVisibility(View.VISIBLE);
            helpButton.setEnabled(true);
        }
    }

    /* launch correct version of map activity for both assisted person and caregiver
     * mainActivity: the main page of the app, allow user to access other features
     */
    public void launchMapActivity(Context mainActivity){
        if(isCaregiver){
            Intent intent = new Intent(mainActivity, TrackingActivity.class);
            mainActivity.startActivity(intent);
        }
        else{
            Intent intent = new Intent(mainActivity, MapsActivity.class);
            mainActivity.startActivity(intent);
        }
    }

}
