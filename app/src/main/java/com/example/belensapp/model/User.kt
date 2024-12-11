package com.example.belensapp.model

data class User(
    val id: String = "",          // ID pengguna, misalnya dari Firebase UID
    val username: String = "",    // Nama pengguna (username)
    val password: String = "",    // Kata sandi pengguna (pastikan disimpan dengan aman, ini hanya contoh)
    val email: String = "",       // Email pengguna (opsional)
    val profilePhotoUrl: String = "" // URL foto profil (opsional)
)
