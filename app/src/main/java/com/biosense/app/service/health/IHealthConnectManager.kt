package com.biosense.app.service.health

import androidx.health.connect.client.records.*
import java.time.Instant

interface IHealthConnectManager {    suspend fun readSteps(startTime: Instant, endTime: Instant): List<StepsRecord>
    suspend fun readHeartRate(startTime: Instant, endTime: Instant): List<HeartRateRecord>
    suspend fun readActiveCaloriesBurned(startTime: Instant, endTime: Instant): List<ActiveCaloriesBurnedRecord>
    suspend fun readBloodGlucose(startTime: Instant, endTime: Instant): List<BloodGlucoseRecord>
    suspend fun readBloodPressure(startTime: Instant, endTime: Instant): List<BloodPressureRecord>
    suspend fun readBodyFat(startTime: Instant, endTime: Instant): List<BodyFatRecord>
    suspend fun readBodyTemperature(startTime: Instant, endTime: Instant): List<BodyTemperatureRecord>
    suspend fun readBodyWaterMass(startTime: Instant, endTime: Instant): List<BodyWaterMassRecord>
    suspend fun readHydration(startTime: Instant, endTime: Instant): List<HydrationRecord>
    suspend fun readNutrition(startTime: Instant, endTime: Instant): List<NutritionRecord>
    suspend fun readOxygenSaturation(startTime: Instant, endTime: Instant): List<OxygenSaturationRecord>
    suspend fun readRespiratoryRate(startTime: Instant, endTime: Instant): List<RespiratoryRateRecord>
    suspend fun readSleepSessions(startTime: Instant, endTime: Instant): List<SleepSessionRecord>
    suspend fun readTotalCaloriesBurned(startTime: Instant, endTime: Instant): List<TotalCaloriesBurnedRecord>
}
