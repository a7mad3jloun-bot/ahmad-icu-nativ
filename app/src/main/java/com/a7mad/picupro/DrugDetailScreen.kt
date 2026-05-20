package com.a7mad.picupro

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.a7mad.picupro.data.MedicationLoader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray

// ألوان الفئات العمرية
private val NeonateColor = Color(0xFF9333EA)
private val ChildColor = Color(0xFF2563EB)
private val GeneralColor = Color(0xFF0D9488)

private val DarkBg = Color(0xFF0B1120)
private val SurfaceBg = Color(0xFF1A1F2E)
private val CardBg = Color(0xFF1A1F2E)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DrugDetailScreen(
    drugName: String,
    onBackClick: () -> Unit,
    onProtocolSelected: (drugName: String, isContinuous: Boolean, doseUnit: String, maxDose: Double?, minDose: Double?, isNeonate: Boolean, route: String) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var allDrugs by remember { mutableStateOf<List<DrugItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf("") }

    // تحميل الأدوية
    LaunchedEffect(Unit) {
        coroutineScope.launch {
            try {
                val jsonString = withContext(Dispatchers.IO) {
                    MedicationLoader.loadMedications(context)
                }
                val jsonArray = JSONArray(jsonString)
                val list = mutableListOf<DrugItem>()
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    list.add(
                        DrugItem(
                            drugName = obj.getString("drug_name"),
                            indication = obj.optString("indication", ""),
                            ageGroup = obj.optString("age_group", "General"),
                            route = obj.optString("route", ""),
                            unit = obj.optString("unit", ""),
                            minDose = obj.optDouble("min_dose", 0.0),
                            maxDose = obj.optDouble("max_dose", 0.0),
                            isContinuous = obj.optBoolean("is_continuous", false)
                        )
                    )
                }
                allDrugs = list
                isLoading = false
            } catch (e: Exception) {
                errorMessage = e.message ?: "Failed to load medications"
                isLoading = false
            }
        }
    }

    // بروتوكولات هذا الدواء
    val drugProtocols = allDrugs.filter { it.drugName == drugName }

    // تجميع حسب الفئة العمرية
    val ageGroups = drugProtocols.groupBy { it.ageGroup }
    val sortedAges = ageGroups.keys.sortedBy { age ->
        when {
            age.contains("Neonate") -> 0
            age.contains("Child") -> 1
            else -> 2
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(DarkBg, Color(0xFF111827))
                )
            )
    ) {
        // رأس الشاشة
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .background(SurfaceBg.copy(alpha = 0.8f))
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            IconButton(onClick = onBackClick) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = drugName,
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "${drugProtocols.size} protocols",
                    color = Color(0xFF94A3B8),
                    fontSize = 13.sp
                )
            }
        }

        // المحتوى
        when {
            isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = Color(0xFF0D9488))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Loading protocols...", color = Color(0xFF94A3B8))
                    }
                }
            }
            errorMessage.isNotEmpty() -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(errorMessage, color = Color(0xFFEF4444), fontSize = 16.sp, textAlign = TextAlign.Center)
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    sortedAges.forEach { age ->
                        val ageProtocols = ageGroups[age] ?: emptyList()
                        val ageColor = when {
                            age.contains("Neonate") -> NeonateColor
                            age.contains("Child") -> ChildColor
                            else -> GeneralColor
                        }

                        // عنوان الفئة العمرية
                        item {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        Brush.horizontalGradient(
                                            colors = listOf(ageColor.copy(alpha = 0.3f), Color.Transparent)
                                        )
                                    )
                                    .padding(horizontal = 16.dp, vertical = 10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(ageColor)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = age,
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "${ageProtocols.size}",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 14.sp
                                )
                            }
                        }

                        // بطاقات البروتوكولات
                        items(ageProtocols) { protocol ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = CardBg),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onProtocolSelected(
                                            protocol.drugName,
                                            protocol.isContinuous,
                                            protocol.unit,
                                            if (protocol.maxDose > 0) protocol.maxDose else null,
                                            if (protocol.minDose > 0) protocol.minDose else null,
                                            age.contains("Neonate"),
                                            protocol.route
                                        )
                                    }
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    // الشريط العلوي الملون
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(
                                            Icons.Default.Medication,
                                            contentDescription = null,
                                            tint = ageColor,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = protocol.indication,
                                            color = Color.White,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    // طريق الإعطاء
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(Color(0xFF0B1120))
                                            .padding(12.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = "Route",
                                                    color = Color(0xFF6B7280),
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(
                                                    text = protocol.route,
                                                    color = Color(0xFFE2E8F0),
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Medium
                                                )
                                            }
                                            // شارة مستمر / متقطع
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(
                                                        if (protocol.isContinuous)
                                                            Color(0xFFF59E0B).copy(alpha = 0.2f)
                                                        else
                                                            ageColor.copy(alpha = 0.2f)
                                                    )
                                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                                            ) {
                                                Text(
                                                    text = if (protocol.isContinuous) "Continuous" else "Intermittent",
                                                    color = if (protocol.isContinuous) Color(0xFFF59E0B) else ageColor,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    // مدى الجرعة
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(GeneralColor.copy(alpha = 0.15f))
                                                .padding(horizontal = 10.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = "${protocol.minDose} - ${protocol.maxDose} ${protocol.unit}",
                                                color = GeneralColor,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(ageColor.copy(alpha = 0.15f))
                                                .padding(horizontal = 10.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = protocol.unit,
                                                color = ageColor,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Medium
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
    }
}