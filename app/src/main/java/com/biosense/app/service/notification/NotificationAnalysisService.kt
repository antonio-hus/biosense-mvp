package com.biosense.app.service.notification

import com.biosense.app.data.model.User
import com.biosense.app.data.serializer.HealthContextSerializer
import com.biosense.app.service.api.IGeminiApiService
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * Service that uses the Gemini AI to analyze health data and determine
 * if a notification should be sent to the user.
 */
class NotificationAnalysisService(
    private val apiService: IGeminiApiService,
    private val serializer: HealthContextSerializer
) {

    /**
     * Analyzes the current health data and determines if a notification should be sent.
     * 
     * @param user The user profile for context.
     * @param healthDataToon The health data in TOON format.
     * @param currentTime The current time of day.
     * @return A notification message if something needs attention, null otherwise.
     */
    suspend fun analyzeAndGetNotification(
        user: User,
        healthDataToon: String,
        currentTime: LocalTime
    ): String? {
        val prompt = buildNotificationAnalysisPrompt(user, healthDataToon, currentTime)
        
        return try {
            val response = apiService.generateResponse(prompt)
            
            // Parse the response - if it starts with [NOTIFICATION:], extract the message
            // Otherwise, if it's "NO_NOTIFICATION", return null
            when {
                response.startsWith("[NOTIFICATION:") -> {
                    val endIndex = response.indexOf("]", startIndex = 14)
                    if (endIndex > 0) {
                        response.substring(14, endIndex).trim()
                    } else {
                        null
                    }
                }
                response.contains("NO_NOTIFICATION", ignoreCase = true) -> null
                else -> {
                    // If the AI didn't follow the format but gave a response, use it
                    // (fallback for flexibility)
                    response.takeIf { it.isNotBlank() && it.length < 200 }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null // Don't send notification on API errors
        }
    }

    /**
     * Constructs a specialized prompt for notification analysis.
     * The AI should only respond with a notification if something needs attention.
     */
    private fun buildNotificationAnalysisPrompt(
        user: User,
        healthDataToon: String,
        currentTime: LocalTime
    ): String {
        val timeStr = currentTime.format(DateTimeFormatter.ofPattern("HH:mm"))
        
        return """
            ROLE: You are Biosense, an expert health advisor analyzing real-time health data for proactive notifications.
            
            USER_PROFILE:
            - Name: ${user.name}
            - Age: ${user.age}, Gender: ${user.gender}
            - Height: ${user.height}cm, Weight: ${user.weight}kg
            - Profession: ${user.profession}
            - Main Goal: ${user.healthGoal}
            - Current Context: "${user.whatSenseKnows}"
            
            CURRENT_TIME: $timeStr
            
            TASK: Analyze the health data below and determine if the user needs a proactive notification RIGHT NOW.
            
            CRITERIA FOR SENDING NOTIFICATION:
            1. **Low Activity Alert**: If steps are significantly low for the current time (e.g., only 2000 steps at 4 PM suggests they should go for a walk)
            2. **Health Anomalies**: Unusual patterns in heart rate, blood pressure, or other vitals that need attention
            3. **Missing Data**: Important health metrics haven't been logged today
            4. **Goal Reminders**: User is behind on their health goals for the day
            5. **Hydration**: Low water intake detected
            6. **Sleep Recovery**: Poor sleep quality detected that might affect the day
            
            DO NOT SEND NOTIFICATIONS FOR:
            - Normal, expected patterns
            - Minor fluctuations
            - Data that's already been addressed
            - Things that can wait until the user opens the app
            
            OUTPUT FORMAT:
            - If a notification is needed, respond EXACTLY: [NOTIFICATION: Your concise, actionable message here]
            - If no notification is needed, respond EXACTLY: NO_NOTIFICATION
            
            EXAMPLES:
            - Low steps at 4 PM: [NOTIFICATION: You've only taken 2000 steps today. Time for a walk! 🚶]
            - Low hydration: [NOTIFICATION: You're behind on hydration today. Drink some water! 💧]
            - Everything fine: NO_NOTIFICATION
            
            Keep notifications SHORT (max 100 characters), friendly, and actionable.
            
            HEALTH_DATA_TOON:
            $healthDataToon
        """.trimIndent()
    }
}


