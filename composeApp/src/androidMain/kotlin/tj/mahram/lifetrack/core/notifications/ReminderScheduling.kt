package tj.mahram.lifetrack.core.notifications

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import kotlinx.serialization.json.Json
import tj.mahram.lifetrack.domain.model.ReminderSchedule
import tj.mahram.lifetrack.notifications.ReminderReceiver
import java.util.Calendar

/**
 * All the Android AlarmManager + notification plumbing lives here so both the
 * [NotificationScheduler] actual and the boot receiver share one code path.
 *
 * Each enabled reminder arms a one-shot alarm for its next occurrence; when it
 * fires, [ReminderReceiver] arms the following day's alarm and re-checks the
 * reminder's frequency (weekdays / weekends / custom days) before actually
 * posting, so one daily alarm covers every frequency. The reminder list is
 * mirrored into a private prefs file so
 * [tj.mahram.lifetrack.notifications.BootReceiver] can re-arm alarms after a
 * reboot or a clock change without opening the database.
 *
 * Timing: the alarm is exact when the app may schedule exact alarms (always on
 * Android 7–11; on 12+ with the "Alarms & reminders" permission), otherwise it
 * fires within a 10-minute window. The previous `setInexactRepeating` daily
 * alarm could legitimately be deferred by hours.
 */
object ReminderScheduling {

    const val CHANNEL_ID = "lifetrack_reminders"
    const val ACTION_FIRE = "tj.mahram.lifetrack.action.REMINDER"
    const val EXTRA_ID = "reminder_id"

    private const val PREFS = "lifetrack_reminders"
    private const val KEY = "json"
    private val json = Json { ignoreUnknownKeys = true }

    /** Cancel previously-armed alarms, persist the new list, and arm enabled ones. */
    fun sync(context: Context, reminders: List<ReminderSchedule>) {
        ensureChannel(context)
        loadMirror(context).forEach { cancel(context, it.id) }
        saveMirror(context, reminders)
        reminders.filter { it.enabled }.forEach { scheduleNext(context, it) }
    }

    /** Re-arm alarms from the persisted mirror — after a reboot or a clock change. */
    fun rescheduleFromMirror(context: Context) {
        ensureChannel(context)
        loadMirror(context).filter { it.enabled }.forEach { scheduleNext(context, it) }
    }

    fun loadMirror(context: Context): List<ReminderSchedule> {
        val raw = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY, null)
            ?: return emptyList()
        return runCatching { json.decodeFromString<List<ReminderSchedule>>(raw) }.getOrDefault(emptyList())
    }

    private fun saveMirror(context: Context, reminders: List<ReminderSchedule>) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putString(KEY, json.encodeToString(reminders))
            .apply()
    }

    /** Arm (or replace) the one-shot alarm for the reminder's next occurrence. */
    fun scheduleNext(context: Context, reminder: ReminderSchedule) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val trigger = nextTrigger(reminder.hour, reminder.minute)
        val pi = pendingIntent(context, reminder.id, mutableCreate = true) ?: return
        val exactAllowed = Build.VERSION.SDK_INT < Build.VERSION_CODES.S || am.canScheduleExactAlarms()
        try {
            if (exactAllowed) {
                am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, trigger, pi)
            } else {
                am.setWindow(AlarmManager.RTC_WAKEUP, trigger, INEXACT_WINDOW_MS, pi)
            }
        } catch (_: SecurityException) {
            // The exact-alarm permission was revoked between the check and the call.
            am.setWindow(AlarmManager.RTC_WAKEUP, trigger, INEXACT_WINDOW_MS, pi)
        }
    }

    /** Without exact-alarm access the reminder fires within this window. */
    private const val INEXACT_WINDOW_MS = 10 * 60 * 1000L

    private fun cancel(context: Context, id: String) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        pendingIntent(context, id, mutableCreate = false)?.let {
            am.cancel(it)
            it.cancel()
        }
    }

    private fun pendingIntent(context: Context, id: String, mutableCreate: Boolean): PendingIntent? {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            action = ACTION_FIRE
            putExtra(EXTRA_ID, id)
            // Unique data so distinct reminders map to distinct PendingIntents.
            data = Uri.parse("lifetrack://reminder/$id")
        }
        val base = if (mutableCreate) PendingIntent.FLAG_UPDATE_CURRENT else PendingIntent.FLAG_NO_CREATE
        val flags = base or PendingIntent.FLAG_IMMUTABLE
        return PendingIntent.getBroadcast(context, id.hashCode(), intent, flags)
    }

    private fun nextTrigger(hour: Int, minute: Int): Long {
        val now = Calendar.getInstance()
        val next = (now.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        if (next.timeInMillis <= now.timeInMillis) next.add(Calendar.DAY_OF_YEAR, 1)
        return next.timeInMillis
    }

    /** ISO day-of-week (1 = Monday … 7 = Sunday) for today. */
    fun isoDayOfWeekToday(): Int {
        val dow = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) // Sun=1 … Sat=7
        return ((dow + 5) % 7) + 1
    }

    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (nm.getNotificationChannel(CHANNEL_ID) == null) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    "Reminders",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply { description = "Daily reminders for your problems and goals" }
                nm.createNotificationChannel(channel)
            }
        }
    }
}
