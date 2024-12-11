package com.example.belensapp.ui.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val preferences: SettingsPreferences,
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : ViewModel() {
    private val _uiState = MutableLiveData<SettingsUiState>()
    val uiState: LiveData<SettingsUiState> = _uiState

    private val _profileData = MutableLiveData<ProfileData>()
    val profileData: LiveData<ProfileData> = _profileData

    init {
        _uiState.value = SettingsUiState(
            isDarkMode = preferences.getDarkMode(),
            selectedLanguage = preferences.getLanguage()
        )
        loadBadgeStatuses()
    }

    fun updateDarkMode(isEnabled: Boolean) {
        viewModelScope.launch {
            _uiState.value = _uiState.value?.copy(isDarkMode = isEnabled)
            preferences.saveDarkMode(isEnabled)
        }
    }

    fun updateLanguage(languageCode: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value?.copy(selectedLanguage = languageCode)
            preferences.saveLanguage(languageCode)
        }
    }

    fun loadUserProfile(userToken: String) {
        viewModelScope.launch {
            try {
                database.getReference("user/$userToken")
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.exists()) {
                                val username = snapshot.child("username").getValue(String::class.java) ?: "User"
                                val photoUrl = snapshot.child("photoUrl").getValue(String::class.java)
                                _profileData.value = ProfileData(username, photoUrl)
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            _profileData.value = ProfileData("User", null)
                        }
                    })
            } catch (e: Exception) {
                _profileData.value = ProfileData("User", null)
            }
        }
    }

    fun updateBadgeStatus(badgeNumber: Int, isAchieved: Boolean) {
        viewModelScope.launch {
            val currentState = _uiState.value ?: SettingsUiState()
            val updatedBadges = currentState.badgeStatuses.toMutableMap().apply {
                put(badgeNumber, isAchieved)
            }
            _uiState.value = currentState.copy(badgeStatuses = updatedBadges)
            preferences.saveBadgeStatus(badgeNumber, isAchieved)
        }
    }

    fun logout() {
        viewModelScope.launch {
            auth.signOut()
            preferences.clearUserData()
        }
    }

    private fun loadBadgeStatuses() {
        viewModelScope.launch {
            val badges = (1..5).associate { badgeNumber ->
                badgeNumber to preferences.getBadgeStatus(badgeNumber)
            }
            _uiState.value = _uiState.value?.copy(badgeStatuses = badges)
        }
    }

    data class SettingsUiState(
        val isDarkMode: Boolean = false,
        val selectedLanguage: String = "en",
        val badgeStatuses: Map<Int, Boolean> = emptyMap()
    )

    data class ProfileData(
        val username: String,
        val photoUrl: String?
    )
}
