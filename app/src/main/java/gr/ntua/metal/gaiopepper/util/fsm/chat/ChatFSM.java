package gr.ntua.metal.gaiopepper.util.fsm.chat;

import android.app.Activity;
import android.util.Log;

import androidx.annotation.Nullable;

import gr.ntua.metal.gaiopepper.util.fsm.IState;
import gr.ntua.metal.gaiopepper.util.fsm.StateManager;
import gr.ntua.metal.gaiopepper.util.fsm.discussion.DiscussionState;

public class ChatFSM extends StateManager {
    private static final String TAG = "Chat State Machine";

    public ChatFSM(Activity owner, double targetUPS) {
        super(targetUPS);
        Log.d(TAG, "ChatFSM ");

        ChatState.listening = new Listening(owner, TAG, "Listening State");
        ChatState.talking = new Talking(owner, TAG, "Talking State");
        ChatState.alive = new Alive(owner, TAG, "Alive State");
        ChatState.dead = new Dead(owner, TAG, "Dead State");
        ChatState.current = ChatState.dead;
        ChatState.current.enter(null);
    }

    @Override
    public void update() {
        //Log.i(TAG, "update ");

       /* if (!ChatState.next.equals(ChatState.current)) {
            ChatState.current.exit();
            if (ChatState.current.done) {
                ChatState.current = ChatState.next;
                ChatState.current.enter(ChatState.current.getData());
            }
        }*/

    }

    @Override
    public void postUpdate() {

    }

    @Override
    public void changeState(IState newState, @Nullable Object data) {
        if(ChatState.current.equals(newState)){
            return;
        }

        ChatState newChatState = (ChatState) newState;

        ChatState.current.exit();
        Log.d(TAG, ChatState.current.getName() + " -> " + newChatState.getName());
        //ChatState.next = newChatState;
        //ChatState.next.setData(data);
        ChatState.current = newChatState;
        ChatState.current.enter(data);

    }

    public ChatState getState() {
        return ChatState.current;
    }
}


