package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.IntakeViewModel
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalActivity
import androidx.compose.material.icons.filled.CameraEnhance
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun WelcomeDashboardHeaderCard(
    isDarkMode: Boolean,
    profileName: String,
    avatarBackground: Color,
    avatarText: Color
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .testTag("welcome_header_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDarkMode) Color(0xFF231A16) else Color(0xFFFFEBE6)
        ),
        border = BorderStroke(1.dp, if (isDarkMode) Color(0xFF422B22) else Color(0xFFFFD1C5))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Welcome back, $profileName!",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDarkMode) Color.White else Color(0xFF531B10)
                )
                Text(text = "✨", fontSize = 20.sp)
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Keep your body balanced and strong. Pick a wellness tracker below to update your daily goals, check charts, or register logs.",
                fontSize = 13.sp,
                lineHeight = 18.sp,
                color = if (isDarkMode) Color(0xFFD6C2BD) else Color(0xFF704238)
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
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDarkMode) Color(0xFF1D1714) else Color.White
        ),
        border = BorderStroke(
            width = 1.dp,
            color = if (isDarkMode) Color(0xFF332620) else Color(0xFFEDF2F4)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
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
                    color = if (isDarkMode) Color.White else Color(0xFF2B2D42)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    fontSize = 12.sp,
                    color = if (isDarkMode) Color(0xFFAFAFAF) else Color(0xFF6C757D),
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
                tint = if (isDarkMode) Color(0xFF705244) else Color(0xFF8D99AE),
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
    totalWaterMl: Int,
    waterGoalLtr: Double,
    totalCalories: Int,
    calorieGoal: Int
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .testTag("welcome_stats_overview_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDarkMode) Color(0xFF1B1B1F) else Color(0xFFF1F5F9)
        ),
        border = BorderStroke(1.dp, if (isDarkMode) Color(0xFF2B2B30) else Color(0xFFE2E8F0))
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
                color = if (isDarkMode) Color.White else Color(0xFF1E293B)
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Scoops stat
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .background(
                            if (isDarkMode) Color(0xFF241D1A) else Color.White,
                            RoundedCornerShape(12.dp)
                        )
                        .border(
                            1.dp,
                            if (isDarkMode) Color(0xFF332620) else Color(0xFFE2E8F0),
                            RoundedCornerShape(12.dp)
                        )
                        .padding(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "🏋️‍♂️ Scoops", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = if (isDarkMode) Color.LightGray else Color(0xFF475569))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "P: ${totalProtein} | C: ${totalCreatine}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDarkMode) Color.White else Color(0xFF1E293B)
                    )
                }

                // Water stat
                Column(
                    modifier = Modifier
                        .weight(1.2f)
                        .background(
                            if (isDarkMode) Color(0xFF241D1A) else Color.White,
                            RoundedCornerShape(12.dp)
                        )
                        .border(
                            1.dp,
                            if (isDarkMode) Color(0xFF332620) else Color(0xFFE2E8F0),
                            RoundedCornerShape(12.dp)
                        )
                        .padding(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "💧 Hydration", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = if (isDarkMode) Color.LightGray else Color(0xFF475569))
                    Spacer(modifier = Modifier.height(4.dp))
                    val waterGoalMl = (waterGoalLtr * 1000).toInt()
                    Text(
                        text = "$totalWaterMl / $waterGoalMl ml",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF00ACC1)
                    )
                }

                // Calories stat
                Column(
                    modifier = Modifier
                        .weight(1.1f)
                        .background(
                            if (isDarkMode) Color(0xFF241D1A) else Color.White,
                            RoundedCornerShape(12.dp)
                        )
                        .border(
                            1.dp,
                            if (isDarkMode) Color(0xFF332620) else Color(0xFFE2E8F0),
                            RoundedCornerShape(12.dp)
                        )
                        .padding(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "🍎 Calories", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = if (isDarkMode) Color.LightGray else Color(0xFF475569))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$totalCalories / $calorieGoal kcal",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDarkMode) Color.White else Color(0xFF2E7D32)
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
            containerColor = if (isDark) Color(0xFF1F1B1A) else Color(0xFFF7F2FA),
            contentColor = activeColor
        ),
        border = BorderStroke(
            width = 1.dp,
            color = if (isDark) Color(0xFF332620) else Color(0xFFEDF2F4)
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
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
                color = if (isDark) Color.White else Color(0xFF333333)
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
    val maxCreatine by viewModel.creatineMax.collectAsStateWithLifecycle()
    val maxProtein by viewModel.proteinMax.collectAsStateWithLifecycle()
    
    val waterGoal by viewModel.waterGoal.collectAsStateWithLifecycle()
    val waterRemindersEnabled by viewModel.waterRemindersEnabled.collectAsStateWithLifecycle()
    
    val foodLogsTodayRaw by viewModel.foodLogsToday.collectAsStateWithLifecycle()
    val totalCalories = remember(foodLogsTodayRaw) {
        foodLogsTodayRaw.sumOf { it.calories }.toInt()
    }
    val calorieGoal by viewModel.calorieGoal.collectAsStateWithLifecycle()
    
    val historyRaw by viewModel.intakesHistory.collectAsStateWithLifecycle()
    val totalCreatineCount = remember(historyRaw) {
        historyRaw.sumOf { it.creatineCount }
    }
    val totalProteinCount = remember(historyRaw) {
        historyRaw.sumOf { it.proteinCount }
    }
    
    val waterLogsTodayRaw by viewModel.waterLogsToday.collectAsStateWithLifecycle()
    val totalWaterMl = remember(waterLogsTodayRaw) {
        waterLogsTodayRaw.sumOf { it.amountMl }
    }

    val AvatarBackground = if (isDarkMode) Color(0xFFBD8E85) else Color(0xFFEAC2BA)
    val AvatarText = if (isDarkMode) Color(0xFF2E0904) else Color(0xFF531B10)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        WelcomeDashboardHeaderCard(
            isDarkMode = isDarkMode,
            profileName = if (activeProfile == "profile_1") profile1Name else profile2Name,
            avatarBackground = AvatarBackground,
            avatarText = AvatarText
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Track your wellness metrics:",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDarkMode) Color.White else Color(0xFF333333),
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
            )

            WelcomeHubLauncherCard(
                title = "Daily Scoop Tracker",
                description = "Monitor your professional creatine loading & protein intake counts with precision log charts.",
                icon = Icons.Filled.FitnessCenter,
                accentColor = if (isDarkMode) Color(0xFFFFB4A2) else Color(0xFF9C432F),
                isDarkMode = isDarkMode,
                statusInfo = "Protein Max: ${maxProtein}g • Creatine Max: ${maxCreatine}g",
                onClick = { onScreenChange("scoops") },
                testTag = "goto_scoops"
            )

            WelcomeHubLauncherCard(
                title = "Daily Hydration Water",
                description = "Monitor your hydration logs with personalized reminder times and direct quick-tap selectors.",
                icon = Icons.Filled.LocalActivity,
                accentColor = Color(0xFF00ACC1),
                isDarkMode = isDarkMode,
                statusInfo = "Target goal: ${waterGoal} L • Reminders: ${if (waterRemindersEnabled) "On" else "Off"}",
                onClick = { onScreenChange("water") },
                testTag = "goto_water"
            )

            WelcomeHubLauncherCard(
                title = "Nutrition Labels Scanner",
                description = "Log your daily meals with ease by scanning the label image to pull stats automatically using AI.",
                icon = Icons.Filled.CameraEnhance,
                accentColor = if (isDarkMode) Color(0xFF81C784) else Color(0xFF2E7D32),
                isDarkMode = isDarkMode,
                statusInfo = "Calories Target: ${calorieGoal.toInt()} kcal • Logged: ${totalCalories} kcal",
                onClick = { onScreenChange("nutrition") },
                testTag = "goto_nutrition"
            )
        }

        WelcomeHubStatsOverview(
            isDarkMode = isDarkMode,
            totalCreatine = totalCreatineCount,
            totalProtein = totalProteinCount,
            totalWaterMl = totalWaterMl,
            waterGoalLtr = waterGoal,
            totalCalories = totalCalories,
            calorieGoal = calorieGoal.toInt()
        )

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
                text = "Nutrition",
                icon = Icons.Filled.Restaurant,
                isDark = isDarkMode,
                activeColor = if (isDarkMode) Color(0xFF81C784) else Color(0xFF2E7D32),
                onClick = { onScreenChange("nutrition") },
                modifier = Modifier.weight(1f).testTag("goto_nutrition")
            )
        }
    }
}
