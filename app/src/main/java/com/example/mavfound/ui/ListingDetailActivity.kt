package com.example.mavfound.ui

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.mavfound.R
import com.example.mavfound.database.DatabaseHelper

class ListingDetailActivity : AppCompatActivity() {
    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_listing_detail)

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "" // Empty so it doesn't block the image

        dbHelper = DatabaseHelper(this)
        val listingId = intent.getIntExtra("LISTING_ID", -1)

        if (listingId != -1) {
            val item = dbHelper.getListingById(listingId)
            item?.let {
                findViewById<TextView>(R.id.tvDetailTitle).text = it.title
                findViewById<TextView>(R.id.tvDetailDesc).text = it.description
                findViewById<TextView>(R.id.tvDetailLocation).text = "Found at: ${it.location}"
                findViewById<TextView>(R.id.tvDetailReward).text = String.format("Reward: $%.2f", it.rewardAmount)

                // LOAD HERO PHOTO
                val ivHero = findViewById<ImageView>(R.id.ivDetailHero)
                if (!it.imagePath.isNullOrEmpty()) {
                    Glide.with(this).load(it.imagePath).into(ivHero)
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}