package gr.ntua.metal.gaiopepper.util.fsm.discussion;

import android.app.Activity;
import android.util.Log;

import androidx.annotation.Nullable;

import gr.ntua.metal.gaiopepper.util.fsm.IState;
import gr.ntua.metal.gaiopepper.util.fsm.StateManager;
import gr.ntua.metal.gaiopepper.util.fsm.chat.ChatState;


public class DiscussionFSM extends StateManager {
    private static final String TAG = "Discussion State Machine";


    public DiscussionFSM(Activity owner, double targetUPS) {
        super(targetUPS);
        Log.d(TAG, "DiscussionFSM ");


        DiscussionState.none = new None(owner, TAG, "None State");
        DiscussionState.oral = new Oral(owner, TAG, "Oral State");
        DiscussionState.written = new Written(owner, TAG, "Written State");
        DiscussionState.current = DiscussionState.none;
        DiscussionState.current.enter(null);
    }

    @Override
    public void update() {
        //Log.d(TAG, "Update " + DiscussionState.current);

    }

    @Override
    public void postUpdate() {

    }


    @Override
    public void changeState(IState newState, @Nullable Object data) {
        if(DiscussionState.current.equals(newState)){
            return;
        }

        DiscussionState newDiscussionState = (DiscussionState) newState;
        String language = (String) data;

        DiscussionState.current.exit();
        Log.d(TAG, DiscussionState.current.getName() + " -> " + newDiscussionState.getName());
        DiscussionState.current = newDiscussionState;
        if (newState instanceof Oral || newState instanceof Written) {
            DiscussionState.current.enter(language);
        } else if (newState instanceof None) {
            DiscussionState.current.enter(null);
        }
    }
}
