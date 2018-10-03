package com.example.sayyaf.homecare.options;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
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
import com.bumptech.glide.request.RequestOptions;
import com.example.sayyaf.homecare.MainActivity;
import com.example.sayyaf.homecare.R;
import com.example.sayyaf.homecare.accounts.UserAppVersionController;
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
                .child(UserAppVersionController.getUserAppVersionController().getCurrentUserId()).child("hasProfileImage");

        profileImgRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){

                    boolean hasProfileImage = dataSnapshot.getValue(Boolean.class);

                    if(hasProfileImage){
                        showProgress("Loading ...");

                        FirebaseStorage.getInstance()
                                .getReference("UserProfileImage")
                                .child(UserAppVersionController.getUserAppVersionController().getCurrentUserId())
                                .getDownloadUrl()
                                .addOnSuccessListener(onDownloadSuccess())
                                .addOnFailureListener(onDownloadFailure());
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                endProgress();
            }
        });

    }

    private void uploadProfileImage(){
        if(imagePath != null){

            uploadComplete = false;

            StorageReference storageRef = FirebaseStorage.getInstance()
                    .getReference("UserProfileImage").child(
                            UserAppVersionController.getUserAppVersionController().getCurrentUserId());

            storageRef.putFile(imagePath)
                    .addOnCompleteListener(onUploadCompleteAction())
                    .addOnFailureListener(onUploadFailureAction());


            Toast.makeText(ProfileImageActivity.this, "Profile image is uploading",
                    Toast.LENGTH_SHORT).show();

            returnToOptions();

        }
        else{
            Toast.makeText(ProfileImageActivity.this, "No image was selected",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void selectImage(){
        Intent imageSelectIntent = new Intent();
        imageSelectIntent.setType("image/*");
        imageSelectIntent.setAction(Intent.ACTION_GET_CONTENT);

        startActivityForResult(imageSelectIntent, imageSelectReqCode);
    }

    private OnSuccessListener<Uri> onDownloadSuccess(){
        return new OnSuccessListener<Uri>(){
            @Override
            public void onSuccess(Uri userImagePath) {

                Glide.with(getApplicationContext())
                        .load(userImagePath.toString())
                        .apply(new RequestOptions().dontAnimate())
                        .into(profileImage);

                endProgress();
            }
        };
    }

    private OnFailureListener onDownloadFailure(){
        return new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ProfileImageActivity.this, "Profile image is not loaded",
                        Toast.LENGTH_SHORT).show();

                endProgress();
            }
        };
    }

    private OnCompleteListener<UploadTask.TaskSnapshot> onUploadCompleteAction(){

        return new OnCompleteListener<UploadTask.TaskSnapshot>(){
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                Toast.makeText(ProfileImageActivity.this, "Profile image is updated",
                        Toast.LENGTH_SHORT).show();

                uploadComplete = true;

                FirebaseDatabase.getInstance()
                        .getReference("User")
                        .child(UserAppVersionController
                                .getUserAppVersionController().getCurrentUserId())
                        .child("hasProfileImage").setValue(true);

            }
        };
    }

    private OnFailureListener onUploadFailureAction(){

        return new OnFailureListener(){
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ProfileImageActivity.this, "Cannot update profile image",
                        Toast.LENGTH_SHORT).show();

                uploadComplete = true;
            }
        };

    }

    @Override
    protected void onActivityResult(int reqCode, int resCode, Intent intent){
        super.onActivityResult(reqCode, resCode, intent);

        showProgress("Loading ...");

        if(reqCode == imageSelectReqCode && resCode == RESULT_OK
                && intent != null){

            imagePath = intent.getData();

            if(imagePath != null){

                try{
                    Bitmap bitmap = MediaStore
                            .Images
                            .Media
                            .getBitmap(getContentResolver(), imagePath);

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
