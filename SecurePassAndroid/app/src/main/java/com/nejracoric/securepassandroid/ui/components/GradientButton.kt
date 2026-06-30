package com.nejracoric.securepassandroid.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nejracoric.securepassandroid.ui.theme.ButtonGradient
import com.nejracoric.securepassandroid.ui.theme.PurplePrimary
import com.nejracoric.securepassandroid.ui.theme.TextPrimary

@Composable
fun GradientButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    brush: Brush = ButtonGradient,
    icon: ImageVector? = null,
    height: Int = 54,
) {
    val shape = RoundedCornerShape(28.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height.dp)
            .shadow(
                elevation = if (enabled) 12.dp else 0.dp,
                shape = shape,
                ambientColor = PurplePrimary.copy(alpha = 0.4f),
                spotColor = PurplePrimary.copy(alpha = 0.4f),
            )
            .clip(shape)
            .background(
                if (enabled) brush else Brush.linearGradient(
                    listOf(Color(0xFF3A3A48), Color(0xFF2A2A36)),
                ),
            )
            .clickable(
                enabled = enabled,
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = TextPrimary,
                    modifier = Modifier
                        .size(20.dp)
                        .padding(end = 4.dp),
                )
            }
            Text(
                text = text,
                color = TextPrimary,
                fontWeight = FontWeight.SemiBold,
                style = androidx.compose.material3.MaterialTheme.typography.labelLarge,
            )
        }
    }
}

@Composable
fun TextLinkButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = com.nejracoric.securepassandroid.ui.theme.PurpleAccent,
) {
    Text(
        text = text,
        color = color,
        style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
        modifier = modifier.clickable(onClick = onClick),
    )
}
