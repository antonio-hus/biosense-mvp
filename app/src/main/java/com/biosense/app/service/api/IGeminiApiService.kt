package com.biosense.app.service.api

interface IGeminiApiService {
    suspend fun generateResponse(prompt: String): String
}

