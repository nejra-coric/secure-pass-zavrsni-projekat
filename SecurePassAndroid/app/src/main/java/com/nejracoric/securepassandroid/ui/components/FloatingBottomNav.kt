package com.nejracoric.securepassandroid.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.nejracoric.securepassandroid.ui.theme.GlassOverlay
import com.nejracoric.securepassandroid.ui.theme.PurplePrimary
import com.nejracoric.securepassandroid.ui.theme.TextPrimary
import com.nejracoric.securepassandroid.ui.theme.TextSecondary

enum class MainTab { Vault, Generator }

@Composable
fun FloatingBottomNav(
    selectedTab: MainTab,
    onTabSelected: (MainTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(32.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp)
            .shadow(20.dp, shape, ambientColor = PurplePrimary.copy(0.25f))
            .clip(shape)
            .background(GlassOverlay)
            .border(1.dp, Color(0xFF2E2E3A), shape)
            .padding(horizontal = 8.dp, vertical = 8.dp)
            .height(72.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            NavTabItem(
                label = "Moj Trezor",
                selected = selectedTab == MainTab.Vault,
                onClick = { onTabSelected(MainTab.Vault) },
                icon = { tint ->
                    Icon(Icons.Default.Key, contentDescription = null, tint = tint, modifier = Modifier.size(22.dp))
                },
            )
            NavTabItem(
                label = "Generator",
                selected = selectedTab == MainTab.Generator,
                onClick = { onTabSelected(MainTab.Generator) },
                icon = { tint ->
                    Icon(Icons.Outlined.Bolt, contentDescription = null, tint = tint, modifier = Modifier.size(22.dp))
                },
            )
        }
    }
}

@Composable
private fun NavTabItem(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    icon: @Composable (Color) -> Unit,
) {
    val iconTint by animateColorAsState(
        if (selected) TextPrimary else TextSecondary,
        label = "iconTint",
    )
    val scale by animateFloatAsState(
        if (selected) 1f else 0.9f,
        spring(stiffness = Spring.StiffnessMediumLow),
        label = "scale",
    )

    Column(
        modifier = Modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
            .padding(horizontal = 20.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (selected) {
                Box(
                    modifier = Modifier
                        .size((44 * scale).dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(PurplePrimary.copy(0.45f), PurplePrimary.copy(0.15f)),
                            ),
                        ),
                )
            }
            icon(iconTint)
        }
        Text(
            text = label,
            color = if (selected) PurplePrimary else TextSecondary,
            style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(top = 4.dp),
        )
        if (selected) {
            Box(
                modifier = Modifier
                    .padding(top = 4.dp)
                    .size(width = 24.dp, height = 3.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(PurplePrimary),
            )
        }
    }
}
