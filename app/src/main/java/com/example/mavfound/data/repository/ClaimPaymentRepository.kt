package com.example.mavfound.data.repository

import com.example.mavfound.data.model.ClaimRecord
import com.example.mavfound.data.model.PaymentRecord

interface ClaimPaymentRepository {
    suspend fun createPaymentAndClaimRecords(
        listingId: String,
        claimantUserId: String,
        amountCents: Int,
        cardNumber: String,
        billingZip: String,
        verified: Boolean,
        exchangeCode: String
    ): ClaimCreationResult

    suspend fun completeClaimByExchangeCode(exchangeCode: String): ClaimRecord?
}

data class ClaimCreationResult(
    val paymentRecord: PaymentRecord,
    val claimRecord: ClaimRecord
)
