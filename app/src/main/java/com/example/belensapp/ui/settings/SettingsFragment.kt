package com.example.belensapp.ui.settings

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.belensapp.R
import com.example.belensapp.databinding.FragmentSettingsBinding
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.card.MaterialCardView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import android.content.Intent
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.belensapp.ui.login.LoginActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private lateinit var darkModeSwitch: SwitchMaterial
    private lateinit var languageCard: MaterialCardView
    private lateinit var profileCard: MaterialCardView

    private val pickImageResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == AppCompatActivity.RESULT_OK) {
            val imageUri = result.data?.data
            imageUri?.let {
                binding.profileImage.setImageURI(imageUri)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val settingsViewModel = ViewModelProvider(this).get(SettingsViewModel::class.java)

        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textSettings
        settingsViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }
        setupDarkModeSwitch()

        updateIconColors()
        languageCard = binding.cardLanguage
        languageCard.setOnClickListener {
            showLanguageChangeDialog()
        }

        profileCard = binding.cardChangeProfile
        profileCard.setOnClickListener {
            findNavController().navigate(R.id.action_settingsFragment_to_profileFragment)
        }

        val logoutCard: MaterialCardView = binding.cardLogout
        logoutCard.setOnClickListener {
            logoutUser()
        }

        updateBadgeColors(0)
        loadProfilePhoto()
        loadScanCount()
        return root
    }
    private fun saveBadgeStatus(badgeNumber: Int, isAchieved: Boolean) {
        val sharedPref = requireContext().getSharedPreferences("badge_status", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putBoolean("badge_$badgeNumber", isAchieved)
            apply()
        }
    }

    private fun getBadgeStatus(badgeNumber: Int): Boolean {
        val sharedPref = requireContext().getSharedPreferences("badge_status", Context.MODE_PRIVATE)
        return sharedPref.getBoolean("badge_$badgeNumber", false)
    }

    private fun updateBadgeColors(scanCount: Int) {
        if (scanCount >= 1) {
            binding.badge1Gray.visibility = View.GONE
            binding.badge1Color.visibility = View.VISIBLE
        }
        if (scanCount >= 5) {
            binding.badge2Gray.visibility = View.GONE
            binding.badge2Color.visibility = View.VISIBLE
        }
        if (scanCount >= 10) {
            binding.badge3Gray.visibility = View.GONE
            binding.badge3Color.visibility = View.VISIBLE
        }
        if (scanCount >= 20) {
            binding.badge4Gray.visibility = View.GONE
            binding.badge4Color.visibility = View.VISIBLE
        }
        if (scanCount >= 50) {
            binding.badge5Gray.visibility = View.GONE
            binding.badge5Color.visibility = View.VISIBLE
        }
    }


    private fun loadScanCount() {
        val sharedPref = requireContext().getSharedPreferences("user_pref", Context.MODE_PRIVATE)
        val userToken = sharedPref.getString("user_token", null) ?: return

        val databaseRef = FirebaseDatabase.getInstance("https://belensapp-8eff1-default-rtdb.asia-southeast1.firebasedatabase.app")
            .getReference("predictcount/predictcount/$userToken")

        databaseRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val scanCount = snapshot.child("scan-count").getValue(Int::class.java) ?: 0
                    updateBadgeColors(scanCount)
                } else {
                    updateBadgeColors(0)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Failed to load scan count: ${error.message}", Toast.LENGTH_SHORT).show()
                updateBadgeColors(0)
            }
        })
    }


    private fun setupDarkModeSwitch() {
        darkModeSwitch = binding.darkModeSwitch
        val sharedPref = requireContext().getSharedPreferences("app_pref", Context.MODE_PRIVATE)
        val isDarkMode = sharedPref.getBoolean("is_dark_mode", false)
        darkModeSwitch.isChecked = isDarkMode

        darkModeSwitch.setOnCheckedChangeListener { _, isChecked ->
            toggleDarkMode(isChecked)
            updateIconColors()
        }
    }

    private fun saveDarkModeState(isEnabled: Boolean) {
        val sharedPref = requireContext().getSharedPreferences("app_pref", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putBoolean("is_dark_mode", isEnabled)
            apply()
        }
    }

    private fun toggleDarkMode(isEnabled: Boolean) {
        if (isEnabled) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
        saveDarkModeState(isEnabled)
    }

    private fun updateIconColors() {
        val isDarkMode = isDarkModeEnabled()
        val iconColor = if (isDarkMode) {
            requireContext().getColor(android.R.color.white)
        } else {
            requireContext().getColor(android.R.color.black)
        }

        binding.languageIcon.setColorFilter(iconColor)
        binding.changeProfileIcon.setColorFilter(iconColor)
        binding.logoutIcon.setColorFilter(iconColor)
    }

    private fun isDarkModeEnabled(): Boolean {
        val sharedPref = requireContext().getSharedPreferences("app_pref", Context.MODE_PRIVATE)
        return sharedPref.getBoolean("is_dark_mode", false)
    }

    private fun showLanguageChangeDialog() {
        val languages = arrayOf("English", "Bahasa Indonesia")
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(getString(R.string.choose_language))
        builder.setItems(languages) { _, which ->
            when (which) {
                0 -> changeLanguage("en")
                1 -> changeLanguage("id")
            }
        }
        builder.show()
    }

    private fun changeLanguage(languageCode: String) {
        val sharedPref = requireContext().getSharedPreferences("app_pref", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString("selected_language", languageCode)
            apply()
        }

        val locale = java.util.Locale(languageCode)
        java.util.Locale.setDefault(locale)
        val config = requireContext().resources.configuration
        config.setLocale(locale)
        requireContext().resources.updateConfiguration(config, requireContext().resources.displayMetrics)

        Toast.makeText(
            requireContext(),
            getString(R.string.language_changed_to) + " $languageCode",
            Toast.LENGTH_SHORT
        ).show()
        requireActivity().recreate()
        }

    private fun logoutUser() {
        FirebaseAuth.getInstance().signOut()
        val sharedPref = requireContext().getSharedPreferences("user_pref", AppCompatActivity.MODE_PRIVATE)
        with(sharedPref.edit()) {
            clear()
            apply()
        }

        Toast.makeText(requireContext(), "Logged out successfully", Toast.LENGTH_SHORT).show()
        val intent = Intent(requireContext(), LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        startActivity(intent)
        requireActivity().finish()
    }

    private fun loadProfilePhoto() {
        val sharedPref = requireContext().getSharedPreferences("user_pref", Context.MODE_PRIVATE)
        val userToken = sharedPref.getString("user_token", null) ?: return

        val databaseRef = FirebaseDatabase.getInstance("https://belensapp-8eff1-default-rtdb.asia-southeast1.firebasedatabase.app")
            .getReference("user/$userToken")

        databaseRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val username = snapshot.child("username").getValue(String::class.java) ?: "User"
                    binding.username.text = username
                    val photoUrl = snapshot.child("photoUrl").getValue(String::class.java)
                    if (!photoUrl.isNullOrEmpty()) {
                        Glide.with(requireContext())
                            .load(photoUrl)
                            .circleCrop()
                            .into(binding.profileImage)
                    } else {
                        Glide.with(requireContext())
                            .load(R.drawable.ic_launcher_foreground)
                            .circleCrop()
                            .into(binding.profileImage)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                binding.username.text = "User"
                Toast.makeText(requireContext(), "Failed to load profile: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}