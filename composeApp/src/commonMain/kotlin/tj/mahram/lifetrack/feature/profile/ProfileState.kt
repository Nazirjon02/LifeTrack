package tj.mahram.lifetrack.feature.profile

import tj.mahram.lifetrack.domain.model.AppLanguage
import tj.mahram.lifetrack.domain.model.AppSettings
import tj.mahram.lifetrack.domain.model.AppTheme
import tj.mahram.lifetrack.domain.model.BalanceOverview

data class ProfileState(
    val isLoading: Boolean = true,
    val settings: AppSettings = AppSettings(
        theme = AppTheme.DARK,
        currency = "USD",
        language = AppLanguage.ENGLISH,
        notificationsEnabled = true,
        taskNotificationsEnabled = true,
        financeNotificationsEnabled = true,
        cryptoNotificationsEnabled = true
    ),
    val balance: BalanceOverview? = null,
    val tasksDone: Int = 0,
    val tasksTotal: Int = 0,
    val habitsCount: Int = 0,
    val goalsAchieved: Int = 0,
    val goalsTotal: Int = 0,
    val bestStreak: Int = 0
) {
    /** A gamified score aggregated from everything the user has accomplished. */
    val productivityScore: Int
        get() = tasksDone * 10 + habitsCount * 15 + goalsAchieved * 40 + bestStreak * 5

    /** Points needed to advance one level. */
    val pointsPerLevel: Int get() = 100

    /** Current level (starts at 1). */
    val level: Int
        get() = 1 + productivityScore / pointsPerLevel

    /** Progress 0..1 towards the next level. */
    val levelProgress: Float
        get() = (productivityScore % pointsPerLevel) / pointsPerLevel.toFloat()

    /** Points remaining until the next level. */
    val pointsToNextLevel: Int
        get() = pointsPerLevel - (productivityScore % pointsPerLevel)
}

sealed class ProfileIntent {
    data class SetTheme(val theme: AppTheme) : ProfileIntent()
    data class SetCurrency(val currency: String) : ProfileIntent()
    data class SetLanguage(val language: AppLanguage) : ProfileIntent()
    data class SetNotifications(val enabled: Boolean) : ProfileIntent()
    data class SetTaskNotifications(val enabled: Boolean) : ProfileIntent()
    data class SetFinanceNotifications(val enabled: Boolean) : ProfileIntent()
    data class SetDisplayName(val name: String) : ProfileIntent()
    data class SetAvatar(val emoji: String) : ProfileIntent()
}
