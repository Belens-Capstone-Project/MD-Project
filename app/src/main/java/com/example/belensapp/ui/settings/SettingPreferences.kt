package com.example.belensapp.ui.settings

import android.content.Context

class SettingsPreferences(private val context: Context) {
    private val appPrefs = context.getSharedPreferences("app_pref", Context.MODE_PRIVATE)
    private val userPrefs = context.getSharedPreferences("user_pref", Context.MODE_PRIVATE)
    private val badgePrefs = context.getSharedPreferences("badge_status", Context.MODE_PRIVATE)

    fun saveDarkMode(isEnabled: Boolean) {
        appPrefs.edit().putBoolean("is_dark_mode", isEnabled).apply()
    }

    fun getDarkMode(): Boolean = appPrefs.getBoolean("is_dark_mode", false)

    fun saveLanguage(languageCode: String) {
        appPrefs.edit().putString("selected_language", languageCode).apply()
    }

    fun getLanguage(): String = appPrefs.getString("selected_language", "en") ?: "en"

    fun saveBadgeStatus(badgeNumber: Int, isAchieved: Boolean) {
        badgePrefs.edit().putBoolean("badge_$badgeNumber", isAchieved).apply()
    }

    fun getBadgeStatus(badgeNumber: Int): Boolean =
        badgePrefs.getBoolean("badge_$badgeNumber", false)

    fun getUserToken(): String? = userPrefs.getString("user_token", null)

    fun clearUserData() {
        userPrefs.edit().clear().apply()
    }
}