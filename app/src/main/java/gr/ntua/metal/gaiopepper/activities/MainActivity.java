package gr.ntua.metal.gaiopepper.activities;

import static gr.ntua.metal.gaiopepper.models.MessageItem.LayoutRobot;
import static gr.ntua.metal.gaiopepper.models.MessageItem.LayoutRobotImage;
import static gr.ntua.metal.gaiopepper.models.MessageItem.LayoutUser;
import static gr.ntua.metal.gaiopepper.models.MessageItem.LayoutUserImage;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aldebaran.qi.Function;
import com.aldebaran.qi.Future;
import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.QiSDK;
import com.aldebaran.qi.sdk.RobotLifecycleCallbacks;
import com.aldebaran.qi.sdk.builder.ChatBuilder;
import com.aldebaran.qi.sdk.builder.QiChatbotBuilder;
import com.aldebaran.qi.sdk.builder.TopicBuilder;
import com.aldebaran.qi.sdk.design.activity.RobotActivity;
import com.aldebaran.qi.sdk.design.activity.conversationstatus.SpeechBarDisplayPosition;
import com.aldebaran.qi.sdk.design.activity.conversationstatus.SpeechBarDisplayStrategy;
import com.aldebaran.qi.sdk.object.conversation.AutonomousReactionImportance;
import com.aldebaran.qi.sdk.object.conversation.AutonomousReactionValidity;
import com.aldebaran.qi.sdk.object.conversation.Bookmark;
import com.aldebaran.qi.sdk.object.conversation.Chat;
import com.aldebaran.qi.sdk.object.conversation.ChatbotReaction;
import com.aldebaran.qi.sdk.object.conversation.Phrase;
import com.aldebaran.qi.sdk.object.conversation.QiChatbot;
import com.aldebaran.qi.sdk.object.conversation.ReplyReaction;
import com.aldebaran.qi.sdk.object.conversation.SpeechEngine;
import com.aldebaran.qi.sdk.object.conversation.Topic;
import com.aldebaran.qi.sdk.object.locale.Language;
import com.aldebaran.qi.sdk.object.locale.Locale;
import com.aldebaran.qi.sdk.object.locale.Region;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import gr.ntua.metal.gaiopepper.AutonomousAbilitiesController;
import gr.ntua.metal.gaiopepper.ImageManager;
import gr.ntua.metal.gaiopepper.R;

import gr.ntua.metal.gaiopepper.adapters.MessageAdapter;
import gr.ntua.metal.gaiopepper.models.MessageItem;

public class MainActivity extends RobotActivity implements RobotLifecycleCallbacks, View.OnClickListener {
    private static final String TAG = "Main Activity";

    public static String sDefSystemLanguage;

    InputMethodManager inputMethodManager;
    AudioManager audioManager;


    private QiContext qiContext;
    private final Locale localeGR = new Locale(Language.GREEK, Region.GREECE);
    private final Locale localeEN = new Locale(Language.ENGLISH, Region.UNITED_STATES);

    private List<Topic> topicListGR;
    private List<Topic> topicListEN;

    private SpeechEngine speechEngine;

    private FloatingActionButton fabSettings;
    private FloatingActionButton fabEncyclopedia;
    private TextInputLayout textInputLayout;
    private TextInputEditText textInputEditText;
    private RecyclerView recyclerView;
    private ImageButton buttonSend;
    private ImageView expandedImageView;
    private ConstraintLayout constraintLayoutBottom;

    private QiChatbot chatbot;
    private Chat chat;
    private Future<Void> chatFuture;

    private List<MessageItem> messageItemList;
    private MessageAdapter messageAdapter;

    private Bookmark lastBookmark = null;
    private int lastImage = 0;
    private String lastMessage = "";
    private long lastImageUpdate = 100000;
    private long lastMessageUpdate = 100000;


    /**
     * _____________________________________________________________________________
     * <h1>Override Methods</h1>
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        PreferenceManager.setDefaultValues(this, R.xml.preferences_root, false);
        QiSDK.register(this, this);
        this.setContentView(R.layout.activity_main);

        inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);


        /// Set Default Variables
        setSpeechBarDisplayStrategy(SpeechBarDisplayStrategy.IMMERSIVE);
        setSpeechBarDisplayPosition(SpeechBarDisplayPosition.TOP);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);


        findViews();
        addListeners();

        ImageManager.setExpandedImageView(expandedImageView);


        messageItemList = new ArrayList<MessageItem>();
        messageAdapter = new MessageAdapter(getApplicationContext(), messageItemList);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(messageAdapter);



        sDefSystemLanguage = java.util.Locale.getDefault().getLanguage();
        System.out.println("HELLO " + sDefSystemLanguage);

       /* SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
        editor.putString(getString(R.string.CONVERSATION_LANGUAGE), "EN");
*/


    }

    @Override
    protected void onDestroy() {
        Log.i(TAG, "onDestroy");
        QiSDK.unregister(this, this);
        super.onDestroy();
    }

    @Override
    public void onRobotFocusGained(QiContext qiContext) {
        Log.i(TAG, "onRobotFocusGained");
        this.qiContext = qiContext;

        topicListGR = buildTopicList(new LinkedList<Integer>(
                Arrays.asList(
                        R.raw.lexicon_gr,
                        R.raw.introduction_gr,
                        R.raw.bauxite_gr
                )
        ));

        topicListEN = buildTopicList(new LinkedList<Integer>(
                Arrays.asList(
                        R.raw.lexicon_en,
                        R.raw.introduction_en,
                        R.raw.bauxite_en
                )
        ));

        makeSpeechEngine(qiContext);
        applyPreferences(qiContext);


    }

    @Override
    public void onRobotFocusLost() {
        Log.i(TAG, "onRobotFocusLost");
        chatbot = null;
        this.qiContext = null;
        removeListeners();
    }

    @Override
    public void onRobotFocusRefused(String reason) {
        Log.i(TAG, "onRobotFocusRefused: " + reason);
    }

    @Override
    public void onClick(View view) {
        int viewID = view.getId();
        if (viewID == R.id.fab_settings) {
            Intent settingsIntent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(settingsIntent);
        } else if (viewID == R.id.fab_encyclopedia) {
            Intent encyclopediaIntent = new Intent(MainActivity.this, EncyclopediaActivity.class);
            startActivity(encyclopediaIntent);
        } else if (viewID == R.id.editTextHumanInput) {
            textInputLayout.clearFocus();
            hideSoftKeyboard(textInputLayout);
        } else if (viewID == R.id.btn_send) {
            String message = Objects.requireNonNull(textInputEditText.getText()).toString();
            if (!message.isEmpty()) {
                Phrase userPhrase = new Phrase(message);
                Log.d(TAG, "User message: " + userPhrase.getText());
                updateRecyclerView(LayoutUser, message);
                replyTo(userPhrase, localeEN);
                /*messageItemList.add(new MessageItem(LayoutUser, R.drawable.ic_user, message));
                messageAdapter.notifyItemInserted(messageAdapter.getItemCount() - 1);*/
                textInputEditText.getText().clear();
            }
        }
    }


    /**
     * _____________________________________________________________________________
     *
     * <h1>Base Methods</h1>
     */

    private void applyPreferences(QiContext qiContext) {
        boolean autonomousBlinking = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(getString(R.string.AUTONOMOUS_BLINKING), true);
        boolean backgroundMovement = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(getString(R.string.BACKGROUND_MOVEMENT), true);
        boolean basicAwareness = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(getString(R.string.BASIC_AWARENESS), true);
        String conversationMode = PreferenceManager.getDefaultSharedPreferences(this).getString(getString(R.string.CONVERSATION_MODE), "NONE");
        String conversationLanguage = PreferenceManager.getDefaultSharedPreferences(this).getString(getString(R.string.CONVERSATION_LANGUAGE), "EN");
        Log.d(TAG, "[applyPreferences] conversationLanguage: " + conversationLanguage);
        AutonomousAbilitiesController.buildHolders(qiContext);

        if (autonomousBlinking) {
            AutonomousAbilitiesController.startAutonomousBlinking();
        } else {
            AutonomousAbilitiesController.stopAutonomousBlinking(qiContext);
        }
        if (backgroundMovement) {
            AutonomousAbilitiesController.startBackgroundMovement();
        } else {
            AutonomousAbilitiesController.stopBackgroundMovement(qiContext);
        }
        if (basicAwareness) {
            AutonomousAbilitiesController.startBasicAwareness();
        } else {
            AutonomousAbilitiesController.stopBasicAwareness(qiContext);
        }

        if (Objects.equals(conversationLanguage, getString(R.string.GREEK))) {
            try {
                chatbot = buildQiChatbot(topicListGR, localeGR);
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        } else if (Objects.equals(conversationLanguage, getString(R.string.ENGLISH))) {
            try {
                chatbot = buildQiChatbot(topicListEN, localeEN);
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }

        if (Objects.equals(conversationMode, getString(R.string.NONE))) {
            if (chatFuture != null) {
                if (!chatFuture.isSuccess() || !chatFuture.isCancelled() || !chatFuture.isDone()) {
                    chatFuture.requestCancellation();
                }
            }
        } else if (Objects.equals(conversationMode, getString(R.string.ORAL_CONVERSATION))) {
            hideTextInput();
            if (Objects.equals(conversationLanguage, getString(R.string.GREEK))) {
                chat = buildChat(chatbot, localeGR);
                runChat(chat);
            } else if (Objects.equals(conversationLanguage, getString(R.string.ENGLISH))) {
                chat = buildChat(chatbot, localeEN);
                runChat(chat);
            }
        } else if (Objects.equals(conversationMode, getString(R.string.WRITTEN_CONVERSATION))) {
            if (chatFuture != null) {
                if (!chatFuture.isSuccess() || !chatFuture.isCancelled() || !chatFuture.isDone()) {
                    chatFuture.requestCancellation();
                }
            }
            showTextInput();
        }

    }


    /**
     * _____________________________________________________________________________
     * <h1>Conversation Methods</h1>
     */

    public List<Topic> buildTopicList(List<Integer> topics) {
        List<Topic> topicList;
        topicList = new LinkedList<Topic>();
        for (int topicName : topics) {
            Topic topic = TopicBuilder
                    .with(qiContext)
                    .withResource(topicName)
                    .build();
            topicList.add(topic);
        }
        return topicList;
    }

    /**
     * Returns a <b>QiChatbot</b> instance built with the specified parameters.
     * The topicList attribute must be generated by <b>buildTopicList</b> method.
     * The locale is already created as a global variable and can be found on top.
     *
     * @param topicList a topicList created by buildTopicList method
     * @param locale    Locale from package com.aldebaran.qi.sdk.object.locale
     * @see com.aldebaran.qi.sdk.object.conversation.QiChatbot QiChatbot
     * @see com.aldebaran.qi.sdk.object.locale.Locale Locale
     */
    private QiChatbot buildQiChatbot(List<Topic> topicList, Locale locale) throws ExecutionException {

        QiChatbot newChatbot;/* = QiChatbotBuilder
                .with(qiContext)
                .withTopics(topicList)
                .withLocale(locale)
                .build();*/

        Future<QiChatbot> qiChatbotBuilderFuture = QiChatbotBuilder
                .with(qiContext)
                .withTopics(topicList)
                .withLocale(locale)
                .buildAsync();

        newChatbot = qiChatbotBuilderFuture.thenCompose((Function<Future<QiChatbot>, Future<QiChatbot>>) value -> {
            if (value.hasError()) {
                Log.e(TAG, "[qiChatbotBuilder][ERROR]: " + value.getErrorMessage());
                Toast.makeText(getApplicationContext(), value.getErrorMessage(), Toast.LENGTH_LONG).show();
                return null;
            } else {
                return value;
            }
        }).get();

        if (newChatbot != null) {
            newChatbot.addOnBookmarkReachedListener(bookmark -> {
                Log.i(TAG, "[CHATBOT] Bookmark " + bookmark.getName() + " reached.");
                switch (bookmark.getName()) {
                    case "BAUXITE":
                        updateRecyclerView(LayoutRobotImage, R.drawable.red_bauxite_pissoliths);
                        break;
                    case "PISOLITH":
                        updateRecyclerView(LayoutRobotImage, R.drawable.white_bauxite);
                        break;
                    case "ALUMINIUM":
                        updateRecyclerView(LayoutRobotImage, R.drawable.aluminium);
                        break;
                    default:
                        break;
                }
                lastBookmark = bookmark;
            });
            newChatbot.addOnEndedListener(endReason -> {
                Log.i(TAG, "Chatbot ended for reason: " + endReason);
                chatFuture.requestCancellation();
            });
            newChatbot.addOnAutonomousReactionChangedListener(autonomousReaction -> {
                ChatbotReaction chatbotReaction = autonomousReaction.getChatbotReaction();
                Log.d(TAG, "[autonomousReaction]: " + chatbotReaction.toString());
                chatbotReaction.runWith(speechEngine);
            });
            if (lastBookmark != null) {
                newChatbot.goToBookmark(lastBookmark, AutonomousReactionImportance.HIGH, AutonomousReactionValidity.IMMEDIATE);
            }
            return newChatbot;
        } else {
            return null;
        }
    }

    private Chat buildChat(QiChatbot chatbot, Locale locale) {
        if (chatbot == null) {
            Log.e(TAG, "[buildChat]: chatbot is null.");
            return null;
        }
        Chat chat = ChatBuilder
                .with(qiContext)
                .withChatbot(chatbot)
                .withLocale(locale)
                .build();
        chat.addOnStartedListener(() -> {
            Log.i(TAG, "[CHAT] Chat started.");

        });
        chat.addOnListeningChangedListener(listening -> {
            if (listening) {
                Log.i(TAG, "[CHAT] Listening START.");
            } else {
                Log.i(TAG, "[CHAT] Listening END.");
            }

        });
        chat.addOnHeardListener(heardPhrase -> {
            String heardText = heardPhrase.getText();
            if (!heardText.isEmpty()) {
                Log.i(TAG, "[CHAT] Heard phrase: " + heardPhrase.getText());
                updateRecyclerView(LayoutUser, heardText);
            }
        });
        chat.addOnSayingChangedListener(sayingPhrase -> {
            String sayingText = sayingPhrase.getText();
            if (!sayingText.isEmpty()) {
                Log.i(TAG, "[CHAT] Pepper Reply: " + sayingText);
                updateRecyclerView(LayoutRobot, sayingText);
            }
        });
        chat.addOnNormalReplyFoundForListener(input -> {
            Log.i(TAG, "[CHAT] User message: " + input.getText());
        });
        chat.addOnNoPhraseRecognizedListener(() -> {
            Log.i(TAG, "[CHAT] No phrase recognized.");
        });

        chat.addOnFallbackReplyFoundForListener(input -> {
            Log.i(TAG, "[CHAT] Fallback Reply found for user message: " + input.getText());
        });
        chat.addOnNoReplyFoundForListener(input -> {
            Log.i(TAG, "[CHAT] NO Reply found for user message: " + input.getText());
        });
        return chat;
    }

    private void runChat(Chat chat) {
        if (chat == null) {
            Log.e(TAG, "[runChat]: chat is null.");
            return;
        }
        chatFuture = chat.async().run();
        chatFuture.thenConsume(future -> {
            if (future.hasError()) {
                Log.e(TAG, "Chat finished with error: " + future.getErrorMessage());
            } else {
                Log.e(TAG, "Chat finished: " + future.get().toString());
            }
        });
    }

    private void makeSpeechEngine(QiContext qiContext) {
        speechEngine = qiContext.getConversation().makeSpeechEngine(qiContext.getRobotContext());
        speechEngine.addOnSayingChangedListener(phrase -> {
            String sayingText = phrase.getText();
            if (!sayingText.isEmpty()) {
                Log.i(TAG, "[SPEECH ENGINE] Pepper Reply: " + sayingText);
                updateRecyclerView(LayoutRobot, sayingText);
            }
        });
    }

    private void replyTo(Phrase userPhrase, Locale locale) {
        Future<ReplyReaction> replyToFuture = chatbot.async().replyTo(userPhrase, locale);
        replyToFuture.thenConsume(replyReactionFuture -> {
            if (replyReactionFuture.hasError()) {
                Log.e(TAG, "Reply Reaction Future [ERROR]: " + replyReactionFuture.getErrorMessage());
            } else {
                ReplyReaction replyReaction = replyReactionFuture.get();
                Future<ChatbotReaction> getChatbotReactionFuture = replyReaction.async().getChatbotReaction();
                getChatbotReactionFuture.thenConsume(chatbotReactionFuture -> {
                    if (chatbotReactionFuture.hasError()) {
                        Log.e(TAG, "Chatbot Reaction Future [ERROR]: " + chatbotReactionFuture.getErrorMessage());
                    } else {
                        ChatbotReaction chatbotReaction = chatbotReactionFuture.get();
                        Future<Void> runWithFuture = chatbotReaction.async().runWith(speechEngine);
                        runWithFuture.thenConsume(future -> {
                            if (future.hasError()) {
                                Log.e(TAG, "runWith Future [ERROR]: " + future.getErrorMessage());
                            } else {

                            }
                        });
                    }
                });
            }
        });
    }


    /**
     * _____________________________________________________________________________
     * <h1>UI Methods</h1>
     */

    private void findViews() {
        try {
            fabSettings = findViewById(R.id.fab_settings);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            fabEncyclopedia = findViewById(R.id.fab_encyclopedia);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            textInputLayout = findViewById(R.id.editTextLayout);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            textInputEditText = (TextInputEditText) textInputLayout.getEditText();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            recyclerView = findViewById(R.id.recycler_view);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            buttonSend = findViewById(R.id.btn_send);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            constraintLayoutBottom = findViewById(R.id.constraint_bottom);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            expandedImageView = findViewById(R.id.expanded_image);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addListeners() {
        fabSettings.setOnClickListener(this);
        fabEncyclopedia.setOnClickListener(this);
        textInputEditText.setOnClickListener(this);
        buttonSend.setOnClickListener(this);
    }

    private void removeListeners() {
        chat.removeAllOnHeardListeners();
        chat.removeAllOnFallbackReplyFoundForListeners();
        chat.removeAllOnHearingChangedListeners();
        chat.removeAllOnListeningChangedListeners();
        chat.removeAllOnNoReplyFoundForListeners();
        chat.removeAllOnNoPhraseRecognizedListeners();
        chat.removeAllOnNormalReplyFoundForListeners();
        chat.removeAllOnSayingChangedListeners();
        chat.removeAllOnStartedListeners();
    }

    private void updateRecyclerView(int messageLayout, int image) {
        if (purgeDuplicateMessages(image)) {
            return;
        }
        switch (messageLayout) {
            case LayoutRobotImage:
                messageItemList.add(new MessageItem(messageLayout, R.drawable.ic_pepper_w, image));
                break;
            case LayoutUserImage:
                messageItemList.add(new MessageItem(messageLayout, R.drawable.ic_user, image));
                break;
        }
        runOnUiThread(() -> {
            messageAdapter.notifyItemInserted(messageAdapter.getItemCount());
            recyclerView.scrollToPosition(messageItemList.size() - 1);
        });
    }

    private void updateRecyclerView(int messageLayout, String message) {
        if (purgeDuplicateMessages(message)) {
            return;
        }
        switch (messageLayout) {
            case LayoutRobot:
                messageItemList.add(new MessageItem(messageLayout, R.drawable.ic_pepper_w, message));
                break;
            case LayoutUser:
                messageItemList.add(new MessageItem(messageLayout, R.drawable.ic_user, message));
                break;
        }
        runOnUiThread(() -> {
            messageAdapter.notifyItemInserted(messageAdapter.getItemCount());
            recyclerView.scrollToPosition(messageItemList.size() - 1);
        });
    }

    private boolean purgeDuplicateMessages(int image) {
        if (image == lastImage && Math.abs(lastImageUpdate - System.currentTimeMillis()) < 1500) {
            return true;
        }
        lastImage = image;
        lastImageUpdate = System.currentTimeMillis();
        return false;
    }

    private boolean purgeDuplicateMessages(String message) {
        if (message == lastMessage && Math.abs(lastMessageUpdate - System.currentTimeMillis()) < 1500) {
            return true;
        }
        lastMessage = message;
        lastMessageUpdate = System.currentTimeMillis();
        return false;
    }

    public void showSoftKeyboard(View view) {
        if (view.requestFocus()) {
            inputMethodManager.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    public void hideSoftKeyboard(View view) {
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private void hideTextInput() {
        runOnUiThread(() -> {
            constraintLayoutBottom.setMaxHeight(0);
        });
    }

    private void showTextInput() {
        runOnUiThread(() -> {
            ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) constraintLayoutBottom.getLayoutParams();
            layoutParams.height = 150;
            constraintLayoutBottom.setLayoutParams(layoutParams);
            constraintLayoutBottom.setMaxHeight(150);
        });
    }


}