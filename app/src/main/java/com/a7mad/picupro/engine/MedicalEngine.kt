package com.a7mad.picupro.engine

import kotlin.math.roundToInt

object MedicalEngine {

    data class ParsedUnit(
        val baseUnit: String,
        val hasKg: Boolean,
        val timeFactor: Int
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

    data class Concentration(
        val value: Double,
        val unit: String
    )

    fun calculateConcentration(amount: Double, volume: Double, unit: String?): Concentration {
        if (volume <= 0.0 || amount <= 0.0) throw IllegalArgumentException("MissingData")
        return Concentration(value = amount / volume, unit = "${unit?.lowercase() ?: ""}/mL")
    }

    data class CalculationResult(
        val rawResult: Double? = null,
        val roundedResult: Double? = null,
        val calculatedDose: Double? = null,
        val warnings: List<String> = emptyList(),
        val error: String? = null,
        var displayMode: String = ""
    )

    data class CalculateParams(
        val dose: Double, val weight: Double, val unit: String?,
        val totalDrugAmount: Double, val totalVolume: Double, val totalDrugUnit: String?,
        val maxDose: Double?, val isContinuous: Boolean
    )

    fun calculate(params: CalculateParams): CalculationResult {
        val result = CalculationResult()
        try {
            val conc = calculateConcentration(params.totalDrugAmount, params.totalVolume, params.totalDrugUnit)
            val doseInfo = parseUnitString(params.unit)
            val drugInfo = parseUnitString(params.totalDrugUnit)

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
                    warnings.add("DANGER: Max Dose Exceeded! (Calc: ${"%.3f".format(effectiveDose)}, Pt Max: ${"%.3f".format(patientMax)} ${doseInfo.baseUnit})")
                }
            }
            result.warnings = warnings
        } catch (e: Exception) {
            result.error = if (e.message == "MissingData") "Missing Drug Amount or Volume" else (e.message ?: "Unknown error")
        }
        return result
    }

    data class ReverseBolusParams(
        val dose: Double, val weight: Double, val unit: String?,
        val totalDrugAmount: Double, val totalVolume: Double, val totalDrugUnit: String?,
        val maxDose: Double?, val minDose: Double?
    )

    data class ReverseBolusByVolumeParams(
        val givenVolume: Double, val weight: Double, val unit: String?,
        val totalDrugAmount: Double, val totalVolume: Double, val totalDrugUnit: String?,
        val maxDose: Double?, val minDose: Double?
    )

    data class ReverseBolusResult(
        val volume: String? = null, val raw: Double? = null, val doseGiven: String? = null,
        val unit: String? = null, var error: String? = null, var warning: String? = null
    )

    fun reverseBolus(params: ReverseBolusParams): ReverseBolusResult {
        val vol = params.totalVolume; val amt = params.totalDrugAmount
        if (vol <= 0.0 || amt <= 0.0) return ReverseBolusResult(error = "Missing Drug Amount or Volume")
        if (params.dose.isNaN() || params.weight.isNaN()) return ReverseBolusResult(error = "Invalid inputs")

        val doseInfo = parseUnitString(params.unit); val drugInfo = parseUnitString(params.totalDrugUnit)
        val weightFactor = if (doseInfo.hasKg) params.weight else 1.0
        val effectiveDose = params.dose * weightFactor

        var concentration = amt / vol
        when { doseInfo.baseUnit == "mcg" && drugInfo.baseUnit == "mg" -> concentration *= 1000.0; doseInfo.baseUnit == "mg" && drugInfo.baseUnit == "mcg" -> concentration /= 1000.0 }

        val volumeToDraw = effectiveDose / concentration
        val result = ReverseBolusResult(volume = "%.2f".format(volumeToDraw), raw = volumeToDraw, doseGiven = "%.3f".format(effectiveDose), unit = doseInfo.baseUnit)

        params.maxDose?.let { max -> val patientMax = max * weightFactor; if (effectiveDose > patientMax) result.error = "DANGER: Max Dose Exceeded! (Calc: ${"%.3f".format(effectiveDose)}, Pt Max: ${"%.3f".format(patientMax)} ${doseInfo.baseUnit})" }
        params.minDose?.let { min -> val patientMin = min * weightFactor; if (effectiveDose < patientMin) result.warning = "Low Dose Alert! (Calc: ${"%.3f".format(effectiveDose)}, Pt Min: ${"%.3f".format(patientMin)} ${doseInfo.baseUnit}) - Renal/Hepatic Adjustment?" }
        return result
    }

    fun reverseBolusByVolume(params: ReverseBolusByVolumeParams): ReverseBolusResult {
        val vol = params.totalVolume; val amt = params.totalDrugAmount
        if (vol <= 0.0 || amt <= 0.0) return ReverseBolusResult(error = "Missing Drug Amount or Volume")
        if (params.givenVolume.isNaN() || params.weight.isNaN()) return ReverseBolusResult(error = "Invalid inputs")

        val doseInfo = parseUnitString(params.unit); val drugInfo = parseUnitString(params.totalDrugUnit)
        val weightFactor = if (doseInfo.hasKg) params.weight else 1.0

        var concentration = amt / vol
        when { doseInfo.baseUnit == "mcg" && drugInfo.baseUnit == "mg" -> concentration *= 1000.0; doseInfo.baseUnit == "mg" && drugInfo.baseUnit == "mcg" -> concentration /= 1000.0 }

        val effectiveDose = params.givenVolume * concentration
        val result = ReverseBolusResult(volume = "%.2f".format(params.givenVolume), raw = effectiveDose, doseGiven = "%.3f".format(effectiveDose), unit = doseInfo.baseUnit)

        params.maxDose?.let { max -> val patientMax = max * weightFactor; if (effectiveDose > patientMax) result.error = "DANGER: Max Dose Exceeded! (Calc: ${"%.3f".format(effectiveDose)}, Pt Max: ${"%.3f".format(patientMax)} ${doseInfo.baseUnit})" }
        params.minDose?.let { min -> val patientMin = min * weightFactor; if (effectiveDose < patientMin) result.warning = "Low Dose Alert! (Calc: ${"%.3f".format(effectiveDose)}, Pt Min: ${"%.3f".format(patientMin)} ${doseInfo.baseUnit}) - Renal/Hepatic Adjustment?" }
        return result
    }

    data class ReverseInfusionParams(
        val rate: Double, val weight: Double, val unit: String?,
        val totalDrugAmount: Double, val totalVolume: Double, val totalDrugUnit: String?
    )

    data class ReverseInfusionResult(
        val dose: String? = null, val raw: Double? = null, val unit: String? = null,
        val rate: Double? = null, val concentration: Double? = null, val error: String? = null
    )

    fun reverseInfusion(params: ReverseInfusionParams): ReverseInfusionResult {
        val vol = params.totalVolume; val amt = params.totalDrugAmount
        if (vol <= 0.0 || amt <= 0.0) return ReverseInfusionResult(error = "Missing Drug Amount or Volume")
        if (params.rate.isNaN() || params.weight.isNaN()) return ReverseInfusionResult(error = "Invalid inputs")

        val doseInfo = parseUnitString(params.unit); val drugInfo = parseUnitString(params.totalDrugUnit)
        var concentration = amt / vol
        when { doseInfo.baseUnit == "mcg" && drugInfo.baseUnit == "mg" -> concentration *= 1000.0; doseInfo.baseUnit == "mg" && drugInfo.baseUnit == "mcg" -> concentration /= 1000.0 }

        val timeFactor = doseInfo.timeFactor.toDouble()
        val actualDose = (params.rate * concentration) / (params.weight * timeFactor)

        return ReverseInfusionResult(dose = "%.4f".format(actualDose), raw = actualDose, unit = doseInfo.baseUnit, rate = params.rate, concentration = concentration)
    }
}