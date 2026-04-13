package com.example.mavfound.models

data class Payment(
    val paymentId: Int = 0,
    val claimId: Int,
    val amount: Double,
    val paymentDate: String,
    val status: String
)