package com.example.volunteerhub.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.volunteerhub.R

// Both files are variable fonts; weights are applied with variation settings (API 26+),
// older devices fall back to the default weight of the face.
@OptIn(ExperimentalTextApi::class)
private fun variable(res: Int, weight: FontWeight) = Font(
    resId = res,
    weight = weight,
    variationSettings = FontVariation.Settings(FontVariation.weight(weight.weight))
)

/** Rounded, friendly display face for headings, numbers and buttons. */
val DisplayFontFamily = FontFamily(
    variable(R.font.nunito, FontWeight.SemiBold),
    variable(R.font.nunito, FontWeight.Bold),
    variable(R.font.nunito, FontWeight.ExtraBold),
    variable(R.font.nunito, FontWeight.Black)
)

/** Neutral, highly legible face for body copy. */
val BodyFontFamily = FontFamily(
    variable(R.font.inter, FontWeight.Normal),
    variable(R.font.inter, FontWeight.Medium),
    variable(R.font.inter, FontWeight.SemiBold),
    variable(R.font.inter, FontWeight.Bold)
)

private fun display(size: Int, line: Int, weight: FontWeight = FontWeight.ExtraBold) = TextStyle(
    fontFamily = DisplayFontFamily, fontWeight = weight, fontSize = size.sp, lineHeight = line.sp
)

private fun body(size: Int, line: Int, weight: FontWeight = FontWeight.Normal) = TextStyle(
    fontFamily = BodyFontFamily, fontWeight = weight, fontSize = size.sp, lineHeight = line.sp
)

val Typography = Typography(
    displayLarge = display(48, 52, FontWeight.Black),
    displayMedium = display(40, 44, FontWeight.Black),
    displaySmall = display(32, 38),
    headlineLarge = display(30, 36),
    headlineMedium = display(26, 32),
    headlineSmall = display(22, 28),
    titleLarge = display(20, 26),
    titleMedium = display(17, 22, FontWeight.Bold),
    titleSmall = display(15, 20, FontWeight.Bold),
    bodyLarge = body(16, 24),
    bodyMedium = body(14, 21),
    bodySmall = body(13, 18),
    labelLarge = display(16, 20, FontWeight.ExtraBold),
    labelMedium = display(14, 18, FontWeight.Bold),
    labelSmall = display(12, 16, FontWeight.Bold)
)
