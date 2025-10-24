package com.biosense.app.health

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.activity.result.contract.ActivityResultContract
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.*
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import java.time.Instant
import kotlin.reflect.KClass

class HealthConnectManager private constructor(context: Context) : IHealthConnectManager {

    private val healthConnectClient: HealthConnectClient = HealthConnectClient.getOrCreate(context)

    fun getSdkStatus(context: Context): Int {
        return HealthConnectClient.getSdkStatus(context, "com.google.android.apps.healthdata")
    }

    fun getInstallHealthConnectIntent(context: Context): Intent {
        // This is a more robust way to create the install intent
        val providerPackageName = "com.google.android.apps.healthdata"
        val uriString = "market://details?id=$providerPackageName&url=https://play.google.com/store/apps/details?id=$providerPackageName"
        return Intent(Intent.ACTION_VIEW).apply {
            setPackage("com.android.vending")
            data = Uri.parse(uriString)
            putExtra("overlay", true)
            putExtra("callerId", context.packageName)
        }
    }

    val requestPermissionActivityContract: ActivityResultContract<Set<String>, Set<String>>
        get() = PermissionController.createRequestPermissionResultContract()

    suspend fun hasAllPermissions(permissions: Set<String>): Boolean {
        return healthConnectClient.permissionController.getGrantedPermissions().containsAll(permissions)
    }

    override suspend fun readSteps(startTime: Instant, endTime: Instant): List<StepsRecord> {
        return readRecords(StepsRecord::class, startTime, endTime)
    }

    override suspend fun readHeartRate(startTime: Instant, endTime: Instant): List<HeartRateRecord> {
        return readRecords(HeartRateRecord::class, startTime, endTime)
    }

    override suspend fun readActiveCaloriesBurned(startTime: Instant, endTime: Instant): List<ActiveCaloriesBurnedRecord> {
        return readRecords(ActiveCaloriesBurnedRecord::class, startTime, endTime)
    }

    override suspend fun readBloodGlucose(startTime: Instant, endTime: Instant): List<BloodGlucoseRecord> {
        return readRecords(BloodGlucoseRecord::class, startTime, endTime)
    }

    override suspend fun readBloodPressure(startTime: Instant, endTime: Instant): List<BloodPressureRecord> {
        return readRecords(BloodPressureRecord::class, startTime, endTime)
    }

    override suspend fun readBodyFat(startTime: Instant, endTime: Instant): List<BodyFatRecord> {
        return readRecords(BodyFatRecord::class, startTime, endTime)
    }

    override suspend fun readBodyTemperature(startTime: Instant, endTime: Instant): List<BodyTemperatureRecord> {
        return readRecords(BodyTemperatureRecord::class, startTime, endTime)
    }

    override suspend fun readBodyWaterMass(startTime: Instant, endTime: Instant): List<BodyWaterMassRecord> {
        return readRecords(BodyWaterMassRecord::class, startTime, endTime)
    }

    override suspend fun readHydration(startTime: Instant, endTime: Instant): List<HydrationRecord> {
        return readRecords(HydrationRecord::class, startTime, endTime)
    }

    override suspend fun readNutrition(startTime: Instant, endTime: Instant): List<NutritionRecord> {
        return readRecords(NutritionRecord::class, startTime, endTime)
    }

    override suspend fun readOxygenSaturation(startTime: Instant, endTime: Instant): List<OxygenSaturationRecord> {
        return readRecords(OxygenSaturationRecord::class, startTime, endTime)
    }

    override suspend fun readRespiratoryRate(startTime: Instant, endTime: Instant): List<RespiratoryRateRecord> {
        return readRecords(RespiratoryRateRecord::class, startTime, endTime)
    }

    override suspend fun readSleepSessions(startTime: Instant, endTime: Instant): List<SleepSessionRecord> {
        return readRecords(SleepSessionRecord::class, startTime, endTime)
    }

    override suspend fun readTotalCaloriesBurned(startTime: Instant, endTime: Instant): List<TotalCaloriesBurnedRecord> {
        return readRecords(TotalCaloriesBurnedRecord::class, startTime, endTime)
    }

    private suspend fun <T : Record> readRecords(recordType: KClass<T>, startTime: Instant, endTime: Instant): List<T> {
        return try {
            val request = ReadRecordsRequest(
                recordType = recordType,
                timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
            )
            healthConnectClient.readRecords(request).records
        } catch (e: Exception) {
            Log.e("HealthConnectManager", "Error reading ${recordType.simpleName}", e)
            emptyList()
        }
    }

    companion object {
        // @Volatile ensures the instance is always up-to-date across all threads
        @Volatile
        private var INSTANCE: HealthConnectManager? = null

        /**
         * Gets the single instance of HealthConnectManager, creating it if it doesn't exist.
         * This method is thread-safe.
         */
        fun getInstance(context: Context): HealthConnectManager {
            val appContext = context.applicationContext
            return INSTANCE ?: synchronized(this) {
                val instance = HealthConnectManager(appContext)
                INSTANCE = instance
                instance
            }
        }

        val PERMISSIONS =
            setOf(
                HealthPermission.getReadPermission(StepsRecord::class),
                HealthPermission.getReadPermission(HeartRateRecord::class),
                HealthPermission.getReadPermission(ActiveCaloriesBurnedRecord::class),
                HealthPermission.getReadPermission(BloodGlucoseRecord::class),
                HealthPermission.getReadPermission(BloodPressureRecord::class),
                HealthPermission.getReadPermission(BodyFatRecord::class),
                HealthPermission.getReadPermission(BodyTemperatureRecord::class),
                HealthPermission.getReadPermission(BodyWaterMassRecord::class),
                HealthPermission.getReadPermission(HydrationRecord::class),
                HealthPermission.getReadPermission(NutritionRecord::class),
                HealthPermission.getReadPermission(OxygenSaturationRecord::class),
                HealthPermission.getReadPermission(RespiratoryRateRecord::class),
                HealthPermission.getReadPermission(SleepSessionRecord::class),
                HealthPermission.getReadPermission(TotalCaloriesBurnedRecord::class),
            )
    }
}
