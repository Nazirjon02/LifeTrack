package tj.mahram.lifetrack.domain.repository

import kotlinx.coroutines.flow.Flow
import tj.mahram.lifetrack.domain.model.ReminderSchedule

interface ReminderRepository {
    fun getReminders(): Flow<List<ReminderSchedule>>
    /** Snapshot of the current reminders (used to re-arm alarms on launch). */
    fun current(): List<ReminderSchedule>
    suspend fun upsert(reminder: ReminderSchedule)
    suspend fun delete(id: String)
    suspend fun setEnabled(id: String, enabled: Boolean)
}
