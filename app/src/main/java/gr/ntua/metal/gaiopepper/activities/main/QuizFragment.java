package gr.ntua.metal.gaiopepper.activities.main;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.aldebaran.qi.sdk.object.conversation.AutonomousReactionImportance;
import com.aldebaran.qi.sdk.object.conversation.AutonomousReactionValidity;
import com.aldebaran.qi.sdk.object.conversation.Bookmark;

import gr.ntua.metal.gaiopepper.R;

public class QuizFragment extends Fragment implements View.OnClickListener {
    private static final String TAG = "Quiz Fragment";

    private TextView textViewQuestion;
    private Button buttonAnswer1;
    private Button buttonAnswer2;
    private Button buttonAnswer3;
    private Button buttonAnswer4;
    private ImageButton buttonClose;

    private MainActivity mainActivity;

    /**
     * _____________________________________________________________________________
     * <h1>Override Methods</h1>
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        mainActivity = (MainActivity) requireActivity();

        Bookmark questionBookmark = mainActivity.quizManager.questionsEN.get("QUESTION.2");
        assert questionBookmark != null : "questionBookmark is null";

        mainActivity.chatManager.chatbot.async().goToBookmark(questionBookmark, AutonomousReactionImportance.HIGH, AutonomousReactionValidity.IMMEDIATE);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: ");


        return inflater.inflate(R.layout.quiz_view, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onViewCreated: ");
        super.onViewCreated(view, savedInstanceState);

        findViews();
        addListeners();




        //todo: fix stuff here

        Bookmark answerBookmark1 = mainActivity.quizManager.answersEN.get("ANSWER.1.A");
        assert answerBookmark1 != null : "answerBookmark1 is null";
        Bookmark answerBookmark2 = mainActivity.quizManager.answersEN.get("ANSWER.1.B");
        assert answerBookmark2 != null : "answerBookmark2 is null";
        Bookmark answerBookmark3 = mainActivity.quizManager.answersEN.get("ANSWER.1.C");
        assert answerBookmark3 != null : "answerBookmark3 is null";
        Bookmark answerBookmark4 = mainActivity.quizManager.answersEN.get("ANSWER.1.D");
        assert answerBookmark4 != null : "answerBookmark4 is null";


    }

    @Override
    public void onStart() {
        Log.d(TAG, "onStart: ");
        super.onStart();


    }

    @Override
    public void onClick(View view) {
        int viewID = view.getId();
        if (viewID == R.id.btn_answer1) {
            Log.d(TAG, "Touched button1 ");
        } else if (viewID == R.id.btn_answer2) {
            Log.d(TAG, "Touched button2 ");

        } else if (viewID == R.id.btn_answer3) {
            Log.d(TAG, "Touched button3: ");

        } else if (viewID == R.id.btn_answer4) {
            Log.d(TAG, "Touched button4 ");

        } else if (viewID == R.id.btn_close) {
            Log.d(TAG, "Touched button close ");
            mainActivity.changeFragment(mainActivity.chatFragment);
        }
    }


    /**
     * _____________________________________________________________________________
     * <h1>UI Methods</h1>
     */
    private void findViews() {
        View rootView = getView();
        assert rootView != null;
        textViewQuestion = rootView.findViewById(R.id.tv_question);
        assert textViewQuestion != null : "textViewQuestion is null";
        buttonAnswer1 = rootView.findViewById(R.id.btn_answer1);
        assert buttonAnswer1 != null : "buttonAnswer1 is null";
        buttonAnswer2 = rootView.findViewById(R.id.btn_answer2);
        assert buttonAnswer2 != null : "buttonAnswer2 is null";
        buttonAnswer3 = rootView.findViewById(R.id.btn_answer3);
        assert buttonAnswer3 != null : "buttonAnswer3 is null";
        buttonAnswer4 = rootView.findViewById(R.id.btn_answer4);
        assert buttonAnswer4 != null : "buttonAnswer4 is null";
        buttonClose = rootView.findViewById(R.id.btn_close);
        assert buttonClose != null : "buttonCLose is null";

    }

    private void addListeners() {
        buttonAnswer1.setOnClickListener(this);
        buttonAnswer2.setOnClickListener(this);
        buttonAnswer3.setOnClickListener(this);
        buttonAnswer4.setOnClickListener(this);
        buttonClose.setOnClickListener(this);
    }

    public void setQuestion(String question) {
        textViewQuestion.setText(question);
    }

    public void setAnswer(String answerID, String answer) {

        switch (answerID) {
            case "A":
                buttonAnswer1.setText(answer);
                break;
            case "B":
                buttonAnswer2.setText(answer);
                break;
            case "C":
                buttonAnswer3.setText(answer);
                break;
            case "D":
                buttonAnswer4.setText(answer);
                break;

        }
    }


}

