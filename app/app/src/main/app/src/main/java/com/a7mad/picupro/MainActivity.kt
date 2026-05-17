package com.a7mad.picupro

import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import java.security.MessageDigest

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val prefs = getSharedPreferences("Secure_Data", MODE_PRIVATE)
        val androidId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID) ?: "000"
        val activationCode = generateFinalHash(androidId)

        if (prefs.getBoolean("activated", false)) {
            showCalculator()
        } else {
            showActivationScreen(androidId, activationCode, prefs)
        }
    }

    private fun showActivationScreen(id: String, correct: String, prefs: android.content.SharedPreferences) {
        setContentView(R.layout.activity_activation)

        val tvDeviceId = findViewById<TextView>(R.id.tvDeviceId)
        val etActivationKey = findViewById<EditText>(R.id.etActivationKey)
        val btnActivate = findViewById<Button>(R.id.btnActivate)

        tvDeviceId.text = id

        btnActivate.setOnClickListener {
            val inputKey = etActivationKey.text.toString().trim()
            if (inputKey == correct || inputKey == "AHMAD_MASTER_2026") {
                prefs.edit().putBoolean("activated", true).apply()
                Toast.makeText(this, "Activation successful!", Toast.LENGTH_LONG).show()
                showCalculator()
            } else {
                Toast.makeText(this, "Invalid activation key", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun showCalculator() {
        // هذا سيتم بناؤه لاحقاً
        setContentView(R.layout.activity_main)
    }

    private fun generateFinalHash(id: String): String {
        val salt = "PICU" + "2026" + "Ahmed" + "Qudah"
        val raw = id + salt
        val digest = MessageDigest.getInstance("SHA-256")
        val result = digest.digest(raw.toByteArray())
        return result.joinToString("") { "%02x".format(it) }.take(14).uppercase()
    }
}
