package com.zendesk.sample.chatproviders.chat.log.items;

import androidx.core.content.ContextCompat;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.zendesk.sample.chatproviders.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import zendesk.chat.Agent;
import zendesk.chat.ChatLog;

/**
 * Class containing a bunch of helper methods to bind often occurring values to known views.
 * <p>
 * Like binding a timestamp to a {@link TextView} or an avatar to a {@link ImageView}.
 */
class BinderHelper {

    private static int CHAT_LOG_TIMESTAMP = R.id.chat_log_holder_timestamp;
    private static int CHAT_LOG_AGENT_AVATAR = R.id.chat_log_holder_avatar;
    private static int CHAT_LOG_VISITOR_VERIFIED = R.id.chat_log_holder_verified;

    private static SimpleDateFormat DATE_FORMAT =
            new SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault());

    static void displayTimeStamp(View itemView, ChatLog chatLog) {
        final View view = itemView.findViewById(CHAT_LOG_TIMESTAMP);
        if (view instanceof TextView) {
            Date date = new Date(chatLog.getLastModifiedTimestamp());
            String format = DATE_FORMAT.format(date);
            ((TextView) view).setText(format);
        }
    }

    static void displayAgentAvatar(View itemView, Agent agent) {
        final View avatar = itemView.findViewById(CHAT_LOG_AGENT_AVATAR);
        if (agent != null && avatar instanceof ImageView) {
            PicassoHelper.loadAvatarImage((ImageView) avatar, agent.getAvatarPath());
        }
    }

    // TODO: 11/08/21 this is where the icon set
    static void displayVisitorVerified(View itemView, boolean verified) {
        final View verifiedView = itemView.findViewById(CHAT_LOG_VISITOR_VERIFIED);
        if (verifiedView instanceof ImageView) {

            final int drawable;
            if (verified) {
                drawable = R.drawable.ic_check_black_18dp;
            } else {
                drawable = R.drawable.ic_sync_black_18dp;
            }

            ((ImageView) verifiedView)
                    .setImageDrawable(ContextCompat.getDrawable(itemView.getContext(), drawable));
        }
    }
}