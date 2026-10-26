package com.example.volunteerhub.model

import java.util.UUID

/** The causes a student can care about. Every event maps to exactly one. */
enum class Cause(val label: String, val tagline: String) {
    TECH("Tech & Coding", "Robots, code, digital skills"),
    HEALTH("Health", "Hospitals, clinics, wellness"),
    NATURE("Nature", "Parks, trails, wildlife"),
    LEARNING("Teaching & Books", "Tutoring, literacy, mentoring"),
    COMMUNITY("Food & Community", "Meals, neighbors, local help"),
    BUSINESS("Business & Money", "Retail, finance, logistics");

    companion object {
        /** Maps the free-text category stored in the database (old and new spellings) to a [Cause]. */
        fun from(category: String, fallbackText: String = ""): Cause {
            val c = category.lowercase()
            return when {
                c.contains("tech") || c.contains("computer") || c.contains("engineer") || c.contains("robot") -> TECH
                c.contains("health") || c.contains("med") -> HEALTH
                c.contains("environment") || c.contains("nature") -> NATURE
                c.contains("educat") || c.contains("literacy") || c.contains("learning") || c.contains("teach") -> LEARNING
                c.contains("business") || c.contains("finance") -> BUSINESS
                c.contains("community") || c.contains("civic") || c.contains("food") -> COMMUNITY
                else -> guessFromText(fallbackText)
            }
        }

        private fun guessFromText(text: String): Cause {
            val t = text.lowercase()
            return when {
                listOf("code", "coding", "robot", "stem", "3d print", "software").any { t.contains(it) } -> TECH
                listOf("hospital", "patient", "blood", "health", "clinic").any { t.contains(it) } -> HEALTH
                listOf("park", "trail", "tree", "conservation", "garden").any { t.contains(it) } -> NATURE
                listOf("tutor", "reading", "literacy", "library", "mentor").any { t.contains(it) } -> LEARNING
                listOf("store", "retail", "finance", "inventory", "resale").any { t.contains(it) } -> BUSINESS
                else -> COMMUNITY
            }
        }
    }
}

data class VolunteerEvent(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val organizationName: String,
    val location: String,
    /** ISO date, `yyyy-MM-dd`, or empty when the source gave no date. */
    val eventDate: String = "",
    val timeLabel: String = "",
    val description: String = "",
    val category: String = "",
    val sourceUrl: String = "",
    val sourcePlatform: String = "",
    val durationHours: Double = 3.0,
    val isEstimatedDate: Boolean = false,
    val qrSeed: String = ""
) {
    val cause: Cause get() = Cause.from(category, "$title $description")

    val signupUrl: String
        get() {
            if (sourceUrl.isNotBlank()) return sourceUrl
            val org = organizationName.lowercase()
            return when {
                org.contains("food bank") || org.contains("ntfb") -> "https://ntfb.org/get-involved/volunteer/"
                org.contains("fastpacs") -> "https://www.friscofastpacs.org/volunteer-opportunities"
                org.contains("frisco public library") || (org.contains("library") && org.contains("frisco")) -> "https://friscolibrary.com/volunteen/"
                org.contains("sci-tech") -> "https://mindstretchingfun.org/volunteer/"
                org.contains("mckinney parks") -> "https://www.mckinneyparksfoundation.org/volunteer/"
                org.contains("medical city") -> "https://medicalcityhealthcare.com/about/volunteering/"
                org.contains("carter bloodcare") -> "https://www.carterbloodcare.org/volunteer/"
                org.contains("texas health") -> "https://www.texashealth.org/Volunteer"
                org.contains("red cross") -> "https://www.redcross.org/volunteer/become-a-volunteer.html"
                org.contains("step up tutoring") -> "https://www.stepuptutoring.org/volunteer"
                org.contains("stem for others") -> "https://www.idealist.org/en/volunteer-opportunity/96097bcf011542ef8b33fbe0d4088155-stem-mentors-needed-in-frisco-tx-area-stem-for-others-inc-frisco"
                org.contains("ronnia langston") -> "https://www.idealist.org/en/volunteer-opportunity/fb07f559c61e4b3da59dc9e63478e1b3-software-engineers-needed-summer-2027-stem-program-ronnia-langston-foundation-incorporated-plano"
                org.contains("grandparents community") -> "https://www.idealist.org/en/volunteer-opportunity/d61518b18fc24bf5b6018b292aa457b4-data-technology-ai-prompter-evaluator-grandparents-community-network-inc-frisco"
                org.contains("coderdojo") -> "https://coderdojo.com/"
                org.contains("boys & girls") || org.contains("bgccc") -> "https://bgccc.org/volunteer/"
                org.contains("frisco family services") -> "https://friscofamilyservices.org/volunteer/"
                org.contains("junior achievement") -> "https://dallas.ja.org/"
                org.contains("frisco garden club") -> "https://friscogardenclub.org/"
                org.contains("allen senior") -> "https://www.cityofallen.org/facilities/facility/details/Senior-Center-8"
                org.contains("ronald mcdonald") -> "https://rmhdallas.org/get-involved/volunteer/"
                org.contains("dallas pets alive") -> "https://dallaspetsalive.org/volunteer/"
                org.contains("reading partners") -> "https://readingpartners.org/volunteer/"
                org.contains("readers 2 leaders") -> "https://www.readers2leaders.org/volunteer"
                org.contains("upchieve") -> "https://upchieve.org/volunteer"
                org.contains("united way") -> "https://www.unitedwaytarrant.org/volunteer"
                org.contains("catholic charities") -> "https://catholiccharitiesfortworth.org/volunteer/"
                org.contains("sewa international") -> "https://www.sewausa.org/Volunteer"
                org.contains("coachart") -> "https://coachart.org/volunteer/"
                org.contains("cedar ridge") -> "https://audubondallas.org/cedar-ridge-preserve/"
                org.contains("disaster resource") || org.contains("itdrc") -> "https://www.itdrc.org/volunteer"
                org.contains("rescue animals") -> "https://www.volunteermatch.org/"
                org.contains("be kingdom builders") -> "https://www.volunteermatch.org/"
                org.contains("oneinmath") -> "https://www.oneinmath.com/volunteer/"
                org.contains("project sunshine") -> "https://projectsunshine.org/volunteer/"
                org.contains("amedisys hospice") -> "https://www.amedisys.com/services/hospice-care/volunteers/"
                org.contains("silverado hospice") -> "https://silverado.com/volunteer/"
                org.contains("plano conservancy") -> "https://www.planoconservancy.org/"
                org.isNotBlank() -> "https://www.volunteermatch.org/search?l=Plano%2C+TX&k=" + java.net.URLEncoder.encode(organizationName, "UTF-8")
                else -> "https://www.volunteermatch.org/"
            }
        }
}

data class UserProfile(
    val fullName: String = "",
    val school: String = "",
    val grade: String = "",
    val interests: Set<Cause> = emptySet(),
    val targetClub: String = "National Honor Society",
    val goalHours: Double = 30.0,
    /** Shifts per week the student wants to hit. */
    val weeklyGoal: Int = 1
) {
    val isComplete: Boolean get() = fullName.isNotBlank()
    val firstName: String get() = fullName.trim().substringBefore(' ')
}

/** A shift the student attended and a supervisor signed off. */
data class ShiftRecord(
    val eventId: String,
    val title: String,
    val organizationName: String,
    val cause: Cause,
    val eventDate: String,
    val hours: Double,
    val supervisorName: String,
    val supervisorEmail: String,
    val verifiedAtMillis: Long,
    val supervisorRole: String = "Site Coordinator",
    val signatureSvg: String = ""
)

/** The shift currently in progress, if any. */
data class ActiveShift(val eventId: String, val startedAtMillis: Long)

enum class Badge(val title: String, val hint: String) {
    FIRST_PLAN("Game plan", "Add your first shift to your plan"),
    FIRST_SHIFT("First shift", "Get a shift signed off"),
    EXPLORER("Explorer", "Plan or finish shifts in 3 different causes"),
    TEN_HOURS("10 hours", "Reach 10 verified hours"),
    HALFWAY("Halfway there", "Reach half of your hour goal"),
    GOAL("Goal crushed", "Reach your full hour goal")
}
