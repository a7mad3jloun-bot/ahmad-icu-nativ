package com.a7mad.picupro

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
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
fun DrugListScreen(onDrugSelected: (drugName: String) -> Unit) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var allDrugs by remember { mutableStateOf<List<DrugItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf("") }
    var searchQuery by remember { mutableStateOf("") }
    var selectedAgeFilter by remember { mutableStateOf("All") }

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
                allDrugs = list.sortedBy { it.drugName.lowercase() }
                isLoading = false
            } catch (e: Exception) {
                errorMessage = e.message ?: "Failed to load medications"
                isLoading = false
            }
        }
    }

    // أسماء الأدوية الفريدة
    val uniqueDrugNames = allDrugs.map { it.drugName }.distinct()

    // تصفية حسب البحث
    val filteredNames = uniqueDrugNames.filter { name ->
        name.lowercase().contains(searchQuery.lowercase())
    }

    // تصفية حسب الفئة العمرية
    val finalFilteredNames = if (selectedAgeFilter == "All") {
        filteredNames
    } else {
        filteredNames.filter { name ->
            allDrugs.any { it.drugName == name && it.ageGroup.contains(selectedAgeFilter) }
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
            .padding(20.dp)
    ) {
        // رأس الشاشة
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Medications",
                    color = Color.White,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "${uniqueDrugNames.size} drugs available",
                    color = Color(0xFF94A3B8),
                    fontSize = 14.sp
                )
            }
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
                            list.add(DrugItem(
                                drugName = obj.getString("drug_name"),
                                indication = obj.optString("indication", ""),
                                ageGroup = obj.optString("age_group", "General"),
                                route = obj.optString("route", ""),
                                unit = obj.optString("unit", ""),
                                minDose = obj.optDouble("min_dose", 0.0),
                                maxDose = obj.optDouble("max_dose", 0.0),
                                isContinuous = obj.optBoolean("is_continuous", false)
                            ))
                        }
                        allDrugs = list.sortedBy { it.drugName.lowercase() }
                        isLoading = false
                        Toast.makeText(context, "Updated", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        errorMessage = e.message ?: "Failed"
                        isLoading = false
                    }
                }
            }) {
                Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = Color(0xFF0D9488))
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // شريط البحث
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search medications...", color = Color(0xFF6B7280)) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF6B7280)) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF0D9488),
                unfocusedBorderColor = Color(0xFF334155),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                cursorColor = Color(0xFF0D9488)
            ),
            shape = RoundedCornerShape(16.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // أزرار التصفية السريعة
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            listOf("All", "Neonate", "Child").forEach { filter ->
                val isSelected = selectedAgeFilter == filter
                val filterColor = when (filter) {
                    "Neonate" -> NeonateColor
                    "Child" -> ChildColor
                    else -> GeneralColor
                }
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedAgeFilter = filter },
                    label = {
                        Text(
                            text = filter,
                            color = if (isSelected) Color.White else Color(0xFF94A3B8),
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = CardBg,
                        selectedContainerColor = filterColor.copy(alpha = 0.3f)
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        borderColor = if (isSelected) filterColor else Color(0xFF334155),
                        selectedBorderColor = filterColor,
                        enabled = true,
                        selected = isSelected
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // المحتوى
        when {
            isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = Color(0xFF0D9488))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Loading medications...", color = Color(0xFF94A3B8))
                    }
                }
            }
            errorMessage.isNotEmpty() -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Error", color = Color(0xFFEF4444), fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(errorMessage, color = Color(0xFF94A3B8), fontSize = 14.sp, textAlign = TextAlign.Center)
                    }
                }
            }
            else -> {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(finalFilteredNames) { drugName ->
                        // إيجاد أول استخدام لهذا الدواء لأخذ معلومات العمر والوحدة
                        val sampleDrug = allDrugs.firstOrNull { it.drugName == drugName }
                        val drugAgeGroups = allDrugs.filter { it.drugName == drugName }.map { it.ageGroup }.distinct()
                        val primaryAge = sampleDrug?.ageGroup ?: "General"
                        val ageColor = when {
                            primaryAge.contains("Neonate") -> NeonateColor
                            primaryAge.contains("Child") -> ChildColor
                            else -> GeneralColor
                        }

                        Card(
                            colors = CardDefaults.cardColors(containerColor = SurfaceBg),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onDrugSelected(drugName) }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp)
                            ) {
                                // شريط لوني جانبي
                                Box(
                                    modifier = Modifier
                                        .width(4.dp)
                                        .height(56.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(
                                            Brush.verticalGradient(
                                                colors = listOf(ageColor, ageColor.copy(alpha = 0.3f))
                                            )
                                        )
                                )

                                Spacer(modifier = Modifier.width(16.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = drugName,
                                        color = Color.White,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        drugAgeGroups.take(3).forEach { age ->
                                            val chipColor = when {
                                                age.contains("Neonate") -> NeonateColor
                                                age.contains("Child") -> ChildColor
                                                else -> GeneralColor
                                            }
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(chipColor.copy(alpha = 0.2f))
                                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = age,
                                                    color = chipColor,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                            }
                                        }
                                        if (drugAgeGroups.size > 3) {
                                            Text(
                                                text = "+${drugAgeGroups.size - 3}",
                                                color = Color(0xFF6B7280),
                                                fontSize = 10.sp
                                            )
                                        }
                                    }
                                }

                                // سهم للأمام
                                Text(
                                    text = "›",
                                    color = Color(0xFF6B7280),
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Light
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

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