package com.example.mavfound.ui

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.mavfound.R
import com.example.mavfound.utils.AuthManager
import com.google.android.material.textfield.TextInputEditText

class RegisterActivity : AppCompatActivity() {

    private lateinit var authManager: AuthManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        authManager = AuthManager(this)

        val etName = findViewById<TextInputEditText>(R.id.etName)
        val etEmail = findViewById<TextInputEditText>(R.id.etEmail)
        val etPassword = findViewById<TextInputEditText>(R.id.etPassword)
        val etConfirmPassword = findViewById<TextInputEditText>(R.id.etConfirmPassword)
        val cbTerms = findViewById<CheckBox>(R.id.cbTerms)
        val btnRegister = findViewById<Button>(R.id.btnRegister)
        val tvGoToLogin = findViewById<TextView>(R.id.tvGoToLogin)
        val tvPasswordHint = findViewById<TextView>(R.id.tvPasswordHint)

        val bars = arrayOf(
            findViewById<View>(R.id.strengthBar1),
            findViewById<View>(R.id.strengthBar2),
            findViewById<View>(R.id.strengthBar3),
            findViewById<View>(R.id.strengthBar4)
        )

        etPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updatePasswordStrength(s.toString(), bars, tvPasswordHint)
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        btnRegister.setOnClickListener {
            val name = etName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val confirmPassword = etConfirmPassword.text.toString().trim()

            if (name.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!cbTerms.isChecked) {
                Toast.makeText(this, "Please agree to the Terms of Service", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val isSuccess = authManager.registerUser(name, email, password)

            if (isSuccess) {
                Toast.makeText(this, "Account created successfully!", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "Error: Email is already registered", Toast.LENGTH_LONG).show()
            }
        }

        tvGoToLogin.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun updatePasswordStrength(password: String, bars: Array<View>, hint: TextView) {
        var strength = 0
        var message = "Weak — add numbers or symbols"

        if (password.isNotEmpty()) strength++
        if (password.length >= 6) {
            strength++
            message = "Fair — add uppercase letters"
        }
        if (password.length >= 8 && password.any { it.isUpperCase() }) {
            strength++
            message = "Good — almost there"
        }
        if (password.length >= 10 && password.any { !it.isLetterOrDigit() }) {
            strength++
            message = "Strong password"
        }

        val color = when (strength) {
            1 -> Color.RED
            2 -> Color.parseColor("#F59E0B") // colorWarning
            3 -> Color.parseColor("#0064B1") // colorPrimary
            4 -> Color.GREEN
            else -> Color.LTGRAY
        }

        hint.text = if (password.isEmpty()) "" else message
        hint.setTextColor(color)

        for (i in bars.indices) {
            if (i < strength) {
                bars[i].backgroundTintList = ColorStateList.valueOf(color)
            } else {
                bars[i].backgroundTintList = ColorStateList.valueOf(Color.parseColor("#E0E0E0"))
            }
        }
    }
}