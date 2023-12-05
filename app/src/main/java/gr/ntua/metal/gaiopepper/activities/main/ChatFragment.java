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
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aldebaran.qi.sdk.object.conversation.Bookmark;
import com.aldebaran.qi.sdk.object.conversation.Chat;
import com.aldebaran.qi.sdk.object.conversation.Phrase;
import com.aldebaran.qi.sdk.object.conversation.QiChatbot;
import com.aldebaran.qi.sdk.object.conversation.SpeechEngine;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Objects;

import gr.ntua.metal.gaiopepper.util.ImageManager;
import gr.ntua.metal.gaiopepper.R;
import gr.ntua.metal.gaiopepper.models.MessageItem;

public class ChatFragment extends Fragment implements View.OnClickListener, QiChatbot.OnBookmarkReachedListener, SpeechEngine.OnSayingChangedListener, Chat.OnHeardListener, Chat.OnSayingChangedListener {
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
        return inflater.inflate(R.layout.chat_view, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onViewCreated: ");
        super.onViewCreated(view, savedInstanceState);

        findViews();
        addListeners();

        inputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        ImageManager.setExpandedImageView(expandedImageView, expandedImageRelativeLayout);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(mainActivity.messageAdapter);
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
                Phrase userPhrase = new Phrase(message);
                Log.d(TAG, "[onClick] User message: " + userPhrase.getText());
                try {
                    updateRecyclerView(LayoutUser, message);
                    mainActivity.replyTo(userPhrase, mainActivity.localeEN);
                    textInputEditText.getText().clear();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Emitted when a Bookmark is reached
     *
     * @param bookmark the bookmark value
     * @since 3
     */
    @Override
    public void onBookmarkReached(Bookmark bookmark) {
        Log.i(TAG, "[CHATBOT] Bookmark " + bookmark.getName() + " reached.");
        switch (bookmark.getName()) {
            case "BAUXITE":
                updateRecyclerView(LayoutRobotImage, R.drawable.red_bauxite_pissoliths);
                break;
            case "PISOLITH":
                updateRecyclerView(LayoutRobotImage, R.drawable.white_bauxite);
                break;
            case "BAUXITE.3":
                updateRecyclerView(LayoutRobotImage, R.drawable.aluminium);
                break;
            default:
                break;
        }
        mainActivity.lastBookmark = bookmark;
    }

    /**
     * The Phrase said by a Say action from this factory while it is running.
     * This value is set when a Say action from this factory starts running
     * and set to an empty Phrase when the action stops.
     *
     * @param phrase the phrase value
     * @since 3
     */
    @Override
    public void onSayingChanged(Phrase phrase) {
        String sayingText = phrase.getText();
        if (!sayingText.isEmpty()) {
            Log.i(TAG, "[SPEECH ENGINE]/[CHAT] Pepper Reply: " + sayingText);
            updateRecyclerView(LayoutRobot, sayingText);
        }
    }

    /**
     * The Phrase heard, emitted once the robot has heard a valid Phrase.
     *
     * @param heardPhrase the heardPhrase value
     * @since 3
     */
    @Override
    public void onHeard(Phrase heardPhrase) {
        String heardText = heardPhrase.getText();
        if (!heardText.isEmpty()) {
            Log.i(TAG, "[CHAT] Heard phrase: " + heardPhrase.getText());
            updateRecyclerView(LayoutUser, heardText);
        }
    }

    /**
     * _____________________________________________________________________________
     * <h1>UI Methods</h1>
     */
    private void findViews() {
        textInputLayout = getView().findViewById(R.id.editTextLayout);
        assert textInputLayout != null : "textInputLayout is null";

        textInputEditText = (TextInputEditText) textInputLayout.getEditText();
        assert textInputEditText != null : "textInputEditText is null";

        recyclerView = getView().findViewById(R.id.recycler_view);
        assert recyclerView != null : "recyclerView is null";

        buttonSend = getView().findViewById(R.id.btn_send);
        assert buttonSend != null : "buttonSend is null";

        constraintLayoutBottom = getView().findViewById(R.id.chatView_constraint_bottom);
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

    private boolean purgeDuplicateMessages(int image) {
        long currentTime = System.currentTimeMillis();
        long difference = Math.abs(mainActivity.lastImageUpdate - currentTime);
        if (image == mainActivity.lastImage && difference < 1500) {
            Log.d(TAG, "Time between messages: " + difference + "ms. Duplicate message purged.");
            return true;
        }
        mainActivity.lastImage = image;
        mainActivity.lastImageUpdate = currentTime;
        return false;
    }

    private boolean purgeDuplicateMessages(String message) {
        long currentTime = System.currentTimeMillis();
        long difference = Math.abs(mainActivity.lastImageUpdate - currentTime);
        if (message == mainActivity.lastMessage && difference < 1500) {
            Log.d(TAG, "Time between messages: " + difference + "ms. Duplicate message purged.");
            return true;
        }
        mainActivity.lastMessage = message;
        mainActivity.lastMessageUpdate = currentTime;
        return false;
    }

    private String formatMessage(String message) {
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

    private String capitalizeFirstLetter(String input) {
        if (input != null && !input.isEmpty()) {
            return input.substring(0, 1).toUpperCase() + input.substring(1);
        } else {
            return input;
        }
    }

    private void updateRecyclerView(int messageLayout, int image) {
        if (purgeDuplicateMessages(image)) {
            return;
        }
        switch (messageLayout) {
            case LayoutRobotImage:
                mainActivity.runOnUiThread(() -> {
                    mainActivity.messageAdapter.addItem(new MessageItem(messageLayout, R.drawable.ic_pepper_w, image));
                    recyclerView.scrollToPosition(mainActivity.messageItemList.size() - 1);
                    ImageManager.updateImage(image);
                    ImageManager.showImageForSeconds(4);
                });
                break;
            case LayoutUserImage:
                mainActivity.runOnUiThread(() -> {
                    mainActivity.messageAdapter.addItem(new MessageItem(messageLayout, R.drawable.ic_user, image));
                    recyclerView.scrollToPosition(mainActivity.messageItemList.size() - 1);
                    ImageManager.updateImage(image);
                    ImageManager.showImageForSeconds(4);
                });
                break;
        }
    }

    private void updateRecyclerView(int messageLayout, String message) {
        if (purgeDuplicateMessages(message)) {
            return;
        }
        String formattedMessage = formatMessage(message);
        switch (messageLayout) {
            case LayoutRobot:
                mainActivity.runOnUiThread(() -> {
                    mainActivity.messageAdapter.addItem(new MessageItem(messageLayout, R.drawable.ic_pepper_w, formattedMessage));
                    recyclerView.scrollToPosition(mainActivity.messageItemList.size() - 1);
                });
                break;
            case LayoutUser:
                mainActivity.runOnUiThread(() -> {
                    mainActivity.messageAdapter.addItem(new MessageItem(messageLayout, R.drawable.ic_user, formattedMessage));
                    recyclerView.scrollToPosition(mainActivity.messageItemList.size() - 1);
                });
                break;
        }
    }

    public void hideSoftKeyboard(View view) {
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public void hideTextInput() {
        constraintLayoutBottom.setMaxHeight(0);
    }

    public void showTextInput() {
        ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) constraintLayoutBottom.getLayoutParams();
        layoutParams.height = 150;
        constraintLayoutBottom.setLayoutParams(layoutParams);
        constraintLayoutBottom.setMaxHeight(150);
    }
}
