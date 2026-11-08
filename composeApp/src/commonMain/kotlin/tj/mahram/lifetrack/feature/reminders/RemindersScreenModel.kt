package tj.mahram.lifetrack.feature.reminders

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import tj.mahram.lifetrack.core.notifications.NotificationScheduler
import tj.mahram.lifetrack.domain.model.ReminderSchedule
import tj.mahram.lifetrack.domain.repository.ReminderRepository
import tj.mahram.lifetrack.domain.repository.SettingsRepository

data class RemindersState(
    val reminders: List<ReminderSchedule> = emptyList(),
    val hasPermission: Boolean = true,
    /** The Profile "Enable notifications" master switch; off = no reminder fires. */
    val notificationsEnabled: Boolean = true,
    val showSheet: Boolean = false,
    val editing: ReminderSchedule? = null
)

sealed class RemindersIntent {
    data object ShowAdd : RemindersIntent()
    data class ShowEdit(val reminder: ReminderSchedule) : RemindersIntent()
    data object HideSheet : RemindersIntent()
    data class Save(val reminder: ReminderSchedule) : RemindersIntent()
    data class Delete(val id: String) : RemindersIntent()
    data class ToggleEnabled(val id: String, val enabled: Boolean) : RemindersIntent()
}

class RemindersScreenModel(
    private val reminderRepository: ReminderRepository,
    private val notificationScheduler: NotificationScheduler,
    private val settingsRepository: SettingsRepository
) : ScreenModel {

    private val _state = MutableStateFlow(RemindersState(hasPermission = notificationScheduler.hasPermission()))
    val state: StateFlow<RemindersState> = _state.asStateFlow()

    init {
        // OS alarms are re-armed centrally in App.kt (it also honours the master
        // switch); this screen only shows the list.
        screenModelScope.launch {
            reminderRepository.getReminders().collect { reminders ->
                _state.update { it.copy(reminders = reminders, hasPermission = notificationScheduler.hasPermission()) }
            }
        }
        screenModelScope.launch {
            settingsRepository.getSettings().collect { settings ->
                _state.update { it.copy(notificationsEnabled = settings.notificationsEnabled) }
            }
        }
    }

    fun handleIntent(intent: RemindersIntent) {
        when (intent) {
            RemindersIntent.ShowAdd -> _state.update { it.copy(showSheet = true, editing = null) }
            is RemindersIntent.ShowEdit -> _state.update { it.copy(showSheet = true, editing = intent.reminder) }
            RemindersIntent.HideSheet -> _state.update { it.copy(showSheet = false, editing = null) }
            is RemindersIntent.Save -> screenModelScope.launch {
                reminderRepository.upsert(intent.reminder)
                _state.update { it.copy(showSheet = false, editing = null) }
            }
            is RemindersIntent.Delete -> screenModelScope.launch { reminderRepository.delete(intent.id) }
            is RemindersIntent.ToggleEnabled -> screenModelScope.launch {
                reminderRepository.setEnabled(intent.id, intent.enabled)
            }
        }
    }
}
