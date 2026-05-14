package tj.mahram.lifetrack

import androidx.compose.ui.window.ComposeUIViewController
import tj.mahram.lifetrack.data.local.DatabaseDriverFactory

fun MainViewController() = ComposeUIViewController {
    App(driverFactory = DatabaseDriverFactory())
}
