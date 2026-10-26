package com.example.volunteerhub.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.volunteerhub.Route
import com.example.volunteerhub.VolunteerViewModel
import com.example.volunteerhub.data.EventRanker
import com.example.volunteerhub.model.Dates
import com.example.volunteerhub.ui.components.ButtonStyle
import com.example.volunteerhub.ui.components.ChunkyButton
import com.example.volunteerhub.ui.components.ChunkyCard
import com.example.volunteerhub.ui.components.CausePill
import com.example.volunteerhub.ui.components.EmptyState
import com.example.volunteerhub.ui.components.EventCard
import com.example.volunteerhub.ui.components.SectionTitle
import com.example.volunteerhub.ui.components.StatusPill
import com.example.volunteerhub.ui.components.hoursLabel
import com.example.volunteerhub.ui.theme.Hub
import com.example.volunteerhub.ui.theme.Spacing

@Composable
fun PlanScreen(vm: VolunteerViewModel) {
    val h = Hub.colors
    val planned = vm.plannedEvents
    val saved = vm.allEvents.filter { it.id in vm.savedIds && it.id !in vm.plannedIds }
    val done = vm.shifts.sortedByDescending { it.verifiedAtMillis }
    val context = LocalContext.current

    LazyColumn(
        Modifier.fillMaxSize().statusBarsPadding(),
        contentPadding = PaddingValues(Spacing.lg),
        verticalArrangement = Arrangement.spacedBy(Spacing.lg)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                Text("My plan", style = MaterialTheme.typography.headlineMedium, color = h.ink)
                Text("Shifts you're doing, saved ideas and finished work.", style = MaterialTheme.typography.bodyMedium, color = h.inkSoft)
            }
        }

        item { SectionTitle("Coming up") }
        if (planned.isEmpty()) {
            item {
                EmptyState(
                    icon = Icons.Filled.EventAvailable, title = "No shifts planned",
                    body = "Tap “I'm in” on any opportunity to add it here.",
                    actionLabel = "Browse opportunities", onAction = { vm.openTab(Route.Discover) }
                )
            }
        } else {
            items(planned, key = { "p" + it.id }) { event ->
                Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                    EventCard(
                        event = event, isPlanned = true, isSaved = event.id in vm.savedIds,
                        isTopPick = EventRanker.isMatch(event, vm.profile.interests),
                        onOpen = { vm.push(Route.Detail(event.id)) },
                        onTogglePlan = { vm.togglePlan(event) },
                        onToggleSaved = { vm.toggleSaved(event.id) }
                    )
                    ChunkyButton(
                        "Check in to this shift", onClick = { vm.startShift(event); vm.push(Route.CheckIn) },
                        Modifier.fillMaxWidth(), style = ButtonStyle.Success, icon = Icons.Filled.PlayArrow
                    )
                }
            }
        }

        item { SectionTitle("Saved for later") }
        if (saved.isEmpty()) {
            item {
                EmptyState(
                    icon = Icons.Filled.BookmarkBorder, title = "Nothing saved",
                    body = "Tap the bookmark on a card to keep an idea for later."
                )
            }
        } else {
            items(saved, key = { "s" + it.id }) { event ->
                EventCard(
                    event = event, isPlanned = false, isSaved = true,
                    isTopPick = EventRanker.isMatch(event, vm.profile.interests),
                    onOpen = { vm.push(Route.Detail(event.id)) },
                    onTogglePlan = {
                        val willPlan = event.id !in vm.plannedIds
                        vm.togglePlan(event)
                        if (willPlan) {
                            try {
                                context.startActivity(
                                    Intent(Intent.ACTION_VIEW, Uri.parse(event.signupUrl))
                                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                )
                            } catch (_: Exception) {}
                        }
                    },
                    onToggleSaved = { vm.toggleSaved(event.id) }
                )
            }
        }

        item { SectionTitle("Finished") }
        if (done.isEmpty()) {
            item {
                Text(
                    "Verified shifts will appear here once a supervisor signs.",
                    style = MaterialTheme.typography.bodyMedium, color = h.inkSoft
                )
            }
        } else {
            items(done, key = { "d" + it.eventId + it.verifiedAtMillis }) { shift ->
                ChunkyCard {
                    Column(Modifier.padding(Spacing.lg), verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                        Row(
                            Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CausePill(shift.cause)
                            StatusPill("Verified", Icons.Filled.CheckCircle, h.successSoft, h.successDeep)
                        }
                        Text(shift.title, style = MaterialTheme.typography.titleMedium, color = h.ink, maxLines = 2)
                        Text(
                            "${shift.hours.hoursLabel()} hrs · ${shift.organizationName} · signed by ${shift.supervisorName}",
                            style = MaterialTheme.typography.bodySmall, color = h.inkSoft
                        )
                        ChunkyButton(
                            "Export / Xello 📋",
                            onClick = { vm.push(Route.Xello(shift.eventId)) },
                            modifier = Modifier.fillMaxWidth(),
                            style = ButtonStyle.Quiet
                        )
                    }
                }
            }
        }
    }
}
