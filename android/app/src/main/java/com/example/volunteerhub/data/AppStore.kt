package com.example.volunteerhub.data

import android.content.Context
import com.example.volunteerhub.model.ActiveShift
import com.example.volunteerhub.model.Cause
import com.example.volunteerhub.model.ShiftRecord
import com.example.volunteerhub.model.UserProfile
import com.example.volunteerhub.model.VolunteerEvent
import org.json.JSONArray
import org.json.JSONObject

/** Everything the student creates lives here, in SharedPreferences, so it survives restarts. */
class AppStore(context: Context) {
    private val prefs = context.getSharedPreferences("volunteerhub", Context.MODE_PRIVATE)

    fun loadProfile(): UserProfile = UserProfile(
        fullName = prefs.getString("name", "") ?: "",
        school = prefs.getString("school", "") ?: "",
        grade = prefs.getString("grade", "") ?: "",
        interests = (prefs.getStringSet("interests", emptySet()) ?: emptySet())
            .mapNotNull { name -> Cause.values().firstOrNull { it.name == name } }.toSet(),
        targetClub = prefs.getString("club", "National Honor Society") ?: "National Honor Society",
        goalHours = prefs.getFloat("goal", 30f).toDouble(),
        weeklyGoal = prefs.getInt("weeklyGoal", 1)
    )

    fun saveProfile(p: UserProfile) {
        prefs.edit()
            .putString("name", p.fullName)
            .putString("school", p.school)
            .putString("grade", p.grade)
            .putStringSet("interests", p.interests.map { it.name }.toSet())
            .putString("club", p.targetClub)
            .putFloat("goal", p.goalHours.toFloat())
            .putInt("weeklyGoal", p.weeklyGoal)
            .apply()
    }

    fun loadIds(key: String): Set<String> = prefs.getStringSet(key, emptySet())?.toSet() ?: emptySet()

    fun saveIds(key: String, ids: Set<String>) {
        prefs.edit().putStringSet(key, ids).apply()
    }

    /** Events the student planned, stored in full so the plan still works if the feed changes. */
    fun loadPlanned(): List<VolunteerEvent> = readArray("planned") { o ->
        VolunteerEvent(
            id = o.getString("id"), title = o.getString("title"),
            organizationName = o.optString("org"), location = o.optString("loc"),
            eventDate = o.optString("date"), timeLabel = o.optString("time"),
            description = o.optString("desc"), category = o.optString("cat"),
            sourceUrl = o.optString("url"), sourcePlatform = o.optString("platform"),
            durationHours = o.optDouble("hours", 3.0), isEstimatedDate = o.optBoolean("est", false)
        )
    }

    fun savePlanned(events: List<VolunteerEvent>) = writeArray("planned", events) { e ->
        JSONObject().put("id", e.id).put("title", e.title).put("org", e.organizationName)
            .put("loc", e.location).put("date", e.eventDate).put("time", e.timeLabel)
            .put("desc", e.description).put("cat", e.category).put("url", e.sourceUrl)
            .put("platform", e.sourcePlatform).put("hours", e.durationHours).put("est", e.isEstimatedDate)
    }

    fun loadShifts(): List<ShiftRecord> = readArray("shifts") { o ->
        ShiftRecord(
            eventId = o.getString("id"), title = o.getString("title"),
            organizationName = o.optString("org"),
            cause = Cause.values().firstOrNull { it.name == o.optString("cause") } ?: Cause.COMMUNITY,
            eventDate = o.optString("date"), hours = o.optDouble("hours", 0.0),
            supervisorName = o.optString("sup"), supervisorEmail = o.optString("email"),
            verifiedAtMillis = o.optLong("at", 0L)
        )
    }

    fun saveShifts(shifts: List<ShiftRecord>) = writeArray("shifts", shifts) { s ->
        JSONObject().put("id", s.eventId).put("title", s.title).put("org", s.organizationName)
            .put("cause", s.cause.name).put("date", s.eventDate).put("hours", s.hours)
            .put("sup", s.supervisorName).put("email", s.supervisorEmail).put("at", s.verifiedAtMillis)
    }

    fun loadActiveShift(): ActiveShift? {
        val id = prefs.getString("activeId", null) ?: return null
        return ActiveShift(id, prefs.getLong("activeAt", 0L))
    }

    fun saveActiveShift(shift: ActiveShift?) {
        prefs.edit().apply {
            if (shift == null) {
                remove("activeId"); remove("activeAt")
            } else {
                putString("activeId", shift.eventId); putLong("activeAt", shift.startedAtMillis)
            }
        }.apply()
    }

    fun clearAll() = prefs.edit().clear().apply()

    private fun <T> readArray(key: String, map: (JSONObject) -> T): List<T> = try {
        val arr = JSONArray(prefs.getString(key, "[]"))
        (0 until arr.length()).map { map(arr.getJSONObject(it)) }
    } catch (e: Exception) {
        emptyList()
    }

    private fun <T> writeArray(key: String, items: List<T>, map: (T) -> JSONObject) {
        val arr = JSONArray()
        items.forEach { arr.put(map(it)) }
        prefs.edit().putString(key, arr.toString()).apply()
    }
}
