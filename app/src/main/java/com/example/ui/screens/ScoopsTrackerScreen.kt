package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
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
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.IntakeViewModel
import com.example.SoundUtil
import com.example.StableDays
import com.example.StableHistory
import com.example.data.DailyIntake
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.TextStyle
import com.example.ui.components.CircularTimeSelector
import com.example.ui.components.NavButton
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ScoopsTrackerScreen(
    viewModel: IntakeViewModel,
    isDarkMode: Boolean,
    onScreenChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val today by viewModel.todayIntake.collectAsStateWithLifecycle()
    val historyRaw by viewModel.intakesHistory.collectAsStateWithLifecycle()
    val history = remember(historyRaw) { StableHistory(historyRaw) }
    
    val totalCreatineCount = remember(historyRaw) {
        historyRaw.sumOf { it.creatineCount }
    }
    val totalProteinCount = remember(historyRaw) {
        historyRaw.sumOf { it.proteinCount }
    }

    val maxCreatine by viewModel.creatineMax.collectAsStateWithLifecycle()
    val maxProtein by viewModel.proteinMax.collectAsStateWithLifecycle()
    val reminderEnabled by viewModel.remindersEnabled.collectAsStateWithLifecycle()
    val reminderHour by viewModel.reminderHour.collectAsStateWithLifecycle()
    val reminderMinute by viewModel.reminderMinute.collectAsStateWithLifecycle()

    val context = LocalContext.current
    var calendarMonthView by remember { mutableStateOf(Calendar.getInstance()) }
    var calendarClickDate by remember { mutableStateOf<String?>(null) }
    
    val onDayClickStable = remember {
        { dateStr: String ->
            calendarClickDate = dateStr
        }
    }
    
    var showGoalSettings by remember { mutableStateOf(false) }

    var draggingCreatineInput by remember { mutableStateOf("") }
    var draggingProteinInput by remember { mutableStateOf("") }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.toggleReminders(context)
            Toast.makeText(context, "Daily reminders configured successfully!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Notification permission is required for reminders.", Toast.LENGTH_LONG).show()
        }
    }

    // Debounced savings for goal settings to avoid keystroke DB transactions
    LaunchedEffect(draggingCreatineInput) {
        kotlinx.coroutines.delay(500)
        val parsed = draggingCreatineInput.toIntOrNull()
        if (parsed != null && parsed in 1..100 && parsed != maxCreatine) {
            viewModel.setCreatineGoal(parsed)
        }
    }

    LaunchedEffect(draggingProteinInput) {
        kotlinx.coroutines.delay(500)
        val parsed = draggingProteinInput.toIntOrNull()
        if (parsed != null && parsed in 1..100 && parsed != maxProtein) {
            viewModel.setProteinGoal(parsed)
        }
    }

    LaunchedEffect(maxCreatine, maxProtein) {
        val parsedC = draggingCreatineInput.toIntOrNull()
        if (parsedC != maxCreatine) {
            draggingCreatineInput = maxCreatine.toString()
        }
        val parsedP = draggingProteinInput.toIntOrNull()
        if (parsedP != maxProtein) {
            draggingProteinInput = maxProtein.toString()
        }
    }

    val monthName = remember(calendarMonthView) {
        val sdf = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        sdf.format(calendarMonthView.time)
    }

    val onPrevMonthStable = remember {
        {
            val nextView = calendarMonthView.clone() as Calendar
            nextView.add(Calendar.MONTH, -1)
            calendarMonthView = nextView
        }
    }

    val onNextMonthStable = remember {
        {
            val nextView = calendarMonthView.clone() as Calendar
            nextView.add(Calendar.MONTH, 1)
            calendarMonthView = nextView
        }
    }

    val daysOfMonthRaw = remember(calendarMonthView) {
        val cal = calendarMonthView.clone() as Calendar
        cal.set(Calendar.DAY_OF_MONTH, 1)
        val maxDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        val list = mutableListOf<String>()
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        for (i in 1..maxDays) {
            cal.set(Calendar.DAY_OF_MONTH, i)
            list.add(sdf.format(cal.time))
        }
        list
    }
    val daysOfMonth = remember(daysOfMonthRaw) { StableDays(daysOfMonthRaw) }

    val startOffset = remember(calendarMonthView) {
        val cal = calendarMonthView.clone() as Calendar
        cal.set(Calendar.DAY_OF_MONTH, 1)
        val firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
        if (firstDayOfWeek == Calendar.SUNDAY) 6 else firstDayOfWeek - 2
    }

    // Dynamic Theme Color Mapping
    val themeBg = if (isDarkMode) Color(0xFF140F0D) else Color(0xFFFDF8F6)
    val ThemeTextTitle = if (isDarkMode) Color(0xFFFFFFFF) else Color(0xFF201A19)
    val ThemeTextSubtitle = if (isDarkMode) Color(0xFF74797A) else Color(0xFF74797A)
    val CardWhiteBackground = if (isDarkMode) Color(0xFF241D1A) else Color(0xFFFFFFFF)

    val CreatineBackground = if (isDarkMode) Color(0xFF2E1914) else Color(0xFFFCEEEB)
    val CreatineBorder = if (isDarkMode) Color(0xFF4A251E) else Color(0xFFF4DDDA)
    val CreatineAccent = if (isDarkMode) Color(0xFFFFB4A2) else Color(0xFF9C432F)
    val CreatineTextDark = if (isDarkMode) Color(0xFFFFDAD3) else Color(0xFF531B10)

    val ProteinBackground = if (isDarkMode) Color(0xFF14241F) else Color(0xFFF0F3F1)
    val ProteinBorder = if (isDarkMode) Color(0xFF223E36) else Color(0xFFE0E3E1)
    val ProteinAccent = if (isDarkMode) Color(0xFF81D4C0) else Color(0xFF3B695E)
    val ProteinTextDark = if (isDarkMode) Color(0xFFD6EAE4) else Color(0xFF191C1B)

    val ReminderBackground = if (isDarkMode) Color(0xFF42211A) else Color(0xFFF4DDDA)
    val AvatarBackground = if (isDarkMode) Color(0xFFBD8E85) else Color(0xFFEAC2BA)
    val ReminderTextColor = if (isDarkMode) Color(0xFFFFF1EF) else Color(0xFF531B10)
    val ReminderSubtextColor = if (isDarkMode) Color(0xFFFFF1EF).copy(alpha = 0.75f) else Color(0xFF531B10).copy(alpha = 0.7f)

    val displayCMax = today?.creatineMax ?: maxCreatine
    val displayPMax = today?.proteinMax ?: maxProtein

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
                text = "Hydration",
                icon = Icons.Filled.LocalActivity,
                isDark = isDarkMode,
                activeColor = Color(0xFF00ACC1),
                onClick = { onScreenChange("water") },
                modifier = Modifier.weight(1f).testTag("goto_water")
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

        // Main Counters Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            IntakeCounterCard(
                title = "Creatine",
                subtitle = "Total scoops logged",
                count = totalCreatineCount,
                max = displayCMax,
                primaryColor = CreatineAccent,
                textColorDark = CreatineTextDark,
                containerColor = CreatineBackground,
                borderColor = CreatineBorder,
                unitLabel = if (totalCreatineCount == 1) "SCOOP" else "SCOOPS",
                onIncrement = { 
                    viewModel.incrementCreatine() 
                    SoundUtil.playConfirmationSound()
                },
                onDecrement = { viewModel.decrementCreatine() },
                modifier = Modifier.weight(1f)
            )

            IntakeCounterCard(
                title = "Protein",
                subtitle = "Total scoops logged",
                count = totalProteinCount,
                max = displayPMax,
                primaryColor = ProteinAccent,
                textColorDark = ProteinTextDark,
                containerColor = ProteinBackground,
                borderColor = ProteinBorder,
                unitLabel = if (totalProteinCount == 1) "SCOOP" else "SCOOPS",
                onIncrement = { 
                    viewModel.incrementProtein() 
                    SoundUtil.playConfirmationSound()
                },
                onDecrement = { viewModel.decrementProtein() },
                modifier = Modifier.weight(1f)
            )
        }

        // Customize Goal Targets Card
        Card(
            colors = CardDefaults.cardColors(containerColor = CardWhiteBackground),
            border = BorderStroke(1.dp, CreatineBorder),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showGoalSettings = !showGoalSettings }
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Build,
                            contentDescription = "Goal Limits",
                            tint = ThemeTextTitle,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Customize Goal Targets",
                            color = ThemeTextTitle,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Icon(
                        imageVector = if (showGoalSettings) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                        contentDescription = "Toggle Expand",
                        tint = ThemeTextSubtitle
                    )
                }

                AnimatedVisibility(
                    visible = showGoalSettings,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "Creatine Goal Target (Scoops)",
                                color = ThemeTextSubtitle,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                            OutlinedTextField(
                                value = draggingCreatineInput,
                                onValueChange = { newValue ->
                                    draggingCreatineInput = newValue
                                },
                                placeholder = { Text("e.g. 5") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = CreatineAccent,
                                    unfocusedBorderColor = CreatineBorder,
                                    focusedLabelColor = CreatineAccent,
                                    cursorColor = CreatineAccent,
                                    focusedContainerColor = CreatineBackground.copy(alpha = 0.3f),
                                    unfocusedContainerColor = Color.Transparent
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth().testTag("creatine_goal_input")
                            )
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "Protein Goal Target (Scoops)",
                                color = ThemeTextSubtitle,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                            OutlinedTextField(
                                value = draggingProteinInput,
                                onValueChange = { newValue ->
                                    draggingProteinInput = newValue
                                },
                                placeholder = { Text("e.g. 4") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = ProteinAccent,
                                    unfocusedBorderColor = ProteinBorder,
                                    focusedLabelColor = ProteinAccent,
                                    cursorColor = ProteinAccent,
                                    focusedContainerColor = ProteinBackground.copy(alpha = 0.3f),
                                    unfocusedContainerColor = Color.Transparent
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth().testTag("protein_goal_input")
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        HorizontalDivider(color = CreatineBorder.copy(alpha = 0.4f), thickness = 1.dp)
                        Spacer(modifier = Modifier.height(4.dp))

                        OutlinedButton(
                            onClick = { viewModel.clearAllHistory() },
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color(0xFFBA1A1A)
                            ),
                            border = BorderStroke(1.dp, Color(0xFFBA1A1A).copy(alpha = 0.4f)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().testTag("reset_logs_button")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Delete,
                                    contentDescription = "Reset Logs",
                                    modifier = Modifier.size(16.dp),
                                    tint = Color(0xFFBA1A1A)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Reset Intake History",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // Calendar Section
        MonthlyCalendarSection(
            monthName = monthName,
            days = daysOfMonth,
            startOffset = startOffset,
            history = history,
            onDayClick = onDayClickStable,
            onPrevMonth = onPrevMonthStable,
            onNextMonth = onNextMonthStable,
            creatineAccent = CreatineAccent,
            proteinAccent = ProteinAccent,
            creatineBorder = CreatineBorder,
            cardBackground = CardWhiteBackground,
            creatineBg = CreatineBackground,
            textTitleColor = ThemeTextTitle,
            textSubColor = ThemeTextSubtitle
        )

        // Daily Reminder Card
        Card(
            colors = CardDefaults.cardColors(containerColor = ReminderBackground),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Daily Reminder",
                            color = ReminderTextColor,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        val currentReminderText = try {
                            formatReminderTimeString(reminderHour, reminderMinute)
                        } catch (e: Exception) {
                            "Scheduled time"
                        }
                        Text(
                            text = if (reminderEnabled) "Scheduled for $currentReminderText" else "Log your scoops before midnight",
                            color = ReminderSubtextColor,
                            fontSize = 12.sp
                        )
                    }

                    Switch(
                        checked = reminderEnabled,
                        onCheckedChange = { checked ->
                            if (checked && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                val status = ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                                if (status != PackageManager.PERMISSION_GRANTED) {
                                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                } else {
                                    viewModel.toggleReminders(context)
                                }
                            } else {
                                viewModel.toggleReminders(context)
                            }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = CreatineAccent,
                            uncheckedThumbColor = ReminderTextColor,
                            uncheckedTrackColor = AvatarBackground
                        ),
                        modifier = Modifier.testTag("reminder_toggle_switch")
                    )
                }

                AnimatedVisibility(
                    visible = reminderEnabled,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .padding(bottom = 20.dp)
                    ) {
                        HorizontalDivider(color = AvatarBackground.copy(alpha = 0.5f), thickness = 1.dp)
                        Spacer(modifier = Modifier.height(14.dp))
                        
                        CircularTimeSelector(
                            hour = reminderHour,
                            minute = reminderMinute,
                            onTimeChanged = { h, m ->
                                viewModel.changeReminderTime(context, h, m)
                            },
                            activeColor = CreatineAccent,
                            isDarkMode = isDarkMode,
                            testTagPrefix = "scoops_reminder"
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    // Calendar Click Confirmation Dialog
    calendarClickDate?.let { dateStr ->
        val record = history.list.find { it.date == dateStr }
        val initCreatineStr = if (record == null || record.creatineCount == 0) "" else record.creatineCount.toString()
        val initProteinStr = if (record == null || record.proteinCount == 0) "" else record.proteinCount.toString()

        var inputCreatineScoops by remember(record) { mutableStateOf(initCreatineStr) }
        var inputProteinScoops by remember(record) { mutableStateOf(initProteinStr) }

        val otherDaysCreatine = remember(history, dateStr) {
            history.list.filter { it.date != dateStr }.sumOf { it.creatineCount }
        }
        val otherDaysProtein = remember(history, dateStr) {
            history.list.filter { it.date != dateStr }.sumOf { it.proteinCount }
        }

        val creatineInputVal = inputCreatineScoops.toIntOrNull() ?: 0
        val proteinInputVal = inputProteinScoops.toIntOrNull() ?: 0

        val isCreatineExceeded = creatineInputVal > displayCMax
        val isProteinExceeded = proteinInputVal > displayPMax

        val hasWrittenCreatine = inputCreatineScoops.isNotEmpty()
        val hasWrittenProtein = inputProteinScoops.isNotEmpty()

        var isCreatineCompleted by remember(record) {
            mutableStateOf(record?.creatineTicked == true || (record != null && record.creatineCount >= displayCMax))
        }
        var isProteinCompleted by remember(record) {
            mutableStateOf(record?.proteinTicked == true || (record != null && record.proteinCount >= displayPMax))
        }

        AlertDialog(
            onDismissRequest = { calendarClickDate = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.DateRange,
                        contentDescription = "Log date details",
                        tint = CreatineAccent,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Update Log: $dateStr",
                        color = ThemeTextTitle,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CreatineBackground),
                        border = BorderStroke(1.dp, CreatineBorder),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "CREATINE INTAKE",
                                color = CreatineTextDark,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Checkbox(
                                        checked = isCreatineCompleted && hasWrittenCreatine,
                                        onCheckedChange = { checked ->
                                            isCreatineCompleted = checked
                                            if (!checked) {
                                                inputCreatineScoops = ""
                                            }
                                        },
                                        enabled = hasWrittenCreatine,
                                        colors = CheckboxDefaults.colors(
                                            checkedColor = CreatineAccent,
                                            uncheckedColor = CreatineTextDark.copy(alpha = 0.6f)
                                        )
                                    )
                                    Text(
                                        text = "Taken",
                                        color = if (hasWrittenCreatine) CreatineTextDark else CreatineTextDark.copy(alpha = 0.5f),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    val currentVal = inputCreatineScoops.toIntOrNull() ?: 0
                                    Text(
                                        text = "$currentVal ${if (currentVal == 1) "Scoop" else "Scoops"}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = CreatineTextDark,
                                        modifier = Modifier.testTag("creatine_scoops_count")
                                    )
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        IconButton(
                                            onClick = {
                                                val currentVal = inputCreatineScoops.toIntOrNull() ?: 0
                                                if (currentVal > 0) {
                                                    val newVal = currentVal - 1
                                                    inputCreatineScoops = if (newVal == 0) "" else newVal.toString()
                                                    if (newVal == 0) {
                                                        isCreatineCompleted = false
                                                    }
                                                }
                                            },
                                            enabled = (inputCreatineScoops.toIntOrNull() ?: 0) > 0,
                                            modifier = Modifier
                                                .size(38.dp)
                                                .background(
                                                    color = if ((inputCreatineScoops.toIntOrNull() ?: 0) > 0) CreatineAccent.copy(alpha = 0.15f) else Color.LightGray.copy(alpha = 0.2f),
                                                    shape = CircleShape
                                                )
                                                .testTag("creatine_minus_btn")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.Remove,
                                                contentDescription = "Subtract scoop",
                                                tint = if ((inputCreatineScoops.toIntOrNull() ?: 0) > 0) CreatineAccent else Color.Gray,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(24.dp))

                                        IconButton(
                                            onClick = {
                                                val currentVal = inputCreatineScoops.toIntOrNull() ?: 0
                                                if (currentVal < displayCMax) {
                                                    val newVal = currentVal + 1
                                                    inputCreatineScoops = newVal.toString()
                                                    isCreatineCompleted = true
                                                }
                                            },
                                            enabled = (inputCreatineScoops.toIntOrNull() ?: 0) < displayCMax,
                                            modifier = Modifier
                                                .size(38.dp)
                                                .background(
                                                    color = if ((inputCreatineScoops.toIntOrNull() ?: 0) < displayCMax) CreatineAccent.copy(alpha = 0.15f) else Color.LightGray.copy(alpha = 0.2f),
                                                    shape = CircleShape
                                                )
                                                .testTag("creatine_plus_btn")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.Add,
                                                contentDescription = "Add scoop",
                                                tint = if ((inputCreatineScoops.toIntOrNull() ?: 0) < displayCMax) CreatineAccent else Color.Gray,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            if (isCreatineExceeded) {
                                Text(
                                    text = "Creatine intake ($creatineInputVal) exceeds limit ($displayCMax)",
                                    color = Color(0xFFBA1A1A),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                    }

                    Card(
                        colors = CardDefaults.cardColors(containerColor = ProteinBackground),
                        border = BorderStroke(1.dp, ProteinBorder),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "PROTEIN INTAKE",
                                color = ProteinTextDark,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Checkbox(
                                        checked = isProteinCompleted && hasWrittenProtein,
                                        onCheckedChange = { checked ->
                                            isProteinCompleted = checked
                                            if (!checked) {
                                                inputProteinScoops = ""
                                            }
                                        },
                                        enabled = hasWrittenProtein,
                                        colors = CheckboxDefaults.colors(
                                            checkedColor = ProteinAccent,
                                            uncheckedColor = ProteinTextDark.copy(alpha = 0.6f)
                                        )
                                    )
                                    Text(
                                        text = "Taken",
                                        color = if (hasWrittenProtein) ProteinTextDark else ProteinTextDark.copy(alpha = 0.5f),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    val currentVal = inputProteinScoops.toIntOrNull() ?: 0
                                    Text(
                                        text = "$currentVal ${if (currentVal == 1) "Scoop" else "Scoops"}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ProteinTextDark,
                                        modifier = Modifier.testTag("protein_scoops_count")
                                    )
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        IconButton(
                                            onClick = {
                                                val currentVal = inputProteinScoops.toIntOrNull() ?: 0
                                                if (currentVal > 0) {
                                                    val newVal = currentVal - 1
                                                    inputProteinScoops = if (newVal == 0) "" else newVal.toString()
                                                    if (newVal == 0) {
                                                        isProteinCompleted = false
                                                    }
                                                }
                                            },
                                            enabled = (inputProteinScoops.toIntOrNull() ?: 0) > 0,
                                            modifier = Modifier
                                                .size(38.dp)
                                                .background(
                                                    color = if ((inputProteinScoops.toIntOrNull() ?: 0) > 0) ProteinAccent.copy(alpha = 0.15f) else Color.LightGray.copy(alpha = 0.2f),
                                                    shape = CircleShape
                                                )
                                                .testTag("protein_minus_btn")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.Remove,
                                                contentDescription = "Subtract scoop",
                                                tint = if ((inputProteinScoops.toIntOrNull() ?: 0) > 0) ProteinAccent else Color.Gray,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(24.dp))

                                        IconButton(
                                            onClick = {
                                                val currentVal = inputProteinScoops.toIntOrNull() ?: 0
                                                if (currentVal < displayPMax) {
                                                    val newVal = currentVal + 1
                                                    inputProteinScoops = newVal.toString()
                                                    isProteinCompleted = true
                                                }
                                            },
                                            enabled = (inputProteinScoops.toIntOrNull() ?: 0) < displayPMax,
                                            modifier = Modifier
                                                .size(38.dp)
                                                .background(
                                                    color = if ((inputProteinScoops.toIntOrNull() ?: 0) < displayPMax) ProteinAccent.copy(alpha = 0.15f) else Color.LightGray.copy(alpha = 0.2f),
                                                    shape = CircleShape
                                                )
                                                .testTag("protein_plus_btn")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.Add,
                                                contentDescription = "Add scoop",
                                                tint = if ((inputProteinScoops.toIntOrNull() ?: 0) < displayPMax) ProteinAccent else Color.Gray,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            if (isProteinExceeded) {
                                Text(
                                    text = "Protein intake ($proteinInputVal) exceeds limit ($displayPMax)",
                                    color = Color(0xFFBA1A1A),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                val isConfirmEnabled = !isCreatineExceeded && !isProteinExceeded
                Button(
                    onClick = {
                        val finalCCount = inputCreatineScoops.toIntOrNull() ?: 0
                        val finalPCount = inputProteinScoops.toIntOrNull() ?: 0
                        
                        viewModel.updateLogForDay(
                            dateStr = dateStr,
                            cCount = finalCCount,
                            pCount = finalPCount,
                            cTicked = isCreatineCompleted && hasWrittenCreatine,
                            pTicked = isProteinCompleted && hasWrittenProtein
                        )
                        SoundUtil.playConfirmationSound()
                        calendarClickDate = null
                    },
                    enabled = isConfirmEnabled,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CreatineAccent,
                        disabledContainerColor = CreatineAccent.copy(alpha = 0.5f)
                    )
                ) {
                    Text("Confirm", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { calendarClickDate = null }
                ) {
                    Text("Cancel", color = ThemeTextSubtitle)
                }
            },
            containerColor = CardWhiteBackground
        )
    }
}

fun formatReminderTimeString(hour: Int, minute: Int): String {
    val amPm = if (hour < 12) "AM" else "PM"
    val displayHour = when {
        hour == 0 -> 12
        hour > 12 -> hour - 12
        else -> hour
    }
    return String.format(Locale.getDefault(), "%d:%02d %s", displayHour, minute, amPm)
}

@Composable
fun IntakeCounterCard(
    title: String,
    subtitle: String,
    count: Int,
    max: Int,
    primaryColor: Color,
    textColorDark: Color,
    containerColor: Color,
    borderColor: Color,
    unitLabel: String,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.aspectRatio(1.02f),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = BorderStroke(1.dp, borderColor),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    color = textColorDark,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color.White.copy(alpha = 0.35f))
                        .padding(horizontal = 5.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "MAX $max",
                        color = textColorDark,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(vertical = 1.dp)
            ) {
                Text(
                    text = "$count",
                    color = primaryColor,
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Black,
                    lineHeight = 40.sp
                )
                Text(
                    text = unitLabel,
                    color = textColorDark.copy(alpha = 0.7f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 2.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = null,
                    tint = textColorDark.copy(alpha = 0.5f),
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = "Logged via Calendar",
                    color = textColorDark.copy(alpha = 0.5f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}

@Composable
fun MonthlyCalendarSection(
    monthName: String,
    days: StableDays,
    startOffset: Int,
    history: StableHistory,
    onDayClick: (String) -> Unit,
    onPrevMonth: () -> Unit,
    onNextMonth: () -> Unit,
    creatineAccent: Color,
    proteinAccent: Color,
    creatineBorder: Color,
    cardBackground: Color,
    creatineBg: Color,
    textTitleColor: Color,
    textSubColor: Color
) {
    val historyMap = remember(history) {
        history.list.associateBy { it.date }
    }

    val textMeasurer = rememberTextMeasurer()

    Card(
        colors = CardDefaults.cardColors(containerColor = cardBackground),
        border = BorderStroke(1.dp, creatineBorder),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.DateRange,
                        contentDescription = "Calendar",
                        tint = textTitleColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = monthName,
                        color = textTitleColor,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onPrevMonth,
                        modifier = Modifier
                            .size(32.dp)
                            .background(creatineBg, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                            contentDescription = "Prev Month",
                            tint = creatineAccent,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(
                        onClick = onNextMonth,
                        modifier = Modifier
                            .size(32.dp)
                            .background(creatineBg, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = "Next Month",
                            tint = creatineAccent,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val weekDays = remember { listOf("M", "T", "W", "T", "F", "S", "S") }
                weekDays.forEach { day ->
                    Text(
                        text = day,
                        color = textSubColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            val totalCells = days.list.size + startOffset
            val rowsCount = (totalCells + 6) / 7

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(7f / rowsCount)
                    .pointerInput(days, startOffset, rowsCount, onDayClick) {
                        detectTapGestures { offset ->
                            val cellW = size.width.toFloat() / 7f
                            val cellH = size.height.toFloat() / rowsCount
                            val col = (offset.x / cellW).toInt().coerceIn(0, 6)
                            val row = (offset.y / cellH).toInt().coerceIn(0, rowsCount - 1)
                            val cellIndex = row * 7 + col
                            if (cellIndex >= startOffset && cellIndex < days.list.size + startOffset) {
                                val dateStr = days.list[cellIndex - startOffset]
                                onDayClick(dateStr)
                            }
                        }
                    }
            ) {
                val cellW = size.width / 7f
                val cellH = size.height / rowsCount
                val padding = 2.dp.toPx()
                val cornerRadiusPx = 12.dp.toPx()
                val borderStrokeWidth = 1.dp.toPx()

                for (rowIndex in 0 until rowsCount) {
                    for (colIndex in 0..6) {
                        val cellIndex = rowIndex * 7 + colIndex
                        if (cellIndex >= startOffset && cellIndex < totalCells) {
                            val dayDateStr = days.list[cellIndex - startOffset]
                            val dayNum = (cellIndex - startOffset + 1).toString()
                            val record = historyMap[dayDateStr]

                            val cMax = record?.creatineMax ?: 5
                            val pMax = record?.proteinMax ?: 4
                            val creatineCount = record?.creatineCount ?: 0
                            val proteinCount = record?.proteinCount ?: 0

                            val isCreatineTicked = record?.creatineTicked == true || (creatineCount >= cMax && cMax > 0)
                            val isProteinTicked = record?.proteinTicked == true || (proteinCount >= pMax && pMax > 0)

                            val isAnyTicked = isCreatineTicked || isProteinTicked
                            val isAnyLogged = creatineCount > 0 || proteinCount > 0

                            val cellLeft = colIndex * cellW
                            val cellTop = rowIndex * cellH
                            val boxLeft = cellLeft + padding
                            val boxTop = cellTop + padding
                            val boxW = cellW - padding * 2
                            val boxH = cellH - padding * 2

                            // 1. Draw Background
                            val bgAlpha = if (isAnyTicked) 0.5f else if (isAnyLogged) 0.25f else 0.0f
                            if (bgAlpha > 0f) {
                                drawRoundRect(
                                    color = creatineBg.copy(alpha = bgAlpha),
                                    topLeft = Offset(boxLeft, boxTop),
                                    size = Size(boxW, boxH),
                                    cornerRadius = CornerRadius(cornerRadiusPx, cornerRadiusPx)
                                )
                            }

                            // 2. Draw Border
                            if (isAnyTicked || isAnyLogged) {
                                val borderColor = if (isAnyTicked) creatineAccent.copy(alpha = 0.3f) else creatineAccent.copy(alpha = 0.15f)
                                drawRoundRect(
                                    color = borderColor,
                                    topLeft = Offset(boxLeft + borderStrokeWidth / 2f, boxTop + borderStrokeWidth / 2f),
                                    size = Size(boxW - borderStrokeWidth, boxH - borderStrokeWidth),
                                    cornerRadius = CornerRadius(cornerRadiusPx, cornerRadiusPx),
                                    style = Stroke(width = borderStrokeWidth)
                                )
                            }

                            // 3. Draw Text
                            val textStyle = TextStyle(
                                color = textTitleColor,
                                fontSize = 12.sp,
                                fontWeight = if (isAnyTicked) FontWeight.Bold
                                             else if (isAnyLogged) FontWeight.Medium
                                             else FontWeight.Normal
                            )
                            val textResult = textMeasurer.measure(text = dayNum, style = textStyle)
                            val textLeft = boxLeft + (boxW - textResult.size.width) / 2f
                            val textTop = boxTop + (boxH - textResult.size.height) / 2f - 4.dp.toPx()
                            drawText(textResult, topLeft = Offset(textLeft, textTop))

                            // 4. Draw Pill Indicators
                            val creatineColor = if (isCreatineTicked) creatineAccent
                                                else if (creatineCount > 0) creatineAccent.copy(alpha = 0.5f)
                                                else Color.LightGray.copy(alpha = 0.4f)
                            val proteinColor = if (isProteinTicked) proteinAccent
                                               else if (proteinCount > 0) proteinAccent.copy(alpha = 0.5f)
                                               else Color.LightGray.copy(alpha = 0.4f)

                            val capSingleW = 10.dp.toPx()
                            val capH = 4.dp.toPx()
                            val capCorner = 2.dp.toPx()
                            val capsSpace = 2.dp.toPx()
                            val totalCapsW = capSingleW * 2f + capsSpace
                            val capsLeft = boxLeft + (boxW - totalCapsW) / 2f
                            val capsTop = boxTop + boxH - 8.dp.toPx()

                            drawRoundRect(
                                color = creatineColor,
                                topLeft = Offset(capsLeft, capsTop),
                                size = Size(capSingleW, capH),
                                cornerRadius = CornerRadius(capCorner, capCorner)
                            )
                            drawRoundRect(
                                color = proteinColor,
                                topLeft = Offset(capsLeft + capSingleW + capsSpace, capsTop),
                                size = Size(capSingleW, capH),
                                cornerRadius = CornerRadius(capCorner, capCorner)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.size(width = 10.dp, height = 4.dp).clip(CircleShape).background(creatineAccent))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Creatine", color = textSubColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)

                Spacer(modifier = Modifier.width(16.dp))

                Box(modifier = Modifier.size(width = 10.dp, height = 4.dp).clip(CircleShape).background(proteinAccent))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Protein", color = textSubColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
