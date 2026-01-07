package com.biosense.app.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import kotlin.math.min

enum class ECGType {
    Normal,
    Tachycardia,
    Bradycardia
}

@Composable
fun HealthWaveAnimation(
    tapIntensity: Float = 0f,
    modifier: Modifier = Modifier,
    alpha: Float = 0.35f
) {
    val infiniteTransition = rememberInfiniteTransition()

    val speedMultiplier = 1f + tapIntensity * 0.5f

    val waveOffset1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween((2500 / speedMultiplier).toInt(), easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    val waveOffset2 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween((3500 / speedMultiplier).toInt(), easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    val waveOffset3 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween((4500 / speedMultiplier).toInt(), easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .alpha(alpha)
    ) {
        val width = size.width
        val height = size.height
        val baseScale = 1.5f
        val pulseScale = baseScale + tapIntensity * 1.5f
        drawECGLine(width, height * 0.3f, waveOffset1, Color(0xFFFF6B6B), pulseScale, tapIntensity, ECGType.Normal)
        drawECGLine(width, height * 0.5f, waveOffset2, Color(0xFF64B5AD), pulseScale * 1.1f, tapIntensity, ECGType.Tachycardia)
        drawECGLine(width, height * 0.7f, waveOffset3, Color(0xFF4ECDC4), pulseScale, tapIntensity, ECGType.Bradycardia)
    }
}

private fun DrawScope.drawECGLine(
    width: Float,
    yPosition: Float,
    offset: Float,
    color: Color,
    scale: Float = 1f,
    tapIntensity: Float = 0f,
    ecgType: ECGType = ECGType.Normal
) {
    val segmentWidth = when (ecgType) {
        ECGType.Normal -> width / 1.5f
        ECGType.Tachycardia -> width / 2.2f
        ECGType.Bradycardia -> width / 1.0f
    }

    val dynamicScale = scale + (tapIntensity * 0.3f * kotlin.math.sin(offset * 2 * Math.PI).toFloat())
    val segmentRange = when (ecgType) {
        ECGType.Bradycardia -> -1..2
        else -> -1..3
    }

    for (segment in segmentRange) {
        val startX = segment * segmentWidth - offset * segmentWidth

        if (startX > width + segmentWidth || startX < -segmentWidth) continue

        val path = Path()

        when (ecgType) {
            ECGType.Normal -> drawNormalECG(path, startX, yPosition, segmentWidth, dynamicScale)
            ECGType.Tachycardia -> drawTachycardiaECG(path, startX, yPosition, segmentWidth, dynamicScale)
            ECGType.Bradycardia -> drawBradycardiaECG(path, startX, yPosition, segmentWidth, dynamicScale)
        }

        val strokeWidth = (1.5.dp.toPx() + tapIntensity * 1.dp.toPx()) * min(dynamicScale / scale, 1.1f)
        drawPath(
            path = path,
            color = color.copy(alpha = min(0.9f, 0.6f + tapIntensity * 0.15f)),
            style = Stroke(
                width = strokeWidth,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )

        if (tapIntensity > 1f) {
            drawPath(
                path = path,
                color = color.copy(alpha = 0.08f * (tapIntensity - 1f)),
                style = Stroke(
                    width = strokeWidth * 1.5f,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )
        }
    }
}

private fun drawNormalECG(path: Path, startX: Float, yPosition: Float, segmentWidth: Float, scale: Float) {
    path.moveTo(startX, yPosition)

    path.lineTo(startX + 15 * segmentWidth / 100, yPosition)
    path.cubicTo(
        startX + 18 * segmentWidth / 100, yPosition - (20f * scale),
        startX + 22 * segmentWidth / 100, yPosition - (20f * scale),
        startX + 25 * segmentWidth / 100, yPosition
    )

    // PR segment
    path.lineTo(startX + 35 * segmentWidth / 100, yPosition)

    // QRS complex
    path.lineTo(startX + 37 * segmentWidth / 100, yPosition + (8f * scale)) // Q
    path.lineTo(startX + 39 * segmentWidth / 100, yPosition - (80f * scale)) // R
    path.lineTo(startX + 41 * segmentWidth / 100, yPosition + (15f * scale)) // S
    path.lineTo(startX + 43 * segmentWidth / 100, yPosition)

    // ST segment
    path.lineTo(startX + 55 * segmentWidth / 100, yPosition)

    // T wave
    path.cubicTo(
        startX + 58 * segmentWidth / 100, yPosition - (30f * scale),
        startX + 65 * segmentWidth / 100, yPosition - (30f * scale),
        startX + 70 * segmentWidth / 100, yPosition
    )

    // End
    path.lineTo(startX + 100 * segmentWidth / 100, yPosition)
}

private fun drawTachycardiaECG(path: Path, startX: Float, yPosition: Float, segmentWidth: Float, scale: Float) {
    path.moveTo(startX, yPosition)

    // Compressed P wave
    path.cubicTo(
        startX + 8 * segmentWidth / 100, yPosition - (15f * scale),
        startX + 12 * segmentWidth / 100, yPosition - (15f * scale),
        startX + 15 * segmentWidth / 100, yPosition
    )

    // Quick QRS
    path.lineTo(startX + 25 * segmentWidth / 100, yPosition + (5f * scale))
    path.lineTo(startX + 27 * segmentWidth / 100, yPosition - (70f * scale))
    path.lineTo(startX + 29 * segmentWidth / 100, yPosition + (10f * scale))
    path.lineTo(startX + 31 * segmentWidth / 100, yPosition)

    // Compressed T wave
    path.cubicTo(
        startX + 35 * segmentWidth / 100, yPosition - (20f * scale),
        startX + 40 * segmentWidth / 100, yPosition - (20f * scale),
        startX + 45 * segmentWidth / 100, yPosition
    )

    // Next beat starts sooner
    path.lineTo(startX + 100 * segmentWidth / 100, yPosition)
}

private fun drawBradycardiaECG(path: Path, startX: Float, yPosition: Float, segmentWidth: Float, scale: Float) {
    path.moveTo(startX, yPosition)

    // Extended flat line
    path.lineTo(startX + 25 * segmentWidth / 100, yPosition)

    // P wave
    path.cubicTo(
        startX + 28 * segmentWidth / 100, yPosition - (18f * scale),
        startX + 32 * segmentWidth / 100, yPosition - (18f * scale),
        startX + 35 * segmentWidth / 100, yPosition
    )

    // Extended PR interval
    path.lineTo(startX + 45 * segmentWidth / 100, yPosition)

    // QRS complex
    path.lineTo(startX + 47 * segmentWidth / 100, yPosition + (8f * scale))
    path.lineTo(startX + 49 * segmentWidth / 100, yPosition - (75f * scale))
    path.lineTo(startX + 51 * segmentWidth / 100, yPosition + (15f * scale))
    path.lineTo(startX + 53 * segmentWidth / 100, yPosition)

    // Extended ST segment
    path.lineTo(startX + 65 * segmentWidth / 100, yPosition)

    // T wave
    path.cubicTo(
        startX + 68 * segmentWidth / 100, yPosition - (25f * scale),
        startX + 75 * segmentWidth / 100, yPosition - (25f * scale),
        startX + 80 * segmentWidth / 100, yPosition
    )

    // Long flat end
    path.lineTo(startX + 100 * segmentWidth / 100, yPosition)
}
