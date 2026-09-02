package tj.mahram.lifetrack.feature.sync

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import tj.mahram.lifetrack.data.sync.SyncEngine
import tj.mahram.lifetrack.data.sync.SyncStatus
import tj.mahram.lifetrack.domain.model.AppLanguage
import tj.mahram.lifetrack.domain.model.AuthState
import tj.mahram.lifetrack.domain.repository.AuthRepository
import tj.mahram.lifetrack.domain.repository.SettingsRepository

class SyncScreenModel(
    private val auth: AuthRepository,
    private val syncEngine: SyncEngine,
    private val settingsRepository: SettingsRepository
) : ScreenModel {

    data class UiState(
        val authState: AuthState = AuthState.SignedOut,
        val status: SyncStatus = SyncStatus(),
        val language: AppLanguage = AppLanguage.ENGLISH,
        val configured: Boolean = true,
        val busy: Boolean = false,
        val error: String? = null
    )

    private val _state = MutableStateFlow(UiState(configured = syncEngine.isConfigured))
    val state: StateFlow<UiState> = _state.asStateFlow()

    init {
        screenModelScope.launch { auth.state.collect { a -> _state.update { it.copy(authState = a) } } }
        screenModelScope.launch { syncEngine.status.collect { s -> _state.update { it.copy(status = s) } } }
        screenModelScope.launch {
            settingsRepository.getSettings().collect { s -> _state.update { it.copy(language = s.language) } }
        }
    }

    fun signIn(email: String, password: String) = authenticate(email, password, signUp = false)
    fun signUp(email: String, password: String) = authenticate(email, password, signUp = true)

    private fun authenticate(email: String, password: String, signUp: Boolean) {
        if (email.isBlank() || password.isBlank()) {
            _state.update { it.copy(error = EMPTY_FIELDS) }
            return
        }
        screenModelScope.launch {
            _state.update { it.copy(busy = true, error = null) }
            val result = if (signUp) auth.signUp(email, password) else auth.signIn(email, password)
            _state.update { it.copy(busy = false, error = result.exceptionOrNull()?.message) }
            if (result.isSuccess) syncEngine.syncAll()
        }
    }

    fun signOut() {
        screenModelScope.launch { auth.signOut() }
    }

    fun syncNow() {
        screenModelScope.launch { syncEngine.syncAll() }
    }

    fun clearError() = _state.update { it.copy(error = null) }

    companion object {
        /** Sentinel so the UI can localize the "fill in the fields" message. */
        const val EMPTY_FIELDS = "__empty_fields__"
    }
}
