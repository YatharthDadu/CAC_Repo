package com.example.volunteerhub.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/** Semantic colors that Material 3's scheme has no slot for. */
@Immutable
data class HubColors(
    val ink: Color,
    val inkSoft: Color,
    val line: Color,
    val canvas: Color,
    val card: Color,
    val reward: Color,
    val rewardDeep: Color,
    val rewardSoft: Color,
    val success: Color,
    val successDeep: Color,
    val successSoft: Color,
    val urgent: Color,
    val urgentDeep: Color,
    val urgentSoft: Color,
    val info: Color,
    val infoDeep: Color,
    val infoSoft: Color,
    val brandDeep: Color,
    val brandSoft: Color,
    val isDark: Boolean
)

private val LightHub = HubColors(
    ink = Ink, inkSoft = InkSoft, line = Line, canvas = Cream, card = Paper,
    reward = Sun, rewardDeep = SunDeep, rewardSoft = SunSoft,
    success = Mint, successDeep = MintDeep, successSoft = MintSoft,
    urgent = Coral, urgentDeep = CoralDeep, urgentSoft = CoralSoft,
    info = Sky, infoDeep = SkyDeep, infoSoft = SkySoft,
    brandDeep = VioletDeep, brandSoft = VioletSoft, isDark = false
)

private val DarkHub = HubColors(
    ink = NightInk, inkSoft = NightInkSoft, line = NightLine, canvas = Night, card = NightSurface,
    reward = Sun, rewardDeep = SunDeep, rewardSoft = Color(0xFF4A3D10),
    success = Mint, successDeep = Color(0xFF7BE8C3), successSoft = Color(0xFF123F35),
    urgent = Coral, urgentDeep = Color(0xFFFF9C94), urgentSoft = Color(0xFF4A2224),
    info = Sky, infoDeep = Color(0xFF8AD6FF), infoSoft = Color(0xFF173A52),
    brandDeep = Color(0xFF3A1B94), brandSoft = Color(0xFF362A6B), isDark = true
)

val LocalHubColors = staticCompositionLocalOf { LightHub }

object Hub {
    val colors: HubColors
        @Composable get() = LocalHubColors.current
}

private val LightScheme = lightColorScheme(
    primary = Violet, onPrimary = Color.White,
    primaryContainer = VioletSoft, onPrimaryContainer = VioletDeep,
    secondary = Sun, onSecondary = Ink,
    secondaryContainer = SunSoft, onSecondaryContainer = Ink,
    tertiary = Mint, onTertiary = Ink,
    tertiaryContainer = MintSoft, onTertiaryContainer = MintDeep,
    error = Coral, onError = Color.White,
    errorContainer = CoralSoft, onErrorContainer = CoralDeep,
    background = Cream, onBackground = Ink,
    surface = Paper, onSurface = Ink,
    surfaceVariant = VioletSoft, onSurfaceVariant = InkSoft,
    outline = Line, outlineVariant = Line
)

private val DarkScheme = darkColorScheme(
    primary = Color(0xFF9B7BFF), onPrimary = Color(0xFF1B0B52),
    primaryContainer = Color(0xFF362A6B), onPrimaryContainer = Color(0xFFE5DBFF),
    secondary = Sun, onSecondary = Ink,
    secondaryContainer = Color(0xFF4A3D10), onSecondaryContainer = SunSoft,
    tertiary = Mint, onTertiary = Ink,
    tertiaryContainer = Color(0xFF123F35), onTertiaryContainer = Color(0xFF7BE8C3),
    error = Color(0xFFFF8A80), onError = Ink,
    errorContainer = Color(0xFF4A2224), onErrorContainer = Color(0xFFFFD9D6),
    background = Night, onBackground = NightInk,
    surface = NightSurface, onSurface = NightInk,
    surfaceVariant = Color(0xFF2D2557), onSurfaceVariant = NightInkSoft,
    outline = NightLine, outlineVariant = NightLine
)

@Composable
fun VolunteerHubTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(LocalHubColors provides if (darkTheme) DarkHub else LightHub) {
        MaterialTheme(
            colorScheme = if (darkTheme) DarkScheme else LightScheme,
            typography = Typography,
            content = content
        )
    }
}
