package com.biosense.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun TypingIndicator() {
    val dotSize = 8.dp
    val transition = rememberInfiniteTransition(label = "typing")

    // Staggered animation for 3 dots
    val dots = (0..2).map { index ->
        transition.animateFloat(
            initialValue = 0.3f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(600, delayMillis = index * 150, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "dot$index"
        )
    }

    Box(
        modifier = Modifier
            .padding(vertical = 8.dp)
            .width(80.dp)
            .glassEffect(
                shape = RoundedCornerShape(
                    topStart = 20.dp,
                    topEnd = 20.dp,
                    bottomStart = 4.dp,
                    bottomEnd = 20.dp
                )
            )
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            dots.forEach { animatable ->
                Box(
                    modifier = Modifier
                        .size(dotSize)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = animatable.value))
                )
            }
        }
    }
}
