package com.example.volunteerhub.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

enum class OnboardingStep {
    WELCOME,
    CREATE_ACCOUNT,
    ABOUT_YOU,
    YOUR_PATH,
    LOADING
}

@Composable
fun OnboardingScreen(
    onComplete: (name: String, grade: String, major: String) -> Unit
) {
    var step by remember { mutableStateOf(OnboardingStep.WELCOME) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var fullName by remember { mutableStateOf("") }
    var grade by remember { mutableStateOf("11th Grade") }
    var major by remember { mutableStateOf("Pre-Med / Healthcare") }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        AnimatedContent(
            targetState = step,
            transitionSpec = {
                fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300))
            },
            label = "OnboardingTransition"
        ) { currentStep ->
            when (currentStep) {
                OnboardingStep.WELCOME -> {
                    WelcomeContent(
                        onGetStarted = { step = OnboardingStep.CREATE_ACCOUNT },
                        onSignIn = {
                            fullName = "Alex Chen"
                            onComplete(fullName, grade, major)
                        }
                    )
                }
                OnboardingStep.CREATE_ACCOUNT -> {
                    CreateAccountContent(
                        email = email,
                        onEmailChange = { email = it },
                        password = password,
                        onPasswordChange = { password = it },
                        onContinue = { step = OnboardingStep.ABOUT_YOU }
                    )
                }
                OnboardingStep.ABOUT_YOU -> {
                    AboutYouContent(
                        fullName = fullName,
                        onFullNameChange = { fullName = it },
                        grade = grade,
                        onGradeChange = { grade = it },
                        onContinue = { step = OnboardingStep.YOUR_PATH }
                    )
                }
                OnboardingStep.YOUR_PATH -> {
                    YourPathContent(
                        selectedMajor = major,
                        onMajorChange = { major = it },
                        onFindMatches = { step = OnboardingStep.LOADING }
                    )
                }
                OnboardingStep.LOADING -> {
                    LoadingMatchesContent(
                        onFinished = { onComplete(fullName.ifBlank { "Alex Chen" }, grade, major) }
                    )
                }
            }
        }
    }
}

@Composable
private fun WelcomeContent(
    onGetStarted: () -> Unit,
    onSignIn: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Spacer(modifier = Modifier.height(40.dp))

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Surface(
                modifier = Modifier.size(96.dp),
                shape = CircleShape,
                color = Color(0xFF007AFF).copy(alpha = 0.12f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(text = "🤝", fontSize = 48.sp)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Volunteer Hub",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Find high school service hours that perfectly match your major and future career.",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = onGetStarted,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF007AFF))
            ) {
                Text("Get Started", fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(onClick = onSignIn) {
                Text("I already have an account", color = Color(0xFF007AFF), fontWeight = FontWeight.SemiBold)
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun CreateAccountContent(
    email: String,
    onEmailChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    onContinue: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Spacer(modifier = Modifier.height(32.dp))
            Text("Create Account", fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Your password is salted and encrypted securely via Supabase Auth.",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = email,
                onValueChange = onEmailChange,
                label = { Text("School or Personal Email") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = onPasswordChange,
                label = { Text("Password (min 6 characters)") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
        }

        Button(
            onClick = onContinue,
            enabled = email.isNotBlank() && password.length >= 6,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(bottom = 16.dp),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF007AFF))
        ) {
            Text("Continue", fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
        }
    }
}

@Composable
private fun AboutYouContent(
    fullName: String,
    onFullNameChange: (String) -> Unit,
    grade: String,
    onGradeChange: (String) -> Unit,
    onContinue: () -> Unit
) {
    val grades = listOf("9th Grade", "10th Grade", "11th Grade", "12th Grade")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Spacer(modifier = Modifier.height(32.dp))
            Text("About You", fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "This information appears on your certified volunteer transcripts.",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = fullName,
                onValueChange = onFullNameChange,
                label = { Text("Full Name (e.g. Alex Chen)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))
            Text("Select Grade Level:", fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                grades.forEach { g ->
                    FilterChip(
                        selected = (grade == g),
                        onClick = { onGradeChange(g) },
                        label = { Text(g.replace(" Grade", "")) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        Button(
            onClick = onContinue,
            enabled = fullName.isNotBlank(),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(bottom = 16.dp),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF007AFF))
        ) {
            Text("Continue", fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
        }
    }
}

@Composable
private fun YourPathContent(
    selectedMajor: String,
    onMajorChange: (String) -> Unit,
    onFindMatches: () -> Unit
) {
    val majors = listOf(
        "Pre-Med / Healthcare" to "🏥",
        "Computer Science & Tech" to "💻",
        "Engineering & Robotics" to "⚙️",
        "Business & Finance" to "📈",
        "Education & Literacy" to "📚",
        "Environmental Science" to "🌱"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Spacer(modifier = Modifier.height(32.dp))
            Text("Your Path", fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Our AI matches opportunities using semantic vector embeddings tailored to your college goals.",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            majors.forEach { (name, emoji) ->
                val isSelected = selectedMajor == name
                Surface(
                    onClick = { onMajorChange(name) },
                    shape = RoundedCornerShape(16.dp),
                    color = if (isSelected) Color(0xFF007AFF).copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, Color(0xFF007AFF)) else null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = emoji, fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(text = name, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium)
                    }
                }
            }
        }

        Button(
            onClick = onFindMatches,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(bottom = 16.dp),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF007AFF))
        ) {
            Text("Find Matches ✨", fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
        }
    }
}

@Composable
private fun LoadingMatchesContent(
    onFinished: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "Rotation")
    val angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(animation = tween(1200, easing = LinearEasing)),
        label = "SpinnerAngle"
    )

    LaunchedEffect(Unit) {
        delay(2200)
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(88.dp)
                        .rotate(angle),
                    strokeWidth = 6.dp,
                    color = Color(0xFF007AFF)
                )
                Text("✨", fontSize = 32.sp)
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Finding the best volunteering opportunities for you...",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Analyzing North Texas organizations & major alignments",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}
