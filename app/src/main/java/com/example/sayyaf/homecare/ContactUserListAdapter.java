package com.example.sayyaf.homecare;

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

import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.List;

public class ContactUserListAdapter extends ArrayAdapter<User>{

    private Activity activity;
    private ArrayList<User> users;
    private User this_device;
    private DatabaseReference ref;

    public ContactUserListAdapter(@NonNull Context context, int resource, ArrayList<User> users,
                                  User this_device, DatabaseReference ref) {
        super(context, resource, users);
        //users = new ArrayList<User>();
        this.users = users;
        this.activity = (Activity)context;
        this.this_device = this_device;
        this.ref = ref;
    }

    public void addUser(User user){
        users.add(user);
    }

    public void addUser(ArrayList<User> users){
        this.users = users;
    }

    @Override
    public View getView(int i, View v, ViewGroup viewGroup) {

        LayoutInflater inflater = (LayoutInflater) activity.getSystemService( Context.LAYOUT_INFLATER_SERVICE );

        v = inflater.inflate(R.layout.contact_block, null);

        //getting view in row_data
        TextView username = (TextView) v.findViewById(R.id.username);
        TextView contactEmail = (TextView) v.findViewById(R.id.contactEmail);
        Button chatButton = (Button) v.findViewById(R.id.chatButton);

        username.setText(users.get(i).getName());
        contactEmail.setText(users.get(i).getEmail());
        chatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String chatDB = users.get(i).getChatDatabase().get(this_device.getId());

                ChatActivity.setUpChatController(this_device, users.get(i), ref.child("chatDB").child(chatDB));

                Intent intent = new Intent(activity, ChatActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                activity.startActivity(intent);
                activity.finish();
            }
        });

        return v;



    }
}
