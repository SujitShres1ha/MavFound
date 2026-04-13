package com.example.mavfound.models

data class Claim(
    val claimId: Int = 0,
    val listingId: Int,
    val searcherId: Int,
    val claimDate: String,
    val exchangeCode: String? = null,
    val status: String = "Pending"
)