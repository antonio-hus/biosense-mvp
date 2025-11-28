package com.biosense.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.biosense.app.data.BiosenseDatabase
import com.biosense.app.data.entity.ChatMessageEntity
import com.biosense.app.data.entity.ChatSessionEntity
import com.biosense.app.repository.ChatRepository
import com.biosense.app.data.serializer.HealthContextSerializer
import com.biosense.app.service.api.MockGeminiService
import com.biosense.app.service.health.FakeHealthConnectManager
import com.biosense.app.service.health.IHealthConnectManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID

class ChatViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ChatRepository
    private val healthConnectManager: IHealthConnectManager
    private val serializer: HealthContextSerializer

    init {
        // Initialize dependencies (In a real app, use Hilt for injection)
        val database = BiosenseDatabase.getDatabase(application)

        // Using Fake manager for now as per your setup
        healthConnectManager = FakeHealthConnectManager.getInstance()

        // Initialize API service and Repository
        val apiService = MockGeminiService()
        repository = ChatRepository(database.chatDao(), apiService)

        // Initialize Serializer
        serializer = HealthContextSerializer()
    }

    val sessions: StateFlow<List<ChatSessionEntity>> = repository.allSessions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _currentSessionId = MutableStateFlow<String?>(null)
    val currentSessionId: StateFlow<String?> = _currentSessionId.asStateFlow()

    // Track if the session is temporary (unsaved)
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

            // Fetch Health Context (Last 72h)
            val now = Instant.now()
            val startTime = now.minus(3, ChronoUnit.DAYS)
            val healthContext = healthConnectManager.getHealthContext(startTime, now)
            val contextString = serializer.toToon(healthContext)

            // Fetch Chat History (Last 10 messages)
            // If it's a new temp session, history is obviously empty.
            val historyString = if (!isFirst) {
                val recentMessages = repository.getRecentMessages(sessionId, 10)
                if (recentMessages.isNotEmpty()) {
                    buildString {
                        appendLine("[Chat History]")
                        recentMessages.forEach { msg ->
                            val role = if (msg.isUser) "User" else "Assistant"
                            appendLine("$role: ${msg.text}")
                        }
                    }
                } else ""
            } else ""

            // Construct Complete Prompt
            // Order: System Data -> History -> Current Question
            val aiPrompt = """
                [System Context: User health data (Last 24h) in TOON format]
                $contextString
                
                $historyString
                
                [User Question]
                $text
            """.trimIndent()

            // Send to Repo
            repository.sendMessage(
                sessionId = sessionId,
                displayText = text,
                aiPrompt = aiPrompt,
                isFirstMessage = isFirst
            )

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
