package com.example.volunteerhub.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.HapticFeedbackConstants
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Draw
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.volunteerhub.VolunteerViewModel
import com.example.volunteerhub.data.AiReflectionEngine
import com.example.volunteerhub.data.PdfGenerator
import com.example.volunteerhub.data.StarReflection
import com.example.volunteerhub.model.Dates
import com.example.volunteerhub.model.ShiftRecord
import com.example.volunteerhub.ui.components.AppTopBar
import com.example.volunteerhub.ui.components.ButtonStyle
import com.example.volunteerhub.ui.components.CausePill
import com.example.volunteerhub.ui.components.ChunkyButton
import com.example.volunteerhub.ui.components.ChunkyCard
import com.example.volunteerhub.ui.components.EmptyState
import com.example.volunteerhub.ui.components.SectionTitle
import com.example.volunteerhub.ui.components.StatusPill
import com.example.volunteerhub.ui.components.XpBadge
import com.example.volunteerhub.ui.components.hoursLabel
import com.example.volunteerhub.ui.components.rememberReduceMotion
import com.example.volunteerhub.ui.components.style
import com.example.volunteerhub.ui.theme.Hub
import com.example.volunteerhub.ui.theme.Radius
import com.example.volunteerhub.ui.theme.Spacing

private enum class ReflectionMode(val title: String) {
    COMMON_APP("Common App (≤ 150 chars)"),
    XELLO_ESSAY("Xello / Essay Mode")
}

@Composable
fun XelloScreen(vm: VolunteerViewModel, eventId: String) {
    val h = Hub.colors
    val context = LocalContext.current
    val view = LocalView.current
    val reduce = rememberReduceMotion()

    val shift = vm.shifts.firstOrNull { it.eventId == eventId }
        ?: vm.shifts.lastOrNull()

    if (shift == null) {
        Column(Modifier.fillMaxSize().statusBarsPadding()) {
            AppTopBar("Xello Service Bridge", onBack = { vm.back() })
            EmptyState(
                icon = Icons.Filled.Description,
                title = "No verified shift found",
                body = "Complete and verify a volunteer shift with your supervisor to export it to Xello.",
                actionLabel = "Back to my plan",
                onAction = { vm.back() }
            )
        }
        return
    }

    var selectedMode by remember { mutableStateOf(ReflectionMode.COMMON_APP) }
    var variantIndex by remember { mutableIntStateOf(0) }
    val currentReflection = AiReflectionEngine.getVariant(shift, variantIndex)

    val copyToClipboard: (String, String) -> Unit = { label, text ->
        val clipboard = context.getSystemService(ClipboardManager::class.java)
        clipboard?.setPrimaryClip(ClipData.newPlainText(label, text))
        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
        Toast.makeText(context, "Copied $label for Xello!", Toast.LENGTH_SHORT).show()
    }

    Column(Modifier.fillMaxSize().statusBarsPadding()) {
        AppTopBar("Xello Fast-Copy Bridge", onBack = { vm.back() })

        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Spacing.lg)
        ) {
            // A. Verified Activity Header Card
            VerifiedHeaderCard(shift = shift, vm = vm)

            // B. The Xello Fast-Copy Bridge (Copy Drawer)
            XelloCopyDrawer(
                shift = shift,
                reflection = currentReflection,
                onCopy = copyToClipboard
            )

            // C. AI Impact Reflection Engine (STAR Method)
            AiReflectionBox(
                reflection = currentReflection,
                selectedMode = selectedMode,
                onModeSelected = { selectedMode = it },
                onCycle = { variantIndex++ },
                onCopy = copyToClipboard,
                reduce = reduce
            )

            // D. Official Service Transcript PDF Export (The Ultimate Fallback)
            PdfExportBox(
                shift = shift,
                vm = vm,
                context = context
            )

            Spacer(Modifier.height(Spacing.xl))
        }
    }
}

@Composable
private fun VerifiedHeaderCard(shift: ShiftRecord, vm: VolunteerViewModel) {
    val h = Hub.colors
    val s = shift.cause.style()
    val progress = vm.progress

    ChunkyCard(face = s.soft, lip = s.color) {
        Column(Modifier.padding(Spacing.lg), verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CausePill(shift.cause)
                StatusPill("VERIFIED ✓", Icons.Filled.Verified, h.successSoft, h.successDeep)
            }

            Text(shift.title, style = MaterialTheme.typography.titleLarge, color = h.ink)
            Text(shift.organizationName, style = MaterialTheme.typography.titleSmall, color = s.deep)

            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm), verticalAlignment = Alignment.CenterVertically) {
                XpBadge("${shift.hours.hoursLabel()} Hours")
                StatusPill(
                    text = "✍️ Signed by ${shift.supervisorName} (${shift.supervisorRole})",
                    icon = Icons.Filled.Draw,
                    container = h.brandSoft,
                    content = h.brandDeep
                )
            }

            // NHS Sync Indicator
            Box(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White.copy(alpha = 0.85f))
                    .padding(horizontal = Spacing.md, vertical = Spacing.sm)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.School,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(Spacing.sm))
                    Text(
                        text = "+${shift.hours.hoursLabel()} hrs synced to ${vm.profile.targetClub} (${progress.verifiedHours.hoursLabel()}/${progress.goalHours.hoursLabel()} hrs target)",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                        color = h.ink
                    )
                }
            }
        }
    }
}

@Composable
private fun XelloCopyDrawer(
    shift: ShiftRecord,
    reflection: StarReflection,
    onCopy: (String, String) -> Unit
) {
    val h = Hub.colors

    Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        SectionTitle("Xello Form Fields (1-Tap Copy)", trailing = {
            Text("Mapped 1:1", style = MaterialTheme.typography.labelSmall, color = h.inkSoft)
        })

        ChunkyCard {
            Column(Modifier.padding(Spacing.md), verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                CopyFieldRow(
                    label = "1. Organization Name",
                    value = shift.organizationName,
                    copyPayload = shift.organizationName,
                    icon = Icons.Filled.Work,
                    onCopy = { onCopy("Organization Name", shift.organizationName) }
                )

                CopyFieldRow(
                    label = "2. Hours of Service",
                    value = "${shift.hours.hoursLabel()} Hours  •  Date: ${Dates.pretty(shift.eventDate)}",
                    copyPayload = shift.hours.hoursLabel(),
                    icon = Icons.Filled.Schedule,
                    onCopy = { onCopy("Hours of Service", shift.hours.hoursLabel()) }
                )

                CopyFieldRow(
                    label = "3. Supervisor Contact & Role",
                    value = "${shift.supervisorName} (${shift.supervisorRole}) • ${shift.supervisorEmail}",
                    copyPayload = shift.supervisorEmail,
                    icon = Icons.Filled.Person,
                    onCopy = { onCopy("Supervisor Email", shift.supervisorEmail) }
                )

                Spacer(Modifier.height(Spacing.xs))

                // Copy All for Xello Quick-Action Button
                val fullPayload = buildString {
                    appendLine("Activity: ${shift.title}")
                    appendLine("Organization: ${shift.organizationName}")
                    appendLine("Hours: ${shift.hours.hoursLabel()} | Date: ${shift.eventDate}")
                    appendLine("Supervisor: ${shift.supervisorName} (${shift.supervisorEmail} - ${shift.supervisorRole})")
                    appendLine("Summary: ${reflection.commonAppBullet}")
                    appendLine("Reflection: ${reflection.xelloParagraph}")
                }

                ChunkyButton(
                    text = "📋 Copy All for Xello / Notes",
                    onClick = { onCopy("Complete Xello Entry", fullPayload) },
                    modifier = Modifier.fillMaxWidth(),
                    style = ButtonStyle.Primary,
                    icon = Icons.Filled.ContentCopy
                )
            }
        }
    }
}

@Composable
private fun CopyFieldRow(
    label: String,
    value: String,
    copyPayload: String,
    icon: ImageVector,
    onCopy: () -> Unit
) {
    val h = Hub.colors
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Radius.button))
            .background(h.canvas)
            .clickable(onClick = onCopy)
            .padding(Spacing.md),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(h.card),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
            }
            Spacer(Modifier.width(Spacing.md))
            Column {
                Text(label, style = MaterialTheme.typography.labelSmall, color = h.inkSoft)
                Text(value, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium), color = h.ink, maxLines = 1)
            }
        }
        Icon(
            Icons.Filled.ContentCopy,
            contentDescription = "Copy $label",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun AiReflectionBox(
    reflection: StarReflection,
    selectedMode: ReflectionMode,
    onModeSelected: (ReflectionMode) -> Unit,
    onCycle: () -> Unit,
    onCopy: (String, String) -> Unit,
    reduce: Boolean
) {
    val h = Hub.colors

    Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        SectionTitle("AI Impact Reflection (STAR)", trailing = {
            StatusPill("🎯 ${reflection.pathwayFocus}", Icons.Filled.AutoAwesome, h.rewardSoft, h.rewardDeep)
        })

        // Dual-Mode Selector Tabs
        Row(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(h.line)
                .padding(3.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            ReflectionMode.values().forEach { mode ->
                val selected = selectedMode == mode
                Box(
                    Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (selected) h.card else Color.Transparent)
                        .clickable { onModeSelected(mode) }
                        .padding(vertical = Spacing.sm),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        mode.title,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                        ),
                        color = if (selected) MaterialTheme.colorScheme.primary else h.inkSoft
                    )
                }
            }
        }

        // Reflection Card Content
        ChunkyCard {
            Column(Modifier.padding(Spacing.lg), verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
                AnimatedContent(
                    targetState = Pair(selectedMode, reflection),
                    transitionSpec = {
                        if (reduce) fadeIn() togetherWith fadeOut()
                        else fadeIn() togetherWith fadeOut()
                    },
                    label = "reflection_text"
                ) { (mode, ref) ->
                    Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                        val textToDisplay = when (mode) {
                            ReflectionMode.COMMON_APP -> ref.commonAppBullet
                            ReflectionMode.XELLO_ESSAY -> ref.xelloParagraph
                        }

                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                if (mode == ReflectionMode.COMMON_APP) "Active Verb & Quantified Metric" else "Leadership & Pathway Essay",
                                style = MaterialTheme.typography.labelSmall,
                                color = h.inkSoft
                            )

                            if (mode == ReflectionMode.COMMON_APP) {
                                val charCount = textToDisplay.length
                                val isUnder = charCount <= 150
                                StatusPill(
                                    text = "$charCount / 150 chars",
                                    icon = if (isUnder) Icons.Filled.Check else null,
                                    container = if (isUnder) h.successSoft else h.urgentSoft,
                                    content = if (isUnder) h.successDeep else h.urgentDeep
                                )
                            }
                        }

                        Text(
                            text = textToDisplay,
                            style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
                            color = h.ink
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm), verticalAlignment = Alignment.CenterVertically) {
                    ChunkyButton(
                        text = "✨ Next Variant",
                        onClick = onCycle,
                        modifier = Modifier.weight(1f),
                        style = ButtonStyle.Quiet,
                        icon = Icons.Filled.AutoAwesome
                    )

                    val currentText = if (selectedMode == ReflectionMode.COMMON_APP) reflection.commonAppBullet else reflection.xelloParagraph
                    ChunkyButton(
                        text = "📋 Copy Reflection",
                        onClick = { onCopy(selectedMode.title, currentText) },
                        modifier = Modifier.weight(1f),
                        style = ButtonStyle.Primary,
                        icon = Icons.Filled.ContentCopy
                    )
                }
            }
        }
    }
}

@Composable
private fun PdfExportBox(
    shift: ShiftRecord,
    vm: VolunteerViewModel,
    context: Context
) {
    val h = Hub.colors

    Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        SectionTitle("Official PDF Verification Certificate")
        Text(
            "Printable 8.5\" x 11\" transcript with Congressional District TX-04 accreditation seal, digital supervisor signature attestation, and counselor audit hash.",
            style = MaterialTheme.typography.bodySmall,
            color = h.inkSoft
        )

        ChunkyCard {
            Column(Modifier.padding(Spacing.lg), verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
                ChunkyButton(
                    text = "📄 Generate Verified Resume & PDF",
                    onClick = {
                        PdfGenerator.shareTranscriptPdf(context, vm.profile, listOf(shift))
                    },
                    modifier = Modifier.fillMaxWidth(),
                    style = ButtonStyle.Primary,
                    icon = Icons.Filled.PictureAsPdf
                )

                if (vm.shifts.size > 1) {
                    ChunkyButton(
                        text = "Export All ${vm.shifts.size} Shifts (Complete Transcript PDF)",
                        onClick = {
                            PdfGenerator.shareTranscriptPdf(context, vm.profile, vm.shifts)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        style = ButtonStyle.Quiet,
                        icon = Icons.Filled.Share
                    )
                }
            }
        }
    }
}
