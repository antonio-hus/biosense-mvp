package com.biosense.app.service.notification

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.biosense.app.data.BiosenseDatabase
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit

/**
 * Manages scheduling and cancellation of periodic health check notifications.
 */
class NotificationScheduler(private val context: Context) {

    private val workManager = WorkManager.getInstance(context)
    private val database = BiosenseDatabase.getDatabase(context)
    private val settingsDao = database.notificationSettingsDao()

    companion object {
        private const val WORK_NAME = "health_check_notifications"
    }

    /**
     * Schedules or updates the periodic health check work based on current settings.
     * Should be called when:
     * - App starts
     * - Notification settings change
     */
    suspend fun schedulePeriodicChecks() {
        val settings = settingsDao.getSettings().first() ?: return

        if (!settings.enabled) {
            cancelPeriodicChecks()
            return
        }

        // Create periodic work request
        // Note: WorkManager has a minimum interval of 15 minutes
        val intervalMinutes = settings.checkIntervalMinutes.coerceAtLeast(15)
        
        val workRequest = PeriodicWorkRequestBuilder<HealthCheckWorker>(
            intervalMinutes.toLong(),
            TimeUnit.MINUTES
        )
            .build()

        // Use REPLACE to update the schedule when settings change (e.g., interval changes)
        // This ensures the worker runs at the user's chosen interval
        workManager.enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.REPLACE, // Replace existing work to update interval
            workRequest
        )
        
        android.util.Log.d("NotificationScheduler", "Scheduled periodic health checks every $intervalMinutes minutes")
    }

    /**
     * Cancels all scheduled health check notifications.
     */
    fun cancelPeriodicChecks() {
        workManager.cancelUniqueWork(WORK_NAME)
    }
}


