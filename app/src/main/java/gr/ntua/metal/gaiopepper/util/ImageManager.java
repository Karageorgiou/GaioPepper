package gr.ntua.metal.gaiopepper.util;


import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;


public class ImageManager {
    private static ImageView expandedImageView;
    private static RelativeLayout expandedImageLayer;

    public static void setExpandedImageView(ImageView imageView, RelativeLayout imageLayer) {
        expandedImageView = imageView;
        expandedImageLayer = imageLayer;
        expandedImageView.setOnClickListener(view -> {
            hideImage();
        });
    }


    public static void updateImage(ImageView imageView, int resID) {
        imageView.setImageResource(resID);
    }

    public static void updateImage(int resID) {
        expandedImageView.setImageResource(resID);
    }

    public static void showImage() {
        expandedImageLayer.setVisibility(View.VISIBLE);
    }

    public static void showImageForSeconds(long seconds) {
        showImage();
        Handler handler = new Handler();
        handler.postDelayed(() -> {
            hideImage();
        }, seconds * 1000);
    }

    public static void hideImage() {
        expandedImageLayer.setVisibility(View.INVISIBLE);
    }


}
