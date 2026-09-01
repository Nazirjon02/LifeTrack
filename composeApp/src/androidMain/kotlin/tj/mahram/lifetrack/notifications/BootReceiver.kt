package tj.mahram.lifetrack.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import tj.mahram.lifetrack.core.notifications.ReminderScheduling

/**
 * Re-arms all reminder alarms after the device reboots (or the app is updated).
 * AlarmManager alarms do not survive a reboot, so without this reminders would
 * silently stop. Reads the persisted mirror written by [ReminderScheduling].
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED,
            "android.intent.action.LOCKED_BOOT_COMPLETED",
            Intent.ACTION_MY_PACKAGE_REPLACED,
            "android.intent.action.QUICKBOOT_POWERON" -> {
                ReminderScheduling.rescheduleFromMirror(context)
            }
        }
    }
}
