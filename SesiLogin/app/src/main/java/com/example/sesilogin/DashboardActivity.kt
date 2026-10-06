package com.example.sesilogin

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class DashboardActivity : AppCompatActivity() {

    private lateinit var session: SessionManager
    private lateinit var ivAvatar: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        session = SessionManager(this)

        if (!session.adaDataProfil()) {
            kembaliKeLogin()
            return
        }

        ivAvatar = findViewById(R.id.ivAvatar)
        ivAvatar.setImageResource(session.ambilAvatarResId())

        // Klik foto profil atau menu Edit Profil untuk ganti avatar
        ivAvatar.setOnClickListener { tampilkanDialogPilihAvatar() }
        findViewById<TextView>(R.id.tvEditProfil).setOnClickListener { tampilkanDialogPilihAvatar() }

        // Sapaan waktu dinamis berdasarkan jam perangkat
        val namaLengkap = session.ambilNama()
        findViewById<TextView>(R.id.tvNama).text = "${dapatkanSapaanWaktu()}, $namaLengkap"

        findViewById<TextView>(R.id.tvEmail).text = session.ambilEmail()
        findViewById<TextView>(R.id.tvWaktu).text = formatWaktu(session.ambilWaktuLogin())

        // Fitur Ganti Sandi (Pengaturan)
        findViewById<TextView>(R.id.tvPengaturanSandi).setOnClickListener {
            tampilkanDialogGantiSandi()
        }

        // Tombol Logout
        findViewById<Button>(R.id.btnLogout).setOnClickListener { keluar() }
    }

    private fun dapatkanSapaanWaktu(): String {
        val jam = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when (jam) {
            in 4..11 -> "Selamat pagi"
            in 12..14 -> "Selamat siang"
            in 15..17 -> "Selamat sore"
            else -> "Selamat malam"
        }
    }

    private fun formatWaktu(millis: Long): String {
        if (millis == 0L) return "-"
        val format = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID"))
        return format.format(Date(millis))
    }

    // Dialog Pilihan Avatar (Foto Profil)
    private fun tampilkanDialogPilihAvatar() {
        val pilihan = arrayOf("Avatar Hijau (Default)", "Avatar Biru", "Avatar Oranye")
        val avatarSaatIni = session.ambilAvatarIndex()

        AlertDialog.Builder(this)
            .setTitle("Pilih Foto Profil Avatar")
            .setSingleChoiceItems(pilihan, avatarSaatIni) { dialog, which ->
                session.simpanAvatar(which)
                ivAvatar.setImageResource(session.ambilAvatarResId())
                Toast.makeText(this, "Avatar berhasil diubah!", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    // Dialog Ganti Sandi dengan Hash SHA-256
    private fun tampilkanDialogGantiSandi() {
        val layout = layoutInflater.inflate(R.layout.dialog_ganti_sandi, null)
        val etSandiLama = layout.findViewById<EditText>(R.id.etSandiLama)
        val etSandiBaru = layout.findViewById<EditText>(R.id.etSandiBaru)

        AlertDialog.Builder(this)
            .setView(layout)
            .setPositiveButton("Simpan") { _, _ ->
                val sandiLama = etSandiLama.text.toString()
                val sandiBaru = etSandiBaru.text.toString()

                val hashLamaInput = session.hashSandi(sandiLama)
                val hashSandiTersimpan = session.ambilSandiHash()

                if (hashLamaInput != hashSandiTersimpan) {
                    Toast.makeText(this, "Sandi lama tidak cocok!", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                if (sandiBaru.length < 6) {
                    Toast.makeText(this, "Sandi baru minimal 6 karakter!", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                // Simpan sandi baru berupa hash SHA-256
                session.simpanSandiBaru(sandiBaru)
                Toast.makeText(this, "Sandi berhasil diperbarui!", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun keluar() {
        // Hapus status login. Jika "Ingat saya" dicentang saat login, email tetap tersimpan.
        session.hapusSesiIngatEmail()
        kembaliKeLogin()
    }

    private fun kembaliKeLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}