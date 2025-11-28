package com.biosense.app.service.api

import kotlinx.coroutines.delay

/**
 * A fake implementation of the AI service for testing and offline development.
 * Simulates network latency and returns a static placeholder response.
 */
class MockGeminiService : IGeminiApiService {
    override suspend fun generateResponse(prompt: String): String {
        // Simulate 1.5s network delay to test UI loading states
        delay(1500)
        return "I understand you're asking about \"$prompt\". As your Biosense AI health assistant, I can help analyze your wellness data. (This is a mocked response simulating the Gemini API)."
    }
}
