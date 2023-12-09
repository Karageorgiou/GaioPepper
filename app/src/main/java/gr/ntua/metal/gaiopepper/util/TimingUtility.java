package gr.ntua.metal.gaiopepper.util;

import android.util.Log;

import androidx.annotation.NonNull;

import com.aldebaran.qi.sdk.object.conversation.AutonomousReaction;


public class TimingUtility implements ITimingUtility {
    private static final String TAG = "Timing Manager";


    private AutonomousReaction lastAutonomousReaction = null;
    private long lastAutonomousReactionUpdate = 100000;

    private int lastImage = 0;
    private String lastMessage = "";
    private long lastImageUpdate = 100000;
    private long lastMessageUpdate = 100000;


    @Override
    public void checkForDuplicates(AutonomousReaction autonomousReaction, TimingUtility.AutonomousReactionCallback callback) {
        long currentTime = System.currentTimeMillis();
        long difference = Math.abs(lastAutonomousReactionUpdate - currentTime);
        if (autonomousReaction.equals(lastAutonomousReaction) && difference < 1000) {
            Log.d(TAG, "Time between autonomousReactions: " + difference + "ms. Duplicate autonomousReaction purged.");
        } else {
            lastAutonomousReactionUpdate = currentTime;
            lastAutonomousReaction = autonomousReaction;
            callback.handleAutonomousReaction(autonomousReaction);
        }
    }

    @Override
    public void checkForDuplicates(int image, TimingUtility.ImageCallback callback) {
        long currentTime = System.currentTimeMillis();
        long difference = Math.abs(lastImageUpdate - currentTime);
        if (image == lastImage && difference < 1500) {
            Log.d(TAG, "Time between messages: " + difference + "ms. Duplicate message purged.");
            callback.handleImage(true);
        }
        lastImage = image;
        lastImageUpdate = currentTime;
        callback.handleImage(false);


    }

    @Override
    public void checkForDuplicates(String message, MessageCallback callback) {
        long currentTime = System.currentTimeMillis();
        long difference = Math.abs(lastMessageUpdate - currentTime);
        if (message == lastMessage && difference < 1500) {
            Log.d(TAG, "Time between messages: " + difference + "ms. Duplicate message purged.");
            callback.handleMessage(true);
        }
        lastMessage = message;
        lastMessageUpdate = currentTime;
        callback.handleMessage(false);
    }

    public interface AutonomousReactionCallback {
        void handleAutonomousReaction(@NonNull AutonomousReaction autonomousReaction);

    }

    public interface ImageCallback {
        void handleImage(@NonNull boolean purged);
    }

    public interface MessageCallback {
        void handleMessage(@NonNull boolean purged);
    }

}
