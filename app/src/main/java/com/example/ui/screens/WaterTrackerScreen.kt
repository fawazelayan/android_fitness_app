package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Restaurant
import com.example.ui.components.NavButton
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
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
import com.example.StableWaterLogs
import com.example.ui.components.CircularTimeSelector
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun WaterTrackerScreen(
    viewModel: IntakeViewModel,
    isDarkMode: Boolean,
    onScreenChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val waterLogsTodayRaw by viewModel.waterLogsToday.collectAsStateWithLifecycle()
    val waterLogsToday = remember(waterLogsTodayRaw) { StableWaterLogs(waterLogsTodayRaw) }
    val waterGoal by viewModel.waterGoal.collectAsStateWithLifecycle()
    val waterRemindersEnabled by viewModel.waterRemindersEnabled.collectAsStateWithLifecycle()
    val waterReminderHour by viewModel.waterReminderHour.collectAsStateWithLifecycle()
    val waterReminderMinute by viewModel.waterReminderMinute.collectAsStateWithLifecycle()

    val context = LocalContext.current
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
                text = "Nutrition",
                icon = Icons.Filled.Restaurant,
                isDark = isDarkMode,
                activeColor = if (isDarkMode) Color(0xFF81C784) else Color(0xFF2E7D32),
                onClick = { onScreenChange("nutrition") },
                modifier = Modifier.weight(1f).testTag("goto_nutrition")
            )
        }

        WaterProgressDashboardCard(
            isDarkMode = isDarkMode,
            todayLogs = waterLogsToday,
            dailyGoal = waterGoal,
            onAddQuickWater = { ml -> viewModel.addWaterLog(ml) }
        )

        CustomWaterLogCard(
            isDarkMode = isDarkMode,
            onAddCustomWater = { ml -> viewModel.addWaterLog(ml) }
        )

        WaterSettingsCard(
            isDarkMode = isDarkMode,
            dailyGoal = waterGoal,
            onSaveGoal = { liters -> viewModel.setWaterGoal(liters) },
            waterRemindersEnabled = waterRemindersEnabled,
            waterReminderHour = waterReminderHour,
            waterReminderMinute = waterReminderMinute,
            onToggleReminders = { viewModel.toggleWaterReminders(context) },
            onChangeReminderTime = { h, m -> viewModel.changeWaterReminderTime(context, h, m) }
        )

        WaterLogsHistoryCard(
            isDarkMode = isDarkMode,
            todayLogs = waterLogsToday,
            onDeleteLog = { id -> viewModel.deleteWaterLog(id) }
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun WaterProgressDashboardCard(
    isDarkMode: Boolean,
    todayLogs: StableWaterLogs,
    dailyGoal: Double,
    onAddQuickWater: (Int) -> Unit
) {
    val totalLoggedMl = remember(todayLogs) { todayLogs.list.sumOf { it.amountMl } }
    val goalMl = remember(dailyGoal) { (dailyGoal * 1000).toInt() }
    val progress = remember(totalLoggedMl, goalMl) { if (goalMl > 0) (totalLoggedMl.toFloat() / goalMl.toFloat()).coerceIn(0f, 1f) else 0f }
    val percentString = remember(progress) { "${(progress * 100).toInt()}%" }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isDarkMode) Color(0xFF0F2633) else Color(0xFFE3F2FD)
        ),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, if (isDarkMode) Color(0xFF1B4E6C) else Color(0xFFBBDEFB)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Water Intake Goal",
                        color = if (isDarkMode) Color(0xFF80D8FF) else Color(0xFF0288D1),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Goal: $dailyGoal L  |  Logged: $totalLoggedMl mL",
                        color = if (isDarkMode) Color(0xFFFFFFFF) else Color(0xFF01579B),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                Text(
                    text = "💧",
                    fontSize = 32.sp
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                val progressColors = remember { listOf(Color(0xFF00B0FF), Color(0xFF2979FF)) }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(18.dp)
                        .clip(RoundedCornerShape(9.dp))
                        .background(if (isDarkMode) Color(0xFF081A24) else Color(0xFFBBDEFB))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progress)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(9.dp))
                            .background(
                                Brush.horizontalGradient(
                                    colors = progressColors
                                )
                            )
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = percentString + " achieved",
                        color = if (isDarkMode) Color(0xFFB3E5FC) else Color(0xFF0288D1),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    val statusText = if (totalLoggedMl >= goalMl) "Hydrated! 🎉" else "${goalMl - totalLoggedMl} mL remaining"
                    Text(
                        text = statusText,
                        color = if (isDarkMode) Color(0xFFB3E5FC) else Color(0xFF0288D1),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            HorizontalDivider(
                color = if (isDarkMode) Color(0xFF1B4E6C).copy(alpha = 0.5f) else Color(0xFF90CAF9).copy(alpha = 0.5f)
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Quick Log Shortcut:",
                    color = if (isDarkMode) Color(0xFFB3E5FC) else Color(0xFF0288D1),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val presets = remember { listOf(150, 250, 330, 500) }
                    val labels = remember { listOf("150 mL", "250 mL", "330 mL", "500 mL") }
                    presets.forEachIndexed { idx, ml ->
                        Button(
                            onClick = { 
                                onAddQuickWater(ml)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isDarkMode) Color(0xFF0091EA) else Color(0xFF0288D1)
                            ),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .testTag("quick_water_${ml}ml")
                        ) {
                            Text(
                                text = labels[idx],
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CustomWaterLogCard(
    isDarkMode: Boolean,
    onAddCustomWater: (Int) -> Unit
) {
    var customInput by remember { mutableStateOf("") }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isDarkMode) Color(0xFF14222D) else Color(0xFFFFFFFF)
        ),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, if (isDarkMode) Color(0xFF1B3545) else Color(0xFFBBDEFB)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Log Custom Intake",
                color = if (isDarkMode) Color(0xFFFFFFFF) else Color(0xFF01579B),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = customInput,
                    onValueChange = { newValue ->
                        val clean = newValue.filter { it.isDigit() }
                        if (clean.length <= 5) {
                            customInput = clean
                        }
                    },
                    placeholder = { Text("e.g. 750", color = if (isDarkMode) Color(0xFF74797A) else Color(0xFFAFAFAF)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF0288D1),
                        unfocusedBorderColor = if (isDarkMode) Color(0xFF1B3545) else Color(0xFFBBDEFB),
                        focusedLabelColor = Color(0xFF0288D1),
                        cursorColor = Color(0xFF0288D1),
                        focusedTextColor = if (isDarkMode) Color.White else Color.Black,
                        unfocusedTextColor = if (isDarkMode) Color.White else Color.Black
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("custom_water_input")
                )

                Button(
                    onClick = {
                        val parsed = customInput.toIntOrNull()
                        if (parsed != null && parsed > 0) {
                            onAddCustomWater(parsed)
                            customInput = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF00ACC1)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .height(52.dp)
                        .testTag("submit_custom_water")
                ) {
                    Text(
                        text = "+ Log mL",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun WaterSettingsCard(
    isDarkMode: Boolean,
    dailyGoal: Double,
    onSaveGoal: (Double) -> Unit,
    waterRemindersEnabled: Boolean,
    waterReminderHour: Int,
    waterReminderMinute: Int,
    onToggleReminders: () -> Unit,
    onChangeReminderTime: (Int, Int) -> Unit
) {
    var showSettingsExpansion by remember { mutableStateOf(false) }
    var inputGoalLiters by remember { mutableStateOf(dailyGoal.toString()) }

    LaunchedEffect(dailyGoal) {
        val parsed = inputGoalLiters.toDoubleOrNull()
        if (parsed != dailyGoal) {
            inputGoalLiters = dailyGoal.toString()
        }
    }

    LaunchedEffect(inputGoalLiters) {
        kotlinx.coroutines.delay(500)
        val parsed = inputGoalLiters.toDoubleOrNull()
        if (parsed != null && parsed in 0.1..20.0 && parsed != dailyGoal) {
            onSaveGoal(parsed)
        }
    }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isDarkMode) Color(0xFF14222D) else Color(0xFFFFFFFF)
        ),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, if (isDarkMode) Color(0xFF1B3545) else Color(0xFFBBDEFB)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showSettingsExpansion = !showSettingsExpansion }
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.WaterDrop,
                        contentDescription = "Water settings",
                        tint = Color(0xFF00ACC1),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Customize Water Targets & Reminders",
                        color = if (isDarkMode) Color.White else Color(0xFF01579B),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Icon(
                    imageVector = if (showSettingsExpansion) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                    contentDescription = "Expand toggle",
                    tint = if (isDarkMode) Color.White.copy(alpha = 0.6f) else Color.Black.copy(alpha = 0.5f)
                )
            }

            AnimatedVisibility(
                visible = showSettingsExpansion,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Daily Water Goal (Liters)",
                            color = if (isDarkMode) Color(0xFFB3E5FC) else Color(0xFF0288D1),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = inputGoalLiters,
                                onValueChange = { newValue ->
                                    val clean = newValue.filter { it.isDigit() || it == '.' }
                                    if (clean.length <= 5) {
                                        inputGoalLiters = clean
                                    }
                                },
                                placeholder = { Text("e.g. 2.5", color = if (isDarkMode) Color(0xFF74797A) else Color(0xFFAFAFAF)) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF00ACC1),
                                    unfocusedBorderColor = if (isDarkMode) Color(0xFF1B3545) else Color(0xFFBBDEFB),
                                    cursorColor = Color(0xFF00ACC1),
                                    focusedTextColor = if (isDarkMode) Color.White else Color.Black,
                                    unfocusedTextColor = if (isDarkMode) Color.White else Color.Black
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("water_goal_input")
                            )
                        }
                    }

                    HorizontalDivider(
                        color = if (isDarkMode) Color(0xFF1B3545).copy(alpha = 0.5f) else Color(0xFFBBDEFB).copy(alpha = 0.5f)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Daily Hydration Reminder",
                                color = if (isDarkMode) Color.White else Color.Black,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            val displayTime = String.format(Locale.getDefault(), "%02d:%02d", waterReminderHour, waterReminderMinute)
                            Text(
                                text = if (waterRemindersEnabled) "Reminds at $displayTime" else "Disabled",
                                color = if (isDarkMode) Color(0xFFB3E5FC) else Color(0xFF0288D1),
                                fontSize = 12.sp
                            )
                        }

                        Switch(
                            checked = waterRemindersEnabled,
                            onCheckedChange = { onToggleReminders() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFF00ACC1)
                            ),
                            modifier = Modifier.testTag("water_reminder_toggle")
                        )
                    }

                    AnimatedVisibility(
                        visible = waterRemindersEnabled,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        CircularTimeSelector(
                            hour = waterReminderHour,
                            minute = waterReminderMinute,
                            onTimeChanged = { h, m ->
                                onChangeReminderTime(h, m)
                            },
                            activeColor = Color(0xFF00ACC1),
                            isDarkMode = isDarkMode,
                            testTagPrefix = "water_reminder"
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WaterLogsHistoryCard(
    isDarkMode: Boolean,
    todayLogs: StableWaterLogs,
    onDeleteLog: (Int) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isDarkMode) Color(0xFF14222D) else Color(0xFFFFFFFF)
        ),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, if (isDarkMode) Color(0xFF1B3545) else Color(0xFFBBDEFB)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Today's Water Log History",
                color = if (isDarkMode) Color.White else Color(0xFF01579B),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )

            if (todayLogs.list.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No water logged yet today. Tap above or input exact mL! 🥤",
                        color = if (isDarkMode) Color(0xFF74797A) else Color(0xFFAFAFAF),
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Medium
                    )
                }
            } else {
                val timeFormat = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    todayLogs.list.forEach { log ->
                        val timeStr = timeFormat.format(Date(log.timestamp))
                        
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isDarkMode) Color(0xFF1C2E3C) else Color(0xFFF1F8FF))
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "💧",
                                    fontSize = 16.sp,
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                                Column {
                                    Text(
                                        text = "${log.amountMl} mL",
                                        color = if (isDarkMode) Color.White else Color.Black,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                    Text(
                                        text = "Logged at $timeStr",
                                        color = if (isDarkMode) Color(0xFFB3E5FC) else Color(0xFF0288D1),
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            IconButton(
                                onClick = { onDeleteLog(log.id) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Delete,
                                    contentDescription = "Delete log",
                                    tint = Color(0xFFBA1A1A),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
