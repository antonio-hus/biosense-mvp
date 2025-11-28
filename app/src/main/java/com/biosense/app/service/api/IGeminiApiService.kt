package com.biosense.app.service.api

/**
 * Abstraction for the Generative AI service.
 * Allows switching between real Gemini API and Mock implementations for testing.
 */
interface IGeminiApiService {
    /**
     * Sends a text prompt to the AI model and returns the generated response string.
     *
     * @param prompt The full context string (including user query and system instructions).
     * @return The AI's text response.
     */
    suspend fun generateResponse(prompt: String): String
}
