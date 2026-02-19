import android.Manifest
import android.R
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

object NotificationRepublisher {
    private const val CHANNEL_ID = "republished_messages"

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    fun onIncomingMessage(
        context: Context,
        conversationId: String,
        senderDisplay: String,
        message: String,
        timestamp: Long
    ) {
        ensureChannel(context)

        val lines = ConversationStore.add(conversationId, message)

        val style = NotificationCompat.InboxStyle()
            .setBigContentTitle(senderDisplay)
        lines.forEach { style.addLine(it) } // newest -> oldest

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.sym_action_chat) // replace with your own icon later
            .setContentTitle(senderDisplay)
            .setContentText(message)
            .setStyle(style)
            .setWhen(timestamp)
            .setShowWhen(true)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .build()

        // One notification per conversation/person
        NotificationManagerCompat.from(context)
            .notify(conversationId.hashCode(), notification)
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
