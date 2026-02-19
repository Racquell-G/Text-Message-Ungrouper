package proj.racquell.mobiledev.notificationrepublisher

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : ComponentActivity() {

    private val REQ_POST_NOTIFS = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Android 13+: ask permission to POST notifications (for republished ones)
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                REQ_POST_NOTIFS
            )
            return
        }

        // Open Notification Access settings so the user can enable it
        // TODO: I want this to act like a script, but maybe some UI can be used
        startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))

        // Close immediately
        finish()
    }
}
