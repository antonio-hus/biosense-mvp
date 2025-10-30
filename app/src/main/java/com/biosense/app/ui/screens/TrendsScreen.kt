package com.biosense.app.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.biosense.app.ui.components.GlassNavBar
import com.biosense.app.ui.components.Header
import com.biosense.app.viewmodel.TodayViewModel
import com.biosense.app.viewmodel.TrendsViewModel
import com.biosense.app.viewmodel.VitalTrend
import com.biosense.app.viewmodel.MetricTrend

@Composable
fun TrendsScreen(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    onProfileClick: () -> Unit,
    todayViewModel: TodayViewModel,
    trendsViewModel: TrendsViewModel = viewModel()
) {
    val isLoading by trendsViewModel.isLoading.collectAsState()
    val vitalTrends by trendsViewModel.vitalTrends.collectAsState()
    val metricTrends by trendsViewModel.metricTrends.collectAsState()
    val pinnedNames by todayViewModel.pinnedMetricNames.collectAsState()

    LaunchedEffect(pinnedNames) {
        trendsViewModel.loadTrends(pinnedNames)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        bottomBar = {
            GlassNavBar(
                selectedRoute = currentRoute,
                onItemSelected = onNavigate
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            Header(
                title = "Trends",
                onProfileClick = onProfileClick
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color.White)
                }
            } else {
                TrendsSection(title = "Vitals Trends", subtitle = "Last 7 days") {
                    vitalTrends.forEach { trend ->
                        TrendCard(
                            title = trend.name,
                            unit = null,
                            points = trend.points.map { it.toDouble() }
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                TrendsSection(title = "Pinned Metrics", subtitle = "Last 7 days") {
                    if (metricTrends.isEmpty()) {
                        Text(
                            text = "No pinned metrics",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 14.sp,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    } else {
                        metricTrends.forEach { mt ->
                            TrendCard(
                                title = mt.name,
                                unit = mt.unit,
                                points = mt.points
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun TrendsSection(
    title: String,
    subtitle: String,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = title,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = subtitle,
            fontSize = 14.sp,
            color = Color.White.copy(alpha = 0.7f)
        )
        Spacer(modifier = Modifier.height(12.dp))
        content()
    }
}

@Composable
private fun TrendCard(
    title: String,
    unit: String?,
    points: List<Double>
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = Color.Transparent
    ) {
        Box(
            modifier = Modifier
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.2f),
                            Color.White.copy(alpha = 0.1f)
                        )
                    ),
                    shape = RoundedCornerShape(20.dp)
                )
                .border(
                    width = 1.dp,
                    color = Color.White.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(16.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                    if (unit != null) {
                        Text(
                            text = unit,
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }
                Sparkline(points = points)
            }
        }
    }
}

@Composable
private fun Sparkline(points: List<Double>) {
    val safePoints = if (points.isEmpty()) List(7) { 0.0 } else points
    val min = safePoints.minOrNull() ?: 0.0
    val max = safePoints.maxOrNull() ?: 1.0
    val range = if (max - min < 1e-6) 1.0 else (max - min)

    val strokeColor = Color(0xFFFFD54F).copy(alpha = 0.9f)
    val fillColor = Color(0xFFFFD54F).copy(alpha = 0.25f)
    val gridColor = Color.White.copy(alpha = 0.15f)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            // Grid lines (horizontal)
            val gridLines = 3
            for (i in 0..gridLines) {
                val y = size.height * i / gridLines
                drawLine(
                    color = gridColor,
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 1f
                )
            }

            // Build path
            val stepX = if (safePoints.size > 1) size.width / (safePoints.size - 1) else 0f
            val path = Path()
            val areaPath = Path()
            safePoints.forEachIndexed { idx, v ->
                val x = stepX * idx
                val yNorm = (v - min) / range
                val y = size.height - (yNorm * size.height).toFloat()
                if (idx == 0) {
                    path.moveTo(x, y)
                    areaPath.moveTo(x, size.height)
                    areaPath.lineTo(x, y)
                } else {
                    path.lineTo(x, y)
                    areaPath.lineTo(x, y)
                }
            }
            // Close area
            areaPath.lineTo(size.width, size.height)
            areaPath.close()

            // Fill area
            drawPath(
                path = areaPath,
                color = fillColor
            )

            // Stroke line
            drawPath(
                path = path,
                color = strokeColor,
                style = Stroke(width = 6f, cap = StrokeCap.Round)
            )
        }
    }
}
