package com.a7mad.picupro

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
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
private val NeonateColor = Color(0xFF9333EA)    // بنفسجي
private val ChildColor = Color(0xFF2563EB)      // أزرق
private val GeneralColor = Color(0xFF0D9488)    // تيل

private val DarkBg = Color(0xFF111827)
private val CardBg = Color(0xFF1F2937)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DrugListScreen(onDrugSelected: (drugName: String, isContinuous: Boolean, doseUnit: String, maxDose: Double?, minDose: Double?, isNeonate: Boolean) -> Unit) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var drugs by remember { mutableStateOf<List<DrugItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf("") }

    // تحميل الأدوية عند أول ظهور للشاشة
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
                drugs = list.sortedBy { it.drugName.lowercase() }
                isLoading = false
            } catch (e: Exception) {
                errorMessage = e.message ?: "Failed to load medications"
                isLoading = false
            }
        }
    }

    // تجميع الأدوية حسب الفئة العمرية
    val ageGroups = drugs.groupBy { it.ageGroup }
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
            .background(DarkBg)
            .padding(16.dp)
    ) {
        // رأس الشاشة
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Medications List",
                color = Color.White,
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = {
                isLoading = true
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
                        drugs = list.sortedBy { it.drugName.lowercase() }
                        isLoading = false
                        Toast.makeText(context, "Medications refreshed", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        errorMessage = e.message ?: "Failed to load medications"
                        isLoading = false
                    }
                }
            }) {
                Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = Color(0xFF0D9488))
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Select a drug to begin calculation",
            color = Color(0xFF94A3B8),
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(20.dp))

        // عرض حالة التحميل أو الخطأ أو القائمة
        when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = Color(0xFF0D9488))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Loading medications...", color = Color(0xFF94A3B8), fontSize = 14.sp)
                    }
                }
            }
            errorMessage.isNotEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Error",
                            color = Color(0xFFEF4444),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = errorMessage,
                            color = Color(0xFF94A3B8),
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            else -> {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    sortedAges.forEach { age ->
                        val ageDrugs = ageGroups[age] ?: emptyList()
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
                                        .size(10.dp)
                                        .clip(RoundedCornerShape(5.dp))
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
                                    text = "${ageDrugs.size} protocols",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 13.sp
                                )
                            }
                        }

                        // بطاقات الأدوية
                        items(ageDrugs.sortedBy { it.drugName }) { drug ->
                            DrugCard(
                                drug = drug,
                                ageColor = ageColor,
                                onClick = {
                                    onDrugSelected(
                                        drug.drugName,
                                        drug.isContinuous,
                                        drug.unit,
                                        if (drug.maxDose > 0) drug.maxDose else null,
                                        if (drug.minDose > 0) drug.minDose else null,
                                        age.contains("Neonate")
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

// نموذج بيانات الدواء
data class DrugItem(
    val drugName: String,
    val indication: String,
    val ageGroup: String,
    val route: String,
    val unit: String,
    val minDose: Double,
    val maxDose: Double,
    val isContinuous: Boolean
)

// بطاقة دواء واحدة
@Composable
fun DrugCard(drug: DrugItem, ageColor: Color, onClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CardBg),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = drug.drugName,
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = drug.indication,
                        color = Color(0xFF94A3B8),
                        fontSize = 12.sp
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(ageColor.copy(alpha = 0.2f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = drug.route,
                        color = ageColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF0D9488).copy(alpha = 0.15f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Dose: ${drug.minDose} - ${drug.maxDose} ${drug.unit}",
                        color = Color(0xFF0D9488),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                if (drug.isContinuous) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFF59E0B).copy(alpha = 0.15f))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Continuous",
                            color = Color(0xFFF59E0B),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}