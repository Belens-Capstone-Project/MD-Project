package com.example.belensapp.ui.profile

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage

class ProfileViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance("https://belensapp-8eff1-default-rtdb.asia-southeast1.firebasedatabase.app")
    private val storage = FirebaseStorage.getInstance()

    private val _profileData = MutableLiveData<ProfileData>()
    val profileData: LiveData<ProfileData> = _profileData

    private val _updateResult = MutableLiveData<Result<String>>()
    val updateResult: LiveData<Result<String>> = _updateResult

    data class ProfileData(
        val username: String = "",
        val email: String = "",
        val photoUrl: String? = null
    )

    fun loadProfileData() {
        val currentUser = auth.currentUser ?: return

        val userRef = database.reference.child("user").child(currentUser.uid)
        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val username = snapshot.child("username").getValue(String::class.java) ?: ""
                    val email = currentUser.email ?: ""
                    val photoUrl = snapshot.child("photoUrl").getValue(String::class.java)

                    _profileData.value = ProfileData(username, email, photoUrl)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                _profileData.value = ProfileData()
                _updateResult.value = Result.failure(Exception("Failed to load profile: ${error.message}"))
            }
        })
    }

    fun saveProfileChanges(
        username: String,
        currentPassword: String?,
        newPassword: String?,
        selectedPhotoUri: Uri?
    ) {
        val currentUser = auth.currentUser ?: return

        val updates = mutableMapOf<String, Any>()
        updates["username"] = username

        val userRef = database.reference.child("user").child(currentUser.uid)
        userRef.updateChildren(updates)
            .addOnSuccessListener {
                if (!currentPassword.isNullOrEmpty() && !newPassword.isNullOrEmpty()) {
                    updatePassword(currentUser.email!!, currentPassword, newPassword, selectedPhotoUri)
                } else {
                    selectedPhotoUri?.let {
                        uploadProfilePhoto(it)
                    } ?: run {
                        _updateResult.value = Result.success("Profile updated successfully")
                    }
                }
            }
            .addOnFailureListener { e ->
                _updateResult.value = Result.failure(Exception("Failed to update profile: ${e.message}"))
            }
    }

    private fun updatePassword(
        email: String,
        currentPassword: String,
        newPassword: String,
        selectedPhotoUri: Uri?
    ) {
        val currentUser = auth.currentUser ?: return

        val credential = EmailAuthProvider.getCredential(email, currentPassword)
        currentUser.reauthenticate(credential)
            .addOnSuccessListener {
                currentUser.updatePassword(newPassword)
                    .addOnSuccessListener {
                        selectedPhotoUri?.let {
                            uploadProfilePhoto(it)
                        } ?: run {
                            _updateResult.value = Result.success("Password and profile updated successfully")
                        }
                    }
                    .addOnFailureListener { e ->
                        _updateResult.value = Result.failure(Exception("Failed to update password: ${e.message}"))
                    }
            }
            .addOnFailureListener { e ->
                _updateResult.value = Result.failure(Exception("Authentication failed: ${e.message}"))
            }
    }

    private fun uploadProfilePhoto(photoUri: Uri) {
        val currentUser = auth.currentUser ?: return

        val userRef = database.reference.child("user").child(currentUser.uid)
        userRef.child("photoUrl").setValue(photoUri.toString())
            .addOnSuccessListener {
                _updateResult.value = Result.success("Profile photo updated successfully")
            }
            .addOnFailureListener { e ->
                _updateResult.value = Result.failure(Exception("Failed to save photo URL: ${e.message}"))
            }
    }

    override fun onCleared() {
        super.onCleared()
    }
}