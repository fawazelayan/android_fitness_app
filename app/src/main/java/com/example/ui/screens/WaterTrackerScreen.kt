package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.IntakeViewModel
import com.example.StableWaterLogs
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

    val totalLoggedMl = remember(waterLogsToday) { waterLogsToday.list.sumOf { it.amountMl } }
    val goalMl = remember(waterGoal) { (waterGoal * 1000).toInt() }
    val progress = remember(totalLoggedMl, goalMl) { if (goalMl > 0) (totalLoggedMl.toFloat() / goalMl.toFloat()).coerceIn(0f, 1f) else 0f }

    val themeBg = if (isDarkMode) Color(0xFF121316) else Color(0xFFF4F6F8)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(themeBg),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        
        // CENTERED & ENLARGED HEADER
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)
        ) {
            Text(
                text = "DAILY WATER TRACKER",
                color = if (isDarkMode) Color.White else Color(0xFF0B0B0C),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Filled.WaterDrop,
                contentDescription = null,
                tint = Color(0xFF00E5FF),
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        
        val dateFormat = remember { SimpleDateFormat("EEEE, MMM d", Locale.getDefault()) }
        val dateString = remember { dateFormat.format(Date()) }
        Text(
            text = dateString,
            color = Color(0xFF8F8F93),
            fontSize = 16.sp,
            fontWeight = FontWeight.Normal
        )

        Spacer(modifier = Modifier.height(32.dp))

        // THE GLASS (Centerpiece)
        TranslucentWaterGlass(progress = progress, totalLoggedMl = totalLoggedMl, goalMl = goalMl, isDarkMode = isDarkMode)

        Spacer(modifier = Modifier.height(32.dp))

        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)) {
            // CUSTOM INPUT SECTION
            var customInput by remember { mutableStateOf("") }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = customInput,
                    onValueChange = { newValue ->
                        val clean = newValue.filter { it.isDigit() }
                        if (clean.length <= 4) customInput = clean
                    },
                    placeholder = { Text("Custom mL", color = Color(0xFF8F8F93), fontSize = 14.sp) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF00E5FF),
                        unfocusedBorderColor = Color(0xFF00E5FF).copy(alpha = 0.5f),
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedTextColor = if (isDarkMode) Color.White else Color.Black,
                        unfocusedTextColor = if (isDarkMode) Color.White else Color.Black
                    ),
                    singleLine = true,
                    modifier = Modifier.weight(1f).height(50.dp)
                )

                Button(
                    onClick = {
                        val parsed = customInput.toIntOrNull()
                        if (parsed != null && parsed > 0) {
                            viewModel.addWaterLog(parsed)
                            customInput = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    border = BorderStroke(1.dp, Color(0xFF00E5FF)),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    modifier = Modifier.height(50.dp)
                ) {
                    Text("Add", fontWeight = FontWeight.SemiBold, color = Color(0xFF00E5FF), fontSize = 14.sp)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // QUICK ADD SECTION
            Text(
                text = "Quick Add",
                color = Color(0xFF8F8F93),
                fontSize = 13.sp,
                fontWeight = FontWeight.Normal
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                val presets = listOf(250, 500, 750)
                presets.forEach { ml ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .clip(CircleShape)
                            .border(BorderStroke(1.dp, Color(0xFF00E5FF)), CircleShape)
                            .clickable { viewModel.addWaterLog(ml) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "+${ml}ml",
                            color = Color(0xFF00E5FF),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // LOG HISTORY SECTION
            Text(
                text = "LOG HISTORY",
                color = Color(0xFF8F8F93),
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = if (isDarkMode) Color(0xFF232325) else Color(0xFFFFFFFF))
            ) {
                val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
                if (waterLogsToday.list.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No logs yet", color = Color(0xFF8F8F93))
                    }
                } else {
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        var isExpanded by remember { mutableStateOf(false) }
                        val itemsToShow = if (isExpanded) waterLogsToday.list else waterLogsToday.list.take(5)

                        itemsToShow.forEachIndexed { index, log ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 16.dp, end = 12.dp, top = 12.dp, bottom = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.WaterDrop,
                                    contentDescription = null,
                                    tint = Color(0xFF00E5FF),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Text(
                                    text = "${log.amountMl}ml",
                                    color = if (isDarkMode) Color.White else Color.Black,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Normal
                                )
                                Spacer(modifier = Modifier.weight(1f))
                                Text(
                                    text = "at ${timeFormat.format(Date(log.timestamp))}",
                                    color = Color(0xFF8F8F93),
                                    fontSize = 14.sp
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                IconButton(
                                    onClick = { viewModel.deleteWaterLog(log.id) },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Delete,
                                        contentDescription = "Delete",
                                        tint = Color.Red,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            }
                            if (index < itemsToShow.size - 1) {
                                Divider(color = if (isDarkMode) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.08f), modifier = Modifier.padding(horizontal = 16.dp))
                            }
                        }

                        if (waterLogsToday.list.size > 5) {
                            Divider(color = if (isDarkMode) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.08f), modifier = Modifier.padding(horizontal = 16.dp))
                            val arrowRotation by animateFloatAsState(targetValue = if (isExpanded) 180f else 0f)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { isExpanded = !isExpanded }
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.KeyboardArrowDown,
                                    contentDescription = "Expand/Collapse",
                                    tint = Color(0xFF8F8F93),
                                    modifier = Modifier.rotate(arrowRotation).size(24.dp)
                                )
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun TranslucentWaterGlass(progress: Float, totalLoggedMl: Int, goalMl: Int, isDarkMode: Boolean) {
    Box(
        modifier = Modifier
            .width(220.dp)
            .height(260.dp),
        contentAlignment = Alignment.Center
    ) {
        val animatedProgress by animateFloatAsState(
            targetValue = progress,
            animationSpec = tween(1200, easing = FastOutSlowInEasing)
        )

        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            
            // High-Fidelity Tapered Cup Path
            val topRadius = 8.dp.toPx()
            val bottomRadius = 32.dp.toPx()
            
            val leftTop = w * 0.05f
            val rightTop = w * 0.95f
            val leftBottom = w * 0.2f
            val rightBottom = w * 0.8f
            
            val glassPath = Path().apply {
                moveTo(leftTop, topRadius)
                quadraticTo(leftTop, 0f, leftTop + topRadius, 0f)
                lineTo(rightTop - topRadius, 0f)
                quadraticTo(rightTop, 0f, rightTop, topRadius)
                lineTo(rightBottom, h - bottomRadius)
                quadraticTo(rightBottom, h, rightBottom - bottomRadius, h)
                lineTo(leftBottom + bottomRadius, h)
                quadraticTo(leftBottom, h, leftBottom, h - bottomRadius)
                close()
            }

            // Frosted Translucent Glass Body
            drawPath(
                path = glassPath,
                brush = Brush.linearGradient(
                    listOf(
                        if (isDarkMode) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.03f),
                        if (isDarkMode) Color.White.copy(alpha = 0.03f) else Color.Black.copy(alpha = 0.01f)
                    )
                )
            )

            // Fluid Layer Logic
            val waterPath = Path()
            val fillHeight = h * (1f - animatedProgress)
            val waveHeight = if (animatedProgress > 0.02f && animatedProgress < 0.98f) 12.dp.toPx() else 0f

            waterPath.moveTo(0f, h)
            waterPath.lineTo(0f, fillHeight)
            
            if (waveHeight > 0f) {
                // Smooth quadratic wave
                waterPath.quadraticTo(
                    w * 0.25f, fillHeight + waveHeight,
                    w * 0.5f, fillHeight
                )
                waterPath.quadraticTo(
                    w * 0.75f, fillHeight - waveHeight,
                    w, fillHeight
                )
            } else {
                waterPath.lineTo(w, fillHeight)
            }

            waterPath.lineTo(w, h)
            waterPath.close()

            if (animatedProgress > 0.001f) {
                clipPath(glassPath) {
                    drawPath(
                        path = waterPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(Color(0xFF00E5FF), Color(0xFF0091EA)),
                            startY = minOf(fillHeight - waveHeight, h - 1f),
                            endY = h
                        )
                    )
                }
            }

            // High-Fidelity Edge Reflection
            drawPath(
                path = glassPath,
                brush = Brush.verticalGradient(
                    listOf(
                        if (isDarkMode) Color.White.copy(alpha = 0.4f) else Color.Black.copy(alpha = 0.2f),
                        if (isDarkMode) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.05f)
                    )
                ),
                style = Stroke(width = 2.dp.toPx())
            )
            
            // Faint vertical highlight reflection on the left side
            val leftReflectionPath = Path().apply {
                moveTo(w * 0.13f, h * 0.1f)
                lineTo(w * 0.24f, h * 0.85f)
            }
            drawPath(
                path = leftReflectionPath,
                color = if (isDarkMode) Color.White.copy(alpha = 0.15f) else Color.Black.copy(alpha = 0.08f),
                style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round)
            )
        }

        // Overlay Text
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            Text(
                text = "TODAY'S PROGRESS",
                color = if (isDarkMode) Color.White.copy(alpha = 0.7f) else Color.Black.copy(alpha = 0.7f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            val litersLogged = String.format(Locale.US, "%.1f", totalLoggedMl / 1000f)
            val litersGoal = String.format(Locale.US, "%.1f", goalMl / 1000f)
            
            Text(
                text = "${litersLogged}L",
                color = if (isDarkMode) Color.White else Color.Black,
                fontSize = 54.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "/ ${litersGoal}L Goal",
                color = if (isDarkMode) Color.White.copy(alpha = 0.9f) else Color.Black.copy(alpha = 0.8f),
                fontSize = 18.sp,
                fontWeight = FontWeight.Normal
            )
        }
    }
}
