package com.biosense.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.biosense.app.health.FakeHealthConnectManager
import com.biosense.app.health.IHealthConnectManager
import com.biosense.app.ui.screens.PinnedMetric
import com.biosense.app.ui.screens.Vital
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

class TodayViewModel(application: Application) : AndroidViewModel(application) {
    
    private val healthConnectManager: IHealthConnectManager = FakeHealthConnectManager.getInstance()
    
    private val _vitals = MutableStateFlow<List<Vital>>(emptyList())
    val vitals: StateFlow<List<Vital>> = _vitals.asStateFlow()
    
    private val _pinnedMetrics = MutableStateFlow<List<PinnedMetric>>(emptyList())
    val pinnedMetrics: StateFlow<List<PinnedMetric>> = _pinnedMetrics.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    // Pin state management for additional metrics
    private val _pinnedMetricNames = MutableStateFlow<Set<String>>(emptySet())
    val pinnedMetricNames: StateFlow<Set<String>> = _pinnedMetricNames.asStateFlow()
    
    // Store raw data for additional metrics
    private val _heartRateData = MutableStateFlow<List<androidx.health.connect.client.records.HeartRateRecord>>(emptyList())
    private val _sleepData = MutableStateFlow<List<androidx.health.connect.client.records.SleepSessionRecord>>(emptyList())
    private val _stepsData = MutableStateFlow<List<androidx.health.connect.client.records.StepsRecord>>(emptyList())
    private val _bloodPressureData = MutableStateFlow<List<androidx.health.connect.client.records.BloodPressureRecord>>(emptyList())
    private val _oxygenSaturationData = MutableStateFlow<List<androidx.health.connect.client.records.OxygenSaturationRecord>>(emptyList())
    private val _bodyTemperatureData = MutableStateFlow<List<androidx.health.connect.client.records.BodyTemperatureRecord>>(emptyList())
    private val _hydrationData = MutableStateFlow<List<androidx.health.connect.client.records.HydrationRecord>>(emptyList())
    private val _activeCaloriesData = MutableStateFlow<List<androidx.health.connect.client.records.ActiveCaloriesBurnedRecord>>(emptyList())
    private val _totalCaloriesData = MutableStateFlow<List<androidx.health.connect.client.records.TotalCaloriesBurnedRecord>>(emptyList())
    private val _bloodGlucoseData = MutableStateFlow<List<androidx.health.connect.client.records.BloodGlucoseRecord>>(emptyList())
    private val _bodyFatData = MutableStateFlow<List<androidx.health.connect.client.records.BodyFatRecord>>(emptyList())
    private val _bodyWaterMassData = MutableStateFlow<List<androidx.health.connect.client.records.BodyWaterMassRecord>>(emptyList())
    private val _nutritionData = MutableStateFlow<List<androidx.health.connect.client.records.NutritionRecord>>(emptyList())
    private val _respiratoryRateData = MutableStateFlow<List<androidx.health.connect.client.records.RespiratoryRateRecord>>(emptyList())
    
    init {
        loadTodayData()
    }
    
    fun pinMetric(metricName: String) {
        val currentPinned = _pinnedMetricNames.value.toMutableSet()
        currentPinned.add(metricName)
        _pinnedMetricNames.value = currentPinned
        
        // Ensure data is loaded before updating pinned metrics
        if (_heartRateData.value.isEmpty()) {
            loadTodayData()
        } else {
            updatePinnedMetrics()
        }
    }
    
    fun unpinMetric(metricName: String) {
        val currentPinned = _pinnedMetricNames.value.toMutableSet()
        currentPinned.remove(metricName)
        _pinnedMetricNames.value = currentPinned
        updatePinnedMetrics()
    }
    
    fun getAvailableMetrics(): List<String> {
        return listOf(
            "Heart Rate",
            "Blood Pressure", 
            "Oxygen Saturation",
            "Body Temperature",
            "Hydration",
            "Active Calories",
            "Total Calories",
            "Blood Glucose",
            "Body Fat",
            "Body Water Mass",
            "Respiratory Rate"
        )
    }
    
    private fun updatePinnedMetrics() {
        val pinnedNames = _pinnedMetricNames.value
        val pinnedMetricsList = mutableListOf<PinnedMetric>()
        
        // Create pinned metrics from available health data
        pinnedNames.forEach { metricName ->
            when (metricName) {
                "Heart Rate" -> {
                    val heartRateData = _heartRateData.value
                    val avgHeartRate = if (heartRateData.isNotEmpty()) {
                        heartRateData.flatMap { it.samples }.map { it.beatsPerMinute }.average().toInt()
                    } else {
                        0
                    }
                    pinnedMetricsList.add(PinnedMetric("Heart Rate", avgHeartRate.toString(), "bpm"))
                }
                "Blood Pressure" -> {
                    val bpData = _bloodPressureData.value
                    if (bpData.isNotEmpty()) {
                        val systolic = bpData.first().systolic.inMillimetersOfMercury.toInt()
                        val diastolic = bpData.first().diastolic.inMillimetersOfMercury.toInt()
                        pinnedMetricsList.add(PinnedMetric("Blood Pressure", "$systolic/$diastolic", "mmHg"))
                    } else {
                        pinnedMetricsList.add(PinnedMetric("Blood Pressure", "120/80", "mmHg"))
                    }
                }
                "Oxygen Saturation" -> {
                    val oxygenData = _oxygenSaturationData.value
                    val oxygenLevel = if (oxygenData.isNotEmpty()) {
                        oxygenData.first().percentage.value.toInt()
                    } else {
                        98
                    }
                    pinnedMetricsList.add(PinnedMetric("Oxygen Saturation", oxygenLevel.toString(), "%"))
                }
                "Body Temperature" -> {
                    val tempData = _bodyTemperatureData.value
                    val temperature = if (tempData.isNotEmpty()) {
                        String.format("%.1f", tempData.first().temperature.inCelsius)
                    } else {
                        "36.5"
                    }
                    pinnedMetricsList.add(PinnedMetric("Body Temperature", temperature, "°C"))
                }
                "Hydration" -> {
                    val hydrationData = _hydrationData.value
                    val hydration = if (hydrationData.isNotEmpty()) {
                        String.format("%.1f", hydrationData.first().volume.inLiters)
                    } else {
                        "2.0"
                    }
                    pinnedMetricsList.add(PinnedMetric("Hydration", hydration, "L"))
                }
                "Active Calories" -> {
                    val activeCaloriesData = _activeCaloriesData.value
                    val calories = if (activeCaloriesData.isNotEmpty()) {
                        activeCaloriesData.sumOf { it.energy.inKilocalories }.toInt()
                    } else {
                        450
                    }
                    pinnedMetricsList.add(PinnedMetric("Active Calories", calories.toString(), "kcal"))
                }
                "Total Calories" -> {
                    val totalCaloriesData = _totalCaloriesData.value
                    val calories = if (totalCaloriesData.isNotEmpty()) {
                        totalCaloriesData.sumOf { it.energy.inKilocalories }.toInt()
                    } else {
                        2200
                    }
                    pinnedMetricsList.add(PinnedMetric("Total Calories", calories.toString(), "kcal"))
                }
                "Blood Glucose" -> {
                    val glucoseData = _bloodGlucoseData.value
                    val glucose = if (glucoseData.isNotEmpty()) {
                        String.format("%.1f", glucoseData.first().level.inMillimolesPerLiter)
                    } else {
                        "5.0"
                    }
                    pinnedMetricsList.add(PinnedMetric("Blood Glucose", glucose, "mmol/L"))
                }
                "Body Fat" -> {
                    val bodyFatData = _bodyFatData.value
                    val bodyFat = if (bodyFatData.isNotEmpty()) {
                        String.format("%.1f", bodyFatData.first().percentage.value)
                    } else {
                        "20.0"
                    }
                    pinnedMetricsList.add(PinnedMetric("Body Fat", bodyFat, "%"))
                }
                "Body Water Mass" -> {
                    val waterMassData = _bodyWaterMassData.value
                    val waterMass = if (waterMassData.isNotEmpty()) {
                        String.format("%.1f", waterMassData.first().mass.inKilograms)
                    } else {
                        "42.0"
                    }
                    pinnedMetricsList.add(PinnedMetric("Body Water Mass", waterMass, "kg"))
                }
                "Respiratory Rate" -> {
                    val respData = _respiratoryRateData.value
                    val respRate = if (respData.isNotEmpty()) {
                        String.format("%.1f", respData.first().rate)
                    } else {
                        "15.0"
                    }
                    pinnedMetricsList.add(PinnedMetric("Respiratory Rate", respRate, "breaths/min"))
                }
            }
        }
        
        _pinnedMetrics.value = pinnedMetricsList
    }
    
    fun loadTodayData() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val today = LocalDate.now()
                val startOfDay = today.atStartOfDay(ZoneOffset.UTC).toInstant()
                val endOfDay = today.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant()
                
                // Load all health data in parallel
                val stepsData = healthConnectManager.readSteps(startOfDay, endOfDay)
                val heartRateData = healthConnectManager.readHeartRate(startOfDay, endOfDay)
                val activeCaloriesData = healthConnectManager.readActiveCaloriesBurned(startOfDay, endOfDay)
                val totalCaloriesData = healthConnectManager.readTotalCaloriesBurned(startOfDay, endOfDay)
                val sleepData = healthConnectManager.readSleepSessions(startOfDay, endOfDay)
                val bloodPressureData = healthConnectManager.readBloodPressure(startOfDay, endOfDay)
                val oxygenSaturationData = healthConnectManager.readOxygenSaturation(startOfDay, endOfDay)
                val bodyTemperatureData = healthConnectManager.readBodyTemperature(startOfDay, endOfDay)
                val hydrationData = healthConnectManager.readHydration(startOfDay, endOfDay)
                
                // Store raw data for additional metrics
                _stepsData.value = stepsData
                _heartRateData.value = heartRateData
                _sleepData.value = sleepData
                _bloodPressureData.value = bloodPressureData
                _oxygenSaturationData.value = oxygenSaturationData
                _bodyTemperatureData.value = bodyTemperatureData
                _hydrationData.value = hydrationData
                _activeCaloriesData.value = activeCaloriesData
                _totalCaloriesData.value = totalCaloriesData
                _bloodGlucoseData.value = healthConnectManager.readBloodGlucose(startOfDay, endOfDay)
                _bodyFatData.value = healthConnectManager.readBodyFat(startOfDay, endOfDay)
                _bodyWaterMassData.value = healthConnectManager.readBodyWaterMass(startOfDay, endOfDay)
                _nutritionData.value = healthConnectManager.readNutrition(startOfDay, endOfDay)
                _respiratoryRateData.value = healthConnectManager.readRespiratoryRate(startOfDay, endOfDay)
                
                // Process vitals data
                val vitalsList = processVitalsData(
                    stepsData, heartRateData, sleepData, 
                    bloodPressureData, oxygenSaturationData, bodyTemperatureData
                )
                _vitals.value = vitalsList
                
                // Update pinned metrics based on current pin state
                updatePinnedMetrics()
                
            } catch (e: Exception) {
                // Handle error - for now, use empty lists
                _vitals.value = emptyList()
                _pinnedMetrics.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    private fun processVitalsData(
        stepsData: List<androidx.health.connect.client.records.StepsRecord>,
        heartRateData: List<androidx.health.connect.client.records.HeartRateRecord>,
        sleepData: List<androidx.health.connect.client.records.SleepSessionRecord>,
        bloodPressureData: List<androidx.health.connect.client.records.BloodPressureRecord>,
        oxygenSaturationData: List<androidx.health.connect.client.records.OxygenSaturationRecord>,
        bodyTemperatureData: List<androidx.health.connect.client.records.BodyTemperatureRecord>
    ): List<Vital> {
        val vitals = mutableListOf<Vital>()
        
        // Calculate readiness score (simplified)
        val readinessScore = calculateReadinessScore(heartRateData, sleepData, bloodPressureData)
        vitals.add(Vital("Readiness", readinessScore))
        
        // Calculate sleep score
        val sleepScore = calculateSleepScore(sleepData)
        vitals.add(Vital("Sleep", sleepScore))
        
        // Calculate activity score based on steps
        val activityScore = calculateActivityScore(stepsData)
        vitals.add(Vital("Activity", activityScore))
        
        // Calculate stress score (simplified based on heart rate variability)
        val stressScore = calculateStressScore(heartRateData)
        vitals.add(Vital("Stress", stressScore))
        
        // Calculate heart rate score
        val heartRateScore = calculateHeartRateScore(heartRateData)
        vitals.add(Vital("Heart Rate", heartRateScore))
        
        return vitals
    }
    
    
    // Helper functions for calculations
    private fun calculateReadinessScore(
        heartRateData: List<androidx.health.connect.client.records.HeartRateRecord>,
        sleepData: List<androidx.health.connect.client.records.SleepSessionRecord>,
        bloodPressureData: List<androidx.health.connect.client.records.BloodPressureRecord>
    ): Int {
        // Simplified readiness calculation
        var score = 50 // Base score
        
        // Factor in sleep quality
        if (sleepData.isNotEmpty()) {
            val sleepDuration = sleepData.first().endTime.epochSecond - sleepData.first().startTime.epochSecond
            val sleepHours = sleepDuration / 3600.0
            score += when {
                sleepHours >= 8 -> 20
                sleepHours >= 7 -> 15
                sleepHours >= 6 -> 10
                else -> 0
            }
        }
        
        // Factor in heart rate (resting heart rate)
        if (heartRateData.isNotEmpty()) {
            val avgHeartRate = heartRateData.flatMap { it.samples }.map { it.beatsPerMinute }.average()
            score += when {
                avgHeartRate < 60 -> 15
                avgHeartRate < 70 -> 10
                avgHeartRate < 80 -> 5
                else -> 0
            }
        }
        
        return score.coerceIn(0, 100)
    }
    
    private fun calculateSleepScore(sleepData: List<androidx.health.connect.client.records.SleepSessionRecord>): Int {
        if (sleepData.isEmpty()) return 50
        
        val sleepDuration = sleepData.first().endTime.epochSecond - sleepData.first().startTime.epochSecond
        val sleepHours = sleepDuration / 3600.0
        
        return when {
            sleepHours >= 8 -> 95
            sleepHours >= 7.5 -> 85
            sleepHours >= 7 -> 75
            sleepHours >= 6.5 -> 65
            sleepHours >= 6 -> 55
            else -> 45
        }
    }
    
    private fun calculateActivityScore(stepsData: List<androidx.health.connect.client.records.StepsRecord>): Int {
        if (stepsData.isEmpty()) return 50
        
        val totalSteps = stepsData.sumOf { it.count }
        return when {
            totalSteps >= 10000 -> 90
            totalSteps >= 8000 -> 80
            totalSteps >= 6000 -> 70
            totalSteps >= 4000 -> 60
            totalSteps >= 2000 -> 50
            else -> 40
        }
    }
    
    private fun calculateStressScore(heartRateData: List<androidx.health.connect.client.records.HeartRateRecord>): Int {
        if (heartRateData.isEmpty()) return 50
        
        val avgHeartRate = heartRateData.flatMap { it.samples }.map { it.beatsPerMinute }.average()
        return when {
            avgHeartRate < 60 -> 85
            avgHeartRate < 70 -> 75
            avgHeartRate < 80 -> 65
            avgHeartRate < 90 -> 55
            else -> 45
        }
    }
    
    private fun calculateHeartRateScore(heartRateData: List<androidx.health.connect.client.records.HeartRateRecord>): Int {
        if (heartRateData.isEmpty()) return 50
        
        val avgHeartRate = heartRateData.flatMap { it.samples }.map { it.beatsPerMinute }.average()
        return when {
            avgHeartRate < 60 -> 95
            avgHeartRate < 70 -> 85
            avgHeartRate < 80 -> 75
            avgHeartRate < 90 -> 65
            else -> 55
        }
    }
    
    private fun calculateSleepDebt(sleepData: List<androidx.health.connect.client.records.SleepSessionRecord>): Double {
        if (sleepData.isEmpty()) return 0.0
        
        val sleepDuration = sleepData.first().endTime.epochSecond - sleepData.first().startTime.epochSecond
        val sleepHours = sleepDuration / 3600.0
        val targetSleep = 8.0
        
        return (targetSleep - sleepHours).coerceAtLeast(0.0)
    }
}
