package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.GlowAmber
import com.example.ui.theme.MidnightCard
import com.example.ui.theme.MidnightElevated
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonPink
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sin

/**
 * Real-time audio waveform and visual pulse animation responding dynamically
 * to audio input intensity when the assistant is actively 'listening'.
 */
@Composable
fun AudioWaveVisualizer(
    isListening: Boolean,
    audioAmplitude: Float,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isListening,
        enter = fadeIn(tween(250)) + expandVertically(tween(250)),
        exit = fadeOut(tween(200)) + shrinkVertically(tween(200)),
        modifier = modifier
    ) {
        val smoothAmplitude by animateFloatAsState(
            targetValue = audioAmplitude.coerceIn(0f, 1f),
            animationSpec = spring(dampingRatio = 0.65f, stiffness = 400f),
            label = "smooth_amplitude"
        )

        val infiniteTransition = rememberInfiniteTransition(label = "wave_anim")
        val phase by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = (2 * PI).toFloat(),
            animationSpec = infiniteRepeatable(
                animation = tween(1200, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "phase_motion"
        )

        Surface(
            color = MidnightElevated.copy(alpha = 0.85f),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .border(
                    width = 1.dp,
                    brush = Brush.horizontalGradient(
                        listOf(
                            NeonCyan.copy(alpha = 0.3f),
                            ElectricViolet.copy(alpha = 0.5f),
                            NeonPink.copy(alpha = 0.3f)
                        )
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
                .testTag("audio_wave_visualizer")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header with live audio feedback indicator
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Pulsing red/cyan live recording dot
                        val pulseAlpha by infiniteTransition.animateFloat(
                            initialValue = 0.4f,
                            targetValue = 1f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(600, easing = FastOutSlowInEasing),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "live_dot"
                        )
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(NeonPink.copy(alpha = pulseAlpha))
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "LIVE AUDIO INPUT",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonCyan,
                            letterSpacing = 0.8.sp
                        )
                    }

                    // Intensity level meter badge
                    val percent = (smoothAmplitude * 100).toInt()
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "INTENSITY: $percent%",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (percent > 40) GlowAmber else TextMuted
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        // 5-bar mini meter
                        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                            for (i in 1..5) {
                                val barFilled = percent >= (i * 18)
                                Box(
                                    modifier = Modifier
                                        .size(width = 3.dp, height = (8 + i * 2).dp)
                                        .clip(RoundedCornerShape(1.dp))
                                        .background(
                                            if (barFilled) {
                                                if (i > 3) NeonPink else NeonCyan
                                            } else {
                                                MidnightCard
                                            }
                                        )
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Canvas Equalizer Waveform Visualization
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    val canvasWidth = size.width
                    val canvasHeight = size.height
                    val barCount = 32
                    val barSpacing = 4.dp.toPx()
                    val totalSpacing = barSpacing * (barCount - 1)
                    val barWidth = ((canvasWidth - totalSpacing) / barCount).coerceAtLeast(3.dp.toPx())
                    val centerY = canvasHeight / 2f

                    for (i in 0 until barCount) {
                        val normalizedIndex = i.toFloat() / (barCount - 1) // 0f to 1f
                        val distanceToCenter = abs(normalizedIndex - 0.5f) * 2f // 0f at center, 1f at edges
                        val centerWeight = 1f - (distanceToCenter * 0.45f) // Center bars are naturally taller

                        // Combine dynamic sine waves with audio input amplitude
                        val waveOffset1 = sin(phase + i * 0.35f)
                        val waveOffset2 = sin(phase * 1.5f - i * 0.2f)
                        val harmonic = ((waveOffset1 + waveOffset2) / 2f).coerceIn(-1f, 1f)

                        // Base height + reactive amplitude expansion
                        val baseHeight = 4.dp.toPx()
                        val reactiveHeight = (canvasHeight - 6.dp.toPx()) * smoothAmplitude * centerWeight
                        val waveJitter = (harmonic * 10.dp.toPx() * (0.3f + smoothAmplitude * 0.7f))

                        val totalHeight = (baseHeight + reactiveHeight + abs(waveJitter))
                            .coerceIn(4.dp.toPx(), canvasHeight)

                        val x = i * (barWidth + barSpacing)
                        val y = centerY - (totalHeight / 2f)

                        // Dynamic multi-color gradient based on intensity
                        val barColor = when {
                            smoothAmplitude > 0.65f -> NeonPink
                            smoothAmplitude > 0.30f -> ElectricViolet
                            else -> NeonCyan
                        }

                        val barBrush = Brush.verticalGradient(
                            colors = listOf(
                                NeonPink.copy(alpha = 0.9f),
                                barColor,
                                NeonCyan.copy(alpha = 0.85f)
                            ),
                            startY = y,
                            endY = y + totalHeight
                        )

                        drawRoundRect(
                            brush = barBrush,
                            topLeft = Offset(x, y),
                            size = Size(barWidth, totalHeight),
                            cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
                        )
                    }
                }
            }
        }
    }
}
