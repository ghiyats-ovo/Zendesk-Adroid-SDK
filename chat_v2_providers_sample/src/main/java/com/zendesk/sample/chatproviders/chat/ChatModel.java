package com.zendesk.sample.chatproviders.chat;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import android.util.Log;
import java.io.File;
import java.lang.ref.WeakReference;

import java.util.ArrayList;
import java.util.List;
import zendesk.chat.Chat;
import zendesk.chat.ChatLog;
import zendesk.chat.ChatLog.AttachmentMessage;
import zendesk.chat.ChatProvider;
import zendesk.chat.ChatSessionStatus;
import zendesk.chat.ChatState;
import zendesk.chat.ConnectionProvider;
import zendesk.chat.ConnectionStatus;
import zendesk.chat.DeliveryStatus;
import zendesk.chat.FileUploadListener;
import zendesk.chat.ObservationScope;
import zendesk.chat.Observer;

/**
 * Model class that's responsible for interacting with Zendesk Chat API.
 */
class ChatModel implements ChatMvp.Model {

    private final ChatProvider chatProvider;

    private final ConnectionProvider connectionProvider;

    private final Context context;

    private BroadcastReceiver timeoutReceiver;

    private Handler mainHandler;

    private WeakReference<ChatListener> chatListener;

    private ObservationScope chatStateObservationScope;

    private ObservationScope connectionObservationScope;

    private ArrayList<ChatLog> chatLogs = new ArrayList<>();

    ChatModel(ChatProvider chatProvider, ConnectionProvider connectionProvider, Context context) {
        this.chatProvider = chatProvider;
        this.connectionProvider = connectionProvider;
        this.context = context;
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    @Override
    public void sendMessage(final String message) {
        chatProvider.sendMessage(message);
    }

    @Override
    public void sendAttachment(final File file) {
        final AttachmentMessage messageLog = chatProvider.sendFile(file, new FileUploadListener() {
            @Override
            public void onProgress(final String chatLogId, final long bytesUploaded, final long contentLength) {
                Log.d("DUDIDAM", "onProgress: chatLogId:" + chatLogId + " bytesUploaded " + bytesUploaded
                        + " content Length " + contentLength);
            }
        });

//        observeUploadStatus(messageLog);
    }

    private void observeUploadStatus(final AttachmentMessage messageLog) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                ChatLog updatedLog = getChatLogById(messageLog.getId());
                if (updatedLog == null){ return; }
                Log.w("DUDIDAM", "sendAttachment status :" + updatedLog.getDeliveryStatus());
                if (updatedLog.getDeliveryStatus() != DeliveryStatus.DELIVERED){
//                    connectionProvider.disconnect();
                    chatProvider.deleteFailedMessage(messageLog.getId());
//                    connectionProvider.connect();
                    observeUploadStatus(messageLog);
                }
            }
        }, 10000);
    }

    private ChatLog getChatLogById(String chatLogId){
        ChatLog requestedLog = null;
        for (ChatLog log: chatLogs) {
            if (log.getId().equals(chatLogId)){
                requestedLog = log;
                break;
            }
        }
        return requestedLog;
    }

    private void bindChatListener() {
        chatStateObservationScope = new ObservationScope();
        Observer<ChatState> chatStateObserver = new Observer<ChatState>() {
            @Override
            public void update(final ChatState chatState) {
                Log.d("DUDIDAM", " ========== CHATLOG UPDATED ============");
                chatLogs.clear();
                chatLogs.addAll(chatState.getChatLogs());
                for (ChatLog log: chatLogs) {
                    Log.d("DUDIDAM", "Sender: " + log.getChatParticipant().name() +", Type : "+ log.getType().name() +", delivery: " + log.getDeliveryStatus().name());
                }
                // TODO: 11/08/21 1.
                updateChatListener(new UpdateChatLogListener() {
                    @Override
                    public void update(ChatListener chatListener) {
                        chatListener.onUpdateChatState(chatState);
                    }
                });
            }
        };

        chatProvider.observeChatState(chatStateObservationScope, chatStateObserver);
    }

    private void bindConnectionListener(){
        connectionObservationScope = new ObservationScope();
        Observer<ConnectionStatus> connectionStatusObserver = new Observer<ConnectionStatus>() {
            @Override
            public void update(final ConnectionStatus connectionStatus) {
                Log.d("DUDIDAM", ": =========== "+ connectionStatus.name() + " ============");
                updateChatListener(new UpdateChatLogListener() {
                    @Override
                    public void update(ChatListener chatListener) {
                        chatListener.onUpdateConnection(connectionStatus);
                    }
                });
            }
        };

        connectionProvider.observeConnectionStatus(connectionObservationScope, connectionStatusObserver);
    }

    @Override
    public void registerChatListener(final ChatListener chatListener) {
        if (this.chatListener != null) {
            unregisterChatListener();
        }

        this.chatListener = new WeakReference<>(chatListener);
        bindChatListener();
        bindConnectionListener();
    }

    @Override
    public void unregisterChatListener() {
        chatListener = null;
        unbindChatListener();
    }

    @Override
    public void clearChatIfEnded() {
        if (chatProvider.getChatState() != null
                && chatProvider.getChatState().getChatSessionStatus() == ChatSessionStatus.ENDED) {
            Chat.INSTANCE.clearCache();
        }
    }

    private void unbindChatListener() {
        if (chatStateObservationScope != null) {
            chatStateObservationScope.cancel();
        }

        if (connectionObservationScope != null) {
            connectionObservationScope.cancel();
        }

        if (timeoutReceiver != null) {
            context.unregisterReceiver(timeoutReceiver);
            timeoutReceiver = null;
        }
    }

    private void updateChatListener(final UpdateChatLogListener updater) {
        if (chatListener != null && chatListener.get() != null) {
            mainHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (chatListener != null && chatListener.get() != null) {
                        updater.update(chatListener.get());
                    }
                }
            });
        }
    }

    private interface UpdateChatLogListener {

        void update(ChatListener chatListener);
    }
}
