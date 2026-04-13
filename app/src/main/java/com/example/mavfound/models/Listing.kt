package com.example.mavfound.models

data class Listing(
    val listingId: Int = 0,
    val listerId: Int,
    val title: String,
    val description: String,
    val location: String,
    val dateTime: String,
    val imagePath: String? = null,
    val rewardAmount: Double,
    val securityQuestion: String,
    val securityAnswer: String,
    val status: String = "Available"
)