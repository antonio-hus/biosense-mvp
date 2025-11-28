package com.biosense.app.data.serializer

import com.biosense.app.data.model.HealthContext
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializer
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Handles the conversion of [HealthContext] data into string formats suitable for AI consumption.
 * Supports standard JSON for logging/debugging and TOON (Token-Oriented Object Notation) for efficient AI prompting.
 */
class HealthContextSerializer {

    // Date formatters for human-readable time output in TOON
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.US)
        .withZone(ZoneId.systemDefault())

    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm", Locale.US)
        .withZone(ZoneId.systemDefault())

    // Gson instance configured to handle Java 8 Instant types
    private val gson: Gson = GsonBuilder()
        .registerTypeAdapter(Instant::class.java, JsonSerializer<Instant> { src, _, _ ->
            JsonPrimitive(src.toString()) // Serialize Instant as ISO-8601 String
        })
        .setPrettyPrinting()
        .create()

    /**
     * Serializes health context to standard JSON format.
     * Useful for debugging or exporting data.
     */
    fun toJson(context: HealthContext): String {
        return gson.toJson(context)
    }

    /**
     * Serializes health context to TOON (Token-Oriented Object Notation).
     * This format is highly optimized for Large Language Models (LLMs), using 40-60% fewer tokens than JSON
     * by removing repetitive keys and brackets in favor of a Markdown-table-like structure.
     */
    fun toToon(context: HealthContext): String {
        val sb = StringBuilder()

        sb.appendLine("Context:")
        sb.appendLine("  Date: ${dateFormatter.format(context.start)}")
        sb.appendLine("  Range: ${timeFormatter.format(context.start)} to ${timeFormatter.format(context.end)}")

        // Steps
        if (context.steps.isNotEmpty()) {
            sb.appendLine("Steps:")
            sb.appendLine("  | Time  | Count")
            context.steps.forEach { record ->
                sb.appendLine("  | ${timeFormatter.format(record.startTime)} | ${record.count}")
            }
        }

        // Heart Rate
        if (context.heartRate.isNotEmpty()) {
            sb.appendLine("HeartRate:")
            sb.appendLine("  | Time  | BPM")
            context.heartRate.flatMap { it.samples }.forEach { sample ->
                sb.appendLine("  | ${timeFormatter.format(sample.time)} | ${sample.beatsPerMinute}")
            }
        }

        // Sleep
        if (context.sleep.isNotEmpty()) {
            sb.appendLine("Sleep:")
            sb.appendLine("  | Start | End   | Duration(h)")
            context.sleep.forEach { session ->
                val durationMinutes = java.time.Duration.between(session.startTime, session.endTime).toMinutes()
                val durationHours = durationMinutes / 60.0
                sb.appendLine("  | ${timeFormatter.format(session.startTime)} | ${timeFormatter.format(session.endTime)} | ${String.format(Locale.US, "%.1f", durationHours)}")
            }
        }

        // Blood Pressure
        if (context.bloodPressure.isNotEmpty()) {
            sb.appendLine("BloodPressure:")
            sb.appendLine("  | Time  | Sys/Dia")
            context.bloodPressure.forEach { record ->
                sb.appendLine("  | ${timeFormatter.format(record.time)} | ${record.systolic.inMillimetersOfMercury.toInt()}/${record.diastolic.inMillimetersOfMercury.toInt()}")
            }
        }

        // Blood Glucose
        if (context.bloodGlucose.isNotEmpty()) {
            sb.appendLine("BloodGlucose:")
            sb.appendLine("  | Time  | Level(mmol/L)")
            context.bloodGlucose.forEach { record ->
                sb.appendLine("  | ${timeFormatter.format(record.time)} | ${String.format(Locale.US, "%.1f", record.level.inMillimolesPerLiter)}")
            }
        }

        // Active Calories
        if (context.activeCalories.isNotEmpty()) {
            sb.appendLine("ActiveCalories:")
            sb.appendLine("  | Time  | Kcal")
            context.activeCalories.forEach { record ->
                sb.appendLine("  | ${timeFormatter.format(record.startTime)} | ${record.energy.inKilocalories.toInt()}")
            }
        }

        // Total Calories
        if (context.totalCalories.isNotEmpty()) {
            sb.appendLine("TotalCalories:")
            sb.appendLine("  | Time  | Kcal")
            context.totalCalories.forEach { record ->
                sb.appendLine("  | ${timeFormatter.format(record.startTime)} | ${record.energy.inKilocalories.toInt()}")
            }
        }

        // Oxygen Saturation
        if (context.oxygenSaturation.isNotEmpty()) {
            sb.appendLine("OxygenSaturation:")
            sb.appendLine("  | Time  | %")
            context.oxygenSaturation.forEach { record ->
                sb.appendLine("  | ${timeFormatter.format(record.time)} | ${record.percentage.value.toInt()}")
            }
        }

        return sb.toString()
    }
}
