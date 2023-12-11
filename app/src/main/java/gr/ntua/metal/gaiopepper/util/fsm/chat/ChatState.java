package gr.ntua.metal.gaiopepper.util.fsm.chat;

import static gr.ntua.metal.gaiopepper.models.MessageItem.LayoutRobot;
import static gr.ntua.metal.gaiopepper.models.MessageItem.LayoutUser;

import android.app.Activity;
import android.util.Log;

import androidx.core.util.Pair;

import gr.ntua.metal.gaiopepper.activities.main.MainActivity;
import gr.ntua.metal.gaiopepper.util.ChatManager;
import gr.ntua.metal.gaiopepper.util.fsm.IState;

public abstract class ChatState implements IState {
    static ChatState current;
    static ChatState next;

    public static ChatState listening;
    public static ChatState talking;
    public static ChatState alive;
    public static ChatState dead;

    protected String TAG;
    protected String FSM_TAG;
    protected Activity machineOwner;
    protected Object data;

    boolean done = false;



    public ChatState(Activity machineOwner, String FSM_TAG, String TAG) {
        this.TAG = TAG;
        this.FSM_TAG = FSM_TAG;
        this.machineOwner = machineOwner;
    }

    public abstract void setData(Object data);
    public abstract Object getData();
}

class Listening extends ChatState {

    public Listening(Activity machineOwner, String FSM_TAG, String TAG) {
        super(machineOwner, FSM_TAG, TAG);
    }

    @Override
    public void setData(Object data) {
        this.data = data;
    }

    @Override
    public Object getData() {
        return this.data;
    }

    @Override
    public void enter(Object data) {
        //Log.d(FSM_TAG, "Entering " + TAG);
        this.done = false;

    }

    @Override
    public void exit() {
        this.done = true;

        //Log.d(FSM_TAG, "Exiting " + TAG);
    }

    @Override
    public String getName() {
        return TAG;
    }
}

class Talking extends ChatState {

    public Talking(Activity machineOwner, String FSM_TAG, String TAG) {
        super(machineOwner, FSM_TAG, TAG);
    }

    @Override
    public void setData(Object data) {
        this.data = data;
    }

    @Override
    public Object getData() {
        return this.data;
    }

    @Override
    public void enter(Object data) {
        this.done = false;

        //Log.d(FSM_TAG, "Entering " + TAG);
        MainActivity mainActivity = (MainActivity)machineOwner;
        String sayingText = (String) data;
        ChatManager chatManager = mainActivity.chatManager;
        Log.i(TAG, "[SPEECH ENGINE]/[CHAT] Pepper Reply: " + sayingText);
        chatManager.setContent(mainActivity.chatFragment, new Pair<>(LayoutRobot, sayingText));


    }

    @Override
    public void exit() {
        this.done = true;

        //Log.d(FSM_TAG, "Exiting " + TAG);
    }

    @Override
    public String getName() {
        return TAG;
    }
}

class Alive extends ChatState {

    public Alive(Activity machineOwner, String FSM_TAG, String TAG) {
        super(machineOwner, FSM_TAG, TAG);
    }

    @Override
    public void setData(Object data) {
        this.data = data;
    }

    @Override
    public Object getData() {
        return this.data;
    }

    @Override
    public void enter(Object data) {
        this.done = false;

        //Log.d(FSM_TAG, "Entering " + TAG);
        MainActivity mainActivity = (MainActivity)machineOwner;

        if (data != null) {
            String heardText = (String) data;
            mainActivity.chatManager.setContent(mainActivity.chatFragment, new Pair<>(LayoutUser, heardText));
        }

    }

    @Override
    public void exit() {
        this.done = true;

        //Log.d(FSM_TAG, "Exiting " + TAG);
    }

    @Override
    public String getName() {
        return TAG;
    }
}


class Dead extends ChatState {

    public Dead(Activity machineOwner, String FSM_TAG, String TAG) {
        super(machineOwner, FSM_TAG, TAG);
    }

    @Override
    public void setData(Object data) {
        this.data = data;
    }

    @Override
    public Object getData() {
        return this.data;
    }

    @Override
    public void enter(Object data) {
        this.done = false;

        //Log.d(FSM_TAG, "Entering " + TAG);
        MainActivity mainActivity = (MainActivity)machineOwner;


    }

    @Override
    public void exit() {
        this.done = true;

        //Log.d(FSM_TAG, "Exiting " + TAG);
    }

    @Override
    public String getName() {
        return TAG;
    }
}

