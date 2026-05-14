package tj.mahram.lifetrack.feature.settings

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import tj.mahram.lifetrack.domain.model.AppLanguage
import tj.mahram.lifetrack.domain.model.AppTheme
import tj.mahram.lifetrack.domain.repository.SettingsRepository

class SettingsScreenModel(
    private val settingsRepository: SettingsRepository
) : ScreenModel {

    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state.asStateFlow()

    init {
        screenModelScope.launch {
            settingsRepository.getSettings().collectLatest { settings ->
                _state.update { it.copy(isLoading = false, settings = settings) }
            }
        }
    }

    fun handleIntent(intent: SettingsIntent) {
        screenModelScope.launch {
            when (intent) {
                is SettingsIntent.SetTheme -> settingsRepository.setTheme(intent.theme)
                is SettingsIntent.SetCurrency -> settingsRepository.setCurrency(intent.currency)
                is SettingsIntent.SetLanguage -> settingsRepository.setLanguage(intent.language)
                is SettingsIntent.SetNotifications -> settingsRepository.setNotificationsEnabled(intent.enabled)
                is SettingsIntent.SetTaskNotifications -> settingsRepository.setTaskNotificationsEnabled(intent.enabled)
                is SettingsIntent.SetFinanceNotifications -> settingsRepository.setFinanceNotificationsEnabled(intent.enabled)
                is SettingsIntent.SetCryptoNotifications -> settingsRepository.setCryptoNotificationsEnabled(intent.enabled)
            }
        }
    }
}
