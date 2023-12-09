package gr.ntua.metal.gaiopepper.util;

import android.util.Log;

import com.aldebaran.qi.sdk.object.locale.Language;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    public static String extractLanguageCode(Language language) {
        String languageName = language.name();
        if (languageName != null && languageName.length() >= 2) {
            // Extract the first two characters and capitalize them
            String shortenedAndCapitalized = languageName.substring(0, 2).toUpperCase();
            return shortenedAndCapitalized;
        } else {
            return languageName;
        }
    }

    public static String extractQuestionAfterBookmark(String name, String content) {
        Pattern pattern = Pattern.compile(name + "([^?]+\\?)");
        Matcher matcher = pattern.matcher(content);

        while (matcher.find()) {
            String result = matcher.group(1);
            return result != null ? result.trim() : null;
        }
        return null;
    }

    public static Map<String, String> extractAnswersForQuestion(String name, String content) {
        Map<String, String> answersMap = new HashMap<>();
        String questionNumber = extractNumberAfterDot(name);

        Pattern pattern = Pattern.compile("u1:\\(\\[~([A-D]) \"(.*?)\"\\]\\) %ANSWER\\." + questionNumber + "\\.[A-D]");
        Matcher matcher = pattern.matcher(content);

        while (matcher.find()) {
            String answerLetter = matcher.group(1);
            String answerText = matcher.group(2);
            //Log.d(TAG, "Letter: " + answerLetter + ", Found: " + answerText);
            answersMap.put(answerLetter, answerText);
        }
        return answersMap.isEmpty() ? null : answersMap;
    }

    private static String extractNumberAfterDot(String input) {
        Pattern pattern = Pattern.compile("\\.(\\d+)$");
        Matcher matcher = pattern.matcher(input);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    public static String extractProposal(String bookmarkName, String content) {
        String regex = "(proposal: %" + bookmarkName + ".+?)(?:proposal:|%$|$)";
        Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
        Matcher matcher = pattern.matcher(content);

        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return null;
    }

    public static Object checkVariablesForSubstring(IManager activity, String targetSubstring) {
        Field[] fields = activity.getClass().getDeclaredFields();
        for (Field field : fields) {
            String fieldName = field.getName();
            if (fieldName.contains(targetSubstring)) {
                Log.d(TAG, "Found variable with name: " + fieldName);
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
