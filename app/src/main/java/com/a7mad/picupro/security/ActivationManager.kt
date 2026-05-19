package com.a7mad.picupro.security

import android.content.Context
import android.content.SharedPreferences
import java.security.MessageDigest

object ActivationManager {

    private const val PREFS_NAME = "picu_activation"
    private const val KEY_ACTIVATED = "is_activated"
    private const val SECRET_SALT = "PICU2026AhmedQudah"

    private fun prefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun isActivated(context: Context): Boolean {
        return prefs(context).getBoolean(KEY_ACTIVATED, false)
    }

    fun setActivated(context: Context, activated: Boolean) {
        prefs(context).edit().putBoolean(KEY_ACTIVATED, activated).apply()
    }

    fun getDeviceId(context: Context): String {
        val androidId = android.provider.Settings.Secure.getString(
            context.contentResolver,
            android.provider.Settings.Secure.ANDROID_ID
        ) ?: "UNKNOWN_DEVICE"
        return androidId
            .take(16)
            .uppercase()
            .chunked(4)
            .joinToString("-")
    }

    fun generateActivationCode(deviceIdRaw: String): String {
        val cleanId = deviceIdRaw.replace("-", "")
        val input = cleanId + SECRET_SALT
        val hash = sha256(input)
        return hash.take(14).uppercase()
    }

    sealed class ValidationResult {
        object Valid : ValidationResult()
        object Invalid : ValidationResult()
    }

    fun validateCode(deviceIdRaw: String, enteredCode: String): ValidationResult {
        val cleanEntered = enteredCode.replace("-", "").uppercase().trim()
        val expectedCode = generateActivationCode(deviceIdRaw)
        return if (cleanEntered == expectedCode) ValidationResult.Valid
        else ValidationResult.Invalid
    }

    private fun sha256(input: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}