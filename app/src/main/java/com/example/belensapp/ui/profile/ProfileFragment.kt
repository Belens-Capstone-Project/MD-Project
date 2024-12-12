package com.example.belensapp.ui.profile

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.belensapp.R
import com.example.belensapp.ui.login.LoginActivity
import com.example.belensapp.utils.Constants
import com.google.firebase.auth.FirebaseAuth

class ProfileFragment : Fragment() {

    private lateinit var ivProfilePhoto: ImageView
    private lateinit var etUsername: EditText
    private lateinit var etEmail: EditText
    private lateinit var etCurrentPassword: EditText
    private lateinit var etNewPassword: EditText
    private lateinit var btnSave: Button
    private lateinit var btnChangePhoto: Button

    private var selectedPhotoUri: Uri? = null
    private val PICK_IMAGE_REQUEST = 1

    private lateinit var viewModel: ProfileViewModel
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        viewModel = ViewModelProvider(this)[ProfileViewModel::class.java]
        auth = FirebaseAuth.getInstance()

        initializeViews(view)

        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(requireContext(), "You are not logged in", Toast.LENGTH_SHORT).show()
            startActivity(Intent(requireContext(), LoginActivity::class.java))
            return view
        }

        observeProfileData()
        observeUpdateResults()

        viewModel.loadProfileData()

        setupListeners()

        return view
    }

    private fun initializeViews(view: View) {
        ivProfilePhoto = view.findViewById(R.id.ivProfilePhoto)
        etUsername = view.findViewById(R.id.etUsername)
        etEmail = view.findViewById(R.id.etEmail)
        etCurrentPassword = view.findViewById(R.id.etCurrentPassword)
        etNewPassword = view.findViewById(R.id.etNewPassword)
        btnSave = view.findViewById(R.id.btnSave)
        btnChangePhoto = view.findViewById(R.id.btnChangePhoto)
    }

    private fun observeProfileData() {
        viewModel.profileData.observe(viewLifecycleOwner) { profileData ->
            etUsername.setText(profileData.username)
            etEmail.setText(profileData.email)

            if (!profileData.photoUrl.isNullOrEmpty()) {
                Glide.with(requireContext())
                    .load(profileData.photoUrl)
                    .circleCrop()
                    .into(ivProfilePhoto)
            }
        }
    }

    private fun observeUpdateResults() {
        viewModel.updateResult.observe(viewLifecycleOwner) { result ->
            result.onSuccess { message ->
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()

                val sharedPref = requireContext().getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE)
                sharedPref.edit().putString(Constants.KEY_USERNAME, etUsername.text.toString().trim()).apply()

                etCurrentPassword.text.clear()
                etNewPassword.text.clear()

                parentFragmentManager.popBackStack()
            }.onFailure { exception ->
                Toast.makeText(requireContext(), exception.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupListeners() {
        btnChangePhoto.setOnClickListener { openGallery() }

        btnSave.setOnClickListener {
            val username = etUsername.text.toString().trim()
            val currentPassword = etCurrentPassword.text.toString().takeIf { it.isNotEmpty() }
            val newPassword = etNewPassword.text.toString().takeIf { it.isNotEmpty() }

            if (username.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter a username", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.saveProfileChanges(
                username,
                currentPassword,
                newPassword,
                selectedPhotoUri
            )
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            selectedPhotoUri = data?.data
            selectedPhotoUri?.let { uri ->
                Glide.with(this)
                    .load(uri)
                    .circleCrop()
                    .into(ivProfilePhoto)
            }
        }
    }
}