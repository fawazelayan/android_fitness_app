package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.IntakeViewModel
import java.util.Locale

@Composable
fun ProfileScreen(
    viewModel: IntakeViewModel,
    isDarkMode: Boolean,
    modifier: Modifier = Modifier
) {
    val activeProfile by viewModel.activeProfile.collectAsStateWithLifecycle()
    val profile1Name by viewModel.profile1Name.collectAsStateWithLifecycle()
    val profile2Name by viewModel.profile2Name.collectAsStateWithLifecycle()
    val gender by viewModel.gender.collectAsStateWithLifecycle()
    val age by viewModel.age.collectAsStateWithLifecycle()
    val height by viewModel.height.collectAsStateWithLifecycle()
    val weight by viewModel.weight.collectAsStateWithLifecycle()
    val activityLevel by viewModel.activityLevel.collectAsStateWithLifecycle()
    val fitnessGoal by viewModel.fitnessGoal.collectAsStateWithLifecycle()

    val themeBg = if (isDarkMode) Color(0xFF140F0D) else Color(0xFFFDF8F6)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(themeBg),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ProfileMainDashboardCard(
            isDarkMode = isDarkMode,
            activeProfile = activeProfile,
            profile1Name = profile1Name,
            profile2Name = profile2Name,
            onSwitchProfile = { viewModel.switchProfile(it) },
            onRenameProfile = { id, name -> viewModel.renameProfile(id, name) },
            gender = gender,
            age = age,
            height = height,
            weight = weight,
            activityLevel = activityLevel,
            fitnessGoal = fitnessGoal,
            onSavePhysicalProfile = { gen, ag, h, w, act, goal ->
                viewModel.updatePhysicalProfile(gen, ag, h, w, act, goal)
            },
            onApplyCalculatedGoals = { cal, prot, carb, fat, water ->
                viewModel.applyCalculatedGoals(cal, prot, carb, fat, water)
            },
            onClearAllHistory = { viewModel.clearAllHistory() }
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun ProfileMainDashboardCard(
    isDarkMode: Boolean,
    activeProfile: String,
    profile1Name: String,
    profile2Name: String,
    onSwitchProfile: (String) -> Unit,
    onRenameProfile: (String, String) -> Unit,
    gender: String,
    age: String,
    height: String,
    weight: String,
    activityLevel: String,
    fitnessGoal: String,
    onSavePhysicalProfile: (String, String, String, String, String, String) -> Unit,
    onApplyCalculatedGoals: (Double, Double, Double, Double, Double) -> Unit,
    onClearAllHistory: () -> Unit
) {
    var inputGender by remember { mutableStateOf(gender) }
    var inputAge by remember { mutableStateOf(age) }
    var inputHeight by remember { mutableStateOf(height) }
    var inputWeight by remember { mutableStateOf(weight) }
    var inputActivity by remember { mutableStateOf(activityLevel) }
    var inputFitnessGoal by remember { mutableStateOf(fitnessGoal) }

    LaunchedEffect(gender, age, height, weight, activityLevel, fitnessGoal) {
        inputGender = gender
        inputAge = age
        inputHeight = height
        inputWeight = weight
        inputActivity = activityLevel
        inputFitnessGoal = fitnessGoal
    }

    LaunchedEffect(inputGender, inputAge, inputHeight, inputWeight, inputActivity, inputFitnessGoal) {
        kotlinx.coroutines.delay(600)
        if (inputGender != gender || inputAge != age || inputHeight != height || inputWeight != weight || inputActivity != activityLevel || inputFitnessGoal != fitnessGoal) {
            onSavePhysicalProfile(inputGender, inputAge, inputHeight, inputWeight, inputActivity, inputFitnessGoal)
        }
    }

    var isEditingName by remember { mutableStateOf(false) }
    val activeName = if (activeProfile == "profile_1") profile1Name else profile2Name
    var nameInput by remember(activeName) { mutableStateOf(activeName) }

    var showWipeConfirmation by remember { mutableStateOf(false) }
    var showSuccessBanner by remember { mutableStateOf(false) }

    var activityExpanded by remember { mutableStateOf(false) }

    val activitiesList = remember { listOf("Sedentary", "Lightly Active", "Moderately Active", "Very Active", "Extra Active") }
    val gendersList = remember { listOf("Male", "Female", "Other") }
    val goalsList = remember { listOf("Lose Weight", "Maintain", "Build Muscle") }

    val parsedAge = inputAge.toIntOrNull() ?: 25
    val parsedHeight = inputHeight.toDoubleOrNull() ?: 175.0
    val parsedWeight = inputWeight.toDoubleOrNull() ?: 70.0

    val bmr = remember(inputGender, parsedAge, parsedHeight, parsedWeight) {
        if (inputGender == "Female") {
            447.593 + (9.247 * parsedWeight) + (3.098 * parsedHeight) - (4.330 * parsedAge)
        } else {
            88.362 + (13.397 * parsedWeight) + (4.799 * parsedHeight) - (5.677 * parsedAge)
        }
    }

    val activityMultiplier = when (inputActivity) {
        "Sedentary" -> 1.2
        "Lightly Active" -> 1.375
        "Moderately Active" -> 1.55
        "Very Active" -> 1.725
        "Extra Active" -> 1.9
        else -> 1.55
    }

    val tdee = bmr * activityMultiplier

    val targetCalories = when (inputFitnessGoal) {
        "Lose Weight" -> (tdee - 500.0).coerceAtLeast(1200.0)
        "Build Muscle" -> tdee + 300.0
        else -> tdee
    }

    val proteinGrams = (2.0 * parsedWeight).coerceIn(40.0, 250.0)
    val fatCalories = targetCalories * 0.25
    val fatGrams = fatCalories / 9.0
    val remainingCalories = targetCalories - (proteinGrams * 4.0) - fatCalories
    val carbGrams = (remainingCalories / 4.0).coerceAtLeast(50.0)

    val recommendedWaterLiters = (parsedWeight * 0.035).coerceIn(1.5, 6.0)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("profile_main_dashboard"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (showSuccessBanner) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (isDarkMode) Color(0xFF0F2610) else Color(0xFFE8F5E9)
                ),
                border = BorderStroke(1.dp, if (isDarkMode) Color(0xFF2E7D32) else Color(0xFFC8E6C9)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = "Success",
                            tint = if (isDarkMode) Color(0xFF81C784) else Color(0xFF2E7D32),
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Targets successfully synced & calculated!",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDarkMode) Color(0xFFC8E6C9) else Color(0xFF1B5E20)
                        )
                    }
                    IconButton(
                        onClick = { showSuccessBanner = false },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Close Banner",
                            tint = if (isDarkMode) Color(0xFF81C784) else Color(0xFF2E7D32),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isDarkMode) Color(0xFF1F1714) else Color.White
            ),
            border = BorderStroke(1.dp, if (isDarkMode) Color(0xFF332620) else Color(0xFFEDF2F4)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Active Health Profile",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDarkMode) Color.White else Color(0xFF2B2D42)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Track separate objectives for different users. Switch profiles instantly below.",
                    fontSize = 12.sp,
                    color = if (isDarkMode) Color(0xFFAFAFAF) else Color(0xFF6C757D)
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val isP1 = activeProfile == "profile_1"
                    val isP2 = activeProfile == "profile_2"

                    Button(
                        onClick = { onSwitchProfile("profile_1") },
                        modifier = Modifier.weight(1f).height(40.dp).testTag("select_profile_1"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isP1) (if (isDarkMode) Color(0xFFFFB4A2) else Color(0xFF9C432F)) else (if (isDarkMode) Color(0xFF2B221E) else Color(0xFFF1F5F9)),
                            contentColor = if (isP1) (if (isDarkMode) Color(0xFF531B10) else Color.White) else (if (isDarkMode) Color.LightGray else Color(0xFF475569))
                        )
                    ) {
                        Text(text = profile1Name, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { onSwitchProfile("profile_2") },
                        modifier = Modifier.weight(1f).height(40.dp).testTag("select_profile_2"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isP2) (if (isDarkMode) Color(0xFFFFB4A2) else Color(0xFF9C432F)) else (if (isDarkMode) Color(0xFF2B221E) else Color(0xFFF1F5F9)),
                            contentColor = if (isP2) (if (isDarkMode) Color(0xFF531B10) else Color.White) else (if (isDarkMode) Color.LightGray else Color(0xFF475569))
                        )
                    ) {
                        Text(text = profile2Name, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (isEditingName) {
                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        label = { Text("Rename Profile") },
                        modifier = Modifier.fillMaxWidth().testTag("profile_rename_input"),
                        singleLine = true,
                        trailingIcon = {
                            Row {
                                IconButton(
                                    onClick = {
                                        if (nameInput.isNotBlank()) {
                                            onRenameProfile(activeProfile, nameInput)
                                        }
                                        isEditingName = false
                                    }
                                ) {
                                    Icon(imageVector = Icons.Filled.Check, contentDescription = "Save Name", tint = Color(0xFF2E7D32))
                                }
                                IconButton(onClick = { isEditingName = false }) {
                                    Icon(imageVector = Icons.Filled.Close, contentDescription = "Cancel", tint = Color.Red)
                                }
                            }
                        }
                    )
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Currently Tracking: $activeName",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isDarkMode) Color(0xFFFFB4A2) else Color(0xFF9C432F)
                        )
                        TextButton(
                            onClick = { isEditingName = true },
                            modifier = Modifier.testTag("profile_rename_btn")
                        ) {
                            Icon(imageVector = Icons.Filled.Edit, contentDescription = "Edit Name", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Rename Persona", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isDarkMode) Color(0xFF1F1714) else Color.White
            ),
            border = BorderStroke(1.dp, if (isDarkMode) Color(0xFF332620) else Color(0xFFEDF2F4)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Physical Statistics & Biometrics",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDarkMode) Color.White else Color(0xFF2B2D42)
                )
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Gender Selection",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isDarkMode) Color.LightGray else Color(0xFF475569)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    gendersList.forEach { genOpt ->
                        val isSelected = inputGender == genOpt
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isSelected) {
                                        if (isDarkMode) Color(0xFF5D4037) else Color(0xFFFFEBE6)
                                    } else {
                                        if (isDarkMode) Color(0xFF2B221E) else Color(0xFFF8F9FA)
                                    }
                                )
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) {
                                        if (isDarkMode) Color(0xFFFFB4A2) else Color(0xFF9C432F)
                                    } else {
                                        if (isDarkMode) Color(0xFF3C2F2A) else Color(0xFFE2E8F0)
                                    },
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable {
                                    inputGender = genOpt
                                }
                                .testTag("gender_select_$genOpt"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = genOpt,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) {
                                    if (isDarkMode) Color(0xFFFFB4A2) else Color(0xFF9C432F)
                                } else {
                                    if (isDarkMode) Color.LightGray else Color(0xFF6C757D)
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = inputAge,
                        onValueChange = {
                            inputAge = it
                        },
                        label = { Text("Age") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f).testTag("profile_input_age"),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = inputHeight,
                        onValueChange = {
                            inputHeight = it
                        },
                        label = { Text("Height (cm)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1.2f).testTag("profile_input_height"),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = inputWeight,
                        onValueChange = {
                            inputWeight = it
                        },
                        label = { Text("Weight (kg)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1.2f).testTag("profile_input_weight"),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = inputActivity,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Daily Activity Level") },
                        trailingIcon = { 
                            IconButton(onClick = { activityExpanded = !activityExpanded }) {
                                Icon(
                                    imageVector = if (activityExpanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                                    contentDescription = "Toggle level dropdown"
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth().clickable { activityExpanded = !activityExpanded }.testTag("profile_input_activity")
                    )
                    DropdownMenu(
                        expanded = activityExpanded,
                        onDismissRequest = { activityExpanded = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        activitiesList.forEach { actOpt ->
                             DropdownMenuItem(
                                 text = { Text(actOpt) },
                                 onClick = {
                                     inputActivity = actOpt
                                     activityExpanded = false
                                 }
                             )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Weekly Weight Target Objectives",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isDarkMode) Color.LightGray else Color(0xFF475569)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    goalsList.forEach { goalOpt ->
                        val isSelected = inputFitnessGoal == goalOpt
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isSelected) {
                                        if (isDarkMode) Color(0xFF4A148C) else Color(0xFFF3E5F5)
                                    } else {
                                        if (isDarkMode) Color(0xFF2B221E) else Color(0xFFF8F9FA)
                                    }
                                )
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) {
                                        if (isDarkMode) Color(0xFFCE93D8) else Color(0xFF8E24AA)
                                    } else {
                                        if (isDarkMode) Color(0xFF3C2F2A) else Color(0xFFE2E8F0)
                                    },
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable {
                                    inputFitnessGoal = goalOpt
                                }
                                .testTag("fitness_goal_$goalOpt"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = goalOpt,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) {
                                    if (isDarkMode) Color(0xFFE1BEE7) else Color(0xFF8E24AA)
                                } else {
                                    if (isDarkMode) Color.LightGray else Color(0xFF6C757D)
                                }
                            )
                        }
                    }
                }
            }
        }

        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isDarkMode) Color(0xFF1B1B1F) else Color(0xFFF8FAFC)
            ),
            border = BorderStroke(1.dp, if (isDarkMode) Color(0xFF2B2B30) else Color(0xFFE2E8F0)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "Live Metabolic Biological Energy & Water Needs",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDarkMode) Color.White else Color(0xFF1E293B)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "BMR represents calories burned at complete rest, while TDEE reflects your custom daily movement factor.",
                    fontSize = 12.sp,
                    color = if (isDarkMode) Color(0xFFAFAFAF) else Color(0xFF64748B)
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .background(if (isDarkMode) Color(0xFF241D1A) else Color.White, RoundedCornerShape(12.dp))
                            .border(1.dp, if (isDarkMode) Color(0xFF332620) else Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "🔥 BMR Baseline", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = if (isDarkMode) Color.LightGray else Color(0xFF64748B))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "${bmr.toInt()} kcal", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = if (isDarkMode) Color.White else Color(0xFF1E293B))
                    }

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .background(if (isDarkMode) Color(0xFF241D1A) else Color.White, RoundedCornerShape(12.dp))
                            .border(1.dp, if (isDarkMode) Color(0xFF332620) else Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "⚡ TDEE Burn", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = if (isDarkMode) Color.LightGray else Color(0xFF64748B))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "${tdee.toInt()} kcal", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = if (isDarkMode) Color.White else Color(0xFF1E293B))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                HorizontalDivider(color = if (isDarkMode) Color(0xFF2D2321) else Color(0xFFE2E8F0))

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Sync targets recommendations to FitFlow metrics:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDarkMode) Color.LightGray else Color(0xFF1E293B)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Calories", fontSize = 10.sp, color = Color.Gray)
                        Text("${targetCalories.toInt()} kcal", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = if (isDarkMode) Color.White else Color(0xFF1E293B))
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Protein", fontSize = 10.sp, color = Color.Gray)
                        Text("${proteinGrams.toInt()}g", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = if (isDarkMode) Color(0xFFFFB4A2) else Color(0xFF9C432F))
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Carbs", fontSize = 10.sp, color = Color.Gray)
                        Text("${carbGrams.toInt()}g", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = if (isDarkMode) Color(0xFF81C784) else Color(0xFF2E7D32))
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Fats", fontSize = 10.sp, color = Color.Gray)
                        Text("${fatGrams.toInt()}g", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = if (isDarkMode) Color(0xFFFFB74D) else Color(0xFFE65100))
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Water", fontSize = 10.sp, color = Color.Gray)
                        val formattedWater = String.format(Locale.getDefault(), "%.1f", recommendedWaterLiters)
                        Text("${formattedWater} L", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF00ACC1))
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = {
                        onApplyCalculatedGoals(
                            targetCalories,
                            proteinGrams,
                            carbGrams,
                            fatGrams,
                            recommendedWaterLiters
                        )
                        showSuccessBanner = true
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("profile_apply_goals_cta"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isDarkMode) Color(0xFFBD8E85) else Color(0xFF9C432F),
                        contentColor = if (isDarkMode) Color(0xFF2E0904) else Color.White
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(imageVector = Icons.Filled.Sync, contentDescription = "Sync icon", modifier = Modifier.size(18.dp))
                        Text("Apply & Sync Goals Base", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isDarkMode) Color(0xFF2D1616) else Color(0xFFFFF5F5)
            ),
            border = BorderStroke(1.dp, if (isDarkMode) Color(0xFF4D1C1C) else Color(0xFFFED7D7)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "DANGER ZONE: Clear Logs History",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDarkMode) Color(0xFFFF8A80) else Color(0xFFC53030)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Wipes out all tracked daily scoop logs, nutrition, and water records for both personas completely. This is irreversible.",
                    fontSize = 11.sp,
                    color = if (isDarkMode) Color(0xFFE57373) else Color(0xFF9B2C2C),
                    lineHeight = 15.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                if (showWipeConfirmation) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Are you absolutely sure?",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDarkMode) Color.White else Color(0xFF2D3748)
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Button(
                                onClick = {
                                    onClearAllHistory()
                                    showWipeConfirmation = false
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Red, contentColor = Color.White),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f).height(38.dp).testTag("wipe_data_confirm")
                            ) {
                                Text("YES, WIPE ALL DATA", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            OutlinedButton(
                                onClick = { showWipeConfirmation = false },
                                border = BorderStroke(1.dp, if (isDarkMode) Color.LightGray else Color.Gray),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f).height(38.dp)
                            ) {
                                Text("CANCEL", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (isDarkMode) Color.White else Color.Black)
                            }
                        }
                    }
                } else {
                    Button(
                        onClick = { showWipeConfirmation = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isDarkMode) Color(0xFF4D1C1C) else Color(0xFFFFF5F5),
                            contentColor = if (isDarkMode) Color(0xFFFF8A80) else Color(0xFFC53030)
                        ),
                        border = BorderStroke(1.dp, if (isDarkMode) Color(0xFFB71C1C) else Color(0xFFFEB2B2)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().height(42.dp).testTag("wipe_data_trigger")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(imageVector = Icons.Filled.Delete, contentDescription = "Trash icon", modifier = Modifier.size(16.dp))
                            Text("Permanently Erase Database History Logs", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
