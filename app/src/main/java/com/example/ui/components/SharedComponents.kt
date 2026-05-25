package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HeaderSection(
    isDarkMode: Boolean = false,
    onToggleDarkMode: () -> Unit = {},
    avatarBackground: Color = Color(0xFFEAC2BA),
    avatarText: Color = Color(0xFF531B10),
    textColorTitle: Color = Color(0xFF201A19),
    textColorSubtitle: Color = Color(0xFF74797A),
    activeProfile: String = "profile_1",
    profile1Name: String = "Me",
    profile2Name: String = "Person 2",
    onSwitchProfile: (String) -> Unit = { _ -> },
    onRenameProfile: (String, String) -> Unit = { _, _ -> },
    currentScreen: String = "scoops",
    onScreenChange: (String) -> Unit = {}
) {
    val todayDateString = remember {
        val sdf = SimpleDateFormat("EEEE, MMMM dd", Locale.getDefault())
        sdf.format(Date())
    }

    var showProfileDialog by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (currentScreen != "welcome") {
                IconButton(
                    onClick = { onScreenChange("welcome") },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(avatarBackground.copy(alpha = 0.15f))
                        .testTag("back_to_welcome_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back to Welcome Hub",
                        tint = if (isDarkMode) Color.White else avatarText
                    )
                }
            }
            Column {
                Text(
                    text = when (currentScreen) {
                        "water" -> "Hydration"
                        "nutrition" -> "Nutrition"
                        "scoops" -> "Tub Tracker"
                        else -> "FitFlow Hub"
                    },
                    color = textColorTitle,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.5).sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = todayDateString.uppercase(),
                    color = textColorSubtitle,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.sp
                )
            }
        }
        
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            IconButton(
                onClick = { showProfileDialog = true },
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(avatarBackground.copy(alpha = 0.25f))
                    .testTag("profile_switcher_button")
            ) {
                Icon(
                    imageVector = Icons.Filled.SwitchAccount,
                    contentDescription = "Switch Profile",
                    tint = if (isDarkMode) Color.White else avatarText,
                    modifier = Modifier.size(20.dp)
                )
            }

            IconButton(
                onClick = onToggleDarkMode,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(avatarBackground.copy(alpha = 0.25f))
            ) {
                Icon(
                    imageVector = if (isDarkMode) Icons.Filled.NightsStay else Icons.Filled.WbSunny,
                    contentDescription = "Theme Switcher",
                    tint = if (isDarkMode) Color(0xFFFFF59D) else Color(0xFFF57C00),
                    modifier = Modifier.size(20.dp)
                )
            }

            val activeName = if (activeProfile == "profile_1") profile1Name else profile2Name
            val initials = remember(activeName) {
                val trimmed = activeName.trim()
                if (trimmed.isEmpty()) {
                    "P"
                } else {
                    val parts = trimmed.split(" ")
                    if (parts.size >= 2) {
                        (parts[0].take(1) + parts[1].take(1)).uppercase()
                    } else {
                        trimmed.take(2).uppercase()
                    }
                }
            }

            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(if (isDarkMode) Color.White else avatarBackground)
                    .clickable { onScreenChange("profile") }
                    .testTag("avatar_profile_navigation"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initials,
                    color = if (isDarkMode) Color.Black else avatarText,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }

    if (showProfileDialog) {
        var p1TempName by remember(profile1Name) { 
            mutableStateOf(if (profile1Name == "Me") "" else profile1Name) 
        }
        var p2TempName by remember(profile2Name) { 
            mutableStateOf(if (profile2Name == "Person 2") "" else profile2Name) 
        }
        val dialogCardBg = if (isDarkMode) Color(0xFF241D1A) else Color(0xFFFFFFFF)
        val activeSelectionColor = if (isDarkMode) Color.White else avatarText

        AlertDialog(
            onDismissRequest = { showProfileDialog = false },
            title = {
                Text(
                    text = "Manage Profiles",
                    color = if (isDarkMode) Color.White else textColorTitle,
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
                                if (isDarkMode) Color(0xFF3E2D2A) else avatarBackground.copy(alpha = 0.25f)
                            } else Color.Transparent
                        ),
                        border = BorderStroke(
                            width = if (activeProfile == "profile_1") 2.dp else 1.dp,
                            color = if (activeProfile == "profile_1") {
                                if (isDarkMode) Color.White else activeSelectionColor
                            } else {
                                if (isDarkMode) Color.White.copy(alpha = 0.2f) else textColorSubtitle.copy(alpha = 0.3f)
                            }
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onSwitchProfile("profile_1")
                            },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (activeProfile == "profile_1") "Profile 1 (Active)" else "Profile 1",
                                    color = if (activeProfile == "profile_1") {
                                        if (isDarkMode) Color.White else activeSelectionColor
                                    } else {
                                        if (isDarkMode) Color.White.copy(alpha = 0.6f) else textColorSubtitle
                                    },
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
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
                                    onRenameProfile("profile_1", finalName)
                                },
                                placeholder = { Text("Me", color = if (isDarkMode) Color.White.copy(alpha = 0.5f) else Color.Gray) },
                                label = { Text("Custom Name", color = if (isDarkMode) Color.White.copy(alpha = 0.8f) else textColorSubtitle) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = if (isDarkMode) Color.White else activeSelectionColor,
                                    unfocusedBorderColor = if (isDarkMode) Color.White.copy(alpha = 0.3f) else textColorSubtitle.copy(alpha = 0.4f),
                                    focusedLabelColor = if (isDarkMode) Color.White else activeSelectionColor,
                                    unfocusedLabelColor = if (isDarkMode) Color.White.copy(alpha = 0.6f) else textColorSubtitle,
                                    focusedTextColor = if (isDarkMode) Color.White else textColorTitle,
                                    unfocusedTextColor = if (isDarkMode) Color.White else textColorTitle
                                ),
                                shape = RoundedCornerShape(8.dp),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (activeProfile == "profile_2") {
                                if (isDarkMode) Color(0xFF3E2D2A) else avatarBackground.copy(alpha = 0.25f)
                            } else Color.Transparent
                        ),
                        border = BorderStroke(
                            width = if (activeProfile == "profile_2") 2.dp else 1.dp,
                            color = if (activeProfile == "profile_2") {
                                if (isDarkMode) Color.White else activeSelectionColor
                            } else {
                                if (isDarkMode) Color.White.copy(alpha = 0.2f) else textColorSubtitle.copy(alpha = 0.3f)
                            }
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onSwitchProfile("profile_2")
                            },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (activeProfile == "profile_2") "Profile 2 (Active)" else "Profile 2",
                                    color = if (activeProfile == "profile_2") {
                                        if (isDarkMode) Color.White else activeSelectionColor
                                    } else {
                                        if (isDarkMode) Color.White.copy(alpha = 0.6f) else textColorSubtitle
                                    },
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
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
                                    onRenameProfile("profile_2", finalName)
                                },
                                placeholder = { Text("Person 2", color = if (isDarkMode) Color.White.copy(alpha = 0.5f) else Color.Gray) },
                                label = { Text("Custom Name", color = if (isDarkMode) Color.White.copy(alpha = 0.8f) else textColorSubtitle) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = if (isDarkMode) Color.White else activeSelectionColor,
                                    unfocusedBorderColor = if (isDarkMode) Color.White.copy(alpha = 0.3f) else textColorSubtitle.copy(alpha = 0.4f),
                                    focusedLabelColor = if (isDarkMode) Color.White else activeSelectionColor,
                                    unfocusedLabelColor = if (isDarkMode) Color.White.copy(alpha = 0.6f) else textColorSubtitle,
                                    focusedTextColor = if (isDarkMode) Color.White else textColorTitle,
                                    unfocusedTextColor = if (isDarkMode) Color.White else textColorTitle
                                ),
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
                        containerColor = if (isDarkMode) Color(0xFFBD8E85) else avatarText,
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

@Composable
fun GoalQuickPill(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(color.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
            .border(1.dp, color.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(label, fontSize = 11.sp, color = color, fontWeight = FontWeight.SemiBold)
        Text(value, fontSize = 13.sp, color = color, fontWeight = FontWeight.Black)
    }
}

@Composable
fun GoalInputField(label: String, value: String, onValueChange: (String) -> Unit, modifier: Modifier = Modifier) {
    OutlinedTextField(
        value = value,
        onValueChange = { onValueChange(it.filter { char -> char.isDigit() }) },
        label = { Text(label, fontSize = 10.sp) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
    )
}

@Composable
fun StatLabelPill(text: String, color: Color, isDarkMode: Boolean) {
    Box(
        modifier = Modifier
            .background(color.copy(alpha = 0.08f), RoundedCornerShape(6.dp))
            .border(0.5.dp, color.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(text, fontSize = 10.sp, color = color, fontWeight = FontWeight.Bold)
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
