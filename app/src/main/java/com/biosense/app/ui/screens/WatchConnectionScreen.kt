package com.biosense.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.*
import com.biosense.app.ui.theme.*
import kotlinx.coroutines.delay

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun WatchConnectionScreen(
    onContinue: () -> Unit,
    onRequestPermissions: (callback: () -> Unit) -> Unit = { _ -> },
    permissionGranted: Boolean = false
) {
    var isConnecting by remember { mutableStateOf(false) }
    var isConnected by remember { mutableStateOf(false) }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Background,
                        Background.copy(alpha = 0.8f),
                        Primary.copy(alpha = 0.1f)
                    )
                )
            )
    ) {
        WatchConnectionStep(
            isConnecting = isConnecting,
            isConnected = isConnected,
            permissionsGranted = permissionGranted,
            onConnect = {
                isConnecting = true
                onRequestPermissions {
                    isConnected = true
                    isConnecting = false
                }
            },
            onNext = onContinue
        )
    }
    
    LaunchedEffect(permissionGranted, isConnecting) {
        if (permissionGranted && isConnecting && !isConnected) {
            delay(1500)
            isConnected = true
            isConnecting = false
        }
    }
}

@Composable
fun WatchConnectionStep(
    isConnecting: Boolean,
    isConnected: Boolean,
    permissionsGranted: Boolean,
    onConnect: () -> Unit,
    onNext: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition()
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        )
    )
    
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing)
        )
    )
    
    Box(modifier = Modifier.fillMaxSize()) {
        var showPermissionStatus by remember { mutableStateOf(false) }
        
        LaunchedEffect(isConnecting) {
            if (isConnecting) {
                delay(1000)
                showPermissionStatus = true
            }
        }
        
        androidx.compose.animation.AnimatedVisibility(
            visible = showPermissionStatus,
            enter = slideInHorizontally(initialOffsetX = { it }) + fadeIn(),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
        ) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (permissionsGranted) 
                        Color(0xFF4ECDC4).copy(alpha = 0.9f) 
                    else 
                        Color(0xFFE74C3C).copy(alpha = 0.9f)
                ),
                shape = RoundedCornerShape(20.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (permissionsGranted) 
                            Icons.Default.CheckCircle 
                        else 
                            Icons.Default.Cancel,
                        contentDescription = if (permissionsGranted) 
                            "Permissions Granted" 
                        else 
                            "Permissions Denied",
                        modifier = Modifier.size(16.dp),
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (permissionsGranted) 
                            "Permissions OK" 
                        else 
                            "Permissions Denied",
                        fontSize = 12.sp,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
        Box(
            modifier = Modifier.size(200.dp),
            contentAlignment = Alignment.Center
        ) {
            if (isConnecting) {
                repeat(3) { index ->
                    Box(
                        modifier = Modifier
                            .size((120 + index * 30).dp)
                            .scale(pulseScale)
                            .alpha(0.3f - index * 0.1f)
                            .border(
                                width = 2.dp,
                                color = Secondary.copy(alpha = 0.5f),
                                shape = CircleShape
                            )
                    )
                }
            }
            
            Box(
                modifier = Modifier.size(300.dp),
                contentAlignment = Alignment.Center
            ) {
                val composition by rememberLottieComposition(LottieCompositionSpec.Asset("smartwatch.lottie"))
                
                LottieAnimation(
                    composition = composition,
                    isPlaying = true,
                    iterations = LottieConstants.IterateForever,
                    speed = if (isConnecting) 1.5f else 1f,
                    modifier = Modifier.size(300.dp)
                )
                
                // Checkmark overlay when connected
                androidx.compose.animation.AnimatedVisibility(
                    visible = isConnected,
                    enter = scaleIn(animationSpec = spring(dampingRatio = 0.7f))
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Connected",
                        modifier = Modifier
                            .size(40.dp)
                            .offset(x = 80.dp, y = 80.dp),
                        tint = Color(0xFF4ECDC4)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(48.dp))
        
        Text(
            text = when {
                isConnected -> "Watch Connected!"
                isConnecting -> "Connecting to Watch..."
                else -> "Connect Your Smart Watch"
            },
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = OnBackground
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = when {
                isConnected -> "Health data synced from Health Connect"
                isConnecting -> "Accessing Health Connect data..."
                else -> "Connect to Health Connect to access data from your health apps"
            },
            fontSize = 16.sp,
            color = OnSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        
        if (!isConnected && !isConnecting) {
            Column {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Secondary.copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            tint = Secondary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Health Data Access",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Secondary
                            )
                            Text(
                                text = "Connect to Health Connect to access your health app data",
                                fontSize = 12.sp,
                                color = OnSurfaceVariant
                            )
                        }
                    }
                }
                
                Button(
                    onClick = onConnect,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Secondary
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.HealthAndSafety,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Connect Health Data",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            TextButton(
                onClick = onNext,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Skip for now",
                    fontSize = 16.sp,
                    color = OnSurfaceVariant
                )
            }
        }
        
        if (isConnecting) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                var progress by remember { mutableStateOf(0f) }
                
                LaunchedEffect(isConnecting) {
                    if (isConnecting) {
                        for (i in 1..100) {
                            delay(25)
                            progress = i / 100f
                        }
                    }
                }
                
                Text(
                    text = when {
                        progress < 0.3f -> "Requesting Health Connect permissions..."
                        progress < 0.7f -> "Accessing Health Connect data..."
                        else -> "Loading health data from Health Connect... ${(progress * 100).toInt()}%"
                    },
                    fontSize = 14.sp,
                    color = OnSurfaceVariant,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .background(
                            OnSurfaceVariant.copy(alpha = 0.2f),
                            RoundedCornerShape(4.dp)
                        )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progress)
                            .height(8.dp)
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(Secondary, Color(0xFF4ECDC4))
                                ),
                                RoundedCornerShape(4.dp)
                            )
                    )
                }
            }
        }
        
        if (isConnected) {
            Button(
                onClick = onNext,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4ECDC4)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = "Create Your Profile",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        }
    }
}
