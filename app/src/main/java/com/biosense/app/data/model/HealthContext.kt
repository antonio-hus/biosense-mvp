package com.biosense.app.data.model

import androidx.health.connect.client.records.*
import java.time.Instant

data class HealthContext(
    val start: Instant,
    val end: Instant,
    val steps: List<StepsRecord>,
    val heartRate: List<HeartRateRecord>,
    val sleep: List<SleepSessionRecord>,
    val totalCalories: List<TotalCaloriesBurnedRecord>,
    val activeCalories: List<ActiveCaloriesBurnedRecord>,
    val bloodGlucose: List<BloodGlucoseRecord>,
    val bloodPressure: List<BloodPressureRecord>,
    val oxygenSaturation: List<OxygenSaturationRecord>
)