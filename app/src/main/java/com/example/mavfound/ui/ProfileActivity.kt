package com.example.mavfound.ui

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.mavfound.R
import com.example.mavfound.utils.AuthManager
import com.example.mavfound.utils.ThemeManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class ProfileActivity : AppCompatActivity() {
    private lateinit var authManager: AuthManager

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeManager.applySavedTheme(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        authManager = AuthManager(this)

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.profileToolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = ""
        toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        val prefs = getSharedPreferences("MavFoundPrefs", Context.MODE_PRIVATE)
        val currentUserId = prefs.getInt("CURRENT_USER_ID", -1)
        if (currentUserId == -1) {
            finish()
            return
        }

        val currentUser = authManager.getUserById(currentUserId)
        if (currentUser == null) {
            finish()
            return
        }

        val etFirstName = findViewById<TextInputEditText>(R.id.etProfileFirstName)
        val etEmail = findViewById<TextInputEditText>(R.id.etProfileEmail)
        val etNewPassword = findViewById<TextInputEditText>(R.id.etProfileNewPassword)
        val etCurrentPassword = findViewById<TextInputEditText>(R.id.etProfileCurrentPassword)
        val btnSave = findViewById<MaterialButton>(R.id.btnSaveProfile)

        etFirstName.setText(currentUser.name)
        etEmail.setText(currentUser.email)

        btnSave.setOnClickListener {
            val name = etFirstName.text?.toString()?.trim().orEmpty()
            val email = etEmail.text?.toString()?.trim().orEmpty()
            val newPassword = etNewPassword.text?.toString()?.trim().orEmpty()
            val currentPassword = etCurrentPassword.text?.toString()?.trim().orEmpty()

            if (name.isEmpty() || email.isEmpty() || currentPassword.isEmpty()) {
                Toast.makeText(this, "Fill in name, email, and current password.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (newPassword.isNotEmpty() && newPassword.length < 8) {
                Toast.makeText(this, "New password must be at least 8 characters.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            when (authManager.updateUserProfile(currentUserId, name, email, newPassword, currentPassword)) {
                AuthManager.UpdateProfileResult.Success -> {
                    prefs.edit().putString("CURRENT_USER_NAME", name).apply()
                    Toast.makeText(this, "Profile updated.", Toast.LENGTH_SHORT).show()
                    finish()
                }
                AuthManager.UpdateProfileResult.InvalidCurrentPassword -> {
                    Toast.makeText(this, "Current password is incorrect.", Toast.LENGTH_LONG).show()
                }
                AuthManager.UpdateProfileResult.EmailAlreadyUsed -> {
                    Toast.makeText(this, "That email is already registered.", Toast.LENGTH_LONG).show()
                }
                else -> {
                    Toast.makeText(this, "Could not update profile.", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}
