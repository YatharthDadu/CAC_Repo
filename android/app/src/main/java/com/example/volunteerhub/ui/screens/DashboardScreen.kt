package com.example.volunteerhub.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.volunteerhub.model.UserProfile
import com.example.volunteerhub.model.VolunteerEvent
import com.example.volunteerhub.ui.theme.*

@Composable
fun DashboardScreen(
    userProfile: UserProfile,
    events: List<VolunteerEvent>,
    rsvpdEventIds: Set<String>,
    onOpenXello: (VolunteerEvent) -> Unit,
    onNavigateToDiscover: () -> Unit,
    onToggleRsvp: (VolunteerEvent) -> Unit
) {
    val rsvpdEventsList = events.filter { rsvpdEventIds.contains(it.id) }
    val rsvpCount = rsvpdEventIds.size
    val rsvpHours = rsvpCount * 4
    val hoursRemaining = (userProfile.goalHours - userProfile.completedHours).coerceAtLeast(0.0).toInt()
    val progress = (userProfile.completedHours / userProfile.goalHours).toFloat().coerceIn(0f, 1f)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Hello, ${userProfile.fullName.split(" ").firstOrNull() ?: "Alex"} 👋",
                        fontSize = 24.sp,
                        fontFamily = InterFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = Y2KTextDeep
                    )
                    Text(
                        text = "${userProfile.grade} • ${userProfile.intendedMajor}",
                        fontSize = 14.sp,
                        fontFamily = InterFontFamily,
                        color = Y2KTextSecondary
                    )
                }
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = PastelLavender,
                    border = androidx.compose.foundation.BorderStroke(1.dp, PastelLavenderDeep.copy(alpha = 0.6f))
                ) {
                    Text(
                        text = "TX-04",
                        color = DeepRoyalViolet,
                        fontWeight = FontWeight.Bold,
                        fontFamily = InterFontFamily,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }

        item {
            // National Honor Society (NHS) Circular Progress Card
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = PastelLavenderLight),
                border = androidx.compose.foundation.BorderStroke(1.dp, PastelLavenderDeep.copy(alpha = 0.5f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize()
            ) {
                Column(modifier = Modifier.padding(22.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "🏛️ ${userProfile.targetClub} (NHS)",
                                    fontSize = 14.sp,
                                    fontFamily = InterFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    color = VibrantPeriwinkle
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = "${userProfile.completedHours.toInt()} / ${userProfile.goalHours.toInt()} Hours",
                                fontSize = 28.sp,
                                fontFamily = InterFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = Y2KTextDeep
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = if (hoursRemaining > 0) {
                                    "$hoursRemaining hours needed for Fall audit"
                                } else {
                                    "🎉 Fall NHS requirement fulfilled!"
                                },
                                fontSize = 13.sp,
                                fontFamily = InterFontFamily,
                                color = Y2KTextSecondary
                            )
                        }

                        // Circular Progress Indicator
                        Box(contentAlignment = Alignment.Center) {
                            Canvas(modifier = Modifier.size(80.dp)) {
                                drawArc(
                                    color = PastelLavenderDeep.copy(alpha = 0.35f),
                                    startAngle = -90f,
                                    sweepAngle = 360f,
                                    useCenter = false,
                                    style = Stroke(width = 10.dp.toPx(), cap = StrokeCap.Round)
                                )
                                drawArc(
                                    color = VibrantPeriwinkle,
                                    startAngle = -90f,
                                    sweepAngle = 360f * progress,
                                    useCenter = false,
                                    style = Stroke(width = 10.dp.toPx(), cap = StrokeCap.Round)
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "${(progress * 100).toInt()}%",
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = InterFontFamily,
                                    fontSize = 17.sp,
                                    color = Y2KTextDeep
                                )
                                Text(
                                    text = "Goal",
                                    fontSize = 10.sp,
                                    fontFamily = InterFontFamily,
                                    color = Y2KTextMuted
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = PastelLavenderDeep.copy(alpha = 0.4f))
                    Spacer(modifier = Modifier.height(12.dp))

                    // Audit info and RSVP impact breakdown
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Audit Deadline: Nov 15, 2026",
                                fontSize = 11.sp,
                                fontFamily = InterFontFamily,
                                color = Y2KTextSecondary
                            )
                            Text(
                                text = "14h verified base",
                                fontSize = 11.sp,
                                fontFamily = InterFontFamily,
                                color = Y2KTextMuted
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (rsvpCount > 0) PastelMint else Color(0xFFF1F5F9),
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (rsvpCount > 0) VibrantMint.copy(alpha = 0.4f) else Y2KBorderSoft)
                        ) {
                            Text(
                                text = if (rsvpCount > 0) "+${rsvpHours}h from $rsvpCount RSVP${if (rsvpCount != 1) "s" else ""}" else "0 active RSVPs",
                                color = if (rsvpCount > 0) DeepEmerald else Y2KTextMuted,
                                fontWeight = FontWeight.Bold,
                                fontFamily = InterFontFamily,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Upcoming RSVPs & Verified Logs",
                    fontSize = 18.sp,
                    fontFamily = InterFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = Y2KTextDeep
                )

                if (rsvpCount > 0) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = PastelLavender,
                        border = androidx.compose.foundation.BorderStroke(1.dp, PastelLavenderDeep.copy(alpha = 0.6f))
                    ) {
                        Text(
                            text = "$rsvpCount Scheduled",
                            color = DeepRoyalViolet,
                            fontWeight = FontWeight.Bold,
                            fontFamily = InterFontFamily,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }

        if (rsvpdEventsList.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = PastelLavenderLight.copy(alpha = 0.6f)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, PastelLavenderDeep.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("📅", fontSize = 36.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No Upcoming RSVPs Yet",
                            fontSize = 16.sp,
                            fontFamily = InterFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = Y2KTextDeep
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "RSVP to opportunities in Discover to add upcoming shifts here and automatically credit hours toward your National Honor Society goal.",
                            fontSize = 13.sp,
                            fontFamily = InterFontFamily,
                            color = Y2KTextSecondary,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Surface(
                            onClick = onNavigateToDiscover,
                            shape = RoundedCornerShape(14.dp),
                            shadowElevation = 4.dp
                        ) {
                            Box(
                                modifier = Modifier
                                    .background(Y2KButtonGradient)
                                    .padding(horizontal = 18.dp, vertical = 10.dp)
                            ) {
                                Text(
                                    text = "Browse Discover Opportunities →",
                                    fontSize = 13.sp,
                                    fontFamily = InterFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        } else {
            items(rsvpdEventsList) { event ->
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, PastelMint),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = PastelMint,
                                border = androidx.compose.foundation.BorderStroke(1.dp, VibrantMint.copy(alpha = 0.4f))
                            ) {
                                Text(
                                    text = "✓ RSVP Confirmed • +4.0 hrs",
                                    color = DeepEmerald,
                                    fontSize = 11.sp,
                                    fontFamily = InterFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }

                            Text(
                                text = "📍 ${event.location}",
                                fontSize = 12.sp,
                                fontFamily = InterFontFamily,
                                color = Y2KTextMuted
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = event.title,
                            fontWeight = FontWeight.Bold,
                            fontFamily = InterFontFamily,
                            fontSize = 16.sp,
                            color = Y2KTextDeep
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        Text(
                            text = event.organizationName,
                            fontSize = 13.sp,
                            fontFamily = InterFontFamily,
                            color = VibrantPeriwinkle,
                            fontWeight = FontWeight.SemiBold
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "🗓️ ${event.eventDate} • Counts toward NHS Goal",
                            fontSize = 12.sp,
                            fontFamily = InterFontFamily,
                            color = Y2KTextSecondary
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedButton(
                                onClick = { onToggleRsvp(event) },
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text("Cancel RSVP", fontSize = 12.sp, fontFamily = InterFontFamily, color = MaterialTheme.colorScheme.error)
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Surface(
                                onClick = { onOpenXello(event) },
                                shape = RoundedCornerShape(12.dp),
                                shadowElevation = 2.dp
                            ) {
                                Box(
                                    modifier = Modifier
                                        .background(Y2KButtonGradient)
                                        .padding(horizontal = 14.dp, vertical = 8.dp)
                                ) {
                                    Text(
                                        text = "Export / Xello ↗",
                                        fontSize = 12.sp,
                                        fontFamily = InterFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
