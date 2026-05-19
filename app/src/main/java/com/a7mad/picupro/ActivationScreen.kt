package com.a7mad.picupro

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.a7mad.picupro.security.ActivationManager
import kotlinx.coroutines.launch

// الألوان المعتمدة (Dark Theme)
private val DarkBg = Color(0xFF111827)
private val TealPrimary = Color(0xFF0D9488)
private val CardBg = Color(0xFF1F2937)

@Composable
fun ActivationScreen(onActivationSuccess: () -> Unit) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val deviceId = remember { ActivationManager.getDeviceId(context) }
    var enteredCode by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Ahmad Qudah",
            color = Color.White,
            fontSize = 32.sp,
            fontWeight = FontWeight.ExtraBold
        )
        Text(
            text = "Medications calculations",
            color = TealPrimary,
            fontSize = 16.sp,
            modifier = Modifier.padding(top = 8.dp, bottom = 40.dp)
        )

        Card(
            colors = CardDefaults.cardColors(containerColor = CardBg),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Device ID", color = Color.LightGray, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(DarkBg)
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .fillMaxWidth()
                ) {
                    Text(
                        text = deviceId,
                        color = TealPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                    IconButton(onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("Device ID", deviceId)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "Copied!", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = TealPrimary)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = enteredCode,
            onValueChange = { enteredCode = it; errorMessage = "" },
            label = { Text("Enter Activation Code", color = Color.LightGray) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = TealPrimary,
                unfocusedBorderColor = Color.Gray,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            singleLine = true
        )

        if (errorMessage.isNotEmpty()) {
            Text(
                text = errorMessage,
                color = Color(0xFFEF4444),
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                if (ActivationManager.validateCode(deviceId, enteredCode) is ActivationManager.ValidationResult.Valid) {
                    coroutineScope.launch {
                        ActivationManager.setActivated(context, true)
                        onActivationSuccess()
                    }
                } else {
                    errorMessage = "Invalid Activation Code!"
                }
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("ACTIVATE", fontSize = 18.sp, fontWeight = FontWeight.Black, color = DarkBg)
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = {
                val phoneNumber = "+962782088812"
                val message = "Hello, I need an activation code for PICUCalculator.\nMy Device ID is: $deviceId"
                val url = "https://api.whatsapp.com/send?phone=$phoneNumber&text=${Uri.encode(message)}"
                try {
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                } catch (e: Exception) {
                    Toast.makeText(context, "WhatsApp is not installed.", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = TealPrimary),
            border = BorderStroke(1.dp, TealPrimary),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Contact via WhatsApp", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}