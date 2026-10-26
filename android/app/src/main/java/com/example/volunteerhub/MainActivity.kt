package com.example.volunteerhub

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.ViewModelProvider
import com.example.volunteerhub.ui.components.rememberReduceMotion
import com.example.volunteerhub.ui.screens.CelebrationOverlay
import com.example.volunteerhub.ui.screens.CheckInScreen
import com.example.volunteerhub.ui.screens.DetailScreen
import com.example.volunteerhub.ui.screens.DiscoverScreen
import com.example.volunteerhub.ui.screens.HomeScreen
import com.example.volunteerhub.ui.screens.OnboardingScreen
import com.example.volunteerhub.ui.screens.PlanScreen
import com.example.volunteerhub.ui.screens.ProfileScreen
import com.example.volunteerhub.ui.screens.SignatureScreen
import com.example.volunteerhub.ui.screens.TranscriptScreen
import com.example.volunteerhub.ui.screens.XelloScreen
import com.example.volunteerhub.ui.theme.Hub
import com.example.volunteerhub.ui.theme.VolunteerHubTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val vm = ViewModelProvider(this)[VolunteerViewModel::class.java]
        setContent {
            VolunteerHubTheme {
                VolunteerHubApp(vm)
            }
        }
    }
}

private data class Tab(val route: Route, val label: String, val icon: ImageVector)

private val tabs = listOf(
    Tab(Route.Home, "Home", Icons.Filled.Home),
    Tab(Route.Discover, "Discover", Icons.Filled.Explore),
    Tab(Route.Plan, "My plan", Icons.Filled.Event),
    Tab(Route.Profile, "Profile", Icons.Filled.Person)
)

@Composable
fun VolunteerHubApp(vm: VolunteerViewModel) {
    val h = Hub.colors

    if (!vm.profile.isComplete) {
        OnboardingScreen(onDone = { vm.saveProfile(it) })
        return
    }

    BackHandler(enabled = vm.backStack.size > 1) { vm.back() }
    val route = vm.route
    val reduce = rememberReduceMotion()

    Scaffold(
        containerColor = h.canvas,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            if (route.isTab) {
                NavigationBar(containerColor = h.card) {
                    tabs.forEach { tab ->
                        NavigationBarItem(
                            selected = route == tab.route,
                            onClick = { vm.openTab(tab.route) },
                            icon = { Icon(tab.icon, contentDescription = null) },
                            label = { Text(tab.label) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = androidx.compose.material3.MaterialTheme.colorScheme.onPrimary,
                                selectedTextColor = h.ink,
                                indicatorColor = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                                unselectedIconColor = h.inkSoft,
                                unselectedTextColor = h.inkSoft
                            )
                        )
                    }
                }
            }
        }
    ) { inner ->
        Box(Modifier.fillMaxSize().padding(inner).background(h.canvas)) {
            AnimatedContent(
                targetState = route,
                transitionSpec = {
                    if (reduce) fadeIn(tween(0)) togetherWith fadeOut(tween(0))
                    else fadeIn(tween(180)) togetherWith fadeOut(tween(120))
                },
                label = "screen"
            ) { target ->
                when (target) {
                    Route.Home -> HomeScreen(vm)
                    Route.Discover -> DiscoverScreen(vm)
                    Route.Plan -> PlanScreen(vm)
                    Route.Profile -> ProfileScreen(vm)
                    is Route.Detail -> DetailScreen(vm, target.eventId)
                    Route.CheckIn -> CheckInScreen(vm)
                    Route.Signature -> SignatureScreen(vm)
                    Route.Transcript -> TranscriptScreen(vm)
                    is Route.Xello -> XelloScreen(vm, target.eventId)
                }
            }
        }
    }

    vm.celebration?.let { celebration ->
        CelebrationOverlay(
            c = celebration,
            onDismiss = { vm.dismissCelebration() },
            onPrepareXello = { eventId ->
                vm.push(Route.Xello(eventId))
            }
        )
    }
}
