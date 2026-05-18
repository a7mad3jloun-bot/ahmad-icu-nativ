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
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.security.MessageDigest

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = darkColorScheme()) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ActivationScreen()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivationScreen() {
    val context = LocalContext.current
    val deviceId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID) ?: "UNKNOWN"
    var activationCode by remember { mutableStateOf("") }

    // الألوان الجديدة (Medical Cyber Glow)
    val midnightBlue = Color(0xFF0B0F19)
    val deepBlue = Color(0xFF111827)
    val electricBlue = Color(0xFF00E5FF)
    val vividPurple = Color(0xFFB000FF)
    
    // خلفية الشاشة المتدرجة العميقة
    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(midnightBlue, deepBlue, midnightBlue)
    )

    // تأثير الزجاج الشفاف بنسبة 40%
    val glassColor = Color(0xFF1E293B).copy(alpha = 0.4f)
    val glassBorder = Color.White.copy(alpha = 0.15f)
    val textSilver = Color(0xFFE2E8F0)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = backgroundBrush),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .clip(RoundedCornerShape(28.dp))
                .background(glassColor)
                .border(1.5.dp, glassBorder, RoundedCornerShape(28.dp))
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // الأيقونة البرمجية المتوهجة
            Box(
                modifier = Modifier
                    .size(85.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(electricBlue, vividPurple))),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Favorite,
                    contentDescription = "Medical Heart",
                    tint = Color.White,
                    modifier = Modifier.size(45.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "PICU Calculator",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold
            )
            
            Text(
                text = "Ahmad Qudah",
                color = electricBlue,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(35.dp))

            // عرض Device ID مع زر النسخ الأنيق
            Text(
                text = "Device ID:",
                color = textSilver,
                fontSize = 13.sp,
                modifier = Modifier.align(Alignment.Start)
            )
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = deviceId,
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("Device ID", deviceId)
                    clipboard.setPrimaryClip(clip)
                    Toast.makeText(context, "Device ID Copied!", Toast.LENGTH_SHORT).show()
                }) {
                    Icon(
                        imageVector = Icons.Filled.ContentCopy,
                        contentDescription = "Copy ID",
                        tint = electricBlue
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // حقل إدخال كود التفعيل
            OutlinedTextField(
                value = activationCode,
                onValueChange = { activationCode = it },
                label = { Text("Enter Activation Code", color = textSilver) },
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = electricBlue,
                    unfocusedBorderColor = glassBorder,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = electricBlue,
                    containerColor = Color.Transparent
                ),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(28.dp))

            // زر التفعيل المتوهج
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Brush.horizontalGradient(listOf(electricBlue, vividPurple)))
                    .clickable {
                        if (verifyActivationCode(deviceId, activationCode)) {
                            Toast.makeText(context, "Activated Successfully!", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(context, "Invalid Code!", Toast.LENGTH_SHORT).show()
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Text("VERIFY & ACTIVATE", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // زر الواتساب (تم تصحيح BorderStroke هنا)
            OutlinedButton(
                onClick = {
                    val phoneNumber = "+962782088812"
                    val message = "Hello, I need an activation code for PICUCalculator.\nMy Device ID is: $deviceId"
                    val url = "https://api.whatsapp.com/send?phone=$phoneNumber&text=${Uri.encode(message)}"
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        data = Uri.parse(url)
                    }
                    try {
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        Toast.makeText(context, "WhatsApp is not installed.", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = electricBlue),
                border = BorderStroke(1.5.dp, electricBlue),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Get Code via WhatsApp", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
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