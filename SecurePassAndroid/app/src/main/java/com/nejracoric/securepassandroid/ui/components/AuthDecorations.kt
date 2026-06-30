package com.nejracoric.securepassandroid.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.nejracoric.securepassandroid.ui.theme.BlueLight
import com.nejracoric.securepassandroid.ui.theme.BlueNeon
import com.nejracoric.securepassandroid.ui.theme.PurplePrimary

@Composable
fun LockIconCircle(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(88.dp)
            .shadow(
                elevation = 24.dp,
                shape = CircleShape,
                ambientColor = PurplePrimary.copy(alpha = 0.5f),
                spotColor = PurplePrimary.copy(alpha = 0.5f),
            )
            .background(
                brush = Brush.linearGradient(listOf(PurplePrimary, BlueNeon)),
                shape = CircleShape,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Default.Lock,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(36.dp),
        )
    }
}

@Composable
fun AuthWaveDecoration(modifier: Modifier = Modifier) {
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(120.dp),
    ) {
        val w = size.width
        val h = size.height

        fun wavePath(amplitude: Float, yOffset: Float, phase: Float): Path {
            val path = Path()
            path.moveTo(0f, yOffset)
            val steps = 40
            for (i in 0..steps) {
                val x = w * i / steps
                val y = yOffset + amplitude * kotlin.math.sin((i / steps.toFloat()) * 6.28f + phase)
                path.lineTo(x, y)
            }
            return path
        }

        drawPath(
            path = wavePath(h * 0.08f, h * 0.5f, 0f),
            brush = Brush.horizontalGradient(listOf(PurplePrimary.copy(0.5f), BlueLight.copy(0.3f))),
            style = Stroke(width = 2f, cap = StrokeCap.Round),
        )
        drawPath(
            path = wavePath(h * 0.06f, h * 0.65f, 1.2f),
            brush = Brush.horizontalGradient(listOf(BlueLight.copy(0.4f), PurplePrimary.copy(0.2f))),
            style = Stroke(width = 1.5f, cap = StrokeCap.Round),
        )
        drawPath(
            path = wavePath(h * 0.04f, h * 0.8f, 2.4f),
            brush = Brush.horizontalGradient(listOf(PurplePrimary.copy(0.3f), BlueLight.copy(0.15f))),
            style = Stroke(width = 1f, cap = StrokeCap.Round),
        )
    }
}
