package com.biosense.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

@Composable
fun GradientBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F1419))  // Deep dark blue-gray base
            .drawBehind {
                // Upper right gradient (deep teal/cyan)
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF1A4D5C),  // Deep teal
                            Color(0x001A4D5C)
                        ),
                        center = Offset(size.width * 0.97f, 0f),
                        radius = size.width * 1.0f
                    )
                )

                // Lower left gradient (deep purple-blue)
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xCC1E2538),  // Deep blue-purple
                            Color(0x001E2538)
                        ),
                        center = Offset(size.width * -0.48f, size.height * 1.07f),
                        radius = size.height * 0.8f
                    )
                )

                // Center accent gradient (subtle deep purple)
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0x4D2D1B42),  // Subtle purple accent
                            Color(0x002D1B42)
                        ),
                        center = Offset(size.width * 0.5f, size.height * 0.5f),
                        radius = size.height * 0.6f
                    )
                )
            }
    ) {
        content()
    }
}
