package com.biosense.app.service.api

import kotlinx.coroutines.delay

class MockGeminiService : IGeminiApiService {
    override suspend fun generateResponse(prompt: String): String {
        delay(1500)
        return "I understand you're asking about \"$prompt\". As your Biosense AI health assistant, I can help analyze your wellness data. (This is a mocked response simulating the Gemini API)."
    }
}