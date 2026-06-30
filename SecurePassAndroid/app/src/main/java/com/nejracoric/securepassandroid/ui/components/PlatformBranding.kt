package com.nejracoric.securepassandroid.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nejracoric.securepassandroid.ui.theme.BlueLight
import com.nejracoric.securepassandroid.ui.theme.NeonPink
import com.nejracoric.securepassandroid.ui.theme.NeonRed
import com.nejracoric.securepassandroid.ui.theme.PurplePrimary
import com.nejracoric.securepassandroid.ui.theme.TextPrimary

data class PlatformBrand(
    val background: Brush,
    val letter: String,
    val letterColor: Color = TextPrimary,
)

fun platformBrandFor(title: String): PlatformBrand {
    val key = title.lowercase().trim()
    return when {
        key.contains("google") || key.contains("gmail") -> PlatformBrand(
            background = Brush.linearGradient(listOf(Color(0xFFEA4335), Color(0xFF34A853), Color(0xFF4285F4))),
            letter = "G",
        )
        key.contains("facebook") || key.contains("meta") -> PlatformBrand(
            background = Brush.linearGradient(listOf(Color(0xFF1877F2), Color(0xFF0D65D9))),
            letter = "f",
        )
        key.contains("netflix") -> PlatformBrand(
            background = Brush.linearGradient(listOf(Color(0xFFE50914), Color(0xFFB20710))),
            letter = "N",
        )
        key.contains("instagram") -> PlatformBrand(
            background = Brush.linearGradient(listOf(NeonPink, PurplePrimary, Color(0xFFFF9800))),
            letter = "◎",
        )
        key.contains("paypal") -> PlatformBrand(
            background = Brush.linearGradient(listOf(Color(0xFF003087), BlueLight)),
            letter = "P",
        )
        key.contains("github") -> PlatformBrand(
            background = Brush.linearGradient(listOf(Color(0xFF24292E), Color(0xFF444D56))),
            letter = "⚙",
        )
        key.contains("twitter") || key.contains("x.com") -> PlatformBrand(
            background = Brush.linearGradient(listOf(Color(0xFF1DA1F2), Color(0xFF0D8BD9))),
            letter = "𝕏",
        )
        key.contains("amazon") -> PlatformBrand(
            background = Brush.linearGradient(listOf(Color(0xFFFF9900), Color(0xFF232F3E))),
            letter = "a",
        )
        key.contains("spotify") -> PlatformBrand(
            background = Brush.linearGradient(listOf(Color(0xFF1DB954), Color(0xFF169C46))),
            letter = "♫",
        )
        key.contains("binance") -> PlatformBrand(
            background = Brush.linearGradient(listOf(Color(0xFFF3BA2F), Color(0xFFE8A317))),
            letter = "B",
            letterColor = Color(0xFF1E2329),
        )
        else -> PlatformBrand(
            background = Brush.linearGradient(listOf(PurplePrimary, BlueLight)),
            letter = title.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
        )
    }
}

@Composable
fun PlatformLogo(
    title: String,
    modifier: Modifier = Modifier,
    size: Int = 44,
) {
    val brand = platformBrandFor(title)
    Box(
        modifier = modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(brand.background),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = brand.letter,
            color = brand.letterColor,
            fontWeight = FontWeight.Bold,
            fontSize = (size * 0.4f).sp,
        )
    }
}
