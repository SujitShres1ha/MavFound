package com.example.mavfound.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.mavfound.R
import com.example.mavfound.utils.AuthManager
import com.example.mavfound.utils.ThemeManager
import com.google.android.material.textfield.TextInputEditText

class LoginActivity : AppCompatActivity() {

    private lateinit var authManager: AuthManager

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeManager.applySavedTheme(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        authManager = AuthManager(this)

        val etEmail = findViewById<TextInputEditText>(R.id.etEmail)
        val etPassword = findViewById<TextInputEditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val tvGoToRegister = findViewById<TextView>(R.id.tvGoToRegister)

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val loggedInUser = authManager.loginUser(email, password)

            if (loggedInUser != null) {
                Toast.makeText(this, "Welcome back, ${loggedInUser.name}!", Toast.LENGTH_SHORT).show()

                val sharedPrefs = getSharedPreferences("MavFoundPrefs", Context.MODE_PRIVATE)
                sharedPrefs.edit().apply {
                    putInt("CURRENT_USER_ID", loggedInUser.userId)
                    putString("CURRENT_USER_NAME", loggedInUser.name)
                    putBoolean("IS_ADMIN", loggedInUser.isAdmin)
                    apply()
                }
                if (loggedInUser.isAdmin) {
                    val intent = Intent(this, AdminDashboardActivity::class.java)
                    startActivity(intent)
                } else {
                    val intent = Intent(this, DashboardActivity::class.java)
                    startActivity(intent)
                }
                finish()
            } else {
                Toast.makeText(this, "Invalid email or password", Toast.LENGTH_LONG).show()
            }
        }

        tvGoToRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}
