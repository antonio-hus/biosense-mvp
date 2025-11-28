package com.biosense.app.viewmodel

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State
import androidx.lifecycle.AndroidViewModel
import com.biosense.app.data.model.*

class UserViewModel(application: Application) : AndroidViewModel(application) {

    private val _currentUser = mutableStateOf(User())
    val currentUser: State<User> = _currentUser

    private val _isUserCreated = mutableStateOf(false)
    val isUserCreated: State<Boolean> = _isUserCreated

    private val sharedPreferences: SharedPreferences = application.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "biosense_user_prefs"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_USER_AGE = "user_age"
        private const val KEY_USER_HEIGHT = "user_height"
        private const val KEY_USER_WEIGHT = "user_weight"
        private const val KEY_USER_PROFESSION = "user_profession"
        private const val KEY_USER_GENDER = "user_gender"
        private const val KEY_USER_HEALTH_GOAL = "user_health_goal"
        private const val KEY_USER_MOTIVATION_STYLE = "user_motivation_style"
        private const val KEY_USER_WHAT_SENSE_KNOWS = "user_what_sense_knows"
        private const val KEY_USER_PROFILE_PICTURE = "user_profile_picture"
        private const val KEY_IS_USER_CREATED = "is_user_created"
    }

    init {
        loadUserFromPreferences()
    }

    fun createUser(user: User) {
        _currentUser.value = user.copy(id = "user_${System.currentTimeMillis()}")
        _isUserCreated.value = true
        saveUserToPreferences()
    }

    fun updateUser(user: User) {
        _currentUser.value = user
        saveUserToPreferences()
    }


    fun updateName(name: String) { _currentUser.value = _currentUser.value.copy(name = name); saveUserToPreferences() }
    fun updateAge(age: Int) { _currentUser.value = _currentUser.value.copy(age = age); saveUserToPreferences() }
    fun updateGender(gender: Gender) { _currentUser.value = _currentUser.value.copy(gender = gender); saveUserToPreferences() }
    fun updateHeight(height: Int) { _currentUser.value = _currentUser.value.copy(height = height); saveUserToPreferences() }
    fun updateWeight(weight: Int) { _currentUser.value = _currentUser.value.copy(weight = weight); saveUserToPreferences() }
    fun updateProfession(profession: String) { _currentUser.value = _currentUser.value.copy(profession = profession); saveUserToPreferences() }
    fun updateHealthGoal(goal: HealthGoal) { _currentUser.value = _currentUser.value.copy(healthGoal = goal); saveUserToPreferences() }
    fun updateMotivationStyle(style: MotivationStyle) { _currentUser.value = _currentUser.value.copy(motivationStyle = style); saveUserToPreferences() }
    fun updateProfilePicture(path: String?) { _currentUser.value = _currentUser.value.copy(profilePicturePath = path); saveUserToPreferences() }

    fun updateWhatSenseKnows(info: String) {
        _currentUser.value = _currentUser.value.copy(whatSenseKnows = info)
        saveUserToPreferences()
    }

    fun overwriteUserContext(newContext: String) {
        updateWhatSenseKnows(newContext)
    }

    private fun saveUserToPreferences() {
        sharedPreferences.edit().apply {
            val user = _currentUser.value
            putString(KEY_USER_ID, user.id)
            putString(KEY_USER_NAME, user.name)
            putInt(KEY_USER_AGE, user.age)
            putInt(KEY_USER_HEIGHT, user.height)
            putInt(KEY_USER_WEIGHT, user.weight)
            putString(KEY_USER_PROFESSION, user.profession)
            putString(KEY_USER_GENDER, user.gender.name)
            putString(KEY_USER_HEALTH_GOAL, user.healthGoal.name)
            putString(KEY_USER_MOTIVATION_STYLE, user.motivationStyle.name)
            putString(KEY_USER_WHAT_SENSE_KNOWS, user.whatSenseKnows)
            putString(KEY_USER_PROFILE_PICTURE, user.profilePicturePath)
            putBoolean(KEY_IS_USER_CREATED, _isUserCreated.value)
            apply()
        }
    }

    private fun loadUserFromPreferences() {
        val userId = sharedPreferences.getString(KEY_USER_ID, "") ?: ""
        if (userId.isNotEmpty()) {
            val user = User(
                id = userId,
                name = sharedPreferences.getString(KEY_USER_NAME, "") ?: "",
                age = sharedPreferences.getInt(KEY_USER_AGE, 0),
                height = sharedPreferences.getInt(KEY_USER_HEIGHT, 0),
                weight = sharedPreferences.getInt(KEY_USER_WEIGHT, 0),
                profession = sharedPreferences.getString(KEY_USER_PROFESSION, "") ?: "",
                gender = try { Gender.valueOf(sharedPreferences.getString(KEY_USER_GENDER, Gender.NOT_SPECIFIED.name)!!) } catch (e: Exception) { Gender.NOT_SPECIFIED },
                healthGoal = try { HealthGoal.valueOf(sharedPreferences.getString(KEY_USER_HEALTH_GOAL, HealthGoal.ENERGY.name)!!) } catch (e: Exception) { HealthGoal.ENERGY },
                motivationStyle = try { MotivationStyle.valueOf(sharedPreferences.getString(KEY_USER_MOTIVATION_STYLE, MotivationStyle.ENCOURAGEMENT.name)!!) } catch (e: Exception) { MotivationStyle.ENCOURAGEMENT },
                whatSenseKnows = sharedPreferences.getString(KEY_USER_WHAT_SENSE_KNOWS, "") ?: "",
                profilePicturePath = sharedPreferences.getString(KEY_USER_PROFILE_PICTURE, null)
            )

            _currentUser.value = user
            _isUserCreated.value = sharedPreferences.getBoolean(KEY_IS_USER_CREATED, false)
        }
    }
}
