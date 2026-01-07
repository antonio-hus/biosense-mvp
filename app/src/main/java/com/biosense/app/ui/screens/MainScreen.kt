package com.biosense.app.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.painterResource
import com.biosense.app.R
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.biosense.app.ui.theme.OnBackground
import com.biosense.app.ui.theme.OnSecondary
import com.biosense.app.ui.theme.Secondary
import com.biosense.app.ui.theme.TextSecondary
import kotlinx.coroutines.delay
import kotlin.math.min

enum class ECGType {
    Normal,
    Tachycardia,
    Bradycardia
}

@Composable
fun MainScreen(
    onCreateAccount: () -> Unit
) {
    var lottieLoaded by remember { mutableStateOf(false) }
    var cumulativeTapCount by remember { mutableStateOf(0f) }
    var runningManSpeed by remember { mutableStateOf(1.2f) }
    var pulseIntensity by remember { mutableStateOf(0f) }
    var lastTapTime by remember { mutableStateOf(0L) }
    
    LaunchedEffect(cumulativeTapCount) {
        if (cumulativeTapCount > 0) {
            while (cumulativeTapCount > 0) {
                delay(100)
                val currentTime = System.currentTimeMillis()
                if (currentTime - lastTapTime >= 500) {
                    cumulativeTapCount = maxOf(0f, cumulativeTapCount - 0.1f)
                    runningManSpeed = 1.2f + (cumulativeTapCount * 0.1f)
                }
            }
        }
    }
    
    var liveTapCount by remember { mutableStateOf(0) }
    LaunchedEffect(liveTapCount) {
        while (liveTapCount > 0) {
            delay(1000)
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastTapTime >= 1000) {
                liveTapCount = maxOf(0, liveTapCount - 2)
            }
        }
    }
    
    val animatedPulseIntensity by animateFloatAsState(
        targetValue = min(liveTapCount / 10f, 2f),
        animationSpec = tween(
            durationMillis = 100,
            easing = FastOutSlowInEasing
        )
    )
    
    LaunchedEffect(animatedPulseIntensity) {
        pulseIntensity = animatedPulseIntensity
    }
    
    val preloadComposition by rememberLottieComposition(LottieCompositionSpec.Asset("running_man.lottie"))
    LaunchedEffect(preloadComposition) {
        if (preloadComposition != null && !lottieLoaded) {
            lottieLoaded = true
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { 
                cumulativeTapCount += 0.5f
                runningManSpeed = 1.2f + (cumulativeTapCount * 0.1f)
                liveTapCount++
                lastTapTime = System.currentTimeMillis()
            }
    ) {
        HealthWaveAnimation(tapIntensity = pulseIntensity)
        if (pulseIntensity > 1.5f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color.Red.copy(alpha = min(0.15f * (pulseIntensity - 1.5f), 0.2f)),
                                Color.Transparent
                            ),
                            radius = 800f
                        )
                    )
            )
        }
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (lottieLoaded) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        contentAlignment = Alignment.TopCenter
                    ) {
                        RunningManAnimation(
                            speedMultiplier = runningManSpeed,
                            modifier = Modifier.offset(y = (-320).dp)
                        )
                        
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.offset(y = 220.dp)
                        ) {
                            LiveHealthStats(tapIntensity = pulseIntensity)
                            
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.offset(y = 30.dp)
                            ) {
                                // Biosense logo with text
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = "biosense",
                                        fontSize = 48.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Secondary
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Image(
                                        painter = painterResource(id = R.drawable.biosense_logo),
                                        contentDescription = "Biosense Logo",
                                        modifier = Modifier.size(40.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Text(
                                    text = "Intelligent Health Insights",
                                    fontSize = 18.sp,
                                    color = TextSecondary,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(start = 0.dp, top = 0.dp, end = 0.dp, bottom = 0.dp)
                                )
                                
                                Spacer(modifier = Modifier.height(32.dp))
                                SwipeableMarketingCards()
                            }
                            
                            Spacer(modifier = Modifier.height(64.dp))
                            
                            Button(
                                onClick = onCreateAccount,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp)
                                    .padding(start = 24.dp, end = 24.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Secondary,
                                    contentColor = OnSecondary
                                ),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Text(
                                    text = "Get Started",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.SemiBold
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
fun RunningManAnimation(
    speedMultiplier: Float = 1.2f, 
    modifier: Modifier = Modifier,
    onCompositionLoaded: () -> Unit = {}
) {
    val composition by rememberLottieComposition(LottieCompositionSpec.Asset("running_man.lottie"))
    
    LaunchedEffect(composition) {
        if (composition != null) {
            onCompositionLoaded()
        }
    }
    
    LottieAnimation(
        composition = composition,
        isPlaying = true,
        iterations = LottieConstants.IterateForever,
        speed = speedMultiplier,
        modifier = modifier.size(1100.dp)
    )
}



@Composable
fun HealthWaveAnimation(tapIntensity: Float = 0f) {
    val infiniteTransition = rememberInfiniteTransition()
    
    val speedMultiplier = 1f + tapIntensity * 0.5f
    
    val waveOffset1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween((2500 / speedMultiplier).toInt(), easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )
    
    val waveOffset2 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween((3500 / speedMultiplier).toInt(), easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )
    
    val waveOffset3 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween((4500 / speedMultiplier).toInt(), easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )
    
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .alpha(0.35f)
    ) {
        val width = size.width
        val height = size.height
        val baseScale = 1.5f
        val pulseScale = baseScale + tapIntensity * 1.5f
        drawECGLine(width, height * 0.3f, waveOffset1, Color(0xFFFF6B6B), pulseScale, tapIntensity, ECGType.Normal)
        drawECGLine(width, height * 0.5f, waveOffset2, Secondary, pulseScale * 1.1f, tapIntensity, ECGType.Tachycardia)
        drawECGLine(width, height * 0.7f, waveOffset3, Color(0xFF4ECDC4), pulseScale, tapIntensity, ECGType.Bradycardia)
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawECGLine(
    width: Float,
    yPosition: Float,
    offset: Float,
    color: Color,
    scale: Float = 1f,
    tapIntensity: Float = 0f,
    ecgType: ECGType = ECGType.Normal
) {
    val segmentWidth = when (ecgType) {
        ECGType.Normal -> width / 1.5f
        ECGType.Tachycardia -> width / 2.2f
        ECGType.Bradycardia -> width / 1.0f
    }
    
    val dynamicScale = scale + (tapIntensity * 0.3f * kotlin.math.sin(offset * 2 * Math.PI).toFloat())
    val segmentRange = when (ecgType) {
        ECGType.Bradycardia -> -1..2
        else -> -1..3
    }
    
    for (segment in segmentRange) {
        val startX = segment * segmentWidth - offset * segmentWidth
        
        if (startX > width + segmentWidth || startX < -segmentWidth) continue
        
        val path = Path()
        
        when (ecgType) {
            ECGType.Normal -> drawNormalECG(path, startX, yPosition, segmentWidth, dynamicScale)
            ECGType.Tachycardia -> drawTachycardiaECG(path, startX, yPosition, segmentWidth, dynamicScale)
            ECGType.Bradycardia -> drawBradycardiaECG(path, startX, yPosition, segmentWidth, dynamicScale)
        }
        
        val strokeWidth = (1.5.dp.toPx() + tapIntensity * 1.dp.toPx()) * min(dynamicScale / scale, 1.1f)
        drawPath(
            path = path,
            color = color.copy(alpha = min(0.9f, 0.6f + tapIntensity * 0.15f)),
            style = Stroke(
                width = strokeWidth,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )
        
        if (tapIntensity > 1f) {
            drawPath(
                path = path,
                color = color.copy(alpha = 0.08f * (tapIntensity - 1f)),
                style = Stroke(
                    width = strokeWidth * 1.5f,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )
        }
    }
}

private fun drawNormalECG(path: Path, startX: Float, yPosition: Float, segmentWidth: Float, scale: Float) {
    path.moveTo(startX, yPosition)
    
    path.lineTo(startX + 15 * segmentWidth / 100, yPosition)
    path.cubicTo(
        startX + 18 * segmentWidth / 100, yPosition - (20f * scale),
        startX + 22 * segmentWidth / 100, yPosition - (20f * scale),
        startX + 25 * segmentWidth / 100, yPosition
    )
    
    // PR segment
    path.lineTo(startX + 35 * segmentWidth / 100, yPosition)
    
    // QRS complex
    path.lineTo(startX + 37 * segmentWidth / 100, yPosition + (8f * scale)) // Q
    path.lineTo(startX + 39 * segmentWidth / 100, yPosition - (80f * scale)) // R
    path.lineTo(startX + 41 * segmentWidth / 100, yPosition + (15f * scale)) // S
    path.lineTo(startX + 43 * segmentWidth / 100, yPosition)
    
    // ST segment
    path.lineTo(startX + 55 * segmentWidth / 100, yPosition)
    
    // T wave
    path.cubicTo(
        startX + 58 * segmentWidth / 100, yPosition - (30f * scale),
        startX + 65 * segmentWidth / 100, yPosition - (30f * scale),
        startX + 70 * segmentWidth / 100, yPosition
    )
    
    // End
    path.lineTo(startX + 100 * segmentWidth / 100, yPosition)
}

private fun drawTachycardiaECG(path: Path, startX: Float, yPosition: Float, segmentWidth: Float, scale: Float) {
    path.moveTo(startX, yPosition)
    
    // Compressed P wave
    path.cubicTo(
        startX + 8 * segmentWidth / 100, yPosition - (15f * scale),
        startX + 12 * segmentWidth / 100, yPosition - (15f * scale),
        startX + 15 * segmentWidth / 100, yPosition
    )
    
    // Quick QRS
    path.lineTo(startX + 25 * segmentWidth / 100, yPosition + (5f * scale))
    path.lineTo(startX + 27 * segmentWidth / 100, yPosition - (70f * scale))
    path.lineTo(startX + 29 * segmentWidth / 100, yPosition + (10f * scale))
    path.lineTo(startX + 31 * segmentWidth / 100, yPosition)
    
    // Compressed T wave
    path.cubicTo(
        startX + 35 * segmentWidth / 100, yPosition - (20f * scale),
        startX + 40 * segmentWidth / 100, yPosition - (20f * scale),
        startX + 45 * segmentWidth / 100, yPosition
    )
    
    // Next beat starts sooner
    path.lineTo(startX + 100 * segmentWidth / 100, yPosition)
}

private fun drawBradycardiaECG(path: Path, startX: Float, yPosition: Float, segmentWidth: Float, scale: Float) {
    path.moveTo(startX, yPosition)
    
    // Extended flat line
    path.lineTo(startX + 25 * segmentWidth / 100, yPosition)
    
    // P wave
    path.cubicTo(
        startX + 28 * segmentWidth / 100, yPosition - (18f * scale),
        startX + 32 * segmentWidth / 100, yPosition - (18f * scale),
        startX + 35 * segmentWidth / 100, yPosition
    )
    
    // Extended PR interval
    path.lineTo(startX + 45 * segmentWidth / 100, yPosition)
    
    // QRS complex
    path.lineTo(startX + 47 * segmentWidth / 100, yPosition + (8f * scale))
    path.lineTo(startX + 49 * segmentWidth / 100, yPosition - (75f * scale))
    path.lineTo(startX + 51 * segmentWidth / 100, yPosition + (15f * scale))
    path.lineTo(startX + 53 * segmentWidth / 100, yPosition)
    
    // Extended ST segment
    path.lineTo(startX + 65 * segmentWidth / 100, yPosition)
    
    // T wave
    path.cubicTo(
        startX + 68 * segmentWidth / 100, yPosition - (25f * scale),
        startX + 75 * segmentWidth / 100, yPosition - (25f * scale),
        startX + 80 * segmentWidth / 100, yPosition
    )
    
    // Long end segment
    path.lineTo(startX + 100 * segmentWidth / 100, yPosition)
}


@Composable
fun LiveHealthStats(tapIntensity: Float = 0f) {
    val statsTransition = rememberInfiniteTransition()
    
    val baseHeartRate = 72f + (tapIntensity * 40f)
    val targetHeartRate = 85f + (tapIntensity * 45f)
    val heartRate by statsTransition.animateFloat(
        initialValue = baseHeartRate,
        targetValue = targetHeartRate,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = (2000 / (1f + tapIntensity)).toInt(),
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        )
    )
    
    val baseSteps = 8420f + (tapIntensity * 100f)
    val targetSteps = 8450f + (tapIntensity * 150f)
    val steps by statsTransition.animateFloat(
        initialValue = baseSteps,
        targetValue = targetSteps,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = (3000 / (1f + tapIntensity * 0.5f)).toInt(),
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Reverse
        )
    )
    
    val baseSleep = maxOf(78f - tapIntensity * 5f, 60f)
    val targetSleep = maxOf(83f - tapIntensity * 5f, 65f)
    val sleepScore by statsTransition.animateFloat(
        initialValue = baseSleep,
        targetValue = targetSleep,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        LiveStatItem(
            label = "BPM",
            value = heartRate.toInt().toString(),
            color = Color(0xFFFF6B6B)
        )
        LiveStatItem(
            label = "Steps",
            value = steps.toInt().toString(),
            color = Color(0xFF4ECDC4)
        )
        LiveStatItem(
            label = "Sleep",
            value = "${sleepScore.toInt()}%",
            color = Color(0xFF45B7D1)
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SwipeableMarketingCards() {
    val pagerState = rememberPagerState(pageCount = { 3 })
    
    val marketingData = listOf(
        Triple("AI-powered health insights and smart suggestions", "ai_insights.lottie", Color(0xFF9C27B0)),
        Triple("Smart health analysis made effortless with data trends", "health_analysis.lottie", Color(0xFF2196F3)), 
        Triple("Private and secure - all data stays safely on device", "security_lock.lottie", Color(0xFF4CAF50))
    )
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .padding(horizontal = 16.dp)
        ) { page ->
            val (message, lottieFile, cardColor) = marketingData[page]
            
            Card(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 4.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = cardColor.copy(alpha = 0.1f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    MarketingCardLottie(
                        lottieFile = lottieFile,
                        modifier = Modifier.size(130.dp)
                    )
                    
                    Text(
                        text = message,
                        fontSize = 14.sp,
                        color = OnBackground,
                        textAlign = TextAlign.Start,
                        fontWeight = FontWeight.Medium,
                        lineHeight = 20.sp,
                        modifier = Modifier.weight(1f),
                        softWrap = true,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Visible
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(3) { index ->
                val isSelected = pagerState.currentPage == index
                Box(
                    modifier = Modifier
                        .size(if (isSelected) 12.dp else 8.dp)
                        .background(
                            color = if (isSelected) Secondary else Secondary.copy(alpha = 0.3f),
                            shape = CircleShape
                        )
                        .animateContentSize()
                )
            }
        }
    }
}

@Composable
fun MarketingCardLottie(
    lottieFile: String,
    modifier: Modifier = Modifier
) {
    val composition by rememberLottieComposition(LottieCompositionSpec.Asset(lottieFile))
    
    LottieAnimation(
        composition = composition,
        isPlaying = true,
        iterations = LottieConstants.IterateForever,
        speed = 1f,
        modifier = modifier
    )
}

@Composable
fun LiveStatItem(label: String, value: String, color: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = TextSecondary
        )
    }
}