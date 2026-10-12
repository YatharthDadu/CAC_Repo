package com.example.volunteerhub.model

import java.util.UUID

data class VolunteerEvent(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val organizationName: String,
    val location: String,
    val eventDate: String,
    val similarity: Double? = null,
    val description: String = "",
    val qrSeed: String = "",
    val category: String = "General"
)

data class UserProfile(
    val id: String = UUID.randomUUID().toString(),
    val fullName: String = "Alex Chen",
    val grade: String = "11th Grade",
    val intendedMajor: String = "Pre-Med / Healthcare",
    val targetClub: String = "National Honor Society",
    val goalHours: Double = 30.0,
    val completedHours: Double = 14.0
)

data class AttendanceLog(
    val id: String = UUID.randomUUID().toString(),
    val eventId: String,
    val checkInTime: String,
    val checkOutTime: String? = null,
    val awardedHours: Double = 4.0,
    val supervisorName: String = "Marta Alvarez",
    val supervisorEmail: String = "m.alvarez@medicalcity.org",
    val status: String = "verified",
    val aiImpactBlurb: String = "Assembled 250+ pediatric care kits under time constraints, ensuring clinic readiness."
)
