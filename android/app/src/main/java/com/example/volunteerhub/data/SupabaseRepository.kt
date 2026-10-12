package com.example.volunteerhub.data

import com.example.volunteerhub.model.VolunteerEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL

object SupabaseRepository {
    private const val SUPABASE_URL = "https://cjxjjbwywepvuflwyshz.supabase.co"
    // Anon public key safe for client-side Android application
    private const val SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImNqeGpqYnd5d2VwdnVmbHd5c2h6Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3OTE1MDY2MTksImV4cCI6MjEwNzA4MjYxOX0.mH9-921443_your_anon_key_here"

    // Curated catalog descriptions mapped by title keyword
    private val descriptionMap = mapOf(
        "stem mentor" to ("Provide personalized STEM tutoring, Python coding guidance, and robotics mentorship for younger students in Frisco. Perfect for computer science pathway students." to "Computer Science & Tech"),
        "maker lab" to ("High school teen volunteers assist younger students with robotics, Scratch coding, 3D printing equipment, and digital literacy inside the makerspace." to "Computer Science & Tech"),
        "ai prompter" to ("Assist senior community members with digital literacy, testing helpful AI tools, and troubleshooting tablets and computers in Frisco and Plano, TX." to "Computer Science & Tech"),
        "software engineer" to ("Assist instructors with robotics kit assembly, block-based coding, and STEM challenge activities for student cohorts in Collin County." to "Computer Science & Tech"),
        "disaster tech" to ("Student technology volunteers support staging, testing, and configuring emergency Wi-Fi equipment, laptops, and networking gear." to "Computer Science & Tech"),
        "technology team" to ("Collaborate with youth media and tech teams to build digital outreach campaigns and maintain nonprofit web resources." to "Computer Science & Tech"),
        "women in stem" to ("Empower youth girls in North Texas through hands-on coding, science workshops, and technology career mentorship." to "Computer Science & Tech"),
        "tutor" to ("Support K-8 students with reading, homework, and fundamental math skills in after-school learning sessions." to "Education & Literacy"),
        "literacy" to ("Lead interactive storytelling, literacy workshops, and book organization for elementary school youth in Frisco, TX." to "Education & Literacy"),
        "patient comfort" to ("High school volunteers and pre-med pathway students assist hospital staff in assembling comfort kits and hygiene packages in Frisco, TX." to "Pre-Med / Healthcare"),
        "blood drive" to ("Youth volunteers greet blood donors, manage check-in rosters, and provide post-donation refreshments at community mobile drives." to "Pre-Med / Healthcare"),
        "health fair" to ("Support our annual wellness fair! Volunteers assist seniors with navigation, screening booth direction, and health literature." to "Pre-Med / Healthcare"),
        "trail" to ("Trail restoration, mulching nature paths, planting native saplings, and conservation along scenic Collin County trails." to "Environmental Science"),
        "park" to ("Volunteer outdoor workday with local conservationists preserving North Texas native wildlife habitats." to "Environmental Science"),
        "food sort" to ("Inspect, date-check, categorize, and pack emergency food boxes for regional community hunger relief." to "Community & Civic"),
        "packing night" to ("Help pack nutritious weekend food packs for FISD school children in Frisco, TX. Suitable for NHS and teen service clubs." to "Community & Civic")
    )

    suspend fun fetchEvents(): List<VolunteerEvent> = withContext(Dispatchers.IO) {
        val events = mutableListOf<VolunteerEvent>()
        try {
            val url = URL("$SUPABASE_URL/rest/v1/events?select=id,title,organization_name,location,event_date,qr_seed&limit=100")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.setRequestProperty("apikey", SUPABASE_ANON_KEY)
            conn.setRequestProperty("Authorization", "Bearer $SUPABASE_ANON_KEY")
            conn.setRequestProperty("Accept", "application/json")
            conn.connectTimeout = 5000
            conn.readTimeout = 5000

            if (conn.responseCode == 200) {
                val jsonText = conn.inputStream.bufferedReader().use { it.readText() }
                val array = JSONArray(jsonText)
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    val title = obj.optString("title", "Volunteer Shift")
                    val org = obj.optString("organization_name", "North Texas Partner")
                    val loc = obj.optString("location", "Frisco, TX")
                    val date = obj.optString("event_date", "Upcoming")
                    val qr = obj.optString("qr_seed", "")

                    // Look up matched description & category
                    val titleLower = title.lowercase()
                    val matchEntry = descriptionMap.entries.firstOrNull { titleLower.contains(it.key) }
                    val desc = matchEntry?.value?.first ?: "$title opportunity supporting $org in North Texas community initiatives."
                    val cat = matchEntry?.value?.second ?: "General"

                    events.add(
                        VolunteerEvent(
                            id = obj.optString("id"),
                            title = title,
                            organizationName = org,
                            location = loc,
                            eventDate = date.split("T").firstOrNull() ?: date,
                            qrSeed = qr,
                            description = desc,
                            category = cat,
                            similarity = 0.95
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        if (events.isEmpty()) {
            events.addAll(getFallbackEvents())
        }
        events
    }

    fun getFallbackEvents(): List<VolunteerEvent> = listOf(
        VolunteerEvent(
            title = "STEM Mentors Needed in Frisco, TX Area",
            organizationName = "STEM FOR OTHERS INC",
            location = "Frisco, TX",
            eventDate = "Saturday, Oct 18 • 10:00 AM",
            similarity = 0.98,
            category = "Computer Science & Tech",
            description = "Provide personalized STEM tutoring, Python coding guidance, and robotics mentorship for younger students in Frisco. Perfect for computer science pathway students."
        ),
        VolunteerEvent(
            title = "Teen Tech Maker Lab & 3D Printing Assistant",
            organizationName = "Frisco Public Library",
            location = "Frisco, TX",
            eventDate = "Sunday, Oct 19 • 2:00 PM",
            similarity = 0.97,
            category = "Computer Science & Tech",
            description = "High school teen volunteers assist younger students with robotics, Scratch coding, 3D printing equipment, and digital literacy inside the makerspace."
        ),
        VolunteerEvent(
            title = "DATA TECHNOLOGY AI PROMPTER / EVALUATOR",
            organizationName = "GRANDPARENTS COMMUNITY NETWORK INC",
            location = "Frisco, TX",
            eventDate = "Tuesday, Oct 21 • 4:00 PM",
            similarity = 0.96,
            category = "Computer Science & Tech",
            description = "Assist senior community members with digital literacy, testing helpful AI tools, and troubleshooting tablets and computers in Frisco and Plano, TX."
        ),
        VolunteerEvent(
            title = "Patient Comfort Kit Assembly",
            organizationName = "Medical City Frisco",
            location = "Frisco, TX",
            eventDate = "Saturday, Oct 25 • 9:00 AM",
            similarity = 0.98,
            category = "Pre-Med / Healthcare",
            description = "High school volunteers and pre-med pathway students assist hospital staff in assembling comfort kits and hygiene packages for pediatric and adult patients."
        ),
        VolunteerEvent(
            title = "Towne Lake Trail Maintenance & Tree Planting",
            organizationName = "McKinney Parks Foundation",
            location = "McKinney, TX",
            eventDate = "Saturday, Nov 1 • 8:00 AM",
            similarity = 0.92,
            category = "Environmental Science",
            description = "Trail restoration, mulching nature paths, planting native saplings, and conservation along scenic Collin County trails."
        ),
        VolunteerEvent(
            title = "Community Meal Packing Night",
            organizationName = "Frisco Fastpacs",
            location = "Frisco, TX",
            eventDate = "Tuesday, Nov 4 • 5:30 PM",
            similarity = 0.88,
            category = "Community & Civic",
            description = "Help pack nutritious weekend food packs for FISD school children in Frisco, TX. Suitable for NHS and teen service clubs."
        )
    )
}
