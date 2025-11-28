package com.biosense.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.biosense.app.BuildConfig
import com.biosense.app.data.BiosenseDatabase
import com.biosense.app.data.entity.ChatMessageEntity
import com.biosense.app.data.entity.ChatSessionEntity
import com.biosense.app.data.model.User
import com.biosense.app.repository.ChatRepository
import com.biosense.app.data.serializer.HealthContextSerializer
import com.biosense.app.service.api.GeminiService
import com.biosense.app.service.health.FakeHealthConnectManager
import com.biosense.app.service.health.IHealthConnectManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID
import java.util.Properties

class ChatViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ChatRepository
    private val healthConnectManager: IHealthConnectManager
    private val serializer: HealthContextSerializer

    // Internal UserViewModel to access and update user context
    private val userViewModel: UserViewModel

    init {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }

        val database = BiosenseDatabase.getDatabase(application)
        healthConnectManager = FakeHealthConnectManager.getInstance()

        val apiService = GeminiService(apiKey)
        repository = ChatRepository(database.chatDao(), apiService)
        serializer = HealthContextSerializer()

        userViewModel = UserViewModel(application)
    }

    val sessions: StateFlow<List<ChatSessionEntity>> = repository.allSessions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _currentSessionId = MutableStateFlow<String?>(null)
    val currentSessionId: StateFlow<String?> = _currentSessionId.asStateFlow()

    // We keep this boolean mainly to know if we need to trigger the 'Session Creation' logic in Repo
    private val _isTemporarySession = MutableStateFlow(false)

    @OptIn(ExperimentalCoroutinesApi::class)
    val messages: StateFlow<List<ChatMessageEntity>> = _currentSessionId
        .flatMapLatest { sessionId ->
            if (sessionId == null) {
                flowOf(emptyList())
            } else {
                // Always observe the DB.
                // If it's a temp session, DB is empty -> returns empty list.
                // If msg inserted -> DB updates -> returns list with msg.
                // This prevents the 'flash' effect.
                repository.getMessages(sessionId)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun initializeSession() {
        if (_currentSessionId.value == null) prepareNewTempSession()
    }

    fun prepareNewTempSession() {
        _isTemporarySession.value = true
        // Generate ID immediately so we can query the (empty) DB for it
        _currentSessionId.value = UUID.randomUUID().toString()
    }

    fun selectSession(sessionId: String) {
        _isTemporarySession.value = false
        _currentSessionId.value = sessionId
    }

    fun deleteSession(sessionId: String) {
        viewModelScope.launch {
            repository.deleteSession(sessionId)
            if (_currentSessionId.value == sessionId) prepareNewTempSession()
        }
    }

    fun renameSession(sessionId: String, newTitle: String) {
        viewModelScope.launch { repository.renameSession(sessionId, newTitle) }
    }


    fun sendMessage(text: String) {
        val sessionId = _currentSessionId.value ?: return
        if (text.isBlank()) return

        viewModelScope.launch {
            _isLoading.value = true
            val isFirst = _isTemporarySession.value

            // 1. Flip temporary flag.
            // Note: We don't rely on this for Flow switching anymore, so no UI glitch.
            if (isFirst) {
                _isTemporarySession.value = false
            }

            // 2. Gather Context
            val user = userViewModel.currentUser.value

            val now = Instant.now()
            val startTime = now.minus(3, ChronoUnit.DAYS)
            val healthContext = healthConnectManager.getHealthContext(startTime, now)
            val healthString = serializer.toToon(healthContext)

            val historyString = if (!isFirst) {
                val recent = repository.getRecentMessages(sessionId, 10)
                if (recent.isNotEmpty()) {
                    buildString {
                        appendLine("CHAT_HISTORY_LAST_10_MESSAGES:")
                        recent.forEach { msg ->
                            val role = if (msg.isUser) "User" else "BiosenseAI"
                            appendLine("$role: ${msg.text}")
                        }
                    }
                } else ""
            } else ""

            // 3. Build Prompt
            val systemPrompt = constructSystemPrompt(user, healthString)
            val fullAiPrompt = """
                $systemPrompt
                
                $historyString
                
                USER_QUESTION:
                $text
            """.trimIndent()

            // 4. Send to Repo (Inserts User Msg -> Calls AI -> Inserts AI Msg)
            // We pass the exact sessionId we generated in prepareNewTempSession
            val contextUpdate = repository.sendMessage(
                sessionId = sessionId,
                displayText = text,
                aiPrompt = fullAiPrompt,
                isFirstMessage = isFirst
            )

            // 5. Update User Context if needed
            if (contextUpdate != null) {
                userViewModel.overwriteUserContext(contextUpdate)
            }

            _isLoading.value = false
        }
    }

    private fun constructSystemPrompt(user: User, healthData: String): String {
        return """
            ROLE: You are Biosense, an expert, empathetic, and data-driven health advisor.
            
            USER_PROFILE:
            - Name: ${user.name}
            - Age: ${user.age}, Gender: ${user.gender}
            - Height: ${user.height}cm, Weight: ${user.weight}kg
            - Profession: ${user.profession}
            - Main Goal: ${user.healthGoal}
            
            CURRENT_CONTEXT (Temporary States/Conditions): 
            "${user.whatSenseKnows}"
            
            COMMUNICATION_STYLE: ${user.motivationStyle}
            
            INSTRUCTIONS:
            1. Analyze the provided health data (TOON format) relative to the user's profile.
            2. Provide actionable, style-appropriate advice.
            3. MANAGE CONTEXT: You have full control over the 'CURRENT_CONTEXT' field.
               - If the user reports a new condition (e.g., "I have the flu"), ADD it to the context.
               - If the user says a condition is gone (e.g., "I feel better now"), REMOVE it.
               - If the context changes in ANY way, output the command: [SET_CONTEXT: <new complete context string>]
               - If the context is empty or cleared, output: [SET_CONTEXT: ]
            
            EXAMPLES:
            - User: "I twisted my ankle." -> Output: ...advice... [SET_CONTEXT: Twisted ankle]
            - (Later) User: "Ankle is fine now." -> Output: ...advice... [SET_CONTEXT: ]
            - User: "I have a headache." (Context was 'Twisted ankle') -> Output: ...advice... [SET_CONTEXT: Twisted ankle; Headache]
            
            HEALTH_DATA:
            $healthData
        """.trimIndent()
    }
}
