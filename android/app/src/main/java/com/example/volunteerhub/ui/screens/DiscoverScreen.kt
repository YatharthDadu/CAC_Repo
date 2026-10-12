package com.example.volunteerhub.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.volunteerhub.model.UserProfile
import com.example.volunteerhub.model.VolunteerEvent

@Composable
fun DiscoverScreen(
    userProfile: UserProfile,
    events: List<VolunteerEvent>,
    rsvpdEventIds: Set<String>,
    onSelectEvent: (VolunteerEvent) -> Unit,
    onToggleRsvp: (VolunteerEvent) -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var selectedFilter by remember { mutableStateOf("All") }
    val filters = listOf("All", "Top Matches", "STEM & Tech", "Healthcare", "Environment", "Community")

    // Keyword sets for semantic major matching
    val stemKeywords = remember {
        listOf("stem", "tech", "computer", "code", "coding", "software", "robot", "robotics", "data", "ai", "3d print", "maker", "engineer", "python", "developer", "hardware", "wi-fi", "digital")
    }
    val preMedKeywords = remember {
        listOf("medical", "hospital", "patient", "blood", "health", "clinic", "wellness", "comfort kit", "hospice", "care")
    }
    val ecoKeywords = remember {
        listOf("park", "trail", "environment", "tree", "plant", "conservation", "nature", "wildflower", "prairie", "eco", "cleanup", "garden")
    }
    val eduKeywords = remember {
        listOf("tutor", "reading", "literacy", "library", "school", "mentor", "teach", "homework", "book", "youth", "children")
    }
    val bizKeywords = remember {
        listOf("business", "finance", "resale", "store", "inventory", "budget", "merchandising", "logistics", "operations")
    }

    // Dynamic AI match scoring based on the user's selected intended major
    fun calculateScore(event: VolunteerEvent, major: String): Int {
        val text = "${event.title} ${event.organizationName} ${event.description} ${event.category}".lowercase()
        val eventHash = (event.title.hashCode() and 0x7FFFFFFF) % 4

        val (primary, secondary) = when (major) {
            "Computer Science & Tech" -> stemKeywords to (bizKeywords + eduKeywords)
            "Engineering & Robotics" -> stemKeywords to (ecoKeywords + bizKeywords)
            "Pre-Med / Healthcare" -> preMedKeywords to eduKeywords
            "Environmental Science" -> ecoKeywords to eduKeywords
            "Education & Literacy" -> eduKeywords to stemKeywords
            "Business & Finance" -> bizKeywords to stemKeywords
            else -> stemKeywords to emptyList()
        }

        val primaryHits = primary.count { text.contains(it) }
        val secondaryHits = secondary.count { text.contains(it) }

        return when {
            primaryHits >= 2 -> 96 + eventHash // 96% - 99%
            primaryHits == 1 -> 91 + eventHash // 91% - 94%
            secondaryHits >= 1 -> 81 + eventHash // 81% - 84%
            text.contains("food") || text.contains("meal") || text.contains("community") -> 74 + eventHash
            else -> 38 + (eventHash * 3) // 38% - 47%
        }
    }

    // Determine category display tag
    fun getCategoryTag(event: VolunteerEvent): String {
        val text = "${event.title} ${event.organizationName} ${event.description} ${event.category}".lowercase()
        return when {
            stemKeywords.any { text.contains(it) } -> "💻 STEM & Tech"
            preMedKeywords.any { text.contains(it) } -> "🏥 Healthcare"
            ecoKeywords.any { text.contains(it) } -> "🌱 Environmental"
            eduKeywords.any { text.contains(it) } -> "📚 Education"
            bizKeywords.any { text.contains(it) } -> "📈 Business & Data"
            else -> "🤝 Community Service"
        }
    }

    // 1. Re-score and sort by student's intended major descending
    val scoredEvents = remember(events, userProfile.intendedMajor) {
        events.map { event ->
            val score = calculateScore(event, userProfile.intendedMajor)
            event.copy(similarity = score / 100.0)
        }.sortedByDescending { it.similarity ?: 0.0 }
    }

    // 2. Filter by active chip
    val displayedEvents = remember(scoredEvents, selectedFilter) {
        when (selectedFilter) {
            "Top Matches" -> scoredEvents.filter { (it.similarity ?: 0.0) >= 0.85 }
            "STEM & Tech" -> scoredEvents.filter { event ->
                val text = "${event.title} ${event.organizationName} ${event.description} ${event.category}".lowercase()
                stemKeywords.any { text.contains(it) }
            }
            "Healthcare" -> scoredEvents.filter { event ->
                val text = "${event.title} ${event.organizationName} ${event.description} ${event.category}".lowercase()
                preMedKeywords.any { text.contains(it) }
            }
            "Environment" -> scoredEvents.filter { event ->
                val text = "${event.title} ${event.organizationName} ${event.description} ${event.category}".lowercase()
                ecoKeywords.any { text.contains(it) }
            }
            "Community" -> scoredEvents.filter { event ->
                val text = "${event.title} ${event.organizationName} ${event.description} ${event.category}".lowercase()
                text.contains("food") || text.contains("meal") || text.contains("community") || text.contains("pack")
            }
            else -> scoredEvents
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Text(
                    text = "Discover Service",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "AI-ranked opportunities matched across Idealist & North Texas partners.",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Major Priority Badge
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF007AFF).copy(alpha = 0.10f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🎯", fontSize = 22.sp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Prioritizing: ${userProfile.intendedMajor}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color(0xFF007AFF)
                            )
                            Text(
                                text = "Top ${userProfile.intendedMajor} recommendations boosted to top",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Horizontal scrollable filter chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    filters.take(4).forEach { filter ->
                        FilterChip(
                            selected = (selectedFilter == filter),
                            onClick = { selectedFilter = filter },
                            label = { Text(filter, fontSize = 12.sp) },
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    filters.drop(4).forEach { filter ->
                        FilterChip(
                            selected = (selectedFilter == filter),
                            onClick = { selectedFilter = filter },
                            label = { Text(filter, fontSize = 12.sp) },
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            }
        }

        if (displayedEvents.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("🔍", fontSize = 36.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "No events found for '$selectedFilter'",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            "Switch to 'All' to view all matched volunteer opportunities.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        items(displayedEvents) { event ->
            val isRsvpd = rsvpdEventIds.contains(event.id)
            val score = ((event.similarity ?: 0.95) * 100).toInt()
            val categoryTag = getCategoryTag(event)

            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                border = if (isRsvpd) androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF34C759).copy(alpha = 0.5f)) else null,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = when {
                                score >= 90 -> Color(0xFFFF9500).copy(alpha = 0.15f)
                                score >= 75 -> Color(0xFF007AFF).copy(alpha = 0.12f)
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            }
                        ) {
                            Text(
                                text = if (score >= 90) "🔥 $score% Match" else if (score >= 75) "✨ $score% Match" else "🤝 $score% Match",
                                color = when {
                                    score >= 90 -> Color(0xFFFF9500)
                                    score >= 75 -> Color(0xFF007AFF)
                                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                                },
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        Text(
                            text = "📍 ${event.location}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = event.title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = event.organizationName,
                            fontSize = 14.sp,
                            color = Color(0xFF007AFF),
                            fontWeight = FontWeight.SemiBold
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (isRsvpd) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFF34C759).copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = "✓ NHS +4h",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF34C759),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                            ) {
                                Text(
                                    text = categoryTag,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = event.description.ifEmpty { "Verified volunteer session supporting North Texas community initiatives." },
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 3
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🗓️ ${event.eventDate}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            OutlinedButton(
                                onClick = { onSelectEvent(event) },
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text("Details", fontSize = 13.sp, maxLines = 1)
                            }

                            Button(
                                onClick = {
                                    onToggleRsvp(event)
                                    val msg = if (!isRsvpd) {
                                        "✓ RSVP Confirmed! +4.0 hrs added to National Honor Society."
                                    } else {
                                        "RSVP removed. National Honor Society hours updated."
                                    }
                                    android.widget.Toast.makeText(context, msg, android.widget.Toast.LENGTH_SHORT).show()
                                },
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isRsvpd) Color(0xFF34C759) else Color(0xFF007AFF)
                                )
                            ) {
                                Text(
                                    text = if (isRsvpd) "✓ RSVP'd" else "RSVP",
                                    fontSize = 13.sp,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
