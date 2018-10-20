package com.example.sayyaf.homecare.requests;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.sayyaf.homecare.ImageLoader;
import com.example.sayyaf.homecare.R;
import com.example.sayyaf.homecare.accounts.User;
import com.example.sayyaf.homecare.notifications.NetworkConnection;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;


/** Class to handle adapting lists for use by ListView
 */
public class RequestUserListAdapter extends ArrayAdapter<User> {

    private Activity activity;
    private ArrayList<User> users;
    private User currentUser;
    private DatabaseReference ref;
    private Button acceptButton;
    private Button declineButton;

    private Context context;


    public RequestUserListAdapter(@NonNull Context context, int resource, ArrayList<User> users,
                                  User currentUser, DatabaseReference ref) {
        super(context, resource, users);
        this.context = context;
        this.users = users;
        this.activity = (Activity)context;
        this.currentUser = currentUser;
        this.ref = ref;
    }

    /* When in ListView, bind data of the user whose request is being shown in the current block
        to the elements of that block
     */
    @Override
    public View getView(int i, View v, ViewGroup viewGroup) {

        LayoutInflater inflater = (LayoutInflater) activity.getSystemService( Context.LAYOUT_INFLATER_SERVICE );

        v = inflater.inflate(R.layout.request_block, null);

        //getting view in row_data
        TextView username = (TextView) v.findViewById(R.id.usernameRequest);
        TextView contactEmail = (TextView) v.findViewById(R.id.contactEmailRequest);
        ImageView userRequestImage = (ImageView) v.findViewById(R.id.userRequestImage);

        acceptButton = (Button) v.findViewById(R.id.acceptButton);
        declineButton = (Button) v.findViewById(R.id.declineButton);

        String requestEmail = users.get(i).getEmail();
        String requestId = users.get(i).getId();
        String name = users.get(i).getName();

        // display name and email
        username.setText(name);
        contactEmail.setText(requestEmail);

        // set profile image if there is one
        if(!users.get(i).getProfileImage().equals("no Image")){
            ImageLoader.getImageLoader().loadContactImageToView(
                    context, userRequestImage, users.get(i).getProfileImage());
        }

        //On click, current user has chosen to accept friend request
        acceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // block actions those require internet connection
                if(!NetworkConnection.getConnection()){
                    NetworkConnection.requestNetworkConnection(context);
                    return;
                }

                RequestController.acceptRequest(ref, users.get(i), currentUser);
                refreshView();
            }
        });

        //On click, current user has chosen to decline friend request
        declineButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // block actions those require internet connection
                if(!NetworkConnection.getConnection()){
                    NetworkConnection.requestNetworkConnection(context);
                    return;
                }

                RequestController.declineRequest(ref, users.get(i), currentUser);
                refreshView();
            }
        });
        return v;
    }


    private void refreshView() {
        Intent intent = new Intent(activity, RequestActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        activity.startActivity(intent);
        activity.finish();
    }

}
