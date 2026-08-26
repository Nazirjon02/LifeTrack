package tj.mahram.lifetrack.data.repository

import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import tj.mahram.lifetrack.domain.model.AppLanguage
import tj.mahram.lifetrack.domain.model.AppSettings
import tj.mahram.lifetrack.domain.model.AppTheme
import tj.mahram.lifetrack.domain.repository.SettingsRepository

class SettingsRepositoryImpl(private val settings: Settings) : SettingsRepository {

    companion object {
        private const val KEY_THEME = "theme"
        private const val KEY_CURRENCY = "currency"
        private const val KEY_LANGUAGE = "language"
        private const val KEY_NOTIFICATIONS = "notifications"
        private const val KEY_TASK_NOTIF = "task_notifications"
        private const val KEY_FINANCE_NOTIF = "finance_notifications"
        private const val KEY_CRYPTO_NOTIF = "crypto_notifications"
        private const val KEY_FIRST_LAUNCH = "first_launch"
        private const val KEY_OPENING_BALANCE = "opening_balance"
        private const val KEY_DISPLAY_NAME = "display_name"
        private const val KEY_AVATAR_EMOJI = "avatar_emoji"
    }

    private val _settingsFlow = MutableStateFlow(loadCurrent())

    override fun getSettings(): Flow<AppSettings> = _settingsFlow.asStateFlow()

    private fun loadCurrent(): AppSettings = AppSettings(
        theme = settings.getStringOrNull(KEY_THEME)?.let {
            runCatching { AppTheme.valueOf(it) }.getOrDefault(AppTheme.DARK)
        } ?: AppTheme.DARK,
        currency = settings.getStringOrNull(KEY_CURRENCY) ?: "USD",
        language = settings.getStringOrNull(KEY_LANGUAGE)?.let {
            runCatching { AppLanguage.valueOf(it) }.getOrDefault(AppLanguage.ENGLISH)
        } ?: AppLanguage.ENGLISH,
        notificationsEnabled = settings.getBoolean(KEY_NOTIFICATIONS, true),
        taskNotificationsEnabled = settings.getBoolean(KEY_TASK_NOTIF, true),
        financeNotificationsEnabled = settings.getBoolean(KEY_FINANCE_NOTIF, true),
        cryptoNotificationsEnabled = settings.getBoolean(KEY_CRYPTO_NOTIF, true),
        openingBalance = settings.getDouble(KEY_OPENING_BALANCE, 0.0),
        displayName = settings.getStringOrNull(KEY_DISPLAY_NAME) ?: "",
        avatarEmoji = settings.getStringOrNull(KEY_AVATAR_EMOJI) ?: "🚀"
    )

    private fun refresh() {
        _settingsFlow.value = loadCurrent()
    }

    override suspend fun setTheme(theme: AppTheme) {
        settings.putString(KEY_THEME, theme.name)
        refresh()
    }

    override suspend fun setCurrency(currency: String) {
        settings.putString(KEY_CURRENCY, currency)
        refresh()
    }

    override suspend fun setOpeningBalance(amount: Double) {
        settings.putDouble(KEY_OPENING_BALANCE, amount)
        refresh()
    }

    override suspend fun setLanguage(language: AppLanguage) {
        settings.putString(KEY_LANGUAGE, language.name)
        refresh()
    }

    override suspend fun setNotificationsEnabled(enabled: Boolean) {
        settings.putBoolean(KEY_NOTIFICATIONS, enabled)
        refresh()
    }

    override suspend fun setTaskNotificationsEnabled(enabled: Boolean) {
        settings.putBoolean(KEY_TASK_NOTIF, enabled)
        refresh()
    }

    override suspend fun setFinanceNotificationsEnabled(enabled: Boolean) {
        settings.putBoolean(KEY_FINANCE_NOTIF, enabled)
        refresh()
    }

    override suspend fun setCryptoNotificationsEnabled(enabled: Boolean) {
        settings.putBoolean(KEY_CRYPTO_NOTIF, enabled)
        refresh()
    }

    override suspend fun setDisplayName(name: String) {
        settings.putString(KEY_DISPLAY_NAME, name)
        refresh()
    }

    override suspend fun setAvatarEmoji(emoji: String) {
        settings.putString(KEY_AVATAR_EMOJI, emoji)
        refresh()
    }

    override suspend fun isFirstLaunch(): Boolean =
        settings.getBoolean(KEY_FIRST_LAUNCH, true)

    override suspend fun setFirstLaunchDone() {
        settings.putBoolean(KEY_FIRST_LAUNCH, false)
    }
}
