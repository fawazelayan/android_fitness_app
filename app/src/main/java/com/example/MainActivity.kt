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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
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
                val themeBg = if (isDarkMode) Color(0xFF140F0D) else Color(0xFFFDF8F6)
                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(themeBg),
                    containerColor = themeBg,
                    contentWindowInsets = WindowInsets.safeDrawing
                ) { innerPadding ->
                    MainTrackerScreen(
                        viewModel = viewModel,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
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

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(themeBg)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (currentScreen != "welcome") {
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
