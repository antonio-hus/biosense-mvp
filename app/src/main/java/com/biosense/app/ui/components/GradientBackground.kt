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
import com.biosense.app.ui.theme.Background

@Composable
fun GradientBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Background)  // Base #F2F2F7
            .drawBehind {
                // Upper right gradient (mint color)
                // CSS: at 97.46% 0%, radius 81.8% 39.44%
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFA8E6CF),
                            Color(0x00A8E6CF)
                        ),
                        center = Offset(size.width * 0.97f, 0f),
                        radius = size.width * 1.0f
                    )
                )

                // Lower left gradient (dark green)
                // CSS: at -48.09% 107.45%, radius 119.31% 56.8%
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xCC377765),
                            Color(0x00377765)
                        ),
                        center = Offset(size.width * -0.48f, size.height * 1.07f),
                        radius = size.height * 0.8f
                    )
                )
            }
    ) {
        content()
    }
}
