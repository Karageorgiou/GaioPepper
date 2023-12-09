package gr.ntua.metal.gaiopepper.util;


import com.aldebaran.qi.sdk.object.conversation.AutonomousReaction;

public interface ITimingUtility {

    void checkForDuplicates(AutonomousReaction autonomousReaction, TimingUtility.AutonomousReactionCallback callback);
    void checkForDuplicates(int image, TimingUtility.ImageCallback callback);
    void checkForDuplicates(String message, TimingUtility.MessageCallback callback);
}
