package gr.ntua.metal.gaiopepper.activities;

import static gr.ntua.metal.gaiopepper.models.MessageItem.LayoutRobot;
import static gr.ntua.metal.gaiopepper.models.MessageItem.LayoutRobotImage;
import static gr.ntua.metal.gaiopepper.models.MessageItem.LayoutUser;
import static gr.ntua.metal.gaiopepper.models.MessageItem.LayoutUserImage;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

import gr.ntua.metal.gaiopepper.AutonomousAbilitiesController;
import gr.ntua.metal.gaiopepper.R;

import gr.ntua.metal.gaiopepper.adapters.MessageAdapter;
import gr.ntua.metal.gaiopepper.models.MessageItem;

public class MainActivity extends RobotActivity implements RobotLifecycleCallbacks, View.OnClickListener {
    private static final String TAG = "Main Activity";

    InputMethodManager imm;


    private QiContext qiContext;
    private final Locale localeGR = new Locale(Language.GREEK, Region.GREECE);
    private final Locale localeEN = new Locale(Language.ENGLISH, Region.UNITED_STATES);

    private SpeechEngine speechEngine;

    private FloatingActionButton fabSettings;
    private FloatingActionButton fabEncyclopedia;
    private TextInputLayout textInputLayout;
    private TextInputEditText textInputEditText;
    private RecyclerView recyclerView;
    private ImageButton buttonSend;
    private ConstraintLayout constraintLayoutBottom;

    private QiChatbot chatbot;
    private Chat chat;
    private Future<Void> chatFuture;

    private List<MessageItem> messageItemList;
    private MessageAdapter messageAdapter;


    /**
     * Override Methods
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        PreferenceManager.setDefaultValues(this, R.xml.preferences_root, false);
        QiSDK.register(this, this);
        this.setContentView(R.layout.activity_main);

        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);


        /// Set Default Variables
        setSpeechBarDisplayStrategy(SpeechBarDisplayStrategy.IMMERSIVE);
        setSpeechBarDisplayPosition(SpeechBarDisplayPosition.TOP);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        findViews();

        messageItemList = new ArrayList<MessageItem>();
        messageAdapter = new MessageAdapter(getApplicationContext(), messageItemList);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(messageAdapter);


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

        makeSpeechEngine(qiContext);

        /*List<Topic> topicList =new LinkedList<Topic>();
        Future<Topic> futureTopicLex = TopicBuilder
                .with(qiContext)
                .withResource(R.raw.lexicon)
                //.withAsset("app/src/main/res/raw-en-rUS/lexicon.top")
                .buildAsync();
        futureTopicLex.thenConsume(future -> {
            if (future.hasError()) {
                Log.e(TAG,"[TopicBuilder] Error: " + future.getErrorMessage());
            } else if (future.isCancelled()) {
                Log.e(TAG,"[TopicBuilder] Cancelled: ");
            } else if (future.isDone()) {
                Log.i(TAG,"[TopicBuilder] Done: ");
            } else if (future.isSuccess()) {
                Log.i(TAG,"[TopicBuilder] Success: ");
                topicList.add(future.get());
            }
        });*/


        chat = buildChat(
                buildQiChatbot(
                        /*topicList*/
                        buildTopicList(
                                new LinkedList<Integer>(
                                        Arrays.asList(
                                            R.raw.lexicon,
                                            R.raw.introduction,
                                            R.raw.bauxite
                                        )
                                )
                        ),
                        localeEN
                ),
                localeEN
        );

        applyPreferences(qiContext);

        addListeners();


    }

    @Override
    public void onRobotFocusLost() {
        Log.i(TAG, "onRobotFocusLost");
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
     * Base Methods
     */

    private void applyPreferences(QiContext qiContext) {
        boolean autonomousBlinking = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(getString(R.string.AUTONOMOUS_BLINKING), true);
        boolean backgroundMovement = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(getString(R.string.BACKGROUND_MOVEMENT), true);
        boolean basicAwareness = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(getString(R.string.BASIC_AWARENESS), true);
        String conversationMode = PreferenceManager.getDefaultSharedPreferences(this).getString(getString(R.string.CONVERSATION_MODE), "NONE");

        AutonomousAbilitiesController.buildHolders(qiContext);

        Log.d(TAG, "[applyPreferences] autonomousBlinking: " + autonomousBlinking);

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

        Log.d(TAG, "[applyPreferences] conversationMode: " + conversationMode);
        if (Objects.equals(conversationMode, getString(R.string.NONE))) {
            if (chatFuture != null) {
                if (!chatFuture.isSuccess() || !chatFuture.isCancelled() || !chatFuture.isDone()) {
                    chatFuture.requestCancellation();
                }
            }
        } else if (Objects.equals(conversationMode, getString(R.string.ORAL_CONVERSATION))) {
            hideTextInput();
            runChat(chat);
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
     * Conversation Methods
     */

    private List<Topic> buildTopicList(List<Integer> topics) {
        List<Topic> topicList;
        topicList = new LinkedList<Topic>();
        for (int topicName:topics) {
            Topic topic = TopicBuilder
                    .with(qiContext)
                    .withResource(topicName)
                    .build();
            topicList.add(topic);
        }
        return topicList;
    }

    /**
     * Returns a @see(QiChatbot) instance built with the specified parameters.
     * The topicList attribute must be generated by @see buildTopicList method.
     * The locale is already created as a global variable and can be found on top.
     *
     * @param topicList   a topicList created by buildTopicList method
     * @param locale    Locale from package com.aldebaran.qi.sdk.object.locale
     * @return     the newly built chatbot which runs the provided topics.
     */
    private QiChatbot buildQiChatbot(List<Topic> topicList, Locale locale) {

        chatbot = QiChatbotBuilder
                .with(qiContext)
                .withTopics(topicList)
                .withLocale(locale)
                .build();
        chatbot.addOnBookmarkReachedListener(bookmark -> {
            Log.i(TAG, "Bookmark " + bookmark.getName() + " reached.");
            boolean update = false;
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
        });
        chatbot.addOnEndedListener(endReason -> {
            Log.i(TAG, "Chatbot ended for reason: " + endReason);
            chatFuture.requestCancellation();
        });
        chatbot.addOnAutonomousReactionChangedListener(autonomousReaction -> {
            Log.d(TAG, "autonomousssss");
            autonomousReaction.getChatbotReaction().runWith(speechEngine);
        });
        return chatbot;
    }

    private Chat buildChat(QiChatbot chatbot, Locale locale) {
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
            Log.i(TAG, "[CHAT] Reply found for user message: " + input.getText());
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
     * UI Methods
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
        Log.d(TAG, "msgLayout: " + messageLayout + " message: " + message);
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

    public void showSoftKeyboard(View view) {
        if (view.requestFocus()) {
            imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    public void hideSoftKeyboard(View view) {
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
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