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
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.security.MessageDigest

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

// نظام التنقل الرئيسي بين الشاشات الثلاث
@Composable
fun MainNavigationScreen() {
    var currentScreen by remember { mutableStateOf("activation") }
    var confirmedDeviceId by remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize()) {
        when (currentScreen) {
            "activation" -> ActivationScreen(
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

// 1. شاشة التفعيل المبهرة بالتأثير الزجاجي المطور
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivationScreen(onActivationSuccess: (String) -> Unit) {
    val context = LocalContext.current
    val deviceId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID) ?: "UNKNOWN"
    var activationCode by remember { mutableStateOf("") }

    val midnightBlue = Color(0xFF0B0F19)
    val deepBlue = Color(0xFF111827)
    val electricBlue = Color(0xFF00E5FF)
    val vividPurple = Color(0xFFB000FF)
    
    val glassColor = Color(0xFFFFFFFF).copy(alpha = 0.07f)
    val glassBorder = Color.White.copy(alpha = 0.25f)
    val textSilver = Color(0xFFE2E8F0)

    Box(modifier = Modifier.fillMaxSize().background(midnightBlue)) {
        // كرات التوهج الخلفية لخلق تأثير زجاجي ثلاثي الأبعاد
        Box(modifier = Modifier.offset(x = (-50).dp, y = 80.dp).size(230.dp).background(vividPurple.copy(alpha = 0.5f), CircleShape).blur(80.dp))
        Box(modifier = Modifier.align(Alignment.BottomEnd).offset(x = 50.dp, y = (-100).dp).size(260.dp).background(electricBlue.copy(alpha = 0.5f), CircleShape).blur(80.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .align(Alignment.Center)
                .clip(RoundedCornerShape(32.dp))
                .background(glassColor)
                .border(BorderStroke(1.5.dp, Brush.linearGradient(listOf(electricBlue.copy(alpha = 0.6f), vividPurple.copy(alpha = 0.6f)))), RoundedCornerShape(32.dp))
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier.size(90.dp).clip(CircleShape).background(Brush.linearGradient(listOf(electricBlue, vividPurple))),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = Icons.Filled.Favorite, contentDescription = "Heart", tint = Color.White, modifier = Modifier.size(48.dp))
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(text = "Ahmad Qudah", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
            Text(text = "Medications Calculations", color = electricBlue, fontSize = 17.sp, fontWeight = FontWeight.Medium)

            Spacer(modifier = Modifier.height(35.dp))

            Text(text = "Device ID:", color = textSilver, fontSize = 13.sp, modifier = Modifier.align(Alignment.Start))
            
            Row(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Color.White.copy(alpha = 0.05f)).padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = deviceId, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                IconButton(onClick = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText("Device ID", deviceId))
                    Toast.makeText(context, "Device ID Copied!", Toast.LENGTH_SHORT).show()
                }) {
                    Icon(imageVector = Icons.Filled.ContentCopy, contentDescription = "Copy", tint = electricBlue)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            OutlinedTextField(
                value = activationCode,
                onValueChange = { activationCode = it },
                label = { Text("Enter Activation Code", color = textSilver) },
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = electricBlue, unfocusedBorderColor = glassBorder,
                    focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                    cursorColor = electricBlue, containerColor = Color.Transparent
                ),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(30.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Brush.horizontalGradient(listOf(electricBlue, vividPurple)))
                    .clickable {
                        if (verifyActivationCode(deviceId, activationCode)) {
                            Toast.makeText(context, "Activated Successfully!", Toast.LENGTH_LONG).show()
                            onActivationSuccess(deviceId)
                        } else {
                            Toast.makeText(context, "Invalid Code!", Toast.LENGTH_SHORT).show()
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Text("VERIFY & ACTIVATE", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = {
                    val phoneNumber = "+962782088812"
                    val message = "Hello, I need an activation code for PICUCalculator.\nMy Device ID is: $deviceId"
                    val url = "https://api.whatsapp.com/send?phone=$phoneNumber&text=${Uri.encode(message)}"
                    try { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) } catch (e: Exception) {
                        Toast.makeText(context, "WhatsApp is not installed.", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(55.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = electricBlue),
                border = BorderStroke(1.5.dp, electricBlue),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Get Code via WhatsApp", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

// 2. الشاشة الرئيسية للتطبيق (مكان الة الحاسبة) وبها كرت الـ About الجذاب والعائم
@Composable
fun CalculatorScreen(onNavigateToAbout: () -> Unit) {
    val midnightBlue = Color(0xFF0B0F19)
    val electricBlue = Color(0xFF00E5FF)
    val vividPurple = Color(0xFFB000FF)

    Box(modifier = Modifier.fillMaxSize().background(midnightBlue).padding(16.dp)) {
        // بطاقة زجاجية نيونية عائمة لجذب المستخدم للضغط على About
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color.White.copy(alpha = 0.05f))
                .border(BorderStroke(1.5.dp, Brush.horizontalGradient(listOf(electricBlue, vividPurple))), RoundedCornerShape(20.dp))
                .clickable { onNavigateToAbout() }
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Filled.Info, contentDescription = "Info", tint = electricBlue, modifier = Modifier.size(28.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(text = "About PICU Pro", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text(text = "Click to view dedication & disclaimer", color = Color.LightGray, fontSize = 12.sp)
                    }
                }
                Icon(imageVector = Icons.Filled.Favorite, contentDescription = "Heart", tint = vividPurple, modifier = Modifier.size(24.dp))
            }
        }

        // محتوى الحاسبة المستقبلي يوضع هنا
        Text(
            text = "PICU Calculator Content\n(Will be active here)",
            color = Color.Gray,
            fontSize = 18.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

// 3. شاشة "حول التطبيق" المنفصلة كلياً بتصميم عصري وإخلاء المسؤولية الإنجليزي
@Composable
fun AboutScreen(onBack: () -> Unit) {
    val midnightBlue = Color(0xFF0B0F19)
    val electricBlue = Color(0xFF00E5FF)
    val vividPurple = Color(0xFFB000FF)
    val glassColor = Color(0xFFFFFFFF).copy(alpha = 0.05f)

    Box(modifier = Modifier.fillMaxSize().background(midnightBlue).padding(20.dp)) {
        // كرات توهج لتجميل شاشة About
        Box(modifier = Modifier.offset(x = 180.dp, y = 40.dp).size(200.dp).background(electricBlue.copy(alpha = 0.35f), CircleShape).blur(70.dp))

        Column(modifier = Modifier.fillMaxSize()) {
            Spacer(modifier = Modifier.height(24.dp))
            // زر العودة الخلفي الأنيق
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { onBack() }.padding(8.dp)
            ) {
                Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "Back", tint = electricBlue)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Back to Main", color = electricBlue, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }

            Spacer(modifier = Modifier.height(20.dp))

            // بطاقة الدعاء والتوقيع
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(glassColor)
                    .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(24.dp))
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "لا تنسوني ووالدي من صالح دعائكم",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Ahmad Qudah",
                    fontSize = 18.sp,
                    color = vividPurple,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "This application is dedicated to cardiac intensive care units for post-operative congenital heart disease in children.",
                    fontSize = 14.sp,
                    color = Color.LightGray,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // بطاقة إخلاء المسؤولية الصارمة (Disclaimer) باللغة الإنجليزية
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFFEF4444).copy(alpha = 0.08f)) // خلفية حمراء تحذيرية خفيفة جداً
                    .border(1.5.dp, Color(0xFFEF4444).copy(alpha = 0.4f), RoundedCornerShape(24.dp))
                    .padding(24.dp)
            ) {
                Text(
                    text = "MEDICAL DISCLAIMER",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFEF4444),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "This application is a medical calculation tool intended solely for educational and cognitive support for specialized healthcare professionals. It DOES NOT substitute professional medical advice, diagnosis, or treatment. You must obtain official medical approval and verify all doses from authorized clinical protocols before applying any information or calculations derived from this application in practice.",
                    fontSize = 13.sp,
                    color = Color(0xFFFCA5A5),
                    textAlign = TextAlign.Justify,
                    lineHeight = 18.sp
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