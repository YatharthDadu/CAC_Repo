package com.example.volunteerhub.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.volunteerhub.Route
import com.example.volunteerhub.VolunteerViewModel
import com.example.volunteerhub.model.Cause
import com.example.volunteerhub.ui.components.BadgeShelf
import com.example.volunteerhub.ui.components.ButtonStyle
import com.example.volunteerhub.ui.components.ChunkyButton
import com.example.volunteerhub.ui.components.ChunkyCard
import com.example.volunteerhub.ui.components.IconAction
import com.example.volunteerhub.ui.components.SectionTitle
import com.example.volunteerhub.ui.components.SelectChip
import com.example.volunteerhub.ui.components.hoursLabel
import com.example.volunteerhub.ui.components.style
import com.example.volunteerhub.ui.theme.Hub
import com.example.volunteerhub.ui.theme.Radius
import com.example.volunteerhub.ui.theme.Spacing

private val grades = listOf("9th", "10th", "11th", "12th")
private val clubs = listOf("National Honor Society", "Key Club", "Graduation requirement", "Other")

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun ProfileScreen(vm: VolunteerViewModel) {
    val h = Hub.colors
    val p = vm.profile
    var confirmReset by remember { mutableStateOf(false) }

    LazyColumn(
        Modifier.fillMaxSize().statusBarsPadding(),
        contentPadding = PaddingValues(Spacing.lg),
        verticalArrangement = Arrangement.spacedBy(Spacing.lg)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Spacing.lg)) {
                Box(
                    Modifier.size(72.dp).background(MaterialTheme.colorScheme.primary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        p.fullName.trim().split(" ").filter { it.isNotBlank() }.take(2).joinToString("") { it.take(1).uppercase() }.ifBlank { "?" },
                        style = MaterialTheme.typography.headlineSmall, color = Color.White
                    )
                }
                Column {
                    Text(p.fullName.ifBlank { "Your profile" }, style = MaterialTheme.typography.headlineSmall, color = h.ink)
                    Text(
                        listOf(p.school, p.grade).filter { it.isNotBlank() }.joinToString(" · ").ifBlank { "Add your school below" },
                        style = MaterialTheme.typography.bodyMedium, color = h.inkSoft
                    )
                }
            }
        }

        item { SectionTitle("Badges") }
        item { BadgeShelf(vm.progress.badges) }

        item { SectionTitle("About you") }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
                ProfileField("Full name", p.fullName) { vm.saveProfile(p.copy(fullName = it)) }
                ProfileField("School", p.school) { vm.saveProfile(p.copy(school = it)) }
                Text("Grade", style = MaterialTheme.typography.titleSmall, color = h.ink)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(Spacing.sm), verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                    grades.forEach { g -> SelectChip(g, p.grade == g, { vm.saveProfile(p.copy(grade = g)) }) }
                }
            }
        }

        item { SectionTitle("What you care about") }
        item {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(Spacing.sm), verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                Cause.values().forEach { c ->
                    val s = c.style()
                    val on = c in p.interests
                    SelectChip(
                        c.label, on,
                        { vm.saveProfile(p.copy(interests = if (on) p.interests - c else p.interests + c)) },
                        icon = s.icon, accent = s.color, accentSoft = s.soft
                    )
                }
            }
        }

        item { SectionTitle("Your goal") }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(Spacing.sm), verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                    clubs.forEach { c -> SelectChip(c, p.targetClub == c, { vm.saveProfile(p.copy(targetClub = c)) }) }
                }
                Stepper("Hours needed", "${p.goalHours.hoursLabel()} hrs",
                    onMinus = { vm.saveProfile(p.copy(goalHours = (p.goalHours - 5).coerceAtLeast(5.0))) },
                    onPlus = { vm.saveProfile(p.copy(goalHours = (p.goalHours + 5).coerceAtMost(500.0))) })
                Stepper("Shifts per week", "${p.weeklyGoal}",
                    onMinus = { vm.saveProfile(p.copy(weeklyGoal = (p.weeklyGoal - 1).coerceAtLeast(1))) },
                    onPlus = { vm.saveProfile(p.copy(weeklyGoal = (p.weeklyGoal + 1).coerceAtMost(7))) })
            }
        }

        item {
            ChunkyButton(
                "Service transcript", { vm.push(Route.Transcript) }, Modifier.fillMaxWidth(),
                ButtonStyle.Quiet, Icons.Filled.Description
            )
        }
        item {
            ChunkyButton("Reset all my data", { confirmReset = true }, Modifier.fillMaxWidth(), ButtonStyle.Quiet)
        }
    }

    if (confirmReset) {
        AlertDialog(
            onDismissRequest = { confirmReset = false },
            title = { Text("Reset everything?") },
            text = { Text("This deletes your profile, plan and verified hours from this device. It can't be undone.") },
            confirmButton = {
                TextButton(onClick = { confirmReset = false; vm.resetEverything() }) { Text("Delete everything") }
            },
            dismissButton = { TextButton(onClick = { confirmReset = false }) { Text("Keep my data") } }
        )
    }
}

@Composable
private fun ProfileField(label: String, value: String, onChange: (String) -> Unit) {
    val h = Hub.colors
    OutlinedTextField(
        value = value, onValueChange = onChange, label = { Text(label) }, singleLine = true,
        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(Radius.button),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary, unfocusedBorderColor = h.line,
            focusedContainerColor = h.card, unfocusedContainerColor = h.card,
            focusedTextColor = h.ink, unfocusedTextColor = h.ink,
            focusedLabelColor = MaterialTheme.colorScheme.primary, unfocusedLabelColor = h.inkSoft
        )
    )
}

@Composable
private fun Stepper(label: String, value: String, onMinus: () -> Unit, onPlus: () -> Unit) {
    val h = Hub.colors
    ChunkyCard {
        Row(Modifier.fillMaxWidth().padding(Spacing.md), verticalAlignment = Alignment.CenterVertically) {
            Text(label, style = MaterialTheme.typography.titleSmall, color = h.ink, modifier = Modifier.weight(1f))
            IconAction(Icons.Filled.Remove, "Decrease $label", onMinus, tint = MaterialTheme.colorScheme.primary)
            Text(value, style = MaterialTheme.typography.titleMedium, color = h.ink, modifier = Modifier.width(72.dp), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            IconAction(Icons.Filled.Add, "Increase $label", onPlus, tint = MaterialTheme.colorScheme.primary)
        }
    }
}
