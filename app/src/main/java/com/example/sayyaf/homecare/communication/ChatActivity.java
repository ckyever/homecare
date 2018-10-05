package com.example.sayyaf.homecare.communication;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.Toast;

import com.example.sayyaf.homecare.R;
import com.example.sayyaf.homecare.accounts.User;
import com.example.sayyaf.homecare.accounts.UserAppVersionController;
import com.example.sayyaf.homecare.contacts.ContactChatActivity;
import com.example.sayyaf.homecare.notifications.EmergencyCallActivity;
import com.example.sayyaf.homecare.notifications.NetworkConnection;
import com.example.sayyaf.homecare.options.ProfileImageActivity;
import com.example.sayyaf.homecare.requests.RequestActivity;
import com.google.firebase.database.DatabaseReference;

import java.io.IOException;

public class ChatActivity extends AppCompatActivity implements View.OnClickListener{

    private static ChatController chatController;
    private static boolean uploadComplete = true;
    private final int imageSelectReqCode = 6;

    private EditText textMsg;
    private TextView contactName;
    private ListView msgView;
    private Button sendMsg;
    private Button helpButton;

    private ProgressBar progressBar;
    private TextView progressBarMsg;

    private ImageView selectImage;
    private Uri imagePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // look for the UI elements
        textMsg = (EditText) findViewById(R.id.textInputs);
        msgView = (ListView) findViewById(R.id.msgView);
        contactName = (TextView) findViewById(R.id.contactName);

        sendMsg = (Button) findViewById(R.id.sendMsg);
        helpButton = (Button) findViewById(R.id.optionHelp);

        selectImage = (ImageView) findViewById(R.id.selectImage);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBarMsg = (TextView) findViewById(R.id.progressBarMsg);

        // activate help button on assisted person version
        UserAppVersionController.getUserAppVersionController().resetButton(helpButton);

        // show the chat peer name
        chatController.displayReceiverName(contactName);

        // setup listener between chat database and the UI elements
        chatController.initialiseContentUpdateAdapter(this,
                R.layout.msg_block_sender, R.layout.msg_block,
                R.layout.img_msg_block_sender, R.layout.img_msg_block);

        // setup listener between chat database and user view
        chatController.setContentUpdateAdapter(msgView);

        // actively listen to chat database change
        chatController.listenToChatChanges();

    }

    // set image upload is completed
    public static void setUploadComplete(boolean isComplete){
        uploadComplete = isComplete;
    }

    // set up chat pair from the account list
    public static void setUpChatController(User this_device, User contact_person, DatabaseReference chatDB){
        chatController = new ChatController(this_device, contact_person, chatDB);
    }

    @Override
    public void onClick(View v) {

        if(v == selectImage){
            if(!uploadComplete){
                Toast.makeText(ChatActivity.this,
                        "Please wait until another image is uploaded," +
                                "you may still send text messages without images",
                        Toast.LENGTH_SHORT).show();

                return;
            }

            selectImage();
            return;
        }

        if(!NetworkConnection.getConnection()){
            NetworkConnection.requestNetworkConnection(ChatActivity.this);
            return;
        }

        if(v != textMsg){
            // hide keyboard
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        }

       if(v == sendMsg){
            if(imagePath != null){

                if(!uploadComplete){
                    Toast.makeText(ChatActivity.this,
                            "Please wait until another image is uploaded," +
                                    "you may still send text messages without images",
                            Toast.LENGTH_SHORT).show();

                    // reset selected image
                    selectImage.setImageResource(R.drawable.ic_menu_gallery);
                    imagePath = null;
                    return;
                }

                Toast.makeText(ChatActivity.this,
                        "Your message may not be sent instantly," +
                                "depends on the upload time of the image",
                        Toast.LENGTH_SHORT).show();
                chatController.sendMsg(textMsg, imagePath, ChatActivity.this);

                // reset selected image
                selectImage.setImageResource(R.drawable.ic_menu_gallery);
                imagePath = null;
                return;
            }

           chatController.sendMsg(textMsg);
       }

       if(v == helpButton){
            EmergencyCallActivity.setBackToActivity(ChatActivity.class);

            Intent intent = new Intent(ChatActivity.this, EmergencyCallActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
       }
    }

    private void selectImage(){
        Intent imageSelectIntent = new Intent();

        // get image from phone
        imageSelectIntent.setType("image/*");
        imageSelectIntent.setAction(Intent.ACTION_GET_CONTENT);

        startActivityForResult(imageSelectIntent, imageSelectReqCode);
    }

    // show selected image in view
    @Override
    protected void onActivityResult(int reqCode, int resCode, Intent intent){
        super.onActivityResult(reqCode, resCode, intent);

        if(reqCode == imageSelectReqCode && resCode == RESULT_OK
                && intent != null){

            imagePath = intent.getData();

            if(imagePath != null){

                try{
                    Bitmap bitmap = MediaStore
                            .Images
                            .Media
                            .getBitmap(getContentResolver(), imagePath);

                    bitmap = bitMapScaling(bitmap, bitmap.getWidth(), bitmap.getHeight());
                    selectImage.setImageBitmap(bitmap);

                }
                catch (IOException e){
                    e.printStackTrace();
                }
            }
            else{
                Toast.makeText(ChatActivity.this, "Cannot get image folder",
                        Toast.LENGTH_SHORT).show();
            }

        }

    }



    // lower resolution for performance
    private Bitmap bitMapScaling(Bitmap bitmap, int originalX, int originalY){
        // possible max size to display on phone
        int maxSize = 1000;

        // not need to resize if both sides are small
        if(originalX < maxSize && originalY < maxSize) return bitmap;

        int exportX;
        int exportY;

        // find the longest side
        if(originalX > originalY){
            exportX = maxSize;
            exportY = (originalY * maxSize) / originalX;
        }
        else{
            exportY = maxSize;
            exportX = (originalX * maxSize) / originalY;
        }

        return Bitmap.createScaledBitmap(bitmap, exportX, exportY, false);

    }

    @Override
    protected void onStart() {
        // listen to chat database changes and update the view
        super.onStart();
        chatController.listenToChatChanges();
    }

    @Override
    protected void onPause(){
        // stop listen to chat database and update view
        chatController.stopListenToChatChanges();
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        // go back to the contact page
        chatController.returnToMenu(this);
    }

}
