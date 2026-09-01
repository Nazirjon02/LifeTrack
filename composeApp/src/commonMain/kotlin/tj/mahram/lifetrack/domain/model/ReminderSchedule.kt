package tj.mahram.lifetrack.domain.model

import kotlinx.serialization.Serializable

enum class ReminderFrequency { DAILY, WEEKDAYS, WEEKENDS, CUSTOM }

/**
 * A single daily push reminder the user configured. Persisted (as JSON) so it
 * survives process death and device reboots, then handed to the platform
 * [tj.mahram.lifetrack.core.notifications.NotificationScheduler] to arm alarms.
 *
 * [daysOfWeek] uses ISO numbering (1 = Monday … 7 = Sunday) and only matters for
 * [ReminderFrequency.CUSTOM]. Several reminders at different times give the user
 * multiple notifications per day.
 */
@Serializable
data class ReminderSchedule(
    val id: String,
    val message: String,
    val hour: Int,
    val minute: Int,
    val frequency: ReminderFrequency,
    val daysOfWeek: Set<Int> = emptySet(),
    val enabled: Boolean = true
) {
    /** Whether this reminder should fire on the given ISO day-of-week (1=Mon..7=Sun). */
    fun firesOn(isoDayOfWeek: Int): Boolean = when (frequency) {
        ReminderFrequency.DAILY    -> true
        ReminderFrequency.WEEKDAYS -> isoDayOfWeek in 1..5
        ReminderFrequency.WEEKENDS -> isoDayOfWeek in 6..7
        ReminderFrequency.CUSTOM   -> isoDayOfWeek in daysOfWeek
    }

    val timeLabel: String
        get() = "${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}"
}
