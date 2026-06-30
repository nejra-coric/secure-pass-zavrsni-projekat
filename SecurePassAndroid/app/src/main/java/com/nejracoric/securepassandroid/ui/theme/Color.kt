package com.nejracoric.securepassandroid.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

val DeepBackground = Color(0xFF0B0B0F)
val SurfaceDark = Color(0xFF121218)
val SurfaceElevated = Color(0xFF1A1A22)
val SurfaceCard = Color(0xFF1C1C26)
val SurfaceInput = Color(0xFF16161E)
val GlassOverlay = Color(0xFF1E1E2A)

val PurplePrimary = Color(0xFF8E2DE2)
val PurpleDeep = Color(0xFF6B21D4)
val BlueNeon = Color(0xFF4A00E0)
val BlueLight = Color(0xFF00BFFF)
val PurpleAccent = Color(0xFFA855F7)

val NeonCyan = BlueLight
val NeonPurple = PurplePrimary
val NeonPink = Color(0xFFE040FB)
val NeonGreen = Color(0xFF00E676)
val NeonOrange = Color(0xFFFFAB40)
val NeonRed = Color(0xFFFF5252)
val NeonYellow = Color(0xFFFFD740)

val TextPrimary = Color(0xFFFFFFFF)
val TextSecondary = Color(0xFF9CA3AF)
val TextMuted = Color(0xFF6B7280)

val PrimaryGradient = Brush.horizontalGradient(
    colors = listOf(PurplePrimary, BlueNeon, BlueLight),
)

val AccentGradient = Brush.horizontalGradient(
    colors = listOf(PurpleDeep, PurplePrimary, BlueNeon),
)

val ButtonGradient = Brush.horizontalGradient(
    colors = listOf(Color(0xFF7C3AED), Color(0xFF6366F1), Color(0xFF3B82F6)),
)

val SuccessGradient = Brush.horizontalGradient(
    colors = listOf(PurplePrimary, BlueNeon),
)

val DangerGradient = Brush.horizontalGradient(
    colors = listOf(NeonRed, NeonOrange),
)

val PurpleGlow = Brush.radialGradient(
    colors = listOf(PurplePrimary.copy(alpha = 0.5f), Color.Transparent),
)

fun entropyNeonColor(entropyBits: Double): Color = when {
    entropyBits < 40 -> NeonRed
    entropyBits <= 70 -> NeonOrange
    else -> NeonGreen
}

fun strengthLabel(entropyBits: Double): String = when {
    entropyBits < 40 -> "SLABA"
    entropyBits < 55 -> "SREDNJA"
    entropyBits < 75 -> "JAKA"
    else -> "VRLO JAKA"
}

fun crackTimeEstimate(entropyBits: Double): String = when {
    entropyBits < 40 -> "Nekoliko sekundi"
    entropyBits < 55 -> "Nekoliko mjeseci"
    entropyBits < 75 -> "Nekoliko godina"
    else -> "600+ godina"
}
