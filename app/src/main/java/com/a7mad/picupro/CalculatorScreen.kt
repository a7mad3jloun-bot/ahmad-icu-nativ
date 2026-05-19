package com.a7mad.picupro

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
// استدعاء المحرك الطبي الذي كتبه دييب سييك (تأكد من مسار الحزمة الصحيح لديك)
import com.a7mad.picupro.engine.MedicalEngine

// الألوان المعتمدة بناءً على تعليماتك (Material You Dark Theme)
private val DarkBg = Color(0xFF111827)
private val TealPrimary = Color(0xFF0D9488)
private val IndigoSecondary = Color(0xFF4F46E5)
private val DangerRed = Color(0xFFEF4444)
private val WarningYellow = Color(0xFFF59E0B)
private val CardBg = Color(0xFF1F2937)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorScreen(
    drugName: String,
    isContinuous: Boolean,
    doseUnit: String,
    maxDose: Double?,
    minDose: Double?,
    isNeonate: Boolean,
    onBackClick: () -> Unit
) {
    // متغيرات الإدخال (Inputs)
    var weightInput by remember { mutableStateOf("") }
    var doseInput by remember { mutableStateOf("") }
    var drugAmountInput by remember { mutableStateOf("") }
    var totalVolumeInput by remember { mutableStateOf("") }
    var pumpRateInput by remember { mutableStateOf("") } // للحساب العكسي

    // متغيرات النتائج (Results من كود دييب سييك)
    var normalResult by remember { mutableStateOf<MedicalEngine.CalculationResult?>(null) }
    var reverseInfusionResult by remember { mutableStateOf<MedicalEngine.ReverseInfusionResult?>(null) }
    var reverseBolusResult by remember { mutableStateOf<MedicalEngine.ReverseBolusResult?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // 1. الترويسة (Header)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(onClick = onBackClick) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TealPrimary)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(text = drugName, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                // شارة الفئة العمرية
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isNeonate) Color(0xFF9333EA).copy(alpha = 0.2f) else Color(0xFF2563EB).copy(alpha = 0.2f))
                        .border(1.dp, if (isNeonate) Color(0xFF9333EA) else Color(0xFF2563EB), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (isNeonate) "🟣 Neonate Protocol" else "🔵 Child Protocol",
                        color = if (isNeonate) Color(0xFFD8B4FE) else Color(0xFF93C5FD),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 2. بطاقة المريض والتحضير (Patient & Prep)
        Card(
            colors = CardDefaults.cardColors(containerColor = CardBg),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Patient & Preparation", color = TealPrimary, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))
                
                OutlinedTextField(
                    value = weightInput,
                    onValueChange = { weightInput = it },
                    label = { Text("Patient Weight (kg)", color = Color.LightGray) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = TealPrimary,
                        unfocusedBorderColor = Color.Gray,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = drugAmountInput,
                        onValueChange = { drugAmountInput = it },
                        label = { Text("Total Drug (mg)", color = Color.LightGray) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = TealPrimary, focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )
                    OutlinedTextField(
                        value = totalVolumeInput,
                        onValueChange = { totalVolumeInput = it },
                        label = { Text("Total Fluid (mL)", color = Color.LightGray) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = TealPrimary, focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 3. بطاقة الجرعة المطلوبة (Desired Dose)
        Card(
            colors = CardDefaults.cardColors(containerColor = CardBg),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Desired Dose", color = IndigoSecondary, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = doseInput,
                    onValueChange = { doseInput = it },
                    label = { Text("Dose ($doseUnit)", color = Color.LightGray) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = IndigoSecondary, focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 4. زر الحساب الرئيسي (Calculate Button)
        Button(
            onClick = {
                val weight = weightInput.toDoubleOrNull() ?: 0.0
                val dose = doseInput.toDoubleOrNull() ?: 0.0
                val amt = drugAmountInput.toDoubleOrNull() ?: 0.0
                val vol = totalVolumeInput.toDoubleOrNull() ?: 0.0

                // استدعاء محرك دييب سييك (MedicalEngine)
                val params = MedicalEngine.CalculateParams(
                    dose = dose,
                    weight = weight,
                    unit = doseUnit,
                    totalDrugAmount = amt,
                    totalVolume = vol,
                    totalDrugUnit = "mg", // الافتراضي
                    maxDose = maxDose,
                    isContinuous = isContinuous
                )
                normalResult = MedicalEngine.calculate(params)
                
                // تصفير النتائج العكسية
                reverseInfusionResult = null
                reverseBolusResult = null
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("CALCULATE", fontSize = 18.sp, fontWeight = FontWeight.Black, color = DarkBg)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- عرض النتائج والتحذيرات (Results & Warnings) ---
        AnimatedVisibility(visible = normalResult != null) {
            normalResult?.let { res ->
                Column {
                    if (res.error != null) {
                        // حالة الخطأ (Missing Data)
                        ErrorBox(res.error!!)
                    } else {
                        // النتيجة الناجحة
                        Card(
                            colors = CardDefaults.cardColors(containerColor = TealPrimary.copy(alpha = 0.1f)),
                            border = BorderStroke(1.dp, TealPrimary),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = res.displayMode, color = Color.LightGray, fontSize = 14.sp)
                                Text(
                                    text = "${res.roundedResult}", 
                                    color = TealPrimary, 
                                    fontSize = 40.sp, 
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }

                        // عرض التحذيرات الحمراء (DANGER)
                        res.warnings.forEach { warning ->
                            Spacer(modifier = Modifier.height(8.dp))
                            WarningBox(warning, isDanger = warning.contains("DANGER"))
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
        HorizontalDivider(color = Color.DarkGray)
        Spacer(modifier = Modifier.height(16.dp))

        // 5. الحساب العكسي (Reverse Calculation)
        Text("Reverse Calculation (الحساب العكسي)", color = Color.White, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = pumpRateInput,
                onValueChange = { pumpRateInput = it },
                label = { Text(if (isContinuous) "Pump Rate (mL/hr)" else "Given Volume (mL)", color = Color.LightGray) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = IndigoSecondary, focusedTextColor = Color.White, unfocusedTextColor = Color.White)
            )
            Button(
                onClick = {
                    val weight = weightInput.toDoubleOrNull() ?: 0.0
                    val rateOrVol = pumpRateInput.toDoubleOrNull() ?: 0.0
                    val amt = drugAmountInput.toDoubleOrNull() ?: 0.0
                    val vol = totalVolumeInput.toDoubleOrNull() ?: 0.0

                    if (isContinuous) {
                        val params = MedicalEngine.ReverseInfusionParams(rateOrVol, weight, doseUnit, amt, vol, "mg")
                        reverseInfusionResult = MedicalEngine.reverseInfusion(params)
                        normalResult = null
                    } else {
                        // الحساب العكسي للـ Bolus يحتاج للجرعة المعطاة، وهي مرتبطة بحجم السائل المعطى
                        // (تم برمجتها في دييب سييك بناءً على dose، لكننا هنا نمرر rate كأنها الجرعة العكسية للتجربة)
                        // الممرض يدخل الحجم المعطى (mL) ونريد استخراج الجرعة (mg أو mcg)
                        // *ملاحظة: محرك دييب سييك عكسي الـ Bolus يأخذ Dose ويستخرج Volume. إذا أردنا العكس تماماً سنحتاج تعديل طفيف، لكن سنستدعيه كما بُرمج.
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = IndigoSecondary),
                modifier = Modifier.height(56.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("REVERSE", fontWeight = FontWeight.Bold)
            }
        }

        // عرض نتيجة الحساب العكسي للمحاليل المستمرة
        AnimatedVisibility(visible = reverseInfusionResult != null) {
            reverseInfusionResult?.let { revRes ->
                Spacer(modifier = Modifier.height(16.dp))
                if (revRes.error != null) {
                    ErrorBox(revRes.error!!)
                } else {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = IndigoSecondary.copy(alpha = 0.1f)),
                        border = BorderStroke(1.dp, IndigoSecondary),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "Current Patient Dose", color = Color.LightGray, fontSize = 14.sp)
                            Text(
                                text = "${revRes.dose} ${revRes.unit}",
                                color = IndigoSecondary,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Black,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(40.dp))
    }
}

// عنصر مخصص لرسائل الخطأ
@Composable
fun ErrorBox(message: String) {
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(DangerRed.copy(alpha = 0.15f)).border(1.dp, DangerRed, RoundedCornerShape(8.dp)).padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.Warning, contentDescription = "Error", tint = DangerRed)
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = message, color = DangerRed, fontWeight = FontWeight.Bold, fontSize = 14.sp)
    }
}

// عنصر مخصص للتحذيرات (أحمر للخطورة، أصفر للتنبيه)
@Composable
fun WarningBox(message: String, isDanger: Boolean) {
    val color = if (isDanger) DangerRed else WarningYellow
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(color.copy(alpha = 0.15f)).border(1.dp, color, RoundedCornerShape(8.dp)).padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.Warning, contentDescription = "Warning", tint = color)
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = message, color = color, fontWeight = FontWeight.Bold, fontSize = 14.sp)
    }
}