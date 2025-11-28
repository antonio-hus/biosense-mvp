package com.biosense.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.biosense.app.data.entity.ChatSessionEntity
import com.biosense.app.ui.components.ChatBubble
import com.biosense.app.ui.components.ChatInputField
import com.biosense.app.ui.components.GlassNavBar
import com.biosense.app.ui.components.Header
import com.biosense.app.ui.components.TypingIndicator
import com.biosense.app.viewmodel.ChatViewModel
import kotlinx.coroutines.launch

@Composable
fun ChatScreen(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    onProfileClick: () -> Unit,
    viewModel: ChatViewModel = viewModel()
) {
    val messages by viewModel.messages.collectAsState()
    val sessions by viewModel.sessions.collectAsState()
    val currentSessionId by viewModel.currentSessionId.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    // Initial session check
    LaunchedEffect(sessions) {
        if (sessions.isEmpty() && currentSessionId == null) {
            viewModel.createNewSession()
        } else if (sessions.isNotEmpty() && currentSessionId == null) {
            viewModel.selectSession(sessions.first().id)
        }
    }

    // Auto-scroll
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
                    viewModel.createNewSession()
                    scope.launch { drawerState.close() }
                },
                onSessionSelected = { id ->
                    viewModel.selectSession(id)
                    scope.launch { drawerState.close() }
                }
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
                // Modified Header with Menu Button
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { scope.launch { drawerState.open() } },
                        modifier = Modifier
                            .size(48.dp)
                            .glassEffect() // Helper extension below
                    ) {
                        Icon(Icons.Default.Menu, "History", tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Header(title = "Advisor", onProfileClick = onProfileClick)
                }

                // Messages List
                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 16.dp),
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
fun GlassDrawerContent(
    sessions: List<ChatSessionEntity>,
    currentSessionId: String?,
    onNewChat: () -> Unit,
    onSessionSelected: (String) -> Unit
) {
    ModalDrawerSheet(
        drawerContainerColor = Color.Transparent,
        modifier = Modifier
            .fillMaxHeight()
            .width(300.dp)
            .padding(end = 16.dp) // Gap from edge
    ) {
        // Glass container for the drawer
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp))
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF1A4D5C).copy(alpha = 0.9f), // Darker for legibility
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
                // New Chat Button
                Button(
                    onClick = onNewChat,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .glassEffect(cornerRadius = 16.dp),
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
                        val isSelected = session.id == currentSessionId
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onSessionSelected(session.id) }
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
                        }
                    }
                }
            }
        }
    }
}

// Reusable Glass Modifier Extension
fun Modifier.glassEffect(cornerRadius: androidx.compose.ui.unit.Dp = 24.dp) = this
    .clip(RoundedCornerShape(cornerRadius))
    .background(
        brush = Brush.linearGradient(
            colors = listOf(Color.White.copy(alpha = 0.25f), Color.White.copy(alpha = 0.15f))
        )
    )
    .border(
        width = 1.dp,
        brush = Brush.linearGradient(
            colors = listOf(Color.White.copy(alpha = 0.3f), Color.White.copy(alpha = 0.1f))
        ),
        shape = RoundedCornerShape(cornerRadius)
    )
