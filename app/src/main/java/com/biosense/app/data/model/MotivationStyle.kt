package com.biosense.app.data.model

/**
 * Defines the personality and communication tone of the AI health advisor.
 * Used to tailor responses to the user's psychological preference.
 *
 * @property displayName User-friendly name for UI display.
 */
enum class MotivationStyle(val displayName: String) {
    /** Relaxed, conversational, and friendly tone. */
    CASUAL("Casual"),

    /** Warm, supportive, focusing on positive reinforcement and celebrating wins. */
    ENCOURAGEMENT("Encouragement"),

    /** Concise, no-nonsense, and action-oriented advice ("Tough Love"). */
    DIRECT("Direct"),

    /** Data-heavy, analytical, focusing on the "why" and physiological mechanisms. */
    SCIENTIFIC("Scientific")
}
