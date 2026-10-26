package com.example.volunteerhub

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import com.example.volunteerhub.data.AppStore
import com.example.volunteerhub.data.EventFilter
import com.example.volunteerhub.data.EventRanker
import com.example.volunteerhub.data.Progress
import com.example.volunteerhub.data.ProgressCalculator
import com.example.volunteerhub.data.SupabaseRepository
import com.example.volunteerhub.model.ActiveShift
import com.example.volunteerhub.model.Badge
import com.example.volunteerhub.model.Dates
import com.example.volunteerhub.model.ShiftRecord
import com.example.volunteerhub.model.UserProfile
import com.example.volunteerhub.model.VolunteerEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

sealed class Route {
    object Home : Route()
    object Discover : Route()
    object Plan : Route()
    object Profile : Route()
    data class Detail(val eventId: String) : Route()
    object CheckIn : Route()
    object Signature : Route()
    object Transcript : Route()
    data class Xello(val eventId: String) : Route()

    val isTab: Boolean get() = this is Home || this is Discover || this is Plan || this is Profile
}

sealed interface EventsState {
    object Loading : EventsState
    data class Ready(val events: List<VolunteerEvent>, val isSample: Boolean = false) : EventsState
    data class Error(val message: String) : EventsState
}

/** What the celebration overlay shows after a shift is signed off. */
data class Celebration(val hours: Double, val title: String, val newBadges: List<Badge>, val goalReached: Boolean, val eventId: String = "")

class VolunteerViewModel(app: Application) : AndroidViewModel(app) {
    private val store = AppStore(app)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    var profile by mutableStateOf(store.loadProfile())
        private set
    var eventsState by mutableStateOf<EventsState>(EventsState.Loading)
        private set
    var filter by mutableStateOf(EventFilter())
        private set
    var shifts by mutableStateOf(store.loadShifts())
        private set
    var activeShift by mutableStateOf(store.loadActiveShift())
        private set
    var celebration by mutableStateOf<Celebration?>(null)
        private set

    private var planned by mutableStateOf(store.loadPlanned())
    var savedIds by mutableStateOf(store.loadIds("saved"))
        private set

    val backStack = mutableStateListOf<Route>(Route.Home)
    val route: Route get() = backStack.last()

    val plannedEvents: List<VolunteerEvent>
        get() = planned.filter { p -> shifts.none { it.eventId == p.id } }
            .sortedBy { Dates.daysUntil(it.eventDate) ?: Int.MAX_VALUE }

    val plannedIds: Set<String> get() = planned.map { it.id }.toSet()

    val progress: Progress
        get() = ProgressCalculator.compute(profile, shifts, planned)

    val visibleEvents: List<VolunteerEvent>
        get() = (eventsState as? EventsState.Ready)?.let {
            EventRanker.apply(it.events, profile.interests, filter)
        } ?: emptyList()

    val allEvents: List<VolunteerEvent>
        get() = (eventsState as? EventsState.Ready)?.events ?: emptyList()

    init {
        refresh()
    }

    // --- data -------------------------------------------------------------------------------

    fun refresh() {
        eventsState = EventsState.Loading
        scope.launch {
            eventsState = try {
                EventsState.Ready(SupabaseRepository.fetchEvents())
            } catch (e: IllegalStateException) {
                EventsState.Error(
                    if (SupabaseRepository.isConfigured) "We couldn't reach the opportunity feed."
                    else "This build isn't connected to the opportunity feed yet."
                )
            } catch (e: Exception) {
                EventsState.Error("We couldn't reach the opportunity feed. Check your connection and try again.")
            }
        }
    }

    fun useSampleEvents() {
        eventsState = EventsState.Ready(SupabaseRepository.sampleEvents(), isSample = true)
    }

    fun eventById(id: String): VolunteerEvent? =
        allEvents.firstOrNull { it.id == id } ?: planned.firstOrNull { it.id == id }

    // --- profile ----------------------------------------------------------------------------

    fun saveProfile(p: UserProfile) {
        profile = p
        store.saveProfile(p)
    }

    fun resetEverything() {
        store.clearAll()
        profile = store.loadProfile()
        planned = emptyList(); shifts = emptyList(); savedIds = emptySet(); activeShift = null
        backStack.clear(); backStack.add(Route.Home)
    }

    // --- plan / save ------------------------------------------------------------------------

    fun togglePlan(event: VolunteerEvent) {
        planned = if (planned.any { it.id == event.id }) planned.filter { it.id != event.id } else planned + event
        store.savePlanned(planned)
        if (activeShift?.eventId == event.id && event.id !in plannedIds) cancelShift()
    }

    fun toggleSaved(id: String) {
        savedIds = if (id in savedIds) savedIds - id else savedIds + id
        store.saveIds("saved", savedIds)
    }

    fun updateFilter(f: EventFilter) {
        filter = f
    }

    // --- shifts -----------------------------------------------------------------------------

    fun startShift(event: VolunteerEvent) {
        if (event.id !in plannedIds) togglePlan(event)
        val s = ActiveShift(event.id, System.currentTimeMillis())
        activeShift = s
        store.saveActiveShift(s)
    }

    fun cancelShift() {
        activeShift = null
        store.saveActiveShift(null)
    }

    /** Called when the supervisor signs. Hours are only credited here, never on sign-up. */
    fun verifyShift(
        supervisorName: String,
        supervisorEmail: String,
        supervisorRole: String = "Site Coordinator",
        signatureSvg: String = ""
    ) {
        val shift = activeShift ?: return
        val event = eventById(shift.eventId) ?: return
        val before = progress.badges
        val record = ShiftRecord(
            eventId = event.id, title = event.title, organizationName = event.organizationName,
            cause = event.cause, eventDate = event.eventDate, hours = event.durationHours,
            supervisorName = supervisorName.trim(), supervisorEmail = supervisorEmail.trim(),
            verifiedAtMillis = System.currentTimeMillis(),
            supervisorRole = supervisorRole.trim().ifBlank { "Site Coordinator" },
            signatureSvg = signatureSvg
        )
        shifts = shifts + record
        store.saveShifts(shifts)
        cancelShift()
        val after = progress
        celebration = Celebration(
            hours = record.hours, title = event.title,
            newBadges = (after.badges - before).toList(), goalReached = after.goalReached,
            eventId = event.id
        )
        backStack.clear(); backStack.add(Route.Home)
    }

    fun dismissCelebration() {
        celebration = null
    }

    // --- navigation -------------------------------------------------------------------------

    fun openTab(tab: Route) {
        backStack.clear()
        backStack.add(Route.Home)
        if (tab != Route.Home) backStack.add(tab)
    }

    fun push(route: Route) {
        if (route != this.route) backStack.add(route)
    }

    /** Returns false when there is nothing left to pop (the system should handle back). */
    fun back(): Boolean {
        if (backStack.size <= 1) return false
        backStack.removeAt(backStack.lastIndex)
        return true
    }

    override fun onCleared() {
        scope.cancel()
    }
}
