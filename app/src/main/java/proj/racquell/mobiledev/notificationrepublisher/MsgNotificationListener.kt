package proj.racquell.mobiledev.notificationrepublisher

import android.app.Notification
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification

class MsgNotificationListener : NotificationListenerService() {

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        if (sbn.packageName != "com.google.android.apps.messaging") return

        val n = sbn.notification
        val extras = n.extras

        // Skip summary/header notifications
        val isSummary = (n.flags and Notification.FLAG_GROUP_SUMMARY) != 0
        if (isSummary) return

        val sender =
            extras.getCharSequence(Notification.EXTRA_TITLE)?.toString()
                ?: return

        val groupTitle =
            extras.getCharSequence(Notification.EXTRA_CONVERSATION_TITLE)?.toString()
                ?.takeIf { it.isNotBlank() && it != sender } // only treat as group if distinct

        val messageText =
            extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()
                ?: ""

        val conversationKey = buildConversationKey(sbn, n) ?: return

        val shortcutId = if (Build.VERSION.SDK_INT >= 26) n.shortcutId else null
        val openThreadIntent = n.contentIntent

        NotificationRepublisher.onIncomingMessage(
            context = this,
            conversationId = conversationKey,
            senderDisplay = sender,
            groupTitle = groupTitle,          // null for 1:1
            message = messageText,
            timestamp = sbn.postTime,
            shortcutId = shortcutId,
            contentIntent = openThreadIntent
        )
    }

    private fun buildConversationKey(sbn: StatusBarNotification, n: Notification): String? {
        val shortcutId = if (Build.VERSION.SDK_INT >= 26) n.shortcutId else null
        if (!shortcutId.isNullOrBlank()) return "gmsg:shortcut:$shortcutId"

        val tag = sbn.tag
        if (!tag.isNullOrBlank()) return "gmsg:tag:$tag:id:${sbn.id}"

        val key = sbn.key
        if (key.isNotBlank()) return "gmsg:key:$key"

        return null
    }
}