package com.example.volunteerhub.data

import com.example.volunteerhub.model.Cause
import com.example.volunteerhub.model.Dates
import com.example.volunteerhub.model.VolunteerEvent

enum class SortMode(val label: String) {
    BEST_MATCH("Best match"),
    SOONEST("Soonest"),
    SHORTEST("Shortest")
}

data class EventFilter(
    val query: String = "",
    val cause: Cause? = null,
    val sort: SortMode = SortMode.BEST_MATCH,
    val maxHours: Double? = null,
    val thisWeekOnly: Boolean = false
) {
    val activeCount: Int
        get() = listOfNotNull(
            cause,
            maxHours,
            if (thisWeekOnly) true else null,
            if (sort != SortMode.BEST_MATCH) sort else null
        ).size
}

/** Single place where events are searched, filtered and ordered. */
object EventRanker {
    fun isMatch(event: VolunteerEvent, interests: Set<Cause>): Boolean =
        interests.isNotEmpty() && event.cause in interests

    fun apply(events: List<VolunteerEvent>, interests: Set<Cause>, filter: EventFilter): List<VolunteerEvent> {
        val q = filter.query.trim().lowercase()
        val filtered = events.filter { e ->
            (q.isEmpty() || "${e.title} ${e.organizationName} ${e.location} ${e.description}".lowercase().contains(q)) &&
                (filter.cause == null || e.cause == filter.cause) &&
                (filter.maxHours == null || e.durationHours <= filter.maxHours) &&
                (!filter.thisWeekOnly || (Dates.daysUntil(e.eventDate) ?: 99) in 0..6)
        }
        // Undated events sink to the bottom of date-based sorts.
        val dateKey = { e: VolunteerEvent -> Dates.daysUntil(e.eventDate) ?: Int.MAX_VALUE }
        return when (filter.sort) {
            SortMode.BEST_MATCH -> filtered.sortedWith(
                compareByDescending<VolunteerEvent> { isMatch(it, interests) }.thenBy(dateKey)
            )
            SortMode.SOONEST -> filtered.sortedBy(dateKey)
            SortMode.SHORTEST -> filtered.sortedBy { it.durationHours }
        }
    }
}
