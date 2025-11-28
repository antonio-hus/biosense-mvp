package com.biosense.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.biosense.app.service.health.FakeHealthConnectManager
import com.biosense.app.service.health.IHealthConnectManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneOffset

data class VitalTrend(val name: String, val points: List<Int>)

data class MetricTrend(val name: String, val unit: String, val points: List<Double>)

class TrendsViewModel(application: Application) : AndroidViewModel(application) {
    private val healthConnectManager: IHealthConnectManager = FakeHealthConnectManager.getInstance()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _vitalTrends = MutableStateFlow<List<VitalTrend>>(emptyList())
    val vitalTrends: StateFlow<List<VitalTrend>> = _vitalTrends.asStateFlow()

    private val _metricTrends = MutableStateFlow<List<MetricTrend>>(emptyList())
    val metricTrends: StateFlow<List<MetricTrend>> = _metricTrends.asStateFlow()

    fun loadTrends(pinnedMetricNames: Set<String>) {
        _isLoading.value = true
        viewModelScope.launch(Dispatchers.Default) {
            try {
                val today = LocalDate.now()
                val dayStarts = (6 downTo 0).map { offset ->
                    val d = today.minusDays(offset.toLong())
                    d.atStartOfDay(ZoneOffset.UTC).toInstant() to d.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant()
                }

                // Compute vital scores for 7 days
                val readiness = mutableListOf<Int>()
                val sleep = mutableListOf<Int>()
                val activity = mutableListOf<Int>()
                val stress = mutableListOf<Int>()
                val hrScore = mutableListOf<Int>()
                for ((start, end) in dayStarts) {
                    val stepsData = healthConnectManager.readSteps(start, end)
                    val heartRateData = healthConnectManager.readHeartRate(start, end)
                    val sleepData = healthConnectManager.readSleepSessions(start, end)
                    val bpData = healthConnectManager.readBloodPressure(start, end)

                    readiness.add(calculateReadinessScore(heartRateData, sleepData, bpData))
                    sleep.add(calculateSleepScore(sleepData))
                    activity.add(calculateActivityScore(stepsData))
                    stress.add(calculateStressScore(heartRateData))
                    hrScore.add(calculateHeartRateScore(heartRateData))
                }
                _vitalTrends.value = listOf(
                    VitalTrend("Readiness", readiness),
                    VitalTrend("Sleep", sleep),
                    VitalTrend("Activity", activity),
                    VitalTrend("Stress", stress),
                    VitalTrend("Heart Rate", hrScore)
                )

                // Compute pinned metric series
                val metricSeries = mutableListOf<MetricTrend>()
                for (metric in pinnedMetricNames) {
                    when (metric) {
                        "Heart Rate" -> {
                            val pts = dayStarts.map { (start, end) ->
                                val hr = healthConnectManager.readHeartRate(start, end)
                                val avg = hr.flatMap { it.samples }.map { it.beatsPerMinute }.average()
                                if (avg.isNaN()) 0.0 else avg
                            }
                            metricSeries.add(MetricTrend("Heart Rate", "bpm", pts))
                        }
                        "Blood Pressure" -> {
                            val pts = dayStarts.map { (start, end) ->
                                val bp = healthConnectManager.readBloodPressure(start, end)
                                if (bp.isNotEmpty()) bp.first().systolic.inMillimetersOfMercury.toDouble() else 0.0
                            }
                            metricSeries.add(MetricTrend("Blood Pressure (SYS)", "mmHg", pts))
                        }
                        "Oxygen Saturation" -> {
                            val pts = dayStarts.map { (start, end) ->
                                val o2 = healthConnectManager.readOxygenSaturation(start, end)
                                if (o2.isNotEmpty()) o2.first().percentage.value.toDouble() else 0.0
                            }
                            metricSeries.add(MetricTrend("Oxygen Saturation", "%", pts))
                        }
                        "Body Temperature" -> {
                            val pts = dayStarts.map { (start, end) ->
                                val t = healthConnectManager.readBodyTemperature(start, end)
                                if (t.isNotEmpty()) t.first().temperature.inCelsius else 0.0
                            }
                            metricSeries.add(MetricTrend("Body Temperature", "°C", pts))
                        }
                        "Hydration" -> {
                            val pts = dayStarts.map { (start, end) ->
                                val h = healthConnectManager.readHydration(start, end)
                                if (h.isNotEmpty()) h.first().volume.inLiters else 0.0
                            }
                            metricSeries.add(MetricTrend("Hydration", "L", pts))
                        }
                        "Active Calories" -> {
                            val pts = dayStarts.map { (start, end) ->
                                val a = healthConnectManager.readActiveCaloriesBurned(start, end)
                                a.sumOf { it.energy.inKilocalories }
                            }
                            metricSeries.add(MetricTrend("Active Calories", "kcal", pts))
                        }
                        "Total Calories" -> {
                            val pts = dayStarts.map { (start, end) ->
                                val a = healthConnectManager.readTotalCaloriesBurned(start, end)
                                a.sumOf { it.energy.inKilocalories }
                            }
                            metricSeries.add(MetricTrend("Total Calories", "kcal", pts))
                        }
                        "Blood Glucose" -> {
                            val pts = dayStarts.map { (start, end) ->
                                val g = healthConnectManager.readBloodGlucose(start, end)
                                if (g.isNotEmpty()) g.first().level.inMillimolesPerLiter else 0.0
                            }
                            metricSeries.add(MetricTrend("Blood Glucose", "mmol/L", pts))
                        }
                        "Body Fat" -> {
                            val pts = dayStarts.map { (start, end) ->
                                val bf = healthConnectManager.readBodyFat(start, end)
                                if (bf.isNotEmpty()) bf.first().percentage.value.toDouble() else 0.0
                            }
                            metricSeries.add(MetricTrend("Body Fat", "%", pts))
                        }
                        "Body Water Mass" -> {
                            val pts = dayStarts.map { (start, end) ->
                                val w = healthConnectManager.readBodyWaterMass(start, end)
                                if (w.isNotEmpty()) w.first().mass.inKilograms else 0.0
                            }
                            metricSeries.add(MetricTrend("Body Water Mass", "kg", pts))
                        }
                        "Respiratory Rate" -> {
                            val pts = dayStarts.map { (start, end) ->
                                val r = healthConnectManager.readRespiratoryRate(start, end)
                                if (r.isNotEmpty()) r.first().rate else 0.0
                            }
                            metricSeries.add(MetricTrend("Respiratory Rate", "breaths/min", pts))
                        }
                    }
                }
                _metricTrends.value = metricSeries
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Scoring helpers (duplicated from TodayViewModel for isolation)
    private fun calculateReadinessScore(
        heartRateData: List<androidx.health.connect.client.records.HeartRateRecord>,
        sleepData: List<androidx.health.connect.client.records.SleepSessionRecord>,
        bloodPressureData: List<androidx.health.connect.client.records.BloodPressureRecord>
    ): Int {
        var score = 50
        if (sleepData.isNotEmpty()) {
            val dur = sleepData.first().endTime.epochSecond - sleepData.first().startTime.epochSecond
            val hours = dur / 3600.0
            score += when {
                hours >= 8 -> 20
                hours >= 7 -> 15
                hours >= 6 -> 10
                else -> 0
            }
        }
        if (heartRateData.isNotEmpty()) {
            val avg = heartRateData.flatMap { it.samples }.map { it.beatsPerMinute }.average()
            score += when {
                avg < 60 -> 15
                avg < 70 -> 10
                avg < 80 -> 5
                else -> 0
            }
        }
        return score.coerceIn(0, 100)
    }

    private fun calculateSleepScore(sleepData: List<androidx.health.connect.client.records.SleepSessionRecord>): Int {
        if (sleepData.isEmpty()) return 50
        val dur = sleepData.first().endTime.epochSecond - sleepData.first().startTime.epochSecond
        val hours = dur / 3600.0
        return when {
            hours >= 8 -> 95
            hours >= 7.5 -> 85
            hours >= 7 -> 75
            hours >= 6.5 -> 65
            hours >= 6 -> 55
            else -> 45
        }
    }

    private fun calculateActivityScore(stepsData: List<androidx.health.connect.client.records.StepsRecord>): Int {
        if (stepsData.isEmpty()) return 50
        val steps = stepsData.sumOf { it.count }
        return when {
            steps >= 10000 -> 90
            steps >= 8000 -> 80
            steps >= 6000 -> 70
            steps >= 4000 -> 60
            steps >= 2000 -> 50
            else -> 40
        }
    }

    private fun calculateStressScore(heartRateData: List<androidx.health.connect.client.records.HeartRateRecord>): Int {
        if (heartRateData.isEmpty()) return 50
        val avg = heartRateData.flatMap { it.samples }.map { it.beatsPerMinute }.average()
        return when {
            avg < 60 -> 85
            avg < 70 -> 75
            avg < 80 -> 65
            avg < 90 -> 55
            else -> 45
        }
    }

    private fun calculateHeartRateScore(heartRateData: List<androidx.health.connect.client.records.HeartRateRecord>): Int {
        if (heartRateData.isEmpty()) return 50
        val avg = heartRateData.flatMap { it.samples }.map { it.beatsPerMinute }.average()
        return when {
            avg < 60 -> 95
            avg < 70 -> 85
            avg < 80 -> 75
            avg < 90 -> 65
            else -> 55
        }
    }
}
