package com.biosense.app.model

data class User(
    val id: String = "",
    val name: String = "",
    val profilePicturePath: String? = null,
    val gender: Gender = Gender.NOT_SPECIFIED,
    val age: Int = 0,
    val height: Int = 0,
    val weight: Int = 0,
    val profession: String = "",
    val healthGoal: HealthGoal = HealthGoal.ENERGY,
    val motivationStyle: MotivationStyle = MotivationStyle.ENCOURAGEMENT,
    val whatSenseKnows: String = ""
)

enum class Gender(val displayName: String){
    MALE("Male"),
    FEMALE("Female"),
    OTHER("Other"),
    NOT_SPECIFIED("Prefer not to say")
}

enum class MotivationStyle(val displayName: String){
    CASUAL("Casual"),
    ENCOURAGEMENT("Encouragement"),
    DIRECT("Direct"),
    SCIENTIFIC("Scientific")
}

enum class HealthGoal(val displayName: String){
    PREVENTIVE_CARE("Preventive Care"),
    SLEEP_QUALITY("Sleep Quality"),
    ENERGY("Energy"),
    RECOVERY("Recovery"),
    WEIGHT_MANAGEMENT("Weight Management"),
    STRESS_RESILIENCE("Stress Resilience"),
    FITNESS_IMPROVEMENT("Fitness Improvement")
}