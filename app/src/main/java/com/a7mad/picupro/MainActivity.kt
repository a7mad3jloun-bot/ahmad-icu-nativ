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
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
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
import java.security.MessageDigest
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

// الألوان المخصصة للتصميم الزجاجي النيوني
object GlassColors {
    val MidnightBlue = Color(0xFF050A1F)
    val ElectricBlue = Color(0xFF00D4FF)
    val VividPurple = Color(0xFFB829DD)
    val NeonCyan = Color(0xFF00F0FF)
    val GlassWhite = Color(0xFFFFFFFF)
    val GlassBorder = Color(0x33FFFFFF)
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

    Box(modifier = Modifier.fillMaxSize()) {
        when (currentScreen) {
            "activation" -> ActivationScreenVisual(
                onActivationSuccess = { deviceId ->
                    confirmedDeviceId = deviceId
                    currentScreen = "calculator"
                }
            )
            "calculator" -> CalculatorScreen(
                onNavigateToAbout = { currentScreen = "about" }
            )
            "about" -> AboutScreen(
                onBack = { currentScreen = "calculator" }
            )
        }
    }
}

// شاشة التفعيل المطورة بصرياً من كيمي والمربوطة برمجياً بنظامنا
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
        val centerX = size.width / 2
        val centerY = size.height / 3

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(GlassColors.ElectricBlue.copy(alpha = 0.15f), GlassColors.ElectricBlue.copy(alpha = 0.05f), Color.Transparent),
                center = Offset(centerX + cos(glowPhase) * 50f, centerY + sin(glowPhase * 0.7f) * 30f),
                radius = size.width * 0.6f
            ),
            radius = size.width * 0.6f,
            center = Offset(centerX + cos(glowPhase) * 50f, centerY + sin(glowPhase * 0.7f) * 30f)
        )

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(GlassColors.VividPurple.copy(alpha = 0.12f), GlassColors.VividPurple.copy(alpha = 0.04f), Color.Transparent),
                center = Offset(centerX - cos(glowPhase * 0.8f) * 60f, centerY + sin(glowPhase) * 40f + 200f),
                radius = size.width * 0.5f
            ),
            radius = size.width * 0.5f,
            center = Offset(centerX - cos(glowPhase * 0.8f) * 60f, centerY + sin(glowPhase) * 40f + 200f)
        )

        for (i in 0 until 60) {
            val x = (i * 137.5f) % size.width
            val y = (i * 71.3f) % size.height
            val twinkle = sin(glowPhase * 2f + i) * 0.5f + 0.5f
            drawCircle(color = GlassColors.GlassWhite.copy(alpha = 0.3f * twinkle), radius = 1.5f, center = Offset(x, y))
        }
    }
}

@Composable
private fun GlowingMedicalIcon(scale: Float, glowPhase: Float) {
    val glowIntensity = (sin(glowPhase) * 0.3f + 0.7f)
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
    val borderGlow = (sin(glowPhase * 1.5f) * 0.3f + 0.7f)
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
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(24.dp.toPx()),
                    size = Size(width, height)
                )

                drawRect(
                    brush = Brush.linearGradient(
                        colors = listOf(Color(0x40FFFFFF), Color(0x10FFFFFF), Color.Transparent),
                        start = Offset(0f, 0f), end = Offset(width * 0.5f, height * 0.4f)
                    ),
                    size = Size(width * 0.6f, height * 0.5f)
                )

                drawRoundRect(color = GlassColors.ElectricBlue.copy(alpha = 0.6f * borderGlow), cornerRadius = androidx.compose.ui.geometry.CornerRadius(24.dp.toPx()), size = Size(width, height), style = Stroke(width = 1.5.dp.toPx()))
                drawRoundRect(color = GlassColors.VividPurple.copy(alpha = 0.3f * borderGlow), cornerRadius = androidx.compose.ui.geometry.CornerRadius(24.dp.toPx()), size = Size(width - 4.dp.toPx(), height - 4.dp.toPx()), topLeft = Offset(2.dp.toPx(), 2.dp.toPx()), style = Stroke(width = 1.dp.toPx()))
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
                drawRoundRect(brush = Brush.horizontalGradient(listOf(Color(0x18FFFFFF), Color(0x10FFFFFF), Color(0x18FFFFFF))), cornerRadius = androidx.compose.ui.geometry.CornerRadius(24.dp.toPx()))
                drawRoundRect(color = GlassColors.GlassBorder, cornerRadius = androidx.compose.ui.geometry.CornerRadius(24.dp.toPx()), style = Stroke(width = 1.dp.toPx()))
            }
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = "Device ID", color = GlassColors.ElectricBlue.copy(alpha = 0.7f), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = deviceId, color = GlassColors.GlassWhite.copy(alpha = 0.9f), fontSize = 14.sp, fontWeight = FontWeight.Medium, fontFamily = FontFamily.Monospace)
            Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Copy", tint = GlassColors.ElectricBlue, modifier = Modifier.size(18.dp).clickable {
                clipboard.setPrimaryClip(android.content.ClipData.newPlainText("Device ID", deviceId))
                Toast.makeText(context, "Copied!", Toast.LENGTH_SHORT).show()
            })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NeonTextField(value: String, onValueChange: (String) -> Unit, isFocused: Boolean, onFocusChange: (Boolean) -> Unit, glowPhase: Float) {
    val focusRequester = remember { FocusRequester() }
    val neonIntensity = if (isFocused) (sin(glowPhase * 3f) * 0.3f + 0.7f) else 0.3f

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .drawBehind {
                val width = size.width
                val height = size.height
                drawRoundRect(color = Color(0x10FFFFFF), cornerRadius = androidx.compose.ui.geometry.CornerRadius(16.dp.toPx()))

                if (isFocused) {
                    drawRoundRect(
                        brush = Brush.linearGradient(
                            colors = listOf(GlassColors.ElectricBlue.copy(alpha = neonIntensity), GlassColors.VividPurple.copy(alpha = neonIntensity), GlassColors.ElectricBlue.copy(alpha = neonIntensity))
                        ),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(16.dp.toPx()), style = Stroke(width = 2.dp.toPx())
                    )
                } else {
                    drawRoundRect(color = GlassColors.GlassBorder, cornerRadius = androidx.compose.ui.geometry.CornerRadius(16.dp.toPx()), style = Stroke(width = 1.dp.toPx()))
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
    val glowIntensity = (sin(glowPhase * 2f) * 0.2f + 0.8f)
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
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(20.dp.toPx()),
                    size = Size(width + 20.dp.toPx(), height + 20.dp.toPx()), topLeft = Offset(-10.dp.toPx(), -10.dp.toPx())
                )

                drawRoundRect(brush = Brush.linearGradient(colors = listOf(GlassColors.ElectricBlue.copy(alpha = 0.9f), GlassColors.VividPurple.copy(alpha = 0.9f), GlassColors.ElectricBlue.copy(alpha = 0.9f))), cornerRadius = androidx.compose.ui.geometry.CornerRadius(16.dp.toPx()))
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
                drawRoundRect(brush = Brush.linearGradient(colors = listOf(GlassColors.ElectricBlue.copy(alpha = 0.5f), GlassColors.VividPurple.copy(alpha = 0.5f))), cornerRadius = androidx.compose.ui.geometry.CornerRadius(24.dp.toPx()), style = Stroke(width = 1.5f.dp.toPx()))
                drawRoundRect(color = Color(0x08FFFFFF), cornerRadius = androidx.compose.ui.geometry.CornerRadius(24.dp.toPx()))
            }
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(imageVector = Icons.Default.Info, contentDescription = "WhatsApp", tint = GlassColors.NeonCyan.copy(alpha = 0.9f), modifier = Modifier.size(20.dp))
            Text(text = "Contact via WhatsApp", color = GlassColors.NeonCyan.copy(alpha = 0.9f), fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun CalculatorScreen(onNavigateToAbout: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize().background(GlassColors.MidnightBlue).padding(16.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color.White.copy(alpha = 0.05f))
                .border(BorderStroke(1.5.dp, Brush.horizontalGradient(listOf(GlassColors.ElectricBlue, GlassColors.VividPurple))), RoundedCornerShape(20.dp))
                .clickable { onNavigateToAbout() }
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Filled.Info, contentDescription = "Info", tint = GlassColors.ElectricBlue, modifier = Modifier.size(28.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(text = "About PICU Pro", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text(text = "Click to view dedication & disclaimer", color = Color.LightGray, fontSize = 12.sp)
                    }
                }
                Icon(imageVector = Icons.Filled.Favorite, contentDescription = "Heart", tint = GlassColors.VividPurple, modifier = Modifier.size(24.dp))
            }
        }
        Text(text = "PICU Calculator Content\n(Will be active here)", color = Color.Gray, fontSize = 18.sp, textAlign = TextAlign.Center, modifier = Modifier.align(Alignment.Center))
    }
}

@Composable
fun AboutScreen(onBack: () -> Unit) {
    val textSilver = Color(0xFFE2E8F0)
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
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(Color.White.copy(alpha = 0.05f)).border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(24.dp)).padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "لا تنسوني ووالدي من صالح دعائكم", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = Color.White, textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = "Ahmad Qudah", fontSize = 18.sp, color = GlassColors.VividPurple, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = "This application is dedicated to cardiac intensive care units for post-operative congenital heart disease in children.", fontSize = 14.sp, color = Color.LightGray, textAlign = TextAlign.Center, lineHeight = 20.sp)
            }

            Spacer(modifier = Modifier.height(24.dp))

            Column(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(Color(0xFFEF4444).copy(alpha = 0.08f)).border(1.5.dp, Color(0xFFEF4444).copy(alpha = 0.4f), RoundedCornerShape(24.dp)).padding(24.dp)) {
                Text(text = "MEDICAL DISCLAIMER", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFFEF4444), modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(10.dp))
                Text(text = "This application is a medical calculation tool intended solely for educational and cognitive support for specialized healthcare professionals. It DOES NOT substitute professional medical advice, diagnosis, or treatment. You must obtain official medical approval and verify all doses from authorized clinical protocols before applying any information or calculations derived from this application in practice.", fontSize = 13.sp, color = Color(0xFFFCA5A5), textAlign = TextAlign.Justify, lineHeight = 18.sp)
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