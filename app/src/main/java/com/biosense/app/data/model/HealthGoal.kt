package com.biosense.app.data.model

/**
 * Represents the user's primary wellness objective.
 * Guides the AI to prioritize specific metrics and advice categories.
 *
 * @property displayName User-friendly name for UI display.
 */
enum class HealthGoal(val displayName: String) {
    /** Focus on maintaining general health and identifying early warning signs. */
    PREVENTIVE_CARE("Preventive Care"),

    /** Prioritize sleep consistency, duration, and deep sleep metrics. */
    SLEEP_QUALITY("Sleep Quality"),

    /** Optimize vitality, reducing fatigue, and managing daily exertion. */
    ENERGY("Energy"),

    /** Focus on HRV, rest days, and body battery for post-exercise recovery. */
    RECOVERY("Recovery"),

    /** Balance nutrition and activity for weight loss or maintenance. */
    WEIGHT_MANAGEMENT("Weight Management"),

    /** Utilize breathing and mindfulness data to manage stress levels. */
    STRESS_RESILIENCE("Stress Resilience"),

    /** Push for performance gains, VO2 max, and strength/cardio progress. */
    FITNESS_IMPROVEMENT("Fitness Improvement")
}
