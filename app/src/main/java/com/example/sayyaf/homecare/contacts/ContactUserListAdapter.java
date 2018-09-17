package com.example.sayyaf.homecare.contacts;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sayyaf.homecare.R;
import com.example.sayyaf.homecare.accounts.User;
import com.example.sayyaf.homecare.communication.CallScreenActivity;
import com.example.sayyaf.homecare.communication.ChatActivity;
import com.example.sayyaf.homecare.communication.SinchService;
import com.google.firebase.database.DatabaseReference;
import com.sinch.android.rtc.MissingPermissionException;
import com.sinch.android.rtc.calling.Call;

import java.util.ArrayList;

public class ContactUserListAdapter extends ArrayAdapter<User>{

    private Activity activity;
    private ArrayList<User> users;
    private User this_device;
    private DatabaseReference ref;
    private SinchService.SinchServiceInterface sinchServiceInterface;
    private Context context;

    public ContactUserListAdapter(@NonNull Context context, int resource, ArrayList<User> users,
                                  User this_device, DatabaseReference ref) {
        super(context, resource, users);
        //users = new ArrayList<User>();
        this.users = users;
        this.activity = (Activity)context;
        this.this_device = this_device;
        this.ref = ref;
    }

    // not work probably due to database read behavior (will probably be removed)
    public void addUser(ArrayList<User> users){
        this.users = users;
        notifyDataSetChanged();
    }

    @Override
    public View getView(int i, View v, ViewGroup viewGroup) {

        // location the UI contact block
        LayoutInflater inflater = (LayoutInflater) activity.getSystemService( Context.LAYOUT_INFLATER_SERVICE );

        v = inflater.inflate(R.layout.contact_block, null);

        // mapping contact block with database
        TextView username = (TextView) v.findViewById(R.id.username);
        TextView contactEmail = (TextView) v.findViewById(R.id.contactEmail);
        Button chatButton = (Button) v.findViewById(R.id.chatButton);

        // display name and email
        username.setText(users.get(i).getName());
        contactEmail.setText(users.get(i).getEmail());

        // assoicate button to private chat page
        chatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String chatDB = users.get(i).getChatDatabase().get(this_device.getId());

                // look up the common chat room in chat activtity
                ChatActivity.setUpChatController(this_device, users.get(i), ref.child("chatDB").child(chatDB));

                // go to chat page
                Intent intent = new Intent(activity, ChatActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                activity.startActivity(intent);
                activity.finish();
            }
        });

        return v;


    }

    private void callButtonClicked(String username) {

        try {
            Call call = sinchServiceInterface.callUser(username);
            if (call == null) {
                // Service failed for some reason, show a Toast and abort
                Toast.makeText(context, "Service is not started. Try stopping the service and starting it again before "
                        + "placing a call.", Toast.LENGTH_LONG).show();
                return;
            }
            String callId = call.getCallId();
            Intent callScreen = new Intent(context, CallScreenActivity.class);
            callScreen.putExtra(SinchService.CALL_ID, callId);
            callScreen.putExtra("name", username);
            context.startActivity(callScreen);
        } catch (MissingPermissionException e) {
            //ActivityCompat.requestPermissions(context, new String[]{e.getRequiredPermission()}, 0);
        }

    }
}
