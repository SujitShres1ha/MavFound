package com.example.mavfound.ui

import android.content.Context
import android.content.Intent
import android.graphics.drawable.AnimationDrawable
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.example.mavfound.R
import com.google.android.material.card.MaterialCardView

class DashboardActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        // Initialize the animated gradient background
        val rootLayout = findViewById<CoordinatorLayout>(R.id.dashboardRootLayout)
        val animationDrawable = rootLayout.background as AnimationDrawable

        // Set fade durations for ultra-smooth transitions
        animationDrawable.setEnterFadeDuration(2000)
        animationDrawable.setExitFadeDuration(4000)

        // Start the continuous animation loop
        animationDrawable.start()

        val cardFindMode = findViewById<MaterialCardView>(R.id.cardFindMode)
        val cardLostMode = findViewById<MaterialCardView>(R.id.cardLostMode)
        val cardMyPostings = findViewById<MaterialCardView>(R.id.cardMyPostings)
        val btnLogout = findViewById<Button>(R.id.btnLogout)

        cardFindMode.setOnClickListener {
            // Keep commented out until the Searcher Lead builds this screen
            // val intent = Intent(this, FeedActivity::class.java)
            // startActivity(intent)
        }

        cardLostMode.setOnClickListener {
            val intent = Intent(this, PostItemActivity::class.java)
            startActivity(intent)
        }

        cardMyPostings.setOnClickListener {
            val intent = Intent(this, MyListingsActivity::class.java)
            startActivity(intent)
        }

        btnLogout.setOnClickListener {
            val sharedPrefs = getSharedPreferences("MavFoundPrefs", Context.MODE_PRIVATE)
            sharedPrefs.edit().clear().apply()

            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}
