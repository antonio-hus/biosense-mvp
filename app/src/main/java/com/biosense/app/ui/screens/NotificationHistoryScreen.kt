package com.biosense.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.biosense.app.data.entity.NotificationHistoryEntity
import com.biosense.app.ui.theme.*
import com.biosense.app.viewmodel.NotificationHistoryViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationHistoryScreen(
    onNavigateBack: () -> Unit,
    viewModel: NotificationHistoryViewModel = viewModel()
) {
    val notifications by viewModel.notifications.collectAsState()
    val isLoading by viewModel.isLoading
    
    // Load notifications when screen is first shown
    LaunchedEffect(Unit) {
        viewModel.loadNotifications()
    }
    
    // Safely compute grouped notifications
    val groupedNotifications = remember(notifications) {
        try {
            android.util.Log.d("NotificationHistoryScreen", "Grouping ${notifications.size} notifications")
            viewModel.getGroupedNotifications(notifications)
        } catch (e: Exception) {
            android.util.Log.e("NotificationHistoryScreen", "Error grouping notifications", e)
            emptyMap<String, List<NotificationHistoryEntity>>()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Background,
                        Background.copy(alpha = 0.9f),
                        Primary.copy(alpha = 0.05f)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header
            TopAppBar(
                title = {
                    Text(
                        "Notification History",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = OnBackground
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = OnBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                ),
                actions = {
                    if (notifications.isNotEmpty()) {
                        IconButton(
                            onClick = { viewModel.deleteOldNotifications() }
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Clear old",
                                tint = OnBackground
                            )
                        }
                    }
                }
            )

            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (notifications.isEmpty()) {
                // Empty state
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            Icons.Default.Notifications,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = OnBackground.copy(alpha = 0.5f)
                        )
                        Text(
                            "No notifications yet",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = OnBackground.copy(alpha = 0.7f)
                            )
                        )
                        Text(
                            "Your health alerts will appear here",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = OnBackground.copy(alpha = 0.5f)
                            )
                        )
                    }
                }
            } else if (groupedNotifications.isEmpty()) {
                // Empty state (shouldn't happen if notifications is not empty, but safety check)
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            Icons.Default.Notifications,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = OnBackground.copy(alpha = 0.5f)
                        )
                        Text(
                            "No notifications yet",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = OnBackground.copy(alpha = 0.7f)
                            )
                        )
                        Text(
                            "Your health alerts will appear here",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = OnBackground.copy(alpha = 0.5f)
                            )
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    groupedNotifications.forEach { (dateGroup, dateNotifications) ->
                        if (dateNotifications.isNotEmpty()) {
                            item {
                                // Date header
                                Text(
                                    text = dateGroup,
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = OnBackground.copy(alpha = 0.7f)
                                    ),
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }
                            
                            items(dateNotifications) { notification ->
                                NotificationHistoryItem(
                                    notification = notification,
                                    onDelete = { viewModel.deleteNotification(notification.id) },
                                    formatTime = { viewModel.formatTime(it) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationHistoryItem(
    notification: NotificationHistoryEntity,
    onDelete: () -> Unit,
    formatTime: (Long) -> String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Surface.copy(alpha = 0.7f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.Notifications,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = Primary
                    )
                    Text(
                        formatTime(notification.timestamp),
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = OnBackground.copy(alpha = 0.6f),
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = notification.message,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = OnBackground,
                        lineHeight = 20.sp
                    )
                )
            }
            
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete",
                    modifier = Modifier.size(18.dp),
                    tint = OnBackground.copy(alpha = 0.5f)
                )
            }
        }
    }
}

