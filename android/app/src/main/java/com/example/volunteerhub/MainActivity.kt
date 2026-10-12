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
                intendedMajor = "Pre-Med / Healthcare"
            )
        )
    }

    var events by remember { mutableStateOf<List<VolunteerEvent>>(emptyList()) }
    var selectedEvent by remember { mutableStateOf<VolunteerEvent?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        scope.launch {
            events = SupabaseRepository.fetchEvents()
            if (selectedEvent == null && events.isNotEmpty()) {
                selectedEvent = events.first()
            }
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
                        userProfile = userProfile,
                        events = events,
                        onOpenXello = { event ->
                            selectedEvent = event
                            currentScreen = AppScreen.XELLO
                        }
                    )
                }

                AppScreen.DISCOVER -> {
                    DiscoverScreen(
                        events = events,
                        onSelectEvent = { event ->
                            selectedEvent = event
                            currentScreen = AppScreen.XELLO
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
                            // Update hours & return to dashboard
                            userProfile = userProfile.copy(
                                completedHours = userProfile.completedHours + 4.0
                            )
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
                        onBack = {
                            currentScreen = if (selectedTab == 1) AppScreen.DISCOVER else AppScreen.DASHBOARD
                        },
                        onGeneratePdf = {
                            currentScreen = AppScreen.PDF
                        }
                    )
                }

                AppScreen.PDF -> {
                    PdfScreen(
                        userProfile = userProfile,
                        events = events,
                        onClose = {
                            currentScreen = AppScreen.XELLO
                        }
                    )
                }
            }
        }
    }
}