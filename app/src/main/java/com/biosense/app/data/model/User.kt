package com.biosense.app.data.model

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