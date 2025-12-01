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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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

            // Content padding
            Spacer(modifier = Modifier.height(12.dp))

            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color.White)
                }
            } else {
                // Header section - matching registration style
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                ) {
                    Text(
                        text = "Your Health Journey",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Track your progress over the last 7 days",
                        fontSize = 16.sp,
                        color = Color.White.copy(alpha = 0.7f),
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Trends content
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (metricTrends.isEmpty()) {
                        EmptyTrendsCard()
                    } else {
                        metricTrends.forEach { mt ->
                            EnhancedTrendCard(
                                title = mt.name,
                                unit = mt.unit,
                                points = mt.points
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun EmptyTrendsCard() {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = Color.Black.copy(alpha = 0.1f),
                spotColor = Color.Black.copy(alpha = 0.1f)
            ),
        shape = RoundedCornerShape(16.dp),
        color = Color.Transparent
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = Color.White.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(16.dp)
                )
                .border(
                    width = 1.dp,
                    color = Color.White.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(24.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "📊",
                    fontSize = 48.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "No metrics to track yet",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Pin metrics from the Today screen to see trends",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )
            }
        }
    }
}

@Composable
private fun EnhancedTrendCard(
    title: String,
    unit: String?,
    points: List<Double>
) {
    val currentValue = points.lastOrNull() ?: 0.0
    val previousValue = points.getOrNull(points.size - 2) ?: currentValue
    val change = currentValue - previousValue
    val changePercent = if (previousValue != 0.0) (change / previousValue * 100) else 0.0
    val isPositive = change >= 0
    val changeColor = if (isPositive) Color(0xFF4CAF50) else Color(0xFFEF5350)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = Color.Black.copy(alpha = 0.1f),
                spotColor = Color.Black.copy(alpha = 0.1f)
            ),
        shape = RoundedCornerShape(16.dp),
        color = Color.Transparent
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = Color.White.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(16.dp)
                )
                .border(
                    width = 1.dp,
                    color = Color.White.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(20.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header Row - matching registration style
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = title,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        if (unit != null) {
                            Text(
                                text = "Measured in $unit",
                                fontSize = 14.sp,
                                color = Color.White.copy(alpha = 0.7f),
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }

                    // Change indicator - matching registration badge style
                    Box(
                        modifier = Modifier
                            .background(
                                color = changeColor.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .border(
                                width = 1.dp,
                                color = changeColor.copy(alpha = 0.3f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "${if (isPositive) "+" else ""}${String.format("%.1f", changePercent)}%",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                }

                // Current value display
                Column {
                    Text(
                        text = "Current Value",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${String.format("%.1f", currentValue)} ${unit ?: ""}".trim(),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                // Sparkline chart
                Sparkline(points = points)

                // 7-day label
                Text(
                    text = "Last 7 days",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
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

    val strokeColor = Color(0xFF64B5F6).copy(alpha = 1f) // Vibrant blue
    val fillColorTop = Color(0xFF64B5F6).copy(alpha = 0.4f)
    val fillColorBottom = Color(0xFF64B5F6).copy(alpha = 0.05f)
    val gridColor = Color.White.copy(alpha = 0.12f)
    val pointColor = Color(0xFF1976D2).copy(alpha = 0.9f)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .background(
                color = Color.White.copy(alpha = 0.05f),
                shape = RoundedCornerShape(12.dp)
            )
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.1f),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(12.dp)
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            // Grid lines (horizontal) - subtle
            val gridLines = 4
            for (i in 0..gridLines) {
                val y = size.height * i / gridLines
                drawLine(
                    color = gridColor,
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 0.5f
                )
            }

            // Build path
            val stepX = if (safePoints.size > 1) size.width / (safePoints.size - 1) else 0f
            val path = Path()
            val areaPath = Path()
            val pointPositions = mutableListOf<Offset>()

            safePoints.forEachIndexed { idx, v ->
                val x = stepX * idx
                val yNorm = (v - min) / range
                val y = size.height - (yNorm * size.height).toFloat()
                pointPositions.add(Offset(x, y))

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

            // Fill area with gradient
            drawPath(
                path = areaPath,
                brush = Brush.verticalGradient(
                    colors = listOf(fillColorTop, fillColorBottom),
                    startY = 0f,
                    endY = size.height
                )
            )

            // Stroke line with glow effect
            drawPath(
                path = path,
                color = strokeColor.copy(alpha = 0.3f),
                style = Stroke(width = 10f, cap = StrokeCap.Round)
            )
            drawPath(
                path = path,
                color = strokeColor,
                style = Stroke(width = 4f, cap = StrokeCap.Round)
            )

            // Draw data points
            pointPositions.forEach { position ->
                drawCircle(
                    color = Color.White,
                    radius = 8f,
                    center = position
                )
                drawCircle(
                    color = pointColor,
                    radius = 5f,
                    center = position
                )
            }
        }
    }
}
