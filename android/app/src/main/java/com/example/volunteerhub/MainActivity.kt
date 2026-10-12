package com.example.volunteerhub

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.volunteerhub.data.SupabaseRepository
import com.example.volunteerhub.model.UserProfile
import com.example.volunteerhub.model.VolunteerEvent
import com.example.volunteerhub.ui.screens.*
import com.example.volunteerhub.ui.theme.VolunteerHubTheme
import kotlinx.coroutines.launch

enum class AppScreen {
    ONBOARDING,
    DASHBOARD,
    DISCOVER,
    SCANNER,
    SIGNATURE,
    XELLO,
    PDF
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VolunteerHubTheme {
                VolunteerHubApp()
            }
        }
    }
}

@Composable
fun VolunteerHubApp() {
    var currentScreen by remember { mutableStateOf(AppScreen.ONBOARDING) }
    var selectedTab by remember { mutableStateOf(0) } // 0: Dashboard, 1: Discover, 2: Scan

    var userProfile by remember {
        mutableStateOf(
            UserProfile(
                fullName = "Alex Chen",
                grade = "11th Grade",
                intendedMajor = "Computer Science & Tech",
                targetClub = "National Honor Society",
                goalHours = 30.0,
                completedHours = 14.0
            )
        )
    }

    var rsvpdEventIds by remember { mutableStateOf(setOf<String>()) }
    var baseVerifiedHours by remember { mutableStateOf(14.0) }

    fun toggleRsvp(event: VolunteerEvent) {
        rsvpdEventIds = if (rsvpdEventIds.contains(event.id)) {
            rsvpdEventIds - event.id
        } else {
            rsvpdEventIds + event.id
        }
    }

    val totalHours = baseVerifiedHours + (rsvpdEventIds.size * 4.0)
    val activeUserProfile = userProfile.copy(completedHours = totalHours)

    var events by remember { mutableStateOf<List<VolunteerEvent>>(emptyList()) }
    var selectedEvent by remember { mutableStateOf<VolunteerEvent?>(null) }
    val scope = rememberCoroutineScope()

    val sortedEvents = remember(events, activeUserProfile.intendedMajor) {
        val stemKeywords = listOf("stem", "tech", "computer", "code", "coding", "software", "robot", "robotics", "data", "ai", "3d print", "maker", "engineer", "python", "developer", "hardware", "digital")
        val preMedKeywords = listOf("medical", "hospital", "patient", "blood", "health", "clinic", "wellness", "comfort kit", "hospice")
        val ecoKeywords = listOf("park", "trail", "environment", "tree", "plant", "conservation", "nature", "wildflower", "prairie")
        val eduKeywords = listOf("tutor", "reading", "literacy", "library", "school", "mentor", "teach", "homework", "book")
        val bizKeywords = listOf("business", "finance", "resale", "store", "inventory", "budget", "merchandising", "logistics")

        val (primary, secondary) = when (activeUserProfile.intendedMajor) {
            "Computer Science & Tech" -> stemKeywords to (bizKeywords + eduKeywords)
            "Engineering & Robotics" -> stemKeywords to (ecoKeywords + bizKeywords)
            "Pre-Med / Healthcare" -> preMedKeywords to eduKeywords
            "Environmental Science" -> ecoKeywords to eduKeywords
            "Education & Literacy" -> eduKeywords to stemKeywords
            "Business & Finance" -> bizKeywords to stemKeywords
            else -> stemKeywords to emptyList()
        }

        events.map { event ->
            val text = "${event.title} ${event.organizationName} ${event.description} ${event.category}".lowercase()
            val eventHash = (event.title.hashCode() and 0x7FFFFFFF) % 4
            val primaryHits = primary.count { text.contains(it) }
            val secondaryHits = secondary.count { text.contains(it) }
            val score = when {
                primaryHits >= 2 -> 96 + eventHash
                primaryHits == 1 -> 91 + eventHash
                secondaryHits >= 1 -> 81 + eventHash
                text.contains("food") || text.contains("meal") || text.contains("community") -> 74 + eventHash
                else -> 38 + (eventHash * 3)
            }
            event.copy(similarity = score / 100.0)
        }.sortedByDescending { it.similarity ?: 0.0 }
    }

    LaunchedEffect(Unit) {
        scope.launch {
            events = SupabaseRepository.fetchEvents()
        }
    }

    LaunchedEffect(sortedEvents) {
        if (selectedEvent == null && sortedEvents.isNotEmpty()) {
            selectedEvent = sortedEvents.first()
        }
    }

    Scaffold(
        bottomBar = {
            // Only show bottom navigation when inside the main app screens
            if (currentScreen in listOf(AppScreen.DASHBOARD, AppScreen.DISCOVER, AppScreen.SCANNER)) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp
                ) {
                    NavigationBarItem(
                        selected = (selectedTab == 0),
                        onClick = {
                            selectedTab = 0
                            currentScreen = AppScreen.DASHBOARD
                        },
                        icon = { Text("📊", fontSize = 20.sp) },
                        label = { Text("Dashboard", fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal) }
                    )

                    NavigationBarItem(
                        selected = (selectedTab == 1),
                        onClick = {
                            selectedTab = 1
                            currentScreen = AppScreen.DISCOVER
                        },
                        icon = { Text("✨", fontSize = 20.sp) },
                        label = { Text("Discover", fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal) }
                    )

                    NavigationBarItem(
                        selected = (selectedTab == 2),
                        onClick = {
                            selectedTab = 2
                            currentScreen = AppScreen.SCANNER
                        },
                        icon = { Text("📷", fontSize = 20.sp) },
                        label = { Text("Scan", fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal) }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                AppScreen.ONBOARDING -> {
                    OnboardingScreen(
                        onComplete = { name, grade, major ->
                            userProfile = userProfile.copy(
                                fullName = name,
                                grade = grade,
                                intendedMajor = major
                            )
                            currentScreen = AppScreen.DASHBOARD
                        }
                    )
                }

                AppScreen.DASHBOARD -> {
                    DashboardScreen(
                        userProfile = activeUserProfile,
                        events = sortedEvents,
                        rsvpdEventIds = rsvpdEventIds,
                        onOpenXello = { event ->
                            selectedEvent = event
                            currentScreen = AppScreen.XELLO
                        },
                        onNavigateToDiscover = {
                            selectedTab = 1
                            currentScreen = AppScreen.DISCOVER
                        },
                        onToggleRsvp = { event ->
                            toggleRsvp(event)
                        }
                    )
                }

                AppScreen.DISCOVER -> {
                    DiscoverScreen(
                        userProfile = activeUserProfile,
                        events = sortedEvents,
                        rsvpdEventIds = rsvpdEventIds,
                        onSelectEvent = { event ->
                            selectedEvent = event
                            currentScreen = AppScreen.XELLO
                        },
                        onToggleRsvp = { event ->
                            toggleRsvp(event)
                        }
                    )
                }

                AppScreen.SCANNER -> {
                    ScannerScreen(
                        onEndShiftRequestSignature = {
                            currentScreen = AppScreen.SIGNATURE
                        }
                    )
                }

                AppScreen.SIGNATURE -> {
                    SignatureScreen(
                        onSignatureVerified = { name, email ->
                            // Update base hours & return to dashboard
                            baseVerifiedHours += 4.0
                            currentScreen = AppScreen.DASHBOARD
                        },
                        onCancel = {
                            currentScreen = AppScreen.SCANNER
                        }
                    )
                }

                AppScreen.XELLO -> {
                    val ev = selectedEvent ?: events.firstOrNull() ?: SupabaseRepository.getFallbackEvents().first()
                    XelloScreen(
                        event = ev,
                        isRsvpd = rsvpdEventIds.contains(ev.id),
                        onToggleRsvp = {
                            toggleRsvp(ev)
                        },
                        onBack = {
                            currentScreen = if (selectedTab == 1) AppScreen.DISCOVER else AppScreen.DASHBOARD
                        },
                        onGeneratePdf = {
                            currentScreen = AppScreen.PDF
                        }
                    )
                }

                AppScreen.PDF -> {
                    val relevantEvents = sortedEvents.filter { rsvpdEventIds.contains(it.id) }.ifEmpty { sortedEvents.take(3) }
                    PdfScreen(
                        userProfile = activeUserProfile,
                        events = relevantEvents,
                        onClose = {
                            currentScreen = AppScreen.XELLO
                        }
                    )
                }
            }
        }
    }
}