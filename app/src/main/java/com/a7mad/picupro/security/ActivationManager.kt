package com.a7mad.picupro.security

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.security.MessageDigest

object ActivationManager {

    private const val SECRET_SALT = "PICU2026AhmedQudah"

    private val Context.dataStore by preferencesDataStore(name = "picu_activation")

    private object Keys {
        val IS_ACTIVATED = booleanPreferencesKey("is_activated")
    }

    fun isActivated(context: Context): Flow<Boolean> {
        return context.dataStore.data.map { preferences ->
            preferences[Keys.IS_ACTIVATED] ?: false
        }
    }

    suspend fun setActivated(context: Context, activated: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[Keys.IS_ACTIVATED] = activated
        }
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