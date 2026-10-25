package com.example.volunteerhub.model

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/** Small date helpers that work on API 24 without desugaring. */
object Dates {
    private fun parser() = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply { isLenient = false }

    fun parse(iso: String): Date? = try {
        if (iso.length >= 10) parser().parse(iso.substring(0, 10)) else null
    } catch (e: Exception) {
        null
    }

    fun iso(date: Date): String = parser().format(date)

    fun todayIso(): String = iso(Date())

    fun inDaysIso(days: Int): String {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, days)
        return iso(cal.time)
    }

    /** "Sat, Oct 25" or "Date to be confirmed". */
    fun pretty(iso: String): String {
        val d = parse(iso) ?: return "Date to be confirmed"
        return SimpleDateFormat("EEE, MMM d", Locale.US).format(d)
    }

    /** Whole days from today until [iso]; null when unknown. Negative when past. */
    fun daysUntil(iso: String): Int? {
        val d = parse(iso) ?: return null
        val today = parse(todayIso()) ?: return null
        return ((d.time - today.time) / 86_400_000L).toInt()
    }

    fun relative(iso: String): String {
        val n = daysUntil(iso) ?: return "Date TBC"
        return when {
            n < -1 -> "${-n} days ago"
            n == -1 -> "Yesterday"
            n == 0 -> "Today"
            n == 1 -> "Tomorrow"
            n < 7 -> "In $n days"
            else -> pretty(iso)
        }
    }

    /** Start of the current week (Monday 00:00) in millis. */
    fun startOfWeekMillis(now: Long = System.currentTimeMillis()): Long {
        val cal = Calendar.getInstance()
        cal.firstDayOfWeek = Calendar.MONDAY
        cal.timeInMillis = now
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val diff = (cal.get(Calendar.DAY_OF_WEEK) + 5) % 7 // Monday = 0
        cal.add(Calendar.DAY_OF_YEAR, -diff)
        return cal.timeInMillis
    }
}
