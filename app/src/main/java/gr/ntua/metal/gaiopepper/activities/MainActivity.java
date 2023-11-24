package gr.ntua.metal.gaiopepper.activities;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;

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
import com.aldebaran.qi.sdk.object.context.RobotContext;
import com.aldebaran.qi.sdk.object.conversation.AutonomousReaction;
import com.aldebaran.qi.sdk.object.conversation.Chat;
import com.aldebaran.qi.sdk.object.conversation.Chatbot;
import com.aldebaran.qi.sdk.object.conversation.ChatbotReaction;
import com.aldebaran.qi.sdk.object.conversation.ChatbotReactionHandlingStatus;
import com.aldebaran.qi.sdk.object.conversation.Conversation;
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
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import gr.ntua.metal.gaiopepper.R;
import gr.ntua.metal.gaiopepper.adapters.MessageAdapter;
import gr.ntua.metal.gaiopepper.models.MessageItem;

import static gr.ntua.metal.gaiopepper.models.MessageItem.LayoutRobotImage;
import static gr.ntua.metal.gaiopepper.models.MessageItem.LayoutUser;
import static gr.ntua.metal.gaiopepper.models.MessageItem.LayoutRobot;

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

    private QiChatbot chatbot;
    private Chat chat;
    private Future<Void> chatFuture;

    private List<MessageItem> messageItemList;
    private MessageAdapter messageAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate");
        super.onCreate(savedInstanceState);
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
        addListeners();

        makeSpeechEngine(qiContext);


        QiChatbot chatbot = buildQiChatbot();
        chat = buildChat(chatbot);
        runChat(chat);
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
                messageItemList.add(new MessageItem(LayoutRobot, R.drawable.ic_pepper_w, sayingText));
                runOnUiThread(() -> {
                    messageAdapter.notifyItemInserted(messageAdapter.getItemCount() - 1);
                    recyclerView.scrollToPosition(messageItemList.size() - 1);
                });

            }
        });
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
        // The robot focus is refused.
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
            String message = textInputEditText.getText().toString();
            if (!message.isEmpty()) {
                Phrase userPhrase = new Phrase(message);
                Log.d(TAG, "User message: " + userPhrase.getText());


                chatFuture.requestCancellation();
                Future<ReplyReaction> replyToFuture = chatbot.async().replyTo(userPhrase, localeEN);
                replyToFuture.thenConsume(replyReactionFuture -> {
                    if (replyReactionFuture.hasError()) {
                        Log.e(TAG, "Reply Reaction Future [ERROR]: " + replyReactionFuture.getErrorMessage());
                    } else {
                        ReplyReaction replyReaction = replyReactionFuture.get();
                        Future<ChatbotReaction> getChatbotReactionFuture = replyReaction.async().getChatbotReaction();
                        getChatbotReactionFuture.thenConsume(chatbotReactionFuture -> {
                            if (chatbotReactionFuture.hasError()){
                                Log.e(TAG, "Chatbot Reaction Future [ERROR]: " + chatbotReactionFuture.getErrorMessage());
                            } else {
                                ChatbotReaction chatbotReaction = chatbotReactionFuture.get();
                                Future<Void> runWithFuture = chatbotReaction.async().runWith(speechEngine);
                                runWithFuture.thenConsume(future -> {
                                    if (future.hasError()){
                                        Log.e(TAG, "runWith Future [ERROR]: " + future.getErrorMessage());
                                    } else {
                                        chat = buildChat(chatbot);
                                        runChat(chat);
                                    }
                                });
                            }
                        });
                    }
                });

                messageItemList.add(new MessageItem(LayoutUser, R.drawable.ic_user, message));
                messageAdapter.notifyItemInserted(messageAdapter.getItemCount() - 1);
                textInputEditText.getText().clear();
            }
        }
    }

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

    private QiChatbot buildQiChatbot() {

        Topic lexicon = TopicBuilder
                .with(qiContext)
                .withResource(R.raw.lexicon)
                .build();

        Topic introduction = TopicBuilder
                .with(qiContext)
                .withResource(R.raw.introduction)
                .build();

        Topic bauxite = TopicBuilder
                .with(qiContext)
                .withResource(R.raw.bauxite)
                .build();


        Topic test = TopicBuilder
                .with(qiContext)
                .withResource(R.raw.test_topic)
                .build();


        List<Topic> topicList = new LinkedList<Topic>();
        //topicList.add(lexicon);
        //topicList.add(introduction);
        //topicList.add(bauxite);
        topicList.add(test);


        chatbot = QiChatbotBuilder
                .with(qiContext)
                .withTopics(topicList)
                .withLocale(localeEN)
                .build();

        chatbot.addOnBookmarkReachedListener(bookmark -> {
            Log.i(TAG, "Bookmark " + bookmark.getName() + " reached.");
            boolean update = false;
            switch (bookmark.getName()) {
                case "BAUXITE":
                    messageItemList.add(new MessageItem(LayoutRobotImage, R.drawable.ic_pepper_w, R.drawable.red_bauxite_pissoliths));
                    update = true;
                    break;
                case "PISOLITH":
                    messageItemList.add(new MessageItem(LayoutRobotImage, R.drawable.ic_pepper_w, R.drawable.white_bauxite));
                    update = true;
                    break;
                case "ALUMINIUM":
                    messageItemList.add(new MessageItem(LayoutRobotImage, R.drawable.ic_pepper_w, R.drawable.aluminium));
                    update = true;
                    break;
                default:
                    break;
            }

            if (update) {
                runOnUiThread(() -> {
                    messageAdapter.notifyItemInserted(messageAdapter.getItemCount());
                    recyclerView.scrollToPosition(messageItemList.size() - 1);
                });

            }
        });

        chatbot.addOnEndedListener(endReason -> {
            Log.i(TAG, "Chatbot ended for reason: " + endReason);
            chatFuture.requestCancellation();
        });

        chatbot.addOnAutonomousReactionChangedListener(autonomousReaction -> {
            Log.d(TAG,"autonomousssss");
            autonomousReaction.getChatbotReaction().runWith(speechEngine);
        });

        return chatbot;
    }

    private Chat buildChat(QiChatbot chatbot) {
        Chat chat = ChatBuilder
                .with(qiContext)
                .withChatbot(chatbot)
                .withLocale(localeEN)
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
                messageItemList.add(new MessageItem(LayoutUser, R.drawable.ic_user, heardText));

            }
        });
        chat.addOnSayingChangedListener(sayingPhrase -> {
            String sayingText = sayingPhrase.getText();
            if (!sayingText.isEmpty()) {
                Log.i(TAG, "[CHAT] Pepper Reply: " + sayingText);
                messageItemList.add(new MessageItem(LayoutRobot, R.drawable.ic_pepper_w, sayingText));
                runOnUiThread(() -> {
                    messageAdapter.notifyItemInserted(messageAdapter.getItemCount() - 1);
                    recyclerView.scrollToPosition(messageItemList.size() - 1);
                });

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


    public void showSoftKeyboard(View view) {
        if (view.requestFocus()) {
            imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    public void hideSoftKeyboard(View view) {
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }


}