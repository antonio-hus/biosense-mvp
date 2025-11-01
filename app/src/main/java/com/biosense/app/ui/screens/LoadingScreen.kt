package com.biosense.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.*
import com.biosense.app.ui.theme.*
import kotlinx.coroutines.delay
import kotlin.math.min

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun LoadingScreen(
    userName: String = "User",
    onLoadingComplete: () -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    var textVisible by remember { mutableStateOf(false) }
    var progress by remember { mutableStateOf(0f) }
    
    val currentHour = remember { java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY) }
    val greeting = when (currentHour) {
        in 5..11 -> "Good Morning"
        in 12..16 -> "Good Afternoon"
        in 17..21 -> "Good Evening"
        else -> "Hello"
    }
    
    var loadingMessage by remember { mutableStateOf("Organizing your health data...") }
    
    LaunchedEffect(Unit) {
        delay(200)
        visible = true
        delay(800)
        textVisible = true
        
        val messages = listOf(
            "Organizing your health data...",
            "Analyzing recent patterns...",
            "Preparing personalized insights...",
            "Almost ready!"
        )
        
        for (i in 1..100) {
            val messageIndex = when {
                i <= 25 -> 0
                i <= 50 -> 1
                i <= 80 -> 2
                else -> 3
            }
            loadingMessage = messages[messageIndex]
            
            delay(35)
            progress = i / 100f
        }
        
        loadingMessage = "Ready to go!"
        delay(500)
        onLoadingComplete()
    }
    
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(animationSpec = tween(1000)) + scaleIn(
                    animationSpec = tween(1000)
                )
            ) {
                Box(
                    modifier = Modifier.size(320.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val composition by rememberLottieComposition(LottieCompositionSpec.Asset("data_processing.lottie"))
                    
                    LottieAnimation(
                        composition = composition,
                        isPlaying = true,
                        iterations = LottieConstants.IterateForever,
                        speed = 1f,
                        modifier = Modifier.size(320.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            AnimatedVisibility(
                visible = textVisible,
                enter = fadeIn(animationSpec = tween(800)) + slideInVertically(
                    initialOffsetY = { 40 },
                    animationSpec = tween(800)
                )
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "$greeting, $userName!",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Secondary,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text(
                        text = "Welcome back to Biosense",
                        fontSize = 18.sp,
                        color = TextSecondary,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    AnimatedContent(
                        targetState = loadingMessage,
                        transitionSpec = {
                            fadeIn(animationSpec = tween(300)) with
                            fadeOut(animationSpec = tween(300))
                        }
                    ) { message ->
                        Text(
                            text = message,
                            fontSize = 14.sp,
                            color = OnSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(48.dp))
            
            AnimatedVisibility(
                visible = textVisible,
                enter = fadeIn(animationSpec = tween(800, 400))
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .height(6.dp)
                            .background(
                                OnSurfaceVariant.copy(alpha = 0.2f),
                                androidx.compose.foundation.shape.RoundedCornerShape(3.dp)
                            )
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(progress)
                                .height(6.dp)
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(Secondary, Color(0xFF4ECDC4))
                                    ),
                                    androidx.compose.foundation.shape.RoundedCornerShape(3.dp)
                                )
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "${(progress * 100).toInt()}%",
                        fontSize = 12.sp,
                        color = OnSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
        
        AnimatedVisibility(
            visible = textVisible,
            enter = fadeIn(animationSpec = tween(1000, 1000)),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(bottom = 48.dp)
            ) {
                Text(
                    text = "Biosense",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Secondary.copy(alpha = 0.7f)
                )
                Text(
                    text = "Intelligent Health Insights",
                    fontSize = 12.sp,
                    color = OnSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
    }
}