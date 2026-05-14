package tj.mahram.lifetrack.feature.settings

import tj.mahram.lifetrack.domain.model.AppLanguage
import tj.mahram.lifetrack.domain.model.AppSettings
import tj.mahram.lifetrack.domain.model.AppTheme

data class SettingsState(
    val isLoading: Boolean = true,
    val settings: AppSettings = AppSettings(
        theme = AppTheme.DARK,
        currency = "USD",
        language = AppLanguage.ENGLISH,
        notificationsEnabled = true,
        taskNotificationsEnabled = true,
        financeNotificationsEnabled = true,
        cryptoNotificationsEnabled = true
    )
)

sealed class SettingsIntent {
    data class SetTheme(val theme: AppTheme) : SettingsIntent()
    data class SetCurrency(val currency: String) : SettingsIntent()
    data class SetLanguage(val language: AppLanguage) : SettingsIntent()
    data class SetNotifications(val enabled: Boolean) : SettingsIntent()
    data class SetTaskNotifications(val enabled: Boolean) : SettingsIntent()
    data class SetFinanceNotifications(val enabled: Boolean) : SettingsIntent()
    data class SetCryptoNotifications(val enabled: Boolean) : SettingsIntent()
}
