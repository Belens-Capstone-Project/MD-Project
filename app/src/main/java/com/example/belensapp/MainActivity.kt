package com.example.belensapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.belensapp.databinding.ActivityMainBinding
import com.example.belensapp.ui.login.LoginActivity
import com.example.belensapp.ui.welcome.WelcomeOneActivity

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Load dark mode state first
        loadDarkModeState()

        // Cek apakah ini pertama kali aplikasi dibuka
        if (isFirstTime()) {
            startActivity(Intent(this, WelcomeOneActivity::class.java))
            finish()
            return
        }

        // Periksa status login
        if (!isUserLoggedIn()) {
            redirectToLogin()
            return
        }

        // Jika user sudah login, lanjutkan ke MainActivity
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        navView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        navView.setupWithNavController(navController)
    }

    // Method to control bottom navigation visibility
    fun setBottomNavigationVisibility(isVisible: Boolean) {
        navView.visibility = if (isVisible) View.VISIBLE else View.GONE
    }

    private fun loadDarkModeState() {
        val sharedPref = getSharedPreferences("app_pref", MODE_PRIVATE)
        val isDarkMode = sharedPref.getBoolean("is_dark_mode", false)
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }

    private fun isFirstTime(): Boolean {
        val sharedPref = getSharedPreferences("app_pref", MODE_PRIVATE)
        val isFirstTime = sharedPref.getBoolean("is_first_time", true)

        if (isFirstTime) {
            // Set is_first_time to false after first launch
            with(sharedPref.edit()) {
                putBoolean("is_first_time", false)
                apply()
            }
        }

        return isFirstTime
    }

    private fun isUserLoggedIn(): Boolean {
        val sharedPref = getSharedPreferences("user_pref", MODE_PRIVATE)
        val token = sharedPref.getString("user_token", null)
        Log.d("MainActivity", "Token retrieved: $token")
        return token != null
    }

    private fun redirectToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }
}