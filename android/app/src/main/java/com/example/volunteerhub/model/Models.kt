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
    val verifiedAtMillis: Long
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
