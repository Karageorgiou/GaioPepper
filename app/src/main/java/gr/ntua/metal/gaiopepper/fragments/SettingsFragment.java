package gr.ntua.metal.gaiopepper.fragments;


import android.os.Bundle;
import android.util.Log;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import gr.ntua.metal.gaiopepper.AutonomousAbilitiesController;
import gr.ntua.metal.gaiopepper.R;
import gr.ntua.metal.gaiopepper.activities.SettingsActivity;

public class SettingsFragment extends PreferenceFragmentCompat {
    private static final String TAG = "Settings Activity" ;

    Preference prefAutonomousBlinking;
    Preference prefBackgroundMovement;
    Preference prefBasicAwareness;
    Preference prefVolume;
    Preference prefListening;



    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences_root, rootKey);

        SettingsActivity settingsActivity = (SettingsActivity) getActivity();
        assert settingsActivity != null;

        findPreferences();

        prefAutonomousBlinking.setOnPreferenceChangeListener((preference11, newValue) -> {
            Log.i(TAG, "Preference Autonomous Blinking set to: " + newValue.toString());
            if (newValue.equals(true)) {
            } else if (newValue.equals(false)) {
            }
            return true;
        });
        prefBackgroundMovement.setOnPreferenceChangeListener((preference12, newValue) -> {
            Log.i(TAG, "Preference Background Movement set to: " + newValue.toString());
            if (newValue.equals(true)) {

            } else if (newValue.equals(false)) {

            }
            return true;
        });
        prefBasicAwareness.setOnPreferenceChangeListener((preference13, newValue) -> {
            Log.i(TAG, "Preference Basic Awareness set to: " + newValue.toString());
            if (newValue.equals(true)) {
            } else if (newValue.equals(false)) {
            }
            return true;
        });
        prefVolume.setOnPreferenceChangeListener((preference21, newValue) -> {
            Log.i(TAG, "Preference Volume set to: " + newValue.toString());
            return true;
        });
        prefListening.setOnPreferenceChangeListener((preference22, newValue) -> {
            Log.i(TAG, "Preference Robot Listening set to: " + newValue.toString());
            return true;
        });

    }

    private void findPreferences() {
        try {
            prefAutonomousBlinking = findPreference("autonomous_blinking");
            prefBackgroundMovement = findPreference("background_movement");
            prefBasicAwareness = findPreference("basic_awareness");
            prefVolume = findPreference("volume");
            prefListening = findPreference("microphone");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }






}
