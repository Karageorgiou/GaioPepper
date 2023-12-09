package gr.ntua.metal.gaiopepper.util;

import android.util.Log;

import androidx.core.util.Pair;
import androidx.fragment.app.Fragment;

import com.aldebaran.qi.sdk.object.conversation.Bookmark;
import com.aldebaran.qi.sdk.object.locale.Language;
import com.aldebaran.qi.sdk.object.locale.Locale;
import com.aldebaran.qi.sdk.object.locale.Region;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import gr.ntua.metal.gaiopepper.R;
import gr.ntua.metal.gaiopepper.activities.main.MainActivity;
import gr.ntua.metal.gaiopepper.activities.main.QuizFragment;

import static gr.ntua.metal.gaiopepper.models.MessageItem.LayoutUser;


public class QuizManager implements IManager {
    private static final String TAG = "Quiz Manager";

    private final MainActivity mainActivity;
    public final Locale localeGR;
    public final Locale localeEN;


    public Map<String, Bookmark> questionsEN;
    public Map<String, Bookmark> questionsGR;
    public Map<String, Bookmark> answersEN;
    public Map<String, Bookmark> answersGR;

    private String currentContent = "";
    private String currentQuestion = "";
    private Map<String, String> currentAnswers = new HashMap<>();

    public QuizManager(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
        this.localeGR = new Locale(Language.GREEK, Region.GREECE);
        this.localeEN = new Locale(Language.ENGLISH, Region.UNITED_STATES);

        this.questionsEN = new HashMap<>();
        this.questionsGR = new HashMap<>();
        this.answersEN = new HashMap<>();
        this.answersGR = new HashMap<>();
    }

    public void setContent(Fragment fragment, String name, String content) {
        if (content == null) {
            Log.e(TAG, "Content is NULL");
            return;
        }
        currentContent = content;

        String question = StringUtility.extractQuestionAfterBookmark(name, content);
        Map<String, String> answersMap = StringUtility.extractAnswersForQuestion(name, content);
        if (fragment instanceof QuizFragment) {
            if (question != null) {
                //Log.d(TAG, "Question: " + question);
                currentQuestion = question;
                ((QuizFragment) fragment).setQuestion(question);
            }
            if (answersMap != null) {
                currentAnswers = answersMap;
                for (String answerID : answersMap.keySet()) {
                    //Log.d(TAG, "ID: " + answerID + " key: " + answersMap.get(answerID));
                    ((QuizFragment) fragment).setAnswer(answerID, answersMap.get(answerID));
                }
            }
        }
    }

    public Bookmark getRandomQuestionBookmark(Map<String, Bookmark> questions) {
        // Get a list of keys
        List<String> keysList = new ArrayList<>(questions.keySet());

        // Shuffle the list
        Collections.shuffle(keysList);

        // Get the first element from the shuffled list
        String randomKey = keysList.get(0);

        // Get the corresponding Bookmark
        Bookmark randomBookmark = questions.get(randomKey);
        return randomBookmark;
    }

    public void answerToQuestion(String answer, Locale locale) {
        mainActivity.chatManager.setContent(mainActivity.chatFragment, new Pair<>(LayoutUser, answer));
        mainActivity.chatManager.replyTo(answer, locale);

        Map<String, String> solutions = StringUtility.extractAnswerCorrectness(currentContent);
        for (String solutionID : solutions.keySet()) {
            for (String answerID : currentAnswers.keySet()) {
                if (solutionID.equals(answerID)) {
                    if (solutions.get(answerID).equals("CORRECT")) {
                        mainActivity.quizFragment.changeButtonColor(answerID, R.color.green);
                    }
                    if (solutions.get(answerID).equals("FALSE")) {
                        mainActivity.quizFragment.changeButtonColor(answerID, R.color.red);
                    }
                    //Log.d(TAG, "Answer " + solutionID + " is " + solutions.get(solutionID));
                }
            }
        }

    }


}
