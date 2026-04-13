package com.example.mavfound.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.mavfound.R

class AdminDashboardActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_dashboard)

        val btnReviewReports = findViewById<Button>(R.id.btnReviewReports)
        val btnManageUsers = findViewById<Button>(R.id.btnManageUsers)
        val btnAdminLogout = findViewById<Button>(R.id.btnLogout)

        btnReviewReports.setOnClickListener {
            // TODO: Route to Reports RecyclerView
        }

        btnManageUsers.setOnClickListener {
            // TODO: Route to User Management screen
        }


        btnAdminLogout.setOnClickListener {
            val sharedPrefs = getSharedPreferences("MavFoundPrefs", Context.MODE_PRIVATE)
            sharedPrefs.edit().clear().apply()

            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}