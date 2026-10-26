package com.example.volunteerhub.ui.screens

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.volunteerhub.VolunteerViewModel
import com.example.volunteerhub.model.Dates
import com.example.volunteerhub.ui.components.AppTopBar
import com.example.volunteerhub.ui.components.ButtonStyle
import com.example.volunteerhub.ui.components.ChunkyButton
import com.example.volunteerhub.ui.components.ChunkyCard
import com.example.volunteerhub.ui.components.EmptyState
import com.example.volunteerhub.ui.components.hoursLabel
import com.example.volunteerhub.ui.theme.Hub
import com.example.volunteerhub.ui.theme.Spacing

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import com.example.volunteerhub.Route
import com.example.volunteerhub.data.PdfGenerator

@Composable
fun TranscriptScreen(vm: VolunteerViewModel) {
    val h = Hub.colors
    val context = LocalContext.current
    val shifts = vm.shifts.sortedBy { it.verifiedAtMillis }
    val total = shifts.sumOf { it.hours }

    Column(Modifier.fillMaxSize().statusBarsPadding()) {
        AppTopBar("Service transcript", onBack = { vm.back() })
        if (shifts.isEmpty()) {
            EmptyState(
                icon = Icons.Filled.Description, title = "No verified hours yet",
                body = "Finish a shift and get it signed to build your transcript."
            )
            return@Column
        }
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            ChunkyCard {
                Column(Modifier.padding(Spacing.lg), verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                    Text(vm.profile.fullName.ifBlank { "Student" }, style = MaterialTheme.typography.headlineSmall, color = h.ink)
                    Text(
                        listOf(vm.profile.school, vm.profile.grade).filter { it.isNotBlank() }.joinToString(" · "),
                        style = MaterialTheme.typography.bodyMedium, color = h.inkSoft
                    )
                    Text("${total.hoursLabel()} verified hours", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
                }
            }
            shifts.forEach { s ->
                ChunkyCard {
                    Column(Modifier.padding(Spacing.lg), verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(s.title, style = MaterialTheme.typography.titleSmall, color = h.ink, modifier = Modifier.weight(1f))
                            Text("${s.hours.hoursLabel()} hrs", style = MaterialTheme.typography.titleSmall, color = h.successDeep)
                        }
                        Text("${s.organizationName} · ${Dates.pretty(s.eventDate)}", style = MaterialTheme.typography.bodySmall, color = h.inkSoft)
                        Text("Signed by ${s.supervisorName} (${s.supervisorEmail})", style = MaterialTheme.typography.bodySmall, color = h.inkSoft)
                        Row(
                            Modifier.fillMaxWidth().padding(top = Spacing.xs),
                            horizontalArrangement = Arrangement.End
                        ) {
                            ChunkyButton(
                                "Xello / AI Summary 📋",
                                onClick = { vm.push(Route.Xello(s.eventId)) },
                                style = ButtonStyle.Quiet
                            )
                        }
                    }
                }
            }
            Text(
                "Signatures are collected on this device. Your school may confirm hours directly with each supervisor.",
                style = MaterialTheme.typography.bodySmall, color = h.inkSoft
            )
        }
        Column(Modifier.padding(Spacing.lg).navigationBarsPadding(), verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            ChunkyButton(
                "📄 Generate Verified Resume (PDF)",
                icon = Icons.Filled.Description,
                style = ButtonStyle.Reward,
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    PdfGenerator.shareTranscriptPdf(context, vm.profile, shifts)
                }
            )
            ChunkyButton(
                "Share plain text",
                icon = Icons.Filled.Share,
                style = ButtonStyle.Quiet,
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    val text = buildString {
                        appendLine("Volunteer service transcript — ${vm.profile.fullName}")
                        appendLine("Total verified hours: ${total.hoursLabel()}")
                        appendLine()
                        shifts.forEach {
                            appendLine("• ${it.title} — ${it.organizationName}, ${Dates.pretty(it.eventDate)}: ${it.hours.hoursLabel()} hrs (signed by ${it.supervisorName}, ${it.supervisorEmail})")
                        }
                    }
                    val send = Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, text)
                    context.startActivity(Intent.createChooser(send, "Share transcript").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                }
            )
        }
    }
}
