package tj.mahram.lifetrack.notifications

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import tj.mahram.lifetrack.MainActivity
import tj.mahram.lifetrack.core.notifications.ReminderScheduling

/**
 * Fires when an armed daily alarm goes off. Re-checks the reminder's frequency
 * against today before posting, so a single daily alarm serves DAILY / WEEKDAYS
 * / WEEKENDS / CUSTOM schedules.
 */
class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getStringExtra(ReminderScheduling.EXTRA_ID) ?: return
        val reminder = ReminderScheduling.loadMirror(context).firstOrNull { it.id == id } ?: return
        if (!reminder.enabled) return
        if (!reminder.firesOn(ReminderScheduling.isoDayOfWeekToday())) return

        ReminderScheduling.ensureChannel(context)
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return

        val contentIntent = PendingIntent.getActivity(
            context,
            id.hashCode(),
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = Notification.Builder(context, ReminderScheduling.CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle("LifeTracker")
            .setContentText(reminder.message.ifBlank { "Time to review your problems and goals 💪" })
            .setAutoCancel(true)
            .setContentIntent(contentIntent)
            .build()

        runCatching { nm.notify(id.hashCode(), notification) }
    }
}
