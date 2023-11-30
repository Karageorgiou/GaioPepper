package gr.ntua.metal.gaiopepper.activities;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import gr.ntua.metal.gaiopepper.R;

public class EncyclopediaActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_encyclopedia);


        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(R.string.ENCYCLOPEDIA_TITLE);

        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
