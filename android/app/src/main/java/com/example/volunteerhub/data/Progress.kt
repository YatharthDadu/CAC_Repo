package com.example.volunteerhub.data

import com.example.volunteerhub.model.Badge
import com.example.volunteerhub.model.Dates
import com.example.volunteerhub.model.ShiftRecord
import com.example.volunteerhub.model.UserProfile
import com.example.volunteerhub.model.VolunteerEvent

/** Derived numbers shown on Home and Profile. Pure functions, so they are easy to unit test. */
data class Progress(
    val verifiedHours: Double,
    val plannedHours: Double,
    val goalHours: Double,
    val shiftsThisWeek: Int,
    val weeklyGoal: Int,
    val badges: Set<Badge>
) {
    val verifiedFraction: Float
        get() = if (goalHours <= 0) 0f else (verifiedHours / goalHours).coerceIn(0.0, 1.0).toFloat()
    val plannedFraction: Float
        get() = if (goalHours <= 0) 0f else ((verifiedHours + plannedHours) / goalHours).coerceIn(0.0, 1.0).toFloat()
    val hoursToGo: Double get() = (goalHours - verifiedHours).coerceAtLeast(0.0)
    val goalReached: Boolean get() = goalHours > 0 && verifiedHours >= goalHours
}

object ProgressCalculator {
    fun compute(
        profile: UserProfile,
        shifts: List<ShiftRecord>,
        plannedEvents: List<VolunteerEvent>,
        nowMillis: Long = System.currentTimeMillis()
    ): Progress {
        val verifiedIds = shifts.map { it.eventId }.toSet()
        val stillPlanned = plannedEvents.filter { it.id !in verifiedIds }
        val verified = shifts.sumOf { it.hours }
        val planned = stillPlanned.sumOf { it.durationHours }
        val weekStart = Dates.startOfWeekMillis(nowMillis)
        val thisWeek = shifts.count { it.verifiedAtMillis >= weekStart }

        val causes = (shifts.map { it.cause } + stillPlanned.map { it.cause }).toSet()
        val badges = buildSet {
            if (plannedEvents.isNotEmpty() || shifts.isNotEmpty()) add(Badge.FIRST_PLAN)
            if (shifts.isNotEmpty()) add(Badge.FIRST_SHIFT)
            if (causes.size >= 3) add(Badge.EXPLORER)
            if (verified >= 10.0) add(Badge.TEN_HOURS)
            if (profile.goalHours > 0 && verified >= profile.goalHours / 2) add(Badge.HALFWAY)
            if (profile.goalHours > 0 && verified >= profile.goalHours) add(Badge.GOAL)
        }
        return Progress(verified, planned, profile.goalHours, thisWeek, profile.weeklyGoal, badges)
    }
}
