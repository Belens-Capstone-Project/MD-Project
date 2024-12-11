package com.example.belensapp.ui.splash

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.belensapp.MainActivity
import com.example.belensapp.R

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Membuat status bar transparan
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // Mengubah status bar menjadi transparan
            window.statusBarColor = Color.TRANSPARENT

            // Menambahkan flag untuk membuat konten berada di bawah status bar (full screen)
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE

            // Untuk versi Android M (API 23) dan lebih tinggi, kita bisa membuat ikon status bar menjadi gelap
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                window.decorView.systemUiVisibility =
                    window.decorView.systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            }
        }
        // Delay 3 detik sebelum menuju ke MainActivity
        android.os.Handler().postDelayed({
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish() // Menutup SplashActivity agar tidak kembali ke Splash setelah MainActivity
        }, 3000) // 3000 ms = 3 detik
    }
}