package com.example.volunteerhub.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.ui.semantics.Role
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.volunteerhub.Route
import com.example.volunteerhub.VolunteerViewModel
import com.example.volunteerhub.model.Dates
import com.example.volunteerhub.model.VolunteerEvent
import com.example.volunteerhub.ui.components.AppTopBar
import com.example.volunteerhub.ui.components.ButtonStyle
import com.example.volunteerhub.ui.components.ChunkyButton
import com.example.volunteerhub.ui.components.ChunkyCard
import com.example.volunteerhub.ui.components.CausePill
import com.example.volunteerhub.ui.components.EmptyState
import com.example.volunteerhub.ui.components.XpBadge
import com.example.volunteerhub.ui.components.hoursLabel
import com.example.volunteerhub.ui.components.style
import com.example.volunteerhub.ui.theme.Hub
import com.example.volunteerhub.ui.theme.Spacing

@Composable
fun DetailScreen(vm: VolunteerViewModel, eventId: String) {
    val h = Hub.colors
    val event = vm.eventById(eventId)
    val context = LocalContext.current

    Column(Modifier.fillMaxSize().statusBarsPadding()) {
        AppTopBar("Opportunity", onBack = { vm.back() })
        if (event == null) {
            EmptyState(
                icon = Icons.Filled.Warning, title = "This shift is gone",
                body = "It may have been removed from the feed.",
                actionLabel = "Back", onAction = { vm.back() }
            )
            return@Column
        }
        val s = event.cause.style()
        val planned = event.id in vm.plannedIds
        val saved = event.id in vm.savedIds
        val verified = vm.shifts.any { it.eventId == event.id }

        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Spacing.lg)
        ) {
            ChunkyCard(face = s.soft, lip = s.color) {
                Column(Modifier.padding(Spacing.lg), verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm), verticalAlignment = Alignment.CenterVertically) {
                        CausePill(event.cause)
                        XpBadge("${event.durationHours.hoursLabel()} hrs credit")
                    }
                    Text(event.title, style = MaterialTheme.typography.headlineSmall, color = h.ink)
                    Text(event.organizationName, style = MaterialTheme.typography.titleSmall, color = s.deep)
                }
            }

            ChunkyCard {
                Column(Modifier.padding(Spacing.lg), verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
                    Fact(Icons.Filled.Event, "When", Dates.pretty(event.eventDate) +
                        (if (event.timeLabel.isNotBlank()) " · ${event.timeLabel}" else ""))
                    Fact(Icons.Filled.Place, "Where", event.location.ifBlank { "Location on the sign-up page" })
                    Fact(Icons.Filled.Schedule, "Time commitment", "About ${event.durationHours.hoursLabel()} hours")
                    Fact(Icons.Filled.Business, "Organization", event.organizationName)
                }
            }

            if (event.isEstimatedDate) {
                Row(
                    Modifier.fillMaxWidth().background(h.rewardSoft, androidx.compose.foundation.shape.RoundedCornerShape(16.dp)).padding(Spacing.md),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.Warning, null, tint = h.rewardDeep)
                    Spacer(Modifier.width(Spacing.md))
                    Text(
                        "The source didn't list an exact date. Confirm it on the sign-up page before you go.",
                        style = MaterialTheme.typography.bodySmall, color = h.ink
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                Text("About this shift", style = MaterialTheme.typography.titleLarge, color = h.ink)
                Text(
                    event.description.ifBlank { "The organizer hasn't added details. Check the sign-up page for more." },
                    style = MaterialTheme.typography.bodyLarge, color = h.ink
                )
            }
            Spacer(Modifier.size(Spacing.sm))
        }

        Column(
            Modifier.fillMaxWidth().background(h.card).padding(Spacing.lg).navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm)
        ) {
            val platformLabel = event.sourcePlatform.ifBlank { event.organizationName.ifBlank { "organizer site" } }
            ChunkyButton(
                text = "Sign up & reserve spot on $platformLabel",
                onClick = {
                    if (!planned) vm.togglePlan(event)
                    openUrl(context, event.signupUrl)
                },
                modifier = Modifier.fillMaxWidth(),
                style = ButtonStyle.Primary,
                icon = Icons.AutoMirrored.Filled.OpenInNew
            )
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm), verticalAlignment = Alignment.CenterVertically) {
                when {
                    verified -> ChunkyButton("Export / Xello 📋", { vm.push(Route.Xello(event.id)) }, Modifier.weight(1f), ButtonStyle.Primary)
                    planned -> ChunkyButton(
                        "Check in to shift", { vm.startShift(event); vm.push(Route.CheckIn) },
                        Modifier.weight(1f), ButtonStyle.Success, Icons.Filled.PlayArrow
                    )
                    else -> ChunkyButton(
                        "Add to my plan",
                        { vm.togglePlan(event) },
                        Modifier.weight(1f),
                        style = ButtonStyle.Quiet,
                        icon = Icons.Filled.Bookmark
                    )
                }
                com.example.volunteerhub.ui.components.IconAction(
                    icon = if (saved) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                    label = if (saved) "Remove from saved" else "Save for later",
                    onClick = { vm.toggleSaved(event.id) },
                    tint = if (saved) MaterialTheme.colorScheme.primary else h.inkSoft
                )
            }
            if (planned && !verified) {
                Text(
                    "Remove from plan",
                    style = MaterialTheme.typography.labelMedium, color = h.inkSoft,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .clickable(role = Role.Button) { vm.togglePlan(event) }
                        .padding(Spacing.sm)
                )
            }
        }
    }
}

@Composable
private fun Fact(icon: ImageVector, label: String, value: String) {
    val h = Hub.colors
    Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = h.inkSoft)
            Text(value, style = MaterialTheme.typography.bodyMedium, color = h.ink)
        }
    }
}

private fun openUrl(context: android.content.Context, url: String) {
    android.util.Log.d("VolunteerHub", "openUrl called with: '$url'")
    try {
        val uri = Uri.parse(url)
        val intent = Intent(Intent.ACTION_VIEW, uri).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    } catch (e: Exception) {
        android.util.Log.e("VolunteerHub", "openUrl failed", e)
    }
}
