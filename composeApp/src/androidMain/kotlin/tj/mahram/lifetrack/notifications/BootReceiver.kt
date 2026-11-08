package tj.mahram.lifetrack.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import tj.mahram.lifetrack.core.notifications.ReminderScheduling

/**
 * Re-arms all reminder alarms after the device reboots (or the app is updated).
 * AlarmManager alarms do not survive a reboot, so without this reminders would
 * silently stop. Also re-arms after a clock / time-zone change (alarms are set
 * for an absolute moment computed in the old zone) and when the exact-alarm
 * permission changes. Reads the persisted mirror written by [ReminderScheduling].
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED,
            "android.intent.action.LOCKED_BOOT_COMPLETED",
            Intent.ACTION_MY_PACKAGE_REPLACED,
            "android.intent.action.QUICKBOOT_POWERON",
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED,
            "android.app.action.SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED" -> {
                ReminderScheduling.rescheduleFromMirror(context)
            }
        }
    }
}
