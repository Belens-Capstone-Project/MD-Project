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

        loadDarkModeState()

        if (isFirstTime()) {
            startActivity(Intent(this, WelcomeOneActivity::class.java))
            finish()
            return
        }
        if (!isUserLoggedIn()) {
            redirectToLogin()
            return
        }
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        navView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        navView.setupWithNavController(navController)
    }
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