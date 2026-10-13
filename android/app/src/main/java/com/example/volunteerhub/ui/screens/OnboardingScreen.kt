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
import com.example.volunteerhub.ui.theme.*
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

enum class AuthScreenState {
    HERO_WELCOME,      // Fluid Dreamy Pastel hero screen with Inter typography & swipe slider
    SIGN_IN,           // "Welcome Back!" view with soft dreamy pastel accents
    CREATE_ACCOUNT,    // "Create Account" view with soft dreamy pastel accents
    PROFILE_SETUP,     // Student details: name, grade, location, major
    POST_AUTH_LAUNCH   // Post-login celebration launch view with swipe slider
}

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
        color = Color(0xFFFDFBFE)
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
// 1. HERO WELCOME SCREEN (Nostalgic Y2K & Soft Dreamy Pastels)
// =========================================================================
@Composable
private fun HeroWelcomeView(
    onSwipeToStart: () -> Unit,
    onSignInClick: () -> Unit,
    onSignUpClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DreamyHeroGradient)
    ) {
        // Decorative soft ambient pastel blobs for ethereal depth
        Box(
            modifier = Modifier
                .size(260.dp)
                .offset(x = 180.dp, y = (-40).dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            PastelBlushPink.copy(alpha = 0.7f),
                            Color.Transparent
                        )
                    )
                )
        )

        Box(
            modifier = Modifier
                .size(240.dp)
                .offset(x = (-80).dp, y = 300.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            PastelSkyBlue.copy(alpha = 0.65f),
                            Color.Transparent
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top status row / District badge + Sign In button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White.copy(alpha = 0.85f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, PastelLavenderDeep.copy(alpha = 0.8f)),
                    shadowElevation = 2.dp
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "✦",
                            color = VibrantPeriwinkle,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Congressional App Challenge 🇺🇸",
                            color = DeepRoyalViolet,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = InterFontFamily
                        )
                    }
                }

                Surface(
                    onClick = onSignInClick,
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White.copy(alpha = 0.85f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, PastelLavenderDeep.copy(alpha = 0.6f)),
                    shadowElevation = 2.dp
                ) {
                    Text(
                        text = "Sign In",
                        color = DeepRoyalViolet,
                        fontWeight = FontWeight.Bold,
                        fontFamily = InterFontFamily,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                }
            }

            // Center Hero Typography with Inter font & dreamy Y2K styling
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = PastelBlushPink.copy(alpha = 0.9f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, VibrantBubblegum.copy(alpha = 0.35f))
                ) {
                    Text(
                        text = "✦ FOR HIGH SCHOOL CHANGEMAKERS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = InterFontFamily,
                        letterSpacing = 0.8.sp,
                        color = DeepRoseBerry,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "No more —\nHurdles or\nRoadblocks\n— Just Pure\nVolunteering\nImpact.",
                    fontSize = 42.sp,
                    lineHeight = 52.sp,
                    fontFamily = InterFontFamily,
                    fontWeight = FontWeight.Normal,
                    letterSpacing = (-1).sp,
                    color = Y2KTextDeep
                )
            }

            // Bottom Swipe To Get Started Slider & Create Account prompt
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                SwipeToStartSlider(
                    text = "Get Started",
                    thumbGradient = Y2KButtonGradient,
                    trackColor = Color.White.copy(alpha = 0.92f),
                    trackBorderColor = PastelLavenderDeep.copy(alpha = 0.7f),
                    textColor = Y2KTextSecondary,
                    onSwipeComplete = onSwipeToStart
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "New to VolunteerHub? ",
                        fontFamily = InterFontFamily,
                        fontSize = 13.sp,
                        color = Y2KTextMuted
                    )
                    Text(
                        text = "Create Account",
                        fontFamily = InterFontFamily,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = VibrantPeriwinkle,
                        modifier = Modifier.clickable { onSignUpClick() }
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

// =========================================================================
// 2. SIGN IN SCREEN ("Welcome Back!" in Dreamy Pastel Theme)
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        PastelLavenderLight,
                        Color.White,
                        PastelBlushLight
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                // Top Back button
                Surface(
                    onClick = onBack,
                    shape = CircleShape,
                    color = Color.White,
                    border = androidx.compose.foundation.BorderStroke(1.dp, PastelLavenderDeep.copy(alpha = 0.6f)),
                    shadowElevation = 2.dp,
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("←", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = DeepRoyalViolet)
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Heading & Subtitle
                Text(
                    text = "Welcome Back!",
                    fontSize = 32.sp,
                    fontFamily = InterFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = Y2KTextDeep
                )

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Bring service hours tailored to your future career.",
                    fontSize = 14.sp,
                    fontFamily = InterFontFamily,
                    color = Y2KTextSecondary
                )

                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = PastelSkyBlue.copy(alpha = 0.8f)
                ) {
                    Text(
                        text = "✦ TX-04 Student Verification Active",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = InterFontFamily,
                        color = DeepSkyBlue,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(30.dp))

                // Email Address Input
                Text(
                    text = "Email Address",
                    fontSize = 12.sp,
                    fontFamily = InterFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    color = Y2KTextDeep,
                    modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = onEmailChange,
                    placeholder = { Text("Enter your email", color = Color(0xFF94A3B8), fontSize = 14.sp, fontFamily = InterFontFamily) },
                    leadingIcon = { Text("✉️", fontSize = 16.sp) },
                    singleLine = true,
                    shape = RoundedCornerShape(18.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color(0xFFFBFBFE),
                        focusedBorderColor = VibrantPeriwinkle,
                        unfocusedBorderColor = Y2KBorderSoft
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Password Input
                Text(
                    text = "Password",
                    fontSize = 12.sp,
                    fontFamily = InterFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    color = Y2KTextDeep,
                    modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = onPasswordChange,
                    placeholder = { Text("Enter your password", color = Color(0xFF94A3B8), fontSize = 14.sp, fontFamily = InterFontFamily) },
                    leadingIcon = { Text("🔒", fontSize = 16.sp) },
                    trailingIcon = {
                        IconButton(onClick = onPasswordVisibleToggle) {
                            Icon(
                                painter = androidx.compose.ui.res.painterResource(
                                    if (passwordVisible) com.example.volunteerhub.R.drawable.ic_eye_open else com.example.volunteerhub.R.drawable.ic_eye_closed
                                ),
                                contentDescription = if (passwordVisible) "Hide password" else "Show password",
                                tint = if (passwordVisible) VibrantPeriwinkle else Y2KTextMuted,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true,
                    shape = RoundedCornerShape(18.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color(0xFFFBFBFE),
                        focusedBorderColor = VibrantPeriwinkle,
                        unfocusedBorderColor = Y2KBorderSoft
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
                            colors = CheckboxDefaults.colors(checkedColor = VibrantPeriwinkle)
                        )
                        Text("Remember me", fontSize = 13.sp, fontFamily = InterFontFamily, color = Y2KTextSecondary)
                    }

                    TextButton(onClick = { /* Forgot password action */ }) {
                        Text("Forgot Password?", fontSize = 13.sp, fontFamily = InterFontFamily, color = VibrantPeriwinkle, fontWeight = FontWeight.SemiBold)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Primary SIGN IN Button (Vibrant Y2K Gradient)
                Surface(
                    onClick = onSignInSubmit,
                    shape = RoundedCornerShape(28.dp),
                    shadowElevation = 6.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Y2KButtonGradient),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "SIGN IN",
                            fontSize = 15.sp,
                            fontFamily = InterFontFamily,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // "or" Divider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HorizontalDivider(modifier = Modifier.weight(1f), color = Y2KBorderSoft)
                    Text(
                        text = "or",
                        fontSize = 13.sp,
                        fontFamily = InterFontFamily,
                        color = Color(0xFF94A3B8),
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    HorizontalDivider(modifier = Modifier.weight(1f), color = Y2KBorderSoft)
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
                Text("Don't have an account? ", fontSize = 13.sp, fontFamily = InterFontFamily, color = Y2KTextMuted)
                Text(
                    text = "Sign Up",
                    fontSize = 13.sp,
                    fontFamily = InterFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = VibrantPeriwinkle,
                    modifier = Modifier.clickable { onNavigateToSignUp() }
                )
            }
        }
    }
}

// =========================================================================
// 3. CREATE ACCOUNT SCREEN (Dreamy Pastel Aesthetic)
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        PastelBlushLight,
                        Color.White,
                        PastelLavenderLight
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Surface(
                    onClick = onBack,
                    shape = CircleShape,
                    color = Color.White,
                    border = androidx.compose.foundation.BorderStroke(1.dp, PastelLavenderDeep.copy(alpha = 0.6f)),
                    shadowElevation = 2.dp,
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("←", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = DeepRoyalViolet)
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = "Create Account",
                    fontSize = 32.sp,
                    fontFamily = InterFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = Y2KTextDeep
                )

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Start building your verified high school service transcript.",
                    fontSize = 14.sp,
                    fontFamily = InterFontFamily,
                    color = Y2KTextSecondary
                )

                Spacer(modifier = Modifier.height(30.dp))

                // Email Field
                Text(
                    text = "Email Address",
                    fontSize = 12.sp,
                    fontFamily = InterFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    color = Y2KTextDeep,
                    modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = onEmailChange,
                    placeholder = { Text("student@school.org", color = Color(0xFF94A3B8), fontSize = 14.sp, fontFamily = InterFontFamily) },
                    leadingIcon = { Text("✉️", fontSize = 16.sp) },
                    singleLine = true,
                    shape = RoundedCornerShape(18.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color(0xFFFBFBFE),
                        focusedBorderColor = VibrantPeriwinkle,
                        unfocusedBorderColor = Y2KBorderSoft
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Password Field
                Text(
                    text = "Password",
                    fontSize = 12.sp,
                    fontFamily = InterFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    color = Y2KTextDeep,
                    modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = onPasswordChange,
                    placeholder = { Text("Create password", color = Color(0xFF94A3B8), fontSize = 14.sp, fontFamily = InterFontFamily) },
                    leadingIcon = { Text("🔒", fontSize = 16.sp) },
                    trailingIcon = {
                        IconButton(onClick = onPasswordVisibleToggle) {
                            Icon(
                                painter = androidx.compose.ui.res.painterResource(
                                    if (passwordVisible) com.example.volunteerhub.R.drawable.ic_eye_open else com.example.volunteerhub.R.drawable.ic_eye_closed
                                ),
                                contentDescription = if (passwordVisible) "Hide password" else "Show password",
                                tint = if (passwordVisible) VibrantPeriwinkle else Y2KTextMuted,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true,
                    shape = RoundedCornerShape(18.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color(0xFFFBFBFE),
                        focusedBorderColor = VibrantPeriwinkle,
                        unfocusedBorderColor = Y2KBorderSoft
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Confirm Password Field
                Text(
                    text = "Confirm Password",
                    fontSize = 12.sp,
                    fontFamily = InterFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    color = Y2KTextDeep,
                    modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
                )
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = onConfirmPasswordChange,
                    placeholder = { Text("Re-enter password", color = Color(0xFF94A3B8), fontSize = 14.sp, fontFamily = InterFontFamily) },
                    leadingIcon = { Text("🔒", fontSize = 16.sp) },
                    trailingIcon = {
                        IconButton(onClick = onPasswordVisibleToggle) {
                            Icon(
                                painter = androidx.compose.ui.res.painterResource(
                                    if (passwordVisible) com.example.volunteerhub.R.drawable.ic_eye_open else com.example.volunteerhub.R.drawable.ic_eye_closed
                                ),
                                contentDescription = if (passwordVisible) "Hide password" else "Show password",
                                tint = if (passwordVisible) VibrantPeriwinkle else Y2KTextMuted,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true,
                    shape = RoundedCornerShape(18.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color(0xFFFBFBFE),
                        focusedBorderColor = VibrantPeriwinkle,
                        unfocusedBorderColor = Y2KBorderSoft
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(26.dp))

                // Continue Button
                Surface(
                    onClick = onSignUpSubmit,
                    shape = RoundedCornerShape(28.dp),
                    shadowElevation = 6.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Y2KButtonGradient),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "CONTINUE",
                            fontSize = 15.sp,
                            fontFamily = InterFontFamily,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HorizontalDivider(modifier = Modifier.weight(1f), color = Y2KBorderSoft)
                    Text(
                        text = "or",
                        fontSize = 13.sp,
                        fontFamily = InterFontFamily,
                        color = Color(0xFF94A3B8),
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    HorizontalDivider(modifier = Modifier.weight(1f), color = Y2KBorderSoft)
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
                Text("Already have an account? ", fontSize = 13.sp, fontFamily = InterFontFamily, color = Y2KTextMuted)
                Text(
                    text = "Sign In",
                    fontSize = 13.sp,
                    fontFamily = InterFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = VibrantPeriwinkle,
                    modifier = Modifier.clickable { onNavigateToSignIn() }
                )
            }
        }
    }
}

// =========================================================================
// 4. PROFILE SETUP SCREEN (Soft Dreamy Pastels & Y2K Chips)
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        PastelLavenderLight,
                        Color.White,
                        PastelSkyLight
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Set Up Profile",
                    fontSize = 30.sp,
                    fontFamily = InterFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = Y2KTextDeep
                )

                Text(
                    text = "Customize your profile to discover aligned North Texas volunteer hours.",
                    fontSize = 14.sp,
                    fontFamily = InterFontFamily,
                    color = Y2KTextSecondary
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Profile Avatar Placeholder with Dreamy Iridescent Ring
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Box(contentAlignment = Alignment.BottomEnd) {
                        Surface(
                            shape = CircleShape,
                            color = Color.White,
                            border = androidx.compose.foundation.BorderStroke(3.dp, Y2KButtonGradient),
                            shadowElevation = 4.dp,
                            modifier = Modifier.size(92.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(PastelLavenderLight),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("👤", fontSize = 42.sp)
                            }
                        }

                        Surface(
                            shape = CircleShape,
                            color = VibrantPeriwinkle,
                            shadowElevation = 2.dp,
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
                    fontFamily = InterFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    color = Y2KTextDeep,
                    modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
                )
                OutlinedTextField(
                    value = fullName,
                    onValueChange = onFullNameChange,
                    placeholder = { Text("Alex Chen", color = Color(0xFF94A3B8), fontFamily = InterFontFamily) },
                    leadingIcon = { Text("🧑‍🎓", fontSize = 16.sp) },
                    singleLine = true,
                    shape = RoundedCornerShape(18.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color(0xFFFBFBFE),
                        focusedBorderColor = VibrantPeriwinkle,
                        unfocusedBorderColor = Y2KBorderSoft
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Location Input
                Text(
                    text = "School & Location",
                    fontSize = 12.sp,
                    fontFamily = InterFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    color = Y2KTextDeep,
                    modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
                )
                OutlinedTextField(
                    value = location,
                    onValueChange = onLocationChange,
                    placeholder = { Text("Frisco High School • TX-04", color = Color(0xFF94A3B8), fontFamily = InterFontFamily) },
                    leadingIcon = { Text("📍", fontSize = 16.sp) },
                    singleLine = true,
                    shape = RoundedCornerShape(18.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color(0xFFFBFBFE),
                        focusedBorderColor = VibrantPeriwinkle,
                        unfocusedBorderColor = Y2KBorderSoft
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Grade Chips
                Text(
                    text = "Current Grade",
                    fontSize = 12.sp,
                    fontFamily = InterFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    color = Y2KTextDeep,
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
                            shape = RoundedCornerShape(14.dp),
                            color = if (isSelected) PastelLavender else Color.White,
                            border = androidx.compose.foundation.BorderStroke(
                                if (isSelected) 1.5.dp else 1.dp,
                                if (isSelected) VibrantPeriwinkle else Y2KBorderSoft
                            ),
                            shadowElevation = if (isSelected) 3.dp else 0.dp,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = g.replace(" Grade", ""),
                                fontSize = 12.sp,
                                fontFamily = InterFontFamily,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) DeepRoyalViolet else Y2KTextSecondary,
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
                    fontFamily = InterFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    color = Y2KTextDeep,
                    modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
                )
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    majors.forEach { (maj, emoji) ->
                        val isSelected = major == maj
                        Surface(
                            onClick = { onMajorChange(maj) },
                            shape = RoundedCornerShape(16.dp),
                            color = if (isSelected) PastelLavenderLight else Color.White,
                            border = androidx.compose.foundation.BorderStroke(
                                if (isSelected) 1.5.dp else 1.dp,
                                if (isSelected) VibrantPeriwinkle else Y2KBorderSoft
                            ),
                            shadowElevation = if (isSelected) 2.dp else 0.dp,
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
                                    fontFamily = InterFontFamily,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) DeepRoyalViolet else Y2KTextDeep
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            Surface(
                onClick = onContinue,
                shape = RoundedCornerShape(28.dp),
                shadowElevation = 6.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Y2KButtonGradient),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "CONTINUE",
                        fontSize = 15.sp,
                        fontFamily = InterFontFamily,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = Color.White
                    )
                }
            }
        }
    }
}

// =========================================================================
// 5. POST-AUTH LAUNCH HOME SCREEN (Dreamy Pastel Celebration)
// =========================================================================
@Composable
private fun PostAuthLaunchView(
    studentName: String,
    grade: String,
    major: String,
    targetClub: String,
    onSwipeToLaunch: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DreamyHeroGradient)
    ) {
        // Soft ambient pastel glow in background
        Box(
            modifier = Modifier
                .size(240.dp)
                .offset(x = 160.dp, y = (-20).dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            PastelBlushPink.copy(alpha = 0.7f),
                            Color.Transparent
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.padding(top = 16.dp)) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = PastelMint,
                    border = androidx.compose.foundation.BorderStroke(1.dp, VibrantMint.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = "✓ AUTHENTICATED",
                        color = DeepEmerald,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = InterFontFamily,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Welcome,\n$studentName 👋",
                    fontSize = 36.sp,
                    lineHeight = 42.sp,
                    fontFamily = InterFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = Y2KTextDeep
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "$grade • $major",
                    fontSize = 15.sp,
                    fontFamily = InterFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    color = VibrantPeriwinkle
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Verified Setup Card (Frosted Glass Pastel)
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = Color.White.copy(alpha = 0.90f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, PastelLavenderDeep.copy(alpha = 0.8f)),
                    shadowElevation = 6.dp,
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
                                    fontFamily = InterFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    color = Y2KTextDeep
                                )
                                Text(
                                    text = "Congressional District TX-04",
                                    fontSize = 12.sp,
                                    fontFamily = InterFontFamily,
                                    color = Y2KTextSecondary
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = PastelButtercup,
                                border = androidx.compose.foundation.BorderStroke(1.dp, VibrantButtercup.copy(alpha = 0.4f))
                            ) {
                                Text(
                                    text = "14/30 hrs",
                                    color = Color(0xFFB45309),
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = InterFontFamily,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider(color = PastelLavenderDeep.copy(alpha = 0.4f))
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
                                    fontFamily = InterFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    color = Y2KTextDeep
                                )
                                Text(
                                    text = "Top STEM & Tech opportunities prioritized",
                                    fontSize = 11.sp,
                                    fontFamily = InterFontFamily,
                                    color = Y2KTextSecondary
                                )
                            }
                        }
                    }
                }
            }

            // Swipe to Get Started slider
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Swipe to launch your dashboard",
                    fontSize = 13.sp,
                    fontFamily = InterFontFamily,
                    color = Y2KTextSecondary,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                SwipeToStartSlider(
                    text = "Swipe to Get Started >>",
                    thumbGradient = Y2KButtonGradient,
                    trackColor = Color.White.copy(alpha = 0.92f),
                    trackBorderColor = PastelLavenderDeep.copy(alpha = 0.7f),
                    textColor = Y2KTextSecondary,
                    onSwipeComplete = onSwipeToLaunch
                )

                Spacer(modifier = Modifier.height(16.dp))
            }
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
    thumbGradient: Brush = Y2KButtonGradient,
    trackColor: Color = Color.White.copy(alpha = 0.92f),
    trackBorderColor: Color = PastelLavenderDeep.copy(alpha = 0.7f),
    textColor: Color = Y2KTextSecondary,
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
            .shadow(4.dp, RoundedCornerShape(32.dp), spotColor = VibrantPeriwinkle.copy(alpha = 0.25f))
            .background(trackColor, RoundedCornerShape(32.dp))
            .border(1.dp, trackBorderColor, RoundedCornerShape(32.dp))
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
                fontFamily = InterFontFamily,
                color = textColor,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.5.sp
            )
        }

        // Draggable Circular Thumb with Vibrant Y2K Gradient
        Box(
            modifier = Modifier
                .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                .size(thumbSizeDp)
                .shadow(4.dp, CircleShape, spotColor = VibrantBubblegum.copy(alpha = 0.4f))
                .background(thumbGradient, CircleShape)
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
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                color = Color.White
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
        border = androidx.compose.foundation.BorderStroke(1.dp, Y2KBorderSoft),
        shadowElevation = 1.dp,
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
                fontFamily = InterFontFamily,
                fontWeight = FontWeight.SemiBold,
                color = Y2KTextDeep
            )
        }
    }
}
