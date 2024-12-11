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

        // Membuat status bar transparan
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // Mengubah status bar menjadi transparan
            window.statusBarColor = Color.TRANSPARENT

            // Menambahkan flag untuk membuat konten berada di bawah status bar (full screen)
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE

            // Untuk versi Android M (API 23) dan lebih tinggi, kita bisa membuat ikon status bar menjadi gelap
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                window.decorView.systemUiVisibility =
                    window.decorView.systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            }
        }

        // Initialize Firebase Auth and Database
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

                // Mengatur tint ikon untuk tombol visibilitas password di mode malam
                btnPasswordVisibility.imageTintList = ContextCompat.getColorStateList(this, R.color.nighttext)

                // Mengatur ikon status bar agar berwarna putih di mode malam
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    window.statusBarColor = Color.TRANSPARENT // Mengatur status bar menjadi transparan
                }
            }
            Configuration.UI_MODE_NIGHT_NO -> {
                ivImagePreviewNight.visibility = View.GONE
                ivImagePreview.visibility = View.VISIBLE
                tvLoginTitle.setTextColor(resources.getColor(R.color.red_gradient_end, theme))

                // Mengatur tint ikon untuk tombol visibilitas password di mode terang
                btnPasswordVisibility.imageTintList = ContextCompat.getColorStateList(this, R.color.black)

                // Mengatur ikon status bar agar berwarna hitam di mode terang
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                    window.statusBarColor = Color.TRANSPARENT // Mengatur status bar menjadi transparan
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

        // Password visibility toggle
        btnPasswordVisibility.setOnClickListener {
            isPasswordVisible = !isPasswordVisible
            togglePasswordVisibility()
        }
    }

    private fun togglePasswordVisibility() {
        if (isPasswordVisible) {
            // Hide password
            etPassword.transformationMethod = PasswordTransformationMethod.getInstance()
            btnPasswordVisibility.setImageDrawable(
                ContextCompat.getDrawable(this, R.drawable.ic_visibility_off)
            )
        } else {
            // Show password
            etPassword.transformationMethod = HideReturnsTransformationMethod.getInstance()
            btnPasswordVisibility.setImageDrawable(
                ContextCompat.getDrawable(this, R.drawable.ic_visibility)
            )
        }

        // Move cursor to the end of the text
        etPassword.setSelection(etPassword.text.length)
    }

    private fun loginUser(email: String, password: String) {
        btnLogin.isEnabled = false
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (user?.isEmailVerified == true) {
                        // Retrieve username from database
                        database.child(user.uid).addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                val username = snapshot.child("username").getValue(String::class.java)

                                // Save user token and username
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
        // Check if user is already logged in
        val currentUser = auth.currentUser
        if (currentUser != null && currentUser.isEmailVerified) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}