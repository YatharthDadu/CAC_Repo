package com.example.volunteerhub.ui.components

import android.provider.Settings
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.volunteerhub.model.Cause
import com.example.volunteerhub.ui.theme.Hub
import com.example.volunteerhub.ui.theme.Lip
import com.example.volunteerhub.ui.theme.Radius
import com.example.volunteerhub.ui.theme.Spacing
import com.example.volunteerhub.ui.theme.Touch

// --- motion -------------------------------------------------------------------------------------

/** True when the user turned system animations off; celebratory motion should then be skipped. */
@Composable
fun rememberReduceMotion(): Boolean {
    val context = LocalContext.current
    return remember {
        try {
            Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f
        } catch (e: Exception) {
            false
        }
    }
}

// --- cause styling ------------------------------------------------------------------------------

data class CauseStyle(val icon: ImageVector, val color: Color, val soft: Color, val deep: Color)

/** Each cause gets its own icon AND color, so color is never the only signal. */
@Composable
fun Cause.style(): CauseStyle {
    val h = Hub.colors
    return when (this) {
        Cause.TECH -> CauseStyle(Icons.Filled.Code, h.info, h.infoSoft, h.infoDeep)
        Cause.HEALTH -> CauseStyle(Icons.Filled.LocalHospital, h.urgent, h.urgentSoft, h.urgentDeep)
        Cause.NATURE -> CauseStyle(Icons.Filled.Eco, h.success, h.successSoft, h.successDeep)
        Cause.LEARNING -> CauseStyle(Icons.Filled.School, MaterialTheme.colorScheme.primary, h.brandSoft, h.brandDeep)
        Cause.COMMUNITY -> CauseStyle(Icons.Filled.Restaurant, h.reward, h.rewardSoft, h.rewardDeep)
        Cause.BUSINESS -> CauseStyle(
            Icons.Filled.Storefront,
            Color(0xFFFF9A2E),
            if (h.isDark) Color(0xFF4A3010) else Color(0xFFFFEBD3),
            if (h.isDark) Color(0xFFFFC27A) else Color(0xFF9A4F00)
        )
    }
}

// --- chunky surfaces ----------------------------------------------------------------------------

/**
 * Duolingo-style tactile card: a flat face sitting on a darker "lip". Pass [onClick] to make it
 * pressable (the face sinks onto the lip).
 */
@Composable
fun ChunkyCard(
    modifier: Modifier = Modifier,
    face: Color = Hub.colors.card,
    lip: Color = Hub.colors.line,
    lipHeight: Dp = Lip.card,
    corner: Dp = Radius.card,
    onClick: (() -> Unit)? = null,
    onClickLabel: String? = null,
    content: @Composable () -> Unit
) {
    val shape = RoundedCornerShape(corner)
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val sink by animateDpAsState(
        if (pressed && onClick != null) lipHeight else 0.dp,
        spring(stiffness = Spring.StiffnessHigh), label = "cardSink"
    )
    val clickable = if (onClick != null) {
        Modifier.clickable(
            interactionSource = interaction, indication = null,
            onClickLabel = onClickLabel, role = Role.Button, onClick = onClick
        )
    } else Modifier
    Box(modifier.then(clickable)) {
        Box(Modifier.matchParentSize().padding(top = lipHeight).background(lip, shape))
        Box(
            Modifier
                .padding(bottom = lipHeight)
                .offset(y = sink)
                .fillMaxWidth()
                .background(face, shape)
                .border(BorderStroke(2.dp, lip), shape)
        ) { content() }
    }
}

enum class ButtonStyle { Primary, Reward, Success, Danger, Quiet }

@Composable
fun ChunkyButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: ButtonStyle = ButtonStyle.Primary,
    icon: ImageVector? = null,
    enabled: Boolean = true
) {
    val cs = MaterialTheme.colorScheme
    val h = Hub.colors
    val (face, lip, label) = when (style) {
        ButtonStyle.Primary -> Triple(cs.primary, h.brandDeep, cs.onPrimary)
        ButtonStyle.Reward -> Triple(h.reward, h.rewardDeep, Color(0xFF1B1535))
        ButtonStyle.Success -> Triple(h.success, h.successDeep, Color(0xFF0B2B20))
        ButtonStyle.Danger -> Triple(h.urgent, h.urgentDeep, Color.White)
        ButtonStyle.Quiet -> Triple(h.card, h.line, h.ink)
    }
    val faceColor by animateColorAsState(if (enabled) face else h.line, label = "btnFace")
    val labelColor = if (enabled) label else h.inkSoft
    val lipColor = if (enabled) lip else h.line
    val lipH = Lip.button
    val shape = RoundedCornerShape(Radius.button)
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val sink by animateDpAsState(
        if (pressed && enabled) lipH else 0.dp,
        spring(stiffness = Spring.StiffnessHigh), label = "btnSink"
    )
    Box(
        modifier
            .heightIn(min = Touch.min)
            .clickable(
                interactionSource = interaction, indication = null, enabled = enabled,
                role = Role.Button, onClick = onClick
            )
    ) {
        Box(Modifier.matchParentSize().padding(top = lipH).background(lipColor, shape))
        Row(
            Modifier
                .padding(bottom = lipH)
                .offset(y = sink)
                .fillMaxWidth()
                .heightIn(min = 48.dp)
                .background(faceColor, shape)
                .then(if (style == ButtonStyle.Quiet) Modifier.border(BorderStroke(2.dp, h.line), shape) else Modifier)
                .padding(horizontal = Spacing.lg, vertical = Spacing.md),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Icon(icon, contentDescription = null, tint = labelColor, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(Spacing.sm))
            }
            Text(text, color = labelColor, style = MaterialTheme.typography.labelLarge, textAlign = TextAlign.Center)
        }
    }
}

// --- chips and pills ----------------------------------------------------------------------------

/** A selectable filter chip (>= 48dp tall touch target, selected state also shown with a check-like weight). */
@Composable
fun SelectChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    accent: Color = MaterialTheme.colorScheme.primary,
    accentSoft: Color = Hub.colors.brandSoft
) {
    val h = Hub.colors
    val shape = RoundedCornerShape(Radius.chip)
    val bg by animateColorAsState(if (selected) accentSoft else h.card, label = "chipBg")
    val stroke = if (selected) accent else h.line
    Row(
        modifier
            .heightIn(min = Touch.min)
            .clip(shape)
            .background(bg, shape)
            .border(BorderStroke(if (selected) 2.5.dp else 2.dp, stroke), shape)
            .clickable(role = Role.Checkbox, onClick = onClick)
            .semantics { contentDescription = if (selected) "$label, selected" else label }
            .padding(horizontal = Spacing.md, vertical = Spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
    ) {
        if (icon != null) Icon(icon, null, tint = if (selected) accent else h.inkSoft, modifier = Modifier.size(18.dp))
        Text(
            label,
            color = h.ink,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.Bold
        )
    }
}

/** Small status label: icon + text so meaning never depends on color alone. */
@Composable
fun StatusPill(
    text: String,
    icon: ImageVector?,
    container: Color,
    content: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier
            .background(container, RoundedCornerShape(50))
            .padding(horizontal = Spacing.md, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
    ) {
        if (icon != null) Icon(icon, null, tint = content, modifier = Modifier.size(14.dp))
        Text(text, color = content, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
fun CausePill(cause: Cause, modifier: Modifier = Modifier) {
    val s = cause.style()
    StatusPill(cause.label, s.icon, s.soft, s.deep, modifier)
}

/** Round icon tile used on cards and lists. Decorative. */
@Composable
fun IconTile(icon: ImageVector, color: Color, tint: Color, size: Dp = 48.dp, modifier: Modifier = Modifier) {
    Box(
        modifier.size(size).background(color, RoundedCornerShape(size / 3)),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, null, tint = tint, modifier = Modifier.size(size / 2))
    }
}

// --- progress -----------------------------------------------------------------------------------

/**
 * Goal ring. The inner arc is verified hours, the lighter arc behind it is verified + planned.
 * [center] is drawn in the middle.
 */
@Composable
fun ProgressRing(
    verified: Float,
    planned: Float,
    modifier: Modifier = Modifier,
    size: Dp = 160.dp,
    stroke: Dp = 18.dp,
    description: String,
    trackColor: Color = Hub.colors.line,
    plannedColor: Color = MaterialTheme.colorScheme.primary.copy(alpha = 0.28f),
    arcColor: Color = MaterialTheme.colorScheme.primary,
    doneColor: Color = Hub.colors.reward,
    center: @Composable () -> Unit
) {
    val reduce = rememberReduceMotion()
    val spec = if (reduce) tween<Float>(0) else spring<Float>(dampingRatio = 0.7f, stiffness = Spring.StiffnessLow)
    val v by animateFloatAsState(verified, spec, label = "ringVerified")
    val p by animateFloatAsState(planned, spec, label = "ringPlanned")
    val sweep = if (verified >= 1f) doneColor else arcColor
    Box(
        modifier.size(size).semantics(mergeDescendants = true) { contentDescription = description },
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.matchParentSize().clearAndSetSemantics { }) {
            val w = stroke.toPx()
            val inset = w / 2
            val arcSize = Size(this.size.width - w, this.size.height - w)
            val topLeft = Offset(inset, inset)
            drawArc(trackColor, 0f, 360f, false, topLeft, arcSize, style = Stroke(w))
            if (p > 0f) {
                drawArc(plannedColor, -90f, 360f * p, false, topLeft, arcSize, style = Stroke(w, cap = StrokeCap.Round))
            }
            if (v > 0f) {
                drawArc(sweep, -90f, 360f * v, false, topLeft, arcSize, style = Stroke(w, cap = StrokeCap.Round))
            }
        }
        center()
    }
}

@Composable
fun LinearGoalBar(fraction: Float, modifier: Modifier = Modifier, color: Color = Hub.colors.reward) {
    val reduce = rememberReduceMotion()
    val f by animateFloatAsState(
        fraction.coerceIn(0f, 1f),
        if (reduce) tween(0) else spring(stiffness = Spring.StiffnessLow), label = "bar"
    )
    Box(modifier.fillMaxWidth().height(14.dp).background(Hub.colors.line, RoundedCornerShape(50))) {
        if (f > 0f) {
            Box(
                Modifier
                    .fillMaxWidth(f)
                    .height(14.dp)
                    .background(color, RoundedCornerShape(50))
            )
        }
    }
}

// --- states -------------------------------------------------------------------------------------

@Composable
fun XpBadge(text: String, modifier: Modifier = Modifier) {
    val h = Hub.colors
    StatusPill(text, Icons.Filled.Bolt, h.rewardSoft, if (h.isDark) h.reward else Color(0xFF6B4A00), modifier)
}

@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    body: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    Column(
        modifier.fillMaxWidth().padding(Spacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Spacing.md)
    ) {
        IconTile(icon, Hub.colors.brandSoft, MaterialTheme.colorScheme.primary, size = 72.dp)
        Text(title, style = MaterialTheme.typography.titleLarge, color = Hub.colors.ink, textAlign = TextAlign.Center)
        Text(body, style = MaterialTheme.typography.bodyMedium, color = Hub.colors.inkSoft, textAlign = TextAlign.Center)
        if (actionLabel != null && onAction != null) {
            ChunkyButton(actionLabel, onAction, Modifier.padding(top = Spacing.sm))
        }
    }
}

@Composable
fun ErrorState(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    secondaryLabel: String? = null,
    onSecondary: (() -> Unit)? = null
) {
    Column(
        modifier.fillMaxWidth().padding(Spacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Spacing.md)
    ) {
        IconTile(Icons.Filled.WifiOff, Hub.colors.urgentSoft, Hub.colors.urgentDeep, size = 72.dp)
        Text("Couldn't load opportunities", style = MaterialTheme.typography.titleLarge, color = Hub.colors.ink, textAlign = TextAlign.Center)
        Text(message, style = MaterialTheme.typography.bodyMedium, color = Hub.colors.inkSoft, textAlign = TextAlign.Center)
        ChunkyButton("Try again", onRetry, Modifier.fillMaxWidth(), icon = Icons.Filled.Refresh)
        if (secondaryLabel != null && onSecondary != null) {
            ChunkyButton(secondaryLabel, onSecondary, Modifier.fillMaxWidth(), style = ButtonStyle.Quiet)
        }
    }
}

/** Placeholder card while events load. Static when the user disabled animations. */
@Composable
fun ShimmerCard(modifier: Modifier = Modifier) {
    val reduce = rememberReduceMotion()
    val h = Hub.colors
    val alpha = if (reduce) 0.6f else {
        val t = rememberInfiniteTransition(label = "shimmer")
        val a by t.animateFloat(
            0.35f, 0.9f,
            infiniteRepeatable(tween(900), RepeatMode.Reverse), label = "shimmerAlpha"
        )
        a
    }
    val block = Brush.horizontalGradient(listOf(h.line.copy(alpha = alpha), h.line.copy(alpha = alpha)))
    ChunkyCard(modifier.clearAndSetSemantics { }) {
        Column(Modifier.padding(Spacing.lg), verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
            Box(Modifier.width(110.dp).height(22.dp).background(block, RoundedCornerShape(50)))
            Box(Modifier.fillMaxWidth(0.85f).height(20.dp).background(block, RoundedCornerShape(8.dp)))
            Box(Modifier.fillMaxWidth(0.5f).height(14.dp).background(block, RoundedCornerShape(8.dp)))
            Box(Modifier.fillMaxWidth().height(44.dp).background(block, RoundedCornerShape(Radius.button)))
        }
    }
}

@Composable
fun SectionTitle(text: String, modifier: Modifier = Modifier, trailing: (@Composable () -> Unit)? = null) {
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
        Text(text, style = MaterialTheme.typography.titleLarge, color = Hub.colors.ink)
        trailing?.invoke()
    }
}
