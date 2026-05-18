package com.a7mad.picupro

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.security.MessageDigest

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ActivationScreen()
        }
    }
}

@Composable
fun ActivationScreen() {
    val context = LocalContext.current
    val deviceId = remember {
        val androidId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
            ?: "UNKNOWN_DEVICE"
        androidId.take(16).uppercase().chunked(4).joinToString("-")
    }

    var activationCode by remember { mutableStateOf("") }
    var showCopyToast by remember { mutableStateOf(false) }

    // تأثير نبض للتوهج حول الأيقونة
    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    val blurRadius = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) 20.dp else 0.dp

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF0B1120), Color(0xFF111827))
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            // أيقونة a.q مع توهج نبضي
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color(0x400D9488).copy(alpha = glowAlpha),
                                Color(0x000D9488)
                            )
                        ),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                // رسم أيقونة a.q صغيرة
                Canvas(modifier = Modifier.size(80.dp)) {
                    val canvasWidth = size.width
                    val canvasHeight = size.height

                    // دائرة الخلفية الداكنة
                    drawCircle(
                        color = Color(0xFF0B1120),
                        radius = size.minDimension / 2
                    )

                    // حرف a بخط بسيط
                    drawCircle(
                        color = Color(0xFF14B8A6),
                        radius = 22f,
                        center = Offset(canvasWidth * 0.35f, canvasHeight * 0.55f)
                    )
                    drawLine(
                        color = Color(0xFF14B8A6),
                        start = Offset(canvasWidth * 0.35f, canvasHeight * 0.72f),
                        end = Offset(canvasWidth * 0.48f, canvasHeight * 0.72f),
                        strokeWidth = 4f
                    )

                    // حرف q
                    drawCircle(
                        color = Color(0xFF6366F1),
                        radius = 22f,
                        center = Offset(canvasWidth * 0.65f, canvasHeight * 0.55f)
                    )
                    drawLine(
                        color = Color(0xFF6366F1),
                        start = Offset(canvasWidth * 0.65f, canvasHeight * 0.72f),
                        end = Offset(canvasWidth * 0.65f, canvasHeight * 0.90f),
                        strokeWidth = 4f
                    )

                    // ECG خط
                    val points = listOf(
                        Offset(canvasWidth * 0.1f, canvasHeight * 0.9f),
                        Offset(canvasWidth * 0.25f, canvasHeight * 0.9f),
                        Offset(canvasWidth * 0.3f, canvasHeight * 0.7f),
                        Offset(canvasWidth * 0.38f, canvasHeight * 0.95f),
                        Offset(canvasWidth * 0.45f, canvasHeight * 0.85f),
                        Offset(canvasWidth * 0.9f, canvasHeight * 0.85f)
                    )
                    for (i in 0 until points.size - 1) {
                        drawLine(
                            color = Color.White,
                            start = points[i],
                            end = points[i + 1],
                            strokeWidth = 3f
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "Ahmad Qudah",
                color = Color.White,
                fontSize = 30.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.SansSerif,
                letterSpacing = 1.sp
            )

            Text(
                text = "Medications Calculations",
                color = Color(0xFF14B8A6),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            // الكرت الزجاجي
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(28.dp))
                    .background(
                        Color(0x22FFFFFF),
                        RoundedCornerShape(28.dp)
                    )
                    .then(
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            Modifier.blur(blurRadius)
                        } else {
                            Modifier
                        }
                    )
                    .background(
                        Color(0x18FFFFFF),
                        RoundedCornerShape(28.dp)
                    )
                    .padding(28.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Device ID
                    Text(
                        text = "DEVICE ID",
                        color = Color(0xFF6B7280),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 1.5.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0x12FFFFFF))
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Text(
                            text = deviceId,
                            color = Color(0xFF14B8A6),
                            fontSize = 14.sp,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center
                        )

                        IconButton(
                            onClick = {
                                val clipboard =
                                    context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Device ID", deviceId)
                                clipboard.setPrimaryClip(clip)
                                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) {
                                    Toast.makeText(context, "Copied!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy",
                                tint = Color(0xFF6B7280),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "ACTIVATION CODE",
                        color = Color(0xFF6B7280),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 1.5.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = activationCode,
                        onValueChange = { activationCode = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("XXXX - XXXX - XXXX", color = Color(0x55FFFFFF)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0x40FFFFFF),
                            unfocusedBorderColor = Color(0x20FFFFFF),
                            cursorColor = Color(0xFF0D9488)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        textStyle = androidx.compose.ui.text.TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center
                        ),
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Characters
                        )
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            val deviceClean = deviceId.replace("-", "")
                            val secret = deviceClean + "PICU2026AhmedQudah"
                            val expectedHash = MessageDigest.getInstance("SHA-256")
                                .digest(secret.toByteArray())
                                .joinToString("") { "%02x".format(it) }
                                .take(14)
                                .uppercase()
                            val enteredClean = activationCode.replace("-", "").uppercase().trim()
                            if (enteredClean == expectedHash) {
                                Toast.makeText(context, "تم التفعيل بنجاح", Toast.LENGTH_LONG).show()
                            } else {
                                Toast.makeText(context, "كود التفعيل غير صحيح", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF0D9488),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = "VERIFY & ACTIVATE",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            val phoneNumber = "+962782088812"
                            val message = "Hello Ahmad Qudah,\n\nI need an activation code for my device.\nDevice ID: $deviceId\n\nThank you."
                            val url = "https://wa.me/$phoneNumber?text=${Uri.encode(message)}"
                            val intent = Intent(Intent.ACTION_VIEW).apply {
                                data = Uri.parse(url)
                            }
                            context.startActivity(intent)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF25D366),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = "Request via WhatsApp",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Please contact the developer via WhatsApp to receive your activation code.",
                        color = Color(0xFF6B7280),
                        fontSize = 10.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // تذييل المعلومات
            Text(
                text = "This application is dedicated to cardiac intensive care units for post-operative congenital heart disease in children.",
                color = Color(0xFFB0B8C8),
                fontSize = 11.sp,
                textAlign = TextAlign.Center,
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Light,
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Divider(color = Color(0x20FFFFFF), modifier = Modifier.width(40.dp))

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Developed by Ahmad Qudah",
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )

            Text(
                text = "a7mad3jloun@yahoo.com",
                color = Color(0xFF14B8A6),
                fontSize = 11.sp,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "v1.0",
                color = Color(0xFF6B7280),
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace
            )

            Text(
                text = "Ahmad Qudah © 2026",
                color = Color(0xFF6B7280),
                fontSize = 9.sp,
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Light,
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}