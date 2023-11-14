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

import com.aldebaran.qi.Consumer;
import com.aldebaran.qi.Function;
import com.aldebaran.qi.Future;
import com.aldebaran.qi.Promise;
import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.QiSDK;
import com.aldebaran.qi.sdk.RobotLifecycleCallbacks;
import com.aldebaran.qi.sdk.builder.ChatBuilder;
import com.aldebaran.qi.sdk.builder.QiChatbotBuilder;
import com.aldebaran.qi.sdk.builder.TopicBuilder;
import com.aldebaran.qi.sdk.design.activity.RobotActivity;
import com.aldebaran.qi.sdk.design.activity.conversationstatus.SpeechBarDisplayPosition;
import com.aldebaran.qi.sdk.design.activity.conversationstatus.SpeechBarDisplayStrategy;
import com.aldebaran.qi.sdk.object.conversation.BaseChatbotReaction;
import com.aldebaran.qi.sdk.object.conversation.BaseQiChatExecutor;
import com.aldebaran.qi.sdk.object.conversation.Chat;
import com.aldebaran.qi.sdk.object.conversation.Chatbot;
import com.aldebaran.qi.sdk.object.conversation.ChatbotReactionHandlingStatus;
import com.aldebaran.qi.sdk.object.conversation.Phrase;
import com.aldebaran.qi.sdk.object.conversation.QiChatbot;
import com.aldebaran.qi.sdk.object.conversation.ReplyPriority;
import com.aldebaran.qi.sdk.object.conversation.ReplyReaction;
import com.aldebaran.qi.sdk.object.conversation.RunnableReplyReaction;
import com.aldebaran.qi.sdk.object.conversation.SpeechEngine;
import com.aldebaran.qi.sdk.object.conversation.StandardReplyReaction;
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

import gr.ntua.metal.gaiopepper.R;
import gr.ntua.metal.gaiopepper.adapters.MessageAdapter;
import gr.ntua.metal.gaiopepper.models.MessageItem;

import static gr.ntua.metal.gaiopepper.models.MessageItem.LayoutUser;
import static gr.ntua.metal.gaiopepper.models.MessageItem.LayoutRobot;

public class MainActivity extends RobotActivity implements RobotLifecycleCallbacks, View.OnClickListener {
    private static final String TAG = "Main Activity";

    InputMethodManager imm;


    private QiContext qiContext;
    private final Locale localeGR = new Locale(Language.GREEK, Region.GREECE);
    private final Locale localeEN = new Locale(Language.ENGLISH, Region.UNITED_STATES);

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

    //private final Locale locale_greek = new Locale(Language.GREEK, Region.GREECE);


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

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
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

        QiChatbot chatbot = buildQiChatbot();
        Chat chat = buildChat(chatbot);

        chatFuture = chat.async().run();
        chatFuture.thenConsume(future -> {
            if (future.hasError()) {
                Log.e(TAG, "Chat finished with error: " + future.getErrorMessage());
            } else {
                Log.e(TAG, "Chat finished: " + future.get().toString());
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


                Future<ReplyReaction> replyToFuture = chatbot.async().replyTo(userPhrase, localeEN);
                /*replyToFuture.thenApply(value -> {
                    if (value.hasError()) {
                        Log.e(TAG, "ReplyReaction: ERROR: " + value.getErrorMessage());
                    } else {
                        Log.d(TAG, "ReplyReaction: " + value.get().getChatbotReaction());
                    }
                    return null;
                });*/

                replyToFuture.thenCompose(value -> {

                    if (value.hasError()) {
                        Log.d(TAG, "ERROR: " + value.getErrorMessage());

                    } else {
                        Log.d(TAG, "PRIOR: " + value.get().getReplyPriority());
                        Log.d(TAG, "SAYING: " + value.get().getChatbotReaction().getChatbotReactionHandlingStatus());
                        Log.d(TAG, "QIVALUE: " + value.get().getReplyPriority().getQiValue());

                        value.get().getChatbotReaction().addOnChatbotReactionHandlingStatusChangedListener(new RunnableReplyReaction(qiContext, ReplyPriority.NORMAL) {
                            @Override
                            public void runWith(SpeechEngine speechEngine) {
                                Log.d(TAG, "runwith: " + speechEngine.getSaying());

                            }

                            @Override
                            public void stop() {
                                Log.d(TAG, "stop: ");
                            }
                        });

                    }


                    return null;
                });





               /* Future<Void> acknowledgeHeardFuture = chatbot.async().acknowledgeHeard(userPhrase,localeEN);

                acknowledgeHeardFuture.thenCompose((Function<Future<Void>, Future<Phrase>>) value -> {
                    Log.i(TAG, "acknowledgeHeardFuture " + value.getValue());



                    return null;

                });*/


                messageItemList.add(new MessageItem(LayoutUser, R.drawable.icons8_user_100, message));
                messageAdapter.notifyItemInserted(messageItemList.size());

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

    }

    private QiChatbot buildQiChatbot() {
        Topic lexicon = TopicBuilder
                .with(qiContext)
                .withResource(R.raw.lexicon)
                .build();
        Topic topic_minerals = TopicBuilder
                .with(qiContext)
                .withResource(R.raw.topic_minerals)
                .build();
        Topic test = TopicBuilder
                .with(qiContext)
                .withResource(R.raw.test_topic)
                .build();


        List<Topic> topicList = new LinkedList<Topic>();
        topicList.add(test);
        //topicList.add(lexicon);
        //topicList.add(topic_minerals);

        chatbot = QiChatbotBuilder
                .with(qiContext)
                .withTopics(topicList)
                .withLocale(localeEN)
                .build();

        chatbot.addOnBookmarkReachedListener(bookmark -> {
            Log.i(TAG, "Bookmark " + bookmark.getName() + " reached.");

        });

        chatbot.addOnEndedListener(endReason -> {
            Log.i(TAG, "Chatbot ended for reason: " + endReason);
            chatFuture.requestCancellation();
        });

        return chatbot;
    }

    private Chat buildChat(QiChatbot chatbot) {
        chat = ChatBuilder
                .with(qiContext)
                //.withLocale(locale_greek)
                .withChatbot(chatbot)
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
            Log.i(TAG, "[CHAT] Heard phrase: " + heardPhrase.getText());

        });
        chat.addOnNormalReplyFoundForListener(input -> {
            Log.i(TAG, "[CHAT] Reply found for user message: " + input.getText());
        });
        chat.addOnNoPhraseRecognizedListener(() -> {
            Log.i(TAG, "[CHAT] No phrase recognized.");
        });
        chat.addOnSayingChangedListener(sayingPhrase -> {
            if (!sayingPhrase.getText().isEmpty()) {
                Log.i(TAG, "[CHAT] Pepper Reply: " + sayingPhrase.getText());
                messageItemList.add(new MessageItem(LayoutRobot, R.drawable.icons8_user_100, sayingPhrase.getText()));
                messageAdapter.notifyItemInserted(messageItemList.size());
            }
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