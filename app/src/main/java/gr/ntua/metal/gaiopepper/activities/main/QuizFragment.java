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

    }

    @Override
    public void onClick(View view) {
        int viewID = view.getId();
        if (viewID == R.id.btn_answer1) {
            Log.d(TAG, "Touched button: "+ view.toString());
        } else if (viewID == R.id.btn_answer2) {
            Log.d(TAG, "Touched button: "+ view.toString());

        } else if (viewID == R.id.btn_answer3) {
            Log.d(TAG, "Touched button: "+ view.toString());

        } else if (viewID == R.id.btn_answer4) {
            Log.d(TAG, "Touched button: "+ view.toString());

        } else if (viewID == R.id.btn_close) {
            Log.d(TAG, "Touched button: "+ view.toString());
            mainActivity.changeFragment(mainActivity.chatFragment);
        }
    }


    /**
     * _____________________________________________________________________________
     * <h1>UI Methods</h1>
     */
    private void findViews() {
        textViewQuestion = getView().findViewById(R.id.tv_question);
        assert textViewQuestion != null : "textViewQuestion is null";
        buttonAnswer1 = getView().findViewById(R.id.btn_answer1);
        assert buttonAnswer1 != null : "buttonAnswer1 is null";
        buttonAnswer2 = getView().findViewById(R.id.btn_answer2);
        assert buttonAnswer2 != null : "buttonAnswer2 is null";
        buttonAnswer3 = getView().findViewById(R.id.btn_answer3);
        assert buttonAnswer3 != null : "buttonAnswer3 is null";
        buttonAnswer4 = getView().findViewById(R.id.btn_answer4);
        assert buttonAnswer4 != null : "buttonAnswer4 is null";
        buttonClose = getView().findViewById(R.id.btn_close);
        assert buttonClose != null : "buttonCLose is null";

    }

    private void addListeners() {
        buttonAnswer1.setOnClickListener(this);
        buttonAnswer2.setOnClickListener(this);
        buttonAnswer3.setOnClickListener(this);
        buttonAnswer4.setOnClickListener(this);
        buttonClose.setOnClickListener(this);
    }

}

