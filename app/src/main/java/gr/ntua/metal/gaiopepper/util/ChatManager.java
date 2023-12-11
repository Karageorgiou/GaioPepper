package gr.ntua.metal.gaiopepper.util;

import android.util.Log;
import android.widget.Toast;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.util.Pair;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import com.aldebaran.qi.Function;
import com.aldebaran.qi.Future;
import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.builder.ChatBuilder;
import com.aldebaran.qi.sdk.builder.QiChatbotBuilder;
import com.aldebaran.qi.sdk.builder.TopicBuilder;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

import gr.ntua.metal.gaiopepper.R;
import gr.ntua.metal.gaiopepper.activities.main.ChatFragment;
import gr.ntua.metal.gaiopepper.activities.main.MainActivity;
import gr.ntua.metal.gaiopepper.util.fsm.chat.ChatState;

public class ChatManager implements IManager{
    private static final String TAG = "Chat Manager";
    private Bookmark lastBookmark = null;

    private final MainActivity mainActivity;
    private final Locale localeGR;
    private final Locale localeEN;
    private Locale currentLocale;
    public List<Topic> topicListGR;
    public List<Topic> topicListEN;
    public Map<String, Bookmark> bookmarksGR;
    public Map<String, Bookmark> bookmarksEN;
    public List<Map<String, Bookmark>> bookmarksLibrary;
    public SpeechEngine speechEngine;
    public QiChatbot chatbot;
    public Chat chat;
    public volatile Future<Void> chatFuture;

    public ChatManager(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
        this.localeGR = new Locale(Language.GREEK, Region.GREECE);
        this.localeEN = new Locale(Language.ENGLISH, Region.UNITED_STATES);

        this.bookmarksLibrary = new ArrayList<>();

    }

    public void buildTopics() {
        if (topicListGR == null) {
            Log.i(TAG, "Building topic GR");
            topicListGR = buildTopicList(new LinkedList<>(
                    Arrays.asList(
                            R.raw.lexicon_gr,
                            R.raw.introduction_gr,
                            R.raw.bauxite_gr
                    )
            ), localeGR);
        }
        if (topicListEN == null) {
            Log.i(TAG, "Building topic EN");
            topicListEN = buildTopicList(new LinkedList<>(
                    Arrays.asList(
                            R.raw.lexicon_en,
                            R.raw.introduction_en,
                            R.raw.bauxite_en,
                            R.raw.quiz_en
                    )
            ), localeEN);
        }
    }

    public List<Topic> buildTopicList(List<Integer> topics, Locale locale) {
        List<Topic> topicList;
        topicList = new LinkedList<Topic>();
        for (int topicName : topics) {
            Topic topic = TopicBuilder
                    .with(mainActivity.getQiContext())
                    .withResource(topicName)
                    .build();
            topicList.add(topic);
            extractBookmarks(topic, locale);
        }
        registerBookmarksToLibrary(locale.getLanguage());


        return topicList;
    }

    public void registerBookmarksToLibrary(Language language) {
        String languageCode = StringUtility.extractLanguageCode(language);
        Object foundBookmarks = StringUtility.checkVariablesForSubstring(this, "bookmarks" + languageCode);
        if (foundBookmarks != null) {
            Map<String, Bookmark> tempBookmarks = (Map<String, Bookmark>) foundBookmarks;
            if (!bookmarksLibrary.contains(tempBookmarks)) {
                bookmarksLibrary.add(tempBookmarks);
            }

            Object foundQuestions = StringUtility.checkVariablesForSubstring(mainActivity.quizManager, "questions" + languageCode);
            if (foundQuestions != null) {
                Map<String, Bookmark> tempQuestions = (Map<String, Bookmark>) foundQuestions;
                for (Map.Entry<String, Bookmark> entry : tempBookmarks.entrySet()) {
                    if (entry.getKey().contains("QUESTION.")) {
                        //Log.d(TAG, "Found Bookmark with name: " + entry.getKey());
                        if (!tempQuestions.containsKey(entry.getKey())) {
                            tempQuestions.put(entry.getKey(), entry.getValue());
                        }
                    }
                }

                Object foundAnswers = StringUtility.checkVariablesForSubstring(mainActivity.quizManager, "answers" + languageCode);
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

    public void extractBookmarks(Topic topic, Locale locale) {
        if (locale == localeEN) {
            if (bookmarksEN == null) {
                bookmarksEN = topic.getBookmarks();
            } else {
                bookmarksEN.putAll(topic.getBookmarks());
            }
        }
        if (locale == localeGR) {
            if (bookmarksGR == null) {
                bookmarksGR = topic.getBookmarks();
            } else {
                bookmarksGR.putAll(topic.getBookmarks());
            }
        }

    }

    /**
     * Returns a <b>QiChatbot</b> instance built with the specified parameters.
     * The topicList attribute must be generated by <b>buildTopicList</b> method.
     * The locale is already created as a global variable and can be found on top.
     *
     * @param topicList a topicList created by buildTopicList method
     * @param locale    Locale from package com.aldebaran.qi.sdk.object.locale
     * @see QiChatbot QiChatbot
     * @see Locale Locale
     */
    public QiChatbot buildQiChatbot(List<Topic> topicList, Locale locale) throws ExecutionException {

        QiChatbot newChatbot;

        Future<QiChatbot> qiChatbotBuilderFuture = QiChatbotBuilder
                .with(mainActivity.getQiContext())
                .withTopics(topicList)
                .withLocale(locale)
                .buildAsync();

        newChatbot = qiChatbotBuilderFuture.thenCompose(value -> {
            if (value.hasError()) {
                Log.e(TAG, "[qiChatbotBuilder][ERROR]: " + value.getErrorMessage());
                Toast.makeText(mainActivity.getApplicationContext(), value.getErrorMessage(), Toast.LENGTH_LONG).show();
                return null;
            } else {
                return value;
            }
        }).get();

        if (newChatbot != null) {
            newChatbot.async().addOnBookmarkReachedListener(mainActivity);
            newChatbot.async().addOnEndedListener(endReason -> {
                Log.i(TAG, "Chatbot ended for reason: " + endReason);
                tryCancelChat();
            });
            newChatbot.async().addOnAutonomousReactionChangedListener(mainActivity);
            if (this.lastBookmark != null) {
                newChatbot.async().goToBookmark(this.lastBookmark, AutonomousReactionImportance.HIGH, AutonomousReactionValidity.IMMEDIATE);
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
                .with(mainActivity.getQiContext())
                .withChatbot(chatbot)
                .withLocale(locale)
                .buildAsync();
        Chat chat = buildChatFuture.thenCompose((Function<Future<Chat>, Future<Chat>>) value -> {
            if (value.hasError()) {
                Log.e(TAG, "[ChatBuilder][ERROR]: " + value.getErrorMessage());
                Toast.makeText(mainActivity.getApplicationContext(), value.getErrorMessage(), Toast.LENGTH_LONG).show();
                return null;
            } else {
                return value;
            }
        }).get();
        chat.async().addOnStartedListener(() -> {
            Log.i(TAG, "[CHAT] Chat started.");
        });
        chat.async().addOnListeningChangedListener(listening -> {
            //Log.i(TAG, "OnListeningChanged: ");
            if (listening) {
                mainActivity.chatFSM.changeState(ChatState.listening, null);
                Log.i(TAG, "[CHAT] Listening START.");
            } else {
                mainActivity.chatFSM.changeState(ChatState.alive, null);
                Log.i(TAG, "[CHAT] Listening END.");
            }

        });
        chat.async().addOnSayingChangedListener(mainActivity);
        chat.async().addOnHeardListener(mainActivity);
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

    public void runChat(Chat chat) {
        Log.i(TAG, "[runChat]: Trying to run chat...");
        if (chat == null) {
            Log.e(TAG, "[runChat]: chat is null.");
            return;
        }
        chatFuture = chat.async().run();

        chatFuture.thenConsume(future -> {
            if (future.hasError()) {
                Log.e(TAG, "[runChat]: Chat finished with error: " + future.getErrorMessage());
                chatFuture = null;
            } else if(future.isCancelled()) {
                Log.i(TAG, "[runChat]: Chat canceled successfully. ");
                chatFuture = null;
            } else {
                Log.e(TAG, "[runChat]: Chat finished: " + future.get().toString());
                chatFuture = null;
            }
        });
    }

    public void makeSpeechEngine(QiContext qiContext) {
        speechEngine = qiContext.getConversation().makeSpeechEngine(qiContext.getRobotContext());
        speechEngine.addOnSayingChangedListener(mainActivity);
    }

    private boolean replyTo(Phrase userPhrase, Locale locale) {
        AtomicBoolean error = new AtomicBoolean(false);
        Future<ReplyReaction> replyToFuture = chatbot.async().replyTo(userPhrase, locale);
        replyToFuture.thenConsume(replyReactionFuture -> {
            if (replyReactionFuture.hasError()) {
                Log.e(TAG, "Reply Reaction Future [ERROR]: " + replyReactionFuture.getErrorMessage());
                error.set(true);
            } else {
                tryCancelChat();
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
                                    if(!chat.getListening()) {
                                        Future<Void> runWithFuture = chatbotReaction.async().runWith(speechEngine);
                                        runWithFuture.thenConsume(future -> {
                                            if (future.hasError()) {
                                                Log.e(TAG, "runWith Future [ERROR]: " + future.getErrorMessage());
                                            } else {
                                                String conversationMode = PreferenceManager.getDefaultSharedPreferences(mainActivity).getString(mainActivity.getString(R.string.CONVERSATION_MODE_KEY), "NONE_VALUE");
                                                if ((Objects.equals(conversationMode, mainActivity.getString(R.string.ORAL_CONVERSATION_VALUE))) && mainActivity.chatManager.chatFuture == null) {
                                                    mainActivity.chatManager.runChat(mainActivity.chatManager.chat);
                                                }
                                            }
                                        });
                                    }
                                }
                            }
                        }
                    }
                });
            }
        });
        return error.get();
    }

    public void tryCancelChat() {
        if (chatFuture != null) {
            Log.i(TAG, "Canceling chat...");
            chatFuture.requestCancellation();
            while (true) {
                if (chatFuture == null) break;
            }
        } else {
            Log.i(TAG, "Chat is already canceled");
        }
    }

    public void replyTo(String message, Locale locale) {
        Phrase userPhrase = new Phrase(message);
        Log.i(TAG, "[CHATBOT] [replyTo] User message: " + userPhrase.getText());
        try {
            if (!replyTo(userPhrase, locale)) {
                String conversationMode = PreferenceManager.getDefaultSharedPreferences(mainActivity).getString(mainActivity.getString(R.string.CONVERSATION_MODE_KEY), "NONE_VALUE");
                if ((Objects.equals(conversationMode, mainActivity.getString(R.string.ORAL_CONVERSATION_VALUE))) && mainActivity.chatManager.chatFuture == null) {
                    mainActivity.chatManager.runChat(mainActivity.chatManager.chat);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Bookmark getLastBookmark() {
        return this.lastBookmark;
    }

    public void setLastBookmark(Bookmark bookmark) {
        this.lastBookmark = bookmark;
    }

    public void setContent(Fragment fragment, Pair<Integer, Object> content) {
        if (content == null) {
            Log.e(TAG, "Content is NULL");
            return;
        }
        if (fragment instanceof ChatFragment) {
            if(content.second instanceof Integer) {
                ((ChatFragment) fragment).updateRecyclerView(content.first, (Integer) content.second);
            }
            if (content.second instanceof String) {
                ((ChatFragment) fragment).updateRecyclerView(content.first, (String) content.second);
            }
        }
    }

    public void setCurrentLocale(String conversationLanguage) {
        if (Objects.equals(conversationLanguage, mainActivity.getString(R.string.GREEK))) {
            currentLocale = localeGR;
        } else if (Objects.equals(conversationLanguage, mainActivity.getString(R.string.ENGLISH))) {
            currentLocale = localeEN;
        } else {
            Log.e(TAG, "Requested conversation language is not installed.");
            return;
        }
        Log.i(TAG, "Current locale set to: " + currentLocale.getLanguage());
    }

    public Locale getCurrentLocale() {
        return currentLocale;
    }

    public void hideTextInput() {
        mainActivity.chatFragment.hideTextInput();
    }

    public void showTextInput() {
        mainActivity.chatFragment.showTextInput();

    }



}