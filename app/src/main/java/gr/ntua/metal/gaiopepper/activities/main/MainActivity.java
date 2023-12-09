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

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.util.Pair;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.preference.PreferenceManager;

import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.QiSDK;
import com.aldebaran.qi.sdk.RobotLifecycleCallbacks;
import com.aldebaran.qi.sdk.design.activity.RobotActivity;
import com.aldebaran.qi.sdk.design.activity.conversationstatus.SpeechBarDisplayPosition;
import com.aldebaran.qi.sdk.design.activity.conversationstatus.SpeechBarDisplayStrategy;
import com.aldebaran.qi.sdk.object.conversation.AutonomousReaction;
import com.aldebaran.qi.sdk.object.conversation.Bookmark;
import com.aldebaran.qi.sdk.object.conversation.Chat;
import com.aldebaran.qi.sdk.object.conversation.Chatbot;
import com.aldebaran.qi.sdk.object.conversation.ChatbotReaction;
import com.aldebaran.qi.sdk.object.conversation.ChatbotReactionHandlingStatus;
import com.aldebaran.qi.sdk.object.conversation.Phrase;
import com.aldebaran.qi.sdk.object.conversation.QiChatbot;
import com.aldebaran.qi.sdk.object.conversation.SpeechEngine;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import gr.ntua.metal.gaiopepper.R;
import gr.ntua.metal.gaiopepper.activities.encyclopedia.EncyclopediaActivity;
import gr.ntua.metal.gaiopepper.activities.settings.SettingsActivity;
import gr.ntua.metal.gaiopepper.models.MessageItem;
import gr.ntua.metal.gaiopepper.util.AutonomousAbilitiesManager;
import gr.ntua.metal.gaiopepper.util.ChatManager;
import gr.ntua.metal.gaiopepper.util.QuizManager;
import gr.ntua.metal.gaiopepper.util.StringUtility;
import gr.ntua.metal.gaiopepper.util.TimingUtility;

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
    public final ChatManager chatManager = new ChatManager(this);
    public final QuizManager quizManager = new QuizManager(this);

    private AudioManager audioManager;

    public Intent settingsIntent = null;

    public ChatFragment chatFragment;
    public QuizFragment quizFragment;

    private FloatingActionButton fabSettings;
    private FloatingActionButton fabEncyclopedia;
    private FloatingActionButton fabQuiz;

    protected QiContext qiContext;

    protected List<MessageItem> messageItemList;
    protected MessageAdapter messageAdapter;

    public TimingUtility timingManager;


    public QiContext getQiContext() {
        return this.qiContext;
    }


    /**
     * _____________________________________________________________________________
     * <h1>Override Methods</h1>
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate");
        super.onCreate(savedInstanceState);

        timingManager = new TimingUtility();

        PreferenceManager.getDefaultSharedPreferences(this).edit().clear().commit();
        PreferenceManager.setDefaultValues(this, R.xml.preferences_root, true);

        chatFragment = new ChatFragment();
        quizFragment = new QuizFragment();

        messageItemList = new ArrayList<>();
        messageAdapter = new MessageAdapter(this, messageItemList);


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

        chatManager.buildTopics();
        chatManager.makeSpeechEngine(qiContext);

        changeFragment(chatFragment);


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
        removeListeners();
        this.qiContext = null;
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
            chatManager.setLastBookmark(null);
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
                chatManager.chatbot = chatManager.buildQiChatbot(chatManager.topicListGR, chatManager.localeGR);
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        } else if (Objects.equals(conversationLanguage, getString(R.string.ENGLISH))) {
            try {
                chatManager.chatbot = chatManager.buildQiChatbot(chatManager.topicListEN, chatManager.localeEN);
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
        if (Objects.equals(conversationMode, getString(R.string.NONE_VALUE))) {
            if (chatManager.chatFuture != null) {
                if (!chatManager.chatFuture.isSuccess() || !chatManager.chatFuture.isCancelled() || !chatManager.chatFuture.isDone()) {
                    chatManager.chatFuture.requestCancellation();
                }
            }
        } else if (Objects.equals(conversationMode, getString(R.string.ORAL_CONVERSATION_VALUE))) {
            runOnUiThread(() -> {
                chatFragment.hideTextInput();
            });
            if (Objects.equals(conversationLanguage, getString(R.string.GREEK))) {
                chatManager.chat = chatManager.buildChat(chatManager.chatbot, chatManager.localeGR);
                chatManager.runChat(chatManager.chat, chatManager.localeGR);
                if (chatManager.getLastBookmark() == null) {
                    //chatbot.async().goToBookmark(bookmarksGR.get("INTRO"), AutonomousReactionImportance.HIGH, AutonomousReactionValidity.IMMEDIATE);
                }
            } else if (Objects.equals(conversationLanguage, getString(R.string.ENGLISH))) {
                chatManager.chat = chatManager.buildChat(chatManager.chatbot, chatManager.localeEN);
                chatManager.runChat(chatManager.chat, chatManager.localeGR);
                if (chatManager.getLastBookmark() == null) {
                    //chatbot.async().goToBookmark(bookmarksEN.get("INTRO"), AutonomousReactionImportance.HIGH, AutonomousReactionValidity.IMMEDIATE);
                }
            }
        } else if (Objects.equals(conversationMode, getString(R.string.WRITTEN_CONVERSATION_VALUE))) {
            if (chatManager.chatFuture != null) {
                if (!chatManager.chatFuture.isSuccess() || !chatManager.chatFuture.isCancelled() || !chatManager.chatFuture.isDone()) {
                    chatManager.chatFuture.requestCancellation();
                }
            }
            runOnUiThread(() -> {
                chatFragment.showTextInput();
            });
            if (Objects.equals(conversationLanguage, getString(R.string.GREEK))) {
                if (chatManager.getLastBookmark() == null) {
                    //chatbot.async().goToBookmark(bookmarksGR.get("INTRO"), AutonomousReactionImportance.HIGH, AutonomousReactionValidity.IMMEDIATE);
                }
            } else if (Objects.equals(conversationLanguage, getString(R.string.ENGLISH))) {
                if (chatManager.getLastBookmark() == null) {
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
                chatManager.setContent(chatFragment, new Pair<>(LayoutRobotImage, R.drawable.red_bauxite_pissoliths));
                break;
            case "PISOLITH":
                chatManager.setContent(chatFragment, new Pair<>(LayoutRobotImage, R.drawable.white_bauxite));
                break;
            case "BAUXITE.3":
                chatManager.setContent(chatFragment, new Pair<>(LayoutRobotImage, R.drawable.aluminium));
                break;
            default:
                break;
        }
        if (bookmark.getName().contains("QUESTION.")) {
            String content = bookmark.getTopic().getContent();
            String name = bookmark.getName();
            String proposalContent = StringUtility.extractProposal(name, content);
            runOnUiThread(() -> {
                quizManager.setContent(quizFragment, name, proposalContent);
            });
        }
        chatManager.setLastBookmark(bookmark);
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
            chatManager.setContent(chatFragment, new Pair<>(LayoutRobot, sayingText));
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
            chatManager.setContent(chatFragment, new Pair<>(LayoutUser, heardText));

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
        timingManager.checkForDuplicates(autonomousReaction, autonomousReaction1 -> {
            ChatbotReaction chatbotReaction = autonomousReaction1.getChatbotReaction();
            if (chatbotReaction.getChatbotReactionHandlingStatus() == ChatbotReactionHandlingStatus.HANDLED) {
                Log.d(TAG, "[autonomousReaction] [ALREADY HANDLED] ");
            } else if (chatbotReaction.getChatbotReactionHandlingStatus() == ChatbotReactionHandlingStatus.REJECTED) {
                Log.d(TAG, "[autonomousReaction] [REJECTED] ");
            } else {
                if (!chatManager.speechEngine.getSaying().getText().isEmpty()) {
                    Log.d(TAG, "[autonomousReaction] [NOT HANDLED] [speechEngine.getSaying]: " + chatManager.speechEngine.getSaying().getText());
                } else {
                    if (chatManager.chat == null) {
                        Log.d(TAG, "[autonomousReaction] [NOT HANDLED]: chat is null");
                        chatbotReaction.async().runWith(chatManager.speechEngine);
                    } else {
                        if (!chatManager.chat.getSaying().getText().isEmpty()) {
                            Log.d(TAG, "[autonomousReaction] [NOT HANDLED] [chat.getSaying]: " + chatManager.chat.getSaying().getText());
                        } else {
                            Log.d(TAG, "[autonomousReaction] [RUNNING NOW] ");
                            chatbotReaction.async().runWith(chatManager.speechEngine);
                            return;
                        }
                    }
                }
            }
            return;
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
        if (chatManager.chat != null) {
            chatManager.chat.removeAllOnHeardListeners();
            chatManager.chat.removeAllOnFallbackReplyFoundForListeners();
            chatManager.chat.removeAllOnHearingChangedListeners();
            chatManager.chat.removeAllOnListeningChangedListeners();
            chatManager.chat.removeAllOnNoReplyFoundForListeners();
            chatManager.chat.removeAllOnNoPhraseRecognizedListeners();
            chatManager.chat.removeAllOnNormalReplyFoundForListeners();
            chatManager.chat.removeAllOnSayingChangedListeners();
            chatManager.chat.removeAllOnStartedListeners();
        }
        if (chatManager.chatbot != null) {
            chatManager.chatbot.removeAllOnBookmarkReachedListeners();
            chatManager.chatbot.removeAllOnEndedListeners();
            chatManager.chatbot.removeAllOnAutonomousReactionChangedListeners();
        }
        if (chatManager.speechEngine != null) {
            chatManager.speechEngine.removeAllOnSayingChangedListeners();
        }

    }

    public void changeFragment(Fragment newFragment) {

        if (newFragment instanceof QuizFragment) {
            runOnUiThread(() -> {
                fabQuiz.setImageResource(R.drawable.icons8_chat_100);
            });
        } else if (newFragment instanceof ChatFragment) {
            runOnUiThread(() -> {
                fabQuiz.setImageResource(R.drawable.icons8_quiz_100);
            });
            Bundle bundle = new Bundle();
            bundle.putString(getString(R.string.CONVERSATION_MODE_KEY), PreferenceManager.getDefaultSharedPreferences(this).getString(getString(R.string.CONVERSATION_MODE_KEY), "NONE_VALUE"));
            newFragment.setArguments(bundle);
        }
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.chat_container, newFragment);
        transaction.commit();
    }

}