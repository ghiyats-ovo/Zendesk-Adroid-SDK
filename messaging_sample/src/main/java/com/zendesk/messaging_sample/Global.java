package com.zendesk.messaging_sample;


import android.app.Application;

import com.zendesk.logger.Logger;
import com.zendesk.util.StringUtils;

import zendesk.answerbot.AnswerBot;
import zendesk.chat.Chat;
import zendesk.core.AnonymousIdentity;
import zendesk.core.JwtIdentity;
import zendesk.core.Zendesk;
import zendesk.support.Support;

public class Global extends Application {

    private static final String SUBDOMAIN_URL = "";
    private static final String APPLICATION_ID = "";
    private static final String OAUTH_CLIENT_ID = "";
    private final static String CHAT_ACCOUNT_KEY = "";

    private static boolean missingCredentials = false;

    @Override
    public void onCreate() {
        super.onCreate();

        // Enable logging
        Logger.setLoggable(true);

        if (StringUtils.isEmpty(SUBDOMAIN_URL)
                || StringUtils.isEmpty(APPLICATION_ID)
                || StringUtils.isEmpty(OAUTH_CLIENT_ID)
                || StringUtils.isEmpty(CHAT_ACCOUNT_KEY)) {
            missingCredentials = true;
            return;
        }

        /**
         * Initialize the SDK with your Zendesk subdomain, mobile SDK app ID, and client ID.
         *
         * Get these details from your Zendesk dashboard: Admin -> Channels -> MobileSDK.
         */
        Zendesk.INSTANCE.init(this,
                SUBDOMAIN_URL,
                APPLICATION_ID,
                OAUTH_CLIENT_ID);

        /**
         * Set an identity (authentication).
         *
         * Set either Anonymous or JWT identity, as below:
         */

        // a). Anonymous (All fields are optional)
        Zendesk.INSTANCE.setIdentity(
                new AnonymousIdentity.Builder()
                        .withNameIdentifier("{optional name}")
                        .withEmailIdentifier("{optional email}")
                        .build()
        );

        // b). JWT (Must be initialized with your JWT identifier)
        Zendesk.INSTANCE.setIdentity(new JwtIdentity("{JWT User Identifier}"));

        Support.INSTANCE.init(Zendesk.INSTANCE);
        AnswerBot.INSTANCE.init(Zendesk.INSTANCE, Support.INSTANCE);
        Chat.INSTANCE.init(this, CHAT_ACCOUNT_KEY);
    }

    static boolean isMissingCredentials() {
        return missingCredentials;
    }
}
