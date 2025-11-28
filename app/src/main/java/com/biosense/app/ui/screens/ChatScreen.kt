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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.biosense.app.data.entity.ChatSessionEntity
import com.biosense.app.ui.components.ChatBubble
import com.biosense.app.ui.components.ChatInputField
import com.biosense.app.ui.components.GlassNavBar
import com.biosense.app.ui.components.Header
import com.biosense.app.ui.components.TypingIndicator
import com.biosense.app.ui.components.glassEffect
import com.biosense.app.viewmodel.ChatViewModel
import kotlinx.coroutines.launch

@Composable
fun ChatScreen(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    onProfileClick: () -> Unit,
    viewModel: ChatViewModel = viewModel(),
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
            Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
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
                    Spacer(modifier = Modifier.width(8.dp))
                    Header(title = "Advisor", onProfileClick = onProfileClick)
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
                var text by remember { mutableStateOf("") }
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

@Composable
fun EmptyChatSuggestions(
    suggestions: List<String>,
    onSuggestionClick: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "How can I help you today?",
            fontSize = 20.sp,
            color = Color.White.copy(alpha = 0.8f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        suggestions.forEach { suggestion ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .glassEffect(shape = RoundedCornerShape(16.dp))
                    .clickable { onSuggestionClick(suggestion) }
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = suggestion,
                    color = Color.White,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )
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
                Button(
                    onClick = onNewChat,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .glassEffect(shape = RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Add, null, tint = Color.White)
                            Spacer(Modifier.width(8.dp))
                            Text("New Session", color = Color.White)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                Text("History", color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp)
                Spacer(modifier = Modifier.height(12.dp))

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
                if (isSelected) Color.White.copy(alpha = 0.15f)
                else Color.Transparent
            )
            .padding(16.dp)
    ) {
        Text(
            text = session.title,
            color = if (isSelected) Color.White else Color.White.copy(alpha = 0.7f),
            fontSize = 16.sp
        )

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
