package com.biosense.app.viewmodel

import android.app.Application
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.biosense.app.data.BiosenseDatabase
import com.biosense.app.data.entity.NotificationHistoryEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.catch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

/**
 * ViewModel for managing notification history.
 */
class NotificationHistoryViewModel(application: Application) : AndroidViewModel(application) {

    private val database = BiosenseDatabase.getDatabase(application)
    private val historyDao = database.notificationHistoryDao()

    // Observe notifications as a Flow so it updates automatically when new ones are added
    val notifications: StateFlow<List<NotificationHistoryEntity>> = historyDao.getAllNotifications()
        .catch { e ->
            // Handle any errors from the database Flow
            android.util.Log.e("NotificationHistoryViewModel", "Error observing notifications", e)
            emit(emptyList())
        }
        .stateIn(
            scope = viewModelScope,
            started = kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    /**
     * Loads all notifications from the database.
     * Note: The Flow automatically updates, but this can be used for manual refresh.
     */
    fun loadNotifications() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Force a refresh by getting the count and checking the database
                val count = historyDao.getNotificationCount()
                android.util.Log.d("NotificationHistoryViewModel", "Notification count in database: $count")
                // The Flow will automatically update
            } catch (e: Exception) {
                android.util.Log.e("NotificationHistoryViewModel", "Error loading notifications", e)
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Deletes a specific notification from history.
     */
    fun deleteNotification(id: String) {
        viewModelScope.launch {
            try {
                historyDao.deleteNotification(id)
                // Flow will automatically update
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * Deletes all notifications older than 30 days.
     */
    fun deleteOldNotifications() {
        viewModelScope.launch {
            try {
                val thirtyDaysAgo = Instant.now().minus(30, ChronoUnit.DAYS).toEpochMilli()
                historyDao.deleteOldNotifications(thirtyDaysAgo)
                // Flow will automatically update
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * Groups notifications by date for display.
     */
    fun getGroupedNotifications(notificationList: List<NotificationHistoryEntity> = notifications.value): Map<String, List<NotificationHistoryEntity>> {
        return try {
            val grouped = mutableMapOf<String, MutableList<NotificationHistoryEntity>>()
            val today = LocalDate.now()
            val yesterday = today.minusDays(1)
            val formatter = DateTimeFormatter.ofPattern("MMMM d, yyyy")
            
            notificationList.forEach { notification ->
                try {
                    val date = Instant.ofEpochMilli(notification.timestamp)
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate()
                    
                    val groupKey = when {
                        date == today -> "Today"
                        date == yesterday -> "Yesterday"
                        else -> date.format(formatter)
                    }
                    
                    grouped.getOrPut(groupKey) { mutableListOf() }.add(notification)
                } catch (e: Exception) {
                    android.util.Log.e("NotificationHistoryViewModel", "Error processing notification ${notification.id}", e)
                    // Skip this notification if there's an error
                }
            }
            
            grouped
        } catch (e: Exception) {
            android.util.Log.e("NotificationHistoryViewModel", "Error grouping notifications", e)
            emptyMap()
        }
    }

    /**
     * Formats timestamp to readable time string.
     */
    fun formatTime(timestamp: Long): String {
        val instant = Instant.ofEpochMilli(timestamp)
        val localTime = instant.atZone(ZoneId.systemDefault()).toLocalTime()
        return localTime.format(DateTimeFormatter.ofPattern("h:mm a"))
    }
}

