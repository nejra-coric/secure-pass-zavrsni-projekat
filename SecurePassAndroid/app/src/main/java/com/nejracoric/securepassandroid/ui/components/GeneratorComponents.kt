package com.nejracoric.securepassandroid.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nejracoric.securepassandroid.ui.theme.NeonGreen
import com.nejracoric.securepassandroid.ui.theme.NeonOrange
import com.nejracoric.securepassandroid.ui.theme.NeonRed
import com.nejracoric.securepassandroid.ui.theme.NeonYellow
import com.nejracoric.securepassandroid.ui.theme.PurplePrimary
import com.nejracoric.securepassandroid.ui.theme.TextPrimary
import com.nejracoric.securepassandroid.ui.theme.TextSecondary
import com.nejracoric.securepassandroid.ui.theme.strengthLabel

@Composable
fun PurpleCheckbox(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(if (checked) PurplePrimary else Color(0xFF2A2A36))
                .border(1.dp, if (checked) PurplePrimary else Color(0xFF3A3A48), RoundedCornerShape(6.dp)),
            contentAlignment = Alignment.Center,
        ) {
            if (checked) {
                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
            }
        }
        Text(text = label, color = TextPrimary, style = androidx.compose.material3.MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun CircularStrengthGauge(
    entropyBits: Double,
    hasPassword: Boolean,
    modifier: Modifier = Modifier,
) {
    val progress = if (hasPassword) (entropyBits / 100.0).coerceIn(0.0, 1.0).toFloat() else 0f
    val animatedProgress by animateFloatAsState(progress, tween(800), label = "gauge")
    val label = if (hasPassword) strengthLabel(entropyBits) else "—"

    Box(
        modifier = modifier.size(140.dp),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.size(140.dp)) {
            val stroke = 10f
            val diameter = size.minDimension - stroke
            val topLeft = Offset((size.width - diameter) / 2f, (size.height - diameter) / 2f)
            val arcSize = Size(diameter, diameter)

            drawArc(
                color = Color(0xFF2A2A36),
                startAngle = 135f,
                sweepAngle = 270f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round),
            )

            drawArc(
                brush = Brush.sweepGradient(
                    colors = listOf(NeonRed, NeonOrange, NeonYellow, NeonGreen),
                ),
                startAngle = 135f,
                sweepAngle = 270f * animatedProgress,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round),
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "JAČINA",
                color = TextSecondary,
                fontSize = 10.sp,
                letterSpacing = 1.sp,
            )
            Text(
                text = label,
                color = if (hasPassword) NeonGreen else TextSecondary,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
            )
            Icon(
                imageVector = Icons.Default.Shield,
                contentDescription = null,
                tint = if (hasPassword) NeonGreen else TextSecondary,
                modifier = Modifier
                    .padding(top = 4.dp)
                    .size(20.dp),
            )
        }
    }
}

@Composable
fun ColoredPasswordText(
    password: String,
    modifier: Modifier = Modifier,
) {
    val colors = listOf(TextPrimary, PurplePrimary, com.nejracoric.securepassandroid.ui.theme.BlueLight, NeonOrange)
    Text(
        text = androidx.compose.ui.text.buildAnnotatedString {
            password.forEach { char ->
                pushStyle(
                    androidx.compose.ui.text.SpanStyle(
                        color = when {
                            char.isUpperCase() -> colors[0]
                            char.isLowerCase() -> colors[1]
                            char.isDigit() -> colors[2]
                            else -> colors[3]
                        },
                    ),
                )
                append(char)
                pop()
            }
        },
        style = androidx.compose.ui.text.TextStyle(
            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
            fontSize = 18.sp,
        ),
        modifier = modifier,
    )
}

@Composable
fun CrackTimeInfoBox(
    entropyBits: Double,
    hasPassword: Boolean,
    modifier: Modifier = Modifier,
) {
    if (!hasPassword) return

    val text = when {
        entropyBits < 40 -> "Vrijeme za probijanje: nekoliko sekundi"
        entropyBits < 55 -> "Vrijeme za probijanje: nekoliko mjeseci"
        entropyBits < 75 -> "Vrijeme za probijanje: nekoliko godina"
        else -> "Vrijeme za probijanje 600+ godina"
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, NeonGreen.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
            .background(NeonGreen.copy(alpha = 0.08f))
            .padding(12.dp),
    ) {
        Text(text = text, color = NeonGreen, style = androidx.compose.material3.MaterialTheme.typography.bodySmall)
    }
}
