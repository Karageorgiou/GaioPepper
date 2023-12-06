package gr.ntua.metal.gaiopepper.activities.main;

import static gr.ntua.metal.gaiopepper.models.MessageItem.LayoutRobot;
import static gr.ntua.metal.gaiopepper.models.MessageItem.LayoutRobotImage;
import static gr.ntua.metal.gaiopepper.models.MessageItem.LayoutUser;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import gr.ntua.metal.gaiopepper.R;
import gr.ntua.metal.gaiopepper.models.MessageItem;
import gr.ntua.metal.gaiopepper.util.ImageManager;

public class MessageAdapter extends RecyclerView.Adapter<MessageViewHolder> {
    private static final String TAG = "Message Adapter";

    Context context;
    List<MessageItem> messageItemList;

    public MessageAdapter(Context context, List<MessageItem> messageItemList) {
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

    public void addItem(MessageItem entry){
        this.messageItemList.add(entry);
        notifyItemInserted(messageItemList.size() - 1);
    }


}
