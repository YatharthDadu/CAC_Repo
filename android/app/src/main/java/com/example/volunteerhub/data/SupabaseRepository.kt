package com.example.volunteerhub.data

import com.example.volunteerhub.BuildConfig
import com.example.volunteerhub.model.Dates
import com.example.volunteerhub.model.VolunteerEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

object SupabaseRepository {
    // Explicit list: `select=*` would also download every 384-float embedding.
    private const val COLUMNS = "id,title,organization_name,location,event_date,description,category," +
        "source_url,source_platform,duration_hours,is_estimated_date,qr_seed"

    /** True when the build was given Supabase credentials (see `android/README` / local.properties). */
    val isConfigured: Boolean
        get() = BuildConfig.SUPABASE_URL.isNotBlank() && BuildConfig.SUPABASE_ANON_KEY.isNotBlank()

    /** Fetches upcoming events. Throws on network or server errors so the UI can show a real error state. */
    suspend fun fetchEvents(): List<VolunteerEvent> = withContext(Dispatchers.IO) {
        check(isConfigured) { "Supabase is not configured" }
        val today = Dates.todayIso()
        val url = URL(
            "${BuildConfig.SUPABASE_URL}/rest/v1/events?select=$COLUMNS" +
                "&event_date=gte.$today&order=event_date.asc&limit=200"
        )
        val conn = url.openConnection() as HttpURLConnection
        try {
            conn.requestMethod = "GET"
            conn.setRequestProperty("apikey", BuildConfig.SUPABASE_ANON_KEY)
            conn.setRequestProperty("Authorization", "Bearer ${BuildConfig.SUPABASE_ANON_KEY}")
            conn.setRequestProperty("Accept", "application/json")
            conn.connectTimeout = 8000
            conn.readTimeout = 8000
            if (conn.responseCode != 200) throw IOException("Server answered ${conn.responseCode}")
            val array = JSONArray(conn.inputStream.bufferedReader().use { it.readText() })
            (0 until array.length()).map { parse(array.getJSONObject(it)) }
        } finally {
            conn.disconnect()
        }
    }

    private fun parse(obj: JSONObject): VolunteerEvent {
        val rawDate = obj.optString("event_date", "")
        return VolunteerEvent(
            id = obj.optString("id"),
            title = obj.optString("title", "Volunteer shift"),
            organizationName = obj.optString("organization_name", "Local partner"),
            location = obj.optString("location", ""),
            eventDate = if (rawDate.length >= 10) rawDate.substring(0, 10) else "",
            description = obj.optString("description", ""),
            category = obj.optString("category", ""),
            sourceUrl = obj.optString("source_url", ""),
            sourcePlatform = obj.optString("source_platform", ""),
            durationHours = obj.optDouble("duration_hours", 3.0).let { if (it.isNaN() || it <= 0) 3.0 else it },
            isEstimatedDate = obj.optBoolean("is_estimated_date", false),
            qrSeed = obj.optString("qr_seed", "")
        )
    }

    /** Clearly-labelled sample events for demos and offline development. Dates are relative to today. */
    fun sampleEvents(): List<VolunteerEvent> = listOf(
        VolunteerEvent(
            id = "sample-stem", title = "STEM Mentors Needed in Frisco",
            organizationName = "STEM For Others", location = "Frisco, TX",
            eventDate = Dates.inDaysIso(2), timeLabel = "10:00 AM", durationHours = 3.0,
            category = "Computer Science & Tech", sourcePlatform = "Idealist",
            sourceUrl = "https://www.idealist.org/en/volunteer-opportunity/96097bcf011542ef8b33fbe0d4088155-stem-mentors-needed-in-frisco-tx-area-stem-for-others-inc-frisco",
            description = "Help younger students with Python basics and robotics kits. No experience needed beyond curiosity."
        ),
        VolunteerEvent(
            id = "sample-maker", title = "Teen Maker Lab Helper",
            organizationName = "Frisco Public Library", location = "Frisco, TX",
            eventDate = Dates.inDaysIso(3), timeLabel = "2:00 PM", durationHours = 2.0,
            category = "Computer Science & Tech", sourcePlatform = "Frisco Library",
            sourceUrl = "https://friscolibrary.com/volunteen/",
            description = "Set up 3D printers, guide kids through Scratch projects and keep the makerspace tidy."
        ),
        VolunteerEvent(
            id = "sample-comfort", title = "Patient Comfort Kit Assembly",
            organizationName = "Medical City Frisco", location = "Frisco, TX",
            eventDate = Dates.inDaysIso(5), timeLabel = "9:00 AM", durationHours = 4.0,
            category = "Pre-Med / Healthcare", sourcePlatform = "Medical City",
            sourceUrl = "https://medicalcityhealthcare.com/about/volunteering/",
            description = "Assemble hygiene and comfort kits for hospital patients alongside staff volunteers."
        ),
        VolunteerEvent(
            id = "sample-trail", title = "Trail Cleanup & Tree Planting",
            organizationName = "McKinney Parks Foundation", location = "McKinney, TX",
            eventDate = Dates.inDaysIso(9), timeLabel = "8:00 AM", durationHours = 3.5,
            category = "Environmental Science", sourcePlatform = "McKinney Parks",
            sourceUrl = "https://mckinneyparksfoundation.org/volunteer/",
            description = "Mulch paths and plant native saplings. Bring water and closed-toe shoes."
        ),
        VolunteerEvent(
            id = "sample-reading", title = "Reading Buddy for Grades K-3",
            organizationName = "Frisco Public Library", location = "Frisco, TX",
            eventDate = Dates.inDaysIso(6), timeLabel = "4:00 PM", durationHours = 1.5,
            category = "Education & Literacy", sourcePlatform = "Frisco Library",
            sourceUrl = "https://friscolibrary.com/volunteen/",
            description = "Read aloud with elementary students and help them pick their next favorite book."
        ),
        VolunteerEvent(
            id = "sample-meals", title = "Community Meal Packing Night",
            organizationName = "Frisco Fastpacs", location = "Frisco, TX",
            eventDate = Dates.inDaysIso(12), timeLabel = "5:30 PM", durationHours = 2.0,
            category = "Community & Civic", sourcePlatform = "Frisco Fastpacs",
            sourceUrl = "https://friscofastpacs.org/volunteer/",
            description = "Pack nutritious weekend meal bags for local school children."
        )
    )
}
