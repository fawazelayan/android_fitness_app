package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val viewModel: IntakeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()
            var showSplash by remember { mutableStateOf(true) }

            MyApplicationTheme(darkTheme = isDarkMode, dynamicColor = false) {
                Crossfade(targetState = showSplash, label = "splash_fade") { isSplash ->
                    if (isSplash) {
                        AnimatedSplashScreen(onSplashFinished = { showSplash = false })
                    } else {
                        MainTrackerScreen(viewModel = viewModel)
                    }
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

    val themeBg = if (isDarkMode) Color(0xFF0B0B0C) else Color(0xFFF4F6F8)
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
        if (currentScreen != "welcome" && currentScreen != "water" && currentScreen != "scoops") {
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
    val containerColor = if (isDarkMode) Color(0xFF121316) else Color(0xFFF4F6F8)
    val activeIconColor = Color(0xFF00E5FF) // Neon Cyan
    val inactiveIconColor = if (isDarkMode) Color(0xFFAFAFAF) else Color(0xFF212121)

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
                            if (isDarkMode) {
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
                    val labelColor = if (isDarkMode) {
                        if (isSelected) activeIconColor else inactiveIconColor
                    } else {
                        Color(0xFF212121) // All label text is black in light mode
                    }
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = label,
                        fontSize = 11.sp,
                        color = labelColor,
                        style = if (isSelected && isDarkMode) TextStyle(
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

@Composable
fun AnimatedSplashScreen(onSplashFinished: () -> Unit) {

    // ── Stage 1: Glow bloom ───────────────────────────────────────────────────
    val bloomAlpha  = remember { Animatable(0f) }
    val bloomRadius = remember { Animatable(20f) }

    // ── Stage 2: Logo ─────────────────────────────────────────────────────────
    val logoScale = remember { Animatable(0.18f) }
    val logoAlpha = remember { Animatable(0f) }
    val logoRotZ  = remember { Animatable(-10f) }

    // ── Stage 3: Shockwave rings ──────────────────────────────────────────────
    val r1Radius = remember { Animatable(0f) }; val r1Alpha = remember { Animatable(0f) }
    val r2Radius = remember { Animatable(0f) }; val r2Alpha = remember { Animatable(0f) }
    val r3Radius = remember { Animatable(0f) }; val r3Alpha = remember { Animatable(0f) }

    // ── Stage 4: Orbiting particles ───────────────────────────────────────────
    val pProg  = remember { List(8) { Animatable(0f) } }
    val pAlpha = remember { List(8) { Animatable(0f) } }

    // ── Stage 5: Full-screen fade-out ─────────────────────────────────────────
    val screenAlpha = remember { Animatable(1f) }

    LaunchedEffect(Unit) {

        // Stage 1 – Bloom (t = 0 ms)
        launch { bloomAlpha.animateTo(0.88f, tween(500)) }
        launch { bloomRadius.animateTo(175f, tween(650, easing = FastOutSlowInEasing)) }

        // Stage 2 – Logo materialises (t = 200 ms)
        launch {
            delay(200)
            launch { logoAlpha.animateTo(1f, tween(700, easing = FastOutSlowInEasing)) }
            launch { logoScale.animateTo(1f, spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow)) }
            launch { logoRotZ.animateTo(0f, spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow)) }
        }

        // Stage 3 – Shockwave rings (t = 620 / 840 / 1060 ms)
        launch {
            delay(620)
            r1Alpha.animateTo(1f, tween(60))
            launch { r1Radius.animateTo(295f, tween(950, easing = FastOutSlowInEasing)) }
            r1Alpha.animateTo(0f, tween(950))
        }
        launch {
            delay(840)
            r2Alpha.animateTo(1f, tween(60))
            launch { r2Radius.animateTo(295f, tween(950, easing = FastOutSlowInEasing)) }
            r2Alpha.animateTo(0f, tween(950))
        }
        launch {
            delay(1060)
            r3Alpha.animateTo(1f, tween(60))
            launch { r3Radius.animateTo(295f, tween(950, easing = FastOutSlowInEasing)) }
            r3Alpha.animateTo(0f, tween(950))
        }

        // Stage 4 – Particles (t = 900 ms, staggered)
        pProg.forEachIndexed { i, prog ->
            launch {
                delay(900 + i * 110L)
                launch { prog.animateTo(1f, tween(1000)) }
                pAlpha[i].animateTo(1f, tween(250))
                delay(650)
                pAlpha[i].animateTo(0f, tween(400))
            }
        }

        // Stage 5 – Fade to black (t = 1950 ms)
        delay(1950)
        screenAlpha.animateTo(0f, tween(420))
        onSplashFinished()
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Layout
    // ─────────────────────────────────────────────────────────────────────────
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0B0B0C))
            .graphicsLayer(alpha = screenAlpha.value),
        contentAlignment = Alignment.Center
    ) {

        // ── Full-screen canvas — bloom, rings, particles (drawn BEHIND logo) ──
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cx = size.width / 2f
            val cy = size.height / 2f

            // Dual bloom: cyan (left-centre) + red (right-centre)
            val bR = bloomRadius.value.dp.toPx()
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0x5500E5FF), Color(0x2200AABB), Color.Transparent),
                    center = Offset(cx - bR * 0.12f, cy),
                    radius = bR
                ),
                center = Offset(cx - bR * 0.12f, cy),
                radius = bR,
                alpha = bloomAlpha.value
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0x40FF5555), Color.Transparent),
                    center = Offset(cx + bR * 0.28f, cy - bR * 0.08f),
                    radius = bR * 0.68f
                ),
                center = Offset(cx + bR * 0.28f, cy - bR * 0.08f),
                radius = bR * 0.68f,
                alpha = bloomAlpha.value * 0.72f
            )

            // Shockwave rings
            listOf(
                Triple(r1Radius, r1Alpha, Color(0xFF00E5FF)),
                Triple(r2Radius, r2Alpha, Color(0xFFFF6B6B)),
                Triple(r3Radius, r3Alpha, Color(0xFF3DFF6E))
            ).forEach { (rad, alp, col) ->
                if (alp.value > 0f) {
                    val r = rad.value.dp.toPx()
                    val sw = (3.8f * (1f - rad.value / 295f)).coerceAtLeast(0.5f).dp.toPx()
                    drawCircle(col, radius = r, center = Offset(cx, cy),
                        style = Stroke(sw), alpha = alp.value)
                }
            }

            // Orbiting ember particles
            pProg.forEachIndexed { i, prog ->
                val a = pAlpha[i].value
                if (a > 0f) {
                    val angle = i * 45.0 * (Math.PI / 180.0)
                    val orbitR = 102.dp.toPx()
                    val px = cx + (orbitR * Math.cos(angle)).toFloat()
                    val py = cy + (orbitR * Math.sin(angle)).toFloat() - prog.value * 62.dp.toPx()
                    val pc = if (i % 2 == 0) Color(0xFF00E5FF) else Color(0xFFFF6B6B)
                    val pr = (3f + i % 3).dp.toPx()
                    // Soft glow halo
                    drawCircle(pc.copy(alpha = 0.28f * a), radius = pr * 3.2f, center = Offset(px, py))
                    // Bright core
                    drawCircle(pc, radius = pr, center = Offset(px, py), alpha = a)
                }
            }
        }

        // ── Logo canvas — drawn on top ────────────────────────────────────────
        Canvas(
            modifier = Modifier
                .size(184.dp)
                .graphicsLayer(
                    scaleX = logoScale.value,
                    scaleY = logoScale.value,
                    alpha = logoAlpha.value,
                    rotationZ = logoRotZ.value
                )
        ) {
            val w  = size.width
            val h  = size.height
            val cx = w / 2f
            val cy = h / 2f

            // ── Rounded rectangle background ──────────────────────────────────
            val cr = w * 0.23f
            drawRoundRect(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF1C2030), Color(0xFF0C0D15)),
                    center = Offset(cx, cy * 0.50f),
                    radius = w * 0.88f
                ),
                cornerRadius = CornerRadius(cr)
            )
            // Gloss sheen at top
            drawRoundRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0x26FFFFFF), Color.Transparent),
                    startY = 0f, endY = h * 0.43f
                ),
                cornerRadius = CornerRadius(cr)
            )

            // ── LEFT wing (cyan) ─────────────────────────────────────────────
            val cyan = Color(0xFF00E5FF)

            // Outer feather — widest, most transparent
            drawPath(Path().apply {
                moveTo(cx - w * 0.05f, cy + h * 0.08f)
                cubicTo(cx - w * 0.23f, cy + h * 0.03f,
                    cx - w * 0.38f, cy - h * 0.07f,
                    cx - w * 0.36f, cy - h * 0.29f)
                cubicTo(cx - w * 0.27f, cy - h * 0.18f,
                    cx - w * 0.14f, cy - h * 0.04f,
                    cx - w * 0.04f, cy + h * 0.01f)
                close()
            }, color = cyan.copy(alpha = 0.27f))

            // Middle feather
            drawPath(Path().apply {
                moveTo(cx - w * 0.05f, cy + h * 0.04f)
                cubicTo(cx - w * 0.17f, cy + h * 0.00f,
                    cx - w * 0.27f, cy - h * 0.13f,
                    cx - w * 0.23f, cy - h * 0.29f)
                cubicTo(cx - w * 0.16f, cy - h * 0.20f,
                    cx - w * 0.09f, cy - h * 0.08f,
                    cx - w * 0.04f, cy - h * 0.01f)
                close()
            }, color = cyan.copy(alpha = 0.60f))

            // Inner feather — sharpest, brightest
            drawPath(Path().apply {
                moveTo(cx - w * 0.05f, cy + h * 0.01f)
                cubicTo(cx - w * 0.10f, cy - h * 0.05f,
                    cx - w * 0.15f, cy - h * 0.17f,
                    cx - w * 0.11f, cy - h * 0.27f)
                cubicTo(cx - w * 0.07f, cy - h * 0.18f,
                    cx - w * 0.04f, cy - h * 0.09f,
                    cx - w * 0.03f, cy - h * 0.02f)
                close()
            }, color = cyan.copy(alpha = 0.93f))

            // Glowing leading edge stroke
            drawPath(Path().apply {
                moveTo(cx - w * 0.05f, cy + h * 0.04f)
                cubicTo(cx - w * 0.17f, cy + h * 0.00f,
                    cx - w * 0.27f, cy - h * 0.13f,
                    cx - w * 0.23f, cy - h * 0.29f)
            }, color = cyan.copy(alpha = 0.96f), style = Stroke(1.6.dp.toPx()))

            // ── RIGHT wing (red/orange) ──────────────────────────────────────
            val red = Color(0xFFFF5555)

            // Outer feather
            drawPath(Path().apply {
                moveTo(cx + w * 0.05f, cy + h * 0.08f)
                cubicTo(cx + w * 0.23f, cy + h * 0.03f,
                    cx + w * 0.38f, cy - h * 0.07f,
                    cx + w * 0.36f, cy - h * 0.29f)
                cubicTo(cx + w * 0.27f, cy - h * 0.18f,
                    cx + w * 0.14f, cy - h * 0.04f,
                    cx + w * 0.04f, cy + h * 0.01f)
                close()
            }, color = red.copy(alpha = 0.27f))

            // Middle feather
            drawPath(Path().apply {
                moveTo(cx + w * 0.05f, cy + h * 0.04f)
                cubicTo(cx + w * 0.17f, cy + h * 0.00f,
                    cx + w * 0.27f, cy - h * 0.13f,
                    cx + w * 0.23f, cy - h * 0.29f)
                cubicTo(cx + w * 0.16f, cy - h * 0.20f,
                    cx + w * 0.09f, cy - h * 0.08f,
                    cx + w * 0.04f, cy - h * 0.01f)
                close()
            }, color = red.copy(alpha = 0.60f))

            // Inner feather
            drawPath(Path().apply {
                moveTo(cx + w * 0.05f, cy + h * 0.01f)
                cubicTo(cx + w * 0.10f, cy - h * 0.05f,
                    cx + w * 0.15f, cy - h * 0.17f,
                    cx + w * 0.11f, cy - h * 0.27f)
                cubicTo(cx + w * 0.07f, cy - h * 0.18f,
                    cx + w * 0.04f, cy - h * 0.09f,
                    cx + w * 0.03f, cy - h * 0.02f)
                close()
            }, color = red.copy(alpha = 0.93f))

            // Glowing leading edge stroke
            drawPath(Path().apply {
                moveTo(cx + w * 0.05f, cy + h * 0.04f)
                cubicTo(cx + w * 0.17f, cy + h * 0.00f,
                    cx + w * 0.27f, cy - h * 0.13f,
                    cx + w * 0.23f, cy - h * 0.29f)
            }, color = red.copy(alpha = 0.96f), style = Stroke(1.6.dp.toPx()))

            // ── Letter "F" with neon glow ─────────────────────────────────────
            drawContext.canvas.nativeCanvas.apply {
                val fs = w * 0.40f
                val fx = cx
                val fy = cy + h * 0.14f

                // Soft outer glow
                drawText("F", fx, fy, android.graphics.Paint().apply {
                    isAntiAlias  = true
                    textAlign    = android.graphics.Paint.Align.CENTER
                    textSize     = fs
                    typeface     = android.graphics.Typeface.create(
                        android.graphics.Typeface.DEFAULT_BOLD, android.graphics.Typeface.BOLD)
                    color        = android.graphics.Color.argb(95, 170, 225, 255)
                    maskFilter   = android.graphics.BlurMaskFilter(fs * 0.22f,
                        android.graphics.BlurMaskFilter.Blur.NORMAL)
                })

                // Crisp white-to-silver gradient letterform
                drawText("F", fx, fy, android.graphics.Paint().apply {
                    isAntiAlias = true
                    textAlign   = android.graphics.Paint.Align.CENTER
                    textSize    = fs
                    typeface    = android.graphics.Typeface.create(
                        android.graphics.Typeface.DEFAULT_BOLD, android.graphics.Typeface.BOLD)
                    shader = android.graphics.LinearGradient(
                        fx, fy - fs * 0.85f, fx, fy + fs * 0.08f,
                        intArrayOf(
                            android.graphics.Color.argb(255, 255, 255, 255),
                            android.graphics.Color.argb(255, 185, 222, 255)
                        ),
                        null,
                        android.graphics.Shader.TileMode.CLAMP
                    )
                })
            }
        }
    }
}

