package com.biosense.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.biosense.app.data.BiosenseDatabase
import com.biosense.app.data.entity.ChatMessageEntity
import com.biosense.app.data.entity.ChatSessionEntity
import com.biosense.app.data.repository.ChatRepository
import com.biosense.app.service.api.MockGeminiService
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

class ChatViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ChatRepository

    init {
        val database = BiosenseDatabase.getDatabase(application)
        val apiService = MockGeminiService()
        repository = ChatRepository(database.chatDao(), apiService)
    }

    val sessions: StateFlow<List<ChatSessionEntity>> = repository.allSessions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _currentSessionId = MutableStateFlow<String?>(null)
    val currentSessionId: StateFlow<String?> = _currentSessionId.asStateFlow()

    private val _isTemporarySession = MutableStateFlow(false)

    @OptIn(ExperimentalCoroutinesApi::class)
    val messages: StateFlow<List<ChatMessageEntity>> = combine(
        _currentSessionId,
        _isTemporarySession
    ) { sessionId, isTemp ->
        Pair(sessionId, isTemp)
    }.flatMapLatest { (sessionId, isTemp) ->
        if (sessionId == null) {
            flowOf(emptyList())
        } else if (isTemp) {
            flowOf(emptyList())
        } else {
            repository.getMessages(sessionId)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun initializeSession() {
        if (_currentSessionId.value == null) {
            prepareNewTempSession()
        }
    }

    fun prepareNewTempSession() {
        val tempId = UUID.randomUUID().toString()
        _isTemporarySession.value = true
        _currentSessionId.value = tempId
    }

    fun selectSession(sessionId: String) {
        _isTemporarySession.value = false
        _currentSessionId.value = sessionId
    }

    fun sendMessage(text: String) {
        val sessionId = _currentSessionId.value ?: return
        if (text.isBlank()) return

        viewModelScope.launch {
            _isLoading.value = true

            val isFirst = _isTemporarySession.value

            repository.sendMessage(sessionId, text, isFirstMessage = isFirst)

            if (isFirst) {
                _isTemporarySession.value = false
            }

            _isLoading.value = false
        }
    }

    fun deleteSession(sessionId: String) {
        viewModelScope.launch {
            repository.deleteSession(sessionId)
            if (_currentSessionId.value == sessionId) {
                prepareNewTempSession()
            }
        }
    }

    fun renameSession(sessionId: String, newTitle: String) {
        viewModelScope.launch {
            repository.renameSession(sessionId, newTitle)
        }
    }
}
