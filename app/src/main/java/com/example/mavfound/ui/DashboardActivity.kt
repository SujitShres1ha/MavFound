package com.example.mavfound.ui

import android.content.Context
import android.content.Intent
import android.graphics.drawable.AnimationDrawable
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.example.mavfound.R
import com.example.mavfound.utils.ThemeManager
import com.google.android.material.card.MaterialCardView

class DashboardActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeManager.applySavedTheme(this)
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

        val drawerLayout = findViewById<DrawerLayout>(R.id.dashboardDrawer)
        val cardFindMode = findViewById<MaterialCardView>(R.id.cardFindMode)
        val cardLostMode = findViewById<MaterialCardView>(R.id.cardLostMode)
        val cardMyPostings = findViewById<MaterialCardView>(R.id.cardMyPostings)
        val btnOpenDrawer = findViewById<ImageView>(R.id.btnOpenDrawer)
        val navHome = findViewById<android.view.View>(R.id.navHome)
        val navListings = findViewById<android.view.View>(R.id.navListings)
        val navOrders = findViewById<android.view.View>(R.id.navOrders)
        val profileEntry = findViewById<android.view.View>(R.id.profileEntry)
        val btnLogout = findViewById<com.google.android.material.button.MaterialButton>(R.id.btnLogout)
        val switchTheme = findViewById<com.google.android.material.materialswitch.MaterialSwitch>(R.id.switchTheme)
        val tvWelcomeMessage = findViewById<TextView>(R.id.tvWelcomeMessage)
        val tvSidebarUserName = findViewById<TextView>(R.id.tvSidebarUserName)

        val sharedPrefs = getSharedPreferences("MavFoundPrefs", Context.MODE_PRIVATE)
        val userName = sharedPrefs.getString("CURRENT_USER_NAME", null) ?: "Maverick"
        tvWelcomeMessage.text = "Hello, $userName!"
        tvSidebarUserName.text = userName
        switchTheme.isChecked = ThemeManager.isDarkMode(this)

        btnOpenDrawer.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        navHome.setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.START)
        }

        navListings.setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.START)
            startActivity(Intent(this, MyListingsActivity::class.java))
        }

        navOrders.setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.START)
            startActivity(Intent(this, OrdersActivity::class.java))
        }

        profileEntry.setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.START)
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        switchTheme.setOnCheckedChangeListener { _, isChecked ->
            ThemeManager.setDarkMode(this, isChecked)
            drawerLayout.closeDrawer(GravityCompat.START)
            recreate()
        }

        cardFindMode.setOnClickListener {
            val intent = Intent(this, FeedActivity::class.java)
            startActivity(intent)
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
            sharedPrefs.edit().apply {
                remove("CURRENT_USER_ID")
                remove("CURRENT_USER_NAME")
                remove("IS_ADMIN")
                apply()
            }

            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}
