package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
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
    val containerColor = if (isDarkMode) Color(0xFF121316) else Color.White
    val activeIconColor = Color(0xFF00E5FF) // Neon Cyan
    val inactiveIconColor = if (isDarkMode) Color(0xFFAFAFAF) else Color(0xFF705244)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(containerColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .height(76.dp), // Height matches standard navbar
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            
            @Composable
            fun NavItem(
                id: String,
                label: String,
                activeIcon: androidx.compose.ui.graphics.vector.ImageVector,
                inactiveIcon: androidx.compose.ui.graphics.vector.ImageVector
            ) {
                val isSelected = currentScreen == id
                val color = if (isSelected) activeIconColor else inactiveIconColor
                val interactionSource = remember { MutableInteractionSource() }
                
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null, // Disable default material ripple/pill
                            onClick = { onScreenSelected(id) }
                        )
                        .padding(vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Using a Box to perfectly superimpose the crisp icon over a shape-hugging blurred copy
                    Box(contentAlignment = Alignment.Center) {
                        if (isSelected) {
                            // The path-conforming glow layer (the exact vector paths blurred)
                            Icon(
                                imageVector = activeIcon,
                                contentDescription = null,
                                tint = activeIconColor,
                                modifier = Modifier
                                    .size(28.dp)
                                    .alpha(0.35f) // Subtle, elegant aura
                                    .blur(radius = 8.dp) // Tight shape-hugging blur
                            )
                        }
                        
                        // The crisp, sharp vector icon paths superimposed on top
                        Icon(
                            imageVector = if (isSelected) activeIcon else inactiveIcon,
                            contentDescription = label,
                            tint = color,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = label,
                        fontSize = 11.sp,
                        color = color,
                        style = if (isSelected) TextStyle(
                            shadow = Shadow(color = activeIconColor.copy(alpha = 0.4f), blurRadius = 20f)
                        ) else TextStyle.Default
                    )
                }
            }

            NavItem("welcome", "Dashboard", Icons.Filled.Dashboard, Icons.Outlined.Dashboard)
            NavItem("logs", "Logs", Icons.Filled.Article, Icons.Outlined.Article)
            NavItem("goals", "Goals", Icons.Filled.TrackChanges, Icons.Outlined.TrackChanges)
            NavItem("profile", "Profile", Icons.Filled.Person, Icons.Outlined.Person)
        }
    }
}
