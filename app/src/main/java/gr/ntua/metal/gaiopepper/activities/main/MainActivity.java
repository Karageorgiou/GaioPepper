package gr.ntua.metal.gaiopepper.activities.main;

import static gr.ntua.metal.gaiopepper.models.MessageItem.LayoutRobot;
import static gr.ntua.metal.gaiopepper.models.MessageItem.LayoutRobotImage;
import static gr.ntua.metal.gaiopepper.models.MessageItem.LayoutUser;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;

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
import com.aldebaran.qi.sdk.object.conversation.AutonomousReaction;
import com.aldebaran.qi.sdk.object.conversation.AutonomousReactionImportance;
import com.aldebaran.qi.sdk.object.conversation.AutonomousReactionValidity;
import com.aldebaran.qi.sdk.object.conversation.Bookmark;
import com.aldebaran.qi.sdk.object.conversation.Chat;
import com.aldebaran.qi.sdk.object.conversation.Chatbot;
import com.aldebaran.qi.sdk.object.conversation.ChatbotReaction;
import com.aldebaran.qi.sdk.object.conversation.ChatbotReactionHandlingStatus;
import com.aldebaran.qi.sdk.object.conversation.Phrase;
import com.aldebaran.qi.sdk.object.conversation.QiChatbot;
import com.aldebaran.qi.sdk.object.conversation.ReplyReaction;
import com.aldebaran.qi.sdk.object.conversation.SpeechEngine;
import com.aldebaran.qi.sdk.object.conversation.Topic;
import com.aldebaran.qi.sdk.object.locale.Language;
import com.aldebaran.qi.sdk.object.locale.Locale;
import com.aldebaran.qi.sdk.object.locale.Region;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import gr.ntua.metal.gaiopepper.R;
import gr.ntua.metal.gaiopepper.activities.encyclopedia.EncyclopediaActivity;
import gr.ntua.metal.gaiopepper.activities.settings.SettingsActivity;
import gr.ntua.metal.gaiopepper.models.MessageItem;
import gr.ntua.metal.gaiopepper.util.AutonomousAbilitiesManager;
import gr.ntua.metal.gaiopepper.util.StringUtility;

public class MainActivity extends RobotActivity
        implements
        RobotLifecycleCallbacks,
        View.OnClickListener,
        QiChatbot.OnBookmarkReachedListener,
        SpeechEngine.OnSayingChangedListener,
        Chat.OnHeardListener,
        Chat.OnSayingChangedListener,
        Chatbot.OnAutonomousReactionChangedListener {
    private static final String TAG = "Main Activity";

    private AudioManager audioManager;

    public Intent settingsIntent = null;

    public ChatFragment chatFragment;
    public QuizFragment quizFragment;

    private FloatingActionButton fabSettings;
    private FloatingActionButton fabEncyclopedia;
    private FloatingActionButton fabQuiz;

    private QiContext qiContext;

    public final Locale localeGR = new Locale(Language.GREEK, Region.GREECE);
    public final Locale localeEN = new Locale(Language.ENGLISH, Region.UNITED_STATES);

    protected List<Topic> topicListGR;
    protected List<Topic> topicListEN;

    public Map<String, Bookmark> bookmarksGR;
    public Map<String, Bookmark> bookmarksEN;
    public Map<String, Bookmark> questionsEN;
    public Map<String, Bookmark> questionsGR;
    public Map<String, Bookmark> answersEN;
    public Map<String, Bookmark> answersGR;
    private List<Map<String, Bookmark>> bookmarksLibrary;

    protected SpeechEngine speechEngine;

    protected QiChatbot chatbot;
    protected Chat chat;
    protected Future<Void> chatFuture;

    protected List<MessageItem> messageItemList;
    protected MessageAdapter messageAdapter;
    protected LinearLayoutManager linearLayoutManager;

    protected Bookmark lastBookmark = null;
    protected AutonomousReaction lastAutonomousReaction = null;
    protected int lastImage = 0;
    protected String lastMessage = "";
    protected long lastImageUpdate = 100000;
    protected long lastMessageUpdate = 100000;
    protected long lastAutonomousReactionUpdate = 100000;

    private boolean firstRun = true;


    /**
     * _____________________________________________________________________________
     * <h1>Override Methods</h1>
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        //PreferenceManager.setDefaultValues(this, R.xml.preferences_root, true);

        PreferenceManager.getDefaultSharedPreferences(this).edit().clear().commit();
        PreferenceManager.setDefaultValues(this, R.xml.preferences_root, true);

        chatFragment = new ChatFragment();
        quizFragment = new QuizFragment();

        bookmarksLibrary = new ArrayList<>();
        messageItemList = new ArrayList<>();
        messageAdapter = new MessageAdapter(this, messageItemList);

        questionsEN = new HashMap<>();
        questionsGR = new HashMap<>();
        answersEN = new HashMap<>();
        answersGR = new HashMap<>();

        QiSDK.register(this, this);
        this.setContentView(R.layout.activity_main);

        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        /// Set Default Variables
        setSpeechBarDisplayStrategy(SpeechBarDisplayStrategy.IMMERSIVE);
        setSpeechBarDisplayPosition(SpeechBarDisplayPosition.TOP);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        findViews();
        addListeners();
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

        buildTopics();

        changeFragment(chatFragment);


        makeSpeechEngine(qiContext);
        try {
            applyPreferences(qiContext);
        } catch (ExecutionException e) {
            e.printStackTrace();
        }


        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("Chat UI");
        }
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
            settingsIntent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(settingsIntent);
        } else if (viewID == R.id.fab_encyclopedia) {
            Intent encyclopediaIntent = new Intent(MainActivity.this, EncyclopediaActivity.class);
            startActivity(encyclopediaIntent);
        } else if (viewID == R.id.fab_quiz) {
            if (chatFragment != null && chatFragment.isVisible()) {
                changeFragment(quizFragment);
            } else if (quizFragment != null && quizFragment.isVisible()) {
                changeFragment(chatFragment);
            }
        }
    }


    /**
     * _____________________________________________________________________________
     *
     * <h1>Base Methods</h1>
     */
    private void applyPreferences(QiContext qiContext) throws ExecutionException {
        boolean autonomousBlinking = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(getString(R.string.AUTONOMOUS_BLINKING_KEY), true);
        boolean backgroundMovement = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(getString(R.string.BACKGROUND_MOVEMENT_KEY), true);
        boolean basicAwareness = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(getString(R.string.BASIC_AWARENESS_KEY), true);
        String conversationMode = PreferenceManager.getDefaultSharedPreferences(this).getString(getString(R.string.CONVERSATION_MODE_KEY), "NONE_VALUE");
        String conversationLanguage = PreferenceManager.getDefaultSharedPreferences(this).getString(getString(R.string.CONVERSATION_LANGUAGE_KEY), "EN");
        boolean resetChatState = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(getString(R.string.RESET_CHAT_STATE_KEY), false);
        boolean resetChatLayout = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(getString(R.string.RESET_CHAT_LAYOUT_KEY), false);

        AutonomousAbilitiesManager.buildHolders(qiContext);
        if (autonomousBlinking) {
            AutonomousAbilitiesManager.startAutonomousBlinking();
        } else {
            AutonomousAbilitiesManager.stopAutonomousBlinking(qiContext);
        }
        if (backgroundMovement) {
            AutonomousAbilitiesManager.startBackgroundMovement();
        } else {
            AutonomousAbilitiesManager.stopBackgroundMovement(qiContext);
        }
        if (basicAwareness) {
            AutonomousAbilitiesManager.startBasicAwareness();
        } else {
            AutonomousAbilitiesManager.stopBasicAwareness(qiContext);
        }

        if (resetChatState) {
            lastBookmark = null;
        }
        if (resetChatLayout) {
            int messageItemListSize = messageItemList.size();
            messageItemList.clear();
            runOnUiThread(() -> {
                messageAdapter.notifyItemRangeRemoved(0, messageItemListSize);
            });

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
        if (Objects.equals(conversationMode, getString(R.string.NONE_VALUE))) {
            if (chatFuture != null) {
                if (!chatFuture.isSuccess() || !chatFuture.isCancelled() || !chatFuture.isDone()) {
                    chatFuture.requestCancellation();
                }
            }
        } else if (Objects.equals(conversationMode, getString(R.string.ORAL_CONVERSATION_VALUE))) {
            runOnUiThread(() -> {
                chatFragment.hideTextInput();
            });
            if (Objects.equals(conversationLanguage, getString(R.string.GREEK))) {
                chat = buildChat(chatbot, localeGR);
                runChat(chat, localeGR);
                if (lastBookmark == null) {
                    //chatbot.async().goToBookmark(bookmarksGR.get("INTRO"), AutonomousReactionImportance.HIGH, AutonomousReactionValidity.IMMEDIATE);
                }
            } else if (Objects.equals(conversationLanguage, getString(R.string.ENGLISH))) {
                chat = buildChat(chatbot, localeEN);
                runChat(chat, localeGR);
                if (lastBookmark == null) {
                    //chatbot.async().goToBookmark(bookmarksEN.get("INTRO"), AutonomousReactionImportance.HIGH, AutonomousReactionValidity.IMMEDIATE);
                }
            }
        } else if (Objects.equals(conversationMode, getString(R.string.WRITTEN_CONVERSATION_VALUE))) {
            if (chatFuture != null) {
                if (!chatFuture.isSuccess() || !chatFuture.isCancelled() || !chatFuture.isDone()) {
                    chatFuture.requestCancellation();
                }
            }
            runOnUiThread(() -> {
                chatFragment.showTextInput();
            });
            if (Objects.equals(conversationLanguage, getString(R.string.GREEK))) {
                if (lastBookmark == null) {
                    //chatbot.async().goToBookmark(bookmarksGR.get("INTRO"), AutonomousReactionImportance.HIGH, AutonomousReactionValidity.IMMEDIATE);
                }
            } else if (Objects.equals(conversationLanguage, getString(R.string.ENGLISH))) {
                if (lastBookmark == null) {
                    //chatbot.async().goToBookmark(bookmarksEN.get("INTRO"), AutonomousReactionImportance.HIGH, AutonomousReactionValidity.IMMEDIATE);
                }
            }
        }
    }


    /**
     * _____________________________________________________________________________
     * <h1>Conversation Methods</h1>
     */

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
                chatFragment.updateRecyclerView(LayoutRobotImage, R.drawable.red_bauxite_pissoliths);
                break;
            case "PISOLITH":
                chatFragment.updateRecyclerView(LayoutRobotImage, R.drawable.white_bauxite);
                break;
            case "BAUXITE.3":
                chatFragment.updateRecyclerView(LayoutRobotImage, R.drawable.aluminium);
                break;
            default:
                break;
        }
        lastBookmark = bookmark;
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
            chatFragment.updateRecyclerView(LayoutRobot, sayingText);

            if (lastBookmark != null) {
                lastBookmark.async().getName().thenConsume(value -> {
                    if (value.hasError()) {
                        Log.i(TAG, "[lastBookmark.async().getName()]: " + value.getErrorMessage());
                    } else {
                        if (value.get().contains("QUESTION.")) {
                            if (quizFragment != null && quizFragment.isVisible()) {
                                quizFragment.setQuestion(phrase.getText());
                            }
                        }
                    }
                });
            }


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
            chatFragment.updateRecyclerView(LayoutUser, heardText);
        }
    }

    /**
     * Represents a ChatbotReaction suggested spontaneously by the Chatbot and which is not the
     * reaction to a Phrase (as those should be managed by Chatbot.replyTo()).
     * Ex: The robot sees a person and the Chatbot wants to proactively greet him.
     *
     * @param autonomousReaction the autonomousReaction value
     * @since 3
     */
    @Override
    public void onAutonomousReactionChanged(AutonomousReaction autonomousReaction) {
        long currentTime = System.currentTimeMillis();
        long difference = Math.abs(lastAutonomousReactionUpdate - currentTime);
        if (autonomousReaction.equals(lastAutonomousReaction) && difference < 1000) {
            Log.d(TAG, "Time between autonomousReactions: " + difference + "ms. Duplicate autonomousReaction purged.");

        } else {
            lastAutonomousReactionUpdate = currentTime;
            lastAutonomousReaction = autonomousReaction;

            ChatbotReaction chatbotReaction = autonomousReaction.getChatbotReaction();
            if (chatbotReaction.getChatbotReactionHandlingStatus() == ChatbotReactionHandlingStatus.HANDLED) {
                Log.d(TAG, "[autonomousReaction]: already handled ");
            } else {
                Log.d(TAG, "[autonomousReaction]: " + chatbotReaction.toString());
                if (!speechEngine.getSaying().getText().isEmpty()) {
                    Log.d(TAG, "[autonomousReaction][speechEngine.getSaying]: " + speechEngine.getSaying().getText());
                } else {
                    if (chat == null) {
                        Log.d(TAG, "[autonomousReaction]: chat is null");
                        chatbotReaction.runWith(speechEngine);
                    } else {
                        if (!chat.getSaying().getText().isEmpty()) {
                            Log.d(TAG, "[autonomousReaction][chat.getSaying]: " + chat.getSaying().getText());
                        } else {
                            chatbotReaction.runWith(speechEngine);
                        }
                    }
                }
            }
        }
    }

    private void buildTopics() {
        topicListGR = buildTopicList(new LinkedList<Integer>(
                Arrays.asList(
                        R.raw.lexicon_gr,
                        R.raw.introduction_gr,
                        R.raw.bauxite_gr
                )
        ), localeGR);
        topicListEN = buildTopicList(new LinkedList<Integer>(
                Arrays.asList(
                        R.raw.lexicon_en,
                        R.raw.introduction_en,
                        R.raw.bauxite_en,
                        R.raw.quiz_en
                )
        ), localeEN);


    }

    public List<Topic> buildTopicList(List<Integer> topics, Locale locale) {
        List<Topic> topicList;
        topicList = new LinkedList<>();
        for (int topicName : topics) {
            Topic topic = TopicBuilder
                    .with(qiContext)
                    .withResource(topicName)
                    .build();
            topicList.add(topic);
            extractBookmarks(topic, locale);
        }
        registerBookmarksToLibrary(locale.getLanguage());


        return topicList;
    }

    private void registerBookmarksToLibrary(Language language) {
        String languageCode = StringUtility.getLanguageCode(language);
        Object foundBookmarks = StringUtility.checkVariablesForSubstring(this, "bookmarks" + languageCode);
        if (foundBookmarks != null) {
            Map<String, Bookmark> tempBookmarks = (Map<String, Bookmark>) foundBookmarks;
            if (!bookmarksLibrary.contains(tempBookmarks)) {
                bookmarksLibrary.add(tempBookmarks);
            }

            Object foundQuestions = StringUtility.checkVariablesForSubstring(this, "questions" + languageCode);
            if (foundQuestions != null) {
                Map<String, Bookmark> tempQuestions = (Map<String, Bookmark>) foundQuestions;
                for (Map.Entry<String, Bookmark> entry : tempBookmarks.entrySet()) {
                    if (entry.getKey().contains("QUESTION.")) {
                        if (!tempQuestions.containsKey(entry.getKey())) {
                            tempQuestions.put(entry.getKey(), entry.getValue());
                        }
                    }
                }

                Object foundAnswers = StringUtility.checkVariablesForSubstring(this, "answers" + languageCode);
                if (foundAnswers != null) {
                    Map<String, Bookmark> tempAnswers = (Map<String, Bookmark>) foundAnswers;
                    for (Map.Entry<String, Bookmark> entry : tempBookmarks.entrySet()) {
                        if (entry.getKey().contains("ANSWER.")) {
                            if (!tempAnswers.containsKey(entry.getKey())) {
                                tempAnswers.put(entry.getKey(), entry.getValue());
                            }
                        }
                    }


                }

            }
        }
    }

    private void extractBookmarks(Topic topic, Locale locale) {
        if (locale == localeEN) {
            if (bookmarksEN == null) {
                bookmarksEN = topic.getBookmarks();
            } else {
                bookmarksEN.putAll(topic.getBookmarks());
            }
            //Log.d(TAG, "BookmarksEN: " + bookmarksEN.toString());
        }
        if (locale == localeGR) {
            if (bookmarksGR == null) {
                bookmarksGR = topic.getBookmarks();
            } else {
                bookmarksGR.putAll(topic.getBookmarks());
            }
            //Log.d(TAG, "BookmarksGR: " + bookmarksGR.toString());
        }

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
    public QiChatbot buildQiChatbot(List<Topic> topicList, Locale locale) throws ExecutionException {

        QiChatbot newChatbot;

        Future<QiChatbot> qiChatbotBuilderFuture = QiChatbotBuilder
                .with(qiContext)
                .withTopics(topicList)
                .withLocale(locale)
                .buildAsync();

        newChatbot = qiChatbotBuilderFuture.thenCompose(value -> {
            if (value.hasError()) {
                Log.e(TAG, "[qiChatbotBuilder][ERROR]: " + value.getErrorMessage());
                Toast.makeText(getApplicationContext(), value.getErrorMessage(), Toast.LENGTH_LONG).show();
                return null;
            } else {
                return value;
            }
        }).get();

        if (newChatbot != null) {
            newChatbot.async().addOnBookmarkReachedListener(this);
            newChatbot.async().addOnEndedListener(endReason -> {
                Log.i(TAG, "Chatbot ended for reason: " + endReason);
                chatFuture.requestCancellation();
            });
            newChatbot.async().addOnAutonomousReactionChangedListener(this);
            if (lastBookmark != null) {
                newChatbot.async().goToBookmark(lastBookmark, AutonomousReactionImportance.HIGH, AutonomousReactionValidity.IMMEDIATE);
            }
            return newChatbot;
        } else {
            return null;
        }
    }

    public Chat buildChat(QiChatbot chatbot, Locale locale) throws ExecutionException {
        if (chatbot == null) {
            Log.e(TAG, "[buildChat]: chatbot is null.");
            return null;
        }
        Future<Chat> buildChatFuture = ChatBuilder
                .with(qiContext)
                .withChatbot(chatbot)
                .withLocale(locale)
                .buildAsync();
        Chat chat = buildChatFuture.thenCompose((Function<Future<Chat>, Future<Chat>>) value -> {
            if (value.hasError()) {
                Log.e(TAG, "[ChatBuilder][ERROR]: " + value.getErrorMessage());
                Toast.makeText(getApplicationContext(), value.getErrorMessage(), Toast.LENGTH_LONG).show();
                return null;
            } else {
                return value;
            }
        }).get();
        chat.async().addOnStartedListener(() -> {
            Log.i(TAG, "[CHAT] Chat started.");
        });
        chat.async().addOnListeningChangedListener(listening -> {
            if (listening) {
                Log.i(TAG, "[CHAT] Listening START.");
            } else {
                Log.i(TAG, "[CHAT] Listening END.");
            }

        });
        chat.async().addOnSayingChangedListener(this);
        chat.async().addOnHeardListener(this);
        chat.async().addOnNormalReplyFoundForListener(input -> {
            Log.i(TAG, "[NormalReplyFoundFor]: " + input.getText());
        });
        chat.async().addOnNoPhraseRecognizedListener(() -> {
            Log.i(TAG, "[CHAT] No phrase recognized.");
        });

        chat.async().addOnFallbackReplyFoundForListener(input -> {
            Log.i(TAG, "[CHAT] Fallback Reply found for user message: " + input.getText());
        });
        chat.async().addOnNoReplyFoundForListener(input -> {
            Log.i(TAG, "[CHAT] NO Reply found for user message: " + input.getText());
        });
        return chat;
    }

    public void runChat(Chat chat, Locale locale) {
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
        speechEngine.addOnSayingChangedListener(this);
    }

    public void replyTo(Phrase userPhrase, Locale locale) {
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

                        if (speechEngine.getSaying().getText().isEmpty()) {
                            if (chat != null) {
                                if (chat.getSaying().getText().isEmpty()) {
                                    Future<Void> runWithFuture = chatbotReaction.async().runWith(speechEngine);
                                    runWithFuture.thenConsume(future -> {
                                        if (future.hasError()) {
                                            Log.e(TAG, "runWith Future [ERROR]: " + future.getErrorMessage());
                                        } else {

                                        }
                                    });
                                }
                            }
                        }


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
            fabQuiz = findViewById(R.id.fab_quiz);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addListeners() {
        fabSettings.setOnClickListener(this);
        fabEncyclopedia.setOnClickListener(this);
        fabQuiz.setOnClickListener(this);
    }

    private void removeListeners() {
        if (chat != null) {
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

    }

    public void changeFragment(Fragment newFragment) {
        runOnUiThread(() -> {
            if (newFragment instanceof QuizFragment) {
                fabQuiz.setImageResource(R.drawable.icons8_chat_100);
            } else if (newFragment instanceof ChatFragment) {
                fabQuiz.setImageResource(R.drawable.icons8_quiz_100);
                Bundle bundle = new Bundle();
                bundle.putString(getString(R.string.CONVERSATION_MODE_KEY), PreferenceManager.getDefaultSharedPreferences(this).getString(getString(R.string.CONVERSATION_MODE_KEY), "NONE_VALUE"));
                chatFragment.setArguments(bundle);

            }
        });

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.chat_container, newFragment);
        transaction.commit();
    }



}