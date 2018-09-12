package com.example.sayyaf.homecare.requests;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.example.sayyaf.homecare.R;
import com.example.sayyaf.homecare.accounts.User;
import com.example.sayyaf.homecare.accounts.User;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;

public class RequestUserListAdapter extends ArrayAdapter<User> {

    private Activity activity;
    private ArrayList<User> users;
    private User currentUser;
    private DatabaseReference ref;
    private Button acceptButton;
    private Button declineButton;


    public RequestUserListAdapter(@NonNull Context context, int resource, ArrayList<User> users,
                                  User currentUser, DatabaseReference ref) {
        super(context, resource, users);
        this.users = users;
        this.activity = (Activity)context;
        this.currentUser = currentUser;
        this.ref = ref;
    }

    public void addUser(ArrayList<User> users){
        this.users = users;
        notifyDataSetChanged();
    }

    @Override
    public View getView(int i, View v, ViewGroup viewGroup) {

        LayoutInflater inflater = (LayoutInflater) activity.getSystemService( Context.LAYOUT_INFLATER_SERVICE );

        v = inflater.inflate(R.layout.request_block, null);
        //getting view in row_data
        TextView username = (TextView) v.findViewById(R.id.usernameRequest);
        TextView contactEmail = (TextView) v.findViewById(R.id.contactEmailRequest);
        acceptButton = (Button) v.findViewById(R.id.acceptButton);
        declineButton = (Button) v.findViewById(R.id.declineButton);

        String requestEmail = users.get(i).getEmail();
        String requestId = users.get(i).getId();
        String name = users.get(i).getName();
        username.setText(name);
        contactEmail.setText(requestEmail);

        acceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RequestController.acceptRequest(ref, requestEmail, requestId, currentUser);
                refreshView();
            }
        });

        declineButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RequestController.declineRequest(ref, requestId, currentUser);
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
