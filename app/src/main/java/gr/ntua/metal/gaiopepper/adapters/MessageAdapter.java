package gr.ntua.metal.gaiopepper.adapters;

import static gr.ntua.metal.gaiopepper.models.MessageItem.LayoutRobot;
import static gr.ntua.metal.gaiopepper.models.MessageItem.LayoutRobotImage;
import static gr.ntua.metal.gaiopepper.models.MessageItem.LayoutUser;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aldebaran.qi.sdk.QiSDK;
import com.aldebaran.qi.sdk.design.activity.conversationstatus.SpeechBarDisplayPosition;
import com.aldebaran.qi.sdk.design.activity.conversationstatus.SpeechBarDisplayStrategy;

import java.util.ArrayList;
import java.util.List;

import gr.ntua.metal.gaiopepper.ImageManager;
import gr.ntua.metal.gaiopepper.R;
import gr.ntua.metal.gaiopepper.holders.MessageViewHolder;
import gr.ntua.metal.gaiopepper.models.MessageItem;

public class MessageAdapter extends RecyclerView.Adapter<MessageViewHolder> {
    private static final String TAG = "Message Adapter";

    Context context;
    List<MessageItem> messageItemList;

    private Animator currentAnimator;


    public MessageAdapter(Context context, List<MessageItem> messageItemList) {
        //Log.i(TAG, "Created");

        this.context = context;
        this.messageItemList = messageItemList;
    }

    @Override
    public int getItemViewType(int position) {
        switch (messageItemList.get(position).getViewType()) {
            case 0:
                return LayoutUser;
            case 1:
                return LayoutRobot;
            case 2:
                return LayoutRobotImage;
            default:
                return -1;
        }
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //Log.i(TAG, "onCreateViewHolder");
        MessageViewHolder messageViewHolder;
        switch (viewType) {
            case LayoutUser:
                messageViewHolder = new MessageViewHolder(LayoutInflater.from(context).inflate(R.layout.user_message_item_view, parent, false)); 
                break;
            case LayoutRobot:
                messageViewHolder = new MessageViewHolder(LayoutInflater.from(context).inflate(R.layout.robot_message_item_view, parent, false));
                break;
            case LayoutRobotImage:
                messageViewHolder = new MessageViewHolder(LayoutInflater.from(context).inflate(R.layout.robot_message_image_view, parent, false));
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + viewType);
        }
        return messageViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        //Log.i(TAG, "onBindViewHolder");

        holder.imageView.setImageResource(messageItemList.get(position).getImage());
        if (messageItemList.get(position).getViewType() == LayoutRobot || messageItemList.get(position).getViewType() == LayoutUser) {
            holder.textView.setText(messageItemList.get(position).getMessage());
        } else if (messageItemList.get(position).getViewType() == LayoutRobotImage) {
            holder.imageMessageView.setImageResource(messageItemList.get(position).getImageMessage());
            holder.imageMessageView.setOnClickListener(view -> {
                ImageManager.updateImage(messageItemList.get(position).getImageMessage());
                ImageManager.showImage();
            });
        }

    }

    @Override
    public int getItemCount() {
        return messageItemList.size();
    }




}
