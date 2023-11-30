package gr.ntua.metal.gaiopepper.fragments;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.preference.RingtonePreference;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;

import androidx.annotation.NonNull;
import androidx.preference.CheckBoxPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreference;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import gr.ntua.metal.gaiopepper.R;
import gr.ntua.metal.gaiopepper.activities.SettingsActivity;

public class SettingsFragment extends PreferenceFragmentCompat {
    private static final String TAG = "Settings Activity";

    Preference prefAutonomousBlinking;
    Preference prefBackgroundMovement;
    Preference prefBasicAwareness;
    Preference prefVolume;
    Preference prefListening;
    CheckBoxPreference prefResetChat;

    AlertDialog.Builder alert;


    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences_root, rootKey);

        SettingsActivity settingsActivity = (SettingsActivity) getActivity();
        assert settingsActivity != null;

        findPreferences();

        alert = new AlertDialog.Builder(this.getActivity());


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

        prefResetChat.setChecked(false);
        prefResetChat.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(@NonNull Preference preference, Object newValue) {
                Log.d(TAG, "[onPreferenceChange]: " + newValue);

                prefResetChat.setChecked((boolean) newValue);


                return false;
            }
        });
        prefResetChat.setOnPreferenceClickListener(preference -> {
            alert.setTitle(getResources().getString(R.string.reset_chat_dialog_title));
            alert.setMessage(R.string.reset_chat_dialog);
            alert.setNegativeButton("No", (dialog, whichButton) -> {
                Log.d(TAG, "[setNegativeButton]");
                preference.callChangeListener(false);

            });
            alert.setPositiveButton("Yes", (dialog, whichButton) -> {
                Log.d(TAG, "[setPositiveButton]");
                preference.callChangeListener(true);

            });
            alert.setOnCancelListener(dialogInterface -> {
                Log.d(TAG, "[onCancel]");
                preference.callChangeListener(false);
            });
            alert.setCancelable(true);
            if (PreferenceManager.getDefaultSharedPreferences(this.requireContext()).getBoolean(getString(R.string.RESET_CHAT_KEY), false)) {
                alert.show();
            }
            return true;
        });

    }

    private void findPreferences() {
        try {
            prefAutonomousBlinking = findPreference(getString(R.string.AUTONOMOUS_BLINKING_KEY));
            prefBackgroundMovement = findPreference(getString(R.string.BACKGROUND_MOVEMENT_KEY));
            prefBasicAwareness = findPreference(getString(R.string.BASIC_AWARENESS_KEY));
            prefVolume = findPreference(getString(R.string.VOLUME_KEY));
            prefListening = findPreference(getString(R.string.MICROPHONE_KEY));
            prefResetChat = findPreference(getString(R.string.RESET_CHAT_KEY));

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
