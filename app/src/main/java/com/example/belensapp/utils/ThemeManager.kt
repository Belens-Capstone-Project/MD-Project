package com.example.belensapp.utils

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate

class ThemeManager(private val context: Context) {

    companion object {
        private const val THEME_PREFS = "theme_preferences"
        private const val KEY_DARK_MODE = "is_dark_mode"
    }

    private val sharedPreferences = context.getSharedPreferences(THEME_PREFS, Context.MODE_PRIVATE)

    fun loadSavedTheme() {
        val isDarkMode = sharedPreferences.getBoolean(KEY_DARK_MODE, false)
        setTheme(isDarkMode)
    }

    fun setTheme(isDarkMode: Boolean) {
        AppCompatDelegate.setDefaultNightMode(
            if (isDarkMode) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )
        saveThemeState(isDarkMode)
    }

    private fun saveThemeState(isDarkMode: Boolean) {
        sharedPreferences.edit().apply {
            putBoolean(KEY_DARK_MODE, isDarkMode)
            apply()
        }
    }

    fun isDarkMode(): Boolean {
        return sharedPreferences.getBoolean(KEY_DARK_MODE, false)
    }
}