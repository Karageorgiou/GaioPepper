package gr.ntua.metal.gaiopepper.util.fsm;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.Nullable;

public abstract class StateManager {
    private static final String TAG = "State Manager";

    private final Handler handler;
    private boolean running = false;
    private final double targetUPS;
    private final long targetUpdateTime;

    public StateManager(double targetUPS) {
        if (!Looper.getMainLooper().isCurrentThread()) {
            Looper.getMainLooper().prepare();
        }
        this.targetUPS = targetUPS;
        this.targetUpdateTime = (long) (1000000000.0 / targetUPS);
        handler = new Handler();
    }

    public void startLoop() {
        Log.i(TAG, "Starting loop ");
        running = true;
        loop();
    }

    public void stopLoop() {
        Log.i(TAG, "Stopping loop ");
        running = false;
    }

    private void loop() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (running) {
                    update();
                    postUpdate();
                    handler.postDelayed(this, targetUpdateTime / 1000000);
                }
            }
        }, targetUpdateTime / 1000000);
    }

    // Subclasses must implement the following methods
    public abstract void update();

    public abstract void postUpdate();

    public abstract void changeState(IState newState, @Nullable Object data);
}