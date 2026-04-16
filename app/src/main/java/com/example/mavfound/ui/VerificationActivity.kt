package com.example.mavfound.ui

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.AnimationDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.Animation
import android.view.animation.TranslateAnimation
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.example.mavfound.R
import com.example.mavfound.database.DatabaseHelper
import com.google.android.material.card.MaterialCardView

class VerificationActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verification)

        dbHelper = DatabaseHelper(this)

        // Animated Background
        val rootLayout = findViewById<CoordinatorLayout>(R.id.verifyRootLayout)
        val animationDrawable = rootLayout.background as AnimationDrawable
        animationDrawable.setEnterFadeDuration(2000)
        animationDrawable.setExitFadeDuration(4000)
        animationDrawable.start()

        val cardView = findViewById<MaterialCardView>(R.id.verificationCard)
        val tvVerifyTitle = findViewById<TextView>(R.id.tvVerifyTitle)
        val tvSecurityQuestion = findViewById<TextView>(R.id.tvSecurityQuestion)
        val etAnswer = findViewById<EditText>(R.id.etAnswer)
        val btnSubmitAnswer = findViewById<Button>(R.id.btnSubmitAnswer)
        val tvResult = findViewById<TextView>(R.id.tvResult)
        val icon = findViewById<ImageView>(R.id.ivSecurityIcon)

        // Retrieve intent data
        val itemTitle = intent.getStringExtra("itemTitle") ?: "Item"
        val securityQuestion = intent.getStringExtra("securityQuestion") ?: "No question available."
        val listingId = intent.getIntExtra("LISTING_ID", -1)

        // Retrieve current user ID for the claim
        val sharedPrefs = getSharedPreferences("MavFoundPrefs", Context.MODE_PRIVATE)
        val currentUserId = sharedPrefs.getInt("CURRENT_USER_ID", -1)

        tvVerifyTitle.text = "Claiming: $itemTitle"
        tvSecurityQuestion.text = "\"$securityQuestion\""

        btnSubmitAnswer.setOnClickListener {
            val userAnswer = etAnswer.text.toString().trim()

            if (userAnswer.isEmpty()) {
                tvResult.text = "Please enter a description."
                shakeAnimation(cardView)
                return@setOnClickListener
            }

            if (currentUserId == -1 || listingId == -1) {
                tvResult.text = "Error identifying user or item. Please try again."
                return@setOnClickListener
            }

            // LOGIC CHANGE: Save claim to database for the poster to review
            val result = dbHelper.insertClaim(listingId, currentUserId, userAnswer)

            if (result != -1L) {
                // SUCCESS: Notify user that the "Handshake" request is sent
                tvResult.setTextColor(Color.parseColor("#0064B1")) // MavFound Blue
                tvResult.text = "Claim submitted! The poster will review your proof."

                // Visual feedback: Change icon to email to signify a message sent
                icon.setImageResource(R.drawable.ic_email)
                icon.setColorFilter(Color.parseColor("#0064B1"))

                btnSubmitAnswer.isEnabled = false
                etAnswer.isEnabled = false

                // Return to previous screen after a short delay
                Handler(Looper.getMainLooper()).postDelayed({
                    finish()
                }, 3000)
            } else {
                tvResult.setTextColor(Color.parseColor("#EF4444")) // Red
                tvResult.text = "Failed to submit claim. Database error."
                shakeAnimation(cardView)
            }
        }
    }

    private fun shakeAnimation(view: android.view.View) {
        val shake = TranslateAnimation(0f, 20f, 0f, 0f)
        shake.duration = 50
        shake.repeatMode = Animation.REVERSE
        shake.repeatCount = 5
        view.startAnimation(shake)
    }
}