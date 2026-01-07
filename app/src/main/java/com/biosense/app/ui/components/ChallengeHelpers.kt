package com.biosense.app.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Get the appropriate Material Icon for a health metric based on its name
 */
fun getMetricIcon(metricName: String): ImageVector {
    return when (metricName) {
        "Heart Rate" -> Icons.Filled.FavoriteBorder
        "Blood Pressure" -> Icons.Filled.Favorite
        "Oxygen Saturation" -> Icons.Filled.Air
        "Body Temperature" -> Icons.Filled.Thermostat
        "Hydration" -> Icons.Filled.WaterDrop
        "Active Calories" -> Icons.Filled.LocalFireDepartment
        "Total Calories" -> Icons.Filled.Bolt
        "Blood Glucose" -> Icons.Filled.Bloodtype
        "Body Fat" -> Icons.Filled.BarChart
        "Body Water Mass" -> Icons.Filled.Opacity
        "Respiratory Rate" -> Icons.Filled.Air
        "Weight" -> Icons.Filled.Scale
        "Sleep" -> Icons.Filled.Bedtime
        "Steps" -> Icons.Filled.DirectionsWalk
        "Distance" -> Icons.Filled.DirectionsRun
        "Exercise" -> Icons.Filled.FitnessCenter
        else -> Icons.Filled.ShowChart
    }
}

/**
 * Get the appropriate accent color for a health metric based on its name
 */
fun getMetricColor(metricName: String): Color {
    return when (metricName) {
        "Heart Rate" -> Color(0xFFE91E63)
        "Blood Pressure" -> Color(0xFF9C27B0)
        "Oxygen Saturation" -> Color(0xFF2196F3)
        "Body Temperature" -> Color(0xFFFF5722)
        "Hydration" -> Color(0xFF00BCD4)
        "Active Calories" -> Color(0xFFFF9800)
        "Total Calories" -> Color(0xFFFFC107)
        "Blood Glucose" -> Color(0xFF4CAF50)
        "Body Fat" -> Color(0xFF8BC34A)
        "Body Water Mass" -> Color(0xFF03A9F4)
        "Respiratory Rate" -> Color(0xFF009688)
        "Weight" -> Color(0xFF795548)
        "Sleep" -> Color(0xFF673AB7)
        "Steps" -> Color(0xFF4CAF50)
        "Distance" -> Color(0xFF00BCD4)
        "Exercise" -> Color(0xFF2196F3)
        else -> Color(0xFF607D8B)
    }
}

/**
 * Get the appropriate Material Icon for a challenge based on its title
 */
fun getChallengeIcon(challengeTitle: String): ImageVector {
    return when {
        challengeTitle.contains("step", ignoreCase = true) ||
        challengeTitle.contains("walk", ignoreCase = true) -> Icons.Filled.DirectionsWalk

        challengeTitle.contains("sleep", ignoreCase = true) ||
        challengeTitle.contains("rest", ignoreCase = true) -> Icons.Filled.Bedtime

        challengeTitle.contains("water", ignoreCase = true) ||
        challengeTitle.contains("hydrat", ignoreCase = true) -> Icons.Filled.WaterDrop

        challengeTitle.contains("heart", ignoreCase = true) ||
        challengeTitle.contains("cardio", ignoreCase = true) -> Icons.Filled.FavoriteBorder

        challengeTitle.contains("calor", ignoreCase = true) ||
        challengeTitle.contains("burn", ignoreCase = true) -> Icons.Filled.LocalFireDepartment

        challengeTitle.contains("exercise", ignoreCase = true) ||
        challengeTitle.contains("workout", ignoreCase = true) -> Icons.Filled.FitnessCenter

        challengeTitle.contains("mindful", ignoreCase = true) ||
        challengeTitle.contains("meditat", ignoreCase = true) -> Icons.Filled.SelfImprovement

        challengeTitle.contains("streak", ignoreCase = true) -> Icons.Filled.Whatshot

        else -> Icons.Filled.Flag
    }
}

/**
 * Get the appropriate accent color for a challenge based on its title
 */
fun getChallengeAccentColor(challengeTitle: String): Color {
    return when {
        challengeTitle.contains("step", ignoreCase = true) ||
        challengeTitle.contains("walk", ignoreCase = true) -> Color(0xFF4CAF50)

        challengeTitle.contains("sleep", ignoreCase = true) -> Color(0xFF9C27B0)

        challengeTitle.contains("water", ignoreCase = true) ||
        challengeTitle.contains("hydrat", ignoreCase = true) -> Color(0xFF03A9F4)

        challengeTitle.contains("heart", ignoreCase = true) ||
        challengeTitle.contains("cardio", ignoreCase = true) -> Color(0xFFE91E63)

        challengeTitle.contains("calor", ignoreCase = true) ||
        challengeTitle.contains("burn", ignoreCase = true) -> Color(0xFFFF5722)

        challengeTitle.contains("exercise", ignoreCase = true) ||
        challengeTitle.contains("workout", ignoreCase = true) -> Color(0xFF2196F3)

        else -> Color(0xFF64B5F6)
    }
}
