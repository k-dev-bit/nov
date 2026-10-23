package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.NovaBlue
import com.example.ui.theme.NovaCyan
import com.example.ui.theme.NovaPink
import com.example.ui.theme.NovaPurple
import com.example.voice.VoiceState

@Composable
fun VoiceOrb(
    state: VoiceState,
    audioRms: Float = 0f,
    size: Dp = 180.dp,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "OrbInfiniteTransition")

    // Continuous rotation
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (state == VoiceState.THINKING) 1200 else 6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "OrbRotation"
    )

    // Breathing pulse
    val breathingScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "OrbBreathing"
    )

    // Dynamic scale driven by audio RMS or state
    val dynamicPulse = remember { Animatable(1f) }
    LaunchedEffect(audioRms, state) {
        val targetScale = when (state) {
            VoiceState.LISTENING -> 1f + (audioRms.coerceIn(0f, 10f) / 25f)
            VoiceState.SPEAKING -> 1.08f
            VoiceState.THINKING -> 1.04f
            else -> 1f
        }
        dynamicPulse.animateTo(
            targetValue = targetScale,
            animationSpec = tween(durationMillis = 150)
        )
    }

    val primaryColor = when (state) {
        VoiceState.LISTENING -> NovaCyan
        VoiceState.THINKING -> NovaPurple
        VoiceState.SPEAKING -> NovaPink
        VoiceState.ERROR -> Color(0xFFFF3366)
        VoiceState.IDLE -> NovaBlue
    }

    val secondaryColor = when (state) {
        VoiceState.LISTENING -> NovaBlue
        VoiceState.THINKING -> NovaCyan
        VoiceState.SPEAKING -> NovaPurple
        VoiceState.ERROR -> Color(0xFFFF6B6B)
        VoiceState.IDLE -> NovaPurple
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(size)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val center = Offset(this.size.width / 2f, this.size.height / 2f)
            val radius = (this.size.minDimension / 2f) * 0.7f * breathingScale * dynamicPulse.value

            // 1. Outermost subtle glow wave
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(primaryColor.copy(alpha = 0.35f), Color.Transparent),
                    center = center,
                    radius = radius * 1.5f
                ),
                radius = radius * 1.4f,
                center = center
            )

            // 2. Secondary orbiting energy ring
            drawArc(
                brush = Brush.sweepGradient(
                    colors = listOf(primaryColor, secondaryColor, primaryColor),
                    center = center
                ),
                startAngle = rotation,
                sweepAngle = if (state == VoiceState.THINKING) 270f else 180f,
                useCenter = false,
                style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round),
                topLeft = Offset(center.x - radius * 1.15f, center.y - radius * 1.15f),
                size = androidx.compose.ui.geometry.Size(radius * 2.3f, radius * 2.3f)
            )

            // 3. Counter-rotating outer dotted ring
            drawArc(
                color = secondaryColor.copy(alpha = 0.6f),
                startAngle = -rotation * 1.5f,
                sweepAngle = 120f,
                useCenter = false,
                style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round),
                topLeft = Offset(center.x - radius * 1.28f, center.y - radius * 1.28f),
                size = androidx.compose.ui.geometry.Size(radius * 2.56f, radius * 2.56f)
            )

            // 4. Core energetic sphere
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.95f),
                        primaryColor,
                        secondaryColor,
                        Color(0xFF070B14)
                    ),
                    center = center,
                    radius = radius
                ),
                radius = radius,
                center = center
            )

            // 5. Innermost brilliant nucleus
            drawCircle(
                color = Color.White.copy(alpha = 0.9f),
                radius = radius * 0.22f,
                center = center
            )
        }
    }
}
