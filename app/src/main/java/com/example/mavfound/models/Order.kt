package com.example.mavfound.models

data class Order(
    val orderDbId: Int = 0,
    val orderId: String,
    val listingId: Int,
    val buyerId: Int,
    val listingTitle: String,
    val amount: Double,
    val paymentDate: String,
    val status: String = "Pending",
    val handoffCode: String
)
