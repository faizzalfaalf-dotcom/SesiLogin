package com.example.sesilogin

import android.content.Context
import java.security.MessageDigest

class SessionManager(context: Context) {

    private val prefs = context.getSharedPreferences(NAMA_PREFS, Context.MODE_PRIVATE)

    companion object {
        private const val NAMA_PREFS = "sesi_login"
        private const val KEY_SUDAH_LOGIN = "sudah_login"
        private const val KEY_NAMA = "nama"
        private const val KEY_EMAIL = "email"
        private const val KEY_WAKTU = "waktu_login"
        private const val KEY_SANDI_HASH = "sandi_hash"
        private const val KEY_AVATAR_INDEX = "avatar_index"

        // 7 hari dalam milidetik
        private const val KADALUARSA_7_HARI = 7L * 24L * 60L * 60L * 1000L
    }

    fun hashSandi(sandi: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(sandi.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    // Jika "Ingat saya" dicentang, simpan status login dan email.
    // Jika "Ingat saya" TIDAK dicentang, simpan status login sebagai false dan HAPUS email.
    fun simpanSesi(nama: String, email: String, ingatSaya: Boolean, avatarIndex: Int = 0) {
        val editor = prefs.edit()
            .putBoolean(KEY_SUDAH_LOGIN, ingatSaya)
            .putString(KEY_NAMA, nama)
            .putLong(KEY_WAKTU, System.currentTimeMillis())
            .putInt(KEY_AVATAR_INDEX, avatarIndex)

        if (ingatSaya) {
            editor.putString(KEY_EMAIL, email)
        } else {
            editor.remove(KEY_EMAIL)
        }
        editor.apply()
    }

    fun simpanAvatar(index: Int) {
        prefs.edit().putInt(KEY_AVATAR_INDEX, index).apply()
    }

    fun ambilAvatarIndex(): Int = prefs.getInt(KEY_AVATAR_INDEX, 0)

    fun ambilAvatarResId(): Int {
        return when (ambilAvatarIndex()) {
            1 -> R.drawable.ic_avatar_2
            2 -> R.drawable.ic_avatar_3
            else -> R.drawable.ic_profil
        }
    }

    fun simpanSandiBaru(sandiBaru: String) {
        prefs.edit().putString(KEY_SANDI_HASH, hashSandi(sandiBaru)).apply()
    }

    fun ambilSandiHash(): String {
        val hashDefault = hashSandi("123456")
        return prefs.getString(KEY_SANDI_HASH, hashDefault) ?: hashDefault
    }

    fun sudahLogin(): Boolean {
        val login = prefs.getBoolean(KEY_SUDAH_LOGIN, false)
        val waktuTerakhir = ambilWaktuLogin()

        // Pengecekan Waktu Kadaluarsa (7 Hari)
        if (login && waktuTerakhir > 0) {
            val waktuSekarang = System.currentTimeMillis()
            if (waktuSekarang - waktuTerakhir > KADALUARSA_7_HARI) {
                hapusSesiIngatEmail()
                return false
            }
        }
        return login
    }

    fun adaDataProfil(): Boolean = ambilNama().isNotEmpty() || ambilEmail().isNotEmpty()

    fun ambilNama(): String = prefs.getString(KEY_NAMA, "") ?: ""
    fun ambilEmail(): String = prefs.getString(KEY_EMAIL, "") ?: ""
    fun ambilWaktuLogin(): Long = prefs.getLong(KEY_WAKTU, 0L)

    // Menghapus status login, nama, dan waktu, TETAPI membiarkan email jika sebelumnya disimpan
    fun hapusSesiIngatEmail() {
        prefs.edit()
            .remove(KEY_SUDAH_LOGIN)
            .remove(KEY_NAMA)
            .remove(KEY_WAKTU)
            .apply()
    }

    // Hapus seluruh sesi (full logout / clear)
    fun hapusSesi() {
        prefs.edit().clear().apply()
    }
}