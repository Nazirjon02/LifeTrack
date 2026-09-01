package tj.mahram.lifetrack.core.notifications

import tj.mahram.lifetrack.domain.model.ReminderSchedule

/**
 * iOS reminders would use UNUserNotificationCenter; left as a no-op for now so
 * the shared code compiles. (Not buildable on Windows regardless.)
 */
actual class NotificationScheduler {
    actual fun sync(reminders: List<ReminderSchedule>) {}
    actual fun hasPermission(): Boolean = true
}
