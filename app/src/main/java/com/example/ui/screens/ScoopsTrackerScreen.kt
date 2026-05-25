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
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.material3.ModalBottomSheetDefaults
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.IntakeViewModel
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.ui.platform.LocalFocusManager
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.focus.onFocusChanged
import com.example.SoundUtil
import com.example.StableDays
import com.example.data.Supplement
import com.example.data.SupplementLog
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.rememberTextMeasurer
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.TextStyle
import com.example.ui.components.CircularTimeSelector
import com.example.ui.components.NavButton
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ScoopsTrackerScreen(
    viewModel: IntakeViewModel,
    isDarkMode: Boolean,
    onScreenChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val supplements by viewModel.supplements.collectAsStateWithLifecycle()
    val allSupplementLogs by viewModel.allSupplementLogs.collectAsStateWithLifecycle()

    val context = LocalContext.current
    var calendarMonthView by remember { mutableStateOf(Calendar.getInstance()) }
    var calendarClickDate by remember { mutableStateOf<String?>(null) }
    
    val onDayClickStable = remember {
        { dateStr: String ->
            calendarClickDate = dateStr
        }
    }

    var showAddBottomSheet by remember { mutableStateOf(false) }
    var selectedSupplementId by remember { mutableStateOf<Int?>(null) }

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
    val themeBg = if (isDarkMode) Color(0xFF0B0B0C) else Color(0xFFF4F6F8)
    val ThemeTextTitle = if (isDarkMode) Color(0xFFFFFFFF) else Color(0xFF0B0B0C)
    val ThemeTextSubtitle = if (isDarkMode) Color(0xFFAFAFAF) else Color(0xFF705244)
    val CardWhiteBackground = if (isDarkMode) Color(0xFF1C1C1E) else Color(0xFFFFFFFF)
    val CardBorderColor = if (isDarkMode) Color.White.copy(alpha = 0.15f) else Color.Black.copy(alpha = 0.15f)

    val emptyStateButtonColor = if (isDarkMode) Color(0xFFFFB4A2) else Color(0xFFE64A19)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(themeBg),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        // Section header and Add button
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "ACTIVE TUBS",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = ThemeTextSubtitle,
                letterSpacing = 0.5.sp
            )
            TextButton(
                onClick = {
                    if (supplements.size >= 10) {
                        Toast.makeText(context, "Maximum limit of 10 tubs reached.", Toast.LENGTH_SHORT).show()
                    } else {
                        showAddBottomSheet = true
                    }
                },
                modifier = Modifier.testTag("add_supplement_button")
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add", modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add New", fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Supplements list
        if (supplements.isEmpty()) {
            // Empty State Card
            Card(
                colors = CardDefaults.cardColors(containerColor = CardWhiteBackground),
                border = BorderStroke(1.dp, CardBorderColor),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Medication,
                        contentDescription = "No Tubs",
                        tint = ThemeTextSubtitle,
                        modifier = Modifier.size(48.dp)
                    )
                    Text(
                        text = "No Tubs Tracked",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = ThemeTextTitle
                    )
                    Text(
                        text = "Track your tub servings and monitor stock levels dynamically.",
                        fontSize = 12.sp,
                        color = ThemeTextSubtitle,
                        textAlign = TextAlign.Center
                    )
                    Button(
                        onClick = {
                            if (supplements.size >= 10) {
                                Toast.makeText(context, "Maximum limit of 10 tubs reached.", Toast.LENGTH_SHORT).show()
                            } else {
                                showAddBottomSheet = true
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = emptyStateButtonColor),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Add Your First Tub", color = Color.White)
                    }
                }
            }
        } else {
            if (supplements.size <= 3) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    supplements.forEach { supp ->
                        SupplementStockCard(
                            supp = supp,
                            isDarkMode = isDarkMode,
                            themeTextTitle = ThemeTextTitle,
                            themeTextSubtitle = ThemeTextSubtitle,
                            cardBg = CardWhiteBackground,
                            borderColor = CardBorderColor,
                            onLog = {
                                viewModel.logServingToday(supp)
                                SoundUtil.playConfirmationSound()
                            },
                            onDelete = { viewModel.deleteSupplement(supp.id) }
                        )
                    }
                }
            } else {
                val topThree = remember(supplements) { supplements.take(3) }
                val remaining = remember(supplements) { supplements.drop(3) }
                var isExpanded by remember { mutableStateOf(false) }

                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Show first 3 tubs directly
                    topThree.forEach { supp ->
                        SupplementStockCard(
                            supp = supp,
                            isDarkMode = isDarkMode,
                            themeTextTitle = ThemeTextTitle,
                            themeTextSubtitle = ThemeTextSubtitle,
                            cardBg = CardWhiteBackground,
                            borderColor = CardBorderColor,
                            onLog = {
                                viewModel.logServingToday(supp)
                                SoundUtil.playConfirmationSound()
                            },
                            onDelete = { viewModel.deleteSupplement(supp.id) }
                        )
                    }

                    // Clickable Expand/Collapse Toggle Button
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isExpanded = !isExpanded }
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isExpanded) "Show Less" else "Show More (${remaining.size})",
                            color = ThemeTextSubtitle,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = if (isExpanded) "Collapse" else "Expand",
                            tint = ThemeTextSubtitle,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Show the rest of the tubs inline with animation
                    AnimatedVisibility(
                        visible = isExpanded,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            remaining.forEach { supp ->
                                SupplementStockCard(
                                    supp = supp,
                                    isDarkMode = isDarkMode,
                                    themeTextTitle = ThemeTextTitle,
                                    themeTextSubtitle = ThemeTextSubtitle,
                                    cardBg = CardWhiteBackground,
                                    borderColor = CardBorderColor,
                                    onLog = {
                                        viewModel.logServingToday(supp)
                                        SoundUtil.playConfirmationSound()
                                    },
                                    onDelete = { viewModel.deleteSupplement(supp.id) }
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
            supplements = supplements,
            supplementLogs = allSupplementLogs,
            onDayClick = onDayClickStable,
            onPrevMonth = onPrevMonthStable,
            onNextMonth = onNextMonthStable,
            cardBackground = CardWhiteBackground,
            borderColor = CardBorderColor,
            textTitleColor = ThemeTextTitle,
            textSubColor = ThemeTextSubtitle
        )


    }

    // Add Supplement Bottom Sheet
    if (showAddBottomSheet) {
        var newName by remember { mutableStateOf("") }
        var selectedType by remember { mutableStateOf("Powder") }
        var newTotalStock by remember { mutableStateOf("") }
        var newDailyTarget by remember { mutableStateOf("") }
        
        val swatches = listOf(
            "#FF3333", // Red
            "#FF9100", // Orange
            "#FFEB3B", // Yellow
            "#21D021", // Green
            "#3377FF", // Blue
            "#E040FB"  // Purple
        )
        var selectedColor by remember { mutableStateOf(swatches.first()) }
        
        val types = listOf("Powder", "Capsule", "Liquid", "Gummy")
        
        var isNameFocused by remember { mutableStateOf(false) }
        var isSizeFocused by remember { mutableStateOf(false) }
        var isTargetFocused by remember { mutableStateOf(false) }
        val isAnyFocused = isNameFocused || isSizeFocused || isTargetFocused
        
        ModalBottomSheet(
            onDismissRequest = { showAddBottomSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = CardWhiteBackground,
            properties = ModalBottomSheetDefaults.properties(
                shouldDismissOnBackPress = false
            )
        ) {
            val focusManager = LocalFocusManager.current
            val keyboardController = LocalSoftwareKeyboardController.current
            
            val isKeyboardVisible = WindowInsets.isImeVisible
            LaunchedEffect(isKeyboardVisible) {
                if (!isKeyboardVisible) {
                    focusManager.clearFocus()
                }
            }
            
            BackHandler(enabled = true) {
                if (isAnyFocused) {
                    keyboardController?.hide()
                    focusManager.clearFocus()
                } else {
                    showAddBottomSheet = false
                }
            }
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
                    .navigationBarsPadding()
                    .imePadding()
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Add New Tub",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = ThemeTextTitle
                )
                
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    label = { Text("Tub Name (e.g. Protein, Creatine)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(android.graphics.Color.parseColor(selectedColor)),
                        focusedLabelColor = Color(android.graphics.Color.parseColor(selectedColor)),
                        cursorColor = Color(android.graphics.Color.parseColor(selectedColor))
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { isNameFocused = it.isFocused }
                        .testTag("new_supplement_name")
                )
                
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Type", fontSize = 12.sp, color = ThemeTextSubtitle, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        types.forEach { type ->
                            val isSelected = selectedType == type
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedType = type },
                                label = { Text(type, fontSize = 12.sp) }
                            )
                        }
                    }
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    val unit = when (selectedType) {
                        "Powder" -> "scoops"
                        "Capsule" -> "capsules"
                        "Liquid" -> "ml"
                        "Gummy" -> "gummies"
                        else -> "servings"
                    }
                    
                    OutlinedTextField(
                        value = newTotalStock,
                        onValueChange = { newTotalStock = it.filter { char -> char.isDigit() } },
                        label = { Text("Tub Size ($unit)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(android.graphics.Color.parseColor(selectedColor)),
                            focusedLabelColor = Color(android.graphics.Color.parseColor(selectedColor))
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .onFocusChanged { isSizeFocused = it.isFocused }
                            .testTag("new_supplement_stock")
                    )
                    
                    OutlinedTextField(
                        value = newDailyTarget,
                        onValueChange = { newDailyTarget = it.filter { char -> char.isDigit() } },
                        label = { Text("Daily Target ($unit)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(android.graphics.Color.parseColor(selectedColor)),
                            focusedLabelColor = Color(android.graphics.Color.parseColor(selectedColor))
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .onFocusChanged { isTargetFocused = it.isFocused }
                            .testTag("new_supplement_target")
                    )
                }
                
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Visual Color Swatch",
                        fontSize = 12.sp,
                        color = ThemeTextSubtitle,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.Start)
                    )
                    
                    // Presets Swatches Row
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        swatches.forEach { hexColor ->
                            val isSelected = selectedColor == hexColor
                            val color = Color(android.graphics.Color.parseColor(hexColor))
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .border(
                                        width = if (isSelected) 3.dp else 0.dp,
                                        color = if (isSelected) ThemeTextTitle else Color.Transparent,
                                        shape = CircleShape
                                    )
                                    .clickable { selectedColor = hexColor }
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    // Custom Color Wheel
                    ColorWheel(
                        selectedColorHex = selectedColor,
                        isDarkMode = isDarkMode,
                        onColorChange = { selectedColor = it }
                    )
                }
                
                Button(
                    onClick = {
                        val stock = newTotalStock.toIntOrNull() ?: 0
                        val target = newDailyTarget.toIntOrNull() ?: 0
                        if (newName.isNotBlank() && stock > 0 && target > 0) {
                            val unit = when (selectedType) {
                                "Powder" -> "scoops"
                                "Capsule" -> "capsules"
                                "Liquid" -> "ml"
                                "Gummy" -> "gummies"
                                else -> "servings"
                            }
                            viewModel.addSupplement(
                                name = newName,
                                type = selectedType,
                                servingUnit = unit,
                                totalStock = stock,
                                dailyTarget = target,
                                colorTag = selectedColor
                            )
                            showAddBottomSheet = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(android.graphics.Color.parseColor(selectedColor))),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("confirm_add_supplement")
                ) {
                    Text("Create Tub", color = Color.White, fontWeight = FontWeight.Bold)
                }
                
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }

    // Calendar Click Confirmation Dialog
    calendarClickDate?.let { dateStr ->
        AlertDialog(
            onDismissRequest = { calendarClickDate = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.DateRange,
                        contentDescription = "Log date details",
                        tint = emptyStateButtonColor,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Update Logs: $dateStr",
                        color = ThemeTextTitle,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (supplements.isEmpty()) {
                        Text("No tubs configured yet.", color = ThemeTextSubtitle, fontSize = 13.sp)
                    } else {
                        supplements.forEach { supp ->
                            val color = try { Color(android.graphics.Color.parseColor(supp.colorTag)) } catch(e: Exception) { Color.LightGray }
                            val dayLogs = allSupplementLogs.filter { it.date == dateStr && it.supplementId == supp.id }
                            val loggedCount = dayLogs.size
                            
                            Card(
                                colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.05f)),
                                border = BorderStroke(1.dp, color.copy(alpha = 0.2f)),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(supp.name, fontWeight = FontWeight.Bold, color = ThemeTextTitle, fontSize = 14.sp)
                                        Text("Logged: $loggedCount ${supp.servingUnit}", fontSize = 11.sp, color = ThemeTextSubtitle)
                                    }
                                    
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        // Minus Button
                                        IconButton(
                                            onClick = { viewModel.removeServingOnDate(supp, dateStr) },
                                            enabled = loggedCount > 0,
                                            modifier = Modifier.size(32.dp).background(color.copy(alpha = 0.15f), CircleShape)
                                        ) {
                                            Icon(Icons.Filled.Remove, contentDescription = "Remove", tint = color, modifier = Modifier.size(16.dp))
                                        }
                                        
                                        // Plus Button
                                        IconButton(
                                            onClick = { viewModel.logServingOnDate(supp, dateStr) },
                                            enabled = supp.remainingStock > 0,
                                            modifier = Modifier.size(32.dp).background(color.copy(alpha = 0.15f), CircleShape)
                                        ) {
                                            Icon(Icons.Filled.Add, contentDescription = "Add", tint = color, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { calendarClickDate = null },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
                ) {
                    Text("Done", color = Color.White)
                }
            },
            containerColor = CardWhiteBackground
        )
    }
}

@Composable
fun SupplementStockCard(
    supp: Supplement,
    isDarkMode: Boolean,
    themeTextTitle: Color,
    themeTextSubtitle: Color,
    cardBg: Color,
    borderColor: Color,
    onLog: () -> Unit,
    onDelete: () -> Unit
) {
    val color = try { Color(android.graphics.Color.parseColor(supp.colorTag)) } catch(e: Exception) { Color.LightGray }
    
    Card(
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = BorderStroke(1.dp, borderColor),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.fillMaxWidth().testTag("supplement_card_${supp.name}")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Circular progress stock ring
            Box(
                modifier = Modifier.size(68.dp),
                contentAlignment = Alignment.Center
            ) {
                val progress = if (supp.totalStock > 0) supp.remainingStock.toFloat() / supp.totalStock.toFloat() else 0f
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeW = 6.dp.toPx()
                    drawArc(
                        color = color.copy(alpha = 0.12f),
                        startAngle = 0f,
                        sweepAngle = 360f,
                        useCenter = false,
                        style = Stroke(width = strokeW)
                    )
                    drawArc(
                        color = color,
                        startAngle = -90f,
                        sweepAngle = 360f * progress,
                        useCenter = false,
                        style = Stroke(width = strokeW, cap = StrokeCap.Round)
                    )
                }
                
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    val pct = (progress * 100).toInt()
                    Text(
                        text = "$pct%",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = themeTextTitle
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = supp.name,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = themeTextTitle
                )
                Text(
                    text = "Type: ${supp.type} • Target: ${supp.dailyTarget} ${supp.servingUnit}",
                    fontSize = 11.sp,
                    color = themeTextSubtitle
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Stock: ${supp.remainingStock} / ${supp.totalStock} left",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(
                    onClick = onLog,
                    enabled = supp.remainingStock > 0,
                    modifier = Modifier
                        .size(36.dp)
                        .background(color.copy(alpha = 0.15f), CircleShape)
                        .testTag("log_serving_btn_${supp.name}")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "Log serving",
                        tint = color,
                        modifier = Modifier.size(18.dp)
                    )
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color.Red.copy(alpha = 0.1f), CircleShape)
                        .testTag("delete_supplement_btn_${supp.name}")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Delete tub",
                        tint = Color.Red,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

fun formatReminderTimeString(hour: Int, minute: Int): String {
    val amPm = if (hour < 12) "AM" else "PM"
    val h = if (hour == 0) 12 else if (hour > 12) hour - 12 else hour
    return String.format(Locale.US, "%d:%02d %s", h, minute, amPm)
}

@Composable
fun MonthlyCalendarSection(
    monthName: String,
    days: StableDays,
    startOffset: Int,
    supplements: List<Supplement>,
    supplementLogs: List<SupplementLog>,
    onDayClick: (String) -> Unit,
    onPrevMonth: () -> Unit,
    onNextMonth: () -> Unit,
    cardBackground: Color,
    borderColor: Color,
    textTitleColor: Color,
    textSubColor: Color
) {
    val textMeasurer = rememberTextMeasurer()

    Card(
        colors = CardDefaults.cardColors(containerColor = cardBackground),
        border = BorderStroke(1.dp, borderColor),
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
                            .background(textSubColor.copy(alpha = 0.1f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                            contentDescription = "Prev Month",
                            tint = textTitleColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(
                        onClick = onNextMonth,
                        modifier = Modifier
                            .size(32.dp)
                            .background(textSubColor.copy(alpha = 0.1f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = "Next Month",
                            tint = textTitleColor,
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
                            
                            val dayLogs = supplementLogs.filter { it.date == dayDateStr }
                            val loggedSupplements = supplements.filter { supp -> dayLogs.any { it.supplementId == supp.id } }
                            val isAnyLogged = loggedSupplements.isNotEmpty()

                            val cellLeft = colIndex * cellW
                            val cellTop = rowIndex * cellH
                            val boxLeft = cellLeft + padding
                            val boxTop = cellTop + padding
                            val boxW = cellW - padding * 2
                            val boxH = cellH - padding * 2

                            // 1. Draw Background
                            if (isAnyLogged) {
                                drawRoundRect(
                                    color = textSubColor.copy(alpha = 0.08f),
                                    topLeft = Offset(boxLeft, boxTop),
                                    size = Size(boxW, boxH),
                                    cornerRadius = CornerRadius(cornerRadiusPx, cornerRadiusPx)
                                )
                            }

                            // 2. Draw Border
                            if (isAnyLogged) {
                                drawRoundRect(
                                    color = textSubColor.copy(alpha = 0.2f),
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
                                fontWeight = if (isAnyLogged) FontWeight.Bold else FontWeight.Normal
                            )
                            val textResult = textMeasurer.measure(text = dayNum, style = textStyle)
                            val textLeft = boxLeft + (boxW - textResult.size.width) / 2f
                            val textTop = boxTop + (boxH - textResult.size.height) / 2f - 4.dp.toPx()
                            drawText(textResult, topLeft = Offset(textLeft, textTop))

                            // 4. Draw Pill Indicators (max 4 side-by-side)
                            val count = loggedSupplements.size.coerceAtMost(4)
                            if (count > 0) {
                                val capSingleW = 6.dp.toPx()
                                val capH = 3.dp.toPx()
                                val capCorner = 1.5.dp.toPx()
                                val capsSpace = 2.dp.toPx()
                                val totalCapsW = capSingleW * count + capsSpace * (count - 1)
                                var startLeft = boxLeft + (boxW - totalCapsW) / 2f
                                val capsTop = boxTop + boxH - 7.dp.toPx()
                                
                                for (i in 0 until count) {
                                    val supp = loggedSupplements[i]
                                    val suppColor = try { Color(android.graphics.Color.parseColor(supp.colorTag)) } catch(e: Exception) { Color.LightGray }
                                    drawRoundRect(
                                        color = suppColor,
                                        topLeft = Offset(startLeft, capsTop),
                                        size = Size(capSingleW, capH),
                                        cornerRadius = CornerRadius(capCorner, capCorner)
                                    )
                                    startLeft += capSingleW + capsSpace
                                }
                            }
                        }
                    }
                }
            }


        }
    }
}

@Composable
fun ColorWheel(
    selectedColorHex: String,
    isDarkMode: Boolean,
    onColorChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // Convert hex to hue
    val initialHue = remember(selectedColorHex) {
        val hsv = FloatArray(3)
        try {
            android.graphics.Color.colorToHSV(android.graphics.Color.parseColor(selectedColorHex), hsv)
            hsv[0]
        } catch (e: Exception) {
            0f
        }
    }
    
    var hue by remember(initialHue) { mutableStateOf(initialHue) }
    
    val diameterDp = 130.dp
    val strokeWidthDp = 16.dp
    
    val spectrumColors = remember {
        listOf(
            Color.Red,
            Color.Yellow,
            Color.Green,
            Color.Cyan,
            Color.Blue,
            Color.Magenta,
            Color.Red
        )
    }
    
    val sweepGradientBrush: Brush = remember {
        Brush.sweepGradient(colors = spectrumColors)
    }
    
    val selectedColor = remember(selectedColorHex) {
        try {
            Color(android.graphics.Color.parseColor(selectedColorHex))
        } catch (e: Exception) {
            Color.Red
        }
    }
    
    Box(
        modifier = modifier.size(diameterDp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectDragGestures { change, _ ->
                        change.consume()
                        val center = Offset(size.width / 2f, size.height / 2f)
                        val touch = change.position
                        val x = touch.x - center.x
                        val y = touch.y - center.y
                        var angle = Math.toDegrees(atan2(y.toDouble(), x.toDouble())).toFloat()
                        if (angle < 0) angle += 360f
                        
                        hue = angle
                        val brightness = if (isDarkMode) 0.95f else 0.75f
                        val colorInt = android.graphics.Color.HSVToColor(floatArrayOf(angle, 0.9f, brightness))
                        val hex = String.format("#%06X", 0xFFFFFF and colorInt)
                        onColorChange(hex)
                    }
                }
                .pointerInput(Unit) {
                    detectTapGestures { position ->
                        val center = Offset(size.width / 2f, size.height / 2f)
                        val x = position.x - center.x
                        val y = position.y - center.y
                        var angle = Math.toDegrees(atan2(y.toDouble(), x.toDouble())).toFloat()
                        if (angle < 0) angle += 360f
                        
                        hue = angle
                        val brightness = if (isDarkMode) 0.95f else 0.75f
                        val colorInt = android.graphics.Color.HSVToColor(floatArrayOf(angle, 0.9f, brightness))
                        val hex = String.format("#%06X", 0xFFFFFF and colorInt)
                        onColorChange(hex)
                    }
                }
        ) {
            val strokeWidthPx = strokeWidthDp.toPx()
            val outerRadius = (size.width - strokeWidthPx) / 2f
            
            // Draw Sweep Gradient color spectrum wheel
            drawCircle(
                brush = sweepGradientBrush,
                radius = outerRadius,
                style = Stroke(width = strokeWidthPx)
            )
            
            // Draw active handle indicator on the ring
            val angleRad = Math.toRadians(hue.toDouble())
            val handleX = (size.width / 2f) + outerRadius * cos(angleRad).toFloat()
            val handleY = (size.height / 2f) + outerRadius * sin(angleRad).toFloat()
            
            // Inner white dot
            drawCircle(
                color = Color.White,
                radius = 7.dp.toPx(),
                center = Offset(handleX, handleY)
            )
            
            // Outer thin dark stroke for the indicator ring
            drawCircle(
                color = Color.DarkGray,
                radius = 7.dp.toPx(),
                center = Offset(handleX, handleY),
                style = Stroke(width = 1.5.dp.toPx())
            )
        }
        
        // Inner Preview Circle showing selected color
        val isBright = remember(selectedColor) {
            (selectedColor.red * 0.299f + selectedColor.green * 0.587f + selectedColor.blue * 0.114f) > 0.5f
        }
        Box(
            modifier = Modifier
                .size(72.dp)
                .background(selectedColor, CircleShape)
                .border(BorderStroke(1.dp, if (isDarkMode) Color.White.copy(alpha = 0.3f) else Color.Black.copy(alpha = 0.15f)), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Preview",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (isBright) Color.Black else Color.White
            )
        }
    }
}
