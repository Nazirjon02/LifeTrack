package tj.mahram.lifetrack

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import cafe.adriel.voyager.navigator.tab.*
import org.koin.compose.KoinApplication
import org.koin.compose.getKoin
import tj.mahram.lifetrack.di.appModule
import tj.mahram.lifetrack.domain.model.AppTheme
import tj.mahram.lifetrack.domain.repository.CategoryRepository
import tj.mahram.lifetrack.feature.crypto.CryptoScreen
import tj.mahram.lifetrack.feature.dashboard.DashboardScreen
import tj.mahram.lifetrack.feature.finance.FinanceScreen
import tj.mahram.lifetrack.feature.planner.PlannerScreen
import tj.mahram.lifetrack.feature.settings.SettingsScreen
import tj.mahram.lifetrack.ui.theme.LifeTrackTheme

@Composable
fun App(driverFactory: tj.mahram.lifetrack.data.local.DatabaseDriverFactory) {
    KoinApplication(application = {
        modules(
            org.koin.dsl.module {
                single { driverFactory }
            },
            appModule
        )
    }) {
        val koin = getKoin()
        LaunchedEffect(Unit) {
            runCatching { koin.get<CategoryRepository>().initDefaultCategories() }
        }
        LifeTrackTheme(appTheme = AppTheme.DARK) {
            TabNavigator(tab = DashboardTab) { navigator ->
                Scaffold(
                    containerColor = MaterialTheme.colorScheme.background,
                    bottomBar = {
                        NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                            listOf(DashboardTab, PlannerTab, FinanceTab, CryptoTab, SettingsTab).forEach { tab ->
                                val isSelected = navigator.current == tab
                                NavigationBarItem(
                                    selected = isSelected,
                                    onClick = { navigator.current = tab },
                                    icon = {
                                        Icon(
                                            imageVector = tab.tabIcon,
                                            contentDescription = tab.options.title
                                        )
                                    },
                                    label = { Text(tab.options.title) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = MaterialTheme.colorScheme.primary,
                                        selectedTextColor = MaterialTheme.colorScheme.primary,
                                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                                    )
                                )
                            }
                        }
                    }
                ) { padding ->
                    Box(modifier = Modifier.padding(padding)) {
                        CurrentTab()
                    }
                }
            }
        }
    }
}

// Tab definitions
interface AppTab : Tab {
    val tabIcon: ImageVector
}

object DashboardTab : AppTab {
    @Composable
    override fun Content() = DashboardScreen().Content()
    override val options: TabOptions
        @Composable get() = remember { TabOptions(index = 0u, title = "Dashboard") }
    override val tabIcon: ImageVector get() = Icons.Default.Home
}

object PlannerTab : AppTab {
    @Composable
    override fun Content() = PlannerScreen().Content()
    override val options: TabOptions
        @Composable get() = remember { TabOptions(index = 1u, title = "Planner") }
    override val tabIcon: ImageVector get() = Icons.Default.CheckCircle
}

object FinanceTab : AppTab {
    @Composable
    override fun Content() = FinanceScreen().Content()
    override val options: TabOptions
        @Composable get() = remember { TabOptions(index = 2u, title = "Finance") }
    override val tabIcon: ImageVector get() = Icons.Default.AccountBalance
}

object CryptoTab : AppTab {
    @Composable
    override fun Content() = CryptoScreen().Content()
    override val options: TabOptions
        @Composable get() = remember { TabOptions(index = 3u, title = "Crypto") }
    override val tabIcon: ImageVector get() = Icons.Default.TrendingUp
}

object SettingsTab : AppTab {
    @Composable
    override fun Content() = SettingsScreen().Content()
    override val options: TabOptions
        @Composable get() = remember { TabOptions(index = 4u, title = "Settings") }
    override val tabIcon: ImageVector get() = Icons.Default.Settings
}
