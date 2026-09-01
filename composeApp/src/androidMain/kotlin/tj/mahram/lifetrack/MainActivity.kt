package tj.mahram.lifetrack

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import tj.mahram.lifetrack.core.notifications.NotificationScheduler
import tj.mahram.lifetrack.data.local.DatabaseDriverFactory

class MainActivity : ComponentActivity() {

    private val requestNotificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { /* result ignored */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        maybeRequestNotificationPermission()
        val driverFactory = DatabaseDriverFactory(applicationContext)
        val scheduler = NotificationScheduler(applicationContext)
        setContent {
            App(driverFactory = driverFactory, notificationScheduler = scheduler)
        }
    }

    private fun maybeRequestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                this, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}
