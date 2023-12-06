package gr.ntua.metal.gaiopepper.util;

import android.app.Activity;
import android.util.Log;

import com.aldebaran.qi.sdk.object.locale.Language;

import java.lang.reflect.Field;

public class StringUtility {
    private static final String TAG = "String Utility";


    public static String formatMessage(String message) {
        String regex = "\\\\.*?\\\\";

        // Replace characters between backslashes with an empty string
        String formattedMessage = message.replaceAll(regex, "");

        // Ensure there's one space between words and punctuation marks
        formattedMessage = formattedMessage.replaceAll("\\s+", " ");
        formattedMessage = formattedMessage.replaceAll("\\s+([.,;?!])", "$1");

        // Capitalize the first letter of the first word
        formattedMessage = capitalizeFirstLetter(formattedMessage);

        // Capitalize the first letter after '.', '!', '?', or ';'
        formattedMessage = formattedMessage.replaceAll("([.!?;])\\s*([a-z])", "$1 $2".toUpperCase());

        return formattedMessage;
    }

    public static String capitalizeFirstLetter(String input) {
        if (input != null && !input.isEmpty()) {
            return input.substring(0, 1).toUpperCase() + input.substring(1);
        } else {
            return input;
        }
    }

    public static String getLanguageCode(Language language) {
        String languageName = language.name();
        Log.d(TAG,"LANG: " + language);
        // Check if the username is not null and has at least two characters
        if (languageName != null && languageName.length() >= 2) {
            // Extract the first two characters and capitalize them
            String shortenedAndCapitalized = languageName.substring(0, 2).toUpperCase();
            Log.d(TAG,"CODE: " + shortenedAndCapitalized);
            return shortenedAndCapitalized;
        } else {
            // Return the original username if it doesn't meet the criteria
            return languageName;
        }
    }

    public static Object checkVariablesForSubstring(Activity activity, String targetSubstring) {
        Field[] fields = activity.getClass().getDeclaredFields();
        for (Field field : fields) {
            String fieldName = field.getName();
            if (fieldName.contains(targetSubstring)) {
                try {
                    Object value = field.get(activity);
                    return value;
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }
}
