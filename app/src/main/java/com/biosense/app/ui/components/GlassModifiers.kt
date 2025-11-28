package com.biosense.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Applies the iOS-style Liquid Glass effect (Glassmorphism) defined in the design system.
 */
fun Modifier.glassEffect(
    shape: Shape = RoundedCornerShape(16.dp),
    strokeWidth: Dp = 1.dp
) = this
    .clip(shape)
    .background(
        brush = Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.20f),
                Color.White.copy(alpha = 0.10f)
            ),
            start = Offset(0f, 0f),
            end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
        )
    )
    .border(
        width = strokeWidth,
        brush = Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.30f),
                Color.White.copy(alpha = 0.05f)
            ),
            start = Offset(0f, 0f),
            end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
        ),
        shape = shape
    )
