package com.example.volunteerhub.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// =========================================================================
// NOSTALGIC Y2K & SOFT DREAMY PASTELS DESIGN SYSTEM
// Arbisoft Color Theory & UI/UX Pro Max Design Intelligence
// =========================================================================

// --- Dreamy Pastels (Ethereal, calming, retro-youthful) ---
val PastelLavender = Color(0xFFEDE9FE)       // Soft lavender 100
val PastelLavenderLight = Color(0xFFF5F3FF)  // Softest lavender frost 50
val PastelLavenderDeep = Color(0xFFC4B5FD)   // Lilac border 300
val VibrantPeriwinkle = Color(0xFF8B5CF6)    // Periwinkle accent 500
val DeepRoyalViolet = Color(0xFF4C1D95)      // Accessible dark violet 900

val PastelBlushPink = Color(0xFFFCE7F3)      // Soft cotton candy pink 100
val PastelBlushLight = Color(0xFFFDF2F8)     // Softest blush 50
val VibrantBubblegum = Color(0xFFF472B6)     // Y2K Bubblegum pink 400
val DeepRoseBerry = Color(0xFFDB2777)        // Rose berry 600

val PastelSkyBlue = Color(0xFFE0F2FE)        // Dreamy baby blue 100
val PastelSkyLight = Color(0xFFF0F9FF)       // Light cloud blue 50
val VibrantSkyBlue = Color(0xFF38BDF8)       // Fluid sky accent 400
val DeepSkyBlue = Color(0xFF0284C7)          // Ocean blue 600

val PastelMint = Color(0xFFD1FAE5)           // Calming mint 100
val PastelMintLight = Color(0xFFECFDF5)      // Fresh light sage 50
val VibrantMint = Color(0xFF34D399)          // Mint success 400
val DeepEmerald = Color(0xFF059669)          // Emerald 600

val PastelPeach = Color(0xFFFFEDD5)          // Warm retro peach 100
val PastelButtercup = Color(0xFFFEF3C7)      // Buttercup yellow 100
val VibrantButtercup = Color(0xFFFBBF24)     // Warm gold buttercup 400
val VibrantSunsetOrange = Color(0xFFFB923C)  // Y2K sunset orange 400

// --- Contrast & Typography (WCAG AA/AAA > 4.5:1 on light pastels) ---
val Y2KTextDeep = Color(0xFF1E1B4B)          // Deep plum indigo (12:1 contrast)
val Y2KTextSecondary = Color(0xFF475569)     // Slate violet (7:1 contrast)
val Y2KTextMuted = Color(0xFF64748B)         // Supportive text (4.8:1 contrast)
val Y2KBorderSoft = Color(0xFFE2E8F0)        // Clean subtle border
val Y2KBorderPastel = Color(0xFFDDD6FE)      // Soft periwinkle border

// --- Translucent Glass & Surface Containers ---
val FrostedGlassWhite = Color(0xFFFFFFFF).copy(alpha = 0.88f)
val MarshmallowWhite = Color(0xFFFFFFFF)

// --- Fluid Gradients ---
val DreamyAuroraGradient = Brush.verticalGradient(
    listOf(
        Color(0xFFEDE9FE), // Lavender
        Color(0xFFFCE7F3), // Blush
        Color(0xFFE0F2FE)  // Baby Blue
    )
)

val DreamyHeroGradient = Brush.verticalGradient(
    listOf(
        Color(0xFFF3E8FF), // Soft Lilac
        Color(0xFFFCE7F3), // Cotton Candy
        Color(0xFFE0F2FE), // Sky Cloud
        Color(0xFFEDE9FE)  // Lavender Dew
    )
)

val Y2KButtonGradient = Brush.horizontalGradient(
    listOf(
        Color(0xFF8B5CF6), // Vibrant Periwinkle
        Color(0xFFEC4899)  // Bubblegum Pink
    )
)

val Y2KPeachRoseGradient = Brush.horizontalGradient(
    listOf(
        Color(0xFFFB923C), // Sunset Peach
        Color(0xFFF472B6)  // Blush Rose
    )
)

val Y2KSkyLavenderGradient = Brush.horizontalGradient(
    listOf(
        Color(0xFF38BDF8), // Sky Blue
        Color(0xFFA78BFA)  // Lavender
    )
)