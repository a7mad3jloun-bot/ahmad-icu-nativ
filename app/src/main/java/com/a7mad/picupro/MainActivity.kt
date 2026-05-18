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
                        ActivationScreen()
                    }
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
    var showAboutDialog by remember { mutableStateOf(false) }

    val midnightBlue = Color(0xFF0B0F19)
    val electricBlue = Color(0xFF00E5FF)
    val vividPurple = Color(0xFFB000FF)
    
    // تأثير الزجاج المحسن (شفافية أكثر لكي يظهر ما خلفه)
    val glassColor = Color(0xFFFFFFFF).copy(alpha = 0.08f)
    val glassBorder = Color.White.copy(alpha = 0.2f)
    val textSilver = Color(0xFFE2E8F0)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(midnightBlue)
    ) {
        // 1. الكرات المضيئة في الخلفية لخلق تأثير الزجاج (Glassmorphism) الحقيقي
        Box(
            modifier = Modifier
                .offset(x = (-60).dp, y = 100.dp)
                .size(220.dp)
                .background(vividPurple.copy(alpha = 0.45f), CircleShape)
                .blur(70.dp)
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = 60.dp, y = (-120).dp)
                .size(250.dp)
                .background(electricBlue.copy(alpha = 0.45f), CircleShape)
                .blur(70.dp)
        )

        // 2. زر المعلومات ℹ️ (كما طُلب في الملخص)
        IconButton(
            onClick = { showAboutDialog = true },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
                .padding(top = 24.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Info,
                contentDescription = "About",
                tint = electricBlue,
                modifier = Modifier.size(32.dp)
            )
        }

        // 3. الكرت الزجاجي الشفاف
        Column(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .align(Alignment.Center)
                .clip(RoundedCornerShape(28.dp))
                .background(glassColor)
                .border(1.5.dp, glassBorder, RoundedCornerShape(28.dp))
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
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
                text = "Ahmad Qudah",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold
            )
            
            Text(
                text = "Medications Calculations",
                color = electricBlue,
                fontSize = 17.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(35.dp))

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

    // 4. نافذة "حول التطبيق" (المعلومات المفقودة)
    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            containerColor = Color(0xFF1E293B),
            title = {
                Text(
                    text = "About App",
                    color = electricBlue,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "لا تنسوني ووالدي من صالح دعائكم",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Ahmad Qudah",
                        fontSize = 18.sp,
                        color = vividPurple,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "This application is dedicated to cardiac intensive care units for post-operative congenital heart disease in children.",
                        fontSize = 14.sp,
                        color = textSilver,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showAboutDialog = false }) {
                    Text("Close", color = electricBlue, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        )
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