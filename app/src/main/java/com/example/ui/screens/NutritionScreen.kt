package com.example.ui.screens

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalActivity
import com.example.ui.components.NavButton
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.IntakeViewModel
import com.example.StableFoodLogs
import com.example.data.FoodLog
import com.example.data.NutritionLabelResult
import com.example.ui.components.GoalInputField
import com.example.ui.components.GoalQuickPill
import com.example.ui.components.StatLabelPill

@Composable
fun NutritionScreen(
    viewModel: IntakeViewModel,
    isDarkMode: Boolean,
    onScreenChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val foodLogsTodayRaw by viewModel.foodLogsToday.collectAsStateWithLifecycle()
    val foodLogsToday = remember(foodLogsTodayRaw) { StableFoodLogs(foodLogsTodayRaw) }
    val calorieGoal by viewModel.calorieGoal.collectAsStateWithLifecycle()
    val proteinGoal by viewModel.proteinGoal.collectAsStateWithLifecycle()
    val carbGoal by viewModel.carbGoal.collectAsStateWithLifecycle()
    val fatGoal by viewModel.fatGoal.collectAsStateWithLifecycle()
    val isAnalyzingLabel by viewModel.isAnalyzingLabel.collectAsStateWithLifecycle()
    val analysisResult by viewModel.analysisResult.collectAsStateWithLifecycle()
    val analysisError by viewModel.analysisError.collectAsStateWithLifecycle()

    val themeBg = if (isDarkMode) Color(0xFF140F0D) else Color(0xFFFDF8F6)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(themeBg),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            NavButton(
                text = "Daily Scoops",
                icon = Icons.Filled.FitnessCenter,
                isDark = isDarkMode,
                activeColor = if (isDarkMode) Color(0xFFFFB4A2) else Color(0xFF9C432F),
                onClick = { onScreenChange("scoops") },
                modifier = Modifier.weight(1f).testTag("goto_scoops")
            )
            NavButton(
                text = "Hydration",
                icon = Icons.Filled.LocalActivity,
                isDark = isDarkMode,
                activeColor = Color(0xFF00ACC1),
                onClick = { onScreenChange("water") },
                modifier = Modifier.weight(1f).testTag("goto_water")
            )
        }

        NutritionProgressCard(
            isDarkMode = isDarkMode,
            foodLogs = foodLogsToday,
            calorieGoal = calorieGoal,
            proteinGoal = proteinGoal,
            carbGoal = carbGoal,
            fatGoal = fatGoal
        )

        NutritionAILoggerCard(
            isDarkMode = isDarkMode,
            isAnalyzing = isAnalyzingLabel,
            analysisResult = analysisResult,
            analysisError = analysisError,
            onAnalyzeImage = { bitmap -> viewModel.analyzeLabel(bitmap) },
            onSimulateLabel = { name, cal, prot, carb, fat, sod, sug, fib ->
                viewModel.simulateLabel(name, cal, prot, carb, fat, sod, sug, fib)
            },
            onLogFood = { name, cal, prot, carb, fat, sod, sug, fib, wt ->
                viewModel.addFoodLog(name, cal, prot, carb, fat, sod, sug, fib, wt)
            },
            onClearAnalysis = { viewModel.clearAnalysis() }
        )

        NutritionManualLoggerCard(
            isDarkMode = isDarkMode,
            onLogManualFood = { name, cal, prot, carb, fat, sod, sug, fib, wt ->
                viewModel.addFoodLog(name, cal, prot, carb, fat, sod, sug, fib, wt)
            }
        )

        MacroGoalsSettingsCard(
            isDarkMode = isDarkMode,
            calorieGoal = calorieGoal,
            proteinGoal = proteinGoal,
            carbGoal = carbGoal,
            fatGoal = fatGoal,
            onSaveGoals = { cal, prot, carb, fat ->
                viewModel.setNutritionGoals(cal, prot, carb, fat)
            }
        )

        if (foodLogsToday.list.isEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .testTag("nutrition_empty_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDarkMode) Color(0xFF1E261E) else Color(0xFFE8F5E9)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Filled.Restaurant,
                        contentDescription = "Empty Nutrition Logs",
                        tint = if (isDarkMode) Color(0xFF81C784) else Color(0xFF2E7D32),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No food logged today",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDarkMode) Color(0xFF1B5E20) else Color(0xFF1B5E20)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Scan a nutrition label or use a sample preset above!",
                        fontSize = 13.sp,
                        color = if (isDarkMode) Color(0xFFB0BEC5) else Color(0xFF558B2F),
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            Text(
                text = "Logged Foods Today",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDarkMode) Color.White else Color(0xFF333333),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 8.dp)
            )
            foodLogsToday.list.forEach { foodLog ->
                FoodLogDetailsRow(
                    isDarkMode = isDarkMode,
                    foodLog = foodLog,
                    onDelete = { viewModel.deleteFoodLog(foodLog.id) }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun NutritionProgressCard(
    isDarkMode: Boolean,
    foodLogs: StableFoodLogs,
    calorieGoal: Double,
    proteinGoal: Double,
    carbGoal: Double,
    fatGoal: Double
) {
    val totalCalories = remember(foodLogs) { foodLogs.list.sumOf { it.calories } }
    val totalProtein = remember(foodLogs) { foodLogs.list.sumOf { it.protein } }
    val totalCarbs = remember(foodLogs) { foodLogs.list.sumOf { it.carbs } }
    val totalFats = remember(foodLogs) { foodLogs.list.sumOf { it.fats } }
    val totalSodium = remember(foodLogs) { foodLogs.list.sumOf { it.sodium } }
    val totalSugars = remember(foodLogs) { foodLogs.list.sumOf { it.sugars } }
    val totalFibers = remember(foodLogs) { foodLogs.list.sumOf { it.fibers } }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .testTag("nutrition_progress_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDarkMode) Color(0xFF1B241D) else Color(0xFFF1F8F3)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "Today's Nutrition Summary",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDarkMode) Color(0xFF81C784) else Color(0xFF2E7D32)
            )
            Spacer(modifier = Modifier.height(16.dp))

            val calProgress = if (calorieGoal > 0) (totalCalories / calorieGoal).toFloat().coerceIn(0f, 1f) else 0f
            val remainingCalories = (calorieGoal - totalCalories).coerceAtLeast(0.0)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Calories",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = if (isDarkMode) Color.White else Color(0xFF1B5E20)
                    )
                    Text(
                        text = "${totalCalories.toInt()} / ${calorieGoal.toInt()} kcal",
                        fontSize = 13.sp,
                        color = if (isDarkMode) Color(0xFFAFAFAF) else Color(0xFF558B2F)
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${remainingCalories.toInt()}",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = if (isDarkMode) Color(0xFF81C784) else Color(0xFF2E7D32)
                    )
                    Text(
                        text = "kcal remaining",
                        fontSize = 10.sp,
                        color = if (isDarkMode) Color(0xFFAFAFAF) else Color(0xFF757575)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = calProgress,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(CircleShape),
                color = if (isDarkMode) Color(0xFF81C784) else Color(0xFF2E7D32),
                trackColor = if (isDarkMode) Color(0xFF2E3E31) else Color(0xFFC8E6C9)
            )

            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider(color = if (isDarkMode) Color(0xFF2A362C) else Color(0xFFE0E0E0))
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Macronutrients",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDarkMode) Color(0xFFB0BEC5) else Color(0xFF555555),
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(12.dp))

            val protProgress = if (proteinGoal > 0) (totalProtein / proteinGoal).toFloat().coerceIn(0f, 1f) else 0f
            MacroRowProgress(
                label = "Protein",
                current = totalProtein,
                goal = proteinGoal,
                progress = protProgress,
                color = Color(0xFF4CAF50),
                isDarkMode = isDarkMode
            )

            Spacer(modifier = Modifier.height(12.dp))

            val carbProgress = if (carbGoal > 0) (totalCarbs / carbGoal).toFloat().coerceIn(0f, 1f) else 0f
            MacroRowProgress(
                label = "Carbs",
                current = totalCarbs,
                goal = carbGoal,
                progress = carbProgress,
                color = Color(0xFFFFB300),
                isDarkMode = isDarkMode
            )

            Spacer(modifier = Modifier.height(12.dp))

            val fatProgress = if (fatGoal > 0) (totalFats / fatGoal).toFloat().coerceIn(0f, 1f) else 0f
            MacroRowProgress(
                label = "Fats",
                current = totalFats,
                goal = fatGoal,
                progress = fatProgress,
                color = Color(0xFFE53935),
                isDarkMode = isDarkMode
            )

            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider(color = if (isDarkMode) Color(0xFF2A362C) else Color(0xFFE0E0E0))
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Other Nutrients",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDarkMode) Color(0xFFB0BEC5) else Color(0xFF555555),
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                NutrientPill(label = "Sodium", value = "${totalSodium.toInt()} mg", isDarkMode = isDarkMode)
                NutrientPill(label = "Sugars", value = "${totalSugars.toInt()} g", isDarkMode = isDarkMode)
                NutrientPill(label = "Fiber", value = "${totalFibers.toInt()} g", isDarkMode = isDarkMode)
            }
        }
    }
}

@Composable
fun MacroRowProgress(
    label: String,
    current: Double,
    goal: Double,
    progress: Float,
    color: Color,
    isDarkMode: Boolean
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDarkMode) Color.White else Color(0xFF333333)
            )
            Text(
                text = "${current.toInt()}g / ${goal.toInt()}g",
                fontSize = 13.sp,
                color = if (isDarkMode) Color(0xFFB0BEC5) else Color(0xFF666666)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = progress,
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(CircleShape),
            color = color,
            trackColor = if (isDarkMode) Color(0xFF2A2E2A) else Color(0xFFEEEEEE)
        )
    }
}

@Composable
fun NutrientPill(
    label: String,
    value: String,
    isDarkMode: Boolean
) {
    Column(
        modifier = Modifier
            .background(
                if (isDarkMode) Color(0xFF232D25) else Color(0xFFE8F5E9),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 14.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = if (isDarkMode) Color(0xFF81C784) else Color(0xFF2E7D32)
        )
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = if (isDarkMode) Color.White else Color(0xFF1B5E20)
        )
    }
}

@Composable
fun NutritionAILoggerCard(
    isDarkMode: Boolean,
    isAnalyzing: Boolean,
    analysisResult: NutritionLabelResult?,
    analysisError: String?,
    onAnalyzeImage: (Bitmap) -> Unit,
    onSimulateLabel: (String, Double, Double, Double, Double, Double, Double, Double) -> Unit,
    onLogFood: (String, Double, Double, Double, Double, Double, Double, Double, Double) -> Unit,
    onClearAnalysis: () -> Unit
) {
    val context = LocalContext.current
    var inputWeight by remember(analysisResult) { mutableStateOf("100") }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val inputStream = context.contentResolver.openInputStream(it)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                if (bitmap != null) {
                    onAnalyzeImage(bitmap)
                }
            } catch (e: Exception) {
                Log.e("Nutrition", "Error decoding uri bitmap", e)
            }
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .testTag("nutrition_ai_logger_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDarkMode) Color(0xFF222823) else Color.White
        ),
        border = BorderStroke(1.dp, if (isDarkMode) Color(0xFF2D3B31) else Color(0xFFE2EDE4)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = "AI Icon",
                    tint = if (isDarkMode) Color(0xFF81C784) else Color(0xFF2E7D32),
                    modifier = Modifier.size(24.dp).padding(end = 4.dp)
                )
                Text(
                    text = "AI Label Scan & Log",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDarkMode) Color.White else Color(0xFF333333)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Pick an image of a food nutrition label to extract details, or use modern instant presets.",
                fontSize = 13.sp,
                color = if (isDarkMode) Color(0xFFAFAFAF) else Color(0xFF666666)
            )
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { imagePickerLauncher.launch("image/*") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("btn_select_label_gallery"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isDarkMode) Color(0xFF2E3D31) else Color(0xFFE8F5E9),
                    contentColor = if (isDarkMode) Color(0xFF81C784) else Color(0xFF2E7D32)
                )
            ) {
                Icon(
                    imageVector = Icons.Filled.Photo,
                    contentDescription = "Upload Icon",
                    modifier = Modifier.size(18.dp).padding(end = 6.dp)
                )
                Text("Select Label Image", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Try AI presets:",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDarkMode) Color(0xFF888888) else Color(0xFF999999),
                modifier = Modifier.padding(bottom = 6.dp)
            )

            val presets = remember {
                listOf(
                    Triple("Greek Yogurt", 59.0, 10.0),
                    Triple("Peanut Butter", 588.0, 25.0),
                    Triple("Whey Shake", 370.0, 75.0)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                presets.forEachIndexed { index, item ->
                    Button(
                        onClick = {
                            val (name, cal, pro) = item
                            val carb = when (index) {
                                0 -> 3.6
                                1 -> 20.0
                                else -> 8.0
                            }
                            val fat = when (index) {
                                0 -> 0.4
                                1 -> 50.0
                                else -> 3.5
                            }
                            val sodium = when (index) {
                                0 -> 36.0
                                1 -> 420.0
                                else -> 220.0
                            }
                            val sugars = when (index) {
                                0 -> 3.2
                                1 -> 9.0
                                else -> 3.0
                            }
                            val fibers = when (index) {
                                0 -> 0.0
                                1 -> 6.0
                                else -> 1.0
                            }
                            onSimulateLabel(name, cal, pro, carb, fat, sodium, sugars, fibers)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(34.dp)
                            .testTag("preset_${item.first.lowercase().replace(" ", "_")}"),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isDarkMode) Color(0xFF1E241F) else Color(0xFFF1F8F3),
                            contentColor = if (isDarkMode) Color(0xFF81C784) else Color(0xFF2E7D32)
                        ),
                        border = BorderStroke(0.5.dp, if (isDarkMode) Color(0xFF2D3C30) else Color(0xFFC8E6C9)),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(item.first, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            AnimatedVisibility(visible = isAnalyzing) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(
                        color = if (isDarkMode) Color(0xFF81C784) else Color(0xFF2E7D32),
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Scanned label is being processed by AI...",
                        fontSize = 13.sp,
                        color = if (isDarkMode) Color(0xFF81C784) else Color(0xFF2E7D32),
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            analysisError?.let { err ->
                Spacer(modifier = Modifier.height(14.dp))
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("analysis_error_card"),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDarkMode) Color(0xFF3E1F1F) else Color(0xFFFFEBEE)
                    )
                ) {
                    Text(
                        text = err,
                        color = if (isDarkMode) Color(0xFFFFB4AB) else Color(0xFFC62828),
                        fontSize = 12.sp,
                        modifier = Modifier.padding(12.dp),
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            analysisResult?.let { result ->
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = if (isDarkMode) Color(0xFF2D3B31) else Color(0xFFE2EDE4))
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "AI Extracted Data (Per 100g):",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDarkMode) Color.White else Color(0xFF1B5E20)
                )
                Spacer(modifier = Modifier.height(10.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            if (isDarkMode) Color(0xFF1A1F1B) else Color(0xFFF5F9F6),
                            RoundedCornerShape(12.dp)
                        )
                        .padding(12.dp)
                ) {
                    Text(
                         text = result.foodName,
                         fontSize = 15.sp,
                         fontWeight = FontWeight.ExtraBold,
                         color = if (isDarkMode) Color(0xFF81C784) else Color(0xFF2E7D32)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text("• Calories: ${result.caloriesPer100g.toInt()} kcal", fontSize = 12.sp, color = if (isDarkMode) Color.White else Color.Black)
                    Text("• Protein: ${result.proteinPer100g} g", fontSize = 12.sp, color = if (isDarkMode) Color.White else Color.Black)
                    Text("• Carbs: ${result.carbsPer100g} g", fontSize = 12.sp, color = if (isDarkMode) Color.White else Color.Black)
                    Text("• Fats: ${result.fatsPer100g} g", fontSize = 12.sp, color = if (isDarkMode) Color.White else Color.Black)
                    Text("• Sodium: ${result.sodiumPer100g} mg", fontSize = 12.sp, color = if (isDarkMode) Color.White else Color.Black)
                    Text("• Sugars: ${result.sugarsPer100g} g", fontSize = 12.sp, color = if (isDarkMode) Color.White else Color.Black)
                    Text("• Fibers: ${result.fibersPer100g} g", fontSize = 12.sp, color = if (isDarkMode) Color.White else Color.Black)
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Total Weight Eaten (Grams):",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDarkMode) Color.White else Color(0xFF333333)
                )
                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = inputWeight,
                    onValueChange = { inputWeight = it.filter { char -> char.isDigit() || char == '.' } },
                    modifier = Modifier.fillMaxWidth().testTag("input_food_weight"),
                    placeholder = { Text("e.g. 150") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    ),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (isDarkMode) Color(0xFF81C784) else Color(0xFF2E7D32),
                        unfocusedBorderColor = if (isDarkMode) Color(0xFF435045) else Color(0xFFC8E6C9),
                        focusedLabelColor = if (isDarkMode) Color(0xFF81C784) else Color(0xFF2E7D32)
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                val portionFactor = (inputWeight.toDoubleOrNull() ?: 100.0) / 100.0
                val computedCalories = result.caloriesPer100g * portionFactor
                val computedProtein = result.proteinPer100g * portionFactor
                val computedCarbs = result.carbsPer100g * portionFactor
                val computedFats = result.fatsPer100g * portionFactor
                val computedSodium = result.sodiumPer100g * portionFactor
                val computedSugars = result.sugarsPer100g * portionFactor
                val computedFibers = result.fibersPer100g * portionFactor

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            1.dp,
                            if (isDarkMode) Color(0xFF4F6253) else Color(0xFF81C784),
                            RoundedCornerShape(12.dp)
                        )
                        .padding(12.dp)
                ) {
                    Text(
                        text = "Calculated intake based on portion size (${inputWeight}g):",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isDarkMode) Color(0xFFAFAFAF) else Color(0xFF558B2F)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Portion: ${computedCalories.toInt()} kcal  •  ${computedProtein.toInt()}g Protein",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDarkMode) Color.White else Color(0xFF1B5E20)
                    )
                    Text(
                        text = "Macros: ${computedCarbs.toInt()}g Carbs  •  ${computedFats.toInt()}g Fats",
                        fontSize = 13.sp,
                        color = if (isDarkMode) Color(0xFFE2EDE4) else Color(0xFF333333)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            val finalWeight = inputWeight.toDoubleOrNull() ?: 100.0
                            onLogFood(
                                result.foodName,
                                computedCalories,
                                computedProtein,
                                computedCarbs,
                                computedFats,
                                computedSodium,
                                computedSugars,
                                computedFibers,
                                finalWeight
                            )
                            onClearAnalysis()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("btn_log_food_confirm"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isDarkMode) Color(0xFF81C784) else Color(0xFF2E7D32),
                            contentColor = if (isDarkMode) Color(0xFF112E13) else Color.White
                        )
                    ) {
                        Text("Log Food Portion", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = onClearAnalysis,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("btn_log_food_cancel"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isDarkMode) Color(0xFF332422) else Color(0xFFFFEBEE),
                            contentColor = if (isDarkMode) Color(0xFFFFB4AB) else Color(0xFFC62828)
                        ),
                        border = BorderStroke(0.5.dp, if (isDarkMode) Color(0xFF5E2E2C) else Color(0xFFFFCDD2))
                    ) {
                        Text("Cancel", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun NutritionManualLoggerCard(
    isDarkMode: Boolean,
    onLogManualFood: (String, Double, Double, Double, Double, Double, Double, Double, Double) -> Unit
) {
    var foodName by remember { mutableStateOf("") }
    var weightGrams by remember { mutableStateOf("100") }
    var calories by remember { mutableStateOf("") }
    var protein by remember { mutableStateOf("") }
    var carbs by remember { mutableStateOf("") }
    var fats by remember { mutableStateOf("") }

    var showAdvanced by remember { mutableStateOf(false) }
    var sodium by remember { mutableStateOf("") }
    var sugars by remember { mutableStateOf("") }
    var fibers by remember { mutableStateOf("") }

    val pVal = protein.toDoubleOrNull() ?: 0.0
    val cVal = carbs.toDoubleOrNull() ?: 0.0
    val fVal = fats.toDoubleOrNull() ?: 0.0
    val calculatedCalories = (pVal * 4) + (cVal * 4) + (fVal * 9)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .testTag("nutrition_manual_logger_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDarkMode) Color(0xFF1F2521) else Color(0xFFF9FDF9)
        ),
        border = BorderStroke(1.dp, if (isDarkMode) Color(0xFF2E3B33) else Color(0xFFE2EFE7)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                     imageVector = Icons.Filled.Edit,
                     contentDescription = "Manual Log Icon",
                     tint = if (isDarkMode) Color(0xFF81C784) else Color(0xFF2E7D32),
                     modifier = Modifier.size(24.dp).padding(end = 6.dp)
                )
                Text(
                    text = "Quick Manual Logger",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDarkMode) Color.White else Color(0xFF333333)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Log food details manually by entering stats below.",
                fontSize = 12.sp,
                color = if (isDarkMode) Color(0xFFAFAFAF) else Color(0xFF666666)
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = foodName,
                onValueChange = { foodName = it },
                label = { Text("Food Name", fontSize = 12.sp) },
                placeholder = { Text("e.g. Scrambled Eggs, Oatmeal, Banana") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("manual_food_name"),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = if (isDarkMode) Color(0xFF81C784) else Color(0xFF2E7D32),
                    unfocusedBorderColor = if (isDarkMode) Color(0xFF3B483D) else Color(0xFFC8E6C9)
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = weightGrams,
                    onValueChange = { weightGrams = it.filter { char -> char.isDigit() || char == '.' } },
                    label = { Text("Portion (g)", fontSize = 11.sp) },
                    placeholder = { Text("100") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f).testTag("manual_food_weight"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (isDarkMode) Color(0xFF81C784) else Color(0xFF2E7D32),
                        unfocusedBorderColor = if (isDarkMode) Color(0xFF3B483D) else Color(0xFFC8E6C9)
                    )
                )

                OutlinedTextField(
                    value = calories,
                    onValueChange = { calories = it.filter { char -> char.isDigit() } },
                    label = { Text("Calories (kcal)", fontSize = 11.sp) },
                    placeholder = { Text("kcal") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1.2f).testTag("manual_food_calories"),
                    shape = RoundedCornerShape(12.dp),
                    trailingIcon = {
                        if (calculatedCalories > 0.0) {
                            Text(
                                text = "Autofill",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDarkMode) Color(0xFF81C784) else Color(0xFF2E7D32),
                                modifier = Modifier
                                    .padding(end = 8.dp)
                                    .clickable {
                                        calories = calculatedCalories.toInt().toString()
                                    }
                            )
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (isDarkMode) Color(0xFF81C784) else Color(0xFF2E7D32),
                        unfocusedBorderColor = if (isDarkMode) Color(0xFF3B483D) else Color(0xFFC8E6C9)
                    )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = protein,
                    onValueChange = { protein = it.filter { char -> char.isDigit() || char == '.' } },
                    label = { Text("Protein (g)", fontSize = 10.sp) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f).testTag("manual_food_protein"),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF4CAF50),
                        unfocusedBorderColor = if (isDarkMode) Color(0xFF3B483D) else Color(0xFFD0E3D3)
                    )
                )

                OutlinedTextField(
                    value = carbs,
                    onValueChange = { carbs = it.filter { char -> char.isDigit() || char == '.' } },
                    label = { Text("Carbs (g)", fontSize = 10.sp) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f).testTag("manual_food_carbs"),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFFFB300),
                        unfocusedBorderColor = if (isDarkMode) Color(0xFF3B483D) else Color(0xFFD0E3D3)
                    )
                )

                OutlinedTextField(
                    value = fats,
                    onValueChange = { fats = it.filter { char -> char.isDigit() || char == '.' } },
                    label = { Text("Fats (g)", fontSize = 10.sp) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f).testTag("manual_food_fats"),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFE53935),
                        unfocusedBorderColor = if (isDarkMode) Color(0xFF3B483D) else Color(0xFFD0E3D3)
                    )
                )
            }

            if (calculatedCalories > 0.0) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Calculated from Macros: ${calculatedCalories.toInt()} kcal",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (isDarkMode) Color(0xFFAFAFAF) else Color(0xFF666666)
                    )
                    
                    Text(
                        text = "Use Calculated kcal",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDarkMode) Color(0xFF81C784) else Color(0xFF2E7D32),
                        modifier = Modifier.clickable {
                            calories = calculatedCalories.toInt().toString()
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showAdvanced = !showAdvanced }
                    .padding(vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (showAdvanced) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                    contentDescription = "Toggle Advanced",
                    tint = if (isDarkMode) Color(0xFF81C784) else Color(0xFF2E7D32),
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Advanced Nutrients (Optional)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDarkMode) Color(0xFF81C784) else Color(0xFF2E7D32)
                )
            }

            AnimatedVisibility(visible = showAdvanced) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = sodium,
                            onValueChange = { sodium = it.filter { char -> char.isDigit() || char == '.' } },
                            label = { Text("Sodium (mg)", fontSize = 10.sp) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f).testTag("manual_food_sodium"),
                            shape = RoundedCornerShape(10.dp)
                        )
                        OutlinedTextField(
                            value = sugars,
                            onValueChange = { sugars = it.filter { char -> char.isDigit() || char == '.' } },
                            label = { Text("Sugars (g)", fontSize = 10.sp) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f).testTag("manual_food_sugars"),
                            shape = RoundedCornerShape(10.dp)
                        )
                        OutlinedTextField(
                            value = fibers,
                            onValueChange = { fibers = it.filter { char -> char.isDigit() || char == '.' } },
                            label = { Text("Fiber (g)", fontSize = 10.sp) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f).testTag("manual_food_fibers"),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (foodName.trim().isNotEmpty()) {
                        val portion = weightGrams.toDoubleOrNull() ?: 100.0
                        val calVal = calories.toDoubleOrNull() ?: calculatedCalories
                        val proVal = protein.toDoubleOrNull() ?: 0.0
                        val carbVal = carbs.toDoubleOrNull() ?: 0.0
                        val fatVal = fats.toDoubleOrNull() ?: 0.0
                        
                        val sodVal = sodium.toDoubleOrNull() ?: 0.0
                        val sugVal = sugars.toDoubleOrNull() ?: 0.0
                        val fibVal = fibers.toDoubleOrNull() ?: 0.0

                        onLogManualFood(
                            foodName,
                            calVal,
                            proVal,
                            carbVal,
                            fatVal,
                            sodVal,
                            sugVal,
                            fibVal,
                            portion
                        )

                        foodName = ""
                        calories = ""
                        protein = ""
                        carbs = ""
                        fats = ""
                        sodium = ""
                        sugars = ""
                        fibers = ""
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("btn_log_food_manual_submit"),
                enabled = foodName.trim().isNotEmpty(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isDarkMode) Color(0xFF81C784) else Color(0xFF2E7D32),
                    contentColor = if (isDarkMode) Color(0xFF112E13) else Color.White
                )
            ) {
                Text(
                    text = if (foodName.trim().isEmpty()) "Enter food name to log" else "Log Manual Food",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun FoodLogDetailsRow(
    isDarkMode: Boolean,
    foodLog: FoodLog,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 4.dp)
            .testTag("food_log_row_${foodLog.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDarkMode) Color(0xFF222823) else Color.White
        ),
        border = BorderStroke(1.dp, if (isDarkMode) Color(0xFF2D3B31) else Color(0xFFE2EDE4))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = foodLog.foodName,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDarkMode) Color.White else Color(0xFF333333)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "(${foodLog.weightGrams.toInt()}g)",
                        fontSize = 11.sp,
                        color = if (isDarkMode) Color(0xFFAFAFAF) else Color(0xFF888888)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StatLabelPill(text = "${foodLog.calories.toInt()} kcal", color = if (isDarkMode) Color(0xFF81C784) else Color(0xFF2E7D32), isDarkMode = isDarkMode)
                    StatLabelPill(text = "P: ${foodLog.protein.toInt()}g", color = Color(0xFF4CAF50), isDarkMode = isDarkMode)
                    StatLabelPill(text = "C: ${foodLog.carbs.toInt()}g", color = Color(0xFFFFB300), isDarkMode = isDarkMode)
                    StatLabelPill(text = "F: ${foodLog.fats.toInt()}g", color = Color(0xFFE53935), isDarkMode = isDarkMode)
                }
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(if (isDarkMode) Color(0xFF3E1F1F) else Color(0xFFFFEBEE))
                    .testTag("btn_delete_food_${foodLog.id}"),
            ) {
                Icon(
                     imageVector = Icons.Filled.Delete,
                     contentDescription = "Delete food entry",
                     tint = if (isDarkMode) Color(0xFFFFB4AB) else Color(0xFFC62828),
                     modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun MacroGoalsSettingsCard(
    isDarkMode: Boolean,
    calorieGoal: Double,
    proteinGoal: Double,
    carbGoal: Double,
    fatGoal: Double,
    onSaveGoals: (Double, Double, Double, Double) -> Unit
) {
    var calories by remember { mutableStateOf(calorieGoal.toInt().toString()) }
    var protein by remember { mutableStateOf(proteinGoal.toInt().toString()) }
    var carbs by remember { mutableStateOf(carbGoal.toInt().toString()) }
    var fats by remember { mutableStateOf(fatGoal.toInt().toString()) }

    var isEditing by remember { mutableStateOf(false) }

    LaunchedEffect(calorieGoal, proteinGoal, carbGoal, fatGoal) {
        if (!isEditing) {
            calories = calorieGoal.toInt().toString()
            protein = proteinGoal.toInt().toString()
            carbs = carbGoal.toInt().toString()
            fats = fatGoal.toInt().toString()
        }
    }

    LaunchedEffect(calories, protein, carbs, fats) {
        if (isEditing) {
            kotlinx.coroutines.delay(500)
            val newCal = calories.toDoubleOrNull()
            val newPro = protein.toDoubleOrNull()
            val newCarb = carbs.toDoubleOrNull()
            val newFat = fats.toDoubleOrNull()
            if (newCal != null && newPro != null && newCarb != null && newFat != null) {
                if (newCal != calorieGoal || newPro != proteinGoal || newCarb != carbGoal || newFat != fatGoal) {
                    onSaveGoals(newCal, newPro, newCarb, newFat)
                }
            }
        }
    }

    val curP = protein.toDoubleOrNull() ?: 0.0
    val curC = carbs.toDoubleOrNull() ?: 0.0
    val curF = fats.toDoubleOrNull() ?: 0.0
    val calculatedCalories = (curP * 4) + (curC * 4) + (curF * 9)
    val inputCalories = calories.toDoubleOrNull() ?: 0.0
    val hasMismatch = isEditing && (Math.abs(calculatedCalories - inputCalories) > 5.0)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .testTag("macro_goals_settings_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDarkMode) Color(0xFF222823) else Color.White
        ),
        border = BorderStroke(1.dp, if (isDarkMode) Color(0xFF2D3B31) else Color(0xFFE2EDE4)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Manage Diet & Goals",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDarkMode) Color.White else Color(0xFF333333)
                )
                
                Button(
                    onClick = {
                        if (isEditing) {
                            val newCal = calories.toDoubleOrNull() ?: calorieGoal
                            val newPro = protein.toDoubleOrNull() ?: proteinGoal
                            val newCarb = carbs.toDoubleOrNull() ?: carbGoal
                            val newFat = fats.toDoubleOrNull() ?: fatGoal
                            onSaveGoals(newCal, newPro, newCarb, newFat)
                        }
                        isEditing = !isEditing
                    },
                    modifier = Modifier.height(34.dp).testTag("btn_edit_macro_goals"),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isDarkMode) Color(0xFF2E3D31) else Color(0xFFE8F5E9),
                        contentColor = if (isDarkMode) Color(0xFF81C784) else Color(0xFF2E7D32)
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp)
                ) {
                    Text(if (isEditing) "Save" else "Edit Goals", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (!isEditing) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    GoalQuickPill(label = "Calories", value = "${calorieGoal.toInt()} kcal", color = if (isDarkMode) Color(0xFF81C784) else Color(0xFF2E7D32), modifier = Modifier.weight(1f))
                    GoalQuickPill(label = "Protein", value = "${proteinGoal.toInt()}g", color = Color(0xFF4CAF50), modifier = Modifier.weight(1f))
                    GoalQuickPill(label = "Carbs", value = "${carbGoal.toInt()}g", color = Color(0xFFFFB300), modifier = Modifier.weight(1f))
                    GoalQuickPill(label = "Fats", value = "${fatGoal.toInt()}g", color = Color(0xFFE53935), modifier = Modifier.weight(1f))
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    AnimatedVisibility(visible = hasMismatch) {
                        Surface(
                            onClick = {
                                calories = calculatedCalories.toInt().toString()
                            },
                            color = if (isDarkMode) Color(0xFF3E2F1F) else Color(0xFFFFF3E0),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().testTag("macro_mismatch_warning")
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "⚠️ Live Calorie Mismatch Detected",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDarkMode) Color(0xFFFFB74D) else Color(0xFFE65100)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Your macros (P: ${curP.toInt()}g, C: ${curC.toInt()}g, F: ${curF.toInt()}g) require exactly ${calculatedCalories.toInt()} kcal, but your calorie goal is set to ${inputCalories.toInt()} kcal.",
                                    fontSize = 11.sp,
                                    color = if (isDarkMode) Color(0xFFECEFF1) else Color(0xFF37474F)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "👉 Tap here to automatically sync Calorie Goal to ${calculatedCalories.toInt()} kcal.",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDarkMode) Color(0xFFFFB74D) else Color(0xFFE65100)
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        GoalInputField(label = "Calories (kcal)", value = calories, onValueChange = { calories = it }, modifier = Modifier.weight(1f))
                        GoalInputField(label = "Protein (g)", value = protein, onValueChange = { protein = it }, modifier = Modifier.weight(1f))
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        GoalInputField(label = "Carbs (g)", value = carbs, onValueChange = { carbs = it }, modifier = Modifier.weight(1f))
                        GoalInputField(label = "Fats (g)", value = fats, onValueChange = { fats = it }, modifier = Modifier.weight(1f))
                    }
                    
                    Text(
                        text = "Calorie content based on custom macros: ${calculatedCalories.toInt()} kcal",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (isDarkMode) Color(0xFFAFAFAF) else Color(0xFF666666),
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}

