package com.biosense.app.data.model

/**
 * Represents the user's biological sex or gender identity.
 * Essential for accurate BMR calculations and health benchmark comparisons.
 *
 * @property displayName User-friendly name for UI display.
 */
enum class Gender(val displayName: String) {
    MALE("Male"),
    FEMALE("Female"),
    OTHER("Other"),
    NOT_SPECIFIED("Prefer not to say")
}
