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
import cafe.adriel.voyager.core.lifecycle.LocalNavigatorScreenLifecycleProvider
import cafe.adriel.voyager.core.lifecycle.NavigatorScreenLifecycleProvider
import cafe.adriel.voyager.core.lifecycle.ScreenLifecycleContentProvider
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.tab.*
import cafe.adriel.voyager.transitions.SlideTransition
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import tj.mahram.lifetrack.data.sync.SyncEngine
import tj.mahram.lifetrack.domain.model.AuthState
import tj.mahram.lifetrack.domain.repository.AuthRepository
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
import tj.mahram.lifetrack.domain.repository.ReminderRepository
import tj.mahram.lifetrack.domain.usecase.app.InitializeAppDataUseCase
import tj.mahram.lifetrack.domain.repository.SettingsRepository
import tj.mahram.lifetrack.feature.dashboard.DashboardScreen
import tj.mahram.lifetrack.feature.finance.FinanceScreen
import tj.mahram.lifetrack.feature.goals.GoalsScreen
import tj.mahram.lifetrack.feature.problems.ProblemsScreen
import tj.mahram.lifetrack.feature.planner.PlannerScreen
import tj.mahram.lifetrack.feature.profile.ProfileScreen
import tj.mahram.lifetrack.ui.theme.LifeTrackTheme

/** How often to run a background sync cycle while signed in. */
private const val SYNC_INTERVAL_MS = 20_000L

private val DefaultSettings = AppSettings(
    theme = AppTheme.DARK,
    currency = "USD",
    language = AppLanguage.ENGLISH,
    notificationsEnabled = true,
    taskNotificationsEnabled = true,
    financeNotificationsEnabled = true,
    cryptoNotificationsEnabled = true
)

/**
 * Voyager 1.0.0 gives every screen an `AndroidScreenLifecycleOwner` and destroys it the moment
 * the screen is popped — while [SlideTransition] is still animating it out. When the animation
 * ends, Voyager moves that DESTROYED lifecycle back to STARTED/CREATED, which androidx.lifecycle
 * 2.10 rejects ("State is 'DESTROYED' and cannot be moved to STARTED"), so every back navigation
 * from a nested screen (Wallets, Debts, Investments, Reminders, Sync…) crashed the Android app.
 *
 * LifeTrack keeps screen state in Voyager ScreenModels (disposed on pop independently of this
 * owner) and uses no screen-scoped AndroidX ViewModels or lifecycles, so we opt out of the
 * per-screen owner. Screens then share the Activity's lifecycle — exactly what Voyager already
 * does on Desktop and iOS, whose default provider is also empty.
 */
private object NoScreenLifecycleOwners : NavigatorScreenLifecycleProvider {
    override fun provide(screen: Screen): List<ScreenLifecycleContentProvider> = emptyList()
}

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
        val authRepo = remember { koin.get<AuthRepository>() }
        val authState by authRepo.state.collectAsState()

        LaunchedEffect(Unit) {
            // Default categories every launch; sample tasks only on a fresh install.
            runCatching { koin.get<InitializeAppDataUseCase>()() }
        }

        // The single place that arms OS reminder alarms: on launch (covers
        // schedules the OS may have cleared) and whenever the reminder list or
        // the "Enable notifications" master switch changes — off arms nothing.
        LaunchedEffect(Unit) {
            val scheduler = koin.get<NotificationScheduler>()
            combine(
                koin.get<ReminderRepository>().getReminders(),
                settingsRepo.getSettings().map { it.notificationsEnabled }.distinctUntilChanged()
            ) { reminders, enabled -> if (enabled) reminders else emptyList() }
                .collect { runCatching { scheduler.sync(it) } }
        }

        // Cloud sync: while signed in, sync on launch and then on an interval.
        // Local writes mark rows dirty, so the next tick pushes them up and
        // pulls remote changes down (offline-first — errors are ignored).
        LaunchedEffect(authState) {
            if (authState is AuthState.SignedIn) {
                val engine = koin.get<SyncEngine>()
                // Run the whole loop off the main thread to avoid UI jank.
                withContext(Dispatchers.Default) {
                    while (isActive) {
                        runCatching { engine.syncAll() }
                        delay(SYNC_INTERVAL_MS)
                    }
                }
            }
        }

        CompositionLocalProvider(
            LocalStrings provides stringsFor(settings.language),
            // Must wrap every Navigator (tabs + nested) — see NoScreenLifecycleOwners.
            LocalNavigatorScreenLifecycleProvider provides NoScreenLifecycleOwners
        ) {
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
