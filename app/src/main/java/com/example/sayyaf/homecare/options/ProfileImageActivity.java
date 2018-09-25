package com.example.sayyaf.homecare.options;

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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.sayyaf.homecare.R;
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

    private final int imageSelectReqCode = 5;

    private ImageView profileImage;

    private TextView selectImage;
    private FloatingActionButton selectImageButton;

    private TextView confirmChange;
    private FloatingActionButton confirmChangeButton;

    private Uri imagePath;

    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_photo);

        selectImage = (TextView) findViewById(R.id.selectImage);
        selectImageButton = (FloatingActionButton) findViewById(R.id.selectImageButton);

        confirmChange = (TextView) findViewById(R.id.confirmChange);
        confirmChangeButton = (FloatingActionButton) findViewById(R.id.confirmChangeButton);

        profileImage = (ImageView) findViewById(R.id.profileImage);

        loadProfileImage();
    }

    @Override
    public void onClick(View v) {

        // select image
        if(v == selectImage || v == selectImageButton){
            selectImage();
        }
        // confirm change
        if(v == confirmChange || v == confirmChangeButton){
            getCurrentUserId();
            uploadProfileImage();
        }
    }

    private void getCurrentUserId(){
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    private void loadProfileImage(){
        getCurrentUserId();

        Query profileImgRef = FirebaseDatabase.getInstance()
                .getReference("Users")
                .child(userId).child("setUpProfileImage");

        profileImgRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    FirebaseStorage.getInstance()
                            .getReference("UserProfileImage").child(userId).getDownloadUrl()
                            .addOnSuccessListener(onDownloadSuccess())
                            .addOnFailureListener(onDownloadFailure());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void uploadProfileImage(){
        if(imagePath != null){
            StorageReference storageRef = FirebaseStorage.getInstance()
                    .getReference("UserProfileImage").child(userId);

            storageRef.putFile(imagePath);
                    /*.addOnCompleteListener(onUploadCompleteAction())
                    .addOnFailureListener(onUploadFailureAction());*/

            Toast.makeText(ProfileImageActivity.this, "Profile image is updated",
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
                    .into(profileImage);
            }
        };
    }

    private OnFailureListener onDownloadFailure(){
        return new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ProfileImageActivity.this, "Profile image is not loaded",
                        Toast.LENGTH_SHORT).show();
            }
        };
    }

    private OnCompleteListener<UploadTask.TaskSnapshot> onUploadCompleteAction(){

        return new OnCompleteListener<UploadTask.TaskSnapshot>(){
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                Toast.makeText(ProfileImageActivity.this, "Profile image is updated",
                        Toast.LENGTH_SHORT).show();

                returnToOptions();
            }
        };
    }

    private OnFailureListener onUploadFailureAction(){

        return new OnFailureListener(){
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ProfileImageActivity.this, "Cannot update profile image",
                        Toast.LENGTH_SHORT).show();
            }
        };

    }

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

                    profileImage.setImageBitmap(bitmap);

                    FirebaseDatabase.getInstance()
                            .getReference("Users")
                            .child(userId).child("setUpProfileImage").setValue(true);

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
