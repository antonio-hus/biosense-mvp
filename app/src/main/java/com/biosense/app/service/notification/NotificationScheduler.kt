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
     * 
     * @param forceReplace If true, always replace existing work (use when settings change).
     *                     If false, keep existing work if it's already scheduled (use on app start).
     */
    suspend fun schedulePeriodicChecks(forceReplace: Boolean = false) {
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

        // Determine policy based on forceReplace flag
        // If forceReplace is false (app start), use KEEP to avoid immediate execution
        // If forceReplace is true (settings changed), use REPLACE to update interval
        val policy = if (forceReplace) {
            ExistingPeriodicWorkPolicy.REPLACE // Replace when settings change
        } else {
            ExistingPeriodicWorkPolicy.KEEP // Keep existing schedule to avoid immediate execution on app start
        }
        
        workManager.enqueueUniquePeriodicWork(
            WORK_NAME,
            policy,
            workRequest
        )
        
        android.util.Log.d("NotificationScheduler", "Scheduled periodic health checks every $intervalMinutes minutes (policy: $policy)")
    }

    /**
     * Cancels all scheduled health check notifications.
     */
    fun cancelPeriodicChecks() {
        workManager.cancelUniqueWork(WORK_NAME)
    }
}


