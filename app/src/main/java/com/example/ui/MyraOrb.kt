package com.example.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
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
import com.example.model.VoiceState
import com.example.ui.theme.BrightViolet
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.GlowAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonPink
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun MyraOrb(
    voiceState: VoiceState,
    audioAmplitude: Float,
    modifier: Modifier = Modifier,
    size: Dp = 150.dp,
    onClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "orb_pulse")

    // Smooth audio amplitude responding with bouncy spring feedback
    val smoothAudioAmplitude by animateFloatAsState(
        targetValue = audioAmplitude.coerceIn(0f, 1f),
        animationSpec = spring(dampingRatio = 0.65f, stiffness = 500f),
        label = "smooth_orb_amplitude"
    )

    // Breathing pulse
    val idlePulse by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "idle_pulse"
    )

    // Fast rotation for processing & active orbits
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    // Multi-stage Sonic Wave Shockwave Animations for Listening mode
    val wave1Scale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave1_scale"
    )
    val wave1Alpha by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave1_alpha"
    )

    val wave2Scale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.45f,
        animationSpec = infiniteRepeatable(
            animation = tween(1300, delayMillis = 300, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave2_scale"
    )
    val wave2Alpha by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1300, delayMillis = 300, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave2_alpha"
    )

    val wave3Scale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 2.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, delayMillis = 600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave3_scale"
    )
    val wave3Alpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, delayMillis = 600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave3_alpha"
    )

    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .size(size)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val center = Offset(this.size.width / 2f, this.size.height / 2f)
            val baseRadius = this.size.minDimension / 3.0f

            // Dynamic scale factor based on audio amplitude or state
            val dynamicScale = when (voiceState) {
                VoiceState.LISTENING -> 1.0f + (smoothAudioAmplitude * 0.55f)
                VoiceState.SPEAKING -> 1.0f + (sin(rotation * PI.toFloat() / 30f) * 0.15f)
                VoiceState.PROCESSING -> 1.02f
                VoiceState.IDLE, VoiceState.ERROR -> idlePulse
            }

            val currentRadius = baseRadius * dynamicScale

            // Draw Real-time Expanding Audio Wave Shockwaves when Listening
            if (voiceState == VoiceState.LISTENING) {
                // Wave 1: Immediate primary sonic ring expanding with amplitude boost
                val r1 = currentRadius * (wave1Scale + smoothAudioAmplitude * 0.35f)
                drawCircle(
                    color = NeonCyan.copy(alpha = (wave1Alpha * (0.5f + smoothAudioAmplitude * 0.5f)).coerceIn(0f, 1f)),
                    radius = r1,
                    center = center,
                    style = Stroke(width = (2.5f + smoothAudioAmplitude * 3f).dp.toPx())
                )

                // Wave 2: Harmonic secondary ring
                val r2 = currentRadius * (wave2Scale + smoothAudioAmplitude * 0.25f)
                drawCircle(
                    color = ElectricViolet.copy(alpha = (wave2Alpha * (0.4f + smoothAudioAmplitude * 0.6f)).coerceIn(0f, 1f)),
                    radius = r2,
                    center = center,
                    style = Stroke(width = (2f + smoothAudioAmplitude * 2f).dp.toPx())
                )

                // Wave 3: Far acoustic shockwave pulsing into surrounding space
                val r3 = currentRadius * (wave3Scale + smoothAudioAmplitude * 0.45f)
                drawCircle(
                    color = NeonPink.copy(alpha = (wave3Alpha * (0.3f + smoothAudioAmplitude * 0.7f)).coerceIn(0f, 1f)),
                    radius = r3,
                    center = center,
                    style = Stroke(width = 1.5.dp.toPx())
                )

                // Radial Acoustic Rays radiating from the orb edge responding to audio frequency
                val rayCount = 20
                for (i in 0 until rayCount) {
                    val angle = (i * (360f / rayCount) + rotation * 0.5f) * (PI / 180.0).toFloat()
                    val rayWave = sin(angle * 3f + rotation * 0.1f)
                    val rayLength = (6.dp.toPx() + (smoothAudioAmplitude * 22.dp.toPx() * (0.5f + abs(rayWave) * 0.5f)))

                    val startX = center.x + cos(angle) * (currentRadius + 2.dp.toPx())
                    val startY = center.y + sin(angle) * (currentRadius + 2.dp.toPx())
                    val endX = center.x + cos(angle) * (currentRadius + rayLength)
                    val endY = center.y + sin(angle) * (currentRadius + rayLength)

                    val rayColor = if (i % 2 == 0) NeonCyan else NeonPink
                    drawLine(
                        color = rayColor.copy(alpha = (0.35f + smoothAudioAmplitude * 0.65f).coerceIn(0f, 1f)),
                        start = Offset(startX, startY),
                        end = Offset(endX, endY),
                        strokeWidth = (2f + smoothAudioAmplitude * 1.5f).dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }
            } else if (voiceState == VoiceState.SPEAKING) {
                // Speaking harmonic ring
                val rippleRadius = baseRadius * 1.35f * (1f + sin(rotation * 0.1f) * 0.15f)
                drawCircle(
                    color = NeonPink.copy(alpha = 0.4f),
                    radius = rippleRadius,
                    center = center,
                    style = Stroke(width = 2.5.dp.toPx())
                )
            }

            // Outer soft atmospheric glow
            val glowColor = when (voiceState) {
                VoiceState.LISTENING -> if (smoothAudioAmplitude > 0.4f) NeonPink else NeonCyan
                VoiceState.SPEAKING -> NeonPink
                VoiceState.PROCESSING -> ElectricViolet
                VoiceState.IDLE -> ElectricViolet
                VoiceState.ERROR -> Color(0xFFEF4444)
            }

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        glowColor.copy(alpha = (0.45f + smoothAudioAmplitude * 0.35f).coerceIn(0f, 0.85f)),
                        glowColor.copy(alpha = 0.12f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = currentRadius * (1.5f + smoothAudioAmplitude * 0.3f)
                ),
                radius = currentRadius * (1.5f + smoothAudioAmplitude * 0.3f),
                center = center
            )

            // Dynamic Inner Holographic Gradient Core
            val orbColors = when (voiceState) {
                VoiceState.LISTENING -> listOf(
                    if (smoothAudioAmplitude > 0.5f) NeonPink else NeonCyan,
                    ElectricViolet,
                    Color(0xFF0077B6)
                )
                VoiceState.SPEAKING -> listOf(NeonPink, GlowAmber, ElectricViolet)
                VoiceState.PROCESSING -> listOf(ElectricViolet, NeonCyan, NeonPink)
                VoiceState.IDLE -> listOf(ElectricViolet, NeonCyan, Color(0xFF4C1D95))
                VoiceState.ERROR -> listOf(Color(0xFFEF4444), Color(0xFFB91C1C), Color(0xFF7F1D1D))
            }

            val angleRad = (rotation * PI / 180.0).toFloat()
            val startOffset = Offset(
                center.x + cos(angleRad) * currentRadius,
                center.y + sin(angleRad) * currentRadius
            )
            val endOffset = Offset(
                center.x - cos(angleRad) * currentRadius,
                center.y - sin(angleRad) * currentRadius
            )

            drawCircle(
                brush = Brush.linearGradient(
                    colors = orbColors,
                    start = startOffset,
                    end = endOffset
                ),
                radius = currentRadius,
                center = center
            )

            // Inner specular neural highlight
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = (0.75f + smoothAudioAmplitude * 0.2f).coerceIn(0f, 1f)),
                        Color.White.copy(alpha = 0.15f),
                        Color.Transparent
                    ),
                    center = Offset(center.x - currentRadius * 0.3f, center.y - currentRadius * 0.3f),
                    radius = currentRadius * 0.65f
                ),
                radius = currentRadius * 0.65f,
                center = Offset(center.x - currentRadius * 0.3f, center.y - currentRadius * 0.3f)
            )

            // Futuristic Orbiting Satellite Particles
            val particleCount = if (voiceState == VoiceState.LISTENING) 6 else 4
            for (i in 0 until particleCount) {
                val orbitAngle = (rotation + (i * (360f / particleCount))) * (PI / 180.0).toFloat()
                val orbitDist = currentRadius * (1.18f + smoothAudioAmplitude * 0.25f)
                val pX = center.x + cos(orbitAngle) * orbitDist
                val pY = center.y + sin(orbitAngle) * orbitDist
                val pColor = if (i % 2 == 0) NeonCyan else NeonPink
                drawCircle(
                    color = pColor.copy(alpha = 0.9f),
                    radius = (3.5f + smoothAudioAmplitude * 2f).dp.toPx(),
                    center = Offset(pX, pY)
                )
            }
        }
    }
}
