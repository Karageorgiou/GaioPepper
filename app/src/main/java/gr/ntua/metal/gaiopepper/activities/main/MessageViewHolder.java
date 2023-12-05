package gr.ntua.metal.gaiopepper.activities.main;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import gr.ntua.metal.gaiopepper.R;

public class MessageViewHolder extends RecyclerView.ViewHolder {
    private static final String TAG = "Message ViewHolder";


    public ImageView imageView;
    CardView cardView;
    public TextView textView;
    public ImageView imageMessageView;

    public MessageViewHolder(@NonNull View itemView) {
        super(itemView);

        imageView = itemView.findViewById(R.id.image_user_icon);
        cardView = itemView.findViewById(R.id.card_user_message);
        textView = itemView.findViewById(R.id.tv_user_message);
        imageMessageView = itemView.findViewById(R.id.image_user_message);


    }

}
