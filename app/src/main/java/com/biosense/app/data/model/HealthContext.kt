package com.biosense.app.data.model

import androidx.health.connect.client.records.*
import java.time.Instant

/**
 * Aggregated snapshot of a user's health data over a specific time range.
 * Used to serialize data into TOON format for AI analysis.
 *
 * @property start Start timestamp of the data window.
 * @property end End timestamp of the data window.
 * @property steps List of step count records.
 * @property heartRate List of heart rate samples and series.
 * @property sleep List of sleep sessions including stages if available.
 * @property totalCalories Total energy expenditure records (BMR + Active).
 * @property activeCalories Active energy expenditure records (Activity only).
 * @property bloodGlucose Blood sugar measurements.
 * @property bloodPressure Systolic and diastolic pressure readings.
 * @property oxygenSaturation SpO2 percentage readings.
 */
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
