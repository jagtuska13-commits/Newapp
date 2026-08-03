package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.GlassBorderColor
import com.example.ui.theme.NeonCyan

@Composable
fun FrostedGlassBox(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(24.dp),
    backgroundColor: Color = Color(0x77121526),
    borderColor: Color = GlassBorderColor,
    borderWidth: Dp = 1.dp,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .clip(shape)
            .background(backgroundColor)
            .border(
                border = BorderStroke(
                    width = borderWidth,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            borderColor.copy(alpha = 0.6f),
                            Color.White.copy(alpha = 0.1f),
                            borderColor.copy(alpha = 0.2f)
                        )
                    )
                ),
                shape = shape
            ),
        content = content
    )
}

@Composable
fun FrostedGlassCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    shape: Shape = RoundedCornerShape(20.dp),
    accentGlowColor: Color? = null,
    backgroundColor: Color = Color(0x66181B34),
    content: @Composable BoxScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val clickableModifier = if (onClick != null) {
        Modifier.clickable(
            interactionSource = interactionSource,
            indication = ripple(color = accentGlowColor ?: NeonCyan),
            onClick = onClick
        )
    } else Modifier

    val borderBrush = if (accentGlowColor != null) {
        Brush.linearGradient(
            colors = listOf(
                accentGlowColor.copy(alpha = 0.7f),
                GlassBorderColor,
                accentGlowColor.copy(alpha = 0.3f)
            )
        )
    } else {
        Brush.linearGradient(
            colors = listOf(
                GlassBorderColor.copy(alpha = 0.6f),
                Color.White.copy(alpha = 0.08f),
                GlassBorderColor.copy(alpha = 0.2f)
            )
        )
    }

    Box(
        modifier = modifier
            .clip(shape)
            .background(backgroundColor)
            .border(
                border = BorderStroke(1.dp, borderBrush),
                shape = shape
            )
            .then(clickableModifier),
        content = content
    )
}
