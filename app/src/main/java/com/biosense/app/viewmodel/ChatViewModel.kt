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

/**
 * ViewModel managing the Chat feature.
 * Responsibilities:
 * 1. Manages session state (creation, selection, deletion).
 * 2. Aggregates context for the AI (User Profile + Health Data + Chat History).
 * 3. Orchestrates message sending and handles AI context updates (e.g. updating "sick" status).
 */
class ChatViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ChatRepository
    private val healthConnectManager: IHealthConnectManager
    private val serializer: HealthContextSerializer
    private val userViewModel: UserViewModel

    init {
        // Safely load API Key (fallback to empty string to prevent crashes, though calls will fail)
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }

        val database = BiosenseDatabase.getDatabase(application)

        // Using FakeHealthConnectManager for development; switch to Real implementation for production.
        healthConnectManager = FakeHealthConnectManager.getInstance()

        val apiService = GeminiService(apiKey)
        repository = ChatRepository(database.chatDao(), apiService)
        serializer = HealthContextSerializer()

        userViewModel = UserViewModel(application)
    }

    /** Exposes all chat sessions for the history drawer. */
    val sessions: StateFlow<List<ChatSessionEntity>> = repository.allSessions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** The ID of the currently active chat session. */
    private val _currentSessionId = MutableStateFlow<String?>(null)
    val currentSessionId: StateFlow<String?> = _currentSessionId.asStateFlow()

    /**
     * Tracks if the current session is "temporary" (unsaved).
     * A session remains temporary until the user sends the first message.
     */
    private val _isTemporarySession = MutableStateFlow(false)

    /**
     * Real-time stream of messages for the current session.
     * Uses flatMapLatest to dynamically switch the DB observer when the sessionId changes.
     * This ensures the UI always reflects the correct conversation instantly.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    val messages: StateFlow<List<ChatMessageEntity>> = _currentSessionId
        .flatMapLatest { sessionId ->
            if (sessionId == null) {
                flowOf(emptyList())
            } else {
                // Directly observe the database.
                // Even for a temp session, we observe the (empty) DB so that the moment
                // a message is inserted, the UI updates automatically without manual refresh.
                repository.getMessages(sessionId)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    /** Initializes the session state on screen load. */
    fun initializeSession() {
        if (_currentSessionId.value == null) prepareNewTempSession()
    }

    /** Creates a new ephemeral session ID. No DB entry is made until a message is sent. */
    fun prepareNewTempSession() {
        _isTemporarySession.value = true
        _currentSessionId.value = UUID.randomUUID().toString()
    }

    /** Switches the view to an existing historical session. */
    fun selectSession(sessionId: String) {
        _isTemporarySession.value = false
        _currentSessionId.value = sessionId
    }

    /** Deletes a session and resets to a new one if the deleted session was active. */
    fun deleteSession(sessionId: String) {
        viewModelScope.launch {
            repository.deleteSession(sessionId)
            if (_currentSessionId.value == sessionId) prepareNewTempSession()
        }
    }

    /** Renames a session title. */
    fun renameSession(sessionId: String, newTitle: String) {
        viewModelScope.launch { repository.renameSession(sessionId, newTitle) }
    }

    /**
     * Core logic to process a user's message.
     * 1. Finalizes temporary sessions.
     * 2. Gathers 72h of health data (converted to TOON format).
     * 3. Builds a context-aware System Prompt.
     * 4. Sends to Repository (which inserts User Msg -> Calls AI -> Inserts AI Msg).
     * 5. Updates User Profile if AI detects a context change (e.g. "I feel better").
     */
    fun sendMessage(text: String) {
        val sessionId = _currentSessionId.value ?: return
        if (text.isBlank()) return

        viewModelScope.launch {
            _isLoading.value = true
            val isFirst = _isTemporarySession.value

            // 1. Flip temporary flag so subsequent logic treats this as a persisted session.
            if (isFirst) {
                _isTemporarySession.value = false
            }

            // 2. Gather Context (User Profile + Health Data)
            val user = userViewModel.currentUser.value

            val now = Instant.now()
            val startTime = now.minus(3, ChronoUnit.DAYS)
            val healthContext = healthConnectManager.getHealthContext(startTime, now)
            val healthString = serializer.toToon(healthContext)

            // 3. Gather Chat History (Last 10 messages)
            // Needed so the AI remembers the immediate conversation context.
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

            // 4. Build Complete Prompt
            val systemPrompt = constructSystemPrompt(user, healthString)
            val fullAiPrompt = """
                $systemPrompt
                
                $historyString
                
                USER_QUESTION:
                $text
            """.trimIndent()

            // 5. Send to Repository
            // The repository handles the actual DB insertion and API call.
            // It returns a string if the AI requested a context update (e.g. [SET_CONTEXT: Flu]).
            val contextUpdate = repository.sendMessage(
                sessionId = sessionId,
                displayText = text,
                aiPrompt = fullAiPrompt,
                isFirstMessage = isFirst
            )

            // 6. Update User Context if needed
            if (contextUpdate != null) {
                userViewModel.overwriteUserContext(contextUpdate)
            }

            _isLoading.value = false
        }
    }

    /**
     * Constructs the System Prompt that defines the AI's persona and knowledge.
     * Includes strict instructions on how to interpret data and manage user context.
     */
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
