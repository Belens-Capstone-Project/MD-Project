package com.example.belensapp.ui.register

import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.Patterns
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.belensapp.R
import com.example.belensapp.ui.login.LoginActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class RegisterActivity : AppCompatActivity() {

    private lateinit var etEmail: EditText
    private lateinit var etUsername: EditText
    private lateinit var etPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var btnRegister: Button
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var ivImagePreview: ImageView
    private lateinit var ivImagePreviewNight: ImageView
    private lateinit var btnPasswordVisibility: ImageButton
    private lateinit var btnConfirmPasswordVisibility: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Initialize Firebase Database
        database = FirebaseDatabase.getInstance("https://belensapp-8eff1-default-rtdb.asia-southeast1.firebasedatabase.app")
            .getReference("user")

        // Initialize Views
        etEmail = findViewById(R.id.etEmail)
        etUsername = findViewById(R.id.etUsername)
        etPassword = findViewById(R.id.etPassword)
        etConfirmPassword = findViewById(R.id.etConfirmPassword)
        btnRegister = findViewById(R.id.btnRegister)
        ivImagePreview = findViewById(R.id.ivImagePreview)
        ivImagePreviewNight = findViewById(R.id.ivImagePreviewNight)
        btnPasswordVisibility = findViewById(R.id.btnPasswordVisibility)
        btnConfirmPasswordVisibility = findViewById(R.id.btnConfirmPasswordVisibility)

        // Set status bar to transparent
        makeStatusBarTransparent()

        // Set the appropriate image based on the theme mode
        val currentNightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        when (currentNightMode) {
            Configuration.UI_MODE_NIGHT_YES -> {
                ivImagePreviewNight.visibility = View.VISIBLE
                ivImagePreview.visibility = View.GONE
                btnPasswordVisibility.imageTintList = ContextCompat.getColorStateList(this, R.color.nighttext)
                btnConfirmPasswordVisibility.imageTintList = ContextCompat.getColorStateList(this, R.color.nighttext)
            }
            Configuration.UI_MODE_NIGHT_NO -> {
                ivImagePreview.visibility = View.VISIBLE
                ivImagePreviewNight.visibility = View.GONE
                btnPasswordVisibility.imageTintList = ContextCompat.getColorStateList(this, R.color.black)
                btnConfirmPasswordVisibility.imageTintList = ContextCompat.getColorStateList(this, R.color.black)
            }
        }

        val tvRegisterTitle = findViewById<TextView>(R.id.tvRegisterTitle)
        when (currentNightMode) {
            Configuration.UI_MODE_NIGHT_YES -> {
                tvRegisterTitle.setTextColor(resources.getColor(android.R.color.white, theme))
            }
            Configuration.UI_MODE_NIGHT_NO -> {
                tvRegisterTitle.setTextColor(resources.getColor(R.color.red_gradient_end, theme))
            }
        }

        // Set up password visibility toggle
        setupPasswordVisibility()

        // Set onClickListener for the register button
        btnRegister.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val username = etUsername.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val confirmPassword = etConfirmPassword.text.toString().trim()

            if (!validateInputs(email, username, password, confirmPassword)) {
                return@setOnClickListener
            }

            // Show progress indicator
            showLoading(true)

            // Proceed with registration
            registerUser(username, email, password)
        }
    }

    private fun makeStatusBarTransparent() {
        // Making status bar transparent
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = Color.TRANSPARENT
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        }

        // For API level 23 and above, we set light status bar icons on light mode
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val currentNightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
            if (currentNightMode == Configuration.UI_MODE_NIGHT_NO) {
                window.decorView.systemUiVisibility = window.decorView.systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            }
        }
    }

    private fun setupPasswordVisibility() {
        // Password visibility toggle
        btnPasswordVisibility.setOnClickListener {
            if (etPassword.transformationMethod is PasswordTransformationMethod) {
                // Show password
                etPassword.transformationMethod = HideReturnsTransformationMethod.getInstance()
                btnPasswordVisibility.setImageResource(R.drawable.ic_visibility)
            } else {
                // Hide password
                etPassword.transformationMethod = PasswordTransformationMethod.getInstance()
                btnPasswordVisibility.setImageResource(R.drawable.ic_visibility_off)
            }
            // Move cursor to the end of the text
            etPassword.setSelection(etPassword.text.length)
        }

        // Confirm Password visibility toggle
        btnConfirmPasswordVisibility.setOnClickListener {
            if (etConfirmPassword.transformationMethod is PasswordTransformationMethod) {
                // Show confirm password
                etConfirmPassword.transformationMethod = HideReturnsTransformationMethod.getInstance()
                btnConfirmPasswordVisibility.setImageResource(R.drawable.ic_visibility)
            } else {
                // Hide confirm password
                etConfirmPassword.transformationMethod = PasswordTransformationMethod.getInstance()
                btnConfirmPasswordVisibility.setImageResource(R.drawable.ic_visibility_off)
            }
            // Move cursor to the end of the text
            etConfirmPassword.setSelection(etConfirmPassword.text.length)
        }
    }

    private fun validateInputs(email: String, username: String, password: String, confirmPassword: String): Boolean {
        // Check for empty fields
        if (email.isEmpty() || username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Please enter all fields", Toast.LENGTH_SHORT).show()
            return false
        }

        // Validate email format
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.error = "Please enter a valid email address"
            etEmail.requestFocus()
            return false
        }

        // Validate password length
        if (password.length < 8) {
            etPassword.error = "Password must be at least 8 characters long"
            etPassword.requestFocus()
            return false
        }

        // Validate password complexity
        if (!password.matches(".*[A-Z].*".toRegex())) {
            etPassword.error = "Password must contain at least one uppercase letter"
            etPassword.requestFocus()
            return false
        }

        if (!password.matches(".*[a-z].*".toRegex())) {
            etPassword.error = "Password must contain at least one lowercase letter"
            etPassword.requestFocus()
            return false
        }

        if (!password.matches(".*[0-9].*".toRegex())) {
            etPassword.error = "Password must contain at least one number"
            etPassword.requestFocus()
            return false
        }

        // Check if passwords match
        if (password != confirmPassword) {
            etConfirmPassword.error = "Passwords do not match"
            etConfirmPassword.requestFocus()
            return false
        }

        return true
    }

    private fun registerUser(username: String, email: String, password: String) {
        // Create user with email and password in Firebase Auth
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Get the newly created user's UID
                    val firebaseUser = auth.currentUser
                    val userId = firebaseUser?.uid

                    // Save additional user data to Realtime Database
                    userId?.let { uid ->
                        val user = hashMapOf(
                            "userId" to uid,
                            "username" to username,
                            "email" to email
                        )

                        database.child(uid).setValue(user)
                            .addOnCompleteListener { dbTask ->
                                showLoading(false)
                                if (dbTask.isSuccessful) {
                                    Toast.makeText(this, "Registration successful", Toast.LENGTH_SHORT).show()
                                    // Send email verification
                                    firebaseUser.sendEmailVerification()
                                    // Navigate to login screen
                                    val intent = Intent(this, LoginActivity::class.java)
                                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                    startActivity(intent)
                                    finish()
                                } else {
                                    Toast.makeText(this, "Failed to save user data: ${dbTask.exception?.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                    }
                } else {
                    showLoading(false)
                    Toast.makeText(this, "Registration failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun showLoading(isLoading: Boolean) {
        btnRegister.isEnabled = !isLoading
        // You can also show a progress bar here if you have one in your layout
    }
}
