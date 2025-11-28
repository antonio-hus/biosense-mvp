package com.biosense.app.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.airbnb.lottie.compose.*
import com.biosense.app.data.model.Gender
import com.biosense.app.data.model.HealthGoal
import com.biosense.app.data.model.MotivationStyle
import com.biosense.app.ui.theme.*
import com.biosense.app.viewmodel.UserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountScreen(
    onNavigateBack: () -> Unit,
    userViewModel: UserViewModel = viewModel()
) {
    val user by userViewModel.currentUser
    var isEditing by remember { mutableStateOf(false) }
    var profileImageUri by remember(user.profilePicturePath) {
        mutableStateOf(user.profilePicturePath?.let { Uri.parse(it) })
    }

    var name by remember(user) { mutableStateOf(user.name) }
    var age by remember(user) { mutableStateOf(user.age.toString()) }
    var height by remember(user) { mutableStateOf(user.height.toString()) }
    var weight by remember(user) { mutableStateOf(user.weight.toString()) }
    var profession by remember(user) { mutableStateOf(user.profession) }
    var selectedGender by remember(user) { mutableStateOf(user.gender) }
    var selectedHealthGoal by remember(user) { mutableStateOf(user.healthGoal) }
    var selectedMotivation by remember(user) { mutableStateOf(user.motivationStyle) }
    var whatSenseKnows by remember(user) { mutableStateOf(user.whatSenseKnows) }

    val scrollState = rememberScrollState()

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        profileImageUri = uri
        userViewModel.updateProfilePicture(uri?.toString())
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
            ProfileHeader(
                isEditing = isEditing,
                onEditToggle = { isEditing = !isEditing },
                onNavigateBack = onNavigateBack,
                userName = name,
                profileImageUri = profileImageUri,
                onImagePick = { imagePickerLauncher.launch("image/*") }
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 24.dp)
            ) {
                // --- Personal Information Card ---
                AnimatedVisibility(
                    visible = true,
                    enter = slideInVertically(
                        initialOffsetY = { 100 },
                        animationSpec = tween(500, delayMillis = 100)
                    ) + fadeIn()
                ) {
                    ModernInfoCard(
                        title = "Personal Information",
                        icon = Icons.Default.Person,
                        isEditing = isEditing
                    ) {
                        PersonalInfoSection(
                            name = name,
                            onNameChange = { name = it },
                            age = age,
                            onAgeChange = { if (it.all { char -> char.isDigit() }) age = it },
                            height = height,
                            onHeightChange = { if (it.all { char -> char.isDigit() }) height = it },
                            weight = weight,
                            onWeightChange = { if (it.all { char -> char.isDigit() }) weight = it },
                            profession = profession,
                            onProfessionChange = { profession = it },
                            gender = selectedGender,
                            onGenderChange = { selectedGender = it },
                            isEditing = isEditing
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // --- Health Preferences Card (UPDATED) ---
                AnimatedVisibility(
                    visible = true,
                    enter = slideInVertically(
                        initialOffsetY = { 100 },
                        animationSpec = tween(500, delayMillis = 200)
                    ) + fadeIn()
                ) {
                    ModernInfoCard(
                        title = "Health Preferences",
                        icon = Icons.Default.FavoriteBorder,
                        isEditing = isEditing
                    ) {
                        if (isEditing) {
                            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                SelectionDropdown(
                                    label = "Health Goal",
                                    options = HealthGoal.values().toList(), // Assuming Enum
                                    selectedOption = selectedHealthGoal,
                                    onOptionSelected = { selectedHealthGoal = it },
                                    displayName = { it.displayName }
                                )

                                SelectionDropdown(
                                    label = "Motivation Style",
                                    options = MotivationStyle.values().toList(), // Assuming Enum
                                    selectedOption = selectedMotivation,
                                    onOptionSelected = { selectedMotivation = it },
                                    displayName = { it.displayName }
                                )
                            }
                        } else {
                            Column {
                                ModernInfoRow("Health Goal", selectedHealthGoal.displayName, Icons.Default.Flag)
                                Spacer(modifier = Modifier.height(12.dp))
                                ModernInfoRow("Motivation Style", selectedMotivation.displayName, Icons.Default.Psychology)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // --- AI Context Card ---
                AnimatedVisibility(
                    visible = true,
                    enter = slideInVertically(
                        initialOffsetY = { 100 },
                        animationSpec = tween(500, delayMillis = 300)
                    ) + fadeIn()
                ) {
                    ModernInfoCard(
                        title = "AI Context",
                        icon = Icons.Default.AutoAwesome,
                        isEditing = isEditing
                    ) {
                        if (isEditing) {
                            OutlinedTextField(
                                value = whatSenseKnows,
                                onValueChange = { whatSenseKnows = it },
                                label = { Text("Personal context for AI insights") },
                                placeholder = { Text("e.g., I work night shifts, prefer outdoor activities, have a gym nearby...") },
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Secondary,
                                    unfocusedBorderColor = SurfaceVariant
                                ),
                                minLines = 3,
                                maxLines = 5,
                                shape = RoundedCornerShape(12.dp)
                            )
                        } else {
                            Text(
                                text = whatSenseKnows.ifEmpty { "Add personal context to get customized AI insights" },
                                fontSize = 16.sp,
                                color = if (whatSenseKnows.isEmpty()) OnSurfaceVariant else OnSurface,
                                lineHeight = 24.sp
                            )
                        }
                    }
                }

                // --- Save Button ---
                if (isEditing) {
                    Spacer(modifier = Modifier.height(32.dp))

                    AnimatedVisibility(
                        visible = isEditing,
                        enter = scaleIn() + fadeIn()
                    ) {
                        Button(
                            onClick = {
                                userViewModel.updateUser(
                                    user.copy(
                                        name = name,
                                        age = age.toIntOrNull() ?: 0,
                                        height = height.toIntOrNull() ?: 0,
                                        weight = weight.toIntOrNull() ?: 0,
                                        profession = profession,
                                        gender = selectedGender,
                                        healthGoal = selectedHealthGoal,
                                        motivationStyle = selectedMotivation,
                                        whatSenseKnows = whatSenseKnows,
                                        profilePicturePath = profileImageUri?.toString()
                                    )
                                )
                                isEditing = false
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Secondary,
                                contentColor = OnSecondary
                            ),
                            shape = RoundedCornerShape(16.dp),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
                        ) {
                            Icon(
                                Icons.Default.Save,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Save Changes",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> SelectionDropdown(
    label: String,
    options: List<T>,
    selectedOption: T,
    onOptionSelected: (T) -> Unit,
    displayName: (T) -> String
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            readOnly = true,
            value = displayName(selectedOption),
            onValueChange = {},
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Secondary,
                unfocusedBorderColor = SurfaceVariant
            ),
            shape = RoundedCornerShape(12.dp)
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(displayName(option)) },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                )
            }
        }
    }
}

@Composable
private fun ProfileHeader(
    isEditing: Boolean,
    onEditToggle: () -> Unit,
    onNavigateBack: () -> Unit,
    userName: String,
    profileImageUri: Uri?,
    onImagePick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp)
    ) {
        UpdateAnimation()

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Background.copy(alpha = 0.3f),
                            Background.copy(alpha = 0.8f)
                        )
                    )
                )
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .statusBarsPadding(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier
                    .background(
                        Background.copy(alpha = 0.8f),
                        CircleShape
                    )
            ) {
                Icon(
                    Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = OnBackground
                )
            }

            IconButton(
                onClick = onEditToggle,
                modifier = Modifier
                    .background(
                        if (isEditing) Secondary.copy(alpha = 0.9f) else Background.copy(alpha = 0.8f),
                        CircleShape
                    )
            ) {
                Icon(
                    if (isEditing) Icons.Default.Check else Icons.Default.Edit,
                    contentDescription = "Edit",
                    tint = if (isEditing) OnSecondary else OnBackground
                )
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Secondary.copy(alpha = 0.1f),
                                Primary.copy(alpha = 0.05f)
                            )
                        )
                    )
                    .border(3.dp, Secondary.copy(alpha = 0.3f), CircleShape)
                    .clickable { if (isEditing) onImagePick() }, // Only clickable if editing? Optional logic
                contentAlignment = Alignment.Center
            ) {
                if (profileImageUri != null) {
                    AsyncImage(
                        model = profileImageUri,
                        contentDescription = "Profile Picture",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = "Profile",
                        modifier = Modifier.size(50.dp),
                        tint = Secondary
                    )
                }

                if (isEditing) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Background.copy(alpha = 0.6f),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.CameraAlt,
                            contentDescription = "Upload Photo",
                            modifier = Modifier.size(24.dp),
                            tint = OnBackground
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = userName.ifEmpty { "Your Name" },
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = OnBackground,
                textAlign = TextAlign.Center
            )

            Text(
                text = "BioSense User",
                fontSize = 16.sp,
                color = OnBackground.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun UpdateAnimation() {
    // Using a placeholder loader if lottie isn't strictly required or file missing
    // Re-using your logic
    val composition by rememberLottieComposition(LottieCompositionSpec.Asset("profile_background.lottie"))
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = LottieConstants.IterateForever,
        speed = 0.5f
    )

    LottieAnimation(
        composition = composition,
        progress = { progress },
        modifier = Modifier
            .fillMaxSize()
            .alpha(0.3f),
        contentScale = ContentScale.Crop
    )
}

@Composable
private fun ModernInfoCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isEditing: Boolean,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Surface.copy(alpha = 0.9f)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isEditing) 12.dp else 6.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            Secondary.copy(alpha = 0.1f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        icon,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = Secondary
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = OnSurface
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            content()
        }
    }
}

@Composable
private fun PersonalInfoSection(
    name: String,
    onNameChange: (String) -> Unit,
    age: String,
    onAgeChange: (String) -> Unit,
    height: String,
    onHeightChange: (String) -> Unit,
    weight: String,
    onWeightChange: (String) -> Unit,
    profession: String,
    onProfessionChange: (String) -> Unit,
    gender: Gender,
    onGenderChange: (Gender) -> Unit,
    isEditing: Boolean
) {
    Column {
        if (isEditing) {
            OutlinedTextField(
                value = name,
                onValueChange = onNameChange,
                label = { Text("Name") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Secondary,
                    unfocusedBorderColor = SurfaceVariant
                ),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = age,
                    onValueChange = onAgeChange,
                    label = { Text("Age") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = height,
                    onValueChange = onHeightChange,
                    label = { Text("Height (cm)") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = weight,
                    onValueChange = onWeightChange,
                    label = { Text("Weight (kg)") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp)
                )

                // Added Gender dropdown for completeness in edit mode
                Box(modifier = Modifier.weight(1f)) {
                    SelectionDropdown(
                        label = "Gender",
                        options = Gender.values().toList(),
                        selectedOption = gender,
                        onOptionSelected = onGenderChange,
                        displayName = { it.displayName }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = profession,
                onValueChange = onProfessionChange,
                label = { Text("Profession") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
        } else {
            ModernInfoRow("Name", name.ifEmpty { "Not set" }, Icons.Default.Badge)
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    ModernInfoRow("Age", if (age == "0") "Not set" else "$age years", Icons.Default.Cake)
                }
                Column(modifier = Modifier.weight(1f)) {
                    ModernInfoRow("Height", if (height == "0") "Not set" else "$height cm", Icons.Default.Height)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    ModernInfoRow("Weight", if (weight == "0") "Not set" else "$weight kg", Icons.Default.MonitorWeight)
                }
                Column(modifier = Modifier.weight(1f)) {
                    GenderInfoRow(gender = gender)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            ModernInfoRow("Profession", profession.ifEmpty { "Not set" }, Icons.Default.Work)
        }
    }
}

@Composable
private fun ModernInfoRow(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = Secondary.copy(alpha = 0.7f)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                fontSize = 12.sp,
                color = OnSurface.copy(alpha = 0.6f),
                fontWeight = FontWeight.Medium
            )
            Text(
                text = value,
                fontSize = 16.sp,
                color = OnSurface,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun GenderInfoRow(gender: Gender) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        val genderIcon = when (gender) {
            Gender.MALE -> Icons.Default.Male
            Gender.FEMALE -> Icons.Default.Female
            Gender.OTHER -> Icons.Default.Transgender
            Gender.NOT_SPECIFIED -> Icons.Default.QuestionMark
        }

        Icon(
            genderIcon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = Secondary.copy(alpha = 0.7f)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Gender",
                fontSize = 12.sp,
                color = OnSurface.copy(alpha = 0.6f),
                fontWeight = FontWeight.Medium
            )
            Icon(
                genderIcon,
                contentDescription = gender.displayName,
                modifier = Modifier.size(24.dp),
                tint = Secondary
            )
        }
    }
}
