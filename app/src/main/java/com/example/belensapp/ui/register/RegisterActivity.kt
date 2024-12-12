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

        auth = FirebaseAuth.getInstance()

        database = FirebaseDatabase.getInstance("https://belensapp-8eff1-default-rtdb.asia-southeast1.firebasedatabase.app")
            .getReference("user")

        etEmail = findViewById(R.id.etEmail)
        etUsername = findViewById(R.id.etUsername)
        etPassword = findViewById(R.id.etPassword)
        etConfirmPassword = findViewById(R.id.etConfirmPassword)
        btnRegister = findViewById(R.id.btnRegister)
        ivImagePreview = findViewById(R.id.ivImagePreview)
        ivImagePreviewNight = findViewById(R.id.ivImagePreviewNight)
        btnPasswordVisibility = findViewById(R.id.btnPasswordVisibility)
        btnConfirmPasswordVisibility = findViewById(R.id.btnConfirmPasswordVisibility)

        makeStatusBarTransparent()
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

        setupPasswordVisibility()
        btnRegister.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val username = etUsername.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val confirmPassword = etConfirmPassword.text.toString().trim()

            if (!validateInputs(email, username, password, confirmPassword)) {
                return@setOnClickListener
            }
            showLoading(true)
            registerUser(username, email, password)
        }
    }

    private fun makeStatusBarTransparent() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = Color.TRANSPARENT
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val currentNightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
            if (currentNightMode == Configuration.UI_MODE_NIGHT_NO) {
                window.decorView.systemUiVisibility = window.decorView.systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            }
        }
    }

    private fun setupPasswordVisibility() {
        btnPasswordVisibility.setOnClickListener {
            if (etPassword.transformationMethod is PasswordTransformationMethod) {
                etPassword.transformationMethod = HideReturnsTransformationMethod.getInstance()
                btnPasswordVisibility.setImageResource(R.drawable.ic_visibility)
            } else {
                etPassword.transformationMethod = PasswordTransformationMethod.getInstance()
                btnPasswordVisibility.setImageResource(R.drawable.ic_visibility_off)
            }
            etPassword.setSelection(etPassword.text.length)
        }

        btnConfirmPasswordVisibility.setOnClickListener {
            if (etConfirmPassword.transformationMethod is PasswordTransformationMethod) {
                etConfirmPassword.transformationMethod = HideReturnsTransformationMethod.getInstance()
                btnConfirmPasswordVisibility.setImageResource(R.drawable.ic_visibility)
            } else {
                etConfirmPassword.transformationMethod = PasswordTransformationMethod.getInstance()
                btnConfirmPasswordVisibility.setImageResource(R.drawable.ic_visibility_off)
            }
            etConfirmPassword.setSelection(etConfirmPassword.text.length)
        }
    }

    private fun validateInputs(email: String, username: String, password: String, confirmPassword: String): Boolean {
        if (email.isEmpty() || username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Please enter all fields", Toast.LENGTH_SHORT).show()
            return false
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.error = "Please enter a valid email address"
            etEmail.requestFocus()
            return false
        }
        if (password.length < 8) {
            etPassword.error = "Password must be at least 8 characters long"
            etPassword.requestFocus()
            return false
        }
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
        if (password != confirmPassword) {
            etConfirmPassword.error = "Passwords do not match"
            etConfirmPassword.requestFocus()
            return false
        }

        return true
    }

    private fun registerUser(username: String, email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val firebaseUser = auth.currentUser
                    val userId = firebaseUser?.uid
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
                                    firebaseUser.sendEmailVerification()
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
    }
}
