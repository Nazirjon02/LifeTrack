package tj.mahram.lifetrack.core.notifications

import android.app.NotificationManager
import android.content.Context
import tj.mahram.lifetrack.domain.model.ReminderSchedule

actual class NotificationScheduler(private val context: Context) {

    actual fun sync(reminders: List<ReminderSchedule>) {
        ReminderScheduling.sync(context, reminders)
    }

    actual fun hasPermission(): Boolean {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        return nm?.areNotificationsEnabled() ?: true
    }
}
