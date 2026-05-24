package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.DailyIntake
import com.example.data.WaterLog
import com.example.data.FoodLog
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.components.HeaderSection
import com.example.ui.screens.*

class MainActivity : ComponentActivity() {
    private val viewModel: IntakeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()
            MyApplicationTheme(darkTheme = isDarkMode, dynamicColor = false) {
                MainTrackerScreen(viewModel = viewModel)
            }
        }
    }
}

@Immutable
data class StableHistory(val list: List<DailyIntake> = emptyList())

@Immutable
data class StableDays(val list: List<String> = emptyList())

@Immutable
data class StableWaterLogs(val list: List<WaterLog> = emptyList())

@Immutable
data class StableFoodLogs(val list: List<FoodLog> = emptyList())

@Composable
fun MainTrackerScreen(
    viewModel: IntakeViewModel,
    modifier: Modifier = Modifier
) {
    val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()
    val activeProfile by viewModel.activeProfile.collectAsStateWithLifecycle()
    val profile1Name by viewModel.profile1Name.collectAsStateWithLifecycle()
    val profile2Name by viewModel.profile2Name.collectAsStateWithLifecycle()

    var currentScreen by remember { mutableStateOf("welcome") }

    val themeBg = if (isDarkMode) Color(0xFF140F0D) else Color(0xFFFDF8F6)
    val ThemeTextTitle = if (isDarkMode) Color(0xFFFFFFFF) else Color(0xFF201A19)
    val ThemeTextSubtitle = if (isDarkMode) Color(0xFF74797A) else Color(0xFF74797A)
    
    val AvatarBackground = if (isDarkMode) Color(0xFFBD8E85) else Color(0xFFEAC2BA)
    val AvatarText = if (isDarkMode) Color(0xFF2E0904) else Color(0xFF531B10)

    Scaffold(
        modifier = modifier.fillMaxSize().background(themeBg),
        containerColor = themeBg,
        contentWindowInsets = WindowInsets.safeDrawing,
        bottomBar = {
            AppBottomNavigationBar(
                currentScreen = currentScreen,
                onScreenSelected = { currentScreen = it },
                isDarkMode = isDarkMode
            )
        }
    ) { innerPadding ->
        val horizontalPadding = if (currentScreen == "water") 0.dp else 16.dp
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(themeBg)
                .padding(innerPadding)
                .padding(horizontal = horizontalPadding),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
        if (currentScreen != "welcome" && currentScreen != "water") {
            item(key = "app_header") {
                Spacer(modifier = Modifier.height(8.dp))
                HeaderSection(
                    isDarkMode = isDarkMode,
                    onToggleDarkMode = { viewModel.toggleDarkMode() },
                    avatarBackground = AvatarBackground,
                    avatarText = AvatarText,
                    textColorTitle = ThemeTextTitle,
                    textColorSubtitle = ThemeTextSubtitle,
                    activeProfile = activeProfile,
                    profile1Name = profile1Name,
                    profile2Name = profile2Name,
                    onSwitchProfile = { viewModel.switchProfile(it) },
                    onRenameProfile = { id, name -> viewModel.renameProfile(id, name) },
                    currentScreen = currentScreen,
                    onScreenChange = { currentScreen = it }
                )
            }
        }


        if (currentScreen == "welcome") {
            item(key = "welcome_screen_content") {
                WelcomeScreen(
                    viewModel = viewModel,
                    isDarkMode = isDarkMode,
                    activeProfile = activeProfile,
                    profile1Name = profile1Name,
                    profile2Name = profile2Name,
                    onScreenChange = { currentScreen = it }
                )
            }
        }

        if (currentScreen == "scoops") {
            item(key = "scoops_screen_content") {
                ScoopsTrackerScreen(
                    viewModel = viewModel,
                    isDarkMode = isDarkMode,
                    onScreenChange = { currentScreen = it }
                )
            }
        }

        if (currentScreen == "water") {
            item(key = "water_screen_content") {
                WaterTrackerScreen(
                    viewModel = viewModel,
                    isDarkMode = isDarkMode,
                    onScreenChange = { currentScreen = it }
                )
            }
        }

        if (currentScreen == "nutrition") {
            item(key = "nutrition_screen_content") {
                NutritionScreen(
                    viewModel = viewModel,
                    isDarkMode = isDarkMode,
                    onScreenChange = { currentScreen = it }
                )
            }
        }

        if (currentScreen == "logs") {
            item(key = "logs_screen_content") {
                Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    Text("Logs Screen Coming Soon", color = ThemeTextTitle)
                }
            }
        }

        if (currentScreen == "goals") {
            item(key = "goals_screen_content") {
                Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    Text("Goals Screen Coming Soon", color = ThemeTextTitle)
                }
            }
        }

        if (currentScreen == "profile") {
            item(key = "profile_screen_content") {
                ProfileScreen(
                    viewModel = viewModel,
                    isDarkMode = isDarkMode
                )
            }
        }
        }
    }
}

@Composable
fun AppBottomNavigationBar(
    currentScreen: String,
    onScreenSelected: (String) -> Unit,
    isDarkMode: Boolean
) {
    val containerColor = if (isDarkMode) Color(0xFF1D1714) else Color.White
    val contentColor = if (isDarkMode) Color.White else Color(0xFF2E1A16)
    val activeIconColor = if (isDarkMode) Color(0xFFFFB4A2) else Color(0xFFD84315)
    val inactiveIconColor = if (isDarkMode) Color(0xFFAFAFAF) else Color(0xFF705244)

    NavigationBar(
        containerColor = containerColor,
        contentColor = contentColor,
        tonalElevation = 8.dp
    ) {
        val navColors = NavigationBarItemDefaults.colors(
            selectedIconColor = activeIconColor,
            unselectedIconColor = inactiveIconColor,
            selectedTextColor = activeIconColor,
            unselectedTextColor = inactiveIconColor,
            indicatorColor = Color.Transparent
        )

        NavigationBarItem(
            selected = currentScreen == "welcome",
            onClick = { onScreenSelected("welcome") },
            icon = { 
                Box(contentAlignment = Alignment.Center) {
                    if (currentScreen == "welcome") {
                        Icon(Icons.Filled.Dashboard, contentDescription = null, modifier = Modifier.blur(8.dp).alpha(0.7f))
                    }
                    Icon(if (currentScreen == "welcome") Icons.Filled.Dashboard else Icons.Outlined.Dashboard, contentDescription = "Dashboard")
                }
            },
            label = { 
                Text(
                    "Dashboard", 
                    fontSize = 10.sp,
                    style = if (currentScreen == "welcome") TextStyle(
                        shadow = Shadow(color = activeIconColor, blurRadius = 15f)
                    ) else TextStyle.Default
                ) 
            },
            colors = navColors
        )
        NavigationBarItem(
            selected = currentScreen == "logs",
            onClick = { onScreenSelected("logs") },
            icon = { 
                Box(contentAlignment = Alignment.Center) {
                    if (currentScreen == "logs") {
                        Icon(Icons.Filled.Article, contentDescription = null, modifier = Modifier.blur(8.dp).alpha(0.7f))
                    }
                    Icon(if (currentScreen == "logs") Icons.Filled.Article else Icons.Outlined.Article, contentDescription = "Logs")
                }
            },
            label = { 
                Text(
                    "Logs", 
                    fontSize = 10.sp,
                    style = if (currentScreen == "logs") TextStyle(
                        shadow = Shadow(color = activeIconColor, blurRadius = 15f)
                    ) else TextStyle.Default
                ) 
            },
            colors = navColors
        )
        NavigationBarItem(
            selected = currentScreen == "goals",
            onClick = { onScreenSelected("goals") },
            icon = { 
                Box(contentAlignment = Alignment.Center) {
                    if (currentScreen == "goals") {
                        Icon(Icons.Filled.TrackChanges, contentDescription = null, modifier = Modifier.blur(8.dp).alpha(0.7f))
                    }
                    Icon(if (currentScreen == "goals") Icons.Filled.TrackChanges else Icons.Outlined.TrackChanges, contentDescription = "Goals")
                }
            },
            label = { 
                Text(
                    "Goals", 
                    fontSize = 10.sp,
                    style = if (currentScreen == "goals") TextStyle(
                        shadow = Shadow(color = activeIconColor, blurRadius = 15f)
                    ) else TextStyle.Default
                ) 
            },
            colors = navColors
        )
        NavigationBarItem(
            selected = currentScreen == "profile",
            onClick = { onScreenSelected("profile") },
            icon = { 
                Box(contentAlignment = Alignment.Center) {
                    if (currentScreen == "profile") {
                        Icon(Icons.Filled.Person, contentDescription = null, modifier = Modifier.blur(8.dp).alpha(0.7f))
                    }
                    Icon(if (currentScreen == "profile") Icons.Filled.Person else Icons.Outlined.Person, contentDescription = "Profile")
                }
            },
            label = { 
                Text(
                    "Profile", 
                    fontSize = 10.sp,
                    style = if (currentScreen == "profile") TextStyle(
                        shadow = Shadow(color = activeIconColor, blurRadius = 15f)
                    ) else TextStyle.Default
                ) 
            },
            colors = navColors
        )
    }
}
