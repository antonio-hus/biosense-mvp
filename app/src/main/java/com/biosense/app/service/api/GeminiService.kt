package com.biosense.app.service.api

import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Production implementation of the AI service using Google's Gemini API.
 * Handles network requests on the IO dispatcher.
 *
 * @param apiKey The Google AI Studio API key.
 */
class GeminiService(private val apiKey: String) : IGeminiApiService {

    // "gemini-2.5-flash" is optimized for speed and cost, ideal for chat interactions.
    private val generativeModel = GenerativeModel(
        modelName = "gemini-2.5-flash",
        apiKey = apiKey
    )

    override suspend fun generateResponse(prompt: String): String = withContext(Dispatchers.IO) {
        try {
            // Generates a response from the model
            val response = generativeModel.generateContent(prompt)

            // Return the text, or a fallback if null
            response.text ?: "I'm sorry, I couldn't generate a response at this time."

        } catch (e: Exception) {
            // Log the error in a real app
            e.printStackTrace()
            // Return a user-friendly error string instead of crashing
            "Error: Unable to connect to AI service. (${e.localizedMessage})"
        }
    }
}
