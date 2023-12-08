package gr.ntua.metal.gaiopepper.util;


import android.os.Handler;

import androidx.annotation.Nullable;

import com.aldebaran.qi.Future;
import com.aldebaran.qi.sdk.object.conversation.AutonomousReaction;

public interface ITimingManager {

    void checkForDuplicates(AutonomousReaction autonomousReaction, TimingManager.AutonomousReactionCallback callback);
    void checkForDuplicates(int image, TimingManager.ImageCallback callback);
    void checkForDuplicates(String message, TimingManager.MessageCallback callback);
}
