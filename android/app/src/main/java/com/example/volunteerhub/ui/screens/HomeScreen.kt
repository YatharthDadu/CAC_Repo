package com.example.volunteerhub.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.volunteerhub.VolunteerViewModel
import com.example.volunteerhub.Route
import com.example.volunteerhub.model.Dates
import com.example.volunteerhub.ui.components.BadgeShelf
import com.example.volunteerhub.ui.components.ButtonStyle
import com.example.volunteerhub.ui.components.ChunkyButton
import com.example.volunteerhub.ui.components.ChunkyCard
import com.example.volunteerhub.ui.components.CausePill
import com.example.volunteerhub.ui.components.EmptyState
import com.example.volunteerhub.ui.components.LinearGoalBar
import com.example.volunteerhub.ui.components.ProgressRing
import com.example.volunteerhub.ui.components.SectionTitle
import com.example.volunteerhub.ui.components.StatusPill
import com.example.volunteerhub.ui.components.hoursLabel
import com.example.volunteerhub.ui.theme.Hub
import com.example.volunteerhub.ui.theme.Spacing
import androidx.compose.foundation.lazy.items

@Composable
fun HomeScreen(vm: VolunteerViewModel) {
    val h = Hub.colors
    val progress = vm.progress
    val profile = vm.profile
    val next = vm.plannedEvents.firstOrNull()
    val active = vm.activeShift

    LazyColumn(
        Modifier.fillMaxSize().statusBarsPadding(),
        contentPadding = PaddingValues(Spacing.lg),
        verticalArrangement = Arrangement.spacedBy(Spacing.lg)
    ) {
        item {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(
                        if (profile.firstName.isBlank()) "Hey there!" else "Hey, ${profile.firstName}!",
                        style = MaterialTheme.typography.headlineMedium, color = h.ink
                    )
                    Text(
                        if (progress.goalReached) "You hit your goal. Keep the momentum going."
                        else "${progress.hoursToGo.hoursLabel()} hours until ${profile.targetClub}.",
                        style = MaterialTheme.typography.bodyMedium, color = h.inkSoft
                    )
                }
                StatusPill(
                    "${progress.shiftsThisWeek}/${progress.weeklyGoal} this week",
                    Icons.Filled.LocalFireDepartment,
                    if (progress.shiftsThisWeek >= progress.weeklyGoal) h.successSoft else h.rewardSoft,
                    if (progress.shiftsThisWeek >= progress.weeklyGoal) h.successDeep else (if (h.isDark) h.reward else Color(0xFF6B4A00))
                )
            }
        }

        if (active != null) {
            val ev = vm.eventById(active.eventId)
            item {
                ChunkyCard(
                    face = h.successSoft, lip = h.success,
                    onClick = { vm.push(Route.CheckIn) }, onClickLabel = "Return to your shift"
                ) {
                    Row(Modifier.padding(Spacing.lg), verticalAlignment = Alignment.CenterVertically) {
                        androidx.compose.material3.Icon(Icons.Filled.Timer, null, tint = h.successDeep)
                        Spacer(Modifier.width(Spacing.md))
                        Column(Modifier.weight(1f)) {
                            Text("Shift in progress", style = MaterialTheme.typography.titleSmall, color = h.successDeep)
                            Text(ev?.title ?: "Your shift", style = MaterialTheme.typography.bodyMedium, color = h.ink, maxLines = 1)
                        }
                        Text("Open", style = MaterialTheme.typography.labelLarge, color = h.successDeep)
                    }
                }
            }
        }

        // Goal card: the visual anchor of the app.
        item {
            ChunkyCard(face = MaterialTheme.colorScheme.primary, lip = h.brandDeep) {
                Row(
                    Modifier.padding(Spacing.lg),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Spacing.lg)
                ) {
                    ProgressRingOnBrand(progress.verifiedFraction, progress.plannedFraction, progress.verifiedHours, progress.goalHours)
                    Column(verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                        Text(profile.targetClub, style = MaterialTheme.typography.titleMedium, color = Color.White)
                        Text(
                            "${progress.verifiedHours.hoursLabel()} verified",
                            style = MaterialTheme.typography.headlineSmall, color = h.reward
                        )
                        Text(
                            "+${progress.plannedHours.hoursLabel()} hrs planned",
                            style = MaterialTheme.typography.bodyMedium, color = Color(0xFFE5DBFF)
                        )
                        Text(
                            "Hours count once a supervisor signs.",
                            style = MaterialTheme.typography.bodySmall, color = Color(0xFFE5DBFF)
                        )
                    }
                }
            }
        }

        item {
            ChunkyCard {
                Column(Modifier.padding(Spacing.lg), verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Weekly goal", style = MaterialTheme.typography.titleSmall, color = h.ink)
                        Text(
                            "${progress.shiftsThisWeek} of ${progress.weeklyGoal} shifts",
                            style = MaterialTheme.typography.labelMedium, color = h.inkSoft
                        )
                    }
                    LinearGoalBar(
                        if (progress.weeklyGoal == 0) 0f else progress.shiftsThisWeek.toFloat() / progress.weeklyGoal
                    )
                }
            }
        }

        item { SectionTitle("Next up") }
        item {
            if (next == null) {
                ChunkyCard {
                    EmptyState(
                        icon = Icons.Filled.Explore,
                        title = "Nothing planned yet",
                        body = "Pick a shift from Discover and it will show up here.",
                        actionLabel = "Find a shift",
                        onAction = { vm.openTab(Route.Discover) }
                    )
                }
            } else {
                ChunkyCard(onClick = { vm.push(Route.Detail(next.id)) }, onClickLabel = "Open ${next.title}") {
                    Column(Modifier.padding(Spacing.lg), verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                        CausePill(next.cause)
                        Text(next.title, style = MaterialTheme.typography.titleMedium, color = h.ink, maxLines = 2)
                        Text(
                            "${Dates.relative(next.eventDate)} · ${next.organizationName}",
                            style = MaterialTheme.typography.bodySmall, color = h.inkSoft, maxLines = 1
                        )
                        ChunkyButton(
                            "Check in", onClick = { vm.startShift(next); vm.push(Route.CheckIn) },
                            Modifier.fillMaxWidth(), style = ButtonStyle.Success, icon = Icons.Filled.PlayArrow
                        )
                    }
                }
            }
        }

        item { SectionTitle("Badges") }
        item { BadgeShelf(progress.badges) }

        if (vm.shifts.isNotEmpty()) {
            val latestShift = vm.shifts.maxByOrNull { it.verifiedAtMillis }
            if (latestShift != null) {
                item { SectionTitle("Recent verification") }
                item {
                    ChunkyCard(
                        face = h.card,
                        onClick = { vm.push(Route.Xello(latestShift.eventId)) }
                    ) {
                        Column(Modifier.padding(Spacing.lg), verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                StatusPill("VERIFIED ✓", Icons.Filled.CheckCircle, h.successSoft, h.successDeep)
                                Text("${latestShift.hours.hoursLabel()} hrs", style = MaterialTheme.typography.titleMedium, color = h.successDeep)
                            }
                            Text(latestShift.title, style = MaterialTheme.typography.titleSmall, color = h.ink, maxLines = 1)
                            Text("${latestShift.organizationName} · Signed by ${latestShift.supervisorName}", style = MaterialTheme.typography.bodySmall, color = h.inkSoft)
                            ChunkyButton(
                                "Export / Xello 📋",
                                onClick = { vm.push(Route.Xello(latestShift.eventId)) },
                                modifier = Modifier.fillMaxWidth().padding(top = Spacing.xs),
                                style = ButtonStyle.Quiet
                            )
                        }
                    }
                }
            }
            item {
                ChunkyButton(
                    "View my service transcript", onClick = { vm.push(Route.Transcript) },
                    Modifier.fillMaxWidth(), style = ButtonStyle.Quiet, icon = Icons.Filled.Description
                )
            }
        }
    }
}

@Composable
private fun ProgressRingOnBrand(verified: Float, planned: Float, hours: Double, goal: Double) {
    ProgressRing(
        verified = verified, planned = planned, size = 124.dp, stroke = 14.dp,
        description = "${hours.hoursLabel()} of ${goal.hoursLabel()} hours verified",
        trackColor = Color(0xFF4A22B8), plannedColor = Color.White.copy(alpha = 0.35f),
        arcColor = Hub.colors.reward, doneColor = Color.White
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(hours.hoursLabel(), style = MaterialTheme.typography.headlineMedium, color = Color.White)
            Text("of ${goal.hoursLabel()} hrs", style = MaterialTheme.typography.labelSmall, color = Color(0xFFE5DBFF))
        }
    }
}
