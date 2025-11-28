package com.biosense.app.data.model

/**
 * Represents the core user profile in Biosense.
 * Stores demographic data, health preferences, and persistent AI context.
 *
 * @property id Unique identifier for the user (local UUID).
 * @property name Display name of the user.
 * @property profilePicturePath Local file path to the user's profile image (if set).
 * @property gender Biological sex or gender identity for health baseline adjustments.
 * @property age Age in years, used for metabolic and activity calculations.
 * @property height Height in centimeters (cm).
 * @property weight Weight in kilograms (kg).
 * @property profession Used by AI to tailor advice (e.g., sedentary vs active jobs).
 * @property healthGoal The primary wellness objective guiding the AI's recommendations.
 * @property motivationStyle The preferred tone of voice for the AI assistant.
 * @property whatSenseKnows Persistent context memory for the AI (e.g., "Recovering from flu", "Knee injury").
 */
data class User(
    val id: String = "",
    val name: String = "",
    val profilePicturePath: String? = null,
    val gender: Gender = Gender.NOT_SPECIFIED,
    val age: Int = 0,
    val height: Int = 0,
    val weight: Int = 0,
    val profession: String = "",
    val healthGoal: HealthGoal = HealthGoal.PREVENTIVE_CARE,
    val motivationStyle: MotivationStyle = MotivationStyle.ENCOURAGEMENT,
    val whatSenseKnows: String = ""
)
