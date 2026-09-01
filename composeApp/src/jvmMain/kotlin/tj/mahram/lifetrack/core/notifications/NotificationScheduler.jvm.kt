package tj.mahram.lifetrack.core.notifications

import tj.mahram.lifetrack.domain.model.ReminderSchedule

/** Desktop has no background alarm story; reminders are a no-op here. */
actual class NotificationScheduler {
    actual fun sync(reminders: List<ReminderSchedule>) {}
    actual fun hasPermission(): Boolean = true
}
