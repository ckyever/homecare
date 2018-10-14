package com.example.sayyaf.homecare;

import android.content.Context;
import android.graphics.Bitmap;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

// This class handle image loading and rescaling
public class ImageLoader {

    private static ImageLoader instance = null;

    public static ImageLoader getImageLoader(){
        if(instance == null) instance = new ImageLoader();
        return instance;
    }

    /* return lower resolution bitmap image for higher performance (memory)
     * bitmapSrc: the original bitmap image
     * maxSize: maximum side length to be resized
     */
    public Bitmap bitMapScaling(Bitmap bitmapSrc, int maxSize){
        // not need to resize if both sides are smaller than size to display
        if(bitmapSrc.getWidth() < maxSize && bitmapSrc.getHeight() < maxSize) return bitmapSrc;

        int exportX;
        int exportY;

        // find the longest side
        if(bitmapSrc.getWidth() > bitmapSrc.getHeight()){
            exportX = maxSize;
            exportY = (bitmapSrc.getHeight() * maxSize) / bitmapSrc.getWidth();
        }
        else{
            exportY = maxSize;
            exportX = (bitmapSrc.getWidth() * maxSize) / bitmapSrc.getHeight();
        }
        return Bitmap.createScaledBitmap(bitmapSrc, exportX, exportY, false);
    }

    /* load contact image
     * context: the activity contain list of contacts/ contact requests
     * userImageView: the default image view to be replaced
     * profileImageUri: the image uri reference
     */
    public void loadContactImageToView(Context context, ImageView userImageView, String profileImageUri){
        Glide.with(context.getApplicationContext())
                .load(profileImageUri)
                .apply(new RequestOptions()
                        .override(100, 100) // resize image in pixel
                        .centerCrop()
                        .dontAnimate()
                        .skipMemoryCache(true)
                        .error(R.mipmap.ic_launcher_round))
                .into(userImageView);
    }
}
