package com.example.volunteerhub.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

enum class AuthScreenState {
    HERO_WELCOME,      // Dark hero screen with cards & swipe slider
    SIGN_IN,           // "Welcome Back!" forest green light view
    CREATE_ACCOUNT,    // "Create Account" light view
    PROFILE_SETUP,     // Student details: name, grade, location, major
    POST_AUTH_LAUNCH   // Post-login home screen where user swipes to launch dashboard
}

// Brand color palette matching the design reference
val ForestGreenPrimary = Color(0xFF0F4A3C)
val ForestGreenDark = Color(0xFF09372B)
val ForestGreenLight = Color(0xFFE8F2EE)
val WarmGoldAccent = Color(0xFFF5C518)
val WarmGoldLight = Color(0xFFFFF9E6)
val DarkHeroCanvas = Color(0xFF111317)
val DarkHeroCard = Color(0xFF1C1F28)
val DarkTrackBg = Color(0xFF222630)
val InputFieldBg = Color(0xFFF7F8FA)
val InputBorderColor = Color(0xFFE4E7EC)

@Composable
fun OnboardingScreen(
    onComplete: (name: String, grade: String, major: String) -> Unit
) {
    var screenState by remember { mutableStateOf(AuthScreenState.HERO_WELCOME) }
    var email by remember { mutableStateOf("alex.chen@friscoisd.org") }
    var password by remember { mutableStateOf("••••••••") }
    var confirmPassword by remember { mutableStateOf("••••••••") }
    var rememberMe by remember { mutableStateOf(true) }
    var passwordVisible by remember { mutableStateOf(false) }

    var fullName by remember { mutableStateOf("Alex Chen") }
    var grade by remember { mutableStateOf("11th Grade") }
    var location by remember { mutableStateOf("Frisco, TX • TX-04") }
    var major by remember { mutableStateOf("Computer Science & Tech") }
    var targetClub by remember { mutableStateOf("National Honor Society (NHS)") }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = if (screenState == AuthScreenState.HERO_WELCOME || screenState == AuthScreenState.POST_AUTH_LAUNCH) DarkHeroCanvas else Color.White
    ) {
        AnimatedContent(
            targetState = screenState,
            transitionSpec = {
                fadeIn(animationSpec = tween(350)) togetherWith fadeOut(animationSpec = tween(250))
            },
            label = "AuthNavigation"
        ) { state ->
            when (state) {
                AuthScreenState.HERO_WELCOME -> {
                    HeroWelcomeView(
                        onSwipeToStart = {
                            screenState = AuthScreenState.SIGN_IN
                        },
                        onSignInClick = {
                            screenState = AuthScreenState.SIGN_IN
                        },
                        onSignUpClick = {
                            screenState = AuthScreenState.CREATE_ACCOUNT
                        }
                    )
                }

                AuthScreenState.SIGN_IN -> {
                    SignInView(
                        email = email,
                        onEmailChange = { email = it },
                        password = password,
                        onPasswordChange = { password = it },
                        rememberMe = rememberMe,
                        onRememberMeChange = { rememberMe = it },
                        passwordVisible = passwordVisible,
                        onPasswordVisibleToggle = { passwordVisible = !passwordVisible },
                        onSignInSubmit = {
                            // Proceed to post-auth launch swipe screen
                            screenState = AuthScreenState.POST_AUTH_LAUNCH
                        },
                        onNavigateToSignUp = {
                            screenState = AuthScreenState.CREATE_ACCOUNT
                        },
                        onBack = {
                            screenState = AuthScreenState.HERO_WELCOME
                        }
                    )
                }

                AuthScreenState.CREATE_ACCOUNT -> {
                    CreateAccountView(
                        email = email,
                        onEmailChange = { email = it },
                        password = password,
                        onPasswordChange = { password = it },
                        confirmPassword = confirmPassword,
                        onConfirmPasswordChange = { confirmPassword = it },
                        passwordVisible = passwordVisible,
                        onPasswordVisibleToggle = { passwordVisible = !passwordVisible },
                        onSignUpSubmit = {
                            // Proceed to profile setup
                            screenState = AuthScreenState.PROFILE_SETUP
                        },
                        onNavigateToSignIn = {
                            screenState = AuthScreenState.SIGN_IN
                        },
                        onBack = {
                            screenState = AuthScreenState.HERO_WELCOME
                        }
                    )
                }

                AuthScreenState.PROFILE_SETUP -> {
                    ProfileSetupView(
                        fullName = fullName,
                        onFullNameChange = { fullName = it },
                        grade = grade,
                        onGradeChange = { grade = it },
                        location = location,
                        onLocationChange = { location = it },
                        major = major,
                        onMajorChange = { major = it },
                        onContinue = {
                            // Navigate to post-auth launch screen
                            screenState = AuthScreenState.POST_AUTH_LAUNCH
                        }
                    )
                }

                AuthScreenState.POST_AUTH_LAUNCH -> {
                    PostAuthLaunchView(
                        studentName = fullName.ifBlank { "Alex Chen" },
                        grade = grade,
                        major = major,
                        targetClub = targetClub,
                        onSwipeToLaunch = {
                            onComplete(fullName.ifBlank { "Alex Chen" }, grade, major)
                        }
                    )
                }
            }
        }
    }
}

// =========================================================================
// 1. HERO WELCOME SCREEN (Matches Dark Hero screen in center of mockup)
// =========================================================================
@Composable
private fun HeroWelcomeView(
    onSwipeToStart: () -> Unit,
    onSignInClick: () -> Unit,
    onSignUpClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkHeroCanvas)
            .padding(horizontal = 24.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top status row / District badge
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color.White.copy(alpha = 0.12f)
            ) {
                Text(
                    text = "🇺🇸 Congressional App Challenge",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                )
            }

            TextButton(onClick = onSignInClick) {
                Text("Sign In", color = WarmGoldAccent, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }

        // Center Hero Typography with lighter Inter font
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 32.dp)
        ) {
            Text(
                text = "No more —\nHurdles or\nRoadblocks\n— Just Pure\nVolunteering\nImpact.",
                fontSize = 42.sp,
                lineHeight = 52.sp,
                fontFamily = com.example.volunteerhub.ui.theme.InterFontFamily,
                fontWeight = FontWeight.Normal,
                letterSpacing = (-1).sp,
                color = Color.White
            )
        }

        // Bottom Swipe To Get Started Slider & Create Account prompt
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            SwipeToStartSlider(
                text = "Get Started",
                thumbColor = WarmGoldAccent,
                trackColor = DarkTrackBg,
                textColor = Color(0xFFE2E4E9),
                onSwipeComplete = onSwipeToStart
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "New to VolunteerHub? ",
                    fontFamily = com.example.volunteerhub.ui.theme.InterFontFamily,
                    fontSize = 13.sp,
                    color = Color.White.copy(alpha = 0.6f)
                )
                Text(
                    text = "Create Account",
                    fontFamily = com.example.volunteerhub.ui.theme.InterFontFamily,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = WarmGoldAccent,
                    modifier = Modifier.clickable { onSignUpClick() }
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

// =========================================================================
// 2. SIGN IN SCREEN ("Welcome Back!" from Mockup Right Side)
// =========================================================================
@Composable
private fun SignInView(
    email: String,
    onEmailChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    rememberMe: Boolean,
    onRememberMeChange: (Boolean) -> Unit,
    passwordVisible: Boolean,
    onPasswordVisibleToggle: () -> Unit,
    onSignInSubmit: () -> Unit,
    onNavigateToSignUp: () -> Unit,
    onBack: () -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            // Top Back button
            IconButton(
                onClick = onBack,
                modifier = Modifier.offset(x = (-12).dp)
            ) {
                Text("←", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = ForestGreenPrimary)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Heading & Subtitle
            Text(
                text = "Welcome Back!",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = ForestGreenPrimary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Bring service hours tailored to your future career.",
                fontSize = 14.sp,
                color = Color(0xFF6B7280)
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "FAQ about us",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = ForestGreenPrimary
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Email Address Input
            Text(
                text = "Email Address",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF374151),
                modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
            )
            OutlinedTextField(
                value = email,
                onValueChange = onEmailChange,
                placeholder = { Text("Enter your email", color = Color(0xFF9CA3AF), fontSize = 14.sp) },
                leadingIcon = { Text("✉️", fontSize = 16.sp) },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = InputFieldBg,
                    unfocusedContainerColor = InputFieldBg,
                    focusedBorderColor = ForestGreenPrimary,
                    unfocusedBorderColor = InputBorderColor
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Password Input
            Text(
                text = "Password",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF374151),
                modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
            )
            OutlinedTextField(
                value = password,
                onValueChange = onPasswordChange,
                placeholder = { Text("Enter your password", color = Color(0xFF9CA3AF), fontSize = 14.sp) },
                leadingIcon = { Text("🔒", fontSize = 16.sp) },
                trailingIcon = {
                    IconButton(onClick = onPasswordVisibleToggle) {
                        Text(if (passwordVisible) "👁️" else "🙈", fontSize = 16.sp)
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = InputFieldBg,
                    unfocusedContainerColor = InputFieldBg,
                    focusedBorderColor = ForestGreenPrimary,
                    unfocusedBorderColor = InputBorderColor
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Remember me & Forgot Password row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = rememberMe,
                        onCheckedChange = onRememberMeChange,
                        colors = CheckboxDefaults.colors(checkedColor = ForestGreenPrimary)
                    )
                    Text("Remember me", fontSize = 13.sp, color = Color(0xFF4B5563))
                }

                TextButton(onClick = { /* Forgot password action */ }) {
                    Text("Forgot Password?", fontSize = 13.sp, color = ForestGreenPrimary, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Primary SIGN IN Button
            Button(
                onClick = onSignInSubmit,
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
            ) {
                Text(
                    text = "SIGN IN",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // "or" Divider
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFE5E7EB))
                Text(
                    text = "or",
                    fontSize = 13.sp,
                    color = Color(0xFF9CA3AF),
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFE5E7EB))
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Social Buttons
            SocialLoginButton(
                title = "Login with Google",
                isGoogle = true,
                onClick = onSignInSubmit
            )

            Spacer(modifier = Modifier.height(10.dp))

            SocialLoginButton(
                title = "Login with Apple",
                isGoogle = false,
                onClick = onSignInSubmit
            )
        }

        // Bottom link to Sign Up
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Don't have an account? ", fontSize = 13.sp, color = Color(0xFF6B7280))
            Text(
                text = "Sign Up",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = ForestGreenPrimary,
                modifier = Modifier.clickable { onNavigateToSignUp() }
            )
        }
    }
}

// =========================================================================
// 3. CREATE ACCOUNT SCREEN (Matches Mockup Left Side)
// =========================================================================
@Composable
private fun CreateAccountView(
    email: String,
    onEmailChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    confirmPassword: String,
    onConfirmPasswordChange: (String) -> Unit,
    passwordVisible: Boolean,
    onPasswordVisibleToggle: () -> Unit,
    onSignUpSubmit: () -> Unit,
    onNavigateToSignIn: () -> Unit,
    onBack: () -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            IconButton(
                onClick = onBack,
                modifier = Modifier.offset(x = (-12).dp)
            ) {
                Text("←", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = ForestGreenPrimary)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Create Account",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = ForestGreenPrimary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Start building your verified high school service transcript.",
                fontSize = 14.sp,
                color = Color(0xFF6B7280)
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "FAQ about us",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = ForestGreenPrimary
                )
            }

            Spacer(modifier = Modifier.height(30.dp))

            // Email Field
            Text(
                text = "Email Address",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF374151),
                modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
            )
            OutlinedTextField(
                value = email,
                onValueChange = onEmailChange,
                placeholder = { Text("student@school.org", color = Color(0xFF9CA3AF), fontSize = 14.sp) },
                leadingIcon = { Text("✉️", fontSize = 16.sp) },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = InputFieldBg,
                    unfocusedContainerColor = InputFieldBg,
                    focusedBorderColor = ForestGreenPrimary,
                    unfocusedBorderColor = InputBorderColor
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Password Field
            Text(
                text = "Password",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF374151),
                modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
            )
            OutlinedTextField(
                value = password,
                onValueChange = onPasswordChange,
                placeholder = { Text("Create password", color = Color(0xFF9CA3AF), fontSize = 14.sp) },
                leadingIcon = { Text("🔒", fontSize = 16.sp) },
                trailingIcon = {
                    IconButton(onClick = onPasswordVisibleToggle) {
                        Text(if (passwordVisible) "👁️" else "🙈", fontSize = 16.sp)
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = InputFieldBg,
                    unfocusedContainerColor = InputFieldBg,
                    focusedBorderColor = ForestGreenPrimary,
                    unfocusedBorderColor = InputBorderColor
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Confirm Password Field
            Text(
                text = "Confirm Password",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF374151),
                modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
            )
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = onConfirmPasswordChange,
                placeholder = { Text("Re-enter password", color = Color(0xFF9CA3AF), fontSize = 14.sp) },
                leadingIcon = { Text("🔒", fontSize = 16.sp) },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = InputFieldBg,
                    unfocusedContainerColor = InputFieldBg,
                    focusedBorderColor = ForestGreenPrimary,
                    unfocusedBorderColor = InputBorderColor
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(26.dp))

            Button(
                onClick = onSignUpSubmit,
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
            ) {
                Text(
                    text = "CONTINUE",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFE5E7EB))
                Text(
                    text = "or",
                    fontSize = 13.sp,
                    color = Color(0xFF9CA3AF),
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFE5E7EB))
            }

            Spacer(modifier = Modifier.height(16.dp))

            SocialLoginButton(
                title = "Login with Google",
                isGoogle = true,
                onClick = onSignUpSubmit
            )

            Spacer(modifier = Modifier.height(10.dp))

            SocialLoginButton(
                title = "Login with Apple",
                isGoogle = false,
                onClick = onSignUpSubmit
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Already have an account? ", fontSize = 13.sp, color = Color(0xFF6B7280))
            Text(
                text = "Sign In",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = ForestGreenPrimary,
                modifier = Modifier.clickable { onNavigateToSignIn() }
            )
        }
    }
}

// =========================================================================
// 4. PROFILE SETUP SCREEN ("About You" / Top Center Mockup)
// =========================================================================
@Composable
private fun ProfileSetupView(
    fullName: String,
    onFullNameChange: (String) -> Unit,
    grade: String,
    onGradeChange: (String) -> Unit,
    location: String,
    onLocationChange: (String) -> Unit,
    major: String,
    onMajorChange: (String) -> Unit,
    onContinue: () -> Unit
) {
    val scrollState = rememberScrollState()
    val grades = listOf("9th Grade", "10th Grade", "11th Grade", "12th Grade")
    val majors = listOf(
        "Computer Science & Tech" to "💻",
        "Pre-Med / Healthcare" to "🏥",
        "Engineering & Robotics" to "⚙️",
        "Environmental Science" to "🌱",
        "Business & Finance" to "📈",
        "Education & Literacy" to "📚"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Set Up Profile",
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                color = ForestGreenPrimary
            )
            Text(
                text = "Customize your profile to discover aligned North Texas volunteer hours.",
                fontSize = 14.sp,
                color = Color(0xFF6B7280)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Profile Avatar Placeholder (from top center mockup)
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Box(contentAlignment = Alignment.BottomEnd) {
                    Surface(
                        shape = CircleShape,
                        color = ForestGreenLight,
                        modifier = Modifier.size(86.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("👤", fontSize = 40.sp)
                        }
                    }

                    Surface(
                        shape = CircleShape,
                        color = ForestGreenPrimary,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("✏️", fontSize = 12.sp, color = Color.White)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Full Name Input
            Text(
                text = "Full Name",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF374151),
                modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
            )
            OutlinedTextField(
                value = fullName,
                onValueChange = onFullNameChange,
                placeholder = { Text("Alex Chen", color = Color(0xFF9CA3AF)) },
                leadingIcon = { Text("🧑‍🎓", fontSize = 16.sp) },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = InputFieldBg,
                    unfocusedContainerColor = InputFieldBg,
                    focusedBorderColor = ForestGreenPrimary,
                    unfocusedBorderColor = InputBorderColor
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Location Input
            Text(
                text = "School & Location",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF374151),
                modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
            )
            OutlinedTextField(
                value = location,
                onValueChange = onLocationChange,
                placeholder = { Text("Frisco High School • TX-04", color = Color(0xFF9CA3AF)) },
                leadingIcon = { Text("📍", fontSize = 16.sp) },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = InputFieldBg,
                    unfocusedContainerColor = InputFieldBg,
                    focusedBorderColor = ForestGreenPrimary,
                    unfocusedBorderColor = InputBorderColor
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Grade Chips
            Text(
                text = "Current Grade",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF374151),
                modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                grades.forEach { g ->
                    val isSelected = grade == g
                    Surface(
                        onClick = { onGradeChange(g) },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) ForestGreenPrimary else InputFieldBg,
                        border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, InputBorderColor),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = g.replace(" Grade", ""),
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) Color.White else Color(0xFF4B5563),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 10.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Intended Pathway / Major
            Text(
                text = "Intended Major / Career Pathway",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF374151),
                modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
            )
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                majors.forEach { (maj, emoji) ->
                    val isSelected = major == maj
                    Surface(
                        onClick = { onMajorChange(maj) },
                        shape = RoundedCornerShape(14.dp),
                        color = if (isSelected) ForestGreenLight else InputFieldBg,
                        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, ForestGreenPrimary) else null,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(emoji, fontSize = 20.sp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = maj,
                                fontSize = 14.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) ForestGreenPrimary else Color(0xFF1F2937)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        Button(
            onClick = onContinue,
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary),
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
        ) {
            Text(
                text = "CONTINUE",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = Color.White
            )
        }
    }
}

// =========================================================================
// 5. POST-AUTH LAUNCH HOME SCREEN (Swipe to Get Started after Logging In)
// =========================================================================
@Composable
private fun PostAuthLaunchView(
    studentName: String,
    grade: String,
    major: String,
    targetClub: String,
    onSwipeToLaunch: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkHeroCanvas)
            .padding(24.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.padding(top = 16.dp)) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = ForestGreenPrimary.copy(alpha = 0.6f)
            ) {
                Text(
                    text = "✓ AUTHENTICATED",
                    color = WarmGoldAccent,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Welcome,\n$studentName 👋",
                fontSize = 36.sp,
                lineHeight = 42.sp,
                fontWeight = FontWeight.Black,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "$grade • $major",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = WarmGoldAccent
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Verified Setup Card
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = DarkHeroCard,
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = targetClub,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Congressional District TX-04",
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.6f)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = WarmGoldAccent.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "14/30 hrs",
                                color = WarmGoldAccent,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("🎯", fontSize = 20.sp)
                        Column {
                            Text(
                                text = "AI Vector Match Engine Primed",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Top STEM & Tech opportunities prioritized",
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            }
        }

        // The requested "Swipe to Get Started" slider after logging in / signing in!
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Swipe to launch your dashboard",
                fontSize = 13.sp,
                color = Color.White.copy(alpha = 0.7f),
                modifier = Modifier.padding(bottom = 12.dp)
            )

            SwipeToStartSlider(
                text = "Swipe to Get Started >>",
                thumbColor = WarmGoldAccent,
                trackColor = DarkTrackBg,
                textColor = Color(0xFFE2E4E9),
                onSwipeComplete = onSwipeToLaunch
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// =========================================================================
// REUSABLE COMPONENTS
// =========================================================================

@Composable
fun SwipeToStartSlider(
    modifier: Modifier = Modifier,
    text: String = "Get Started",
    thumbColor: Color = WarmGoldAccent,
    trackColor: Color = DarkTrackBg,
    textColor: Color = Color(0xFFE2E4E9),
    onSwipeComplete: () -> Unit
) {
    var widthPx by remember { mutableFloatStateOf(0f) }
    val thumbSizeDp = 52.dp
    val density = LocalDensity.current
    val thumbSizePx = with(density) { thumbSizeDp.toPx() }
    val paddingPx = with(density) { 6.dp.toPx() }

    val offsetX = remember { Animatable(0f) }
    val coroutineScope = rememberCoroutineScope()
    var isCompleted by remember { mutableStateOf(false) }

    val maxDragPx = (widthPx - thumbSizePx - (paddingPx * 2)).coerceAtLeast(0f)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp)
            .background(trackColor, RoundedCornerShape(32.dp))
            .onGloballyPositioned { coordinates ->
                widthPx = coordinates.size.width.toFloat()
            }
            .padding(6.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        // Centered static instruction text
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                fontFamily = com.example.volunteerhub.ui.theme.InterFontFamily,
                color = textColor,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.5.sp
            )
        }

        // Draggable Circular Thumb
        Box(
            modifier = Modifier
                .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                .size(thumbSizeDp)
                .background(thumbColor, CircleShape)
                .pointerInput(maxDragPx, isCompleted) {
                    if (isCompleted || maxDragPx <= 0f) return@pointerInput
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            coroutineScope.launch {
                                if (offsetX.value >= maxDragPx * 0.70f) {
                                    isCompleted = true
                                    offsetX.animateTo(maxDragPx, tween(180))
                                    onSwipeComplete()
                                } else {
                                    offsetX.animateTo(0f, spring(stiffness = Spring.StiffnessMediumLow))
                                }
                            }
                        },
                        onHorizontalDrag = { change, dragAmount ->
                            change.consume()
                            coroutineScope.launch {
                                val newOffset = (offsetX.value + dragAmount).coerceIn(0f, maxDragPx)
                                offsetX.snapTo(newOffset)
                            }
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "»»",
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF111317)
            )
        }
    }
}

@Composable
private fun SocialLoginButton(
    title: String,
    isGoogle: Boolean = false,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(26.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB)),
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isGoogle) {
                Surface(
                    shape = CircleShape,
                    color = Color(0xFF4285F4).copy(alpha = 0.12f),
                    modifier = Modifier.size(24.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "G",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF4285F4)
                        )
                    }
                }
            } else {
                Surface(
                    shape = CircleShape,
                    color = Color.Black.copy(alpha = 0.08f),
                    modifier = Modifier.size(24.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "🍎",
                            fontSize = 13.sp
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF374151)
            )
        }
    }
}
