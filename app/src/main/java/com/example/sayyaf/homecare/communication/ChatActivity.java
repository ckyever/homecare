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

import com.example.sayyaf.homecare.ImageLoader;
import com.example.sayyaf.homecare.MainActivity;
import com.example.sayyaf.homecare.R;
import com.example.sayyaf.homecare.accounts.User;
import com.example.sayyaf.homecare.accounts.UserAppVersionController;
import com.example.sayyaf.homecare.contacts.ContactChatActivity;
import com.example.sayyaf.homecare.notifications.EmergencyCallActivity;
import com.example.sayyaf.homecare.notifications.NetworkConnection;
import com.example.sayyaf.homecare.options.ProfileImageActivity;
import com.example.sayyaf.homecare.requests.RequestActivity;
import com.google.firebase.database.DatabaseReference;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/* This class has controller to handle display current chat messaging state
 * validate text input and send text messages (with image). It handles selecting image uri from device as well
 */
public class ChatActivity extends AppCompatActivity implements View.OnClickListener{

    private static ChatController chatController;
    private static boolean uploadComplete = true;
    private final int imageSelectReqCode = 6;

    private EditText textMsg;
    private TextView contactName;
    private ListView msgView;
    private Button sendMsg;
    private Button helpButton;

    private Button homeButton;
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
        homeButton = (Button) findViewById(R.id.optionMenu);
        selectImage = (ImageView) findViewById(R.id.selectImage);

        // activate help button on assisted person version
        UserAppVersionController.getUserAppVersionController().resetButton(helpButton);

        // show the chat peer name on the title bar
        chatController.displayReceiverName(contactName);

        // setup listener between chat database and the UI elements
        chatController.initialiseContentUpdateAdapter(this,
                R.layout.msg_block_sender, R.layout.msg_block,
                R.layout.img_msg_block_sender, R.layout.img_msg_block);

        // setup listener between chat database and user view
        chatController.setContentUpdateAdapter(msgView);

        // actively listen to chat messages updates
        chatController.listenToChatChanges();

    }

    // set image upload is completed
    public static void setUploadComplete(boolean isComplete){
        uploadComplete = isComplete;
    }

    // get the state of image upload
    public static boolean isUploading(){
        return !uploadComplete;
    }

    /* set up chat pair from the contact list
     * this_device: user of this device
     * contact_person: the user being selected from the contact list
     * chatDB: the 1 to 1 chat message storage
     */
    public static void setUpChatController(User this_device, User contact_person, DatabaseReference chatDB){
        chatController = new ChatController(this_device, contact_person, chatDB);
    }

    @Override
    public void onClick(View v) {

        if(v == selectImage){
            if(!uploadComplete){
                Toast.makeText(ChatActivity.this,
                        "Please wait until another image is uploaded," +
                                " you may still send text messages without images",
                        Toast.LENGTH_SHORT).show();

                return;
            }

            selectImage();
            return;
        }

        if(v == homeButton){
            goToMenu();
        }

        // block actions those require internet connection
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

            if(trimmedContent().length() == 0){
                Toast.makeText(ChatActivity.this, "Cannot send empty message",
                        Toast.LENGTH_SHORT).show();

                return;
            }

            if(imagePath != null){

                if(!uploadComplete){
                    showSendImgNotice("Please wait until another image is uploaded, " +
                            "you may still send text messages without images");
                    return;
                }

                chatController.sendMsg(textMsg, imagePath, ChatActivity.this);

                showSendImgNotice("Your message may not be sent instantly, " +
                        "depends on the upload time of the image");

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

    // Provide reason of image cannot be selected and clear out selection
    private void showSendImgNotice(String noticeMsg){
        Toast.makeText(ChatActivity.this, noticeMsg, Toast.LENGTH_SHORT).show();

        // reset selected image
        selectImage.setImageResource(R.drawable.ic_menu_gallery);
        imagePath = null;
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

                File file = new File(imagePath.getPath());

                try{

                    // get selected image file size
                    InputStream is = getContentResolver().openInputStream(imagePath);
                    getContentResolver().getType(imagePath);
                    int sizeInMB = is.available() / 1000000;

                    // limit the size of image that user can send (since there is the limit at firebase storage as well)
                    if(sizeInMB > 4){
                        showSendImgNotice("File size is too large, " +
                                "should be less than 5 MB");

                        return;
                    }

                    Bitmap bitmap = MediaStore
                            .Images
                            .Media
                            .getBitmap(getContentResolver(), imagePath);

                    // rescale display image on device
                    bitmap = ImageLoader.getImageLoader().bitMapScaling(bitmap, 500);

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

    // get rid of spaces before and after string
    private String trimmedContent(){
        return textMsg.getText().toString().trim();
    }

    @Override
    protected void onDestroy(){
        // end listening chat messages updates when leaving current page
        chatController.stopListenToChatChanges();
        super.onDestroy();
    }

    public void goToMenu(){
        // back to menu page
        Intent goToMenu = new Intent(ChatActivity.this, MainActivity.class);
        goToMenu.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(goToMenu);
        finish();
    }

    public void goToContact(){
        // back to contact page
        Intent goToContact = new Intent(ChatActivity.this, ContactChatActivity.class);
        goToContact.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(goToContact);
        finish();
    }

    @Override
    public void onBackPressed() { goToContact(); }

}
