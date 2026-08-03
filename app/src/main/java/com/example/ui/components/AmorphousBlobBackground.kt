package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.example.ui.theme.AuroraEmerald
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.MidnightBackground
import com.example.ui.theme.MidnightDark
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonPink
import com.example.ui.theme.SolarAmber
import kotlin.math.cos
import kotlin.math.sin

enum class BlobTheme {
    CYBER_NEON,    // Cyan + Pink + Violet
    AURORA_GLOW,   // Emerald + Violet + Cyan
    MIDNIGHT_GLASS,// Violet + Electric Blue + Dark Pink
    SOLAR_ECLIPSE  // Solar Amber + Hot Coral + Magenta
}

@Composable
fun AmorphousBlobBackground(
    modifier: Modifier = Modifier,
    theme: BlobTheme = BlobTheme.CYBER_NEON,
    content: @Composable () -> Unit = {}
) {
    val transition = rememberInfiniteTransition(label = "BlobAnimation")
    
    val time by transition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(14000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "time"
    )

    val pulsingScale by transition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(7000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val (color1, color2, color3) = when (theme) {
        BlobTheme.CYBER_NEON -> Triple(NeonCyan, NeonPink, ElectricViolet)
        BlobTheme.AURORA_GLOW -> Triple(AuroraEmerald, ElectricViolet, NeonCyan)
        BlobTheme.MIDNIGHT_GLASS -> Triple(ElectricViolet, Color(0xFF0077FF), NeonPink)
        BlobTheme.SOLAR_ECLIPSE -> Triple(SolarAmber, NeonPink, Color(0xFFFF4500))
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(MidnightBackground, MidnightDark, Color(0xFF05060C))
                )
            )
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            if (width == 0f || height == 0f) return@Canvas

            // Blob 1: Top Left Orbit
            val x1 = width * 0.35f + cos(time.toDouble()).toFloat() * (width * 0.22f)
            val y1 = height * 0.28f + sin((time * 0.8f).toDouble()).toFloat() * (height * 0.18f)
            val r1 = width * 0.55f * pulsingScale

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(color1.copy(alpha = 0.45f), color1.copy(alpha = 0.15f), Color.Transparent),
                    center = Offset(x1, y1),
                    radius = r1
                ),
                center = Offset(x1, y1),
                radius = r1
            )

            // Blob 2: Center Right Orbit
            val x2 = width * 0.72f + sin((time * 1.1f).toDouble()).toFloat() * (width * 0.2f)
            val y2 = height * 0.55f + cos((time * 0.9f).toDouble()).toFloat() * (height * 0.22f)
            val r2 = width * 0.6f / pulsingScale

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(color2.copy(alpha = 0.4f), color2.copy(alpha = 0.12f), Color.Transparent),
                    center = Offset(x2, y2),
                    radius = r2
                ),
                center = Offset(x2, y2),
                radius = r2
            )

            // Blob 3: Bottom Left / Center Orbit
            val x3 = width * 0.25f + cos((time * 0.7f).toDouble()).toFloat() * (width * 0.18f)
            val y3 = height * 0.8f + sin((time * 1.3f).toDouble()).toFloat() * (height * 0.15f)
            val r3 = width * 0.5f * pulsingScale

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(color3.copy(alpha = 0.38f), color3.copy(alpha = 0.1f), Color.Transparent),
                    center = Offset(x3, y3),
                    radius = r3
                ),
                center = Offset(x3, y3),
                radius = r3
            )
        }

        content()
    }
}
