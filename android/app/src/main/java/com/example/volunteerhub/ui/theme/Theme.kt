package com.example.volunteerhub.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = VibrantPeriwinkle,
    onPrimary = Color.White,
    primaryContainer = DeepRoyalViolet,
    onPrimaryContainer = PastelLavender,
    secondary = VibrantBubblegum,
    onSecondary = Color.White,
    secondaryContainer = DeepRoseBerry,
    onSecondaryContainer = PastelBlushPink,
    tertiary = VibrantSkyBlue,
    onTertiary = Color.White,
    tertiaryContainer = DeepSkyBlue,
    onTertiaryContainer = PastelSkyBlue,
    background = Color(0xFF13111C),
    onBackground = Color(0xFFF5F3FF),
    surface = Color(0xFF1E1B2E),
    onSurface = Color(0xFFF5F3FF),
    surfaceVariant = Color(0xFF2D2845),
    onSurfaceVariant = Color(0xFFDDD6FE)
)

private val LightColorScheme = lightColorScheme(
    primary = VibrantPeriwinkle,
    onPrimary = Color.White,
    primaryContainer = PastelLavender,
    onPrimaryContainer = DeepRoyalViolet,
    secondary = VibrantBubblegum,
    onSecondary = Color.White,
    secondaryContainer = PastelBlushPink,
    onSecondaryContainer = DeepRoseBerry,
    tertiary = VibrantSkyBlue,
    onTertiary = Color.White,
    tertiaryContainer = PastelSkyBlue,
    onTertiaryContainer = DeepSkyBlue,
    background = Color(0xFFFDFBFE),
    onBackground = Y2KTextDeep,
    surface = Color.White,
    onSurface = Y2KTextDeep,
    surfaceVariant = PastelLavenderLight,
    onSurfaceVariant = Y2KTextSecondary
)

@Composable
fun VolunteerHubTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Set false to prioritize Y2K & Dreamy Pastel branding
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}