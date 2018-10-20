package com.example.sayyaf.homecare.contacts;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sayyaf.homecare.MainActivity;
import com.example.sayyaf.homecare.R;
import com.example.sayyaf.homecare.accounts.User;
import com.example.sayyaf.homecare.accounts.UserAppVersionController;
import com.example.sayyaf.homecare.communication.BaseActivity;
import com.example.sayyaf.homecare.notifications.EmergencyCallActivity;
import com.example.sayyaf.homecare.notifications.NetworkConnection;
import com.example.sayyaf.homecare.requests.RequestActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

/* This class provide a list of contact added as friends
 * and communication actions (video, voice calling, text chatting) with a specific contact person
 */
public class ContactChatActivity extends BaseActivity implements View.OnClickListener, ContactUserListCallback {

    private DatabaseReference ref;
    private User this_device;

    private EditText textInputs;
    private Button searchUser;
    private Button refreshList;
    private Button helpButton;

    private Button homeButton;

    private ProgressBar progressBar;
    private TextView progressBarMsg;

    private ArrayList<User> friends;
    private ListView contactView;
    private ContactUserListAdapter contactUserListAdapter;
    public static final String TAG = ContactChatActivity.class.getSimpleName();


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);

        // look for the UI elements
        textInputs = (EditText) findViewById(R.id.textInputs);
        searchUser = (Button) findViewById(R.id.searchUser);
        refreshList = (Button) findViewById(R.id.refreshList);
        helpButton = (Button) findViewById(R.id.optionHelp);

        homeButton = (Button) findViewById(R.id.optionMenu);

        contactView = (ListView) findViewById(R.id.contactView);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBarMsg = (TextView) findViewById(R.id.progressBarMsg);

        // activate help button on assisted person version
        UserAppVersionController.getUserAppVersionController().resetButton(helpButton);

        ref = FirebaseDatabase.getInstance().getReference();
        friends = new ArrayList<User>();

        // get all added contacts
        showProgress();
        getAllFriends();
    }

    @Override
    public void onClick(View v) {

        String starter = textInputs.getText().toString().trim();

        if(v == homeButton){
            goToMenu();
        }

        // block actions those require internet connection
        if(!NetworkConnection.getConnection()){
            NetworkConnection.requestNetworkConnection(ContactChatActivity.this);
            showProgress();
            return;
        }

        if(v != textInputs){
            // hide keyboard
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        }

        // search user by name or email (added contacts)
        if(v == searchUser){

            // ignore empty input
            if(!validateInput(starter)) return;

            showProgress();
            getFriends(starter, true);
        }

        // refresh the contact listing (show all added contacts)
        if(v == refreshList){
            showProgress();
            getAllFriends();
        }

        if(v == helpButton){
            EmergencyCallActivity.setBackToActivity(ContactChatActivity.class);

            Intent intent = new Intent(ContactChatActivity.this, EmergencyCallActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }

    }

    // debug getting data
    public void showFriends(){
        if(friends == null){
            System.out.println("null");
        }
        else if(friends.isEmpty()) {
            System.out.println("empty");
        }
        else{
            for(User u : friends) System.out.println(u.getName());
        }
    }

    // get all added contacts
    public void getAllFriends(){
        Query query = ref.child("User").orderByChild("id")
                .equalTo(UserAppVersionController.getUserAppVersionController().getCurrentUserId());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot datasnapshot) {
                for (DataSnapshot s : datasnapshot.getChildren()) {
                    if (s.exists()) {
                        this_device = s.getValue(User.class);
                        onCurrentUserCallback(this_device, null, false);
                    }
                }

            }
            @Override
            public void onCancelled(DatabaseError arg0) {

            }
        });
    }

    /* get added contacts base on the search
     * contactUserListCallback: interface to do works until database fetching complete
     * starter: starting letters or username or email (show all friends if it is null)
     * showResult: showing the query result from matching the starter
     */
    public void getFriends(String starter, boolean showResult){

        Query query = ref.child("User").orderByChild("id")
                .equalTo(UserAppVersionController.getUserAppVersionController().getCurrentUserId());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot datasnapshot) {
                for (DataSnapshot s : datasnapshot.getChildren()) {
                    if (s.exists()) {
                        this_device = s.getValue(User.class);
                        onCurrentUserCallback(this_device, starter, showResult);
                    }
                }

            }
            @Override
            public void onCancelled(DatabaseError arg0) {

            }
        });
    }

    @Override
    public void onCurrentUserCallback(User this_device, String starter, boolean showResult){
        ref.child("User").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){

                    friends = new ArrayList<User>();

                    for (DataSnapshot s : dataSnapshot.getChildren()) {
                        if(s.exists()){
                            User fd = s.getValue(User.class);

                            if (fd.getChatDatabase() != null && !fd.getChatDatabase().isEmpty()){
                                if(fd.getChatDatabase().containsKey(this_device.getId())){
                                    if(starter == null)
                                        // add all friends
                                        friends.add(fd);
                                    else if(matchStartingLetters(fd.getName(), fd.getEmail(), starter))
                                        // add friend matches query
                                        friends.add(fd);
                                }
                            }
                        }

                    }

                    // no friends match the starting input letters
                    if(friends.isEmpty() && showResult){
                        Toast.makeText(ContactChatActivity.this,
                                "No result",
                                Toast.LENGTH_SHORT).show();
                    }

                    onContactsCallback(friends);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onContactsCallback(ArrayList<User> friends) {
        resetView(friends);
    }

    // reset view after database query
    private void resetView(ArrayList<User> friends){
        contactUserListAdapter =
                new ContactUserListAdapter(ContactChatActivity.this, R.layout.contact_block,
                        friends, this_device, ref, getSinchServiceInterface());

        contactView.setAdapter(contactUserListAdapter);
        endProgress();
    }

    // show either upload or download progress
    private void showProgress(){
        progressBar.setVisibility(View.VISIBLE);
        progressBarMsg.setVisibility(View.VISIBLE);
    }

    // remove progress bar after finish
    private void endProgress(){
        progressBar.setVisibility(View.GONE);
        progressBarMsg.setVisibility(View.GONE);
    }

    // check input is valid
    private boolean validateInput(String input){
        return !input.trim().isEmpty();
    }

    /* check input match starting letters of username or email
     * username: friend's username
     * email: friend's email
     * starter: search text
     */
    private boolean matchStartingLetters(String username, String email, String starter){
        return username.toLowerCase().startsWith(starter.toLowerCase())
                || email.toLowerCase().startsWith(starter.toLowerCase());
    }

    private void goToMenu(){
        // back to menu page
        Intent goToMenu = new Intent(ContactChatActivity.this, MainActivity.class);
        goToMenu.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(goToMenu);
        finish();
    }

    @Override
    public void onBackPressed() {
        goToMenu();
    }

}
