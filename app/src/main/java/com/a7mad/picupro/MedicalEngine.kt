package com.a7mad.picupro

import kotlin.math.roundToInt

// هياكل البيانات المستخرجة لنتائج الحسابات السريرية لمنع التداخل البرمجي
data class CalculationResult(
    val rawResult: Double?,
    val roundedResult: Double?,
    val calculatedDose: Double?,
    val warnings: List<String>,
    val error: String?,
    val displayMode: String
)

data class ReverseBolusResult(
    val volume: String?,
    val rawVolume: Double?,
    val doseGiven: Double?,
    val unit: String,
    val error: String?,
    val warning: String?
)

data class ReverseInfusionResult(
    val dose: String?,
    val rawDose: Double?,
    val unit: String,
    val rate: Double,
    val concentration: Double,
    val error: String? = null
)

data class UnitInfo(val baseUnit: String, val hasKg: Boolean, val timeFactor: Int)
data class ConcentrationInfo(val value: Double, val unit: String)

class MedicalEngine {

    // 1. دالة تفكيك وتحليل وحدات الجرعات الطبية الصارمة
    fun parseUnitString(unitString: String?): UnitInfo {
        var baseUnit = ""
        var hasKg = false
        var timeFactor = 0

        if (unitString.isNullOrBlank()) {
            return UnitInfo(baseUnit, hasKg, timeFactor)
        }

        val u = unitString.lowercase()
        if (u.contains("/kg")) hasKg = true
        
        when {
            u.contains("/min") -> timeFactor = 60
            u.contains("/hr") -> timeFactor = 1
        }

        when {
            u.contains("mcg") -> baseUnit = "mcg"
            u.contains("mg") -> baseUnit = "mg"
            u.contains("milliunits") -> baseUnit = "milliunits"
            u.contains("units") || u.contains("iu") -> baseUnit = "units"
        }

        return UnitInfo(baseUnit, hasKg, timeFactor)
    }

    // 2. دالة حساب تركيز المادة الفعالة داخل المحلول الوريدي
    fun calculateConcentration(amount: Double, volume: Double, unit: String): ConcentrationInfo {
        if (volume <= 0.0 || amount <= 0.0) {
            throw IllegalArgumentException("MissingData")
        }
        return ConcentrationInfo(amount / volume, "${unit.lowercase()}/mL")
    }

    // 3. محرك الحساب العادي (Normal Mode): حساب معدل التدفق أو حجم الجرعة
    fun calculate(
        dose: Double,
        weight: Double,
        unit: String,
        totalDrugAmount: Double,
        totalVolume: Double,
        totalDrugUnit: String,
        maxDose: Double?,
        isContinuous: Boolean
    ): CalculationResult {
        val warnings = mutableListOf<String>()
        try {
            val conc = calculateConcentration(totalDrugAmount, totalVolume, totalDrugUnit)
            val doseInfo = parseUnitString(unit)
            val drugInfo = parseUnitString(totalDrugUnit)

            // معاملات تحويل الوحدات القياسية الدقيقة
            var unitConv = 1.0
            if (doseInfo.baseUnit == "mcg" && drugInfo.baseUnit == "mg") unitConv = 0.001
            else if (doseInfo.baseUnit == "mg" && drugInfo.baseUnit == "mcg") unitConv = 1000.0
            else if (doseInfo.baseUnit == "milliunits" && drugInfo.baseUnit == "units") unitConv = 0.001
            else if (doseInfo.baseUnit == "units" && drugInfo.baseUnit == "milliunits") unitConv = 1000.0

            val weightFactor = if (doseInfo.hasKg) weight else 1.0
            val effectiveDose = dose * weightFactor * unitConv

            val finalRate: Double
            val displayMode: String
            if (isContinuous && doseInfo.timeFactor != 0) {
                finalRate = (effectiveDose * doseInfo.timeFactor) / conc.value
                displayMode = "Rate (mL/hr)"
            } else {
                finalRate = effectiveDose / conc.value
                displayMode = "Dose Volume (mL)"
            }

            // تقريب النتيجة لخانة عشرية ثنائية لمنع الالتباس السريري
            val roundedResult = (finalRate * 100.0).roundToInt() / 100.0

            // صمام الأمان: فحص تجاوز الحد الأقصى الآمن للجرعة
            if (maxDose != null && maxDose > 0.0) {
                val patientMax = maxDose * weightFactor
                if (effectiveDose > (patientMax + 0.0001)) {
                    warnings.add("DANGER: Max Dose Exceeded! (Calc: ${String.format("%.3f", effectiveDose)}, Pt Max: ${String.format("%.3f", patientMax)} ${doseInfo.baseUnit})")
                }
            }

            return CalculationResult(finalRate, roundedResult, effectiveDose, warnings, null, displayMode)

        } catch (e: Exception) {
            val errMsg = if (e.message == "MissingData") "Missing Drug Amount or Volume" else e.message
            return CalculationResult(null, null, null, warnings, errMsg, "")
        }
    }

    // 4. محرك الحساب العكسي للجرعة الواحدة (Reverse Bolus Mode)
    fun reverseBolus(
        dose: Double,
        weight: Double,
        unit: String,
        totalDrugAmount: Double,
        totalVolume: Double,
        totalDrugUnit: String,
        maxDose: Double?,
        minDose: Double?
    ): ReverseBolusResult {
        if (totalVolume <= 0.0 || totalDrugAmount <= 0.0) {
            return ReverseBolusResult(null, null, null, "", "Missing Drug Amount or Volume", null)
        }

        val doseInfo = parseUnitString(unit)
        val drugInfo = parseUnitString(totalDrugUnit)
        val weightFactor = if (doseInfo.hasKg) weight else 1.0
        val effectiveDose = dose * weightFactor

        var concentration = totalDrugAmount / totalVolume
        if (doseInfo.baseUnit == "mcg" && drugInfo.baseUnit == "mg") concentration *= 1000.0
        else if (doseInfo.baseUnit == "mg" && drugInfo.baseUnit == "mcg") concentration /= 1000.0

        val volumeToDraw = effectiveDose / concentration

        var error: String? = null
        var warning: String? = null

        // صمام الأمان العكسي: فحص الحد الأعلى للجرعة المعطاة
        if (maxDose != null && maxDose > 0.0) {
            val patientMax = maxDose * weightFactor
            if (effectiveDose > patientMax) {
                error = "DANGER: Max Dose Exceeded! (Calc: ${String.format("%.3f", effectiveDose)}, Pt Max: ${String.format("%.3f", patientMax)} ${doseInfo.baseUnit})"
            }
        }

        // صمام الأمان العكسي: فحص انخفاض الجرعة والتنبيه لضبط وظائف الكبد والكلى
        if (minDose != null && minDose > 0.0) {
            val patientMin = minDose * weightFactor
            if (effectiveDose < patientMin) {
                warning = "Low Dose Alert! (Calc: ${String.format("%.3f", effectiveDose)}, Pt Min: ${String.format("%.3f", patientMin)} ${doseInfo.baseUnit}) - Renal/Hepatic Adjustment?"
            }
        }

        return ReverseBolusResult(
            String.format("%.2f", volumeToDraw),
            volumeToDraw,
            effectiveDose,
            doseInfo.baseUnit,
            error,
            warning
        )
    }

    // 5. محرك الحساب العكسي للتسريب الوريدي المستمر (Reverse Infusion Mode)
    fun reverseInfusion(
        rate: Double,
        weight: Double,
        unit: String,
        totalDrugAmount: Double,
        totalVolume: Double,
        totalDrugUnit: String
    ): ReverseInfusionResult {
        if (totalVolume <= 0.0 || totalDrugAmount <= 0.0) {
            return ReverseInfusionResult(null, null, "", 0.0, 0.0, "Missing Drug Amount or Volume")
        }

        val doseInfo = parseUnitString(unit)
        val drugInfo = parseUnitString(totalDrugUnit)
        
        var concentration = totalDrugAmount / totalVolume
        if (doseInfo.baseUnit == "mcg" && drugInfo.baseUnit == "mg") concentration *= 1000.0
        else if (doseInfo.baseUnit == "mg" && drugInfo.baseUnit == "mcg") concentration /= 1000.0

        val timeFactor = if (doseInfo.timeFactor == 0) 1 else doseInfo.timeFactor
        val actualDose = (rate * concentration) / (weight * timeFactor)

        return ReverseInfusionResult(
            String.format("%.4f", actualDose),
            actualDose,
            doseInfo.baseUnit,
            rate,
            concentration
        )
    }
}