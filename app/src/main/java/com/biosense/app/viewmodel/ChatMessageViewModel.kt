package com.biosense.app.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.biosense.app.health.FakeHealthConnectManager
import com.biosense.app.health.HealthConnectManager
import com.biosense.app.model.ChatMessage
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ChatMessageViewModel(application: Application) : AndroidViewModel(application) {

//  Uncomment the following line if using real data
//  private val healthConnectManager = HealthConnectManager.getInstance(application)
    private val healthConnectManager = FakeHealthConnectManager.getInstance()

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        // Add initial greeting message
        _messages.value = listOf(
            ChatMessage(
                text = "Hello! I'm your AI health assistant. How can I help you today?",
                isUser = false
            )
        )
    }

    fun sendMessage(text: String) {
        if (text.isBlank()) return

        // Add user message
        val userMessage = ChatMessage(text = text, isUser = true)
        _messages.value = _messages.value + userMessage

        // Simulate agent response
        _isLoading.value = true
        viewModelScope.launch {
            delay(1000) // Simulate network delay
            val agentMessage = ChatMessage(text = "Working", isUser = false)
            _messages.value = _messages.value + agentMessage
            _isLoading.value = false
        }
    }
}
