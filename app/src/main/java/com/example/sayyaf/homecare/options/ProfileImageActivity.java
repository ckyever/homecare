package com.example.sayyaf.homecare.options;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.example.sayyaf.homecare.MainActivity;
import com.example.sayyaf.homecare.R;
import com.example.sayyaf.homecare.accounts.UserAppVersionController;
import com.example.sayyaf.homecare.contacts.ContactChatActivity;
import com.example.sayyaf.homecare.communication.ChatActivity;
import com.example.sayyaf.homecare.notifications.EmergencyCallActivity;
import com.example.sayyaf.homecare.notifications.NetworkConnection;
import com.example.sayyaf.homecare.requests.RequestActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.io.InputStream;

public class ProfileImageActivity extends AppCompatActivity implements View.OnClickListener {

    private static boolean uploadComplete = true;
    private final int imageSelectReqCode = 5;

    private ProgressBar progressBar;
    private TextView progressBarMsg;

    private ImageView profileImage;

    private TextView selectImage;
    private FloatingActionButton selectImageButton;

    private TextView confirmChange;
    private FloatingActionButton confirmChangeButton;

    private Button helpButton;
    private Button homeButton;

    private Uri imagePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_photo);

        selectImage = (TextView) findViewById(R.id.selectImage);
        selectImageButton = (FloatingActionButton) findViewById(R.id.selectImageButton);

        confirmChange = (TextView) findViewById(R.id.confirmChange);
        confirmChangeButton = (FloatingActionButton) findViewById(R.id.confirmChangeButton);

        profileImage = (ImageView) findViewById(R.id.profileImage);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBarMsg = (TextView) findViewById(R.id.progressBarMsg);

        helpButton = (Button) findViewById(R.id.optionHelp);
        homeButton = (Button) findViewById(R.id.optionMenu);

        // activate help button on assisted person version
        UserAppVersionController.getUserAppVersionController().resetButton(helpButton);

        loadProfileImage();
    }

    public static boolean isUploading(){
        return !uploadComplete;
    }

    @Override
    public void onClick(View v) {

        // select image
        if(v == selectImage || v == selectImageButton){
            selectImage();
            return;
        }

        if(v == homeButton){
            goToMenu();
        }

        if(!NetworkConnection.getConnection()){
            NetworkConnection.requestNetworkConnection(ProfileImageActivity.this);
            return;
        }

        if(v == helpButton){
            EmergencyCallActivity.setBackToActivity(ProfileImageActivity.class);

            Intent intent = new Intent(ProfileImageActivity.this, EmergencyCallActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }

        // confirm change
        if(v == confirmChange || v == confirmChangeButton){
            uploadProfileImage();
        }

    }

    private void loadProfileImage(){

        Query profileImgRef = FirebaseDatabase.getInstance()
                .getReference("User")
                .child(UserAppVersionController.getUserAppVersionController()
                        .getCurrentUserId()).child("profileImage");

        profileImgRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    String profileImageUri = dataSnapshot.getValue(String.class);

                    System.out.println("May exist");

                    // load profile image if user have set
                    if(!profileImageUri.equals("no Image")){
                        System.out.println("Exist");

                        loadImageToView(profileImageUri);
                    }

                }
                else{
                    System.out.println("Not exist");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                endProgress();
            }
        });

    }

    private void loadImageToView(String profileImageUri){
        showProgress("Loading ...");

        Glide.with(getApplicationContext())
                .load(profileImageUri)
                .apply(new RequestOptions()
                        .dontAnimate()
                        .skipMemoryCache(true)
                        .error(R.mipmap.ic_launcher_round))
                .listener(new RequestListener<Drawable>(){

                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model,
                                                Target<Drawable> target, boolean isFirstResource) {

                        endProgress();
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model,
                                                   Target<Drawable> target,
                                                   DataSource dataSource, boolean isFirstResource) {
                        endProgress();
                        return false;
                    }
                })
                .into(profileImage);
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

        showProgress("Loading ...");

        if(reqCode == imageSelectReqCode && resCode == RESULT_OK
                && intent != null){

            imagePath = intent.getData();

            if(imagePath != null){

                try{

                    InputStream is = getContentResolver().openInputStream(imagePath);
                    getContentResolver().getType(imagePath);
                    int sizeInMB = is.available() / 1000000;

                    if(sizeInMB > 4){
                        Toast.makeText(ProfileImageActivity.this, "File size is too large, " +
                                        "should be less than 5 MB",
                                Toast.LENGTH_SHORT).show();

                        imagePath = null;
                        profileImage.setImageResource(R.mipmap.ic_launcher_round);
                        endProgress();
                        return;
                    }

                    Bitmap bitmap = MediaStore
                            .Images
                            .Media
                            .getBitmap(getContentResolver(), imagePath);

                    bitmap = bitMapScaling(bitmap, bitmap.getWidth(), bitmap.getHeight());

                    profileImage.setImageBitmap(bitmap);

                }
                catch (IOException e){
                    e.printStackTrace();
                }
            }
            else{
                Toast.makeText(ProfileImageActivity.this, "Cannot get image folder",
                        Toast.LENGTH_SHORT).show();
            }

        }

        endProgress();

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

    private void uploadProfileImage(){
        // check if image is selected
        if(imagePath != null){

            uploadComplete = false;

            StorageReference storageRef = FirebaseStorage.getInstance()
                    .getReference("UserProfileImage").child(
                            UserAppVersionController.getUserAppVersionController().getCurrentUserId());

            // try to upload image
            storageRef.putFile(imagePath)
                    .addOnCompleteListener(onUploadCompleteAction())
                    .addOnFailureListener(onUploadFailureAction());

            Toast.makeText(ProfileImageActivity.this, "Profile image is uploading",
                    Toast.LENGTH_SHORT).show();

            // do upload process in background (allow user to access other activities)
            returnToOptions();

        }
        else{
            Toast.makeText(ProfileImageActivity.this, "No image was selected",
                    Toast.LENGTH_SHORT).show();
        }
    }

    // Upload success into Storage
    private OnCompleteListener<UploadTask.TaskSnapshot> onUploadCompleteAction(){

        return new OnCompleteListener<UploadTask.TaskSnapshot>(){
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                if(!NetworkConnection.getConnection()){
                    Toast.makeText(ProfileImageActivity.this, "Cannot update profile image",
                            Toast.LENGTH_SHORT).show();

                    uploadComplete = true;
                    endProgress();
                    return;
                }

                FirebaseStorage.getInstance()
                        .getReference("UserProfileImage")
                        .child(UserAppVersionController.getUserAppVersionController().getCurrentUserId())
                        .getDownloadUrl()
                        .addOnSuccessListener(onUploadedUriSuccess())
                        .addOnFailureListener(onUploadedUriFailure());

            }
        };
    }

    // Upload failed
    private OnFailureListener onUploadFailureAction(){

        return new OnFailureListener(){
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ProfileImageActivity.this, "Cannot update profile image",
                        Toast.LENGTH_SHORT).show();

                uploadComplete = true;
                endProgress();
            }
        };

    }

    // get reference to the image link
    private OnSuccessListener<Uri> onUploadedUriSuccess(){
        return new OnSuccessListener<Uri>(){
                @Override
                public void onSuccess(Uri userImagePath) {

                    if(!NetworkConnection.getConnection()){
                        Toast.makeText(ProfileImageActivity.this, "Cannot update profile image",
                                Toast.LENGTH_SHORT).show();

                        uploadComplete = true;
                        endProgress();
                        return;
                    }

                    FirebaseDatabase.getInstance()
                            .getReference("User")
                            .child(UserAppVersionController
                                    .getUserAppVersionController().getCurrentUserId())
                            .child("profileImage").setValue(userImagePath.toString());

                    Toast.makeText(ProfileImageActivity.this, "Profile image is updated",
                            Toast.LENGTH_SHORT).show();

                    uploadComplete = true;
                    endProgress();
            }
        };
    }

    // unable to get the image link reference
    private OnFailureListener onUploadedUriFailure(){
        return new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ProfileImageActivity.this, "Cannot update profile image",
                        Toast.LENGTH_SHORT).show();

                uploadComplete = true;

                endProgress();
            }
        };
    }


    // show either upload or download progress
    private void showProgress(String message){
        progressBar.setVisibility(View.VISIBLE);
        progressBarMsg.setVisibility(View.VISIBLE);
        progressBarMsg.setText(message);
    }

    // remove progress bar after finish
    private void endProgress(){
        progressBar.setVisibility(View.GONE);
        progressBarMsg.setVisibility(View.GONE);
    }

    private void goToMenu(){
        // back to menu page
        Intent goToMenu = new Intent(ProfileImageActivity.this, MainActivity.class);
        goToMenu.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(goToMenu);
        finish();
    }

    @Override
    public void onBackPressed() {
        // back to options page
        returnToOptions();
    }

    private void returnToOptions(){
        Intent goToMenu = new Intent(ProfileImageActivity.this, OptionActivity.class);
        goToMenu.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(goToMenu);
        finish();
    }
}
