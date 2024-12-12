package com.example.belensapp.ui.login

import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.belensapp.MainActivity
import com.example.belensapp.R
import com.example.belensapp.ui.register.RegisterActivity
import com.example.belensapp.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError

class LoginActivity : AppCompatActivity() {

    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var tvRegister: TextView
    private lateinit var tvLoginTitle: TextView
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var ivImagePreview: ImageView
    private lateinit var ivImagePreviewNight: ImageView
    private lateinit var btnPasswordVisibility: ImageButton
    private var isPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = Color.TRANSPARENT

            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                window.decorView.systemUiVisibility =
                    window.decorView.systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            }
        }
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance("https://belensapp-8eff1-default-rtdb.asia-southeast1.firebasedatabase.app")
            .getReference("user")

        initializeViews()
        updateThemeAppearance()
        setupClickListeners()
    }



    private fun initializeViews() {
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        tvRegister = findViewById(R.id.tvRegister)
        tvLoginTitle = findViewById(R.id.tvLoginTitle)
        ivImagePreview = findViewById(R.id.ivImagePreview)
        ivImagePreviewNight = findViewById(R.id.ivImagePreviewNight)
        btnPasswordVisibility = findViewById(R.id.btnPasswordVisibility)
    }

    private fun updateThemeAppearance() {
        when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
            Configuration.UI_MODE_NIGHT_YES -> {
                ivImagePreviewNight.visibility = View.VISIBLE
                ivImagePreview.visibility = View.GONE
                tvLoginTitle.setTextColor(Color.WHITE)
                btnPasswordVisibility.imageTintList = ContextCompat.getColorStateList(this, R.color.nighttext)

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    window.statusBarColor = Color.TRANSPARENT
                }
            }
            Configuration.UI_MODE_NIGHT_NO -> {
                ivImagePreviewNight.visibility = View.GONE
                ivImagePreview.visibility = View.VISIBLE
                tvLoginTitle.setTextColor(resources.getColor(R.color.red_gradient_end, theme))
                btnPasswordVisibility.imageTintList = ContextCompat.getColorStateList(this, R.color.black)

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                    window.statusBarColor = Color.TRANSPARENT
                }
            }
        }
    }



    private fun setupClickListeners() {
        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            loginUser(email, password)
        }

        tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
        btnPasswordVisibility.setOnClickListener {
            isPasswordVisible = !isPasswordVisible
            togglePasswordVisibility()
        }
    }

    private fun togglePasswordVisibility() {
        if (isPasswordVisible) {
            etPassword.transformationMethod = PasswordTransformationMethod.getInstance()
            btnPasswordVisibility.setImageDrawable(
                ContextCompat.getDrawable(this, R.drawable.ic_visibility_off)
            )
        } else {
            etPassword.transformationMethod = HideReturnsTransformationMethod.getInstance()
            btnPasswordVisibility.setImageDrawable(
                ContextCompat.getDrawable(this, R.drawable.ic_visibility)
            )
        }

        etPassword.setSelection(etPassword.text.length)
    }

    private fun loginUser(email: String, password: String) {
        btnLogin.isEnabled = false
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (user?.isEmailVerified == true) {
                        database.child(user.uid).addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                val username = snapshot.child("username").getValue(String::class.java)
                                val sharedPref = getSharedPreferences(Constants.PREF_NAME, MODE_PRIVATE)
                                with(sharedPref.edit()) {
                                    putString(Constants.KEY_USER_TOKEN, user.uid)
                                    putString(Constants.KEY_USERNAME, username)
                                    apply()
                                }

                                Toast.makeText(this@LoginActivity, "Login successful", Toast.LENGTH_SHORT).show()
                                startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                                finish()
                            }

                            override fun onCancelled(error: DatabaseError) {
                                Toast.makeText(this@LoginActivity, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                                btnLogin.isEnabled = true
                            }
                        })
                    } else {
                        Toast.makeText(this, "Please verify your email first", Toast.LENGTH_SHORT).show()
                        auth.signOut()
                        btnLogin.isEnabled = true
                    }
                } else {
                    Toast.makeText(this, "Login failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    btnLogin.isEnabled = true
                }
            }
    }

    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        if (currentUser != null && currentUser.isEmailVerified) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}