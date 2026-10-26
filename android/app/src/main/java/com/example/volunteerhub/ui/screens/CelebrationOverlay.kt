package com.example.volunteerhub.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.volunteerhub.Celebration
import com.example.volunteerhub.ui.components.BadgeTile
import com.example.volunteerhub.ui.components.ButtonStyle
import com.example.volunteerhub.ui.components.ChunkyButton
import com.example.volunteerhub.ui.components.ChunkyCard
import com.example.volunteerhub.ui.components.hoursLabel
import com.example.volunteerhub.ui.components.rememberReduceMotion
import com.example.volunteerhub.ui.theme.Coral
import com.example.volunteerhub.ui.theme.Hub
import com.example.volunteerhub.ui.theme.Mint
import com.example.volunteerhub.ui.theme.Sky
import com.example.volunteerhub.ui.theme.Spacing
import com.example.volunteerhub.ui.theme.Sun
import com.example.volunteerhub.ui.theme.Violet

private val confettiColors = listOf(Sun, Coral, Mint, Sky, Violet)

@Composable
fun CelebrationOverlay(
    c: Celebration,
    onDismiss: () -> Unit,
    onPrepareXello: (String) -> Unit = {}
) {
    val h = Hub.colors
    val reduce = rememberReduceMotion()
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Box(Modifier.fillMaxSize().padding(Spacing.lg), contentAlignment = Alignment.Center) {
            ChunkyCard(Modifier.fillMaxWidth(), face = h.card) {
                Box {
                    if (!reduce) Confetti(Modifier.fillMaxWidth().height(260.dp))
                    Column(
                        Modifier.fillMaxWidth().padding(Spacing.xl),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(Spacing.md)
                    ) {
                        Icon(Icons.Filled.EmojiEvents, null, tint = h.reward, modifier = Modifier.size(72.dp))
                        Text(
                            if (c.goalReached) "Goal crushed!" else "Shift verified!",
                            style = MaterialTheme.typography.headlineMedium, color = h.ink, textAlign = TextAlign.Center
                        )
                        Text(
                            "+${c.hours.hoursLabel()} verified hours",
                            style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary
                        )
                        Text(c.title, style = MaterialTheme.typography.bodyMedium, color = h.inkSoft, textAlign = TextAlign.Center)
                        if (c.newBadges.isNotEmpty()) {
                            Text("New badges", style = MaterialTheme.typography.titleSmall, color = h.ink)
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
                                items(c.newBadges) { BadgeTile(it, earned = true) }
                            }
                        }
                        if (c.eventId.isNotBlank()) {
                            ChunkyButton("Prepare Xello Submission 📋", {
                                onDismiss()
                                onPrepareXello(c.eventId)
                            }, Modifier.fillMaxWidth(), ButtonStyle.Primary)
                        }
                        ChunkyButton("Keep going", onDismiss, Modifier.fillMaxWidth(), ButtonStyle.Quiet)
                    }
                }
            }
        }
    }
}

/** A short, looping burst of falling confetti. Skipped entirely when animations are off. */
@Composable
private fun Confetti(modifier: Modifier) {
    val t = rememberInfiniteTransition(label = "confetti")
    val phase by t.animateFloat(0f, 1f, infiniteRepeatable(tween(2600, easing = LinearEasing), RepeatMode.Restart), label = "phase")
    Canvas(modifier) {
        for (i in 0 until 28) {
            val seed = (i * 37 % 100) / 100f
            val x = size.width * ((i * 53 % 100) / 100f)
            val y = ((phase + seed) % 1f) * size.height
            drawRect(
                confettiColors[i % confettiColors.size],
                Offset(x, y), Size(10f + (i % 3) * 4f, 16f)
            )
        }
    }
}
