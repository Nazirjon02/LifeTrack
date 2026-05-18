package tj.mahram.lifetrack

import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.tab.*
import org.koin.compose.KoinApplication
import org.koin.compose.getKoin
import tj.mahram.lifetrack.core.i18n.LocalStrings
import tj.mahram.lifetrack.core.i18n.stringsFor
import tj.mahram.lifetrack.di.appModule
import tj.mahram.lifetrack.domain.model.AppLanguage
import tj.mahram.lifetrack.domain.model.AppSettings
import tj.mahram.lifetrack.domain.model.AppTheme
import tj.mahram.lifetrack.domain.repository.CategoryRepository
import tj.mahram.lifetrack.domain.repository.SettingsRepository
import tj.mahram.lifetrack.feature.dashboard.DashboardScreen
import tj.mahram.lifetrack.feature.finance.FinanceScreen
import tj.mahram.lifetrack.feature.goals.GoalsScreen
import tj.mahram.lifetrack.feature.habits.HabitsScreen
import tj.mahram.lifetrack.feature.planner.PlannerScreen
import tj.mahram.lifetrack.feature.settings.SettingsScreen
import tj.mahram.lifetrack.ui.theme.LifeTrackTheme

private val DefaultSettings = AppSettings(
    theme = AppTheme.DARK,
    currency = "USD",
    language = AppLanguage.ENGLISH,
    notificationsEnabled = true,
    taskNotificationsEnabled = true,
    financeNotificationsEnabled = true,
    cryptoNotificationsEnabled = true
)

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
        val settingsRepo = remember { koin.get<SettingsRepository>() }
        val settings by settingsRepo.getSettings().collectAsState(initial = DefaultSettings)

        LaunchedEffect(Unit) {
            runCatching { koin.get<CategoryRepository>().initDefaultCategories() }
        }

        CompositionLocalProvider(LocalStrings provides stringsFor(settings.language)) {
            LifeTrackTheme(appTheme = settings.theme) {
                TabNavigator(tab = DashboardTab) { navigator ->
                    Scaffold(
                        containerColor = MaterialTheme.colorScheme.background,
                        bottomBar = {
                            LifeTrackNavBar(navigator = navigator)
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
}

@Composable
private fun LifeTrackNavBar(navigator: TabNavigator) {
    val tabs = listOf(DashboardTab, PlannerTab, HabitsTab, GoalsTab, FinanceTab, SettingsTab)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 16.dp,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp)
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            tabs.forEach { tab ->
                NavPillItem(
                    tab        = tab,
                    isSelected = navigator.current == tab,
                    onClick    = { navigator.current = tab }
                )
            }
        }
    }
}

@Composable
private fun NavPillItem(tab: AppTab, isSelected: Boolean, onClick: () -> Unit) {
    val opts  = tab.options
    val title = opts.title
    val idx   = opts.index.toString()

    val pillColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow, dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "navPill_$idx"
    )
    val iconColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primary
                      else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "navIcon_$idx"
    )

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(pillColor)
            .clickable(onClick = onClick)
            .padding(horizontal = if (isSelected) 14.dp else 10.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Icon(
                imageVector = tab.tabIcon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(20.dp)
            )
            AnimatedVisibility(
                visible = isSelected,
                enter = expandHorizontally(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) +
                        fadeIn(animationSpec = tween(180)),
                exit  = shrinkHorizontally(animationSpec = tween(150)) +
                        fadeOut(animationSpec = tween(100))
            ) {
                Text(
                    title,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = iconColor,
                    maxLines = 1
                )
            }
        }
    }
}

interface AppTab : Tab {
    val tabIcon: ImageVector
}

object DashboardTab : AppTab {
    @Composable
    override fun Content() = DashboardScreen().Content()
    override val options: TabOptions
        @Composable get() {
            val s = LocalStrings.current
            return remember(s) { TabOptions(index = 0u, title = s.tabDashboard) }
        }
    override val tabIcon: ImageVector get() = Icons.Default.Home
}

object PlannerTab : AppTab {
    @Composable
    override fun Content() = PlannerScreen().Content()
    override val options: TabOptions
        @Composable get() {
            val s = LocalStrings.current
            return remember(s) { TabOptions(index = 1u, title = s.tabPlanner) }
        }
    override val tabIcon: ImageVector get() = Icons.Default.CheckCircle
}

object HabitsTab : AppTab {
    @Composable
    override fun Content() = HabitsScreen().Content()
    override val options: TabOptions
        @Composable get() {
            return remember { TabOptions(index = 2u, title = "Habits") }
        }
    override val tabIcon: ImageVector get() = Icons.Default.LocalFireDepartment
}

object GoalsTab : AppTab {
    @Composable
    override fun Content() = GoalsScreen().Content()
    override val options: TabOptions
        @Composable get() = remember { TabOptions(index = 3u, title = "Goals") }
    override val tabIcon: ImageVector get() = Icons.Default.TrackChanges
}

object FinanceTab : AppTab {
    @Composable
    override fun Content() = FinanceScreen().Content()
    override val options: TabOptions
        @Composable get() {
            val s = LocalStrings.current
            return remember(s) { TabOptions(index = 4u, title = s.tabFinance) }
        }
    override val tabIcon: ImageVector get() = Icons.Default.AccountBalance
}

object SettingsTab : AppTab {
    @Composable
    override fun Content() = SettingsScreen().Content()
    override val options: TabOptions
        @Composable get() {
            val s = LocalStrings.current
            return remember(s) { TabOptions(index = 5u, title = s.tabSettings) }
        }
    override val tabIcon: ImageVector get() = Icons.Default.Settings
}
