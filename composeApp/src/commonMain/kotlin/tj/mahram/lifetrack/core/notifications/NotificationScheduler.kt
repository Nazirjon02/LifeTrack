package tj.mahram.lifetrack.core.notifications

import tj.mahram.lifetrack.domain.model.ReminderSchedule

/**
 * Platform bridge that arms/cancels OS-level daily reminders. The Android
 * implementation uses AlarmManager and re-schedules itself after a reboot;
 * desktop/iOS are currently no-ops. Created at each platform entry point (like
 * [tj.mahram.lifetrack.data.local.DatabaseDriverFactory]) and injected via Koin.
 */
expect class NotificationScheduler {
    /** Cancel every previously-armed reminder and re-arm the enabled ones. */
    fun sync(reminders: List<ReminderSchedule>)

    /** Whether the OS currently allows this app to post notifications. */
    fun hasPermission(): Boolean
}
