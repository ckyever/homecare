package com.example.sayyaf.homecare.communication;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.example.sayyaf.homecare.accounts.UserAppVersionController;
import com.example.sayyaf.homecare.contacts.ContactChatActivity;
import com.example.sayyaf.homecare.R;
import com.example.sayyaf.homecare.accounts.User;
import com.example.sayyaf.homecare.options.ProfileImageActivity;
import com.firebase.ui.database.FirebaseListAdapter;
import com.firebase.ui.database.FirebaseListOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.Date;

public class ChatController {

    private User this_device, contact_person;
    private DatabaseReference chatDB;
    private FirebaseListAdapter<ChatMessage> contentUpdateAdapter;

    /*  set up chat control for user-contact person pair (on user adding)
     *  this_device: user of this device
     *  contact_person: chosen contact person to the user
     *  chatDB: database for the chatting pair
     */
    public ChatController(User this_device, User contact_person, DatabaseReference chatDB){

        this.this_device = this_device;
        this.contact_person = contact_person;
        this.chatDB = chatDB;

    }

    /*  initialise ContentUpdateAdapter from database contents
     *  msg_block_this_device: message block for contents are send by this user
     *  msg_block_contact_person: message block for contents are send by your peer
     */
    public void initialiseContentUpdateAdapter(
            ChatActivity chatActivity,
            int msg_block_this_device, int msg_block_contact_person,
            int img_msg_block_this_device, int img_msg_block_contact_person){

        FirebaseListOptions<ChatMessage> options = new FirebaseListOptions.Builder<ChatMessage>()
                .setQuery(chatDB, ChatMessage.class).setLayout(msg_block_this_device).build();

        contentUpdateAdapter = new FirebaseListAdapter<ChatMessage>(options){
            @Override
            public View getView(int position, View v, ViewGroup viewGroup) {

                ChatMessage ct = this.getItem(position);

                if(ct.getMessageSender().equals(this_device.getName())){

                    // use UI element represent the sent is from this device
                    if(ct.getImageSource().equals("no Image")){
                        v = LayoutInflater.from(chatActivity.getApplicationContext()).
                                inflate(msg_block_this_device, viewGroup, false);
                    }
                    else{
                        v = LayoutInflater.from(chatActivity.getApplicationContext()).
                                inflate(img_msg_block_this_device, viewGroup, false);

                        ProgressBar progressBar = (ProgressBar) v.findViewById(R.id.progressBar);
                        TextView progressBarMsg = (TextView) v.findViewById(R.id.progressBarMsg);

                        showProgress(progressBar, progressBarMsg);

                        ImageView imageSrc = (ImageView) v.findViewById(R.id.imageSrc);

                        // Set image
                        loadImageToView(chatActivity, ct.getImageSource(), imageSrc,
                                progressBar, progressBarMsg);
                    }

                }
                else{

                    // use UI element represent the sent is from the other user
                    if(ct.getImageSource().equals("no Image")){
                        v = LayoutInflater.from(chatActivity.getApplicationContext()).
                                inflate(msg_block_contact_person, viewGroup, false);
                    }
                    else {
                        v = LayoutInflater.from(chatActivity.getApplicationContext()).
                                inflate(img_msg_block_contact_person, viewGroup, false);

                        ProgressBar progressBar = (ProgressBar) v.findViewById(R.id.progressBar);
                        TextView progressBarMsg = (TextView) v.findViewById(R.id.progressBarMsg);

                        showProgress(progressBar, progressBarMsg);

                        ImageView imageSrc = (ImageView) v.findViewById(R.id.imageSrc);

                        // Set image
                        loadImageToView(chatActivity, ct.getImageSource(), imageSrc,
                                progressBar, progressBarMsg);
                    }

                }

                // Call out to subclass to marshall this model into the provided view
                populateView(v, ct, position);
                return v;
            }

            @Override
            protected void populateView(View v, ChatMessage ct, int position) {

                // referencing text views
                TextView messageText = (TextView) v.findViewById(R.id.messageText);
                TextView messageUser = (TextView) v.findViewById(R.id.username);
                TextView messageTime = (TextView) v.findViewById(R.id.sendTime);

                // Set their text
                messageText.setText(ct.getMessageText());
                messageUser.setText(ct.getMessageSender());

                // Format the date before showing it
                messageTime.setText(DateFormat.format("d/M/yy (h:mm a)",
                        ct.getMessageTime()));
            }

        };
    }

    private void loadImageToView(ChatActivity chatActivity,
                                 String profileImageUri, ImageView imageSrc,
                                 ProgressBar progressBar, TextView progressBarMsg){

        Glide.with(chatActivity.getApplicationContext())
                .load(profileImageUri)
                .apply(new RequestOptions()
                        .fitCenter()
                        .dontAnimate())
                .listener(new RequestListener<Drawable>(){

                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model,
                                                Target<Drawable> target, boolean isFirstResource) {

                        Toast.makeText(chatActivity,
                                "Unable to load the profile image",
                                Toast.LENGTH_SHORT).show();

                        endProgress(progressBar, progressBarMsg);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model,
                                                   Target<Drawable> target,
                                                   DataSource dataSource, boolean isFirstResource) {

                        endProgress(progressBar, progressBarMsg);
                        return false;
                    }
                })
                .into(imageSrc);
    }

    // show either upload or download progress
    private void showProgress(ProgressBar progressBar, TextView progressBarMsg){
        progressBar.setVisibility(View.VISIBLE);
        progressBarMsg.setVisibility(View.VISIBLE);
    }

    // remove progress bar after finish
    private void endProgress(ProgressBar progressBar, TextView progressBarMsg){
        progressBar.setVisibility(View.GONE);
        progressBarMsg.setVisibility(View.GONE);
    }

    // set ContentUpdateAdapter for the view
    public void setContentUpdateAdapter(ListView msgView){
        msgView.setAdapter(contentUpdateAdapter);
    }

    // start listen to the chat database (for updating view)
    public void listenToChatChanges(){
        contentUpdateAdapter.startListening();
    }

    // stop listen to the chat database (not updating view)
    public void stopListenToChatChanges(){
        contentUpdateAdapter.stopListening();
    }

    /*  post message to the chat database
     *  textMsg: holds text input
     */
    public void sendMsg(EditText textMsg){
        // handle click on sending when there is non-space texts
        if(vaildateMsgContent(textMsg)){

            // upload msg to db
            ChatMessage ct = new ChatMessage(trimmedContent(textMsg),
                    this_device.getName());

            chatDB.push().setValue(ct);

            cleanup(textMsg);

        }
    }

    /*  post message to the chat database
     *  textMsg: holds text input
     */
    public void sendMsg(EditText textMsg, Uri imagePath, Context context){
        // handle click on sending when there is non-space texts
        if(vaildateMsgContent(textMsg)){

            if(imagePath != null){

                ChatActivity.setUploadComplete(false);

                String fileName = this_device.getId() + new Date().getTime() + contact_person.getId();

                StorageReference storageRef = FirebaseStorage.getInstance()
                        .getReference("chatDB").child(fileName);

                // try to upload image
                storageRef.putFile(imagePath)
                        .addOnCompleteListener(onUploadCompleteAction(context,
                                fileName, trimmedContent(textMsg)))
                        .addOnFailureListener(onUploadFailureAction(context));
            }

            cleanup(textMsg);

        }
    }

    // Upload success into Storage
    private OnCompleteListener<UploadTask.TaskSnapshot> onUploadCompleteAction(
            Context context, String fileName, String messageText){

        return new OnCompleteListener<UploadTask.TaskSnapshot>(){
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                FirebaseStorage.getInstance()
                        .getReference("chatDB")
                        .child(fileName)
                        .getDownloadUrl()
                        .addOnSuccessListener(onUploadedUriSuccess(context, messageText))
                        .addOnFailureListener(onUploadedUriFailure(context));

            }
        };
    }

    // Upload failed
    private OnFailureListener onUploadFailureAction(Context context){

        return new OnFailureListener(){
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context, "Failed to upload image",
                        Toast.LENGTH_SHORT).show();

                ChatActivity.setUploadComplete(true);
            }
        };

    }

    // get reference to the image link
    private OnSuccessListener<Uri> onUploadedUriSuccess(Context context, String messageText){
        return new OnSuccessListener<Uri>(){
            @Override
            public void onSuccess(Uri imagePath) {

                // upload msg to db
                ChatMessage ct = new ChatMessage(messageText,
                        this_device.getName(), imagePath.toString());

                chatDB.push().setValue(ct);

                ChatActivity.setUploadComplete(true);

            }
        };
    }

    // unable to get the image link reference
    private OnFailureListener onUploadedUriFailure(Context context){
        return new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context, "Failed to uploaded image reference",
                        Toast.LENGTH_SHORT).show();

                ChatActivity.setUploadComplete(true);
            }
        };
    }

    // get rid of spaces before and after string
    private String trimmedContent(EditText textMsg){
        return textMsg.getText().toString().trim();
    }

    // return true for a non empty string
    private boolean vaildateMsgContent(EditText textMsg){
        // handle click on sending when there is non-space texts
        return trimmedContent(textMsg).length() != 0;
    }

    // blank out input field
    private void cleanup(EditText textMsg){
        // clear out text message input
        textMsg.setText("");
    }

    // show peer's user name
    public void displayReceiverName(TextView contactName) {
        contactName.setText(contact_person.getName());
    }

    // move back to contct page
    public void returnToMenu(ChatActivity chatActivity){
        Intent goToMenu = new Intent(chatActivity, ContactChatActivity.class);
        chatActivity.startActivity(goToMenu);
        chatActivity.finish();
    }


}
