package com.a7mad.picupro

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import java.security.MessageDigest

class MainActivity : AppCompatActivity() {

    private lateinit var imgAppIcon: ImageView
    private lateinit var cardActivation: CardView
    private lateinit var tvDeviceId: TextView
    private lateinit var etActivationCode: EditText
    private lateinit var btnVerify: Button
    private lateinit var btnWhatsapp: Button
    private lateinit var tvEmail: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_activation)

        initViews()
        setupAnimations()
        generateDeviceId()
        setupClickListeners()
    }

    private fun initViews() {
        imgAppIcon = findViewById(R.id.img_app_icon)
        cardActivation = findViewById(R.id.card_activation)
        tvDeviceId = findViewById(R.id.device_id_text)
        etActivationCode = findViewById(R.id.activation_code_input)
        btnVerify = findViewById(R.id.verify_button)
        btnWhatsapp = findViewById(R.id.whatsapp_button)
        tvEmail = findViewById(R.id.tv_email)
    }

    private fun setupAnimations() {
        val pulseAnim = AnimationUtils.loadAnimation(this, R.anim.anim_pulse)
        imgAppIcon.startAnimation(pulseAnim)

        val fadeIn = AnimationUtils.loadAnimation(this, R.anim.anim_fade_in_up)
        cardActivation.startAnimation(fadeIn)

        val childFadeIn = AnimationUtils.loadAnimation(this, R.anim.anim_fade_in_child)
        val viewsToAnimate = listOf(
            findViewById<TextView>(R.id.tv_app_name),
            findViewById<TextView>(R.id.tv_subtitle),
            tvDeviceId,
            etActivationCode,
            btnVerify,
            btnWhatsapp
        )

        viewsToAnimate.forEachIndexed { index, view ->
            view.visibility = View.INVISIBLE
            Handler(Looper.getMainLooper()).postDelayed({
                view.visibility = View.VISIBLE
                view.startAnimation(childFadeIn)
            }, 400 + (index * 120).toLong())
        }
    }

    private fun generateDeviceId() {
        val androidId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
            ?: "UNKNOWN_DEVICE"
        val formatted = androidId.take(16).uppercase().chunked(4).joinToString("-")
        tvDeviceId.text = formatted
    }

    private fun setupClickListeners() {
        // زر التحقق
        btnVerify.setOnClickListener {
            val enteredCode = etActivationCode.text.toString().trim()
            if (enteredCode.isEmpty()) {
                etActivationCode.error = "Please enter activation code"
                return@setOnClickListener
            }
            verifyActivationCode(enteredCode)
        }

        // زر واتساب
        btnWhatsapp.setOnClickListener {
            val phoneNumber = "+962782088812"
            val message = "Hello Dr. Ahmad Qudah,\n\nI need an activation code for my device.\nDevice ID: ${tvDeviceId.text}\n\nThank you."
            val url = "https://wa.me/$phoneNumber?text=${Uri.encode(message)}"
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse(url)
            }
            startActivity(intent)
        }

        // الضغط على البريد الإلكتروني
        tvEmail.setOnClickListener {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:a7mad3jloun@yahoo.com")
                putExtra(Intent.EXTRA_SUBJECT, "Ahmad Qudah App - Activation Request")
                putExtra(Intent.EXTRA_TEXT, "Device ID: ${tvDeviceId.text}\n\nPlease send activation code.")
            }
            startActivity(intent)
        }
    }

    private fun verifyActivationCode(enteredCode: String) {
        val deviceId = tvDeviceId.text.toString().replace("-", "").trim()
        val secret = deviceId + "PICU2026AhmedQudah"
        val expectedHash = sha256(secret).take(14).uppercase()

        if (enteredCode.replace("-", "").uppercase().trim() == expectedHash) {
            Toast.makeText(this, "تم التفعيل بنجاح", Toast.LENGTH_LONG).show()
        } else {
            etActivationCode.error = "Invalid activation code"
            Toast.makeText(this, "كود التفعيل غير صحيح", Toast.LENGTH_SHORT).show()
        }
    }

    private fun sha256(input: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}