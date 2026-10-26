package com.example.volunteerhub.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Draw
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.volunteerhub.Route
import com.example.volunteerhub.VolunteerViewModel
import com.example.volunteerhub.ui.components.AppTopBar
import com.example.volunteerhub.ui.components.ButtonStyle
import com.example.volunteerhub.ui.components.ChunkyButton
import com.example.volunteerhub.ui.components.ChunkyCard
import com.example.volunteerhub.ui.components.EmptyState
import com.example.volunteerhub.ui.components.hoursLabel
import com.example.volunteerhub.ui.theme.Hub
import com.example.volunteerhub.ui.theme.Radius
import com.example.volunteerhub.ui.theme.Spacing
import kotlinx.coroutines.delay

/** Check-in: pick a planned shift, run the timer, then hand the phone to the supervisor. */
@Composable
fun CheckInScreen(vm: VolunteerViewModel) {
    val h = Hub.colors
    val active = vm.activeShift
    val event = active?.let { vm.eventById(it.eventId) }

    Column(Modifier.fillMaxSize().statusBarsPadding()) {
        AppTopBar("Check in", onBack = { vm.back() })

        if (active == null || event == null) {
            val planned = vm.plannedEvents
            if (planned.isEmpty()) {
                EmptyState(
                    icon = Icons.Filled.EventAvailable, title = "No shift to check in to",
                    body = "Add a shift to your plan first, then come back on the day.",
                    actionLabel = "Find a shift", onAction = { vm.openTab(Route.Discover) }
                )
            } else {
                Column(
                    Modifier.padding(Spacing.lg).verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(Spacing.md)
                ) {
                    Text("Which shift are you starting?", style = MaterialTheme.typography.titleLarge, color = h.ink)
                    planned.forEach { e ->
                        ChunkyCard(onClick = { vm.startShift(e) }, onClickLabel = "Start ${e.title}") {
                            Row(Modifier.padding(Spacing.lg), verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f)) {
                                    Text(e.title, style = MaterialTheme.typography.titleMedium, color = h.ink, maxLines = 2)
                                    Text(e.organizationName, style = MaterialTheme.typography.bodySmall, color = h.inkSoft)
                                }
                                androidx.compose.material3.Icon(Icons.Filled.PlayArrow, null, tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                    Text(
                        "QR check-in with your supervisor is coming soon. For now, your supervisor signs when you finish.",
                        style = MaterialTheme.typography.bodySmall, color = h.inkSoft
                    )
                }
            }
            return@Column
        }

        var now by remember { mutableStateOf(System.currentTimeMillis()) }
        LaunchedEffect(active.startedAtMillis) {
            while (true) {
                now = System.currentTimeMillis()
                delay(1000)
            }
        }
        val elapsed = ((now - active.startedAtMillis) / 1000).coerceAtLeast(0)

        Column(
            Modifier.fillMaxSize().padding(Spacing.lg).navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(Spacing.lg)
        ) {
            ChunkyCard(face = MaterialTheme.colorScheme.primary, lip = h.brandDeep) {
                Column(
                    Modifier.fillMaxWidth().padding(Spacing.xl),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(Spacing.sm)
                ) {
                    Text("Shift in progress", style = MaterialTheme.typography.labelLarge, color = h.reward)
                    Text(
                        "%02d:%02d:%02d".format(elapsed / 3600, (elapsed % 3600) / 60, elapsed % 60),
                        style = MaterialTheme.typography.displayMedium, color = androidx.compose.ui.graphics.Color.White
                    )
                    Text(
                        event.title, style = MaterialTheme.typography.titleMedium,
                        color = androidx.compose.ui.graphics.Color.White, maxLines = 2
                    )
                    Text(event.organizationName, style = MaterialTheme.typography.bodyMedium, color = androidx.compose.ui.graphics.Color(0xFFE5DBFF))
                }
            }
            Text(
                "When you're done, hand your phone to your supervisor. They sign to confirm " +
                    "${event.durationHours.hoursLabel()} hours. Hours only count after that signature.",
                style = MaterialTheme.typography.bodyMedium, color = h.inkSoft
            )
            Box(Modifier.weight(1f))
            ChunkyButton("End shift & get signature", { vm.push(Route.Signature) }, Modifier.fillMaxWidth(), ButtonStyle.Reward, Icons.Filled.Stop)
            ChunkyButton("Cancel shift", { vm.cancelShift() }, Modifier.fillMaxWidth(), ButtonStyle.Quiet)
        }
    }
}

@Composable
fun SignatureScreen(vm: VolunteerViewModel) {
    val h = Hub.colors
    val event = vm.activeShift?.let { vm.eventById(it.eventId) }
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("Site Coordinator") }
    var strokes by remember { mutableStateOf(listOf<List<Offset>>()) }
    var current by remember { mutableStateOf(listOf<Offset>()) }
    var triedSubmit by remember { mutableStateOf(false) }

    val nameOk = name.isNotBlank()
    val emailOk = email.contains("@") && email.contains(".")
    val signed = strokes.isNotEmpty()

    Column(Modifier.fillMaxSize().statusBarsPadding().imePadding()) {
        AppTopBar("Supervisor sign-off", onBack = { vm.back() })
        if (event == null) {
            EmptyState(
                icon = Icons.Filled.EventAvailable, title = "No shift to sign",
                body = "Start a shift first.", actionLabel = "Back", onAction = { vm.back() }
            )
            return@Column
        }
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            Text(
                "Supervisor: please confirm that this student volunteered for ${event.durationHours.hoursLabel()} hours at ${event.organizationName}.",
                style = MaterialTheme.typography.bodyLarge, color = h.ink
            )
            SignField("Supervisor name", name, { name = it }, triedSubmit && !nameOk, "Enter the supervisor's name", KeyboardType.Text)
            SignField("Supervisor email", email, { email = it }, triedSubmit && !emailOk, "Enter a valid email", KeyboardType.Email)
            SignField("Supervisor role / title", role, { role = it }, false, "", KeyboardType.Text)

            Text("Signature", style = MaterialTheme.typography.titleSmall, color = h.ink)
            val shape = RoundedCornerShape(Radius.card)
            val strokeColor = h.ink
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .background(h.card, shape)
                    .border(2.dp, if (triedSubmit && !signed) h.urgent else h.line, shape)
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { current = listOf(it) },
                            onDrag = { change, _ ->
                                change.consume()
                                current = current + change.position
                            },
                            onDragEnd = {
                                if (current.size > 1) strokes = strokes + listOf(current)
                                current = emptyList()
                            },
                            onDragCancel = { current = emptyList() }
                        )
                    }
            ) {
                Canvas(Modifier.fillMaxSize()) {
                    (strokes + listOf(current)).forEach { pts ->
                        if (pts.size > 1) {
                            val path = Path().apply {
                                moveTo(pts.first().x, pts.first().y)
                                pts.drop(1).forEach { lineTo(it.x, it.y) }
                            }
                            drawPath(path, strokeColor, style = Stroke(5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
                        }
                    }
                }
                if (!signed && current.isEmpty()) {
                    Text(
                        "Sign here with your finger", style = MaterialTheme.typography.bodyMedium,
                        color = h.inkSoft, modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
            if (triedSubmit && !signed) {
                Text("A signature is required.", style = MaterialTheme.typography.bodySmall, color = h.urgentDeep)
            }
            ChunkyButton("Clear signature", { strokes = emptyList(); current = emptyList() }, Modifier.fillMaxWidth(), ButtonStyle.Quiet, Icons.Filled.Draw)
        }
        Column(Modifier.padding(Spacing.lg).navigationBarsPadding()) {
            ChunkyButton(
                "Verify ${event.durationHours.hoursLabel()} hours",
                onClick = {
                    triedSubmit = true
                    if (nameOk && emailOk && signed) {
                        val sigSvg = strokes.joinToString(" ") { stroke ->
                            if (stroke.isEmpty()) ""
                            else "M ${stroke.first().x.toInt()} ${stroke.first().y.toInt()} " + stroke.drop(1).joinToString(" ") { "L ${it.x.toInt()} ${it.y.toInt()}" }
                        }
                        vm.verifyShift(name, email, role, sigSvg)
                    }
                },
                modifier = Modifier.fillMaxWidth(), style = ButtonStyle.Success, icon = Icons.Filled.Verified
            )
        }
    }
}

@Composable
private fun SignField(
    label: String, value: String, onChange: (String) -> Unit,
    isError: Boolean, errorText: String, keyboard: KeyboardType
) {
    val h = Hub.colors
    OutlinedTextField(
        value = value, onValueChange = onChange, modifier = Modifier.fillMaxWidth(),
        label = { Text(label) }, singleLine = true, isError = isError,
        supportingText = { if (isError) Text(errorText) },
        keyboardOptions = KeyboardOptions(keyboardType = keyboard),
        shape = RoundedCornerShape(Radius.button),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary, unfocusedBorderColor = h.line,
            focusedContainerColor = h.card, unfocusedContainerColor = h.card,
            focusedTextColor = h.ink, unfocusedTextColor = h.ink,
            focusedLabelColor = MaterialTheme.colorScheme.primary, unfocusedLabelColor = h.inkSoft
        )
    )
}
