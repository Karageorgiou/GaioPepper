package gr.ntua.metal.gaiopepper;


import android.view.View;
import android.widget.ImageView;


public class ImageManager {
    private static ImageView expandedImageView;

    public static void setExpandedImageView(ImageView imageView) {
        expandedImageView = imageView;
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
        expandedImageView.setVisibility(View.VISIBLE);
    }

    public static void hideImage() {
        expandedImageView.setVisibility(View.INVISIBLE);
    }


}
