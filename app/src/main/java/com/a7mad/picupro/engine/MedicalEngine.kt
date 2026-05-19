package com.a7mad.picupro.engine

import kotlin.math.roundToInt

/**
 * المحرك الطبي الذهبي – معزول تمامًا عن واجهة المستخدم.
 * يحتوي على دوال الحساب العادي، الحساب العكسي للجرعات المتقطعة
 * (Bolus)، والحساب العكسي للجرعات المستمرة (Infusion).
 */
object MedicalEngine {

    // ---------------------------------------------------------------
    // 1. تحليل الوحدة
    // ---------------------------------------------------------------

    data class ParsedUnit(
        val baseUnit: String,   // mcg, mg, units, milliunits
        val hasKg: Boolean,
        val timeFactor: Int     // 60 إذا كانت /min، و 1 إذا كانت /hr
    )

    fun parseUnitString(unitString: String?): ParsedUnit {
        if (unitString.isNullOrBlank()) {
            return ParsedUnit(baseUnit = "", hasKg = false, timeFactor = 0)
        }
        val u = unitString.lowercase()
        val hasKg = "/kg" in u
        val timeFactor = when {
            "/min" in u -> 60
            "/hr" in u  -> 1
            else       -> 0
        }
        val baseUnit = when {
            "mcg" in u         -> "mcg"
            "mg" in u          -> "mg"
            "units" in u || "iu" in u -> "units"
            "milliunits" in u  -> "milliunits"
            else               -> ""
        }
        return ParsedUnit(baseUnit, hasKg, timeFactor)
    }

    // ---------------------------------------------------------------
    // 2. حساب التركيز
    // ---------------------------------------------------------------

    data class Concentration(
        val value: Double,
        val unit: String   // مثل "mg/mL"
    )

    fun calculateConcentration(
        amount: Double,
        volume: Double,
        unit: String?
    ): Concentration {
        if (volume <= 0.0 || amount <= 0.0) {
            throw IllegalArgumentException("MissingData")
        }
        val safeUnit = unit?.lowercase() ?: ""
        return Concentration(
            value = amount / volume,
            unit = "$safeUnit/mL"
        )
    }

    // ---------------------------------------------------------------
    // 3. كائن النتيجة
    // ---------------------------------------------------------------

    data class CalculationResult(
        val rawResult: Double? = null,
        val roundedResult: Double? = null,
        val calculatedDose: Double? = null,
        val warnings: List<String> = emptyList(),
        val error: String? = null,
        val displayMode: String = ""
    )

    // ---------------------------------------------------------------
    // 4. الحساب العادي
    // ---------------------------------------------------------------

    data class CalculateParams(
        val dose: Double,
        val weight: Double,
        val unit: String?,
        val totalDrugAmount: Double,
        val totalVolume: Double,
        val totalDrugUnit: String?,
        val maxDose: Double?,
        val isContinuous: Boolean
    )

    fun calculate(params: CalculateParams): CalculationResult {
        val result = CalculationResult()

        return try {
            val conc = calculateConcentration(
                params.totalDrugAmount, params.totalVolume, params.totalDrugUnit
            )
            val doseInfo = parseUnitString(params.unit)
            val drugInfo = parseUnitString(params.totalDrugUnit)

            // معامل تحويل الوحدات
            var unitConv = 1.0
            when {
                doseInfo.baseUnit == "mcg" && drugInfo.baseUnit == "mg"       -> unitConv = 0.001
                doseInfo.baseUnit == "mg"  && drugInfo.baseUnit == "mcg"      -> unitConv = 1000.0
                doseInfo.baseUnit == "milliunits" && drugInfo.baseUnit == "units" -> unitConv = 0.001
                doseInfo.baseUnit == "units" && drugInfo.baseUnit == "milliunits" -> unitConv = 1000.0
            }

            val weightFactor = if (doseInfo.hasKg) params.weight else 1.0
            val effectiveDose = params.dose * weightFactor * unitConv

            val finalRate: Double
            if (params.isContinuous && doseInfo.timeFactor != 0) {
                finalRate = (effectiveDose * doseInfo.timeFactor) / conc.value
                result.displayMode = "Rate (mL/hr)"
            } else {
                finalRate = effectiveDose / conc.value
                result.displayMode = "Dose Volume (mL)"
            }

            result.rawResult = finalRate
            result.roundedResult = (finalRate * 100.0).roundToInt() / 100.0
            result.calculatedDose = effectiveDose

            val warnings = mutableListOf<String>()
            params.maxDose?.let { max ->
                val patientMax = max * weightFactor
                if (effectiveDose > patientMax + 0.0001) {
                    warnings.add(
                        "DANGER: Max Dose Exceeded! " +
                        "(Calc: ${"%.3f".format(effectiveDose)}, " +
                        "Pt Max: ${"%.3f".format(patientMax)} ${doseInfo.baseUnit})"
                    )
                }
            }
            result.warnings = warnings

            result
        } catch (e: Exception) {
            result.error = if (e.message == "MissingData") {
                "Missing Drug Amount or Volume"
            } else {
                e.message ?: "Unknown error"
            }
            result
        }
    }

    // ---------------------------------------------------------------
    // 5. الحساب العكسي – جرعة متقطعة (Bolus)
    // ---------------------------------------------------------------

    data class ReverseBolusParams(
        val dose: Double,
        val weight: Double,
        val unit: String?,
        val totalDrugAmount: Double,
        val totalVolume: Double,
        val totalDrugUnit: String?,
        val maxDose: Double?,
        val minDose: Double?
    )

    data class ReverseBolusResult(
        val volume: String? = null,
        val raw: Double? = null,
        val doseGiven: String? = null,
        val unit: String? = null,
        val error: String? = null,
        val warning: String? = null
    )

    fun reverseBolus(params: ReverseBolusParams): ReverseBolusResult {
        val vol = params.totalVolume
        val amt = params.totalDrugAmount

        if (vol <= 0.0 || amt <= 0.0) {
            return ReverseBolusResult(error = "Missing Drug Amount or Volume")
        }
        if (params.dose.isNaN() || params.weight.isNaN() || vol <= 0.0) {
            return ReverseBolusResult(error = "Invalid inputs")
        }

        val doseInfo = parseUnitString(params.unit)
        val drugInfo = parseUnitString(params.totalDrugUnit)
        val weightFactor = if (doseInfo.hasKg) params.weight else 1.0
        val effectiveDose = params.dose * weightFactor

        // تحويل الوحدات
        var concentration = amt / vol
        when {
            doseInfo.baseUnit == "mcg" && drugInfo.baseUnit == "mg"  -> concentration *= 1000.0
            doseInfo.baseUnit == "mg"  && drugInfo.baseUnit == "mcg" -> concentration /= 1000.0
        }

        val volumeToDraw = effectiveDose / concentration

        var error: String? = null
        var warning: String? = null

        // فحص الحد الأعلى
        params.maxDose?.let { max ->
            val patientMax = max * weightFactor
            if (effectiveDose > patientMax) {
                error = "DANGER: Max Dose Exceeded! " +
                    "(Calc: ${"%.3f".format(effectiveDose)}, " +
                    "Pt Max: ${"%.3f".format(patientMax)} ${doseInfo.baseUnit})"
            }
        }

        // فحص الحد الأدنى
        params.minDose?.let { min ->
            val patientMin = min * weightFactor
            if (effectiveDose < patientMin) {
                warning = "Low Dose Alert! " +
                    "(Calc: ${"%.3f".format(effectiveDose)}, " +
                    "Pt Min: ${"%.3f".format(patientMin)} ${doseInfo.baseUnit}) - Renal/Hepatic Adjustment?"
            }
        }

        return ReverseBolusResult(
            volume = "%.2f".format(volumeToDraw),
            raw = volumeToDraw,
            doseGiven = "%.3f".format(effectiveDose),
            unit = doseInfo.baseUnit,
            error = error,
            warning = warning
        )
    }

    // ---------------------------------------------------------------
    // 6. الحساب العكسي – جرعة مستمرة (Infusion)
    // ---------------------------------------------------------------

    data class ReverseInfusionParams(
        val rate: Double,
        val weight: Double,
        val unit: String?,
        val totalDrugAmount: Double,
        val totalVolume: Double,
        val totalDrugUnit: String?
    )

    data class ReverseInfusionResult(
        val dose: String? = null,
        val raw: Double? = null,
        val unit: String? = null,
        val rate: Double? = null,
        val concentration: Double? = null,
        val error: String? = null
    )

    fun reverseInfusion(params: ReverseInfusionParams): ReverseInfusionResult {
        val vol = params.totalVolume
        val amt = params.totalDrugAmount

        if (vol <= 0.0 || amt <= 0.0) {
            return ReverseInfusionResult(error = "Missing Drug Amount or Volume")
        }
        if (params.rate.isNaN() || params.weight.isNaN() || vol <= 0.0) {
            return ReverseInfusionResult(error = "Invalid inputs")
        }

        val doseInfo = parseUnitString(params.unit)
        val drugInfo = parseUnitString(params.totalDrugUnit)

        var concentration = amt / vol
        when {
            doseInfo.baseUnit == "mcg" && drugInfo.baseUnit == "mg"  -> concentration *= 1000.0
            doseInfo.baseUnit == "mg"  && drugInfo.baseUnit == "mcg" -> concentration /= 1000.0
        }

        val timeFactor = doseInfo.timeFactor.toDouble()
        val actualDose = (params.rate * concentration) / (params.weight * timeFactor)

        return ReverseInfusionResult(
            dose = "%.4f".format(actualDose),
            raw = actualDose,
            unit = doseInfo.baseUnit,
            rate = params.rate,
            concentration = concentration
        )
    }
}