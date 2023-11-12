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

public class MessageAdapter extends RecyclerView.Adapter<MessageViewHolder> {
    private static final String TAG = "Message Adapter";

    Context context;
    List<MessageItem> messageItemList;

    public MessageAdapter(Context context, List<MessageItem> messageItemList) {
        Log.i(TAG, "Created");

        this.context = context;
        this.messageItemList = messageItemList;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.i(TAG, "onCreateViewHolder");

        return new MessageViewHolder(LayoutInflater.from(context).inflate(R.layout.message_item_view, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        Log.i(TAG, "onBindViewHolder");

        holder.imageView.setImageResource(messageItemList.get(position).getImage());
        holder.textView.setText(messageItemList.get(position).getMessage());
    }

    @Override
    public int getItemCount() {
        Log.i(TAG, "getItemCount");
        return messageItemList.size();
    }
}
