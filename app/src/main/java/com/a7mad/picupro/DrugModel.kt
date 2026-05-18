package com.a7mad.picupro

// تحديد الفئة العمرية بصيغة صارمة تمنع التخمين أو الخطأ البرمجي
enum class AgeCategory {
    NEONATE, // مخصص لحديثي الولادة (سيُلون بالبنفسجي في الواجهة)
    CHILD    // مخصص للأطفال (سيُلون بالأزرق في الواجهة)
}

// هيكل بروتوكول الجرعات والاستخدامات لكل فئة عمرية مستقلة
data class DrugProtocol(
    val category: AgeCategory,
    val indications: List<String>,  // قائمة استخدامات العلاج السريرية
    val defaultDose: Double,        // الجرعة الافتراضية عند البدء
    val minDose: Double,            // صمام الأمان: الجرعة الدنيا
    val maxDose: Double,            // صمام الأمان الصارم: الجرعة القصوى
    val doseUnit: String            // وحدة الجرعة (مثل: mcg/kg/min أو mg/kg)
)

// الهيكل الكلي الموحد والآمن لأي دواء يدخل المحرك الطبي
data class MedicalDrug(
    val id: String,                 // معرف فريد للدواء
    val name: String,               // اسم الدواء الدقيق (مثل: Dopamine)
    val classification: String,     // التصنيف العلاجي (مثل: Inotrope)
    val isContinuous: Boolean,      // هل الدواء تسريب مستمر (Continuous) أم جرعة واحدة (Bolus)
    val protocols: List<DrugProtocol> // مصفوفة تحتوي على بروتوكول الـ Neonate والـ Child للدواء
)