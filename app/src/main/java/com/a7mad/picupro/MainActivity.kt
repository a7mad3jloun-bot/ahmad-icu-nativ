package com.a7mad.picupro

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.security.MessageDigest
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

object GlassColors {
    val MidnightBlue = Color(0xFF050A1F)
    val ElectricBlue = Color(0xFF00D4FF)
    val VividPurple = Color(0xFFB829DD)
    val NeonCyan = Color(0xFF00F0FF)
    val GlassWhite = Color(0xFFFFFFFF)
    val GlassBorder = Color(0x33FFFFFF)
    val DangerRed = Color(0xFFEF4444)
    val WarningYellow = Color(0xFFEAB308)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = darkColorScheme()) {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        MainNavigationScreen()
                    }
                }
            }
        }
    }
}

@Composable
fun MainNavigationScreen() {
    var currentScreen by remember { mutableStateOf("activation") }
    var confirmedDeviceId by remember { mutableStateOf("") }
    var selectedDrugForCalculation by remember { mutableStateOf<MedicalDrug?>(null) }
    var selectedProtocolForCalculation by remember { mutableStateOf<DrugProtocol?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        when (currentScreen) {
            "activation" -> ActivationScreenVisual(
                onActivationSuccess = { deviceId ->
                    confirmedDeviceId = deviceId
                    currentScreen = "calculator"
                }
            )
            "calculator" -> CalculatorScreen(
                onNavigateToAbout = { currentScreen = "about" },
                onNavigateToCalculate = { drug, protocol ->
                    selectedDrugForCalculation = drug
                    selectedProtocolForCalculation = protocol
                    currentScreen = "calculation_panel"
                }
            )
            "calculation_panel" -> CalculationPanelScreen(
                drug = selectedDrugForCalculation!!,
                protocol = selectedProtocolForCalculation!!,
                onBack = { currentScreen = "calculator" }
            )
            "about" -> AboutScreen(
                onBack = { currentScreen = "calculator" }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivationScreenVisual(onActivationSuccess: (String) -> Unit) {
    val context = LocalContext.current
    val deviceId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID) ?: "UNKNOWN"
    var activationCode by remember { mutableStateOf("") }
    var isFocused by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val glowPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "glow_phase"
    )

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Box(modifier = Modifier.fillMaxSize().background(GlassColors.MidnightBlue)) {
        NeonBackground(glowPhase)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            GlowingMedicalIcon(pulseScale, glowPhase)

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Ahmad Qudah",
                fontSize = 36.sp,
                fontWeight = FontWeight.ExtraBold,
                color = GlassColors.GlassWhite,
                textAlign = TextAlign.Center,
                modifier = Modifier.graphicsLayer { shadowElevation = 20f },
                style = TextStyle(
                    brush = Brush.linearGradient(
                        colors = listOf(GlassColors.ElectricBlue, GlassColors.VividPurple, GlassColors.GlassWhite)
                    )
                )
            )

            Text(
                text = "Medications Calculations",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = GlassColors.ElectricBlue.copy(alpha = 0.8f),
                textAlign = TextAlign.Center,
                letterSpacing = 2.sp,
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(40.dp))

            GlassCard(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp), glowPhase = glowPhase) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    DeviceIdCapsule(deviceId, context)

                    Spacer(modifier = Modifier.height(24.dp))

                    NeonTextField(
                        value = activationCode,
                        onValueChange = { activationCode = it },
                        isFocused = isFocused,
                        onFocusChange = { isFocused = it },
                        glowPhase = glowPhase
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    GlowingGradientButton(
                        text = "VERIFY & ACTIVATE",
                        onClick = {
                            if (verifyActivationCode(deviceId, activationCode)) {
                                Toast.makeText(context, "Activated Successfully!", Toast.LENGTH_LONG).show()
                                onActivationSuccess(deviceId)
                            } else {
                                Toast.makeText(context, "Invalid Code!", Toast.LENGTH_SHORT).show()
                            }
                        },
                        glowPhase = glowPhase
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    WhatsAppOutlineButton(
                        onClick = {
                            val phoneNumber = "+962782088812"
                            val message = "Hello, I need an activation code for PICUCalculator.\nMy Device ID is: $deviceId"
                            val url = "https://api.whatsapp.com/send?phone=$phoneNumber&text=${Uri.encode(message)}"
                            try { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) } catch (e: Exception) {
                                Toast.makeText(context, "WhatsApp is not installed.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                }
            }
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
private fun NeonBackground(glowPhase: Float) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        drawRect(color = GlassColors.MidnightBlue)
        val centerX = size.width / 2f
        val centerY = size.height / 3f

        val cos1 = cos(glowPhase.toDouble()).toFloat()
        val sin1 = sin((glowPhase * 0.7f).toDouble()).toFloat()

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(GlassColors.ElectricBlue.copy(alpha = 0.15f), GlassColors.ElectricBlue.copy(alpha = 0.05f), Color.Transparent),
                center = Offset(centerX + cos1 * 50f, centerY + sin1 * 30f),
                radius = size.width * 0.6f
            ),
            radius = size.width * 0.6f,
            center = Offset(centerX + cos1 * 50f, centerY + sin1 * 30f)
        )

        val cos2 = cos((glowPhase * 0.8f).toDouble()).toFloat()
        val sin2 = sin(glowPhase.toDouble()).toFloat()

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(GlassColors.VividPurple.copy(alpha = 0.12f), GlassColors.VividPurple.copy(alpha = 0.04f), Color.Transparent),
                center = Offset(centerX - cos2 * 60f, centerY + sin2 * 40f + 200f),
                radius = size.width * 0.5f
            ),
            radius = size.width * 0.5f,
            center = Offset(centerX - cos2 * 60f, centerY + sin2 * 40f + 200f)
        )

        for (i in 0 until 60) {
            val x = (i * 137.5f) % size.width
            val y = (i * 71.3f) % size.height
            val twinkle = sin((glowPhase * 2f + i).toDouble()).toFloat() * 0.5f + 0.5f
            drawCircle(color = GlassColors.GlassWhite.copy(alpha = 0.3f * twinkle), radius = 1.5f, center = Offset(x, y))
        }
    }
}

@Composable
private fun GlowingMedicalIcon(scale: Float, glowPhase: Float) {
    val glowIntensity = sin(glowPhase.toDouble()).toFloat() * 0.3f + 0.7f
    Box(
        modifier = Modifier
            .size(100.dp)
            .scale(scale)
            .drawBehind {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(GlassColors.ElectricBlue.copy(alpha = 0.4f * glowIntensity), GlassColors.VividPurple.copy(alpha = 0.2f * glowIntensity), Color.Transparent)
                    ),
                    radius = size.width * 0.8f
                )
                drawCircle(color = GlassColors.ElectricBlue.copy(alpha = 0.6f), radius = size.width * 0.45f, style = Stroke(width = 2.dp.toPx()))
            },
        contentAlignment = Alignment.Center
    ) {
        Icon(imageVector = Icons.Filled.Favorite, contentDescription = "Heart", modifier = Modifier.size(48.dp).graphicsLayer { shadowElevation = 30f }, tint = GlassColors.ElectricBlue)
    }
}

@Composable
private fun GlassCard(modifier: Modifier = Modifier, glowPhase: Float, content: @Composable () -> Unit) {
    val borderGlow = sin((glowPhase * 1.5f).toDouble()).toFloat() * 0.3f + 0.7f
    Box(
        modifier = modifier
            .graphicsLayer { rotationX = 2f; rotationY = 0f; cameraDistance = 12f; shadowElevation = 25f }
            .drawBehind {
                val width = size.width
                val height = size.height

                drawRoundRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0x20FFFFFF), Color(0x10FFFFFF), Color(0x08FFFFFF), Color(0x15FFFFFF))
                    ),
                    cornerRadius = CornerRadius(24.dp.toPx()),
                    size = Size(width, height)
                )

                drawRect(
                    brush = Brush.linearGradient(
                        colors = listOf(Color(0x40FFFFFF), Color(0x10FFFFFF), Color.Transparent),
                        start = Offset(0f, 0f), end = Offset(width * 0.5f, height * 0.4f)
                    ),
                    size = Size(width * 0.6f, height * 0.5f)
                )

                drawRoundRect(color = GlassColors.ElectricBlue.copy(alpha = 0.6f * borderGlow), cornerRadius = CornerRadius(24.dp.toPx()), size = Size(width, height), style = Stroke(width = 1.5.dp.toPx()))
                drawRoundRect(color = GlassColors.VividPurple.copy(alpha = 0.3f * borderGlow), cornerRadius = CornerRadius(24.dp.toPx()), size = Size(width - 4.dp.toPx(), height - 4.dp.toPx()), topLeft = Offset(2.dp.toPx(), 2.dp.toPx()), style = Stroke(width = 1.dp.toPx()))
            }
    ) { content() }
}

@Composable
private fun DeviceIdCapsule(deviceId: String, context: Context) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .drawBehind {
                drawRoundRect(brush = Brush.horizontalGradient(listOf(Color(0x18FFFFFF), Color(0x10FFFFFF), Color(0x18FFFFFF))), cornerRadius = CornerRadius(24.dp.toPx()))
                drawRoundRect(color = GlassColors.GlassBorder, cornerRadius = CornerRadius(24.dp.toPx()), style = Stroke(width = 1.dp.toPx()))
            }
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = "Device ID", color = GlassColors.ElectricBlue.copy(alpha = 0.7f), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = deviceId, color = GlassColors.GlassWhite.copy(alpha = 0.9f), fontSize = 14.sp, fontWeight = FontWeight.Medium, fontFamily = FontFamily.Monospace)
            Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Copy", tint = GlassColors.ElectricBlue, modifier = Modifier.size(18.dp).clickable {
                clipboard.setPrimaryClip(ClipData.newPlainText("Device ID", deviceId))
                Toast.makeText(context, "Copied!", Toast.LENGTH_SHORT).show()
            })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NeonTextField(value: String, onValueChange: (String) -> Unit, isFocused: Boolean, onFocusChange: (Boolean) -> Unit, glowPhase: Float) {
    val focusRequester = remember { FocusRequester() }
    val neonIntensity = if (isFocused) (sin((glowPhase * 3f).toDouble()).toFloat() * 0.3f + 0.7f) else 0.3f

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .drawBehind {
                val width = size.width
                val height = size.height
                drawRoundRect(color = Color(0x10FFFFFF), cornerRadius = CornerRadius(16.dp.toPx()))

                if (isFocused) {
                    drawRoundRect(
                        brush = Brush.linearGradient(
                            colors = listOf(GlassColors.ElectricBlue.copy(alpha = neonIntensity), GlassColors.VividPurple.copy(alpha = neonIntensity), GlassColors.ElectricBlue.copy(alpha = neonIntensity))
                        ),
                        cornerRadius = CornerRadius(16.dp.toPx()), style = Stroke(width = 2.dp.toPx())
                    )
                } else {
                    drawRoundRect(color = GlassColors.GlassBorder, cornerRadius = CornerRadius(16.dp.toPx()), style = Stroke(width = 1.dp.toPx()))
                }
            }
    ) {
        OutlinedTextField(
            value = value, onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth().focusRequester(focusRequester).onFocusChanged { onFocusChange(it.isFocused) },
            placeholder = { Text("Enter Activation Code", color = GlassColors.GlassWhite.copy(alpha = 0.4f)) },
            colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent, focusedBorderColor = Color.Transparent, unfocusedBorderColor = Color.Transparent, focusedTextColor = GlassColors.GlassWhite, unfocusedTextColor = GlassColors.GlassWhite),
            textStyle = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 4.sp, textAlign = TextAlign.Center),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text), singleLine = true
        )
    }
}

@Composable
private fun GlowingGradientButton(text: String, onClick: () -> Unit, glowPhase: Float) {
    val glowIntensity = sin((glowPhase * 2f).toDouble()).toFloat() * 0.2f + 0.8f
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .drawBehind {
                val width = size.width
                val height = size.height

                drawRoundRect(
                    brush = Brush.radialGradient(
                        colors = listOf(GlassColors.ElectricBlue.copy(alpha = 0.3f * glowIntensity), GlassColors.VividPurple.copy(alpha = 0.2f * glowIntensity), Color.Transparent)
                    ),
                    cornerRadius = CornerRadius(20.dp.toPx()),
                    size = Size(width + 20.dp.toPx(), height + 20.dp.toPx()), topLeft = Offset(-10.dp.toPx(), -10.dp.toPx())
                )

                drawRoundRect(brush = Brush.linearGradient(colors = listOf(GlassColors.ElectricBlue.copy(alpha = 0.9f), GlassColors.VividPurple.copy(alpha = 0.9f), GlassColors.ElectricBlue.copy(alpha = 0.9f))), cornerRadius = CornerRadius(16.dp.toPx()))
                drawRect(brush = Brush.verticalGradient(colors = listOf(Color(0x40FFFFFF), Color.Transparent), startY = 0f, endY = height * 0.5f), size = Size(width, height * 0.5f))
            }
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, color = GlassColors.GlassWhite, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 2.sp)
    }
}

@Composable
private fun WhatsAppOutlineButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .drawBehind {
                drawRoundRect(brush = Brush.linearGradient(colors = listOf(GlassColors.ElectricBlue.copy(alpha = 0.5f), GlassColors.VividPurple.copy(alpha = 0.5f))), cornerRadius = CornerRadius(24.dp.toPx()), style = Stroke(width = 1.5f.dp.toPx()))
                drawRoundRect(color = Color(0x08FFFFFF), cornerRadius = CornerCornerRadius(24.dp.toPx()))
            }
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(imageVector = Icons.Default.Info, contentDescription = "Info", tint = GlassColors.NeonCyan.copy(alpha = 0.9f), modifier = Modifier.size(20.dp))
            Text(text = "Contact via WhatsApp", color = GlassColors.NeonCyan.copy(alpha = 0.9f), fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorScreen(
    onNavigateToAbout: () -> Unit,
    onNavigateToCalculate: (MedicalDrug, DrugProtocol) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedDrug by remember { mutableStateOf<MedicalDrug?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showBottomSheet by remember { mutableStateOf(false) }

    val sampleDrugs = remember {
        listOf(
            MedicalDrug(
                id = "1",
                name = "Dopamine",
                classification = "Inotrope / Vasopressor",
                isContinuous = true,
                protocols = listOf(
                    DrugProtocol(AgeCategory.NEONATE, listOf("Neonatal Shock", "Low cardiac output support"), 5.0, 2.0, 20.0, "mcg/kg/min"),
                    DrugProtocol(AgeCategory.CHILD, listOf("Fluid-refractory septic shock", "Post-cardiac surgery hypotension"), 7.5, 5.0, 20.0, "mcg/kg/min")
                )
            ),
            MedicalDrug(
                id = "2",
                name = "Amiodarone",
                classification = "Antiarrhythmic (Class III)",
                isContinuous = false,
                protocols = listOf(
                    DrugProtocol(AgeCategory.CHILD, listOf("Pulseless VT/VF defibrillation bolus", "Refractory JET/SVT loading infusion"), 5.0, 5.0, 5.0, "mg/kg")
                )
            ),
            MedicalDrug(
                id = "3",
                name = "Epinephrine",
                classification = "Inotrope / Vasopressor / Anaphylaxis",
                isContinuous = true,
                protocols = listOf(
                    DrugProtocol(AgeCategory.NEONATE, listOf("Post-resuscitation stabilization", "Severe bradycardia"), 0.1, 0.05, 1.0, "mcg/kg/min"),
                    DrugProtocol(AgeCategory.CHILD, listOf("Cardiac arrest epinephrine bolus", "Profound hypotension and shock"), 0.1, 0.1, 1.5, "mcg/kg/min")
                )
            )
        )
    }

    val filteredDrugs = sampleDrugs.filter {
        it.name.contains(searchQuery, ignoreCase = true) || it.classification.contains(searchQuery, ignoreCase = true)
    }

    var visibleItemCount by remember { mutableStateOf(0) }
    LaunchedEffect(filteredDrugs) {
        visibleItemCount = 0
        filteredDrugs.forEachIndexed { index, _ ->
            delay(60)
            visibleItemCount = index + 1
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(GlassColors.MidnightBlue).padding(16.dp)) {
        Column(modifier = Modifier.fillMaxSize()) {
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White.copy(alpha = 0.04f))
                    .border(BorderStroke(1.2.dp, Brush.horizontalGradient(listOf(GlassColors.ElectricBlue, GlassColors.VividPurple))), RoundedCornerShape(20.dp))
                    .clickable { onNavigateToAbout() }
                    .padding(14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Filled.Info, contentDescription = "Info", tint = GlassColors.ElectricBlue, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(text = "About PICU Pro", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            Text(text = "Click to view dedication & medical disclaimer", color = Color.Gray, fontSize = 11.sp)
                        }
                    }
                    Icon(imageVector = Icons.Filled.Favorite, contentDescription = "Heart", tint = GlassColors.VividPurple, modifier = Modifier.size(20.dp))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search Drug or Classification...", color = Color.Gray, fontSize = 14.sp) },
                leadingIcon = { Icon(imageVector = Icons.Filled.Search, contentDescription = "Search", tint = GlassColors.ElectricBlue) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White.copy(alpha = 0.03f),
                    unfocusedContainerColor = Color.White.copy(alpha = 0.03f),
                    focusedBorderColor = GlassColors.ElectricBlue,
                    unfocusedBorderColor = GlassColors.GlassBorder,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                shape = RoundedCornerShape(16.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                itemsIndexed(filteredDrugs) { index, drug ->
                    val isVisible = index < visibleItemCount
                    
                    AnimatedVisibility(
                        visible = isVisible,
                        enter = fadeIn(animationSpec = tween(300)) + slideInVertically(animationSpec = tween(300), initialOffsetY = { 50 }),
                        exit = fadeOut(animationSpec = tween(150))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color.White.copy(alpha = 0.05f))
                                .border(BorderStroke(1.dp, GlassColors.GlassBorder), RoundedCornerShape(20.dp))
                                .clickable {
                                    selectedDrug = drug
                                    showBottomSheet = true
                                }
                                .padding(16.dp)
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = drug.name, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
                                    
                                    Text(
                                        text = if (drug.isContinuous) "Continuous" else "Bolus",
                                        color = if (drug.isContinuous) GlassColors.NeonCyan else GlassColors.VividPurple,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (drug.isContinuous) GlassColors.ElectricBlue.copy(alpha = 0.1f) else GlassColors.VividPurple.copy(alpha = 0.1f))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }

                                Text(text = drug.classification, color = Color.Gray, fontSize = 13.sp, modifier = Modifier.padding(top = 2.dp))

                                Spacer(modifier = Modifier.height(12.dp))

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    drug.protocols.forEach { protocol ->
                                        val isNeonate = protocol.category == AgeCategory.NEONATE
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(if (isNeonate) Color(0xFFB829DD).copy(alpha = 0.15f) else Color(0xFF00D4FF).copy(alpha = 0.15f))
                                                .border(BorderStroke(1.dp, if (isNeonate) Color(0xFFB829DD) else Color(0xFF00D4FF)), RoundedCornerShape(12.dp))
                                                .padding(horizontal = 10.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = if (isNeonate) "🟣 Neonate" else "🔵 Child",
                                                color = if (isNeonate) Color(0xFFE879F9) else Color(0xFF38BDF8),
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showBottomSheet && selectedDrug != null) {
            ModalBottomSheet(
                onDismissRequest = { showBottomSheet = false },
                sheetState = sheetState,
                containerColor = GlassColors.MidnightBlue,
                scrimColor = Color.Black.copy(alpha = 0.7f),
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
            ) {
                val drug = selectedDrug!!
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 8.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(text = drug.name, color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Black)
                    Text(text = drug.classification, color = GlassColors.ElectricBlue, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    
                    HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = GlassColors.GlassBorder)

                    drug.protocols.forEach { protocol ->
                        val isNeonate = protocol.category == AgeCategory.NEONATE
                        val sectionColor = if (isNeonate) Color(0xFFB829DD) else Color(0xFF00D4FF)

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .background(Color.White.copy(alpha = 0.03f))
                                .border(BorderStroke(1.2.dp, sectionColor.copy(alpha = 0.6f)), RoundedCornerShape(18.dp))
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (isNeonate) "🟣 Neonatal Protocol" else "🔵 Pediatric Protocol",
                                    color = if (isNeonate) Color(0xFFE879F9) else Color(0xFF38BDF8),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                
                                Button(
                                    onClick = {
                                        showBottomSheet = false
                                        onNavigateToCalculate(drug, protocol)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = sectionColor),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("OPEN CALC", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            Text(text = "Clinical Indications:", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            
                            protocol.indications.forEach { indication ->
                                Text(text = "• $indication", color = Color.LightGray, fontSize = 13.sp, modifier = Modifier.padding(start = 8.dp, top = 2.dp))
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = "Min Dose", color = Color.Gray, fontSize = 11.sp)
                                    Text(text = "${protocol.minDose} ${protocol.doseUnit}", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = "Default Start", color = sectionColor, fontSize = 11.sp)
                                    Text(text = "${protocol.defaultDose} ${protocol.doseUnit}", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = "Max Safe Dose", color = Color(0xFFEF4444), fontSize = 11.sp)
                                    Text(text = "${protocol.maxDose} ${protocol.doseUnit}", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFEAB308).copy(alpha = 0.1f))
                            .border(BorderStroke(1.dp, Color(0xFFEAB308).copy(alpha = 0.4f)), RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Filled.Info, contentDescription = "Alert", tint = Color(0xFFEAB308), modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "CRITICAL SAFETY: Always cross-verify calculated doses with your specific unit protocols before clinical administration.",
                            color = Color(0xFFFEF08A),
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(30.dp))
                }
            }
        }
    }
}

// ----------------------------------------------------------------------------------
// شاشة لوحة الحساب السريرية الحقيقية (Calculation Panel) المتصلة بالـ MedicalEngine
// ----------------------------------------------------------------------------------
@Composable
fun CalculationPanelScreen(
    drug: MedicalDrug,
    protocol: DrugProtocol,
    onBack: () -> Unit
) {
    var weightInput by remember { mutableStateOf("") }
    var doseInput by remember { mutableStateOf(protocol.defaultDose.toString()) }
    var drugAmountInput by remember { mutableStateOf("250") } // قيمة افتراضية للتحضير mg
    var totalVolumeInput by remember { mutableStateOf("50") }   // حجم المحلول الوريدي افتراضي mL
    
    var calculationResult by remember { mutableStateOf<CalculationResult?>(null) }
    val medicalEngine = remember { MedicalEngine() }

    Box(modifier = Modifier.fillMaxSize().background(GlassColors.MidnightBlue).padding(16.dp)) {
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { onBack() }.padding(4.dp)) {
                Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "Back", tint = GlassColors.ElectricBlue)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Back to List", color = GlassColors.ElectricBlue, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(text = drug.name, color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Black)
            Text(
                text = if (protocol.category == AgeCategory.NEONATE) "🟣 NEONATAL CALCULATOR" else "🔵 PEDIATRIC CALCULATOR",
                color = if (protocol.category == AgeCategory.NEONATE) Color(0xFFE879F9) else Color(0xFF38BDF8),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(20.dp))

            // كرت المدخلات السريرية الحركية
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White.copy(alpha = 0.04f))
                    .border(BorderStroke(1.dp, GlassColors.GlassBorder), RoundedCornerShape(20.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // الوزن
                OutlinedTextField(
                    value = weightInput,
                    onValueChange = { weightInput = it },
                    label = { Text("Patient Weight (kg)", color = Color.Gray) },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GlassColors.ElectricBlue, unfocusedBorderColor = GlassColors.GlassBorder, focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true, modifier = Modifier.fillMaxWidth()
                )

                // الجرعة المطلوبة
                OutlinedTextField(
                    value = doseInput,
                    onValueChange = { doseInput = it },
                    label = { Text("Desired Dose (${protocol.doseUnit})", color = Color.Gray) },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GlassColors.ElectricBlue, unfocusedBorderColor = GlassColors.GlassBorder, focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true, modifier = Modifier.fillMaxWidth()
                )

                HorizontalDivider(color = GlassColors.GlassBorder)
                Text("Preparation / Dilution Settings:", color = Color.Gray, fontSize = 13.sp, fontWeight = FontWeight.Bold)

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = drugAmountInput,
                        onValueChange = { drugAmountInput = it },
                        label = { Text("Total Drug (mg)", color = Color.Gray) },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GlassColors.ElectricBlue, unfocusedBorderColor = GlassColors.GlassBorder, focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true, modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = totalVolumeInput,
                        onValueChange = { totalVolumeInput = it },
                        label = { Text("Total Fluid (mL)", color = Color.Gray) },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GlassColors.ElectricBlue, unfocusedBorderColor = GlassColors.GlassBorder, focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true, modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // زر الحساب الكبير النابض
            Button(
                onClick = {
                    val w = weightInput.toDoubleOrNull() ?: 0.0
                    val d = doseInput.toDoubleOrNull() ?: 0.0
                    val amt = drugAmountInput.toDoubleOrNull() ?: 0.0
                    val vol = totalVolumeInput.toDoubleOrNull() ?: 0.0

                    if (w <= 0.0 || d <= 0.0 || amt <= 0.0 || vol <= 0.0) {
                        calculationResult = CalculationResult(null, null, null, emptyList(), "Please enter valid positive clinical parameters.", "")
                    } else {
                        // استدعاء الحساب الفعلي الحقيقي من المحرك الأصيل
                        calculationResult = medicalEngine.calculate(
                            dose = d,
                            weight = w,
                            unit = protocol.doseUnit,
                            totalDrugAmount = amt,
                            totalVolume = vol,
                            totalDrugUnit = "mg",
                            maxDose = protocol.maxDose,
                            isContinuous = drug.isContinuous
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth().height(54.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GlassColors.ElectricBlue),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("RUN CLINICAL CALCULATION", color = GlassColors.MidnightBlue, fontSize = 15.sp, fontWeight = FontWeight.Black)
            }

            Spacer(modifier = Modifier.height(20.dp))

            // عرض النتائج والتحذيرات الصارمة من المحرك الطبي
            calculationResult?.let { res ->
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    if (res.error != null) {
                        Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(GlassColors.DangerRed.copy(alpha = 0.1f)).border(BorderStroke(1.2.dp, GlassColors.DangerRed), RoundedCornerShape(14.dp)).padding(16.dp)) {
                            Text(text = res.error, color = GlassColors.DangerRed, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        // كرت النتيجة الرقمية الدقيقة
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color.White.copy(alpha = 0.06f))
                                .border(BorderStroke(1.5.dp, GlassColors.NeonCyan), RoundedCornerShape(20.dp))
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = res.displayMode, color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            Text(text = "${res.roundedResult} mL/hr", color = GlassColors.NeonCyan, fontSize = 38.sp, fontWeight = FontWeight.Black)
                            Text(text = "Exact output: ${res.rawResult}", color = Color.DarkGray, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                        }

                        // صمامات الأمان والتحذيرات الحرجة (Max Dose Exceeded)
                        if (res.warnings.isNotEmpty()) {
                            res.warnings.forEach { warning ->
                                Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(GlassColors.DangerRed.copy(alpha = 0.15f)).border(BorderStroke(1.5.dp, GlassColors.DangerRed), RoundedCornerShape(14.dp)).padding(16.dp)) {
                                    Text(text = warning, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold)
                                }
                            }
                        } else {
                            Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(Color(0xFF10B981).copy(alpha = 0.1f)).border(BorderStroke(1.2.dp, Color(0xFF10B981)), RoundedCornerShape(14.dp)).padding(14.dp)) {
                                Text(text = "✅ Dose is within secure therapeutic boundaries.", color = Color(0xFF34D399), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun AboutScreen(onBack: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize().background(GlassColors.MidnightBlue).padding(20.dp)) {
        Column(modifier = Modifier.fillMaxSize()) {
            Spacer(modifier = Modifier.height(24.dp))
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { onBack() }.padding(8.dp)) {
                Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "Back", tint = GlassColors.ElectricBlue)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Back to Main", color = GlassColors.ElectricBlue, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }

            Spacer(modifier = Modifier.height(20.dp))

            Column(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(Color.White.copy(alpha = 0.05f)).border(BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)), RoundedCornerShape(24.dp)).padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "لا تنسوني ووالدي من صالح دعائكم", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = Color.White, textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = "Ahmad Qudah", fontSize = 18.sp, color = GlassColors.VividPurple, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = "This application is dedicated to cardiac intensive care units for post-operative congenital heart disease in children.", fontSize = 14.sp, color = Color.LightGray, textAlign = TextAlign.Center, lineHeight = 20.sp)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // كرت اخلاء المسؤولية العربي الحرج المطلوب بالخط الأحمر الصارم والتحذير الفوري
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(GlassColors.DangerRed.copy(alpha = 0.08f))
                    .border(BorderStroke(2.dp, GlassColors.DangerRed), RoundedCornerShape(24.dp))
                    .padding(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Filled.Info, contentDescription = "Warning", tint = GlassColors.DangerRed, modifier = Modifier.size(28.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "تحذير طبي حرج",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = GlassColors.DangerRed
                    )
                }
                
                Text(
                    text = "لا يتحمل مطور البرنامج أحمد القضاه أي مسؤولية اذا تم الاعتماد على حساب الادوية من التطبيق وحده , يجب استشارة الطبيب المسؤول قبل اعتماد أي جرعة , هذا التطبيق للمساعدة فقط وليس لإتخاذ أي قرار طبي.",
                    fontSize = 15.sp,
                    color = GlassColors.DangerRed,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Right,
                    lineHeight = 24.sp,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

fun verifyActivationCode(deviceId: String, inputCode: String): Boolean {
    if (inputCode.isBlank()) return false
    val rawString = deviceId + "PICU2026AhmedQudah"
    val digest = MessageDigest.getInstance("SHA-256")
    val hashBytes = digest.digest(rawString.toByteArray(Charsets.UTF_8))
    val fullHash = hashBytes.joinToString("") { "%02x".format(it) }
    val expectedCode = fullHash.take(14).uppercase()
    return inputCode.uppercase().trim() == expectedCode
}