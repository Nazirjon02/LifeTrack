package tj.mahram.lifetrack.data.repository

import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.Json
import tj.mahram.lifetrack.domain.model.ReminderSchedule
import tj.mahram.lifetrack.domain.repository.ReminderRepository

/**
 * Stores the reminder list as a JSON blob in multiplatform-settings. This keeps
 * a single, serialisable source of truth the platform scheduler can also read
 * back after a reboot without touching the SQLDelight database.
 */
class ReminderRepositoryImpl(private val settings: Settings) : ReminderRepository {

    private val json = Json { ignoreUnknownKeys = true }
    private val _flow = MutableStateFlow(load())

    override fun getReminders(): Flow<List<ReminderSchedule>> = _flow.asStateFlow()

    override fun current(): List<ReminderSchedule> = _flow.value

    override suspend fun upsert(reminder: ReminderSchedule) {
        val updated = _flow.value.filter { it.id != reminder.id } + reminder
        persist(updated.sortedWith(compareBy({ it.hour }, { it.minute })))
    }

    override suspend fun delete(id: String) {
        persist(_flow.value.filter { it.id != id })
    }

    override suspend fun setEnabled(id: String, enabled: Boolean) {
        persist(_flow.value.map { if (it.id == id) it.copy(enabled = enabled) else it })
    }

    private fun persist(list: List<ReminderSchedule>) {
        settings.putString(KEY, json.encodeToString(list))
        _flow.value = list
    }

    private fun load(): List<ReminderSchedule> {
        val raw = settings.getStringOrNull(KEY) ?: return emptyList()
        return runCatching { json.decodeFromString<List<ReminderSchedule>>(raw) }.getOrDefault(emptyList())
    }

    companion object {
        private const val KEY = "reminders_json"
    }
}
