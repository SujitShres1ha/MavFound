package com.example.mavfound.feature.payment

sealed interface PaymentEvent {
    data object SubmitFinderListing : PaymentEvent
    data object BackToFinderForm : PaymentEvent
    data object StartClaim : PaymentEvent
    data object SubmitClaimAnswers : PaymentEvent
    data object SubmitPayment : PaymentEvent
    data object OpenFinderVerification : PaymentEvent
    data object ReturnToExchangeCode : PaymentEvent
    data object VerifyFinderCode : PaymentEvent
    data class CardNumberChanged(val value: String) : PaymentEvent
    data class CardNameChanged(val value: String) : PaymentEvent
    data class ExpiryChanged(val value: String) : PaymentEvent
    data class CvvChanged(val value: String) : PaymentEvent
    data class StreetChanged(val value: String) : PaymentEvent
    data class CityChanged(val value: String) : PaymentEvent
    data class StateChanged(val value: String) : PaymentEvent
    data class ZipChanged(val value: String) : PaymentEvent
    data class FinderCodeChanged(val value: String) : PaymentEvent
    data class SecurityAnswerOneChanged(val value: String) : PaymentEvent
    data class SecurityAnswerTwoChanged(val value: String) : PaymentEvent
    data class SecurityAnswerThreeChanged(val value: String) : PaymentEvent
    data class ProductTitleChanged(val value: String) : PaymentEvent
    data class ListingLocationChanged(val value: String) : PaymentEvent
    data class ListingDateChanged(val value: String) : PaymentEvent
    data class ListingTimeChanged(val value: String) : PaymentEvent
    data class ListingDescriptionChanged(val value: String) : PaymentEvent
    data class RewardAmountChanged(val value: String) : PaymentEvent
    data class SecurityQuestionOneChanged(val value: String) : PaymentEvent
    data class SecurityQuestionTwoChanged(val value: String) : PaymentEvent
    data class SecurityQuestionThreeChanged(val value: String) : PaymentEvent
}
