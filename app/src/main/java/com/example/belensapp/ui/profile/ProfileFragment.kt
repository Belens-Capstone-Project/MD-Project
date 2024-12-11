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
import com.bumptech.glide.Glide
import com.example.belensapp.R
import com.example.belensapp.ui.login.LoginActivity
import com.example.belensapp.utils.Constants
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage

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

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var storage: FirebaseStorage

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        // Initialize Firebase components
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance("https://belensapp-8eff1-default-rtdb.asia-southeast1.firebasedatabase.app")
        storage = FirebaseStorage.getInstance()

        // Initialize Views
        ivProfilePhoto = view.findViewById(R.id.ivProfilePhoto)
        etUsername = view.findViewById(R.id.etUsername)
        etEmail = view.findViewById(R.id.etEmail)
        etCurrentPassword = view.findViewById(R.id.etCurrentPassword)  // Changed to match XML ID
        etNewPassword = view.findViewById(R.id.etNewPassword)
        btnSave = view.findViewById(R.id.btnSave)
        btnChangePhoto = view.findViewById(R.id.btnChangePhoto)

        // Check login status
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(requireContext(), "You are not logged in", Toast.LENGTH_SHORT).show()
            startActivity(Intent(requireContext(), LoginActivity::class.java))
            return view
        }

        // Load profile data
        loadProfileData()

        // Set click listeners
        btnChangePhoto.setOnClickListener { openGallery() }
        btnSave.setOnClickListener { saveProfileChanges() }

        return view
    }

    private fun loadProfileData() {
        val currentUser = auth.currentUser
        currentUser?.let { user ->
            // Load user data from Realtime Database
            val userRef = database.reference.child("user").child(user.uid)
            userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        // Get user data
                        val username = snapshot.child("username").getValue(String::class.java) ?: ""
                        val email = user.email ?: ""
                        val photoUrl = snapshot.child("photoUrl").getValue(String::class.java)

                        // Set data to views
                        etUsername.setText(username)
                        etEmail.setText(email)

                        // Load profile photo if exists
                        if (!photoUrl.isNullOrEmpty()) {
                            Glide.with(requireContext())
                                .load(photoUrl)
                                .circleCrop()
                                .into(ivProfilePhoto)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(requireContext(), "Failed to load profile: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
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

    private fun saveProfileChanges() {
        val currentUser = auth.currentUser ?: return

        val username = etUsername.text.toString().trim()
        val currentPassword = etCurrentPassword.text.toString().trim()
        val newPassword = etNewPassword.text.toString().trim()

        if (username.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter a username", Toast.LENGTH_SHORT).show()
            return
        }

        val updates = mutableMapOf<String, Any>()
        updates["username"] = username

        val userRef = database.reference.child("user").child(currentUser.uid)
        userRef.updateChildren(updates)
            .addOnSuccessListener {
                val sharedPref = requireContext().getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE)
                sharedPref.edit().putString(Constants.KEY_USERNAME, username).apply()

                if (currentPassword.isNotEmpty() && newPassword.isNotEmpty()) {
                    // Re-authenticate user
                    val credential = EmailAuthProvider.getCredential(currentUser.email!!, currentPassword)
                    currentUser.reauthenticate(credential)
                        .addOnSuccessListener {
                            // Update password
                            currentUser.updatePassword(newPassword)
                                .addOnSuccessListener {
                                    Toast.makeText(requireContext(), "Password updated successfully", Toast.LENGTH_SHORT).show()
                                    // Clear password fields
                                    etCurrentPassword.text.clear()
                                    etNewPassword.text.clear()

                                    // Upload photo if selected then navigate
                                    if (selectedPhotoUri != null) {
                                        uploadProfilePhoto {
                                            navigateBackToSettings()
                                        }
                                    } else {
                                        navigateBackToSettings()
                                    }
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(requireContext(), "Failed to update password: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(requireContext(), "Authentication failed: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    // If no password change, just upload photo and go back
                    if (selectedPhotoUri != null) {
                        uploadProfilePhoto {
                            Toast.makeText(requireContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show()
                            navigateBackToSettings()
                        }
                    } else {
                        Toast.makeText(requireContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show()
                        navigateBackToSettings()
                    }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Failed to update profile: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }

    private fun navigateBackToSettings() {
        parentFragmentManager.popBackStack()
    }

    private fun uploadProfilePhoto(onSuccess: () -> Unit = {}) {
        val currentUser = auth.currentUser ?: return
        val photoUri = selectedPhotoUri ?: return

        val userRef = database.reference.child("user").child(currentUser.uid)
        userRef.child("photoUrl").setValue(photoUri.toString())
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Profile photo updated successfully", Toast.LENGTH_SHORT).show()
                onSuccess()
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Failed to save photo URL: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

}
