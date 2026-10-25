package com.example.volunteerhub.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.volunteerhub.model.Badge
import com.example.volunteerhub.model.Dates
import com.example.volunteerhub.model.VolunteerEvent
import com.example.volunteerhub.ui.theme.Hub
import com.example.volunteerhub.ui.theme.Spacing
import com.example.volunteerhub.ui.theme.Touch

fun Double.hoursLabel(): String =
    if (this % 1.0 == 0.0) toInt().toString() else String.format(java.util.Locale.US, "%.1f", this)

@Composable
fun AppTopBar(title: String, onBack: (() -> Unit)?, modifier: Modifier = Modifier) {
    Row(
        modifier.fillMaxWidth().padding(horizontal = Spacing.sm, vertical = Spacing.xs),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (onBack != null) {
            Box(
                Modifier
                    .size(Touch.min)
                    .clickable(role = Role.Button, onClickLabel = "Go back", onClick = onBack),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Hub.colors.ink)
            }
        } else {
            Spacer(Modifier.width(Spacing.md))
        }
        Text(
            title, style = MaterialTheme.typography.titleLarge, color = Hub.colors.ink,
            maxLines = 1, overflow = TextOverflow.Ellipsis
        )
    }
}

/** Icon-only button with a guaranteed 48dp touch target and a spoken label. */
@Composable
fun IconAction(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: androidx.compose.ui.graphics.Color = Hub.colors.inkSoft
) {
    Box(
        modifier.size(Touch.min).clickable(role = Role.Button, onClickLabel = label, onClick = onClick),
        contentAlignment = Alignment.Center
    ) { Icon(icon, contentDescription = label, tint = tint) }
}

fun badgeIcon(badge: Badge) = when (badge) {
    Badge.FIRST_PLAN -> Icons.Filled.Explore
    Badge.FIRST_SHIFT -> Icons.Filled.Check
    Badge.EXPLORER -> Icons.Filled.Star
    Badge.TEN_HOURS -> Icons.Filled.Schedule
    Badge.HALFWAY -> Icons.Filled.Verified
    Badge.GOAL -> Icons.Filled.WorkspacePremium
}

@Composable
fun BadgeTile(badge: Badge, earned: Boolean, modifier: Modifier = Modifier) {
    val h = Hub.colors
    val state = if (earned) "earned" else "locked. ${badge.hint}"
    Column(
        modifier
            .width(92.dp)
            .semantics(mergeDescendants = true) { contentDescription = "${badge.title} badge, $state" },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Spacing.xs)
    ) {
        Box(
            Modifier
                .size(64.dp)
                .background(if (earned) h.reward else h.line, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                if (earned) badgeIcon(badge) else Icons.Filled.Lock, null,
                tint = if (earned) androidx.compose.ui.graphics.Color(0xFF1B1535) else h.inkSoft,
                modifier = Modifier.size(30.dp)
            )
        }
        Text(
            badge.title, style = MaterialTheme.typography.labelSmall,
            color = if (earned) h.ink else h.inkSoft, textAlign = TextAlign.Center, maxLines = 2
        )
    }
}

@Composable
fun BadgeShelf(earned: Set<Badge>, modifier: Modifier = Modifier) {
    LazyRow(modifier, horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
        items(Badge.values().toList()) { BadgeTile(it, it in earned) }
    }
}

/**
 * The one event card used in Discover, Plan and Home.
 * Hierarchy: cause + when -> title -> where -> credit + one clear action.
 */
@Composable
fun EventCard(
    event: VolunteerEvent,
    isPlanned: Boolean,
    isSaved: Boolean,
    isTopPick: Boolean,
    onOpen: () -> Unit,
    onTogglePlan: () -> Unit,
    onToggleSaved: () -> Unit,
    modifier: Modifier = Modifier
) {
    val h = Hub.colors
    val days = Dates.daysUntil(event.eventDate)
    val soon = days != null && days in 0..2
    ChunkyCard(modifier, onClick = onOpen, onClickLabel = "Open details for ${event.title}") {
        Column(Modifier.padding(Spacing.lg), verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CausePill(event.cause)
                StatusPill(
                    text = Dates.relative(event.eventDate) + if (event.isEstimatedDate) " (approx.)" else "",
                    icon = Icons.Filled.Event,
                    container = if (soon) h.urgentSoft else h.brandSoft,
                    content = if (soon) h.urgentDeep else h.brandDeep
                )
            }
            Text(
                event.title, style = MaterialTheme.typography.titleMedium, color = h.ink,
                maxLines = 2, overflow = TextOverflow.Ellipsis
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Place, null, tint = h.inkSoft, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(Spacing.xs))
                Text(
                    listOf(event.organizationName, event.location).filter { it.isNotBlank() }.joinToString(" · "),
                    style = MaterialTheme.typography.bodySmall, color = h.inkSoft,
                    maxLines = 1, overflow = TextOverflow.Ellipsis
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm), verticalAlignment = Alignment.CenterVertically) {
                XpBadge("${event.durationHours.hoursLabel()} hrs")
                if (isTopPick) StatusPill("Top pick for you", Icons.Filled.Star, h.successSoft, h.successDeep)
            }
            Row(
                Modifier.fillMaxWidth().padding(top = Spacing.xs),
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ChunkyButton(
                    text = if (isPlanned) "In my plan" else "I'm in",
                    onClick = onTogglePlan,
                    modifier = Modifier.weight(1f),
                    style = if (isPlanned) ButtonStyle.Success else ButtonStyle.Primary,
                    icon = if (isPlanned) Icons.Filled.Check else null
                )
                IconAction(
                    icon = if (isSaved) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                    label = if (isSaved) "Remove ${event.title} from saved" else "Save ${event.title} for later",
                    onClick = onToggleSaved,
                    tint = if (isSaved) MaterialTheme.colorScheme.primary else h.inkSoft
                )
            }
        }
    }
}
