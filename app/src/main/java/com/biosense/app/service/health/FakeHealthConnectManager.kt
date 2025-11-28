package com.biosense.app.service.health

import androidx.health.connect.client.records.*
import androidx.health.connect.client.records.metadata.Metadata
import androidx.health.connect.client.units.*
import com.biosense.app.data.model.HealthContext
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import java.time.Instant
import java.time.ZoneOffset
import kotlin.random.Random

// The fake provider also implements the interface
class FakeHealthConnectManager private constructor() : IHealthConnectManager {

    // All methods are 'suspend' to match the real manager,
    // so we add a small delay to simulate a real data fetch.
    private suspend fun simulateDelay() {
        delay(200) // 200 milliseconds
    }

    override suspend fun getHealthContext(startTime: Instant, endTime: Instant): HealthContext = coroutineScope {
        // Fetch all data in parallel for efficiency
        val steps = async { readSteps(startTime, endTime) }
        val heartRate = async { readHeartRate(startTime, endTime) }
        val sleep = async { readSleepSessions(startTime, endTime) }
        val totalCals = async { readTotalCaloriesBurned(startTime, endTime) }
        val activeCals = async { readActiveCaloriesBurned(startTime, endTime) }
        val glucose = async { readBloodGlucose(startTime, endTime) }
        val bp = async { readBloodPressure(startTime, endTime) }
        val oxygen = async { readOxygenSaturation(startTime, endTime) }

        HealthContext(
            start = startTime,
            end = endTime,
            steps = steps.await(),
            heartRate = heartRate.await(),
            sleep = sleep.await(),
            totalCalories = totalCals.await(),
            activeCalories = activeCals.await(),
            bloodGlucose = glucose.await(),
            bloodPressure = bp.await(),
            oxygenSaturation = oxygen.await()
        )
    }

    override suspend fun readSteps(startTime: Instant, endTime: Instant): List<StepsRecord> {
        simulateDelay()
        return listOf(
            StepsRecord(
                count = Random.nextLong(6500, 8500),
                startTime = startTime,
                endTime = endTime,
                startZoneOffset = ZoneOffset.UTC,
                endZoneOffset = ZoneOffset.UTC,
                metadata = Metadata()
            )
        )
    }

    override suspend fun readHeartRate(startTime: Instant, endTime: Instant): List<HeartRateRecord> {
        simulateDelay()
        val samples = mutableListOf<HeartRateRecord.Sample>()
        var currentSampleTime = startTime
        while (currentSampleTime.isBefore(endTime)) {
            samples.add(
                HeartRateRecord.Sample(
                    time = currentSampleTime,
                    beatsPerMinute = Random.nextLong(60, 95)
                )
            )
            currentSampleTime = currentSampleTime.plusSeconds(300) // sample every 5 minutes
        }
        return listOf(
            HeartRateRecord(
                startTime = startTime,
                endTime = endTime,
                startZoneOffset = ZoneOffset.UTC,
                endZoneOffset = ZoneOffset.UTC,
                samples = samples,
                metadata = Metadata()
            )
        )
    }

    override suspend fun readActiveCaloriesBurned(startTime: Instant, endTime: Instant): List<ActiveCaloriesBurnedRecord> {
        simulateDelay()
        return listOf(
            ActiveCaloriesBurnedRecord(
                energy = Energy.kilocalories(Random.nextDouble(300.0, 550.0)),
                startTime = startTime,
                endTime = endTime,
                startZoneOffset = ZoneOffset.UTC,
                endZoneOffset = ZoneOffset.UTC
            )
        )
    }

    override suspend fun readBloodGlucose(startTime: Instant, endTime: Instant): List<BloodGlucoseRecord> {
        simulateDelay()
        return listOf(
            BloodGlucoseRecord(
                level = BloodGlucose.millimolesPerLiter(Random.nextDouble(4.0, 6.0)),
                specimenSource = BloodGlucoseRecord.SPECIMEN_SOURCE_CAPILLARY_BLOOD,
                time = startTime,
                zoneOffset = ZoneOffset.UTC
            )
        )
    }


    override suspend fun readBloodPressure(startTime: Instant, endTime: Instant): List<BloodPressureRecord> {
        simulateDelay()
        return listOf(
            BloodPressureRecord(
                systolic = Pressure.millimetersOfMercury(Random.nextDouble(115.0, 125.0).toFloat().toDouble()),
                diastolic = Pressure.millimetersOfMercury(Random.nextDouble(75.0, 85.0).toFloat().toDouble()),
                bodyPosition = BloodPressureRecord.BODY_POSITION_SITTING_DOWN,
                measurementLocation = BloodPressureRecord.MEASUREMENT_LOCATION_LEFT_UPPER_ARM,
                time = startTime,
                zoneOffset = ZoneOffset.UTC
            )
        )
    }

    override suspend fun readBodyFat(startTime: Instant, endTime: Instant): List<BodyFatRecord> {
        simulateDelay()
        return listOf(
            BodyFatRecord(
                percentage = Percentage(Random.nextDouble(18.0, 25.0).toFloat().toDouble()),
                time = startTime,
                zoneOffset = ZoneOffset.UTC
            )
        )
    }

    override suspend fun readBodyTemperature(startTime: Instant, endTime: Instant): List<BodyTemperatureRecord> {
        simulateDelay()
        return listOf(
            BodyTemperatureRecord(
                temperature = Temperature.celsius(Random.nextDouble(36.5, 37.2).toFloat().toDouble()),
                time = startTime,
                zoneOffset = ZoneOffset.UTC
            )
        )
    }

    override suspend fun readBodyWaterMass(startTime: Instant, endTime: Instant): List<BodyWaterMassRecord> {
        simulateDelay()
        return listOf(
            BodyWaterMassRecord(
                mass = Mass.kilograms(Random.nextDouble(40.0, 45.0)),
                time = startTime,
                zoneOffset = ZoneOffset.UTC
            )
        )
    }

    override suspend fun readHydration(startTime: Instant, endTime: Instant): List<HydrationRecord> {
        simulateDelay()
        return listOf(
            HydrationRecord(
                volume = Volume.liters(Random.nextDouble(1.8, 2.5)),
                startTime = startTime,
                endTime = endTime,
                startZoneOffset = ZoneOffset.UTC,
                endZoneOffset = ZoneOffset.UTC
            )
        )
    }

    override suspend fun readNutrition(startTime: Instant, endTime: Instant): List<NutritionRecord> {
        simulateDelay()
        return listOf(
            NutritionRecord(
                energy = Energy.kilocalories(Random.nextDouble(2000.0, 2500.0)),
                protein = Mass.grams(Random.nextDouble(80.0, 120.0)),
                totalCarbohydrate = Mass.grams(Random.nextDouble(250.0, 350.0)),
                totalFat = Mass.grams(Random.nextDouble(60.0, 90.0)),
                startTime = startTime,
                endTime = endTime,
                startZoneOffset = ZoneOffset.UTC,
                endZoneOffset = ZoneOffset.UTC
            )
        )
    }

    override suspend fun readOxygenSaturation(startTime: Instant, endTime: Instant): List<OxygenSaturationRecord> {
        simulateDelay()
        return listOf(
            OxygenSaturationRecord(
                percentage = Percentage(Random.nextDouble(97.0, 99.5).toFloat().toDouble()),
                time = startTime,
                zoneOffset = ZoneOffset.UTC
            )
        )
    }

    override suspend fun readRespiratoryRate(startTime: Instant, endTime: Instant): List<RespiratoryRateRecord> {
        simulateDelay()
        return listOf(
            RespiratoryRateRecord(
                rate = Random.nextDouble(12.0, 18.0),
                time = startTime,
                zoneOffset = ZoneOffset.UTC
            )
        )
    }

    override suspend fun readSleepSessions(startTime: Instant, endTime: Instant): List<SleepSessionRecord> {
        simulateDelay()
        val sessionStart = startTime.minusSeconds(Random.nextLong(3600 * 8)) // Sleep started up to 8 hours ago
        val sessionEnd = sessionStart.plusSeconds(Random.nextLong(3600 * 7, 3600 * 9)) // Slept 7-9 hours
        return listOf(
            SleepSessionRecord(
                startTime = sessionStart,
                startZoneOffset = ZoneOffset.UTC,
                endTime = sessionEnd,
                endZoneOffset = ZoneOffset.UTC,
                title = "Nightly Sleep"
            )
        )
    }

    override suspend fun readTotalCaloriesBurned(startTime: Instant, endTime: Instant): List<TotalCaloriesBurnedRecord> {
        simulateDelay()
        return listOf(
            TotalCaloriesBurnedRecord(
                energy = Energy.kilocalories(Random.nextDouble(2200.0, 2800.0)),
                startTime = startTime,
                endTime = endTime,
                startZoneOffset = ZoneOffset.UTC,
                endZoneOffset = ZoneOffset.UTC
            )
        )
    }

    // --- Singleton Pattern ---
    companion object {
        @Volatile
        private var INSTANCE: IHealthConnectManager? = null

        fun getInstance(): IHealthConnectManager {
            return INSTANCE ?: synchronized(this) {
                val instance = FakeHealthConnectManager()
                INSTANCE = instance
                instance
            }
        }
    }
}
