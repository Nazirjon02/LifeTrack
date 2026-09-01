package tj.mahram.lifetrack

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import tj.mahram.lifetrack.core.notifications.NotificationScheduler
import tj.mahram.lifetrack.data.local.DatabaseDriverFactory

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "LifeTrack",
        state = rememberWindowState(width = 1200.dp, height = 800.dp)
    ) {
        App(driverFactory = DatabaseDriverFactory(), notificationScheduler = NotificationScheduler())
    }
}
