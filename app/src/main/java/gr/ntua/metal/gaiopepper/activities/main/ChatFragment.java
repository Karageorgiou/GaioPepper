package gr.ntua.metal.gaiopepper.activities.main;

import static gr.ntua.metal.gaiopepper.models.MessageItem.LayoutRobot;
import static gr.ntua.metal.gaiopepper.models.MessageItem.LayoutRobotImage;
import static gr.ntua.metal.gaiopepper.models.MessageItem.LayoutUser;
import static gr.ntua.metal.gaiopepper.models.MessageItem.LayoutUserImage;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.util.Pair;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aldebaran.qi.sdk.object.conversation.Phrase;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Objects;

import gr.ntua.metal.gaiopepper.R;
import gr.ntua.metal.gaiopepper.models.MessageItem;
import gr.ntua.metal.gaiopepper.util.ImageManager;
import gr.ntua.metal.gaiopepper.util.StringUtility;
import gr.ntua.metal.gaiopepper.util.fsm.discussion.DiscussionState;

public class ChatFragment extends Fragment implements View.OnClickListener {
    private static final String TAG = "Chat Fragment";

    private InputMethodManager inputMethodManager;

    private TextInputLayout textInputLayout;
    private TextInputEditText textInputEditText;
    private RecyclerView recyclerView;
    private ImageButton buttonSend;
    private ImageView expandedImageView;
    private RelativeLayout expandedImageRelativeLayout;
    private ConstraintLayout constraintLayoutBottom;

    private MainActivity mainActivity;

    private String conversationMode;

    /**
     * _____________________________________________________________________________
     * <h1>Override Methods</h1>
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        //Log.i(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        mainActivity = (MainActivity) requireActivity();


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //Log.d(TAG, "onCreateView: ");




        return inflater.inflate(R.layout.chat_view, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        //Log.d(TAG, "onViewCreated: ");
        super.onViewCreated(view, savedInstanceState);

        findViews();
        addListeners();

        inputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        ImageManager.setExpandedImageView(expandedImageView, expandedImageRelativeLayout);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mainActivity);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(mainActivity.messageAdapter);

        conversationMode = getArguments().getString(getString(R.string.CONVERSATION_MODE_KEY));
        applyPreferences();

        recyclerView.scrollToPosition(mainActivity.messageItemList.size()-1);

    }


    @Override
    public void onClick(View view) {
        int viewID = view.getId();
        if (viewID == R.id.editTextHumanInput) {
            textInputLayout.clearFocus();
            hideSoftKeyboard(textInputLayout);
        } else if (viewID == R.id.btn_send) {
            String message = Objects.requireNonNull(textInputEditText.getText()).toString();
            if (!message.isEmpty()) {
                mainActivity.chatManager.setContent(this, new Pair<>(LayoutUser, message));
                mainActivity.chatManager.replyTo(message, mainActivity.chatManager.getCurrentLocale());
                textInputEditText.getText().clear();
            }
        }
    }


    /**
     * _____________________________________________________________________________
     * <h1>UI Methods</h1>
     */
    private void findViews() {
        View rootView = getView();
        assert rootView != null;

        textInputLayout = rootView.findViewById(R.id.editTextLayout);
        assert textInputLayout != null : "textInputLayout is null";

        textInputEditText = (TextInputEditText) textInputLayout.getEditText();
        assert textInputEditText != null : "textInputEditText is null";

        recyclerView = rootView.findViewById(R.id.recycler_view);
        assert recyclerView != null : "recyclerView is null";

        buttonSend = rootView.findViewById(R.id.btn_send);
        assert buttonSend != null : "buttonSend is null";

        constraintLayoutBottom = rootView.findViewById(R.id.chatView_constraint_bottom);
        assert constraintLayoutBottom != null : "constraintLayoutBottom is null";

        expandedImageView = mainActivity.findViewById(R.id.expanded_image);
        assert expandedImageView != null : "expandedImageView is null";

        expandedImageRelativeLayout = mainActivity.findViewById(R.id.expanded_image_layer);
        assert expandedImageRelativeLayout != null : "expandedImageRelativeLayout is null";
    }

    private void addListeners() {
        textInputEditText.setOnClickListener(this);
        buttonSend.setOnClickListener(this);
    }

    private void applyPreferences() {
        if (Objects.equals(conversationMode, getString(R.string.NONE_VALUE))) {
            //mainActivity.discussionFSM.changeState(DiscussionState.none, null);
            //mainActivity.chatManager.tryCancelChat();
            hideTextInput();
        } else if (Objects.equals(conversationMode, getString(R.string.ORAL_CONVERSATION_VALUE))) {
            //mainActivity.discussionFSM.changeState(DiscussionState.oral, null);
            hideTextInput();
        } else if (Objects.equals(conversationMode, getString(R.string.WRITTEN_CONVERSATION_VALUE))) {
            //mainActivity.discussionFSM.changeState(DiscussionState.written, null);
            //mainActivity.chatManager.tryCancelChat();
            showTextInput();
        }
    }

    public void updateRecyclerView(int messageLayout, int image) {
        mainActivity.timingManager.checkForDuplicates(image, purged -> {
            //Log.d(TAG, "Updating recyclerView");
            if (purged) {
                return;
            }
            switch (messageLayout) {
                case LayoutRobotImage:
                    mainActivity.runOnUiThread(() -> {
                        mainActivity.messageAdapter.addItem(new MessageItem(messageLayout, R.drawable.ic_pepper_w, image));
                        if(mainActivity.messageItemList.size()>1) {
                            recyclerView.scrollToPosition(mainActivity.messageItemList.size() - 1);
                        }
                        ImageManager.updateImage(image);
                        ImageManager.showImageForSeconds(4);
                    });

                    break;
                case LayoutUserImage:
                    mainActivity.runOnUiThread(() -> {
                        mainActivity.messageAdapter.addItem(new MessageItem(messageLayout, R.drawable.ic_user, image));
                        if(mainActivity.messageItemList.size()>1) {
                            recyclerView.scrollToPosition(mainActivity.messageItemList.size() - 1);
                        }
                        ImageManager.updateImage(image);
                        ImageManager.showImageForSeconds(4);
                    });
                    break;
            }
            return;
        });
    }

    public void updateRecyclerView(int messageLayout, String message) {
        mainActivity.timingManager.checkForDuplicates(message, purged -> {
            //Log.d(TAG, "Updating recyclerView");
            if (purged) {
                return;
            }
            String formattedMessage = StringUtility.formatMessage(message);
            switch (messageLayout) {
                case LayoutRobot:
                    mainActivity.runOnUiThread(() -> {
                        mainActivity.messageAdapter.addItem(new MessageItem(messageLayout, R.drawable.ic_pepper_w, formattedMessage));
                        if(mainActivity.messageItemList.size()>1) {
                            recyclerView.scrollToPosition(mainActivity.messageItemList.size() - 1);
                        }
                    });
                    break;
                case LayoutUser:
                    mainActivity.runOnUiThread(() -> {
                        mainActivity.messageAdapter.addItem(new MessageItem(messageLayout, R.drawable.ic_user, formattedMessage));
                        if(mainActivity.messageItemList.size()>1) {
                            recyclerView.scrollToPosition(mainActivity.messageItemList.size() - 1);
                        }
                    });
                    break;
            }
            return;
        });
    }

    protected void hideSoftKeyboard(View view) {
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public void hideTextInput() {
        if (constraintLayoutBottom != null) {
            constraintLayoutBottom.setMaxHeight(0);
        }
    }

    public void showTextInput() {
        ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) constraintLayoutBottom.getLayoutParams();
        layoutParams.height = 150;
        constraintLayoutBottom.setLayoutParams(layoutParams);
        constraintLayoutBottom.setMaxHeight(150);
    }
}
