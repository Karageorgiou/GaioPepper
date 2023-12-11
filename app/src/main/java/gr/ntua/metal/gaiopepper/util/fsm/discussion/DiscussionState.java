package gr.ntua.metal.gaiopepper.util.fsm.discussion;

import android.app.Activity;

import java.util.Objects;
import java.util.concurrent.ExecutionException;

import gr.ntua.metal.gaiopepper.R;
import gr.ntua.metal.gaiopepper.activities.main.MainActivity;
import gr.ntua.metal.gaiopepper.util.ChatManager;
import gr.ntua.metal.gaiopepper.util.fsm.chat.ChatState;
import gr.ntua.metal.gaiopepper.util.fsm.IState;

public abstract class DiscussionState implements IState {
    static DiscussionState current;

    public static DiscussionState oral;
    public static DiscussionState written;
    public static DiscussionState none;

    String TAG;
    String FSM_TAG;
    Activity machineOwner;

    public DiscussionState(Activity machineOwner, String FSM_TAG, String TAG) {
        this.TAG = TAG;
        this.FSM_TAG = FSM_TAG;
        this.machineOwner = machineOwner;
    }

    /*@Override
    public abstract void enter(Object data);

    @Override
    public abstract void exit();*/
}

class Oral extends DiscussionState {

    public Oral(Activity machineOwner, String FSM_TAG, String TAG) {
        super(machineOwner,FSM_TAG,  TAG);
    }

    @Override
    public void enter(Object data) {
        //Log.d(FSM_TAG, "Entering " + TAG);
        MainActivity mainActivity = (MainActivity)machineOwner;
        String conversationLanguage = (String) data;
        ChatManager chatManager = mainActivity.chatManager;

        mainActivity.chatFSM.changeState(ChatState.alive, null);

        mainActivity.runOnUiThread(() -> {
            chatManager.hideTextInput();
        });
        if (conversationLanguage != null) {
            if (Objects.equals(conversationLanguage, mainActivity.getString(R.string.GREEK))) {
                try {
                    chatManager.chat = chatManager.buildChat(chatManager.chatbot, chatManager.getCurrentLocale());
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
                chatManager.runChat(chatManager.chat);
                if (chatManager.getLastBookmark() == null) {
                    //chatbot.async().goToBookmark(bookmarksGR.get("INTRO"), AutonomousReactionImportance.HIGH, AutonomousReactionValidity.IMMEDIATE);
                }
            } else if (Objects.equals(conversationLanguage, mainActivity.getString(R.string.ENGLISH))) {
                try {
                    chatManager.chat = chatManager.buildChat(chatManager.chatbot, chatManager.getCurrentLocale());
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
                chatManager.runChat(chatManager.chat);
                if (chatManager.getLastBookmark() == null) {
                    //chatbot.async().goToBookmark(bookmarksEN.get("INTRO"), AutonomousReactionImportance.HIGH, AutonomousReactionValidity.IMMEDIATE);
                }
            }
        }

    }

    @Override
    public void exit() {
        //Log.d(FSM_TAG, "Exiting " + TAG);

    }

    @Override
    public String getName() {
        return TAG;
    }
}

class Written extends DiscussionState {

    public Written(Activity machineOwner, String FSM_TAG, String TAG) {
        super(machineOwner,FSM_TAG,  TAG);
    }

    @Override
    public void enter(Object data) {
        //Log.d(FSM_TAG, "Entering " + TAG);
        MainActivity mainActivity = (MainActivity)machineOwner;
        String conversationLanguage = (String) data;
        ChatManager chatManager = mainActivity.chatManager;

        mainActivity.chatFSM.changeState(ChatState.alive, null);

        chatManager.tryCancelChat();
        mainActivity.runOnUiThread(() -> {
            chatManager.showTextInput();
        });
        if(conversationLanguage != null) {
            if (Objects.equals(conversationLanguage, mainActivity.getString(R.string.GREEK))) {
                if (chatManager.getLastBookmark() == null) {
                    //chatbot.async().goToBookmark(bookmarksGR.get("INTRO"), AutonomousReactionImportance.HIGH, AutonomousReactionValidity.IMMEDIATE);
                }
            } else if (Objects.equals(conversationLanguage, mainActivity.getString(R.string.ENGLISH))) {
                if (chatManager.getLastBookmark() == null) {
                    //chatbot.async().goToBookmark(bookmarksEN.get("INTRO"), AutonomousReactionImportance.HIGH, AutonomousReactionValidity.IMMEDIATE);
                }
            }
        }
    }

    @Override
    public void exit() {
        //Log.d(FSM_TAG, "Exiting " + TAG);

    }

    @Override
    public String getName() {
        return TAG;
    }
}

class None extends DiscussionState {

    public None(Activity machineOwner, String FSM_TAG, String TAG) {
        super(machineOwner,FSM_TAG,  TAG);
    }

    @Override
    public void enter(Object data) {
        //Log.d(FSM_TAG, "Entering " + TAG);
        MainActivity mainActivity = (MainActivity)machineOwner;
        ChatManager chatManager = mainActivity.chatManager;

        mainActivity.chatFSM.changeState(ChatState.dead, null);

        chatManager.tryCancelChat();
        mainActivity.runOnUiThread(() -> {
            chatManager.hideTextInput();
        });

    }

    @Override
    public void exit() {
        //Log.d(FSM_TAG, "Exiting " + TAG);

    }

    @Override
    public String getName() {
        return TAG;
    }
}

