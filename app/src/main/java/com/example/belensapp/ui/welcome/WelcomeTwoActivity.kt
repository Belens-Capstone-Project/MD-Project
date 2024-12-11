package com.example.belensapp.ui.welcome

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.belensapp.MainActivity
import com.example.belensapp.R
import com.example.belensapp.databinding.ActivityWelcomeTwoBinding
import com.example.belensapp.ui.login.LoginActivity

class WelcomeTwoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityWelcomeTwoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWelcomeTwoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupView()
        setupAction()
        playAnimation()
        markWelcomeShown()
    }

    private fun markWelcomeShown() {
        val sharedPref = getSharedPreferences("app_pref", MODE_PRIVATE)
        with(sharedPref.edit()) {
            putBoolean("is_first_time", false)
            apply()
        }
    }

    private fun setupView() {
        // Menyembunyikan status bar untuk tampilan fullscreen
        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
        supportActionBar?.hide()
    }

    private fun setupAction() {
        binding.loginButton.setOnClickListener {
            // Cek status login menggunakan SharedPreferences
            val sharedPref = getSharedPreferences("user_pref", MODE_PRIVATE)
            val token = sharedPref.getString("user_token", null)

            val intent = if (token != null) {
                // Jika token ada, berarti pengguna sudah login, lanjut ke MainActivity
                Intent(this, MainActivity::class.java)
            } else {
                // Jika token kosong, lanjutkan ke LoginActivity
                Intent(this, LoginActivity::class.java)
            }

            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }

    private fun playAnimation() {
        // Animasi untuk elemen-elemen UI
        ObjectAnimator.ofFloat(binding.imageView, View.TRANSLATION_X, -30f, 30f).apply {
            duration = 6000
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
        }.start()

        val login = ObjectAnimator.ofFloat(binding.loginButton, View.ALPHA, 1f).setDuration(100)
        val title = ObjectAnimator.ofFloat(binding.titleTextView, View.ALPHA, 1f).setDuration(100)
        val desc = ObjectAnimator.ofFloat(binding.descTextView, View.ALPHA, 1f).setDuration(100)

        val together = AnimatorSet().apply {
            playTogether(login)
        }

        AnimatorSet().apply {
            playSequentially(title, desc, together)
            start()
        }
    }
}
