package com.biosense.app.service.notification

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.biosense.app.BuildConfig
import com.biosense.app.data.BiosenseDatabase
import com.biosense.app.data.serializer.HealthContextSerializer
import com.biosense.app.service.api.GeminiService
import com.biosense.app.service.health.FakeHealthConnectManager
import com.biosense.app.service.health.IHealthConnectManager
import com.biosense.app.viewmodel.UserViewModel
import kotlinx.coroutines.flow.first
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit

/**
 * WorkManager worker that periodically checks health data and sends notifications
 * if the AI detects something that needs attention.
 */
class HealthCheckWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            // Initialize dependencies
            val database = BiosenseDatabase.getDatabase(applicationContext)
            val settingsDao = database.notificationSettingsDao()
            val settings = settingsDao.getSettingsSync() ?: return Result.success()

            // Check if notifications are enabled
            if (!settings.enabled) {
                return Result.success()
            }

            // Check if current time is within allowed hours
            val currentTime = LocalTime.now(ZoneId.systemDefault())
            val currentHour = currentTime.hour
            
            val isWithinHours = if (settings.allowOvernight) {
                true // Allow all hours
            } else {
                // Check if current hour is between startHour and endHour
                if (settings.startHour <= settings.endHour) {
                    currentHour >= settings.startHour && currentHour < settings.endHour
                } else {
                    // Handle overnight range (e.g., 22 to 8)
                    currentHour >= settings.startHour || currentHour < settings.endHour
                }
            }

            if (!isWithinHours) {
                return Result.success() // Outside notification hours
            }

            // Initialize services
            val apiKey = try {
                BuildConfig.GEMINI_API_KEY
            } catch (e: Exception) {
                ""
            }
            
            if (apiKey.isEmpty()) {
                return Result.success() // No API key, skip
            }

            val healthConnectManager: IHealthConnectManager = FakeHealthConnectManager.getInstance()
            val apiService = GeminiService(apiKey)
            val serializer = HealthContextSerializer()
            val analysisService = NotificationAnalysisService(apiService, serializer)
            val notificationManager = NotificationManager(applicationContext)
            val userViewModel = UserViewModel(applicationContext as android.app.Application)

            // Get user profile
            val user = userViewModel.currentUser.value

            // Get health data for today
            val now = Instant.now()
            val startOfDay = now.minus(1, ChronoUnit.DAYS) // Get last 24 hours for context
            val healthContext = healthConnectManager.getHealthContext(startOfDay, now)
            val healthDataToon = serializer.toToon(healthContext)

            // Analyze and get notification
            val notificationMessage = analysisService.analyzeAndGetNotification(
                user = user,
                healthDataToon = healthDataToon,
                currentTime = currentTime
            )

            // Show notification if needed
            if (notificationMessage != null) {
                notificationManager.showNotification(notificationMessage)
            }

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            // Return success to avoid retrying on transient errors
            // In production, you might want to retry on certain errors
            Result.success()
        }
    }
}

