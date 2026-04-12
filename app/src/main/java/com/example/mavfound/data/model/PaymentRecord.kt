package com.example.mavfound.data.model

data class PaymentRecord(
    val id: String,
    val listingId: String,
    val claimantUserId: String,
    val amountCents: Int,
    val paymentMethodLastFour: String,
    val billingZip: String,
    val verified: Boolean,
    val createdAtMillis: Long
)
