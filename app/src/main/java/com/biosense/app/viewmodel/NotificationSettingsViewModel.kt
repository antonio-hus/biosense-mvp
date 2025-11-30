package com.biosense.app.viewmodel

import android.app.Application
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.biosense.app.BuildConfig
import com.biosense.app.data.BiosenseDatabase
import com.biosense.app.data.entity.NotificationSettingsEntity
import com.biosense.app.data.serializer.HealthContextSerializer
import com.biosense.app.service.api.GeminiService
import com.biosense.app.service.health.FakeHealthConnectManager
import com.biosense.app.service.health.IHealthConnectManager
import com.biosense.app.service.notification.NotificationAnalysisService
import com.biosense.app.service.notification.NotificationManager
import com.biosense.app.service.notification.NotificationScheduler
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit

/**
 * ViewModel for managing notification settings.
 */
class NotificationSettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val database = BiosenseDatabase.getDatabase(application)
    private val settingsDao = database.notificationSettingsDao()

    private val _settings = mutableStateOf(
        NotificationSettingsEntity(
            enabled = true,
            startHour = 8,
            endHour = 22,
            allowOvernight = false,
            checkIntervalMinutes = 60
        )
    )
    val settings: State<NotificationSettingsEntity> = _settings

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _isTesting = mutableStateOf(false)
    val isTesting: State<Boolean> = _isTesting

    private val _testMessage = mutableStateOf<String?>(null)
    val testMessage: State<String?> = _testMessage

    init {
        loadSettings()
    }

    /**
     * Loads settings from the database, or creates default if none exist.
     */
    private fun loadSettings() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val existing = settingsDao.getSettings().first()
                if (existing != null) {
                    _settings.value = existing
                } else {
                    // Create default settings
                    val default = NotificationSettingsEntity()
                    settingsDao.insertOrUpdate(default)
                    _settings.value = default
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Updates the enabled state.
     */
    fun setEnabled(enabled: Boolean) {
        updateSettings(_settings.value.copy(enabled = enabled))
    }

    /**
     * Updates the start hour (0-23).
     */
    fun setStartHour(hour: Int) {
        val clamped = hour.coerceIn(0, 23)
        updateSettings(_settings.value.copy(startHour = clamped))
    }

    /**
     * Updates the end hour (0-23).
     */
    fun setEndHour(hour: Int) {
        val clamped = hour.coerceIn(0, 23)
        updateSettings(_settings.value.copy(endHour = clamped))
    }

    /**
     * Updates the allow overnight setting.
     */
    fun setAllowOvernight(allow: Boolean) {
        updateSettings(_settings.value.copy(allowOvernight = allow))
    }

    /**
     * Updates the check interval in minutes.
     */
    fun setCheckIntervalMinutes(minutes: Int) {
        val clamped = minutes.coerceIn(15, 240) // Between 15 minutes and 4 hours
        updateSettings(_settings.value.copy(checkIntervalMinutes = clamped))
    }

    /**
     * Updates settings in the database and reschedules notifications.
     */
    private fun updateSettings(newSettings: NotificationSettingsEntity) {
        viewModelScope.launch {
            try {
                _settings.value = newSettings
                settingsDao.insertOrUpdate(newSettings)
                
                // Reschedule notifications with new settings
                NotificationScheduler(getApplication()).schedulePeriodicChecks()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * Tests the notification system by running a health check and showing a notification.
     * This will analyze current health data and show a notification if the AI detects something.
     */
    fun testNotification() {
        viewModelScope.launch {
            _isTesting.value = true
            _testMessage.value = null
            
            try {
                // Get API key
                val apiKey = try {
                    BuildConfig.GEMINI_API_KEY
                } catch (e: Exception) {
                    ""
                }
                
                if (apiKey.isEmpty()) {
                    _testMessage.value = "Error: No API key configured. Please add GEMINI_API_KEY to local.properties"
                    return@launch
                }

                // Initialize services
                val healthConnectManager: IHealthConnectManager = FakeHealthConnectManager.getInstance()
                val apiService = GeminiService(apiKey)
                val serializer = HealthContextSerializer()
                val analysisService = NotificationAnalysisService(apiService, serializer)
                val notificationManager = NotificationManager(getApplication())
                val userViewModel = UserViewModel(getApplication())

                // Get user profile
                val user = userViewModel.currentUser.value

                // Get health data for last 24 hours
                val now = Instant.now()
                val startOfDay = now.minus(1, ChronoUnit.DAYS)
                val healthContext = healthConnectManager.getHealthContext(startOfDay, now)
                val healthDataToon = serializer.toToon(healthContext)

                // Analyze and get notification
                val currentTime = LocalTime.now(ZoneId.systemDefault())
                val notificationMessage = analysisService.analyzeAndGetNotification(
                    user = user,
                    healthDataToon = healthDataToon,
                    currentTime = currentTime
                )

                // Check if notifications are enabled
                if (!notificationManager.areNotificationsEnabled()) {
                    _testMessage.value = "❌ Notifications are disabled! Please enable notifications for Biosense in your phone settings."
                    return@launch
                }

                if (notificationMessage != null) {
                    // Show the notification
                    val success = notificationManager.showNotification(notificationMessage)
                    if (success) {
                        _testMessage.value = "✅ Notification sent! Check your notification tray."
                    } else {
                        _testMessage.value = "❌ Failed to show notification. Check app permissions."
                    }
                } else {
                    // Show a test notification anyway so user can verify the system works
                    val testMsg = "🧪 Test notification: Your health data looks good right now!"
                    val success = notificationManager.showNotification(testMsg)
                    if (success) {
                        _testMessage.value = "✅ Test notification sent! (AI didn't detect any issues, but notification system is working)"
                    } else {
                        _testMessage.value = "❌ Failed to show notification. Check app permissions."
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _testMessage.value = "❌ Error: ${e.localizedMessage ?: e.message}"
            } finally {
                _isTesting.value = false
            }
        }
    }
}

