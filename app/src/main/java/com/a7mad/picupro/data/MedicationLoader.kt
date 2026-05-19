package com.a7mad.picupro.data

import android.content.Context
import android.os.Build
import android.os.Debug
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * محمل الأدوية الآمن.
 * - يحمل medications.enc من GitHub.
 * - يفك تشفيره بمفتاح AHMAD_MASTER_2026 (مُقطّع).
 * - يحتوي على فحوصات Anti-Debug.
 * - يرجع قائمة JSON كنص.
 */
object MedicationLoader {

    private const val DATA_URL =
        "https://raw.githubusercontent.com/a7mad3jloun-bot/PICU-med/refs/heads/main/medications.enc"

    // مفتاح التشفير – مقسم إلى 3 أجزاء (لا يظهر كاملاً أبداً)
    private const val KEY_PART_1 = "AHMAD_MA"
    private const val KEY_PART_2 = "STER_20"
    private const val KEY_PART_3 = "26"

    /**
     * تحميل وفك تشفير بيانات الأدوية.
     * يرجع النص المفكوك (JSON) أو يرمي استثناء.
     */
    fun loadMedications(context: Context): String {
        // طبقة الأمان: فحص Anti-Debug
        checkAntiDebug()

        // 1. تحميل الملف المشفر من GitHub
        val encryptedBase64 = downloadEncryptedFile()

        // 2. تجميع المفتاح الكامل
        val encryptionKey = KEY_PART_1 + KEY_PART_2 + KEY_PART_3

        // 3. فك التشفير (محاكاة CryptoJS.AES.decrypt)
        return decryptCryptoJsAes(encryptedBase64, encryptionKey)
    }

    /**
     * تحميل الملف النصي من الرابط.
     */
    private fun downloadEncryptedFile(): String {
        val url = URL(DATA_URL)
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.connectTimeout = 15000
        connection.readTimeout = 15000

        val responseCode = connection.responseCode
        if (responseCode != HttpURLConnection.HTTP_OK) {
            throw Exception("Server error: $responseCode")
        }

        val reader = BufferedReader(InputStreamReader(connection.inputStream))
        val content = reader.readText()
        reader.close()
        connection.disconnect()

        if (content.isBlank()) {
            throw Exception("Empty response from server")
        }

        return content.trim()
    }

    /**
     * فك تشفير متوافق مع CryptoJS.AES.decrypt.
     *
     * صيغة CryptoJS:
     * - أول 8 بايتات: "Salted__"
     * - ثم 8 بايتات: salt
     * - الباقي: ciphertext
     *
     * المفتاح والـ IV يُشتقان من المفتاح النصي + salt باستخدام
     * EvpKDF (MD5, 1 iteration).
     */
    private fun decryptCryptoJsAes(encryptedBase64: String, passphrase: String): String {
        // فك ترميز Base64
        val encryptedBytes = android.util.Base64.decode(encryptedBase64, android.util.Base64.DEFAULT)

        // التحقق من الترويسة "Salted__"
        val header = encryptedBytes.copyOfRange(0, 8)
        val headerString = String(header, Charsets.UTF_8)
        if (headerString != "Salted__") {
            throw Exception("Invalid encrypted file format")
        }

        // استخراج salt (8 بايتات بعد الترويسة)
        val salt = encryptedBytes.copyOfRange(8, 16)

        // استخراج النص المشفر (باقي المصفوفة)
        val ciphertext = encryptedBytes.copyOfRange(16, encryptedBytes.size)

        // اشتقاق المفتاح والـ IV باستخدام EvpKDF
        val (key, iv) = evpKdf(passphrase.toByteArray(Charsets.UTF_8), salt, 256, 128)

        // فك التشفير باستخدام AES/CBC/PKCS5Padding
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        val secretKeySpec = SecretKeySpec(key, "AES")
        val ivParameterSpec = IvParameterSpec(iv)
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec)

        val decryptedBytes = cipher.doFinal(ciphertext)
        return String(decryptedBytes, Charsets.UTF_8)
    }

    /**
     * محاكاة EvpKDF التي تستخدمها CryptoJS.
     * تشتق مفتاحاً و IV من كلمة المرور و salt.
     */
    private fun evpKdf(
        password: ByteArray,
        salt: ByteArray,
        keySize: Int,
        ivSize: Int
    ): Pair<ByteArray, ByteArray> {
        val totalSize = (keySize + ivSize) / 8
        val result = ByteArray(totalSize)
        var offset = 0

        var block = ByteArray(0)
        val md = java.security.MessageDigest.getInstance("MD5")

        while (offset < totalSize) {
            md.reset()
            md.update(block)
            md.update(password)
            md.update(salt)
            block = md.digest()

            val copyLen = minOf(block.size, totalSize - offset)
            System.arraycopy(block, 0, result, offset, copyLen)
            offset += copyLen
        }

        val key = result.copyOfRange(0, keySize / 8)
        val iv = result.copyOfRange(keySize / 8, totalSize)
        return Pair(key, iv)
    }

    /**
     * طبقة الأمان: فحص Anti-Debug.
     * يمنع تشغيل التطبيق إذا كان قيد التصحيح أو إذا اكتشف وجود Frida.
     */
    private fun checkAntiDebug() {
        // 1. فحص ما إذا كان التطبيق قيد التصحيح (Debugger متصل)
        if (Debug.isDebuggerConnected()) {
            throw SecurityException("Debugging detected – access denied")
        }

        // 2. فحص وجود Frida (من خلال البحث عن منفذ Frida الافتراضي)
        if (isFridaDetected()) {
            throw SecurityException("Frida detected – access denied")
        }

        // 3. فحص وجود Xposed (من خلال البحث عن ملفات Xposed في classpath)
        if (isXposedDetected()) {
            throw SecurityException("Xposed detected – access denied")
        }
    }

    /**
     * فحص وجود Frida عبر البحث عن منفذ Frida الافتراضي.
     */
    private fun isFridaDetected(): Boolean {
        return try {
            val process = Runtime.getRuntime().exec(arrayOf("su", "-c", "netstat -an | grep 27042"))
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val output = reader.readText()
            reader.close()
            process.waitFor()
            output.contains("27042")
        } catch (e: Exception) {
            false
        }
    }

    /**
     * فحص وجود Xposed عبر البحث عن ملفات Xposed في classpath.
     */
    private fun isXposedDetected(): Boolean {
        return try {
            val cl = ClassLoader.getSystemClassLoader()
            val method = cl.javaClass.getDeclaredMethod("findLibrary", String::class.java)
            method.isAccessible = true
            method.invoke(cl, "xposed") != null
        } catch (e: Exception) {
            false
        }
    }
}