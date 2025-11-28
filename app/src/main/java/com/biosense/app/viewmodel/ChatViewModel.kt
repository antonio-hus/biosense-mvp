package com.biosense.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.biosense.app.data.BiosenseDatabase
import com.biosense.app.service.api.MockGeminiService
import com.biosense.app.repository.ChatRepository
import com.biosense.app.data.entity.ChatMessageEntity
import com.biosense.app.data.entity.ChatSessionEntity
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ChatViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ChatRepository

    init {
        val database = BiosenseDatabase.getDatabase(application)
        val apiService = MockGeminiService()
        repository = ChatRepository(database.chatDao(), apiService)
    }

    // Session State
    val sessions: StateFlow<List<ChatSessionEntity>> = repository.allSessions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _currentSessionId = MutableStateFlow<String?>(null)
    val currentSessionId: StateFlow<String?> = _currentSessionId.asStateFlow()

    // Message State (Reactive to currentSessionId)
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val messages: StateFlow<List<ChatMessageEntity>> = _currentSessionId
        .flatMapLatest { sessionId ->
            if (sessionId == null) flowOf(emptyList())
            else repository.getMessages(sessionId)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun createNewSession() {
        viewModelScope.launch {
            val count = sessions.value.size + 1
            val newId = repository.createSession("Conversation #$count")
            _currentSessionId.value = newId
        }
    }

    fun selectSession(sessionId: String) {
        _currentSessionId.value = sessionId
    }

    fun sendMessage(text: String) {
        val sessionId = _currentSessionId.value ?: return
        if (text.isBlank()) return

        viewModelScope.launch {
            _isLoading.value = true
            repository.sendMessage(sessionId, text)
            _isLoading.value = false
        }
    }

    // Initialize with a session if none exist
    fun ensureSessionExists() {
        if (_currentSessionId.value == null) {
            // We can't synchronously check sessions.value here reliably on init without logic
            // This is handled better by UI checking if list is empty
        }
    }
}
