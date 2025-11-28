package com.biosense.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.imePadding
import com.airbnb.lottie.compose.*
import com.biosense.app.ui.theme.*
import com.biosense.app.viewmodel.UserViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.biosense.app.data.model.Gender
import com.biosense.app.data.model.HealthGoal
import com.biosense.app.data.model.MotivationStyle
import com.biosense.app.data.model.User

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun CreateAccountScreen(
    onComplete: () -> Unit,
    userViewModel: UserViewModel = viewModel()
) {
    var currentStep by remember { mutableStateOf(0) }
    val totalSteps = 5
    
    var name by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var height by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var selectedGender by remember { mutableStateOf(Gender.NOT_SPECIFIED) }
    var profession by remember { mutableStateOf("") }
    var selectedHealthGoal by remember { mutableStateOf(HealthGoal.ENERGY) }
    var selectedMotivation by remember { mutableStateOf(MotivationStyle.ENCOURAGEMENT) }
    var whatSenseKnows by remember { mutableStateOf("") }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Background,
                        Background.copy(alpha = 0.95f),
                        Primary.copy(alpha = 0.05f)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            TopAppBar(
                title = { 
                    Text(
                        text = when(currentStep) {
                            0 -> "Welcome"
                            1 -> "Basic Information"
                            2 -> "Physical Details"
                            3 -> "Your Goals"
                            4 -> "Personalization"
                            else -> "Account Setup"
                        },
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    if (currentStep > 0) {
                        IconButton(onClick = { currentStep-- }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
            
            StepProgressIndicator(
                currentStep = currentStep,
                totalSteps = totalSteps,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
            
            AnimatedContent(
                targetState = currentStep,
                transitionSpec = {
                    if (targetState > initialState) {
                        slideInHorizontally(
                            animationSpec = tween(
                                durationMillis = 300,
                                easing = FastOutSlowInEasing
                            )
                        ) { it } + fadeIn(
                            animationSpec = tween(250)
                        ) with slideOutHorizontally(
                            animationSpec = tween(
                                durationMillis = 250,
                                easing = FastOutSlowInEasing
                            )
                        ) { -it } + fadeOut(
                            animationSpec = tween(200)
                        )
                    } else {
                        slideInHorizontally(
                            animationSpec = tween(
                                durationMillis = 300,
                                easing = FastOutSlowInEasing
                            )
                        ) { -it } + fadeIn(
                            animationSpec = tween(250)
                        ) with slideOutHorizontally(
                            animationSpec = tween(
                                durationMillis = 250,
                                easing = FastOutSlowInEasing
                            )
                        ) { it } + fadeOut(
                            animationSpec = tween(200)
                        )
                    }
                },
                modifier = Modifier.weight(1f)
            ) { step ->
                when (step) {
                    0 -> WelcomeStep(
                        onNext = { currentStep = 1 }
                    )
                    1 -> BasicInfoStep(
                        name = name,
                        selectedGender = selectedGender,
                        profession = profession,
                        onNameChange = { name = it },
                        onGenderChange = { selectedGender = it },
                        onProfessionChange = { profession = it },
                        onNext = { currentStep = 2 }
                    )
                    2 -> PhysicalDetailsStep(
                        age = age,
                        height = height,
                        weight = weight,
                        onAgeChange = { age = it },
                        onHeightChange = { height = it },
                        onWeightChange = { weight = it },
                        onNext = { currentStep = 3 }
                    )
                    3 -> GoalsStep(
                        selectedHealthGoal = selectedHealthGoal,
                        selectedMotivation = selectedMotivation,
                        onHealthGoalChange = { selectedHealthGoal = it },
                        onMotivationChange = { selectedMotivation = it },
                        onNext = { currentStep = 4 }
                    )
                    4 -> PersonalizationStep(
                        whatSenseKnows = whatSenseKnows,
                        onWhatSenseKnowsChange = { whatSenseKnows = it },
                        onComplete = {
                            userViewModel.createUser(
                                User(
                                    name = name,
                                    age = age.toIntOrNull() ?: 0,
                                    height = height.toIntOrNull() ?: 0,
                                    weight = weight.toIntOrNull() ?: 0,
                                    gender = selectedGender,
                                    profession = profession,
                                    healthGoal = selectedHealthGoal,
                                    motivationStyle = selectedMotivation,
                                    whatSenseKnows = whatSenseKnows
                                )
                            )
                            onComplete()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun StepProgressIndicator(
    currentStep: Int,
    totalSteps: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        for (step in 0 until totalSteps) {
            val progress by animateFloatAsState(
                targetValue = if (step <= currentStep) 1f else 0f,
                animationSpec = tween(
                    durationMillis = 400,
                    easing = FastOutSlowInEasing
                )
            )
            
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(4.dp)
                    .padding(horizontal = 2.dp)
                    .background(
                        OnSurfaceVariant.copy(alpha = 0.3f),
                        RoundedCornerShape(2.dp)
                    )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress)
                        .height(4.dp)
                        .background(
                            Secondary,
                            RoundedCornerShape(2.dp)
                        )
                )
            }
        }
    }
}

@Composable
fun WelcomeStep(onNext: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        val composition by rememberLottieComposition(LottieCompositionSpec.Asset("profile_setup.lottie"))
        val progress by animateLottieCompositionAsState(
            composition = composition,
            iterations = LottieConstants.IterateForever,
            isPlaying = true,
            speed = 1f,
            restartOnPlay = false
        )
        
        LottieAnimation(
            composition = composition,
            progress = { progress },
            modifier = Modifier.size(280.dp)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Let's Get Started",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Text
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Text(
            text = "We'll help you set up your personalized health profile in just a few steps",
            fontSize = 16.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = onNext,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Secondary
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = "Create Profile",
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BasicInfoStep(
    name: String,
    selectedGender: Gender,
    profession: String,
    onNameChange: (String) -> Unit,
    onGenderChange: (Gender) -> Unit,
    onProfessionChange: (String) -> Unit,
    onNext: () -> Unit
) {
    val scrollState = rememberScrollState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(scrollState)
            .padding(24.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Person,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = Secondary
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Text(
            text = "Tell us about yourself",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Text
        )
        
        Text(
            text = "This helps us personalize your experience",
            fontSize = 16.sp,
            color = TextSecondary,
            modifier = Modifier.padding(top = 8.dp)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            label = { Text("Full Name") },
            leadingIcon = {
                Icon(Icons.Default.Person, contentDescription = null)
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Secondary,
                unfocusedBorderColor = OnSurfaceVariant.copy(alpha = 0.3f)
            )
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        var genderExpanded by remember { mutableStateOf(false) }
        
        Text(
            text = "Gender",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = Text,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Gender.values().forEach { gender ->
                val isSelected = selectedGender == gender
                val genderIcon = when (gender) {
                    Gender.MALE -> Icons.Default.Male
                    Gender.FEMALE -> Icons.Default.Female
                    Gender.OTHER -> Icons.Default.Transgender
                    Gender.NOT_SPECIFIED -> Icons.Default.QuestionMark
                }
                
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onGenderChange(gender) },
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) Secondary.copy(alpha = 0.1f) else Surface
                    ),
                    border = if (isSelected) BorderStroke(2.dp, Secondary) else null,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            genderIcon,
                            contentDescription = gender.displayName,
                            modifier = Modifier.size(32.dp),
                            tint = if (isSelected) Secondary else OnSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = when (gender) {
                                Gender.MALE -> "M"
                                Gender.FEMALE -> "F"
                                Gender.OTHER -> "O"
                                Gender.NOT_SPECIFIED -> "N/S"
                            },
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                            color = if (isSelected) Secondary else OnSurfaceVariant
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        OutlinedTextField(
            value = profession,
            onValueChange = onProfessionChange,
            label = { Text("Profession / Work Type") },
            leadingIcon = {
                Icon(Icons.Default.Work, contentDescription = null)
            },
            placeholder = { Text("e.g., Software Engineer, Teacher, Doctor") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Secondary,
                unfocusedBorderColor = OnSurfaceVariant.copy(alpha = 0.3f)
            )
        )
        
        Spacer(modifier = Modifier.height(20.dp))
        
        Button(
            onClick = onNext,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Secondary
            ),
            shape = RoundedCornerShape(16.dp),
            enabled = name.isNotEmpty()
        ) {
            Text(
                text = "Continue",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun PhysicalDetailsStep(
    age: String,
    height: String,
    weight: String,
    onAgeChange: (String) -> Unit,
    onHeightChange: (String) -> Unit,
    onWeightChange: (String) -> Unit,
    onNext: () -> Unit
) {
    val scrollState = rememberScrollState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(scrollState)
            .padding(24.dp)
    ) {
        Icon(
            imageVector = Icons.Default.FitnessCenter,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = Secondary
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Text(
            text = "Physical Details",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Text
        )
        
        Text(
            text = "Help us calculate accurate health metrics",
            fontSize = 16.sp,
            color = TextSecondary,
            modifier = Modifier.padding(top = 8.dp)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        OutlinedTextField(
            value = age,
            onValueChange = { 
                val filtered = it.filter { char -> char.isDigit() }
                if (filtered.length <= 3) {
                    val ageInt = filtered.toIntOrNull() ?: 0
                    if (ageInt <= 120) onAgeChange(filtered)
                }
            },
            label = { Text("Age") },
            leadingIcon = {
                Icon(Icons.Default.Cake, contentDescription = null)
            },
            suffix = { Text("years") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Secondary,
                unfocusedBorderColor = OnSurfaceVariant.copy(alpha = 0.3f)
            ),
            isError = age.isNotEmpty() && (age.toIntOrNull() ?: 0) !in 1..120,
            supportingText = if (age.isNotEmpty() && (age.toIntOrNull() ?: 0) !in 1..120) {
                { Text("Age must be between 1 and 120", color = Error) }
            } else null
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        OutlinedTextField(
            value = height,
            onValueChange = { 
                val filtered = it.filter { char -> char.isDigit() }
                if (filtered.length <= 3) {
                    val heightInt = filtered.toIntOrNull() ?: 0
                    if (heightInt <= 300) onHeightChange(filtered)
                }
            },
            label = { Text("Height") },
            leadingIcon = {
                Icon(Icons.Default.Height, contentDescription = null)
            },
            suffix = { Text("cm") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Secondary,
                unfocusedBorderColor = OnSurfaceVariant.copy(alpha = 0.3f)
            ),
            isError = height.isNotEmpty() && (height.toIntOrNull() ?: 0) !in 50..300,
            supportingText = if (height.isNotEmpty() && (height.toIntOrNull() ?: 0) !in 50..300) {
                { Text("Height must be between 50 and 300 cm", color = Error) }
            } else null
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        OutlinedTextField(
            value = weight,
            onValueChange = { 
                val filtered = it.filter { char -> char.isDigit() }
                if (filtered.length <= 3) {
                    val weightInt = filtered.toIntOrNull() ?: 0
                    if (weightInt <= 500) onWeightChange(filtered)
                }
            },
            label = { Text("Weight") },
            leadingIcon = {
                Icon(Icons.Default.MonitorWeight, contentDescription = null)
            },
            suffix = { Text("kg") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Secondary,
                unfocusedBorderColor = OnSurfaceVariant.copy(alpha = 0.3f)
            ),
            isError = weight.isNotEmpty() && (weight.toIntOrNull() ?: 0) !in 20..500,
            supportingText = if (weight.isNotEmpty() && (weight.toIntOrNull() ?: 0) !in 20..500) {
                { Text("Weight must be between 20 and 500 kg", color = Error) }
            } else null
        )
        
        val heightInt = height.toIntOrNull() ?: 0
        val weightInt = weight.toIntOrNull() ?: 0
        if (heightInt in 50..300 && weightInt in 20..500) {
            val bmi = weightInt.toFloat() / ((heightInt.toFloat() / 100) * (heightInt.toFloat() / 100))
            val bmiCategory = when {
                bmi < 18.5 -> "Underweight"
                bmi < 25 -> "Normal weight"
                bmi < 30 -> "Overweight"
                else -> "Obese"
            }
            val bmiColor = when {
                bmi < 18.5 -> Color(0xFFFFA726)
                bmi < 25 -> Color(0xFF66BB6A)
                bmi < 30 -> Color(0xFFFFA726)
                else -> Color(0xFFEF5350)
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = bmiColor.copy(alpha = 0.1f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Your BMI",
                            fontSize = 14.sp,
                            color = TextSecondary
                        )
                        Text(
                            text = String.format("%.1f", bmi),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = bmiColor
                        )
                    }
                    Text(
                        text = bmiCategory,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = bmiColor
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(20.dp))
        
        val isFormValid = (age.isEmpty() || age.toIntOrNull() in 1..120) &&
            (height.isEmpty() || height.toIntOrNull() in 50..300) &&
            (weight.isEmpty() || weight.toIntOrNull() in 20..500)
        
        Button(
            onClick = onNext,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Secondary
            ),
            shape = RoundedCornerShape(16.dp),
            enabled = isFormValid
        ) {
            Text(
                text = "Continue",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalsStep(
    selectedHealthGoal: HealthGoal,
    selectedMotivation: MotivationStyle,
    onHealthGoalChange: (HealthGoal) -> Unit,
    onMotivationChange: (MotivationStyle) -> Unit,
    onNext: () -> Unit
) {
    val scrollState = rememberScrollState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(scrollState)
            .padding(24.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Star,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = Secondary
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Text(
            text = "Your Health Goals",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Text
        )
        
        Text(
            text = "What would you like to focus on?",
            fontSize = 16.sp,
            color = TextSecondary,
            modifier = Modifier.padding(top = 8.dp)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Primary Health Goal",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = Text,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        HealthGoal.values().forEach { goal ->
            val isSelected = selectedHealthGoal == goal
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable { onHealthGoalChange(goal) },
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) Secondary.copy(alpha = 0.1f) else Surface
                ),
                border = if (isSelected) BorderStroke(2.dp, Secondary) else null,
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = when(goal) {
                                HealthGoal.PREVENTIVE_CARE -> Icons.Default.MonitorHeart
                                HealthGoal.SLEEP_QUALITY -> Icons.Default.Bedtime
                                HealthGoal.ENERGY -> Icons.Default.BatteryChargingFull
                                HealthGoal.RECOVERY -> Icons.Default.Healing
                                HealthGoal.WEIGHT_MANAGEMENT -> Icons.Default.MonitorWeight
                                HealthGoal.STRESS_RESILIENCE -> Icons.Default.SelfImprovement
                                HealthGoal.FITNESS_IMPROVEMENT -> Icons.Default.FitnessCenter
                            },
                            contentDescription = null,
                            tint = if (isSelected) Secondary else OnSurfaceVariant,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = goal.displayName,
                            fontSize = 16.sp,
                            fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                            color = if (isSelected) Secondary else Text
                        )
                    }
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Secondary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Motivation Style",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = Text,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        Text(
            text = "How would you like to receive insights?",
            fontSize = 14.sp,
            color = TextSecondary,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        MotivationStyle.values().forEach { style ->
            val isSelected = selectedMotivation == style
            val description = when(style) {
                MotivationStyle.CASUAL -> "Friendly and relaxed tone"
                MotivationStyle.ENCOURAGEMENT -> "Positive and motivating messages"
                MotivationStyle.DIRECT -> "Straightforward and to the point"
                MotivationStyle.SCIENTIFIC -> "Data-driven with technical details"
            }
            
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable { onMotivationChange(style) },
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) Secondary.copy(alpha = 0.1f) else Surface
                ),
                border = if (isSelected) BorderStroke(2.dp, Secondary) else null,
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = style.displayName,
                            fontSize = 16.sp,
                            fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                            color = if (isSelected) Secondary else Text
                        )
                        Text(
                            text = description,
                            fontSize = 14.sp,
                            color = OnSurfaceVariant
                        )
                    }
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Secondary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(20.dp))
        
        Button(
            onClick = onNext,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Secondary
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = "Continue",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun PersonalizationStep(
    whatSenseKnows: String,
    onWhatSenseKnowsChange: (String) -> Unit,
    onComplete: () -> Unit
) {
    var isCompleting by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .padding(24.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Psychology,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = Secondary
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Text(
            text = "Help AI Understand You",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Text
        )
        
        Text(
            text = "Share context about your lifestyle for personalized insights",
            fontSize = 16.sp,
            color = TextSecondary,
            modifier = Modifier.padding(top = 8.dp)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = Primary.copy(alpha = 0.05f)
            )
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = Secondary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Why this matters",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Secondary
                    )
                    Text(
                        text = "The more context you provide, the better AI can tailor recommendations to your unique situation",
                        fontSize = 14.sp,
                        color = OnSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        OutlinedTextField(
            value = whatSenseKnows,
            onValueChange = onWhatSenseKnowsChange,
            label = { Text("What would you like Biosense to know about you?") },
            placeholder = { 
                Text(
                    "Example: I work night shifts, have a gym nearby, prefer outdoor activities on weekends, vegetarian diet, training for a marathon..."
                ) 
            },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Secondary,
                unfocusedBorderColor = OnSurfaceVariant.copy(alpha = 0.3f)
            ),
            minLines = 5
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Text(
            text = "You can always update this later in your profile settings",
            fontSize = 12.sp,
            color = OnSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = {
                isCompleting = true
                onComplete()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF4ECDC4)
            ),
            shape = RoundedCornerShape(16.dp),
            enabled = !isCompleting
        ) {
            if (isCompleting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = OnSecondary
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Complete Setup",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
        
        TextButton(
            onClick = onComplete,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Skip for now",
                fontSize = 16.sp,
                color = OnSurfaceVariant
            )
        }
    }
}