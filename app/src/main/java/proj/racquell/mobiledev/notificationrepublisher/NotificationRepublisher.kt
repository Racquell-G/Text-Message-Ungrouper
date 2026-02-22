package proj.racquell.mobiledev.notificationrepublisher

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.Person
import androidx.core.content.ContextCompat
import androidx.core.content.LocusIdCompat

object NotificationRepublisher {
    private const val CHANNEL_ID = "republished_messages"

    fun onIncomingMessage(
        context: Context,
        conversationId: String,
        senderDisplay: String,
        groupTitle: String?,              // null => 1:1 chat, non-null => group chat
        message: String,
        timestamp: Long,
        shortcutId: String?,
        contentIntent: PendingIntent?
    ) {
        ensureChannel(context)

        // Android 13+: must be allowed to post notifications
        if (Build.VERSION.SDK_INT >= 33) {
            val granted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) return
        }

        val isGroup = !groupTitle.isNullOrBlank()

        // Store newest -> oldest history per conversation
        val lines = ConversationStore.add(conversationId, message)

        // "Me" required by MessagingStyle
        val me = Person.Builder().setName("You").build()

        // Sender for the message bubbles
        val senderPerson = Person.Builder().setName(senderDisplay).build()

        // IMPORTANT:
        // Do NOT use Kotlin ".apply { }" on MessagingStyle.
        // NotificationCompat.MessagingStyle has an internal/restricted method named "apply(...)"
        // which collides with Kotlin's scope function name and triggers that library-group error.
        val style = NotificationCompat.MessagingStyle(me)

        // We intentionally DO NOT call setConversationTitle / setGroupConversation here,
        // because your current androidx.core version appears not to expose those methods.
        // Instead, we control the collapsed title via setContentTitle below.
        for (line in lines) {
            style.addMessage(line, timestamp, senderPerson)
        }

        // Collapsed title: group name for group chats, sender for 1:1
        val collapsedTitle = if (isGroup) groupTitle!! else senderDisplay

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.sym_action_chat) // replace later with your app icon
            .setStyle(style)
            .setContentTitle(collapsedTitle)
            .setContentText(message)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setWhen(timestamp)
            .setShowWhen(true)
            .setOnlyAlertOnce(true)
            .setAutoCancel(true)
        // NOTE: do NOT call setGroup(...) and do NOT post any summary notification

        // Reuse Google Messages' tap action if available
        if (contentIntent != null) {
            builder.setContentIntent(contentIntent)
        }

        // Help the OS treat each conversation distinctly (best-effort)
        // setShortcutId exists on NotificationCompat.Builder in modern AndroidX;
        // if your dependency is very old and this fails, tell me and I’ll give a fallback.
        if (!shortcutId.isNullOrBlank()) {
            builder.setShortcutId(shortcutId)
            builder.setLocusId(LocusIdCompat(shortcutId))
        } else {
            builder.setLocusId(LocusIdCompat("conv_" + Ids.stableIntId(conversationId)))
        }

        try {
            NotificationManagerCompat.from(context)
                .notify(Ids.stableIntId(conversationId), builder.build())
        } catch (_: SecurityException) {
            // Permission revoked mid-run; ignore
        }
    }

    private fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT < 26) return
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.createNotificationChannel(
            NotificationChannel(
                CHANNEL_ID,
                "Republished Messages",
                NotificationManager.IMPORTANCE_HIGH
            )
        )
    }
}