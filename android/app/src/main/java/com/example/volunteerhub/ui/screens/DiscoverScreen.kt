package com.example.volunteerhub.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import com.example.volunteerhub.EventsState
import com.example.volunteerhub.Route
import com.example.volunteerhub.VolunteerViewModel
import com.example.volunteerhub.data.EventFilter
import com.example.volunteerhub.data.EventRanker
import com.example.volunteerhub.data.SortMode
import com.example.volunteerhub.model.Cause
import com.example.volunteerhub.ui.components.ButtonStyle
import com.example.volunteerhub.ui.components.ChunkyButton
import com.example.volunteerhub.ui.components.EmptyState
import com.example.volunteerhub.ui.components.ErrorState
import com.example.volunteerhub.ui.components.EventCard
import com.example.volunteerhub.ui.components.IconAction
import com.example.volunteerhub.ui.components.SelectChip
import com.example.volunteerhub.ui.components.ShimmerCard
import com.example.volunteerhub.ui.components.StatusPill
import com.example.volunteerhub.ui.components.style
import com.example.volunteerhub.ui.theme.Hub
import com.example.volunteerhub.ui.theme.Radius
import com.example.volunteerhub.ui.theme.Spacing

@Composable
fun DiscoverScreen(vm: VolunteerViewModel) {
    val h = Hub.colors
    val filter = vm.filter
    val state = vm.eventsState
    val results = vm.visibleEvents

    LazyColumn(
        Modifier.fillMaxSize().statusBarsPadding(),
        contentPadding = PaddingValues(Spacing.lg),
        verticalArrangement = Arrangement.spacedBy(Spacing.lg)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                Text("Find your next shift", style = MaterialTheme.typography.headlineMedium, color = h.ink)
                Text(
                    when (state) {
                        is EventsState.Ready -> "${results.size} opportunities near you"
                        else -> "Everything local, in one place."
                    },
                    style = MaterialTheme.typography.bodyMedium, color = h.inkSoft
                )
            }
        }

        item {
            OutlinedTextField(
                value = filter.query,
                onValueChange = { vm.updateFilter(filter.copy(query = it)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("Search by cause, place or organization") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                trailingIcon = {
                    if (filter.query.isNotEmpty()) {
                        IconAction(Icons.Filled.Clear, "Clear search", { vm.updateFilter(filter.copy(query = "")) })
                    }
                },
                shape = RoundedCornerShape(Radius.button),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = h.line,
                    focusedContainerColor = h.card,
                    unfocusedContainerColor = h.card,
                    focusedTextColor = h.ink,
                    unfocusedTextColor = h.ink,
                    focusedPlaceholderColor = h.inkSoft,
                    unfocusedPlaceholderColor = h.inkSoft,
                    focusedLeadingIconColor = h.inkSoft,
                    unfocusedLeadingIconColor = h.inkSoft
                )
            )
        }

        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                item {
                    SelectChip("All causes", filter.cause == null, { vm.updateFilter(filter.copy(cause = null)) })
                }
                items(Cause.values().toList()) { cause ->
                    val s = cause.style()
                    SelectChip(
                        label = cause.label, selected = filter.cause == cause,
                        onClick = { vm.updateFilter(filter.copy(cause = if (filter.cause == cause) null else cause)) },
                        icon = s.icon, accent = s.color, accentSoft = s.soft
                    )
                }
            }
        }

        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                items(SortMode.values().toList()) { mode ->
                    SelectChip(mode.label, filter.sort == mode, { vm.updateFilter(filter.copy(sort = mode)) })
                }
                item {
                    SelectChip("This week", filter.thisWeekOnly, { vm.updateFilter(filter.copy(thisWeekOnly = !filter.thisWeekOnly)) })
                }
                item {
                    SelectChip(
                        "Under 3 hrs", filter.maxHours == 3.0,
                        { vm.updateFilter(filter.copy(maxHours = if (filter.maxHours == 3.0) null else 3.0)) }
                    )
                }
            }
        }

        when (state) {
            is EventsState.Loading -> items(3) { ShimmerCard() }

            is EventsState.Error -> item {
                ErrorState(
                    message = state.message,
                    onRetry = { vm.refresh() },
                    secondaryLabel = "Explore with sample events",
                    onSecondary = { vm.useSampleEvents() }
                )
            }

            is EventsState.Ready -> {
                if (state.isSample) {
                    item {
                        StatusPill(
                            "Sample events — not real opportunities", Icons.Filled.Info,
                            h.infoSoft, h.infoDeep
                        )
                    }
                }
                if (results.isEmpty()) {
                    item {
                        EmptyState(
                            icon = Icons.Filled.Explore,
                            title = "No shifts match",
                            body = "Try a different search or clear your filters.",
                            actionLabel = if (filter != EventFilter()) "Clear filters" else null,
                            onAction = { vm.updateFilter(EventFilter()) }
                        )
                    }
                } else {
                    items(results, key = { it.id }) { event ->
                        EventCard(
                            event = event,
                            isPlanned = event.id in vm.plannedIds,
                            isSaved = event.id in vm.savedIds,
                            isTopPick = EventRanker.isMatch(event, vm.profile.interests),
                            onOpen = { vm.push(Route.Detail(event.id)) },
                            onTogglePlan = { vm.togglePlan(event) },
                            onToggleSaved = { vm.toggleSaved(event.id) }
                        )
                    }
                }
            }
        }
    }
}
