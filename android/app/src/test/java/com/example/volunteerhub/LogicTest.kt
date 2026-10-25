package com.example.volunteerhub

import com.example.volunteerhub.data.EventFilter
import com.example.volunteerhub.data.EventRanker
import com.example.volunteerhub.data.ProgressCalculator
import com.example.volunteerhub.data.SortMode
import com.example.volunteerhub.model.Badge
import com.example.volunteerhub.model.Cause
import com.example.volunteerhub.model.Dates
import com.example.volunteerhub.model.ShiftRecord
import com.example.volunteerhub.model.UserProfile
import com.example.volunteerhub.model.VolunteerEvent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LogicTest {
    private fun event(id: String, cat: String, inDays: Int, hours: Double = 3.0, title: String = id) =
        VolunteerEvent(
            id = id, title = title, organizationName = "Org", location = "Frisco, TX",
            eventDate = Dates.inDaysIso(inDays), category = cat, durationHours = hours
        )

    private fun shift(e: VolunteerEvent, at: Long = System.currentTimeMillis()) = ShiftRecord(
        e.id, e.title, e.organizationName, e.cause, e.eventDate, e.durationHours, "Sam", "sam@x.org", at
    )

    @Test fun causeMapsLegacyAndNewCategories() {
        assertEquals(Cause.TECH, Cause.from("Computer Science & Tech"))
        assertEquals(Cause.HEALTH, Cause.from("Pre-Med / Healthcare"))
        assertEquals(Cause.NATURE, Cause.from("Environmental Science"))
        assertEquals(Cause.LEARNING, Cause.from("Education & Literacy"))
        assertEquals(Cause.COMMUNITY, Cause.from("Community & Civic"))
        assertEquals(Cause.BUSINESS, Cause.from("Business & Finance"))
        assertEquals(Cause.NATURE, Cause.from("", "Trail cleanup and tree planting"))
        assertEquals(Cause.COMMUNITY, Cause.from("", "something else entirely"))
    }

    @Test fun datesParseAndCountDays() {
        assertEquals(0, Dates.daysUntil(Dates.todayIso()))
        assertEquals(5, Dates.daysUntil(Dates.inDaysIso(5)))
        assertEquals(-2, Dates.daysUntil(Dates.inDaysIso(-2)))
        assertEquals(null, Dates.daysUntil(""))
        assertEquals("Today", Dates.relative(Dates.todayIso()))
        assertEquals("Tomorrow", Dates.relative(Dates.inDaysIso(1)))
        assertEquals("Date to be confirmed", Dates.pretty("garbage"))
        assertEquals(5, Dates.daysUntil(Dates.inDaysIso(5) + "T10:00:00+00:00"))
    }

    @Test fun plannedHoursAreNotVerifiedHours() {
        val a = event("a", "Environmental Science", 2, 4.0)
        val p = ProgressCalculator.compute(UserProfile(fullName = "Al", goalHours = 30.0), emptyList(), listOf(a))
        assertEquals(0.0, p.verifiedHours, 0.0)
        assertEquals(4.0, p.plannedHours, 0.0)
        assertTrue(Badge.FIRST_PLAN in p.badges)
        assertFalse(Badge.FIRST_SHIFT in p.badges)
    }

    @Test fun verifiedShiftsCountOnceAndAwardBadges() {
        val a = event("a", "Environmental Science", -1, 12.0)
        val profile = UserProfile(fullName = "Al", goalHours = 20.0)
        val p = ProgressCalculator.compute(profile, listOf(shift(a)), listOf(a))
        assertEquals(12.0, p.verifiedHours, 0.0)
        assertEquals(0.0, p.plannedHours, 0.0) // verified shift is not double counted as planned
        assertTrue(Badge.FIRST_SHIFT in p.badges)
        assertTrue(Badge.TEN_HOURS in p.badges)
        assertTrue(Badge.HALFWAY in p.badges)
        assertFalse(Badge.GOAL in p.badges)
        assertEquals(1, p.shiftsThisWeek)
        assertEquals(0.6f, p.verifiedFraction, 0.001f)
    }

    @Test fun goalBadgeAndExplorer() {
        val evs = listOf(event("a", "Tech", -1, 10.0), event("b", "Health", -1, 10.0), event("c", "Nature", -1, 10.0))
        val p = ProgressCalculator.compute(UserProfile(fullName = "Al", goalHours = 30.0), evs.map { shift(it) }, evs)
        assertTrue(p.goalReached)
        assertTrue(Badge.GOAL in p.badges)
        assertTrue(Badge.EXPLORER in p.badges)
        assertEquals(1f, p.verifiedFraction, 0f)
    }

    @Test fun oldShiftsDoNotCountForThisWeek() {
        val a = event("a", "Tech", -20, 3.0)
        val p = ProgressCalculator.compute(
            UserProfile(fullName = "Al"), listOf(shift(a, at = System.currentTimeMillis() - 20L * 86_400_000L)), emptyList()
        )
        assertEquals(0, p.shiftsThisWeek)
    }

    @Test fun rankerPutsInterestsFirstThenSoonest() {
        val events = listOf(
            event("far-match", "Computer Science & Tech", 9),
            event("near-other", "Environmental Science", 1),
            event("near-match", "Computer Science & Tech", 2)
        )
        val ids = EventRanker.apply(events, setOf(Cause.TECH), EventFilter()).map { it.id }
        assertEquals(listOf("near-match", "far-match", "near-other"), ids)
    }

    @Test fun rankerFiltersSearchesAndSorts() {
        val events = listOf(
            event("a", "Tech", 3, 1.0, "Robot club helper"),
            event("b", "Health", 10, 5.0, "Blood drive"),
            event("c", "Nature", 8, 2.0, "Trail day")
        )
        val none = emptySet<Cause>()
        assertEquals(listOf("b"), EventRanker.apply(events, none, EventFilter(query = "BLOOD")).map { it.id })
        assertEquals(listOf("c"), EventRanker.apply(events, none, EventFilter(cause = Cause.NATURE)).map { it.id })
        assertEquals(listOf("a", "c"), EventRanker.apply(events, none, EventFilter(maxHours = 3.0, sort = SortMode.SOONEST)).map { it.id })
        assertEquals(listOf("a"), EventRanker.apply(events, none, EventFilter(thisWeekOnly = true)).map { it.id })
        assertEquals(listOf("a", "c", "b"), EventRanker.apply(events, none, EventFilter(sort = SortMode.SHORTEST)).map { it.id })
        assertEquals(0, EventFilter().activeCount)
        assertEquals(2, EventFilter(cause = Cause.TECH, thisWeekOnly = true).activeCount)
    }

    @Test fun undatedEventsSinkInDateSorts() {
        val undated = event("u", "Tech", 0).copy(eventDate = "")
        val dated = event("d", "Tech", 4)
        val ids = EventRanker.apply(listOf(undated, dated), emptySet(), EventFilter(sort = SortMode.SOONEST)).map { it.id }
        assertEquals(listOf("d", "u"), ids)
    }
}
