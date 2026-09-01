package tj.mahram.lifetrack

import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.tab.*
import cafe.adriel.voyager.transitions.SlideTransition
import tj.mahram.lifetrack.ui.components.AuroraBackground
import tj.mahram.lifetrack.ui.components.brandHorizontalGradient
import tj.mahram.lifetrack.ui.components.glassSurface
import org.koin.compose.KoinApplication
import org.koin.compose.getKoin
import tj.mahram.lifetrack.core.i18n.LocalStrings
import tj.mahram.lifetrack.core.i18n.stringsFor
import tj.mahram.lifetrack.core.notifications.NotificationScheduler
import tj.mahram.lifetrack.di.appModule
import tj.mahram.lifetrack.domain.model.AppLanguage
import tj.mahram.lifetrack.domain.model.AppSettings
import tj.mahram.lifetrack.domain.model.AppTheme
import tj.mahram.lifetrack.domain.repository.CategoryRepository
import tj.mahram.lifetrack.domain.repository.ReminderRepository
import tj.mahram.lifetrack.domain.repository.SettingsRepository
import tj.mahram.lifetrack.feature.dashboard.DashboardScreen
import tj.mahram.lifetrack.feature.finance.FinanceScreen
import tj.mahram.lifetrack.feature.goals.GoalsScreen
import tj.mahram.lifetrack.feature.problems.ProblemsScreen
import tj.mahram.lifetrack.feature.planner.PlannerScreen
import tj.mahram.lifetrack.feature.profile.ProfileScreen
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
fun App(
    driverFactory: tj.mahram.lifetrack.data.local.DatabaseDriverFactory,
    notificationScheduler: NotificationScheduler
) {
    KoinApplication(application = {
        modules(
            org.koin.dsl.module {
                single { driverFactory }
                single { notificationScheduler }
            },
            appModule
        )
    }) {
        val koin = getKoin()
        val settingsRepo = remember { koin.get<SettingsRepository>() }
        val settings by settingsRepo.getSettings().collectAsState(initial = DefaultSettings)

        LaunchedEffect(Unit) {
            runCatching { koin.get<CategoryRepository>().initDefaultCategories() }
            // Re-arm reminder alarms on every launch (covers first launch and
            // schedules that may have been cleared by the OS).
            runCatching {
                val reminders = koin.get<ReminderRepository>().current()
                koin.get<NotificationScheduler>().sync(reminders)
            }
        }

        CompositionLocalProvider(LocalStrings provides stringsFor(settings.language)) {
            LifeTrackTheme(appTheme = settings.theme) {
                AuroraBackground {
                    TabNavigator(tab = DashboardTab) { navigator ->
                        Scaffold(
                            containerColor = Color.Transparent,
                            bottomBar = { LifeTrackNavBar(navigator = navigator) }
                        ) { padding ->
                            Box(modifier = Modifier.fillMaxSize().padding(padding)) {
                                CurrentTab()
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LifeTrackNavBar(navigator: TabNavigator) {
    val tabs = listOf(DashboardTab, PlannerTab, ProblemsTab, GoalsTab, FinanceTab, ProfileTab)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .glassSurface(shape = RoundedCornerShape(28.dp), elevated = true)
                .padding(horizontal = 6.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            tabs.forEach { tab ->
                NavPillItem(
                    tab        = tab,
                    isSelected = navigator.current == tab,
                    onClick    = { navigator.current = tab },
                    modifier   = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun NavPillItem(
    tab: AppTab,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val idx = tab.options.index.toString()
    val sel by animateFloatAsState(
        targetValue = if (isSelected) 1f else 0f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow, dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "navSel_$idx"
    )
    val brand = brandHorizontalGradient()
    val glow = MaterialTheme.colorScheme.primary

    Box(
        modifier = modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .scale(0.9f + 0.1f * sel)
                .drawBehind {
                    if (sel > 0.05f) {
                        drawCircle(
                            brush = Brush.radialGradient(
                                listOf(glow.copy(alpha = 0.45f * sel), Color.Transparent),
                                radius = size.maxDimension * 0.75f
                            ),
                            radius = size.maxDimension * 0.75f
                        )
                    }
                }
                .size(46.dp)
                .clip(RoundedCornerShape(16.dp))
                .then(
                    if (sel > 0.05f) Modifier.background(brand, RoundedCornerShape(16.dp))
                    else Modifier
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = tab.tabIcon,
                contentDescription = tab.options.title,
                tint = lerpColor(MaterialTheme.colorScheme.onSurfaceVariant, Color.White, sel),
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

private fun lerpColor(a: Color, b: Color, t: Float): Color = Color(
    red   = a.red   + (b.red   - a.red)   * t,
    green = a.green + (b.green - a.green) * t,
    blue  = a.blue  + (b.blue  - a.blue)  * t,
    alpha = a.alpha + (b.alpha - a.alpha) * t,
)

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

object ProblemsTab : AppTab {
    @Composable
    override fun Content() = ProblemsScreen().Content()
    override val options: TabOptions
        @Composable get() {
            val s = LocalStrings.current
            return remember(s) { TabOptions(index = 2u, title = s.tabProblems) }
        }
    override val tabIcon: ImageVector get() = Icons.Default.Lightbulb
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
    override fun Content() {
        // Nested navigator so Finance can push a full-screen Debts sub-screen.
        Navigator(FinanceScreen()) { nav -> SlideTransition(nav) }
    }
    override val options: TabOptions
        @Composable get() {
            val s = LocalStrings.current
            return remember(s) { TabOptions(index = 4u, title = s.tabFinance) }
        }
    override val tabIcon: ImageVector get() = Icons.Default.AccountBalance
}

object ProfileTab : AppTab {
    @Composable
    override fun Content() {
        // Nested navigator so Profile can push the Reminders management screen.
        Navigator(ProfileScreen()) { nav -> SlideTransition(nav) }
    }
    override val options: TabOptions
        @Composable get() {
            val s = LocalStrings.current
            return remember(s) { TabOptions(index = 5u, title = s.tabProfile) }
        }
    override val tabIcon: ImageVector get() = Icons.Default.Person
}
