package com.example.mavfound.feature.payment

import com.example.mavfound.data.model.ClaimRecord
import com.example.mavfound.data.model.PaymentRecord

data class PaymentUiState(
    val productTitle: String = "TI-84 Plus Calculator",
    val listingLocation: String = "UTA Central Library, 702 Planetarium Pl, Arlington, TX 76019",
    val listingDate: String = "April 12, 2026",
    val listingTime: String = "2:35 PM",
    val listingDescription: String = "Black graphing calculator found on a second-floor study table near the printers.",
    val rewardAmountLabel: String = "$10.00",
    val securityQuestionOne: String = "What is the color of the product?",
    val securityQuestionTwo: String = "What does it say at the end of the calculator cover?",
    val securityQuestionThree: String = "Which colors are marked at the top of the calculator?",
    val securityAnswerOne: String = "",
    val securityAnswerTwo: String = "",
    val securityAnswerThree: String = "",
    val cardNumber: String = "",
    val cardName: String = "",
    val expiry: String = "",
    val cvv: String = "",
    val street: String = "",
    val city: String = "",
    val state: String = "",
    val zip: String = "",
    val stage: PaymentStage = PaymentStage.FINDER_FORM,
    val isProcessing: Boolean = false,
    val finderEnteredCode: String = "",
    val exchangeCode: String? = null,
    val paymentRecord: PaymentRecord? = null,
    val claimRecord: ClaimRecord? = null,
    val fieldErrors: PaymentFieldErrors = PaymentFieldErrors(),
    val finderListingError: String? = null,
    val securityQuestionError: String? = null,
    val finderCodeError: String? = null,
    val finderVerificationMessage: String? = null,
    val statusMessage: String? = null
)

enum class PaymentStage {
    FINDER_FORM,
    LISTING,
    SECURITY_QUESTIONS,
    FORM,
    PROCESSING,
    EXCHANGE_CODE,
    FINDER_VERIFICATION
}

data class PaymentFieldErrors(
    val cardNumber: String? = null,
    val cardName: String? = null,
    val expiry: String? = null,
    val cvv: String? = null,
    val street: String? = null,
    val city: String? = null,
    val state: String? = null,
    val zip: String? = null
) {
    fun hasErrors(): Boolean {
        return listOf(cardNumber, cardName, expiry, cvv, street, city, state, zip).any { it != null }
    }
}
