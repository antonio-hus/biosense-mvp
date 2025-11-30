package com.biosense.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.core.content.ContextCompat
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.biosense.app.ui.theme.*
import com.biosense.app.viewmodel.NotificationSettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationSettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToHistory: () -> Unit = {},
    viewModel: NotificationSettingsViewModel = viewModel()
) {
    val settings by viewModel.settings
    val isLoading by viewModel.isLoading
    val isTesting by viewModel.isTesting
    val testMessage by viewModel.testMessage

    val scrollState = rememberScrollState()
    
    // Request notification permission for Android 13+
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Permission granted, can now test notification
        }
    }
    
    val context = LocalContext.current
    val hasNotificationPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
    } else {
        true // Pre-Android 13, no permission needed
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
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            // Header
            TopAppBar(
                title = {
                    Text(
                        "Notification Settings",
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
                )
            )

            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .padding(bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Spacer(modifier = Modifier.height(16.dp))

                    // Enable/Disable Toggle
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
                                .padding(20.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "Enable Notifications",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = OnBackground
                                    )
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    "Receive AI-powered health alerts",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = OnBackground.copy(alpha = 0.7f)
                                    )
                                )
                            }
                            Switch(
                                checked = settings.enabled,
                                onCheckedChange = { viewModel.setEnabled(it) }
                            )
                        }
                    }

                    // Notification History Button - Always visible
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToHistory() },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Surface.copy(alpha = 0.7f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(
                                        Primary.copy(alpha = 0.1f),
                                        RoundedCornerShape(8.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Notifications,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                    tint = Primary
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "View Notification History",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = OnBackground
                                )
                                Text(
                                    "See all past health alerts",
                                    fontSize = 14.sp,
                                    color = OnBackground.copy(alpha = 0.6f)
                                )
                            }
                            Icon(
                                Icons.Default.ChevronRight,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = OnBackground.copy(alpha = 0.5f)
                            )
                        }
                    }

                    if (settings.enabled) {
                        // Notification Hours
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Surface.copy(alpha = 0.7f)
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Text(
                                    "Notification Hours",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = OnBackground
                                    )
                                )
                                Text(
                                    "Choose when you want to receive notifications",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = OnBackground.copy(alpha = 0.7f)
                                    )
                                )

                                // Start Hour
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "Start Hour",
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            color = OnBackground
                                        )
                                    )
                                    HourSelector(
                                        hour = settings.startHour,
                                        onHourChange = { viewModel.setStartHour(it) }
                                    )
                                }

                                // End Hour
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "End Hour",
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            color = OnBackground
                                        )
                                    )
                                    HourSelector(
                                        hour = settings.endHour,
                                        onHourChange = { viewModel.setEndHour(it) }
                                    )
                                }

                                // Allow Overnight
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            "Allow Overnight",
                                            style = MaterialTheme.typography.bodyLarge.copy(
                                                color = OnBackground
                                            )
                                        )
                                        Text(
                                            "Receive notifications during night hours",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = OnBackground.copy(alpha = 0.6f)
                                            )
                                        )
                                    }
                                    Switch(
                                        checked = settings.allowOvernight,
                                        onCheckedChange = { viewModel.setAllowOvernight(it) }
                                    )
                                }
                            }
                        }

                        // Check Interval
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Surface.copy(alpha = 0.7f)
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp)
                            ) {
                                Text(
                                    "Check Interval",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = OnBackground
                                    )
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "How often to check your health data",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = OnBackground.copy(alpha = 0.7f)
                                    )
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                
                                val intervalOptions = listOf(15, 30, 60, 120, 240)
                                val intervalLabels = listOf("15m", "30m", "1h", "2h", "4h")
                                
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    intervalOptions.forEachIndexed { index, minutes ->
                                        Box(
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            FilterChip(
                                                selected = settings.checkIntervalMinutes == minutes,
                                                onClick = { viewModel.setCheckIntervalMinutes(minutes) },
                                                label = { 
                                                    Text(
                                                        intervalLabels[index], 
                                                        fontSize = 11.sp,
                                                        textAlign = TextAlign.Center,
                                                        modifier = Modifier.fillMaxWidth(),
                                                        maxLines = 1
                                                    ) 
                                                },
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(36.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Permission Warning (Android 13+)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasNotificationPermission) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFFFF9800).copy(alpha = 0.2f)
                                )
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(20.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Text(
                                        "⚠️ Notification Permission Required",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.SemiBold,
                                            color = OnBackground
                                        )
                                    )
                                    Text(
                                        "Please grant notification permission to receive health alerts",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            color = OnBackground.copy(alpha = 0.7f)
                                        )
                                    )
                                    Button(
                                        onClick = {
                                            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFFFF9800)
                                        )
                                    ) {
                                        Text("Grant Permission")
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        // Test Notification Button
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Secondary.copy(alpha = 0.2f)
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    "🧪 Test Notification",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = OnBackground
                                    )
                                )
                                Text(
                                    "Manually trigger a health check to test if notifications are working",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = OnBackground.copy(alpha = 0.7f)
                                    )
                                )
                                
                                Button(
                                    onClick = { 
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasNotificationPermission) {
                                            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                        } else {
                                            viewModel.testNotification() 
                                        }
                                    },
                                    enabled = !isTesting,
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Secondary
                                    )
                                ) {
                                    if (isTesting) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(20.dp),
                                            color = OnSecondary
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Testing...")
                                    } else {
                                        Icon(
                                            Icons.Default.Notifications,
                                            contentDescription = null,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasNotificationPermission) 
                                            "Grant Permission First" 
                                        else 
                                            "Test Notification Now")
                                    }
                                }
                                
                                // Show test result message
                                testMessage?.let { message ->
                                    Text(
                                        message,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = if (message.startsWith("✅")) 
                                                Color(0xFF4CAF50) 
                                            else 
                                                Color(0xFFF44336)
                                        )
                                    )
                                }
                            }
                        }

                        // Info Card
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Primary.copy(alpha = 0.1f)
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp)
                            ) {
                                Text(
                                    "ℹ️ How it works",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = OnBackground
                                    )
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "Biosense AI analyzes your health data and sends notifications when it detects something that needs attention, like low activity levels or unusual patterns.",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = OnBackground.copy(alpha = 0.8f)
                                    )
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
private fun HourSelector(
    hour: Int,
    onHourChange: (Int) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }

    Button(
        onClick = { showDialog = true },
        colors = ButtonDefaults.buttonColors(
            containerColor = Primary.copy(alpha = 0.2f)
        )
    ) {
        Text(
            String.format("%02d:00", hour),
            color = Primary
        )
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Select Hour") },
            text = {
                Column {
                    (0..23).forEach { h ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            TextButton(
                                onClick = {
                                    onHourChange(h)
                                    showDialog = false
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(String.format("%02d:00", h))
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

