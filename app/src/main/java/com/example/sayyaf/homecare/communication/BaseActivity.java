package com.example.sayyaf.homecare.communication;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.support.v4.app.ActivityCompat;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

/**
 * Acts as the base template for activities that need to use Sinch communication services
 */
public abstract class BaseActivity extends Activity implements ServiceConnection {

    private SinchService.SinchServiceInterface mSinchServiceInterface;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //binds the service to the activity
        bindService();
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN
                        | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                        | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                        | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                        | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
    }

    /**
     * Asynchronous method called when the service has become connected
     * Used to bind the SinchServiceInterface to the activity
     */
    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        if (SinchService.class.getName().equals(componentName.getClassName())) {
            mSinchServiceInterface = (SinchService.SinchServiceInterface) iBinder;
            onServiceConnected();
        }
    }

    /**
     * Asynchronous method called when the service has become disconnected
     * Used to unbind the SinchServiceInterface from the activity
     */
    @Override
    public void onServiceDisconnected(ComponentName componentName) {
        if (SinchService.class.getName().equals(componentName.getClassName())) {
            mSinchServiceInterface = null;
            onServiceDisconnected();
        }
    }


    //Allows for customised behaviour in subclasses
    protected void onServiceConnected() {
    }

    //Allows for customised behaviour in subclasses
    protected void onServiceDisconnected() {
    }

    protected SinchService.SinchServiceInterface getSinchServiceInterface() {
        return mSinchServiceInterface;
    }



    //Initialises the Sinch messenger backend
    private Messenger messenger = new Messenger(new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SinchService.MESSAGE_PERMISSIONS_NEEDED:
                    Bundle bundle = msg.getData();
                    String requiredPermission = bundle.getString(SinchService.REQUIRED_PERMISSION);
                    ActivityCompat.requestPermissions(BaseActivity.this, new String[]{requiredPermission}, 0);
                    break;
            }
        }
    });


    /**
     * Requests required permissions from the user
     * @param requestCode
     * @param permissions Permissions Required
     * @param grantResults Permissions Granted
     */
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        boolean granted = grantResults.length > 0;
        for (int grantResult : grantResults) {
            granted &= grantResult == PackageManager.PERMISSION_GRANTED;
        }
        if (granted) {
            Toast.makeText(this, "You may now place a call", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "This application needs permission to use your microphone and camera to function properly.", Toast.LENGTH_LONG).show();
        }
        //try to restart service after permissions granted
        mSinchServiceInterface.retryStartAfterPermissionGranted();
    }

    /**
     * Used to bind the service to the Activity
     */
    private void bindService() {
        Intent serviceIntent = new Intent(this, SinchService.class);
        serviceIntent.putExtra(SinchService.MESSENGER, messenger);
        getApplicationContext().bindService(serviceIntent, this, BIND_AUTO_CREATE);
    }

}