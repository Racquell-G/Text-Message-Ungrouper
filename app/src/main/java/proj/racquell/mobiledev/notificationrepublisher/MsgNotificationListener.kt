package proj.racquell.mobiledev.notificationrepublisher
import android.Manifest
import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import androidx.annotation.RequiresPermission

class MsgNotificationListener : NotificationListenerService() {

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override fun onNotificationPosted(sbn: StatusBarNotification) {
        if (sbn.packageName != "com.google.android.apps.messaging") return

        val n = sbn.notification
        val extras = n.extras

        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString() ?: return
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""

        // A "best effort" conversation/person key
        val conversationId =
            extras.getCharSequence(Notification.EXTRA_CONVERSATION_TITLE)?.toString()
                ?: title

        // Republish (you’ll implement this next)
        NotificationRepublisher.onIncomingMessage(
            context = this,
            conversationId = conversationId,
            senderDisplay = title,
            message = text,
            timestamp = sbn.postTime
        )
    }
}
