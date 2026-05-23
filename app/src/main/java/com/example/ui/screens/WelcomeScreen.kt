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
import com.example.IntakeViewModel
import java.util.Calendar
import java.util.Locale

@Composable
fun NeonProgressDial(
    progress: Float,
    valueText: String,
    subText: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    neonColor: Color,
    isDarkMode: Boolean,
    modifier: Modifier = Modifier
) {
    val textColor = if (isDarkMode) Color.White else Color(0xFF2E1A16)
    val labelColor = if (isDarkMode) Color(0xFFAFAFAF) else Color(0xFF705244)
    
    BoxWithConstraints(
        modifier = modifier
            .width(96.dp)
            .aspectRatio(1f),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize().padding(6.dp)) {
            val strokeWidth = 5.dp.toPx()
            val sweepAngle = 270f * progress.coerceIn(0f, 1f)
            
            // 1. Draw inactive background track (very subtle, low-opacity active color)
            drawArc(
                color = if (isDarkMode) neonColor.copy(alpha = 0.12f) else neonColor.copy(alpha = 0.08f),
                startAngle = 135f,
                sweepAngle = 270f,
                useCenter = false,
                style = Stroke(
                    width = strokeWidth - 1.dp.toPx(),
                    cap = StrokeCap.Round
                )
            )
            
            // 2. Draw active neon progress arc
            drawArc(
                color = neonColor,
                startAngle = 135f,
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
            modifier = Modifier.padding(bottom = 6.dp)
        ) {
            Text(
                text = valueText,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
            Text(
                text = subText,
                fontSize = 9.sp,
                fontWeight = FontWeight.Medium,
                color = labelColor
            )
        }
        
        // Icon at the bottom gap
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 2.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = neonColor,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
fun WelcomeHubLauncherCard(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    isDarkMode: Boolean,
    statusInfo: String,
    onClick: () -> Unit,
    testTag: String
) {
    val cardBg = if (isDarkMode) Color(0xFF1D1714) else Color.White
    val borderCol = if (isDarkMode) Color(0xFF332620) else Color(0xFFFFE6E1)
    val textTitleCol = if (isDarkMode) Color.White else Color(0xFF2E1A16)
    val textDescCol = if (isDarkMode) Color(0xFFAFAFAF) else Color(0xFF705244)

    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = BorderStroke(width = 1.dp, color = borderCol),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Glowing round corner container for icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(accentColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = accentColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = textTitleCol
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    fontSize = 12.sp,
                    color = textDescCol,
                    lineHeight = 16.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = accentColor.copy(alpha = 0.08f),
                    border = BorderStroke(0.5.dp, accentColor.copy(alpha = 0.2f))
                ) {
                    Text(
                        text = statusInfo,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = accentColor,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Navigate to $title",
                tint = if (isDarkMode) Color(0xFF705244) else Color(0xFFFFB4A2),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun WelcomeHubStatsOverview(
    isDarkMode: Boolean,
    totalCreatine: Int,
    totalProtein: Int,
    maxCreatine: Int,
    maxProtein: Int,
    totalWaterMl: Int,
    waterGoalLtr: Double,
    totalCalories: Int,
    calorieGoal: Int
) {
    val borderCol = if (isDarkMode) Color(0xFF332620) else Color(0xFFFFD1C5)
    val titleCol = if (isDarkMode) Color.White else Color(0xFF2E1A16)
    
    val totalScoops = totalCreatine + totalProtein
    val scoopsGoal = maxCreatine + maxProtein
    
    val scoopsProgress = if (scoopsGoal > 0) totalScoops.toFloat() / scoopsGoal.toFloat() else 0f
    val waterProgress = if (waterGoalLtr > 0) (totalWaterMl / 1000f) / waterGoalLtr.toFloat() else 0f
    val caloriesProgress = if (calorieGoal > 0) totalCalories.toFloat() / calorieGoal.toFloat() else 0f

    val cardBgBrush = if (isDarkMode) {
        Brush.verticalGradient(listOf(Color(0xFF231C19), Color(0xFF1D1714)))
    } else {
        Brush.verticalGradient(listOf(Color(0xFFFFFDFD), Color(0xFFFFF6F4)))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .background(cardBgBrush, RoundedCornerShape(24.dp))
            .testTag("welcome_stats_overview_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(1.dp, borderCol)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Text(
                text = "Today's Quick Summary",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = titleCol
            )
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Scoops Metric Column
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "SCOOPS",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDarkMode) Color(0xFFAFAFAF) else Color(0xFF705244),
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    NeonProgressDial(
                        progress = scoopsProgress,
                        valueText = "$totalScoops",
                        subText = "/ $scoopsGoal Goal",
                        icon = Icons.Filled.FitnessCenter,
                        neonColor = if (isDarkMode) Color(0xFFFF7A5C) else Color(0xFFD35400),
                        isDarkMode = isDarkMode
                    )
                }

                // Hydration Metric Column
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "HYDRATION",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDarkMode) Color(0xFFAFAFAF) else Color(0xFF705244),
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    NeonProgressDial(
                        progress = waterProgress,
                        valueText = String.format(Locale.US, "%.1fL", totalWaterMl / 1000f),
                        subText = String.format(Locale.US, "/ %.1fL", waterGoalLtr),
                        icon = Icons.Filled.Opacity,
                        neonColor = Color(0xFF00E5FF),
                        isDarkMode = isDarkMode
                    )
                }

                // Calories Metric Column
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "CALORIES",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDarkMode) Color(0xFFAFAFAF) else Color(0xFF705244),
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    NeonProgressDial(
                        progress = caloriesProgress,
                        valueText = String.format(Locale.US, "%,d", totalCalories),
                        subText = String.format(Locale.US, "/ %,d kcal", calorieGoal),
                        icon = Icons.Filled.Whatshot,
                        neonColor = if (isDarkMode) Color(0xFF69F0AE) else Color(0xFF2E7D32),
                        isDarkMode = isDarkMode
                    )
                }
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
    
    val maxCreatine by viewModel.creatineMax.collectAsStateWithLifecycle()
    val maxProtein by viewModel.proteinMax.collectAsStateWithLifecycle()
    
    val waterGoal by viewModel.waterGoal.collectAsStateWithLifecycle()
    val waterRemindersEnabled by viewModel.waterRemindersEnabled.collectAsStateWithLifecycle()
    
    val foodLogsTodayRaw by viewModel.foodLogsToday.collectAsStateWithLifecycle()
    val totalCalories = remember(foodLogsTodayRaw) {
        foodLogsTodayRaw.sumOf { it.calories }.toInt()
    }
    val calorieGoal by viewModel.calorieGoal.collectAsStateWithLifecycle()
    
    val todayIntake by viewModel.todayIntake.collectAsStateWithLifecycle()
    val todayCreatine = todayIntake?.creatineCount ?: 0
    val todayProtein = todayIntake?.proteinCount ?: 0
    
    val waterLogsTodayRaw by viewModel.waterLogsToday.collectAsStateWithLifecycle()
    val totalWaterMl = remember(waterLogsTodayRaw) {
        waterLogsTodayRaw.sumOf { it.amountMl }
    }

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
                    .background(if (isDarkMode) Color(0xFF1D1714) else Color(0xFFFFEBE6))
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
                        .background(if (isDarkMode) Color(0xFF1D1714) else Color(0xFFFFEBE6))
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
                        .background(if (isDarkMode) Color(0xFF1D1714) else Color(0xFFFFEBE6))
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
            totalCreatine = todayCreatine,
            totalProtein = todayProtein,
            maxCreatine = maxCreatine,
            maxProtein = maxProtein,
            totalWaterMl = totalWaterMl,
            waterGoalLtr = waterGoal,
            totalCalories = totalCalories,
            calorieGoal = calorieGoal.toInt()
        )

        // Launchers Section
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "QUICK ACCESS",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDarkMode) Color(0xFFAFAFAF) else Color(0xFF705244),
                letterSpacing = 0.5.sp,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
            )

            WelcomeHubLauncherCard(
                title = "Scoop Tracker",
                description = "Log your daily scoops",
                icon = Icons.Filled.FitnessCenter,
                accentColor = if (isDarkMode) Color(0xFFFF7A5C) else Color(0xFFD35400),
                isDarkMode = isDarkMode,
                statusInfo = "Protein Max: ${maxProtein}g • Creatine Max: ${maxCreatine}g",
                onClick = { onScreenChange("scoops") },
                testTag = "goto_scoops"
            )

            WelcomeHubLauncherCard(
                title = "Water Intake",
                description = "Track your water consumption",
                icon = Icons.Filled.Opacity,
                accentColor = Color(0xFF00E5FF),
                isDarkMode = isDarkMode,
                statusInfo = "Target goal: ${waterGoal} L • Reminders: ${if (waterRemindersEnabled) "On" else "Off"}",
                onClick = { onScreenChange("water") },
                testTag = "goto_water"
            )

            WelcomeHubLauncherCard(
                title = "Nutrition",
                description = "Log meals & view macros",
                icon = Icons.Filled.Restaurant,
                accentColor = if (isDarkMode) Color(0xFF69F0AE) else Color(0xFF2E7D32),
                isDarkMode = isDarkMode,
                statusInfo = "Calories Target: ${calorieGoal.toInt()} kcal • Logged: ${totalCalories} kcal",
                onClick = { onScreenChange("nutrition") },
                testTag = "goto_nutrition"
            )
        }

        // Bottom Navigation Quick Shortcuts (styled matching the neon theme)
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
                activeColor = if (isDarkMode) Color(0xFFFF7A5C) else Color(0xFFD35400),
                onClick = { onScreenChange("scoops") },
                modifier = Modifier.weight(1f).testTag("goto_scoops")
            )
            NavButton(
                text = "Nutrition",
                icon = Icons.Filled.Restaurant,
                isDark = isDarkMode,
                activeColor = if (isDarkMode) Color(0xFF69F0AE) else Color(0xFF2E7D32),
                onClick = { onScreenChange("nutrition") },
                modifier = Modifier.weight(1f).testTag("goto_nutrition")
            )
        }
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
