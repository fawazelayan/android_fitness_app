package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun CircularTimeSelector(
    hour: Int,
    minute: Int,
    onTimeChanged: (hour: Int, minute: Int) -> Unit,
    activeColor: Color,
    isDarkMode: Boolean,
    testTagPrefix: String = "reminder"
) {
    var localHour by remember(hour) { mutableStateOf(hour) }
    var localMinute by remember(minute) { mutableStateOf(minute) }

    // Force minute to be rounded to the nearest multiple of 5 initially or on change
    val roundedMinute = (localMinute / 5) * 5
    val currentTotalMinutes = localHour * 60 + roundedMinute

    val backgroundColors = remember(isDarkMode) {
        if (isDarkMode) listOf(Color(0xFF112534), Color(0xFF0C1924)) else listOf(Color(0xFFFAFDFF), Color(0xFFF1F8FF))
    }
    val sweepColors = remember(activeColor) {
        listOf(
            activeColor.copy(alpha = 0.3f),
            activeColor,
            activeColor.copy(alpha = 0.3f)
        )
    }

    var boxWidth by remember { mutableStateOf(0f) }
    var boxHeight by remember { mutableStateOf(0f) }

    // Time increment/decrement logic with wrap-around
    val onIncrement = {
        val nextTotal = (currentTotalMinutes + 5) % 1440
        val nextHour = nextTotal / 60
        val nextMin = (nextTotal % 60)
        localHour = nextHour
        localMinute = nextMin
        onTimeChanged(nextHour, nextMin)
    }

    val onDecrement = {
        val prevTotal = if (currentTotalMinutes - 5 < 0) 1440 - 5 else currentTotalMinutes - 5
        val prevHour = prevTotal / 60
        val prevMin = (prevTotal % 60)
        localHour = prevHour
        localMinute = prevMin
        onTimeChanged(prevHour, prevMin)
    }

    // Modern styled clock face
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Decrement Button
            IconButton(
                onClick = onDecrement,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(if (isDarkMode) Color(0xFF1E2F3D) else Color(0xFFF1F8FF))
                    .testTag("${testTagPrefix}_decrement"),
            ) {
                Icon(
                    imageVector = Icons.Filled.Remove,
                    contentDescription = "Subtract 5 minutes",
                    tint = activeColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(20.dp))

            // Central Circle Clock representation
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .onSizeChanged {
                        boxWidth = it.width.toFloat()
                        boxHeight = it.height.toFloat()
                    }
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = backgroundColors
                        )
                    )
                    .border(
                        width = 4.dp,
                        brush = Brush.sweepGradient(
                            colors = sweepColors
                        ),
                        shape = CircleShape
                    )
                    .pointerInput(Unit) {
                        awaitPointerEventScope {
                            var isDragging = false
                            while (true) {
                                val event = awaitPointerEvent()
                                val dragEvent = event.changes.firstOrNull()
                                if (dragEvent != null) {
                                    if (dragEvent.pressed) {
                                        val changed = dragEvent.position != dragEvent.previousPosition
                                        if (changed) {
                                            dragEvent.consume()
                                        }
                                        isDragging = true
                                        val posX = dragEvent.position.x
                                        val posY = dragEvent.position.y
                                        val cX = boxWidth / 2f
                                        val cY = boxHeight / 2f
                                        if (cX > 0 && cY > 0) {
                                            val angleRad = Math.atan2((posY - cY).toDouble(), (posX - cX).toDouble())
                                            var angleDeg = Math.toDegrees(angleRad) + 90.0
                                            if (angleDeg < 0) {
                                                angleDeg += 360.0
                                            }
                                            val step = ((angleDeg / 360.0) * 288.0).roundToInt() % 288
                                            val targetStep = if (step < 0) step + 288 else step
                                            val totalMinutes = targetStep * 5
                                            val nextHour = (totalMinutes / 60) % 24
                                            val nextMin = totalMinutes % 60
                                            localHour = nextHour
                                            localMinute = nextMin
                                        }
                                    } else {
                                        if (isDragging) {
                                            isDragging = false
                                            onTimeChanged(localHour, localMinute)
                                        }
                                    }
                                }
                            }
                        }
                    }
                    .testTag("${testTagPrefix}_circle_face"),
                contentAlignment = Alignment.Center
            ) {
                // Background visual tick canvas to represent the 24 hour radial position
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val centerX = size.width / 2f
                    val centerY = size.height / 2f
                    val radius = size.minDimension / 2f - 12.dp.toPx()
                    
                    // Draw continuous progress arc based on the time position through the day (288 ticks)
                    val totalTicks = 288
                    val currentTick = (localHour * 12) + (roundedMinute / 5)
                    val sweepAngle = (currentTick.toFloat() / totalTicks.toFloat()) * 360f
                    
                    // Draw outer subtle glowing track
                    drawCircle(
                        color = activeColor.copy(alpha = 0.08f),
                        radius = radius + 6.dp.toPx(),
                        style = Stroke(2.dp.toPx())
                    )
                    
                    // Draw arc from top (-90 degrees)
                    drawArc(
                        color = activeColor,
                        startAngle = -90f,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        topLeft = androidx.compose.ui.geometry.Offset(centerX - radius, centerY - radius),
                        size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
                        style = Stroke(
                            width = 4.dp.toPx(),
                            cap = androidx.compose.ui.graphics.StrokeCap.Round
                        )
                    )

                    // Draw subtle clocks ticks at every 30 degrees (each represents 2 hours)
                    for (angle in 0 until 360 step 30) {
                        val radian = Math.toRadians((angle - 90).toDouble())
                        val tickStartRadius = radius - 8.dp.toPx()
                        val tickEndRadius = radius - 2.dp.toPx()
                        
                        val startX = centerX + tickStartRadius * Math.cos(radian).toFloat()
                        val startY = centerY + tickStartRadius * Math.sin(radian).toFloat()
                        val endX = centerX + tickEndRadius * Math.cos(radian).toFloat()
                        val endY = centerY + tickEndRadius * Math.sin(radian).toFloat()
                        
                        val isHighlighted = (angle == (currentTick * 360 / totalTicks) % 360)
                        drawLine(
                            color = if (isHighlighted) activeColor else activeColor.copy(alpha = 0.25f),
                            start = androidx.compose.ui.geometry.Offset(startX, startY),
                            end = androidx.compose.ui.geometry.Offset(endX, endY),
                            strokeWidth = if (isHighlighted) 3.dp.toPx() else 1.5.dp.toPx(),
                            cap = androidx.compose.ui.graphics.StrokeCap.Round
                        )
                    }
                }

                // Inner content displaying the formatted clock output
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    val displayTimeStr = String.format(Locale.getDefault(), "%02d:%02d", localHour, roundedMinute)
                    val amPmSuffix = if (localHour >= 12) "PM" else "AM"
                    val hr12 = when {
                        localHour == 0 -> 12
                        localHour > 12 -> localHour - 12
                        else -> localHour
                    }
                    val display12HrStr = String.format(Locale.getDefault(), "%d:%02d", hr12, roundedMinute)

                    Text(
                        text = display12HrStr,
                        color = if (isDarkMode) Color.White else Color(0xFF0F1E29),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black
                    )
                    
                    Text(
                        text = amPmSuffix,
                        color = activeColor,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = "($displayTimeStr 24h)",
                        color = if (isDarkMode) Color.White.copy(alpha = 0.5f) else Color.Black.copy(alpha = 0.4f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.width(20.dp))

            // Increment Button
            IconButton(
                onClick = onIncrement,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(if (isDarkMode) Color(0xFF1E2F3D) else Color(0xFFF1F8FF))
                    .testTag("${testTagPrefix}_increment"),
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Add 5 minutes",
                    tint = activeColor,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // Quick jump preset pills
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val quickLabels = remember { listOf("-1h", "-30m", "+30m", "+1h") }
            val quickActions = remember { listOf(-60, -30, 30, 60) }
            
            quickLabels.forEachIndexed { idx, label ->
                Surface(
                    onClick = {
                        val mins = quickActions[idx]
                        var nextTotal = (currentTotalMinutes + mins) % 1440
                        if (nextTotal < 0) {
                            nextTotal += 1440
                        }
                        onTimeChanged(nextTotal / 60, nextTotal % 60)
                    },
                    color = if (isDarkMode) Color(0xFF152633) else Color(0xFFE1F5FE),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, activeColor.copy(alpha = 0.25f)),
                    modifier = Modifier
                        .height(30.dp)
                        .testTag("${testTagPrefix}_quick_${label}")
                ) {
                    Box(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            color = activeColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
