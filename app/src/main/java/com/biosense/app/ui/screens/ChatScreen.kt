package com.biosense.app.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.biosense.app.data.entity.ChatSessionEntity
import com.biosense.app.ui.components.ChatBubble
import com.biosense.app.ui.components.ChatInputField
import com.biosense.app.ui.components.GlassNavBar
import com.biosense.app.ui.components.Header
import com.biosense.app.ui.components.Logo
import com.biosense.app.ui.components.TypingIndicator
import com.biosense.app.ui.components.glassEffect
import com.biosense.app.ui.components.HealthWaveAnimation
import com.biosense.app.viewmodel.ChatViewModel
import kotlinx.coroutines.launch

@Composable
fun ChatScreen(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    onProfileClick: () -> Unit,
    viewModel: ChatViewModel = viewModel(),
    initialPrompt: String? = null,
    suggestedQuestions: List<String> = listOf(
        "How is my sleep quality this week?",
        "Analyze my heart rate trends.",
        "Give me a summary of my activity."
    )
) {
    val messages by viewModel.messages.collectAsState()
    val sessions by viewModel.sessions.collectAsState()
    val currentSessionId by viewModel.currentSessionId.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    // Initialize session logic
    LaunchedEffect(Unit) {
        viewModel.initializeSession()
    }

    // Auto-scroll to bottom when new message arrives
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            GlassDrawerContent(
                sessions = sessions,
                currentSessionId = currentSessionId,
                onNewChat = {
                    viewModel.prepareNewTempSession()
                    scope.launch { drawerState.close() }
                },
                onSessionSelected = { id ->
                    viewModel.selectSession(id)
                    scope.launch { drawerState.close() }
                },
                onDeleteSession = { id -> viewModel.deleteSession(id) },
                onRenameSession = { id, newTitle -> viewModel.renameSession(id, newTitle) }
            )
        },
        scrimColor = Color.Black.copy(alpha = 0.6f)
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent,
            bottomBar = { GlassNavBar(currentRoute, onNavigate) }
        ) { innerPadding ->
            Box(modifier = Modifier.fillMaxSize()) {
                // Heartbeat animation background
                HealthWaveAnimation(
                    alpha = 0.12f,
                    modifier = Modifier.fillMaxSize()
                )

                Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                // Header with logo
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { scope.launch { drawerState.open() } },
                            modifier = Modifier
                                .size(48.dp)
                                .glassEffect(shape = RoundedCornerShape(16.dp))
                        ) {
                            Icon(Icons.Default.Menu, "History", tint = Color.White)
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Logo(
                                iconSize = 20.dp,
                                fontSize = 22.sp,
                                color = Color.White,
                                showText = true
                            )
                            Text(
                                text = "AI Health Advisor",
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.6f),
                                modifier = Modifier.padding(start = 26.dp)
                            )
                        }
                    }
                }

                // Content Area
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    if (messages.isEmpty() && !isLoading) {
                        // Empty State with Suggestions
                        EmptyChatSuggestions(
                            suggestions = suggestedQuestions,
                            onSuggestionClick = { question ->
                                viewModel.sendMessage(question)
                            }
                        )
                    } else {
                        // Messages List
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            state = listState,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(messages, key = { it.id }) { message ->
                                ChatBubble(message)
                            }
                            if (isLoading) {
                                item { TypingIndicator() }
                            }
                        }
                    }
                }

                // Input Field
                var text by remember { mutableStateOf(initialPrompt ?: "") }
                ChatInputField(
                    messageText = text,
                    onMessageChange = { text = it },
                    onSendClick = {
                        viewModel.sendMessage(text)
                        text = ""
                    },
                    modifier = Modifier.padding(16.dp)
                )
                }
            }
        }
    }
}

@Composable
fun EmptyChatSuggestions(
    suggestions: List<String>,
    onSuggestionClick: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // AI Icon
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(
                    color = Color(0xFF64B5AD).copy(alpha = 0.2f),
                    shape = CircleShape
                )
                .border(
                    width = 2.dp,
                    color = Color(0xFF64B5AD).copy(alpha = 0.5f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Psychology,
                contentDescription = "AI Advisor",
                tint = Color(0xFF64B5AD),
                modifier = Modifier.size(40.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Title
        Text(
            text = "How can I help you today?",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Ask me about your health metrics or get personalized insights",
            fontSize = 15.sp,
            color = Color.White.copy(alpha = 0.75f),
            textAlign = TextAlign.Center,
            lineHeight = 22.sp
        )

        Spacer(modifier = Modifier.height(40.dp))

        // Suggestions - matching app style with icons
        suggestions.forEachIndexed { index, suggestion ->
            val suggestionIcon = when {
                suggestion.contains("sleep", ignoreCase = true) -> Icons.Filled.Bedtime
                suggestion.contains("heart", ignoreCase = true) -> Icons.Filled.FavoriteBorder
                suggestion.contains("activity", ignoreCase = true) ||
                suggestion.contains("summary", ignoreCase = true) -> Icons.Filled.Summarize
                suggestion.contains("analyze", ignoreCase = true) ||
                suggestion.contains("trend", ignoreCase = true) -> Icons.Filled.TrendingUp
                else -> Icons.Outlined.Psychology
            }

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .shadow(
                        elevation = 4.dp,
                        shape = RoundedCornerShape(16.dp),
                        ambientColor = Color.Black.copy(alpha = 0.1f),
                        spotColor = Color.Black.copy(alpha = 0.1f)
                    )
                    .clickable { onSuggestionClick(suggestion) },
                shape = RoundedCornerShape(16.dp),
                color = Color.Transparent
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.15f),
                                    Color.White.copy(alpha = 0.08f)
                                )
                            ),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .border(
                            width = 1.dp,
                            color = Color.White.copy(alpha = 0.25f),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .padding(20.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Icon
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    color = Color(0xFF64B5AD).copy(alpha = 0.2f),
                                    shape = CircleShape
                                )
                                .border(
                                    width = 1.dp,
                                    color = Color(0xFF64B5AD).copy(alpha = 0.3f),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = suggestionIcon,
                                contentDescription = null,
                                tint = Color(0xFF64B5AD),
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        // Text
                        Text(
                            text = suggestion,
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            lineHeight = 22.sp,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

// ... (Keep GlassDrawerContent and SessionItem exactly as they were) ...
@Composable
fun GlassDrawerContent(
    sessions: List<ChatSessionEntity>,
    currentSessionId: String?,
    onNewChat: () -> Unit,
    onSessionSelected: (String) -> Unit,
    onDeleteSession: (String) -> Unit,
    onRenameSession: (String, String) -> Unit
) {
    var showRenameDialog by remember { mutableStateOf<Pair<String, String>?>(null) }

    ModalDrawerSheet(
        drawerContainerColor = Color.Transparent,
        modifier = Modifier
            .fillMaxHeight()
            .width(300.dp)
            .padding(end = 16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp))
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF1A4D5C).copy(alpha = 0.9f),
                            Color(0xFF0F1419).copy(alpha = 0.95f)
                        )
                    )
                )
                .border(
                    width = 1.dp,
                    brush = Brush.horizontalGradient(
                        colors = listOf(Color.White.copy(alpha = 0.2f), Color.Transparent)
                    ),
                    shape = RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp)
                )
                .padding(16.dp)
        ) {
            Column {
                // Drawer Title
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Psychology,
                        contentDescription = null,
                        tint = Color(0xFF64B5AD),
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "AI Advisor",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                // New Session Button
                Button(
                    onClick = onNewChat,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        Color(0xFF64B5AD).copy(alpha = 0.3f),
                                        Color(0xFF64B5AD).copy(alpha = 0.15f)
                                    )
                                ),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .border(
                                width = 1.dp,
                                color = Color(0xFF64B5AD).copy(alpha = 0.5f),
                                shape = RoundedCornerShape(16.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "New Chat",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // History Section Title
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.History,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.5f),
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Recent Chats",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))

                if (sessions.isEmpty()) {
                    // Empty state
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ChatBubbleOutline,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.3f),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No chat history yet",
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(sessions) { session ->
                            SessionItem(
                                session = session,
                                isSelected = session.id == currentSessionId,
                                onClick = { onSessionSelected(session.id) },
                                onDelete = { onDeleteSession(session.id) },
                                onRename = { showRenameDialog = session.id to session.title }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showRenameDialog != null) {
        val (sessionId, currentTitle) = showRenameDialog!!
        var newTitle by remember { mutableStateOf(currentTitle) }

        AlertDialog(
            onDismissRequest = { showRenameDialog = null },
            title = { Text("Rename Chat") },
            text = {
                OutlinedTextField(
                    value = newTitle,
                    onValueChange = { newTitle = it },
                    singleLine = true,
                    label = { Text("Title") }
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newTitle.isNotBlank()) {
                        onRenameSession(sessionId, newTitle)
                    }
                    showRenameDialog = null
                }) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = null }) { Text("Cancel") }
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SessionItem(
    session: ChatSessionEntity,
    isSelected: Boolean,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onRename: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .combinedClickable(
                onClick = onClick,
                onLongClick = { showMenu = true }
            )
            .background(
                if (isSelected) {
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFF64B5AD).copy(alpha = 0.25f),
                            Color(0xFF64B5AD).copy(alpha = 0.1f)
                        )
                    )
                } else {
                    Brush.horizontalGradient(
                        colors = listOf(Color.Transparent, Color.Transparent)
                    )
                },
                shape = RoundedCornerShape(12.dp)
            )
            .border(
                width = if (isSelected) 1.dp else 0.dp,
                color = if (isSelected) Color(0xFF64B5AD).copy(alpha = 0.4f) else Color.Transparent,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.ChatBubbleOutline,
                contentDescription = null,
                tint = if (isSelected) Color(0xFF64B5AD) else Color.White.copy(alpha = 0.4f),
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = session.title,
                color = if (isSelected) Color.White else Color.White.copy(alpha = 0.7f),
                fontSize = 15.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                modifier = Modifier.weight(1f),
                maxLines = 1
            )
        }

        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false }
        ) {
            DropdownMenuItem(
                text = { Text("Rename") },
                onClick = {
                    showMenu = false
                    onRename()
                }
            )
            DropdownMenuItem(
                text = { Text("Delete") },
                onClick = {
                    showMenu = false
                    onDelete()
                }
            )
        }
    }
}
