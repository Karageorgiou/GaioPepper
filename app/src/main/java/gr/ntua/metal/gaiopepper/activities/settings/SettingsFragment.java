package gr.ntua.metal.gaiopepper.activities.settings;


import android.app.AlertDialog;
import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.CheckBoxPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.PreferenceScreen;
import androidx.preference.SeekBarPreference;
import androidx.preference.SwitchPreferenceCompat;

import gr.ntua.metal.gaiopepper.R;

public class SettingsFragment extends PreferenceFragmentCompat implements Preference.OnPreferenceChangeListener, Preference.OnPreferenceClickListener {
    private static final String TAG = "Settings Activity";

    PreferenceScreen preferenceScreen;

    SwitchPreferenceCompat prefAutonomousBlinking;
    SwitchPreferenceCompat prefBackgroundMovement;
    SwitchPreferenceCompat prefBasicAwareness;
    SeekBarPreference prefVolume;
    SwitchPreferenceCompat prefListening;
    CheckBoxPreference prefResetChatState;
    CheckBoxPreference prefResetChatLayout;

    AlertDialog.Builder alert;
    AudioManager audioManager;


    /**
     * Called during {@link #onCreate(Bundle)} to supply the preferences for this fragment.
     * Subclasses are expected to call {@link #setPreferenceScreen(PreferenceScreen)} either
     * directly or via helper methods such as {@link #addPreferencesFromResource(int)}.
     *
     * @param savedInstanceState If the fragment is being re-created from a previous saved state,
     *                           this is the state.
     * @param rootKey            If non-null, this preference fragment should be rooted at the
     *                           {@link PreferenceScreen} with this key.
     */
    @Override
    public void onCreatePreferences(@Nullable Bundle savedInstanceState, @Nullable String rootKey) {
        setPreferencesFromResource(R.xml.preferences_root, rootKey);

        SettingsActivity settingsActivity = (SettingsActivity) getActivity();
        assert settingsActivity != null;

        alert = new AlertDialog.Builder(this.getActivity());

        audioManager = (AudioManager) getActivity().getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
        findPreferences();
        addListeners();

    }

    /**
     * Called when a preference has been clicked.
     *
     * @param preference The preference that was clicked
     * @return {@code true} if the click was handled
     */
    @Override
    public boolean onPreferenceClick(@NonNull Preference preference) {
        String preferenceKey = preference.getKey();
        if (preferenceKey.equals(getString(R.string.RESET_CHAT_STATE_KEY))) {
            if (!prefResetChatState.isChecked()) {
                prefResetChatState.callChangeListener(false);
            } else if (prefResetChatState.isChecked()) {
                alert.setTitle(R.string.RESET_CHAT_STATE_DIALOG_TITLE);
                alert.setMessage(R.string.RESET_CHAT_STATE_DIALOG_MESSAGE);
                alert.setNegativeButton(R.string.NO, (dialog, whichButton) -> {
                    Log.d(TAG, "[setNegativeButton]");
                    preference.callChangeListener(false);
                });
                alert.setPositiveButton(R.string.YES, (dialog, whichButton) -> {
                    Log.d(TAG, "[setPositiveButton]");
                    preference.callChangeListener(true);
                });
                alert.setOnCancelListener(dialogInterface -> {
                    Log.d(TAG, "[onCancel]");
                    preference.callChangeListener(false);
                });
                alert.setCancelable(true);
                if (PreferenceManager.getDefaultSharedPreferences(this.requireContext()).getBoolean(getString(R.string.RESET_CHAT_STATE_KEY), false)) {
                    alert.show();
                }
            }
            return true;
        } else if (preferenceKey.equals(getString(R.string.RESET_CHAT_LAYOUT_KEY))) {
            if (!prefResetChatLayout.isChecked()) {
                prefResetChatLayout.callChangeListener(false);
            } else if (prefResetChatLayout.isChecked()) {
                alert.setTitle(R.string.RESET_CHAT_LAYOUT_DIALOG_TITLE);
                alert.setMessage(R.string.RESET_CHAT_LAYOUT_DIALOG_MESSAGE);
                alert.setNegativeButton(R.string.NO, (dialog, whichButton) -> {
                    Log.d(TAG, "[setNegativeButton]");
                    preference.callChangeListener(false);

                });
                alert.setPositiveButton(R.string.YES, (dialog, whichButton) -> {
                    Log.d(TAG, "[setPositiveButton]");
                    preference.callChangeListener(true);

                });
                alert.setOnCancelListener(dialogInterface -> {
                    Log.d(TAG, "[onCancel]");
                    preference.callChangeListener(false);
                });
                alert.setCancelable(true);
                if (PreferenceManager.getDefaultSharedPreferences(this.requireContext()).getBoolean(getString(R.string.RESET_CHAT_LAYOUT_KEY), false)) {
                    alert.show();
                }
            }
            return true;
        }
        return false;
    }

    /**
     * Called when a preference has been changed by the user. This is called before the state
     * of the preference is about to be updated and before the state is persisted.
     *
     * @param preference The changed preference
     * @param newValue   The new value of the preference
     * @return {@code true} to update the state of the preference with the new value
     */
    @Override
    public boolean onPreferenceChange(@NonNull Preference preference, Object newValue) {
        String preferenceKey = preference.getKey();
        if (preferenceKey.equals(getString(R.string.AUTONOMOUS_BLINKING_KEY))) {
            prefAutonomousBlinking.setChecked((boolean) newValue);
            return true;
        } else if (preferenceKey.equals(getString(R.string.BASIC_AWARENESS_KEY))) {
            prefBasicAwareness.setChecked((boolean) newValue);
            return true;
        } else if (preferenceKey.equals(getString(R.string.BACKGROUND_MOVEMENT_KEY))) {
            prefBackgroundMovement.setChecked((boolean) newValue);
            return true;
        } else if (preferenceKey.equals(getString(R.string.MICROPHONE_KEY))) {
            prefListening.setChecked((boolean) newValue);
            return true;
        } else if (preferenceKey.equals(getString(R.string.RESET_CHAT_STATE_KEY))) {
            prefResetChatState.setChecked((boolean) newValue);
            return true;
        } else if (preferenceKey.equals(getString(R.string.RESET_CHAT_LAYOUT_KEY))) {
            prefResetChatLayout.setChecked((boolean) newValue);
            return true;
        }
        return false;
    }

    private void addListeners() {
        prefAutonomousBlinking.setOnPreferenceChangeListener(this);
        prefBackgroundMovement.setOnPreferenceChangeListener(this);
        prefBasicAwareness.setOnPreferenceChangeListener(this);
        prefVolume.setSeekBarIncrement(audioManager.getStreamVolume(AudioManager.STREAM_SYSTEM));
        prefVolume.setOnPreferenceChangeListener(this);
        prefVolume.setOnPreferenceChangeListener((preference21, newValue) -> {
            Log.i(TAG, "Preference Volume set to: " + newValue.toString());
            audioManager.setStreamVolume(AudioManager.STREAM_SYSTEM, (Integer) newValue, AudioManager.FLAG_PLAY_SOUND);
            return true;
        });

        prefResetChatState.setChecked(false);
        prefResetChatState.setOnPreferenceChangeListener(this);
        prefResetChatState.setOnPreferenceClickListener(this);

        prefResetChatLayout.setChecked(false);
        prefResetChatLayout.setOnPreferenceChangeListener(this);
        prefResetChatLayout.setOnPreferenceClickListener(this);
    }

    private void findPreferences() {
        try {
            preferenceScreen = findPreference(getString(R.string.PREFERENCE_SCREEN_KEY));

            prefAutonomousBlinking = findPreference(getString(R.string.AUTONOMOUS_BLINKING_KEY));
            prefBackgroundMovement = findPreference(getString(R.string.BACKGROUND_MOVEMENT_KEY));
            prefBasicAwareness = findPreference(getString(R.string.BASIC_AWARENESS_KEY));
            prefVolume = findPreference(getString(R.string.VOLUME_KEY));
            prefListening = findPreference(getString(R.string.MICROPHONE_KEY));
            prefResetChatState = findPreference(getString(R.string.RESET_CHAT_STATE_KEY));
            prefResetChatLayout = findPreference(getString(R.string.RESET_CHAT_LAYOUT_KEY));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
