package com.example.belensapp.ui.settings

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.belensapp.R
import com.example.belensapp.databinding.FragmentSettingsBinding
import com.example.belensapp.ui.login.LoginActivity
import android.content.Intent
import java.util.Locale

class SettingsFragment : Fragment() {
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SettingsViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val preferences = SettingsPreferences(requireContext())
                @Suppress("UNCHECKED_CAST")
                return SettingsViewModel(preferences) as T
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupObservers()
        setupClickListeners()
        loadUserData()
    }

    private fun setupObservers() {
        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            binding.darkModeSwitch.isChecked = state.isDarkMode
            updateBadgeVisibility(state.badgeStatuses)
            updateIconColors(state.isDarkMode)
            applyDarkMode(state.isDarkMode)
        }

        viewModel.profileData.observe(viewLifecycleOwner) { profileData ->
            binding.username.text = profileData.username
            loadProfileImage(profileData.photoUrl)
        }
    }

    private fun setupClickListeners() {
        with(binding) {
            darkModeSwitch.setOnCheckedChangeListener { _, isChecked ->
                viewModel.updateDarkMode(isChecked)
            }

            cardLanguage.setOnClickListener {
                showLanguageDialog()
            }

            cardChangeProfile.setOnClickListener {
                findNavController().navigate(R.id.action_settingsFragment_to_profileFragment)
            }

            cardLogout.setOnClickListener {
                showLogoutConfirmationDialog()
            }

            profileImage.setOnClickListener {
                findNavController().navigate(R.id.action_settingsFragment_to_profileFragment)
            }
        }
    }

    private fun loadUserData() {
        val preferences = SettingsPreferences(requireContext())
        preferences.getUserToken()?.let { token ->
            viewModel.loadUserProfile(token)
        }
    }

    private fun showLanguageDialog() {
        val languages = arrayOf("English", "Bahasa Indonesia")
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.choose_language))
            .setItems(languages) { _, which ->
                val languageCode = if (which == 0) "en" else "id"
                viewModel.updateLanguage(languageCode)
                updateLocale(languageCode)
            }
            .show()
    }

    private fun showLogoutConfirmationDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.logout_confirmation_title))
            .setMessage(getString(R.string.logout_confirmation_message))
            .setPositiveButton(getString(R.string.yes)) { _, _ ->
                performLogout()
            }
            .setNegativeButton(getString(R.string.no), null)
            .show()
    }

    private fun performLogout() {
        viewModel.logout()
        navigateToLogin()
    }

    private fun updateLocale(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val config = requireContext().resources.configuration
        config.setLocale(locale)
        requireContext().resources.updateConfiguration(config, requireContext().resources.displayMetrics)

        // Refresh the current fragment
        findNavController().navigate(R.id.action_settingsFragment_self)
    }

    private fun applyDarkMode(isDarkMode: Boolean) {
        AppCompatDelegate.setDefaultNightMode(
            if (isDarkMode) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )
    }

    private fun loadProfileImage(photoUrl: String?) {
        Glide.with(requireContext())
            .load(photoUrl ?: R.drawable.ic_launcher_foreground)
            .circleCrop()
            .into(binding.profileImage)
    }

    private fun updateIconColors(isDarkMode: Boolean) {
        val iconColor = if (isDarkMode) {
            requireContext().getColor(android.R.color.white)
        } else {
            requireContext().getColor(android.R.color.black)
        }

        with(binding) {
            languageIcon.setColorFilter(iconColor)
            changeProfileIcon.setColorFilter(iconColor)
            logoutIcon.setColorFilter(iconColor)
            if (darkModeIcon != null) {  // Check if the view exists in your layout
                darkModeIcon.setColorFilter(iconColor)
            }
        }
    }

    private fun updateBadgeVisibility(badgeStatuses: Map<Int, Boolean>) {
        badgeStatuses.forEach { (badgeNumber, isAchieved) ->
            when (badgeNumber) {
                1 -> {
                    binding.badge1Gray.visibility = if (isAchieved) View.GONE else View.VISIBLE
                    binding.badge1Color.visibility = if (isAchieved) View.VISIBLE else View.GONE
                }
                2 -> {
                    binding.badge2Gray.visibility = if (isAchieved) View.GONE else View.VISIBLE
                    binding.badge2Color.visibility = if (isAchieved) View.VISIBLE else View.GONE
                }
                3 -> {
                    binding.badge3Gray.visibility = if (isAchieved) View.GONE else View.VISIBLE
                    binding.badge3Color.visibility = if (isAchieved) View.VISIBLE else View.GONE
                }
                4 -> {
                    binding.badge4Gray.visibility = if (isAchieved) View.GONE else View.VISIBLE
                    binding.badge4Color.visibility = if (isAchieved) View.VISIBLE else View.GONE
                }
                5 -> {
                    binding.badge5Gray.visibility = if (isAchieved) View.GONE else View.VISIBLE
                    binding.badge5Color.visibility = if (isAchieved) View.VISIBLE else View.GONE
                }
            }
        }
    }

    private fun navigateToLogin() {
        val intent = Intent(requireContext(), LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        startActivity(intent)
        requireActivity().finish()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}