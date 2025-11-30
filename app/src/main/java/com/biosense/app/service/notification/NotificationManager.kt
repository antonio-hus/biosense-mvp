package com.biosense.app.service.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.biosense.app.MainActivity
import com.biosense.app.R
import com.biosense.app.data.BiosenseDatabase
import com.biosense.app.data.entity.NotificationHistoryEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Manages displaying health notifications to the user.
 */
class NotificationManager(private val context: Context) {

    private val notificationManager: android.app.NotificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
    
    private val database = BiosenseDatabase.getDatabase(context)
    private val historyDao = database.notificationHistoryDao()
    
    // Coroutine scope for database operations
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    companion object {
        private const val CHANNEL_ID = "biosense_health_notifications"
        private const val CHANNEL_NAME = "Health Notifications"
        private const val NOTIFICATION_ID = 1001
    }

    init {
        createNotificationChannel()
    }

    /**
     * Creates the notification channel for Android O+.
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                android.app.NotificationManager.IMPORTANCE_HIGH // Higher importance for health alerts
            ).apply {
                description = "Notifications about your health and wellness"
                enableVibration(true)
                enableLights(true)
                setShowBadge(true)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Checks if notifications are enabled for this app.
     */
    fun areNotificationsEnabled(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            notificationManager.areNotificationsEnabled()
        } else {
            true // Pre-Android 7.0, notifications are always enabled
        }
    }

    /**
     * Shows a health notification to the user.
     * 
     * @param message The notification message to display.
     * @return true if notification was shown, false if permissions are missing
     */
    fun showNotification(message: String): Boolean {
        // Check if notifications are enabled
        if (!areNotificationsEnabled()) {
            android.util.Log.e("NotificationManager", "Notifications are disabled for this app")
            return false
        }
        // Create intent to open the app when notification is tapped
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Use app icon for notification
        val iconRes = android.R.drawable.ic_dialog_info // Using system icon for now
        // Note: For production, you might want to create a white notification icon
        // and use R.drawable.ic_notification or similar

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(iconRes)
            .setContentTitle("Biosense Health Alert")
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText(message)
                .setSummaryText("Tap to open Biosense"))
            .setPriority(NotificationCompat.PRIORITY_HIGH) // High priority for health alerts
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setWhen(System.currentTimeMillis())
            .setShowWhen(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()

        try {
            notificationManager.notify(NOTIFICATION_ID, notification)
            android.util.Log.d("NotificationManager", "Notification shown successfully: $message")
            
            // Save to notification history (async, don't block)
            saveToHistory(message, null)
            
            return true
        } catch (e: SecurityException) {
            android.util.Log.e("NotificationManager", "Failed to show notification - permission denied", e)
            return false
        } catch (e: Exception) {
            android.util.Log.e("NotificationManager", "Failed to show notification", e)
            return false
        }
    }
    
    /**
     * Saves a notification to the history database.
     * 
     * @param message The notification message.
     * @param category Optional category (e.g., "activity", "health", "hydration").
     */
    private fun saveToHistory(message: String, category: String?) {
        scope.launch {
            try {
                // Verify database is accessible
                val countBefore = try {
                    historyDao.getNotificationCount()
                } catch (e: Exception) {
                    android.util.Log.e("NotificationManager", "Cannot access notification_history table - database may need migration", e)
                    android.util.Log.e("NotificationManager", "Error details: ${e.message}", e)
                    e.printStackTrace()
                    return@launch
                }
                
                val historyEntity = NotificationHistoryEntity(
                    message = message,
                    timestamp = System.currentTimeMillis(),
                    category = category
                )
                historyDao.insertNotification(historyEntity)
                
                val countAfter = historyDao.getNotificationCount()
                android.util.Log.d("NotificationManager", "Notification saved to history: $message (count: $countBefore -> $countAfter)")
            } catch (e: Exception) {
                android.util.Log.e("NotificationManager", "Failed to save notification to history", e)
                android.util.Log.e("NotificationManager", "Error type: ${e.javaClass.simpleName}, Message: ${e.message}")
                e.printStackTrace()
            }
        }
    }
    
    /**
     * Shows a notification with a specific category (for future use).
     */
    fun showNotification(message: String, category: String?): Boolean {
        val success = showNotification(message)
        if (success && category != null) {
            // Update the category if notification was shown successfully
            scope.launch {
                try {
                    val recent = historyDao.getRecentNotifications(1)
                    recent.firstOrNull()?.let { latest ->
                        historyDao.insertNotification(
                            latest.copy(category = category)
                        )
                    }
                } catch (e: Exception) {
                    android.util.Log.e("NotificationManager", "Failed to update notification category", e)
                }
            }
        }
        return success
    }

    /**
     * Cancels all health notifications.
     */
    fun cancelAllNotifications() {
        notificationManager.cancelAll()
    }
}

