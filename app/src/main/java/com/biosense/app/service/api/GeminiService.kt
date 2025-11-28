package com.biosense.app.service.api

import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GeminiService(private val apiKey: String) : IGeminiApiService {

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
            "Error: Unable to connect to AI service. (${e.localizedMessage})"
        }
    }
}
