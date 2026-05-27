package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import com.example.IntakeViewModel
import java.util.Calendar
import java.util.Locale

@Composable
fun NeonProgressDial(
    progress: Float,
    valueMain: String,
    valueFraction: String,
    fractionOnSameLine: Boolean,
    percentageText: String,
    neonColor: Color,
    isDarkMode: Boolean,
    modifier: Modifier = Modifier,
    icon: (@Composable () -> Unit)? = null,
    indicators: (@Composable () -> Unit)? = null
) {
    val textColor = if (isDarkMode) Color.White else Color.Black
    val labelColor = Color.Gray
    
    BoxWithConstraints(
        modifier = modifier
            .width(120.dp)
            .aspectRatio(1f),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize().padding(8.dp)) {
            val strokeWidth = 8.dp.toPx()
            val sweepAngle = 360f * progress.coerceIn(0f, 1f)
            
            // 1. Draw inactive background track (full 360-degree circle)
            drawArc(
                color = if (isDarkMode) neonColor.copy(alpha = 0.15f) else neonColor.copy(alpha = 0.08f),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(
                    width = strokeWidth,
                    cap = StrokeCap.Round
                )
            )
            
            // 2. Draw active neon progress arc with Glow Effect
            if (sweepAngle > 0f && isDarkMode) {
                drawIntoCanvas { canvas ->
                    val paint = android.graphics.Paint().apply {
                        color = neonColor.toArgb()
                        this.strokeWidth = strokeWidth * 1.5f
                        style = android.graphics.Paint.Style.STROKE
                        strokeCap = android.graphics.Paint.Cap.ROUND
                        maskFilter = android.graphics.BlurMaskFilter(20f, android.graphics.BlurMaskFilter.Blur.NORMAL)
                    }
                    val rect = android.graphics.RectF(0f, 0f, size.width, size.height)
                    canvas.nativeCanvas.drawArc(rect, -90f, sweepAngle, false, paint)
                }
            }
            // Core bright arc
            drawArc(
                color = neonColor,
                startAngle = -90f,
                sweepAngle = sweepAngle,
                useCenter = false,
                style = Stroke(
                    width = strokeWidth,
                    cap = StrokeCap.Round
                )
            )
        }
        
        // Content inside circle
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(8.dp)
        ) {
            if (icon != null) {
                icon()
                Spacer(modifier = Modifier.height(2.dp))
            } else {
                Spacer(modifier = Modifier.height(2.dp))
            }
            
            if (fractionOnSameLine) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = valueMain,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = valueFraction,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Normal,
                        color = labelColor
                    )
                }
            } else {
                val isAction = valueMain.startsWith("Add")
                Text(
                    text = valueMain,
                    fontSize = if (isAction) 15.sp else 21.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(1.dp))
                Text(
                    text = valueFraction,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Normal,
                    color = labelColor
                )
            }
            
            if (indicators != null) {
                Spacer(modifier = Modifier.height(2.dp))
                indicators()
            }
            
            if (percentageText.isNotEmpty()) {
                Spacer(modifier = Modifier.height(1.dp))
                Text(
                    text = percentageText,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = neonColor
                )
            }
        }
    }
}

@Composable
fun WelcomeHubLauncherCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    isDarkMode: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String
) {
    val borderCol = if (isDarkMode) accentColor.copy(alpha = 0.4f) else Color.Black.copy(alpha = 0.15f)
    val cardBg = if (isDarkMode) Color(0xFF1C1C1E).copy(alpha = 0.8f) else Color(0xFFFFFFFF)

    Box(
        modifier = modifier
            .height(68.dp)
            .clickable(onClick = onClick)
            .testTag(testTag)
    ) {
        if (isDarkMode) {
            // Glow background layer
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(2.dp)
                    .blur(10.dp)
                    .background(
                        color = accentColor.copy(alpha = 0.25f),
                        shape = RoundedCornerShape(14.dp)
                    )
            )
        }
        
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(cardBg, RoundedCornerShape(14.dp))
                .border(BorderStroke(1.dp, borderCol), RoundedCornerShape(14.dp))
                .padding(horizontal = 8.dp, vertical = 8.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Icon Container
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            color = if (isDarkMode) Color(0xFF0F0F11) else Color(0xFFE8E8EC),
                            shape = RoundedCornerShape(8.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = accentColor,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Title Text
                Text(
                    text = title,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDarkMode) Color.White else Color.Black,
                    lineHeight = 13.sp
                )
            }
        }
    }
}

@Composable
fun WelcomeHubStatsOverview(
    isDarkMode: Boolean,
    supplements: List<com.example.data.Supplement>,
    supplementLogsToday: List<com.example.data.SupplementLog>,
    totalWaterMl: Int,
    waterGoalLtr: Double,
    totalCalories: Int,
    calorieGoal: Int,
    onScoopsDialClick: () -> Unit = {},
    onHydrationDialClick: () -> Unit = {},
    onCaloriesDialClick: () -> Unit = {}
) {
    val totalActiveSupplements = supplements.size
    val numberLoggedToday = supplements.count { supp ->
        val loggedAmount = supplementLogsToday.filter { it.supplementId == supp.id }.sumOf { it.amount }
        loggedAmount >= supp.dailyTarget
    }
    
    val scoopsProgress = if (totalActiveSupplements > 0) numberLoggedToday.toFloat() / totalActiveSupplements.toFloat() else 0f
    val waterProgress = if (waterGoalLtr > 0) (totalWaterMl / 1000f) / waterGoalLtr.toFloat() else 0f
    val caloriesProgress = if (calorieGoal > 0) totalCalories.toFloat() / calorieGoal.toFloat() else 0f

    val scoopsPct = if (totalActiveSupplements > 0) (numberLoggedToday * 100) / totalActiveSupplements else 0
    val waterPct = if (waterGoalLtr > 0) ((totalWaterMl / 1000f) / waterGoalLtr.toFloat() * 100).toInt() else 0
    val caloriesPct = if (calorieGoal > 0) (totalCalories * 100) / calorieGoal else 0

    val scoopsColor = if (isDarkMode) Color(0xFFFF7A5C) else Color(0xFFD35400)
    val waterColor = Color(0xFF00E5FF)
    val caloriesColor = Color(0xFF21D021)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
            .testTag("welcome_stats_overview_card")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Scoops Metric Column
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .weight(1f)
                    .clickable { onScoopsDialClick() }
            ) {
                val headerShadow = if (isDarkMode) {
                    Shadow(color = scoopsColor.copy(alpha = 0.6f), blurRadius = 12f)
                } else null
                Text(
                    text = "TUBS",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDarkMode) scoopsColor else Color(0xFF0B0B0C),
                    letterSpacing = 0.5.sp,
                    style = TextStyle(shadow = headerShadow)
                )
                Spacer(modifier = Modifier.height(12.dp))
                NeonProgressDial(
                    progress = scoopsProgress,
                    valueMain = if (totalActiveSupplements > 0) "$numberLoggedToday" else "Add Tub",
                    valueFraction = if (totalActiveSupplements > 0) "/ $totalActiveSupplements" else "tap to start",
                    fractionOnSameLine = totalActiveSupplements > 0,
                    percentageText = "",
                    neonColor = scoopsColor,
                    isDarkMode = isDarkMode,
                    icon = if (totalActiveSupplements == 0) {
                        {
                            Icon(
                                imageVector = Icons.Default.Medication,
                                contentDescription = null,
                                tint = scoopsColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    } else null,
                    indicators = if (totalActiveSupplements > 0) {
                        {
                            val row1 = supplements.take(5)
                            val row2 = supplements.drop(5).take(5)
                            
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                // Row 1
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    row1.forEach { supp ->
                                        val color = try { Color(android.graphics.Color.parseColor(supp.colorTag)) } catch(e: Exception) { Color.Gray }
                                        val loggedAmount = supplementLogsToday.filter { it.supplementId == supp.id }.sumOf { it.amount }
                                        val isTargetMet = loggedAmount >= supp.dailyTarget
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .background(
                                                    color = if (isTargetMet) color else color.copy(alpha = 0.2f),
                                                    shape = CircleShape
                                                )
                                                .border(
                                                    width = 0.5.dp,
                                                    color = if (isTargetMet) Color.Transparent else color.copy(alpha = 0.5f),
                                                    shape = CircleShape
                                                )
                                        )
                                    }
                                }
                                
                                // Row 2
                                if (row2.isNotEmpty()) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        row2.forEach { supp ->
                                            val color = try { Color(android.graphics.Color.parseColor(supp.colorTag)) } catch(e: Exception) { Color.Gray }
                                            val loggedAmount = supplementLogsToday.filter { it.supplementId == supp.id }.sumOf { it.amount }
                                            val isTargetMet = loggedAmount >= supp.dailyTarget
                                            Box(
                                                modifier = Modifier
                                                    .size(6.dp)
                                                    .background(
                                                        color = if (isTargetMet) color else color.copy(alpha = 0.2f),
                                                        shape = CircleShape
                                                    )
                                                    .border(
                                                        width = 0.5.dp,
                                                        color = if (isTargetMet) Color.Transparent else color.copy(alpha = 0.5f),
                                                        shape = CircleShape
                                                    )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    } else null
                )
            }

            // Hydration Metric Column
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .weight(1f)
                    .clickable { onHydrationDialClick() }
            ) {
                val headerShadow = if (isDarkMode) {
                    Shadow(color = waterColor.copy(alpha = 0.6f), blurRadius = 12f)
                } else null
                Text(
                    text = "HYDRATION",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDarkMode) waterColor else Color(0xFF0B0B0C),
                    letterSpacing = 0.5.sp,
                    style = TextStyle(shadow = headerShadow)
                )
                Spacer(modifier = Modifier.height(12.dp))
                NeonProgressDial(
                    progress = waterProgress,
                    valueMain = if (totalWaterMl == 0) "Add Water" else String.format(Locale.US, "%.1fL", totalWaterMl / 1000f),
                    valueFraction = if (totalWaterMl == 0) "tap to start" else String.format(Locale.US, "/ %.1fL", waterGoalLtr),
                    fractionOnSameLine = false,
                    percentageText = if (totalWaterMl == 0) "" else "$waterPct%",
                    neonColor = waterColor,
                    isDarkMode = isDarkMode,
                    icon = if (totalWaterMl == 0) {
                        {
                            Icon(
                                imageVector = Icons.Default.Opacity,
                                contentDescription = null,
                                tint = waterColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    } else null
                )
            }

            // Calories Metric Column
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .weight(1f)
                    .clickable { onCaloriesDialClick() }
            ) {
                val headerShadow = if (isDarkMode) {
                    Shadow(color = caloriesColor.copy(alpha = 0.6f), blurRadius = 12f)
                } else null
                Text(
                    text = "CALORIES",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDarkMode) caloriesColor else Color(0xFF0B0B0C),
                    letterSpacing = 0.5.sp,
                    style = TextStyle(shadow = headerShadow)
                )
                Spacer(modifier = Modifier.height(12.dp))
                NeonProgressDial(
                    progress = caloriesProgress,
                    valueMain = if (totalCalories == 0) "Add Food" else String.format(Locale.US, "%,d", totalCalories),
                    valueFraction = if (totalCalories == 0) "tap to start" else String.format(Locale.US, "kcal / %,d", calorieGoal),
                    fractionOnSameLine = false,
                    percentageText = if (totalCalories == 0) "" else "$caloriesPct%",
                    neonColor = caloriesColor,
                    isDarkMode = isDarkMode,
                    icon = if (totalCalories == 0) {
                        {
                            Icon(
                                imageVector = Icons.Default.Restaurant,
                                contentDescription = null,
                                tint = caloriesColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    } else null
                )
            }
        }
    }
}

@Composable
fun NavButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isDark: Boolean,
    activeColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        shape = RoundedCornerShape(24.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = activeColor
        ),
        border = BorderStroke(
            width = 1.dp,
            color = activeColor.copy(alpha = 0.4f)
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                tint = activeColor,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = text,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDark) Color.White else Color(0xFF2E1A16)
            )
        }
    }
}

@Composable
fun WelcomeScreen(
    viewModel: IntakeViewModel,
    isDarkMode: Boolean,
    activeProfile: String,
    profile1Name: String,
    profile2Name: String,
    onScreenChange: (String) -> Unit
) {
    val context = LocalContext.current
    
    val waterGoal by viewModel.waterGoal.collectAsStateWithLifecycle()
    val waterRemindersEnabled by viewModel.waterRemindersEnabled.collectAsStateWithLifecycle()
    
    val foodLogsTodayRaw by viewModel.foodLogsToday.collectAsStateWithLifecycle()
    val totalCalories = remember(foodLogsTodayRaw) {
        foodLogsTodayRaw.sumOf { it.calories }.toInt()
    }
    val calorieGoal by viewModel.calorieGoal.collectAsStateWithLifecycle()
    
    val waterLogsTodayRaw by viewModel.waterLogsToday.collectAsStateWithLifecycle()
    val totalWaterMl = remember(waterLogsTodayRaw) {
        waterLogsTodayRaw.sumOf { it.amountMl }
    }

    val supplements by viewModel.supplements.collectAsStateWithLifecycle()
    val supplementLogsToday by viewModel.supplementLogsToday.collectAsStateWithLifecycle()

    val activeProfileName = if (activeProfile == "profile_1") profile1Name else profile2Name
    
    val greeting = remember {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        when (hour) {
            in 0..11 -> "Good morning,"
            in 12..16 -> "Good afternoon,"
            else -> "Good evening,"
        }
    }

    var showProfileDialog by remember { mutableStateOf(false) }

    val avatarBrush = remember {
        Brush.sweepGradient(listOf(Color(0xFF00E5FF), Color(0xFFFF7A5C), Color(0xFF69F0AE), Color(0xFF00E5FF)))
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Custom Greeting Header Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Profile avatar with neon glowing border
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .border(BorderStroke(2.dp, avatarBrush), CircleShape)
                    .clip(CircleShape)
                    .background(Color.Transparent)
                    .clickable { showProfileDialog = true }
                    .testTag("avatar_profile_navigation"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = activeProfileName.take(1).uppercase(),
                    color = if (isDarkMode) Color.White else Color(0xFF531B10),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            // Top action buttons: Notifications reminder bell & Dark Mode toggle
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                IconButton(
                    onClick = { viewModel.toggleReminders(context) },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.Transparent)
                ) {
                    Icon(
                        imageVector = if (waterRemindersEnabled) Icons.Filled.Notifications else Icons.Filled.NotificationsNone,
                        contentDescription = "Toggle Reminders",
                        tint = if (isDarkMode) Color.White else Color(0xFF531B10),
                        modifier = Modifier.size(20.dp)
                    )
                }

                IconButton(
                    onClick = { viewModel.toggleDarkMode() },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.Transparent)
                ) {
                    Icon(
                        imageVector = if (isDarkMode) Icons.Filled.NightsStay else Icons.Filled.WbSunny,
                        contentDescription = "Toggle Theme",
                        tint = if (isDarkMode) Color(0xFFFFF59D) else Color(0xFFF57C00),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Welcome back large typography card
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp)
                .testTag("welcome_header_card")
        ) {
            Text(
                text = greeting,
                fontSize = 24.sp,
                fontWeight = FontWeight.Normal,
                color = if (isDarkMode) Color(0xFFD6C2BD) else Color(0xFF704238)
            )
            Text(
                text = activeProfileName,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDarkMode) Color.White else Color(0xFF531B10)
            )
        }

        // Stats summary circular dial panel
        WelcomeHubStatsOverview(
            isDarkMode = isDarkMode,
            supplements = supplements,
            supplementLogsToday = supplementLogsToday,
            totalWaterMl = totalWaterMl,
            waterGoalLtr = waterGoal,
            totalCalories = totalCalories,
            calorieGoal = calorieGoal.toInt(),
            onScoopsDialClick = { onScreenChange("scoops") },
            onHydrationDialClick = { onScreenChange("water") },
            onCaloriesDialClick = { onScreenChange("nutrition") }
        )


    }

    // Switch / Edit Profile dialog (copied from HeaderSection to remain accessible)
    if (showProfileDialog) {
        var p1TempName by remember(profile1Name) { 
            mutableStateOf(if (profile1Name == "Me") "" else profile1Name) 
        }
        var p2TempName by remember(profile2Name) { 
            mutableStateOf(if (profile2Name == "Person 2") "" else profile2Name) 
        }
        val dialogCardBg = if (isDarkMode) Color(0xFF1D1714) else Color(0xFFFFFFFF)
        val activeSelectionColor = if (isDarkMode) Color.White else Color(0xFF531B10)

        AlertDialog(
            onDismissRequest = { showProfileDialog = false },
            title = {
                Text(
                    text = "Manage Profiles",
                    color = if (isDarkMode) Color.White else Color(0xFF2E1A16),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (activeProfile == "profile_1") {
                                if (isDarkMode) Color(0xFF2D221E) else Color(0xFFFFEBE6)
                            } else Color.Transparent
                        ),
                        border = BorderStroke(
                            width = if (activeProfile == "profile_1") 2.dp else 1.dp,
                            color = if (activeProfile == "profile_1") {
                                if (isDarkMode) Color.White else activeSelectionColor
                            } else {
                                if (isDarkMode) Color.White.copy(alpha = 0.2f) else Color.LightGray
                            }
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.switchProfile("profile_1") },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (activeProfile == "profile_1") "$profile1Name (Active)" else profile1Name,
                                    color = if (isDarkMode) Color.White else Color(0xFF531B10),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                if (activeProfile == "profile_1") {
                                    Icon(
                                        imageVector = Icons.Filled.CheckCircle,
                                        contentDescription = "Active",
                                        tint = if (isDarkMode) Color.White else activeSelectionColor,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = p1TempName,
                                onValueChange = {
                                    p1TempName = it
                                    val finalName = if (it.trim().isEmpty()) "Me" else it
                                    viewModel.renameProfile("profile_1", finalName)
                                },
                                placeholder = { Text("Me", color = Color.Gray) },
                                label = { Text("Custom Name") },
                                shape = RoundedCornerShape(8.dp),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (activeProfile == "profile_2") {
                                if (isDarkMode) Color(0xFF2D221E) else Color(0xFFFFEBE6)
                            } else Color.Transparent
                        ),
                        border = BorderStroke(
                            width = if (activeProfile == "profile_2") 2.dp else 1.dp,
                            color = if (activeProfile == "profile_2") {
                                if (isDarkMode) Color.White else activeSelectionColor
                            } else {
                                if (isDarkMode) Color.White.copy(alpha = 0.2f) else Color.LightGray
                            }
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.switchProfile("profile_2") },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (activeProfile == "profile_2") "$profile2Name (Active)" else profile2Name,
                                    color = if (isDarkMode) Color.White else Color(0xFF531B10),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                if (activeProfile == "profile_2") {
                                    Icon(
                                        imageVector = Icons.Filled.CheckCircle,
                                        contentDescription = "Active",
                                        tint = if (isDarkMode) Color.White else activeSelectionColor,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = p2TempName,
                                onValueChange = {
                                    p2TempName = it
                                    val finalName = if (it.trim().isEmpty()) "Person 2" else it
                                    viewModel.renameProfile("profile_2", finalName)
                                },
                                placeholder = { Text("Person 2", color = Color.Gray) },
                                label = { Text("Custom Name") },
                                shape = RoundedCornerShape(8.dp),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showProfileDialog = false },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isDarkMode) Color(0xFFBD8E85) else activeSelectionColor,
                        contentColor = if (isDarkMode) Color(0xFF2E0904) else Color.White
                    )
                ) {
                    Text("Done")
                }
            },
            containerColor = dialogCardBg
        )
    }
}
