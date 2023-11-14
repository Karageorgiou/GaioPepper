package gr.ntua.metal.gaiopepper.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import gr.ntua.metal.gaiopepper.R;
import gr.ntua.metal.gaiopepper.holders.MessageViewHolder;
import gr.ntua.metal.gaiopepper.models.MessageItem;

import static gr.ntua.metal.gaiopepper.models.MessageItem.LayoutUser;
import static gr.ntua.metal.gaiopepper.models.MessageItem.LayoutRobot;

public class MessageAdapter extends RecyclerView.Adapter<MessageViewHolder> {
    private static final String TAG = "Message Adapter";

    Context context;
    List<MessageItem> messageItemList;

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
            default:
                throw new IllegalStateException("Unexpected value: " + viewType);
        }
        return messageViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        //Log.i(TAG, "onBindViewHolder");

        holder.imageView.setImageResource(messageItemList.get(position).getImage());
        holder.textView.setText(messageItemList.get(position).getMessage());
    }

    @Override
    public int getItemCount() {
        return messageItemList.size();
    }
}
