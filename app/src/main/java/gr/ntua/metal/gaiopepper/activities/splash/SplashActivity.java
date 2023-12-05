package gr.ntua.metal.gaiopepper.activities.splash;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Timer;
import java.util.TimerTask;

import gr.ntua.metal.gaiopepper.R;
import gr.ntua.metal.gaiopepper.activities.main.MainActivity;

public class SplashActivity extends AppCompatActivity {
    private static final String TAG = "Splash Activity" ;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        Log.i(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);


        new Timer().schedule(new TimerTask(){
            public void run() {
                startActivity(new Intent(SplashActivity.this, MainActivity.class));
                finish();

                Log.d(TAG, "onCreate: waiting 3 seconds for Splash Activity.");
            }
        }, 3000 );
    }
}
