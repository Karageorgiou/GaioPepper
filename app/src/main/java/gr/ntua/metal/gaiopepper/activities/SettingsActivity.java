package gr.ntua.metal.gaiopepper.activities;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.aldebaran.qi.Consumer;
import com.aldebaran.qi.Future;
import com.aldebaran.qi.sdk.Qi;
import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.QiSDK;
import com.aldebaran.qi.sdk.RobotLifecycleCallbacks;
import com.aldebaran.qi.sdk.builder.HolderBuilder;
import com.aldebaran.qi.sdk.object.holder.AutonomousAbilitiesType;
import com.aldebaran.qi.sdk.object.holder.Holder;

import gr.ntua.metal.gaiopepper.R;
import gr.ntua.metal.gaiopepper.fragments.SettingsFragment;

public class SettingsActivity extends AppCompatActivity implements RobotLifecycleCallbacks {
    private static final String TAG = "Settings Activity" ;

    public QiContext qiContext;

    public Holder holderAB;
    public Holder holderBM;
    public Holder holderBA;


    //todo: transfer RobotLifecycleCallbacks to Main activity and pass prefs to Main activity

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        QiSDK.register(this, this);


        //setSpeechBarDisplayStrategy(SpeechBarDisplayStrategy.IMMERSIVE);
        //setSpeechBarDisplayPosition(SpeechBarDisplayPosition.TOP);

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings, new SettingsFragment())
                    .commit();
        }

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("Ρυθμίσεις");
        }



    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onRobotFocusGained(QiContext qiContext) {
        Log.i(TAG, "onRobotFocusGained");
        this.qiContext = qiContext;

        // Build the holder for the abilities.
        holderAB = HolderBuilder.with(qiContext)
                .withAutonomousAbilities(
                        AutonomousAbilitiesType.AUTONOMOUS_BLINKING)
                .build();
        holderBM = HolderBuilder.with(qiContext)
                .withAutonomousAbilities(
                        AutonomousAbilitiesType.BACKGROUND_MOVEMENT)
                .build();
        holderBA = HolderBuilder.with(qiContext)
                .withAutonomousAbilities(
                        AutonomousAbilitiesType.BASIC_AWARENESS)
                .build();




    }

    @Override
    public void onRobotFocusLost() {
        Log.i(TAG, "onRobotFocusLost");
        this.qiContext = null;
    }

    @Override
    public void onRobotFocusRefused(String reason) {
        Log.i(TAG, "onRobotFocusRefused: " + reason);

    }




    public void stopAutonomousBlinking(QiContext qiContext) {
        Future<Void> holdFuture = holderAB.async().hold();
        // Chain the hold with a lambda on the UI thread.
        holdFuture.andThenConsume(Qi.onUiThread((Consumer<Void>) ignore -> {
            //todo:
        }));
    }

    public void startAutonomousBlinking(Holder holder) {
        Future<Void> releaseFuture = holder.async().release();
        // Chain the release with a lambda on the UI thread.
        releaseFuture.andThenConsume(Qi.onUiThread((Consumer<Void>) ignore -> {
            //todo:

        }));
    }

    public void stopBackgroundMovement(QiContext qiContext) {
        Future<Void> holdFuture = holderBM.async().hold();
        // Chain the hold with a lambda on the UI thread.
        holdFuture.andThenConsume(Qi.onUiThread((Consumer<Void>) ignore -> {
            //todo:
        }));
    }

    public void startBackgroundMovement(Holder holder) {
        Future<Void> releaseFuture = holder.async().release();
        // Chain the release with a lambda on the UI thread.
        releaseFuture.andThenConsume(Qi.onUiThread((Consumer<Void>) ignore -> {
            //todo:

        }));
    }

    public void stopBasicAwareness(QiContext qiContext) {
        Future<Void> holdFuture = holderBA.async().hold();
        // Chain the hold with a lambda on the UI thread.
        holdFuture.andThenConsume(Qi.onUiThread((Consumer<Void>) ignore -> {
            //todo:
        }));
    }

    public void startBasicAwareness(Holder holder) {
        Future<Void> releaseFuture = holder.async().release();
        // Chain the release with a lambda on the UI thread.
        releaseFuture.andThenConsume(Qi.onUiThread((Consumer<Void>) ignore -> {
            //todo:

        }));
    }



}
