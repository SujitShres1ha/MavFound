package com.example.mavfound.data.repository

import com.example.mavfound.data.model.ClaimRecord
import com.example.mavfound.data.model.ListingStatus
import com.example.mavfound.data.model.PaymentRecord
import kotlinx.coroutines.delay
import java.util.UUID

class FakeClaimPaymentRepository : ClaimPaymentRepository {

    private val payments = mutableListOf<PaymentRecord>()
    private val claims = mutableListOf<ClaimRecord>()

    override suspend fun createPaymentAndClaimRecords(
        listingId: String,
        claimantUserId: String,
        amountCents: Int,
        cardNumber: String,
        billingZip: String,
        verified: Boolean,
        exchangeCode: String
    ): ClaimCreationResult {
        delay(300)

        val now = System.currentTimeMillis()
        val paymentRecord = PaymentRecord(
            id = UUID.randomUUID().toString(),
            listingId = listingId,
            claimantUserId = claimantUserId,
            amountCents = amountCents,
            paymentMethodLastFour = cardNumber.takeLast(4),
            billingZip = billingZip,
            verified = verified,
            createdAtMillis = now
        )

        val claimRecord = ClaimRecord(
            id = UUID.randomUUID().toString(),
            listingId = listingId,
            claimantUserId = claimantUserId,
            paymentId = paymentRecord.id,
            exchangeCode = exchangeCode,
            status = ListingStatus.CLAIMED,
            createdAtMillis = now
        )

        payments += paymentRecord
        claims += claimRecord

        return ClaimCreationResult(
            paymentRecord = paymentRecord,
            claimRecord = claimRecord
        )
    }

    override suspend fun completeClaimByExchangeCode(exchangeCode: String): ClaimRecord? {
        delay(150)

        val index = claims.indexOfFirst { it.exchangeCode == exchangeCode }
        if (index == -1) return null

        val updated = claims[index].copy(
            status = ListingStatus.COMPLETED,
            completedAtMillis = System.currentTimeMillis()
        )
        claims[index] = updated
        return updated
    }
}
