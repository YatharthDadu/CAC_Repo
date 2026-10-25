package com.example.volunteerhub.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import com.example.volunteerhub.model.Cause
import com.example.volunteerhub.model.UserProfile
import com.example.volunteerhub.ui.components.ButtonStyle
import com.example.volunteerhub.ui.components.ChunkyButton
import com.example.volunteerhub.ui.components.SelectChip
import com.example.volunteerhub.ui.components.style
import com.example.volunteerhub.ui.theme.Coral
import com.example.volunteerhub.ui.theme.Hub
import com.example.volunteerhub.ui.theme.Mint
import com.example.volunteerhub.ui.theme.Radius
import com.example.volunteerhub.ui.theme.Sky
import com.example.volunteerhub.ui.theme.Spacing
import com.example.volunteerhub.ui.theme.Sun
import com.example.volunteerhub.ui.theme.Violet

private val gradeOptions = listOf("9th", "10th", "11th", "12th")

/** Three short steps: welcome, who you are, what you care about. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OnboardingScreen(onDone: (UserProfile) -> Unit) {
    val h = Hub.colors
    var step by rememberSaveable { mutableStateOf(0) }
    var name by rememberSaveable { mutableStateOf("") }
    var school by rememberSaveable { mutableStateOf("") }
    var grade by rememberSaveable { mutableStateOf("") }
    var picked by rememberSaveable { mutableStateOf(listOf<String>()) }
    var triedNext by rememberSaveable { mutableStateOf(false) }

    BackHandler(enabled = step > 0) { step -= 1; triedNext = false }

    Column(Modifier.fillMaxSize().background(h.canvas).statusBarsPadding().navigationBarsPadding().imePadding()) {
        if (step == 0) {
            Welcome(onStart = { step = 1 })
            return@Column
        }

        // Progress dots
        Row(
            Modifier.fillMaxWidth().padding(Spacing.lg),
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
        ) {
            (1..2).forEach { i ->
                Box(
                    Modifier.weight(1f).height(10.dp)
                        .background(if (i <= step) MaterialTheme.colorScheme.primary else h.line, RoundedCornerShape(50))
                )
            }
        }

        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Spacing.lg)
        ) {
            if (step == 1) {
                Text("First, who are you?", style = MaterialTheme.typography.headlineMedium, color = h.ink)
                Text("We use this to personalize your feed. It stays on your phone.", style = MaterialTheme.typography.bodyMedium, color = h.inkSoft)
                OutlinedTextField(
                    value = name, onValueChange = { name = it }, label = { Text("Your name") },
                    singleLine = true, isError = triedNext && name.isBlank(),
                    supportingText = { if (triedNext && name.isBlank()) Text("We need a name for your transcript") },
                    modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(Radius.button),
                    colors = fieldColors()
                )
                OutlinedTextField(
                    value = school, onValueChange = { school = it }, label = { Text("School (optional)") },
                    singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(Radius.button),
                    colors = fieldColors()
                )
                Text("Grade", style = MaterialTheme.typography.titleSmall, color = h.ink)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(Spacing.sm), verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                    gradeOptions.forEach { g -> SelectChip(g, grade == g, { grade = g }) }
                }
            } else {
                Text("What gets you fired up?", style = MaterialTheme.typography.headlineMedium, color = h.ink)
                Text(
                    "Pick at least one. We'll put matching shifts first.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (triedNext && picked.isEmpty()) h.urgentDeep else h.inkSoft
                )
                FlowRow(horizontalArrangement = Arrangement.spacedBy(Spacing.sm), verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                    Cause.values().forEach { c ->
                        val s = c.style()
                        val on = c.name in picked
                        SelectChip(
                            c.label, on, { picked = if (on) picked - c.name else picked + c.name },
                            icon = s.icon, accent = s.color, accentSoft = s.soft
                        )
                    }
                }
            }
        }

        Column(Modifier.padding(Spacing.lg)) {
            ChunkyButton(
                text = if (step == 1) "Next" else "Start exploring",
                onClick = {
                    triedNext = true
                    if (step == 1 && name.isNotBlank()) { step = 2; triedNext = false }
                    else if (step == 2 && picked.isNotEmpty()) {
                        onDone(
                            UserProfile(
                                fullName = name.trim(), school = school.trim(), grade = grade,
                                interests = picked.mapNotNull { n -> Cause.values().firstOrNull { it.name == n } }.toSet()
                            )
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                icon = if (step == 1) Icons.AutoMirrored.Filled.ArrowForward else Icons.Filled.Check
            )
        }
    }
}

@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = MaterialTheme.colorScheme.primary, unfocusedBorderColor = Hub.colors.line,
    focusedContainerColor = Hub.colors.card, unfocusedContainerColor = Hub.colors.card,
    focusedTextColor = Hub.colors.ink, unfocusedTextColor = Hub.colors.ink,
    focusedLabelColor = MaterialTheme.colorScheme.primary, unfocusedLabelColor = Hub.colors.inkSoft
)

@Composable
private fun Welcome(onStart: () -> Unit) {
    val h = Hub.colors
    Column(Modifier.fillMaxSize()) {
        // Decorative hero: chunky tilted shapes. Purely visual, hidden from screen readers.
        Box(Modifier.weight(1f).fillMaxWidth()) {
            Canvas(Modifier.fillMaxSize()) {
                val w = size.width
                val hgt = size.height
                rotate(-12f, Offset(w * 0.3f, hgt * 0.4f)) {
                    drawRoundRect(Violet, Offset(w * 0.08f, hgt * 0.18f), Size(w * 0.46f, hgt * 0.36f), CornerRadius(48f))
                }
                rotate(14f, Offset(w * 0.7f, hgt * 0.3f)) {
                    drawRoundRect(Sun, Offset(w * 0.52f, hgt * 0.1f), Size(w * 0.36f, hgt * 0.3f), CornerRadius(48f))
                }
                drawCircle(Mint, radius = w * 0.17f, center = Offset(w * 0.72f, hgt * 0.62f))
                drawCircle(Coral, radius = w * 0.1f, center = Offset(w * 0.22f, hgt * 0.72f))
                rotate(25f, Offset(w * 0.5f, hgt * 0.5f)) {
                    drawRoundRect(Sky, Offset(w * 0.4f, hgt * 0.42f), Size(w * 0.2f, w * 0.2f), CornerRadius(32f))
                }
            }
        }
        Column(Modifier.padding(Spacing.lg), verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
            Text("Volunteer\nsmarter.", style = MaterialTheme.typography.displayMedium, color = h.ink)
            Text(
                "Every local shift for students, in one place. Find it, plan it, get it signed, and watch your hours add up.",
                style = MaterialTheme.typography.bodyLarge, color = h.inkSoft
            )
            ChunkyButton("Let's go", onStart, Modifier.fillMaxWidth(), ButtonStyle.Primary, Icons.AutoMirrored.Filled.ArrowForward)
        }
    }
}
