package gr.ntua.metal.gaiopepper.util;

import com.aldebaran.qi.Consumer;
import com.aldebaran.qi.Future;
import com.aldebaran.qi.sdk.Qi;
import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.builder.HolderBuilder;
import com.aldebaran.qi.sdk.object.holder.AutonomousAbilitiesType;
import com.aldebaran.qi.sdk.object.holder.Holder;

public class AutonomousAbilitiesManager {

    public static Holder holderAB;
    public static Holder holderBM;
    public static Holder holderBA;

    public static void buildHolders(QiContext qiContext) {
        AutonomousAbilitiesManager.holderAB = HolderBuilder.with(qiContext)
                .withAutonomousAbilities(
                        AutonomousAbilitiesType.AUTONOMOUS_BLINKING)
                .build();
        AutonomousAbilitiesManager.holderBM = HolderBuilder.with(qiContext)
                .withAutonomousAbilities(
                        AutonomousAbilitiesType.BACKGROUND_MOVEMENT)
                .build();
        AutonomousAbilitiesManager.holderBA = HolderBuilder.with(qiContext)
                .withAutonomousAbilities(
                        AutonomousAbilitiesType.BASIC_AWARENESS)
                .build();
    }


    public static void stopAutonomousBlinking(QiContext qiContext) {
        Future<Void> holdFuture = holderAB.async().hold();
        // Chain the hold with a lambda on the UI thread.
        holdFuture.andThenConsume(Qi.onUiThread((Consumer<Void>) ignore -> {
            //todo:
        }));
    }

    public static void startAutonomousBlinking() {
        Future<Void> releaseFuture = holderAB.async().release();
        releaseFuture.andThenConsume(Qi.onUiThread((Consumer<Void>) ignore -> {
            //todo:

        }));
    }

    public static void stopBackgroundMovement(QiContext qiContext) {
        Future<Void> holdFuture = holderBM.async().hold();
        // Chain the hold with a lambda on the UI thread.
        holdFuture.andThenConsume(Qi.onUiThread((Consumer<Void>) ignore -> {
            //todo:
        }));
    }

    public static void startBackgroundMovement() {
        Future<Void> releaseFuture = holderBM.async().release();
        releaseFuture.andThenConsume(Qi.onUiThread((Consumer<Void>) ignore -> {
            //todo:

        }));
    }

    public static void stopBasicAwareness(QiContext qiContext) {
        Future<Void> holdFuture = holderBA.async().hold();
        // Chain the hold with a lambda on the UI thread.
        holdFuture.andThenConsume(Qi.onUiThread((Consumer<Void>) ignore -> {
            //todo:
        }));
    }

    public static void startBasicAwareness() {
        Future<Void> releaseFuture = holderBA.async().release();
        releaseFuture.andThenConsume(Qi.onUiThread((Consumer<Void>) ignore -> {
            //todo:

        }));
    }
}
