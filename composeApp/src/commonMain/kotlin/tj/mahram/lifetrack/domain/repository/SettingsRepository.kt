package tj.mahram.lifetrack.domain.repository

import kotlinx.coroutines.flow.Flow
import tj.mahram.lifetrack.domain.model.AppLanguage
import tj.mahram.lifetrack.domain.model.AppSettings
import tj.mahram.lifetrack.domain.model.AppTheme

interface SettingsRepository {
    fun getSettings(): Flow<AppSettings>
    suspend fun setTheme(theme: AppTheme)
    suspend fun setCurrency(currency: String)
    suspend fun setOpeningBalance(amount: Double)
    suspend fun setLanguage(language: AppLanguage)
    suspend fun setNotificationsEnabled(enabled: Boolean)
    suspend fun setTaskNotificationsEnabled(enabled: Boolean)
    suspend fun setFinanceNotificationsEnabled(enabled: Boolean)
    suspend fun setCryptoNotificationsEnabled(enabled: Boolean)
    suspend fun setDisplayName(name: String)
    suspend fun setAvatarEmoji(emoji: String)
    suspend fun isFirstLaunch(): Boolean
    suspend fun setFirstLaunchDone()
}
