package com.biosense.app.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.ui.graphics.vector.ImageVector
import com.biosense.app.ui.components.HealthWaveAnimation
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.biosense.app.data.model.*
import com.biosense.app.ui.components.GlassNavBar
import com.biosense.app.ui.components.Header
import com.biosense.app.ui.components.getChallengeIcon
import com.biosense.app.ui.components.getChallengeAccentColor
import com.biosense.app.ui.components.getMetricIcon
import com.biosense.app.ui.components.getMetricColor
import com.biosense.app.viewmodel.TodayViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.random.Random

// --- Data Models ---
data class Vital(val name: String, val score: Int)
data class PinnedMetric(val name: String, val value: String, val unit: String)
data class HealthScoreBreakdown(
    val sleep: Int = 0,
    val activity: Int = 0,
    val heartHealth: Int = 0,
    val recovery: Int = 0
)


@Composable
fun TodayScreen(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    onProfileClick: () -> Unit,
    onNavigateToNotificationHistory: () -> Unit = {},
    viewModel: TodayViewModel = viewModel()
) {
    val pinnedMetrics by viewModel.pinnedMetrics.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val userProgress by viewModel.userProgress.collectAsState()
    val dailyChallenges by viewModel.dailyChallenges.collectAsState()
    val dailyHealthScore by viewModel.dailyHealthScore.collectAsState()
    val healthScoreBreakdown by viewModel.healthScoreBreakdown.collectAsState()

    // Set default pinned metrics on first composition
    LaunchedEffect(Unit) {
        viewModel.setDefaultPinnedMetrics()
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
        Box(modifier = Modifier.fillMaxSize()) {
            // Heartbeat animation background
            HealthWaveAnimation(
                alpha = 0.15f,
                modifier = Modifier.fillMaxSize()
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
            ) {
            // Header
            Header(
                title = "Today",
                onProfileClick = onProfileClick,
                onNotificationClick = onNavigateToNotificationHistory
            )

            // Content padding
            Spacer(modifier = Modifier.height(12.dp))

            // Quick Stats Overview
            QuickStatsOverview(
                dailyHealthScore = dailyHealthScore,
                healthScoreBreakdown = healthScoreBreakdown,
                userProgress = userProgress,
                onNavigate = onNavigate,
                onNavigateToNotificationHistory = onNavigateToNotificationHistory
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Show loading indicator or content
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color.White)
                }
            } else {
                // --- ML Abnormality Detection Card ---
                MLAbnormalityCard()

                // Add space between sections
                Spacer(modifier = Modifier.height(24.dp))

                // --- Pinned Section ---
                PinnedSection(
                    pinnedMetrics = pinnedMetrics,
                    onUnpin = { metricName -> viewModel.unpinMetric(metricName) },
                    onAddMetric = {
                        // Navigate to Metrics Selection screen to find and pin metrics
                        onNavigate("metrics_selection")
                    },
                    onMetricClick = { metricName ->
                        // Navigate to AI chat screen with prefilled prompt about this metric
                        val prompt = "Tell me about my $metricName metric and how I can improve it."
                        onNavigate("chat?prompt=$prompt")
                    }
                )
            }

            // Add final padding at the bottom
            Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun getGreeting(): String {
    val hour = java.time.LocalTime.now().hour
    return when (hour) {
        in 5..11 -> "morning"
        in 12..17 -> "afternoon"
        in 18..21 -> "evening"
        else -> "night"
    }
}

// --- ML Abnormality Detection Card ---
@Composable
fun MLAbnormalityCard() {
    var isExpanded by remember { mutableStateOf(false) }
    val analysisStatus = remember { getMLAnalysisStatus() }

    val (statusText, statusIcon, statusColor, detailText) = when (analysisStatus) {
        HealthStatus.NORMAL -> Tuple4(
            "All Clear",
            Icons.Filled.CheckCircle,
            Color(0xFF4CAF50),
            "Your health metrics are within optimal ranges. Keep up the great work!"
        )
        HealthStatus.WATCH -> Tuple4(
            "Minor Anomaly",
            Icons.Filled.Warning,
            Color(0xFFFFA726),
            "Some metrics show slight deviations. Monitor your sleep and activity levels."
        )
        HealthStatus.ALERT -> Tuple4(
            "Attention Needed",
            Icons.Filled.Error,
            Color(0xFFEF5350),
            "Multiple metrics outside normal range. Consider consulting healthcare provider."
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
    ) {
        // Compact header
        Text(
            text = "AI Health Analysis",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Status Card - clickable to expand
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 4.dp,
                    shape = RoundedCornerShape(16.dp),
                    ambientColor = Color.Black.copy(alpha = 0.1f),
                    spotColor = Color.Black.copy(alpha = 0.1f)
                )
                .clickable { isExpanded = !isExpanded },
            shape = RoundedCornerShape(16.dp),
            color = Color.Transparent
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                statusColor.copy(alpha = 0.15f),
                                statusColor.copy(alpha = 0.05f)
                            )
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .border(
                        width = 1.dp,
                        color = statusColor.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(16.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            // Status Icon - compact
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(
                                        color = statusColor.copy(alpha = 0.2f),
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = statusIcon,
                                    contentDescription = statusText,
                                    tint = statusColor,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Column {
                                Text(
                                    text = statusText,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                if (!isExpanded) {
                                    Text(
                                        text = "Tap for details",
                                        fontSize = 12.sp,
                                        color = Color.White.copy(alpha = 0.6f)
                                    )
                                }
                            }
                        }

                        // Expand indicator
                        Icon(
                            imageVector = if (isExpanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                            contentDescription = if (isExpanded) "Collapse" else "Expand",
                            tint = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    // Expandable content
                    androidx.compose.animation.AnimatedVisibility(visible = isExpanded) {
                        Column {
                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = detailText,
                                fontSize = 14.sp,
                                color = Color.White.copy(alpha = 0.8f),
                                lineHeight = 20.sp
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Analysis Details Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                AnalysisMetric(
                                    label = "Data Points",
                                    value = "2,847",
                                    modifier = Modifier.weight(1f)
                                )
                                AnalysisMetric(
                                    label = "Confidence",
                                    value = "97%",
                                    modifier = Modifier.weight(1f)
                                )
                                AnalysisMetric(
                                    label = "Last Scan",
                                    value = "Now",
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AnalysisMetric(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .background(
                color = Color.White.copy(alpha = 0.1f),
                shape = RoundedCornerShape(12.dp)
            )
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.2f),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(vertical = 12.dp)
    ) {
        Text(
            text = value,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color.White.copy(alpha = 0.7f)
        )
    }
}

// Helper classes
enum class HealthStatus { NORMAL, WATCH, ALERT }

data class Tuple4<A, B, C, D>(val a: A, val b: B, val c: C, val d: D)

private fun getMLAnalysisStatus(): HealthStatus {
    // Simulate ML analysis - in production, this would call actual ML model
    val randomValue = Random.nextInt(100)
    return when {
        randomValue < 70 -> HealthStatus.NORMAL
        randomValue < 90 -> HealthStatus.WATCH
        else -> HealthStatus.ALERT
    }
}

// --- Pinned Section Composable ---
@Composable
fun PinnedSection(
    pinnedMetrics: List<PinnedMetric>,
    onUnpin: (String) -> Unit,
    onAddMetric: () -> Unit,
    onMetricClick: (String) -> Unit = {} // Add callback for metric click
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
    ) {
        // Compact section header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Your Metrics",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            // Add metric button - more compact
            IconButton(
                onClick = onAddMetric,
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = Color.White.copy(alpha = 0.15f),
                        shape = CircleShape
                    )
                    .border(
                        width = 1.dp,
                        color = Color.White.copy(alpha = 0.3f),
                        shape = CircleShape
                    )
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Add Metric",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Show message if no pinned metrics
        if (pinnedMetrics.isEmpty()) {
            EmptyPinnedMetricsCard(onAddMetric = onAddMetric)
        } else {
            // Use a Column to list pinned items vertically
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                pinnedMetrics.forEach { metric ->
                    PinnedMetricItem(
                        metric = metric,
                        onUnpin = { onUnpin(metric.name) },
                        onClick = { onMetricClick(metric.name) } // Pass the click handler
                    )
                }
            }
        }
    }
}

// --- Empty Pinned Metrics Card ---
@Composable
fun EmptyPinnedMetricsCard(onAddMetric: () -> Unit) {
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
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Empty state icon - matching registration style
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Add Metrics",
                    tint = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.size(48.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "No metrics pinned yet",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Tap the + button to add your favorite health metrics",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )
            }
        }
    }
}

// --- Pinned Metric Item Composable ---
@Composable
fun PinnedMetricItem(
    metric: PinnedMetric,
    onUnpin: () -> Unit,
    onClick: () -> Unit = {}
) {
    var isExpanded by remember { mutableStateOf(false) }
    val metricIcon = getMetricIcon(metric.name)
    val accentColor = getMetricColor(metric.name)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = Color.Black.copy(alpha = 0.1f),
                spotColor = Color.Black.copy(alpha = 0.1f)
            )
            .clickable { isExpanded = !isExpanded },
        shape = RoundedCornerShape(16.dp),
        color = Color.Transparent
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            accentColor.copy(alpha = 0.15f),
                            accentColor.copy(alpha = 0.05f)
                        )
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
                .border(
                    width = 1.dp,
                    color = accentColor.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(16.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Left side with icon and compact info
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        // Icon - professional Material Icon
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    color = accentColor.copy(alpha = 0.2f),
                                    shape = CircleShape
                                )
                                .border(
                                    width = 1.5.dp,
                                    color = accentColor.copy(alpha = 0.4f),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = metricIcon,
                                contentDescription = metric.name,
                                tint = accentColor,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Column {
                            Text(
                                text = metric.name,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.Bottom
                            ) {
                                Text(
                                    text = metric.value,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = metric.unit,
                                    fontSize = 13.sp,
                                    color = Color.White.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }

                    // Right side with expand/unpin buttons
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Expand indicator
                        Icon(
                            imageVector = if (isExpanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                            contentDescription = if (isExpanded) "Collapse" else "Expand",
                            tint = Color.White.copy(alpha = 0.6f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Expandable detailed view
                androidx.compose.animation.AnimatedVisibility(visible = isExpanded) {
                    Column {
                        Spacer(modifier = Modifier.height(16.dp))

                        // Quick actions row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Ask AI button
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(
                                        color = Color(0xFF1976D2).copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = Color(0xFF64B5F6).copy(alpha = 0.3f),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable { onClick() }
                                    .padding(12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Ask AI",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.White
                                )
                            }

                            // Unpin button
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(
                                        color = Color.White.copy(alpha = 0.1f),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = Color.White.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable { onUnpin() }
                                    .padding(12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.PushPin,
                                        contentDescription = "Unpin",
                                        tint = Color.White.copy(alpha = 0.8f),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = "Unpin",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color.White.copy(alpha = 0.8f)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Info text
                        Text(
                            text = "Tap 'Ask AI' to learn more about this metric",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.6f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

// Note: Metric styling now uses shared helper functions from ChallengeHelpers.kt
// - getMetricIcon(metricName): ImageVector
// - getMetricColor(metricName): Color

// === HEALTH SCORE COMPONENTS ===

// Quick Stats Overview - Health-focused dashboard
@Composable
fun QuickStatsOverview(
    dailyHealthScore: Int,
    healthScoreBreakdown: HealthScoreBreakdown,
    userProgress: UserProgress,
    onNavigate: (String) -> Unit,
    onNavigateToNotificationHistory: () -> Unit
) {
    val currentDate = LocalDate.now()
    val formatter = DateTimeFormatter.ofPattern("EEEE, MMMM d")
    val formattedDate = currentDate.format(formatter)

    val currentHour = java.time.LocalTime.now().hour

    // Determine contextual insight based on time and health data
    val insightText = when {
        currentHour in 5..11 -> {
            if (healthScoreBreakdown.sleep < 60) {
                "Your sleep score is low (${healthScoreBreakdown.sleep}/100). Try to get 7-8 hours tonight for better recovery."
            } else {
                "Great morning! Your sleep score is ${healthScoreBreakdown.sleep}/100. You're well-rested for the day ahead."
            }
        }
        currentHour in 12..17 -> {
            if (healthScoreBreakdown.activity < 50) {
                "Low activity detected. A 15-minute walk can boost your score by 10-15 points!"
            } else {
                "Good activity level (${healthScoreBreakdown.activity}/100). Keep moving to maintain your momentum!"
            }
        }
        currentHour in 18..21 -> {
            if (healthScoreBreakdown.heartHealth < 70) {
                "Heart rate elevated today. Try 5 minutes of deep breathing to improve recovery."
            } else {
                "Heart health looking good (${healthScoreBreakdown.heartHealth}/100). Wind down for quality sleep tonight."
            }
        }
        else -> {
            if (dailyHealthScore < 70) {
                "Time to rest. Your health score is ${dailyHealthScore}/100. Prioritize 7-8 hours of sleep."
            } else {
                "Great day! Score: ${dailyHealthScore}/100. Get good sleep to maintain this tomorrow."
            }
        }
    }

    // Determine health score color and message
    val (scoreColor, scoreMessage) = when {
        dailyHealthScore >= 85 -> Pair(Color(0xFF4CAF50), "Excellent")
        dailyHealthScore >= 70 -> Pair(Color(0xFF8BC34A), "Great")
        dailyHealthScore >= 55 -> Pair(Color(0xFF64B5F6), "Good")
        dailyHealthScore >= 40 -> Pair(Color(0xFFFFA726), "Fair")
        else -> Pair(Color(0xFFEF5350), "Needs Attention")
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
    ) {
        // Welcome text with streak
        Column {
            // Good morning with streak
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Good ${getGreeting()}!",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                // Small streak indicator with Material Icon
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier
                        .background(
                            color = Color(0xFFFF5722).copy(alpha = 0.2f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .border(
                            width = 1.dp,
                            color = Color(0xFFFF5722).copy(alpha = 0.3f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Whatshot,
                        contentDescription = "Streak",
                        tint = Color(0xFFFF5722),
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "${userProgress.currentStreak}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Text(
                text = formattedDate,
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.7f),
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(top = 4.dp)
            )

            // AI Insight text
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = insightText,
                fontSize = 13.sp,
                color = Color.White.copy(alpha = 0.85f),
                lineHeight = 18.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Daily Health Score - Featured
        HealthScoreCard(
            score = dailyHealthScore,
            breakdown = healthScoreBreakdown,
            scoreColor = scoreColor,
            scoreMessage = scoreMessage
        )

    }
}

// Health Score Card with Breakdown
@Composable
fun HealthScoreCard(
    score: Int,
    breakdown: HealthScoreBreakdown,
    scoreColor: Color,
    scoreMessage: String
) {
    var isExpanded by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = Color.Black.copy(alpha = 0.1f),
                spotColor = Color.Black.copy(alpha = 0.1f)
            )
            .clickable { isExpanded = !isExpanded },
        shape = RoundedCornerShape(20.dp),
        color = Color.Transparent
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            scoreColor.copy(alpha = 0.2f),
                            scoreColor.copy(alpha = 0.1f)
                        )
                    ),
                    shape = RoundedCornerShape(20.dp)
                )
                .border(
                    width = 2.dp,
                    color = scoreColor.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(20.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Daily Health Score",
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.8f),
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "$score",
                                fontSize = 48.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Column(modifier = Modifier.padding(bottom = 8.dp)) {
                                Text(
                                    text = "/100",
                                    fontSize = 16.sp,
                                    color = Color.White.copy(alpha = 0.6f)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))

                        // Circle rating
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val filledCircles = when {
                                score >= 85 -> 5
                                score >= 70 -> 4
                                score >= 55 -> 3
                                score >= 40 -> 2
                                else -> 1
                            }

                            repeat(5) { index ->
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .background(
                                            color = if (index < filledCircles) scoreColor else Color.White.copy(alpha = 0.3f),
                                            shape = CircleShape
                                        )
                                        .border(
                                            width = 1.dp,
                                            color = scoreColor.copy(alpha = 0.5f),
                                            shape = CircleShape
                                        )
                                )
                            }

                            Spacer(modifier = Modifier.width(4.dp))

                            Text(
                                text = scoreMessage,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = scoreColor
                            )
                        }
                    }

                    // Expand indicator
                    Icon(
                        imageVector = if (isExpanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                        contentDescription = if (isExpanded) "Collapse" else "Expand",
                        tint = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Expandable breakdown
                androidx.compose.animation.AnimatedVisibility(visible = isExpanded) {
                    Column {
                        Spacer(modifier = Modifier.height(20.dp))

                        Text(
                            text = "Score Breakdown",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.9f)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        HealthScoreBreakdownItem("Sleep", breakdown.sleep, Icons.Filled.Bedtime)
                        Spacer(modifier = Modifier.height(8.dp))
                        HealthScoreBreakdownItem("Activity", breakdown.activity, Icons.Filled.FitnessCenter)
                        Spacer(modifier = Modifier.height(8.dp))
                        HealthScoreBreakdownItem("Heart Health", breakdown.heartHealth, Icons.Filled.FavoriteBorder)
                        Spacer(modifier = Modifier.height(8.dp))
                        HealthScoreBreakdownItem("Recovery", breakdown.recovery, Icons.Filled.BatteryFull)
                    }
                }
            }
        }
    }
}

@Composable
fun HealthScoreBreakdownItem(label: String, score: Int, icon: ImageVector) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color.White.copy(alpha = 0.7f),
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = label,
                fontSize = 13.sp,
                color = Color.White.copy(alpha = 0.8f)
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .width(80.dp)
                    .height(6.dp)
                    .background(
                        color = Color.White.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(3.dp)
                    )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(score / 100f)
                        .height(6.dp)
                        .background(
                            color = Color.White.copy(alpha = 0.8f),
                            shape = RoundedCornerShape(3.dp)
                        )
                )
            }
            Text(
                text = "$score",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.width(30.dp)
            )
        }
    }
}

@Composable
fun CompactStatCard(
    emoji: String,
    value: String,
    label: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .shadow(
                elevation = 2.dp,
                shape = RoundedCornerShape(12.dp),
                ambientColor = Color.Black.copy(alpha = 0.1f),
                spotColor = Color.Black.copy(alpha = 0.1f)
            )
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        color = Color.Transparent
    ) {
        Box(
            modifier = Modifier
                .background(
                    color = color.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(12.dp)
                )
                .border(
                    width = 1.dp,
                    color = color.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(12.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = emoji,
                    fontSize = 24.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = value,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = label,
                    fontSize = 10.sp,
                    color = Color.White.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun QuickStatCard(
    emoji: String,
    title: String,
    subtitle: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = Color.Black.copy(alpha = 0.1f),
                spotColor = Color.Black.copy(alpha = 0.1f)
            )
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        color = Color.Transparent
    ) {
        Box(
            modifier = Modifier
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            color.copy(alpha = 0.15f),
                            color.copy(alpha = 0.05f)
                        )
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
                .border(
                    width = 1.dp,
                    color = color.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(16.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = emoji,
                    fontSize = 32.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}


@Composable
fun DailyChallengeCard(challenge: DailyChallenge) {
    val accentColor = getChallengeAccentColor(challenge.title)
    val challengeColor = if (challenge.completed) Color(0xFF4CAF50) else accentColor
    val challengeIcon = getChallengeIcon(challenge.title)

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
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            challengeColor.copy(alpha = 0.15f),
                            challengeColor.copy(alpha = 0.05f)
                        )
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
                .border(
                    width = 1.dp,
                    color = challengeColor.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Challenge icon
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            color = challengeColor.copy(alpha = 0.2f),
                            shape = CircleShape
                        )
                        .border(
                            width = 1.5.dp,
                            color = challengeColor.copy(alpha = 0.4f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = challengeIcon,
                        contentDescription = challenge.title,
                        tint = challengeColor,
                        modifier = Modifier.size(26.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Challenge info
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = challenge.title,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )

                        Text(
                            text = "+${challenge.xpReward} XP",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFC107)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = challenge.description,
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.7f)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Progress bar
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${challenge.currentValue}/${challenge.targetValue} ${challenge.unit}",
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                            Text(
                                text = "${(challenge.progress * 100).toInt()}%",
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        val animatedProgress by animateFloatAsState(
                            targetValue = challenge.progress,
                            animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing)
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .background(
                                    color = Color.White.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(3.dp)
                                )
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(animatedProgress)
                                    .height(6.dp)
                                    .background(
                                        color = challengeColor,
                                        shape = RoundedCornerShape(3.dp)
                                    )
                            )
                        }
                    }
                }
            }
        }
    }
}


