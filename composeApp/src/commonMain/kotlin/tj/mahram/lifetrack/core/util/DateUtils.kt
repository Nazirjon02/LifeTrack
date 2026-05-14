package tj.mahram.lifetrack.core.util

import kotlin.time.Clock
import kotlinx.datetime.*

fun Long.toInstant(): Instant = Instant.fromEpochMilliseconds(this)

fun Instant.toLocalDate(timeZone: TimeZone = TimeZone.currentSystemDefault()): LocalDate {
    val dt = this.toLocalDateTime(timeZone)
    return LocalDate(dt.year, dt.monthNumber, dt.dayOfMonth)
}

fun Instant.formatDate(timeZone: TimeZone = TimeZone.currentSystemDefault()): String {
    val dt = this.toLocalDateTime(timeZone)
    return "${dt.dayOfMonth.toString().padStart(2,'0')}/${dt.monthNumber.toString().padStart(2,'0')}/${dt.year}"
}

fun Instant.formatDateTime(timeZone: TimeZone = TimeZone.currentSystemDefault()): String {
    val dt = this.toLocalDateTime(timeZone)
    val hour = dt.hour.toString().padStart(2, '0')
    val min = dt.minute.toString().padStart(2, '0')
    return "${dt.dayOfMonth.toString().padStart(2,'0')}/${dt.monthNumber.toString().padStart(2,'0')}/${dt.year} $hour:$min"
}

fun Instant.formatTime(timeZone: TimeZone = TimeZone.currentSystemDefault()): String {
    val dt = this.toLocalDateTime(timeZone)
    return "${dt.hour.toString().padStart(2,'0')}:${dt.minute.toString().padStart(2,'0')}"
}

fun Instant.formatRelative(): String {
    val now = Clock.System.now()
    val seconds: Long = now.epochSeconds - this.epochSeconds
    return when {
        seconds < 60L -> "Just now"
        seconds < 3600L -> "${seconds / 60L}m ago"
        seconds < 86400L -> "${seconds / 3600L}h ago"
        seconds < 604800L -> "${seconds / 86400L}d ago"
        else -> formatDate()
    }
}

fun startOfDay(timeZone: TimeZone = TimeZone.currentSystemDefault()): Instant {
    val now = Clock.System.now()
    val dt = now.toLocalDateTime(timeZone)
    val today = LocalDate(dt.year, dt.monthNumber, dt.dayOfMonth)
    return today.atStartOfDayIn(timeZone)
}

fun endOfDay(timeZone: TimeZone = TimeZone.currentSystemDefault()): Instant {
    val now = Clock.System.now()
    val dt = now.toLocalDateTime(timeZone)
    val today = LocalDate(dt.year, dt.monthNumber, dt.dayOfMonth)
    return today.plus(1, DateTimeUnit.DAY).atStartOfDayIn(timeZone).minus(1, DateTimeUnit.MILLISECOND)
}

fun startOfMonth(timeZone: TimeZone = TimeZone.currentSystemDefault()): Instant {
    val now = Clock.System.now()
    val dt = now.toLocalDateTime(timeZone)
    return LocalDate(dt.year, dt.monthNumber, 1).atStartOfDayIn(timeZone)
}

fun endOfMonth(timeZone: TimeZone = TimeZone.currentSystemDefault()): Instant {
    val now = Clock.System.now()
    val dt = now.toLocalDateTime(timeZone)
    val firstOfNext = LocalDate(dt.year, dt.monthNumber, 1)
        .plus(1, DateTimeUnit.MONTH)
    return firstOfNext.atStartOfDayIn(timeZone).minus(1, DateTimeUnit.MILLISECOND)
}

fun Instant.isToday(timeZone: TimeZone = TimeZone.currentSystemDefault()): Boolean {
    val now = Clock.System.now()
    val dtNow = now.toLocalDateTime(timeZone)
    val today = LocalDate(dtNow.year, dtNow.monthNumber, dtNow.dayOfMonth)
    val dtThis = this.toLocalDateTime(timeZone)
    return LocalDate(dtThis.year, dtThis.monthNumber, dtThis.dayOfMonth) == today
}

val MonthNames = listOf("Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec")

fun Instant.monthName(timeZone: TimeZone = TimeZone.currentSystemDefault()): String {
    val dt = this.toLocalDateTime(timeZone)
    return MonthNames[dt.monthNumber - 1]
}
