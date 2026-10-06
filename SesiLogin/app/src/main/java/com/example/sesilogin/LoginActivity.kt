package com.example.sesilogin

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {

    companion object {
        private const val EMAIL_DEMO = "rani@example.com"
    }

    private lateinit var session: SessionManager
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var cbIngatSaya: CheckBox
    private lateinit var tvPesanMasuk: TextView
    private lateinit var layoutForm: LinearLayout
    private lateinit var ivLogoSplash: ImageView
    private lateinit var ivLogo: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        session = SessionManager(this)

        layoutForm = findViewById(R.id.layoutForm)
        ivLogoSplash = findViewById(R.id.ivLogoSplash)
        ivLogo = findViewById(R.id.ivLogo)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        cbIngatSaya = findViewById(R.id.cbIngatSaya)
        tvPesanMasuk = findViewById(R.id.tvPesanMasuk)

        // Set gambar avatar sesuai sesi
        ivLogoSplash.setImageResource(session.ambilAvatarResId())
        ivLogo.setImageResource(session.ambilAvatarResId())

        // 1) Tampilkan splash screen logo (1,5 detik)
        layoutForm.visibility = View.GONE
        ivLogoSplash.visibility = View.VISIBLE

        Handler(Looper.getMainLooper()).postDelayed({
            if (isFinishing || isDestroyed) return@postDelayed

            // 2) Periksa Sesi (Auto-Login / Kadaluarsa 7 Hari)
            if (session.sudahLogin()) {
                bukaDashboard()
                return@postDelayed
            }

            // Tampilkan form login
            ivLogoSplash.visibility = View.GONE
            layoutForm.visibility = View.VISIBLE

            // Isikan email otomatis jika sebelumnya disimpan (karena mencentang "Ingat saya")
            val emailTersimpan = session.ambilEmail()
            if (emailTersimpan.isNotEmpty()) {
                etEmail.setText(emailTersimpan)
                cbIngatSaya.isChecked = true
            }
        }, 1500)

        findViewById<Button>(R.id.btnMasuk).setOnClickListener { prosesLogin() }
    }

    private fun prosesLogin() {
        val email = etEmail.text.toString().trim()
        val sandi = etPassword.text.toString()

        tvPesanMasuk.text = ""

        if (email.isEmpty()) {
            etEmail.error = getString(R.string.pesan_email_kosong)
            return
        }
        if (sandi.length < 6) {
            etPassword.error = getString(R.string.pesan_sandi_pendek)
            return
        }

        // Memeriksa kata sandi dengan SHA-256 Hash
        val hashSandiInput = session.hashSandi(sandi)
        val hashSandiTersimpan = session.ambilSandiHash()

        if (email != EMAIL_DEMO || hashSandiInput != hashSandiTersimpan) {
            tvPesanMasuk.text = getString(R.string.pesan_login_gagal)
            return
        }

        val nama = email.substringBefore("@").replaceFirstChar { it.uppercase() }
        session.simpanSesi(nama, email, cbIngatSaya.isChecked, session.ambilAvatarIndex())
        bukaDashboard()
    }

    private fun bukaDashboard() {
        startActivity(Intent(this, DashboardActivity::class.java))
        finish()
    }
}