package com.example.mavfound.feature.payment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.mavfound.data.repository.ClaimPaymentRepository
import com.example.mavfound.domain.exchange.ExchangeCodeGenerator
import java.time.YearMonth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class PaymentViewModel(
    private val repository: ClaimPaymentRepository,
    private val exchangeCodeGenerator: ExchangeCodeGenerator
) : ViewModel() {

    private val _uiState = MutableStateFlow(PaymentUiState())
    val uiState: StateFlow<PaymentUiState> = _uiState.asStateFlow()

    fun onEvent(event: PaymentEvent) {
        when (event) {
            PaymentEvent.SubmitFinderListing -> submitFinderListing()
            PaymentEvent.BackToFinderForm -> updateField {
                copy(stage = PaymentStage.FINDER_FORM, finderListingError = null, statusMessage = null)
            }
            PaymentEvent.StartClaim -> updateField {
                copy(stage = PaymentStage.SECURITY_QUESTIONS, finderListingError = null, securityQuestionError = null, statusMessage = null)
            }
            PaymentEvent.SubmitClaimAnswers -> submitClaimAnswers()
            is PaymentEvent.ProductTitleChanged -> updateField {
                copy(productTitle = event.value, finderListingError = null, statusMessage = null)
            }
            is PaymentEvent.ListingLocationChanged -> updateField {
                copy(listingLocation = event.value, finderListingError = null, statusMessage = null)
            }
            is PaymentEvent.ListingDateChanged -> updateField {
                copy(listingDate = event.value, finderListingError = null, statusMessage = null)
            }
            is PaymentEvent.ListingTimeChanged -> updateField {
                copy(listingTime = event.value, finderListingError = null, statusMessage = null)
            }
            is PaymentEvent.ListingDescriptionChanged -> updateField {
                copy(listingDescription = event.value, finderListingError = null, statusMessage = null)
            }
            is PaymentEvent.RewardAmountChanged -> updateField {
                copy(rewardAmountLabel = event.value, finderListingError = null, statusMessage = null)
            }
            is PaymentEvent.SecurityQuestionOneChanged -> updateField {
                copy(securityQuestionOne = event.value, finderListingError = null, securityQuestionError = null, statusMessage = null)
            }
            is PaymentEvent.SecurityQuestionTwoChanged -> updateField {
                copy(securityQuestionTwo = event.value, finderListingError = null, securityQuestionError = null, statusMessage = null)
            }
            is PaymentEvent.SecurityQuestionThreeChanged -> updateField {
                copy(securityQuestionThree = event.value, finderListingError = null, securityQuestionError = null, statusMessage = null)
            }
            is PaymentEvent.CardNumberChanged -> updateField { copy(cardNumber = event.value) }
            is PaymentEvent.CardNameChanged -> updateField { copy(cardName = event.value) }
            is PaymentEvent.ExpiryChanged -> updateField { copy(expiry = event.value) }
            is PaymentEvent.CvvChanged -> updateField { copy(cvv = event.value) }
            is PaymentEvent.StreetChanged -> updateField { copy(street = event.value) }
            is PaymentEvent.CityChanged -> updateField { copy(city = event.value) }
            is PaymentEvent.StateChanged -> updateField { copy(state = event.value) }
            is PaymentEvent.ZipChanged -> updateField { copy(zip = event.value) }
            is PaymentEvent.SecurityAnswerOneChanged -> updateField {
                copy(securityAnswerOne = event.value, securityQuestionError = null, statusMessage = null)
            }
            is PaymentEvent.SecurityAnswerTwoChanged -> updateField {
                copy(securityAnswerTwo = event.value, securityQuestionError = null, statusMessage = null)
            }
            is PaymentEvent.SecurityAnswerThreeChanged -> updateField {
                copy(securityAnswerThree = event.value, securityQuestionError = null, statusMessage = null)
            }
            is PaymentEvent.FinderCodeChanged -> updateField {
                copy(
                    finderEnteredCode = event.value.uppercase(),
                    finderCodeError = null,
                    finderVerificationMessage = null
                )
            }
            PaymentEvent.SubmitPayment -> submitPayment()
            PaymentEvent.OpenFinderVerification -> updateField {
                copy(
                    stage = PaymentStage.FINDER_VERIFICATION,
                    finderCodeError = null,
                    finderVerificationMessage = null
                )
            }
            PaymentEvent.ReturnToExchangeCode -> updateField {
                copy(
                    stage = PaymentStage.EXCHANGE_CODE,
                    finderCodeError = null,
                    finderVerificationMessage = null
                )
            }
            PaymentEvent.VerifyFinderCode -> completeClaim()
        }
    }

    private fun submitFinderListing() {
        val state = _uiState.value
        val isComplete = listOf(
            state.productTitle,
            state.listingLocation,
            state.listingDate,
            state.listingTime,
            state.listingDescription,
            state.rewardAmountLabel,
            state.securityQuestionOne,
            state.securityQuestionTwo,
            state.securityQuestionThree
        ).all { it.isNotBlank() }

        if (!isComplete) {
            updateField {
                copy(
                    finderListingError = "Fill out the item details, found location, time, reward, and all verification questions.",
                    statusMessage = null
                )
            }
            return
        }

        updateField {
            copy(
                stage = PaymentStage.LISTING,
                finderListingError = null,
                statusMessage = "Listing created. This is the claimant-facing preview."
            )
        }
    }

    private fun submitClaimAnswers() {
        val state = _uiState.value
        val answersFilled = listOf(
            state.securityAnswerOne,
            state.securityAnswerTwo,
            state.securityAnswerThree
        ).all { it.isNotBlank() }

        if (!answersFilled) {
            updateField {
                copy(
                    securityQuestionError = "Answer all three finder questions before continuing.",
                    statusMessage = null
                )
            }
            return
        }

        val answerOneCorrect = normalizeAnswer(state.securityAnswerOne) == "black"
        val answerTwoCorrect = normalizeAnswer(state.securityAnswerTwo) == "texas instruments"
        val answerThreeCorrect = normalizeAnswer(state.securityAnswerThree) in setOf(
            "green yellow red",
            "red yellow green",
            "yellow green red",
            "yellow red green",
            "green red yellow",
            "red green yellow"
        )

        if (answerOneCorrect && answerTwoCorrect && answerThreeCorrect) {
            updateField {
                copy(
                    stage = PaymentStage.FORM,
                    finderListingError = null,
                    securityQuestionError = null,
                    statusMessage = "Answers verified. Continue to payment."
                )
            }
        } else {
            updateField {
                copy(
                    securityQuestionError = "One or more answers do not match the finder's verification details.",
                    statusMessage = null
                )
            }
        }
    }

    private fun submitPayment() {
        val state = _uiState.value
        val validationErrors = validateForm(state)
        if (validationErrors.hasErrors()) {
            updateField {
                copy(
                    fieldErrors = validationErrors,
                    finderCodeError = null,
                    finderListingError = null,
                    securityQuestionError = null,
                    statusMessage = "Fix the highlighted fields before continuing."
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    stage = PaymentStage.PROCESSING,
                    isProcessing = true,
                    fieldErrors = PaymentFieldErrors(),
                    securityQuestionError = null,
                    statusMessage = "Processing payment..."
                )
            }

            val code = exchangeCodeGenerator.generate()
            val result = repository.createPaymentAndClaimRecords(
                listingId = DEMO_LISTING_ID,
                claimantUserId = DEMO_USER_ID,
                amountCents = REWARD_AMOUNT_CENTS,
                cardNumber = state.cardNumber,
                billingZip = state.zip,
                verified = true,
                exchangeCode = code
            )
            delay(1200)

            _uiState.update {
                it.copy(
                    stage = PaymentStage.EXCHANGE_CODE,
                    isProcessing = false,
                    exchangeCode = code,
                    paymentRecord = result.paymentRecord,
                    claimRecord = result.claimRecord,
                    finderEnteredCode = "",
                    finderCodeError = null,
                    finderVerificationMessage = null,
                    statusMessage = "Listing marked Claimed. Exchange code ready."
                )
            }
        }
    }

    private fun completeClaim() {
        val code = _uiState.value.finderEnteredCode.trim()
        if (code.isBlank()) {
            updateField {
                copy(
                    finderCodeError = "Finder must enter the exchange code.",
                    finderVerificationMessage = null
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    stage = PaymentStage.PROCESSING,
                    isProcessing = true,
                    finderCodeError = null,
                    finderVerificationMessage = null,
                    statusMessage = "Finder is verifying the exchange code..."
                )
            }
            val completedClaim = repository.completeClaimByExchangeCode(code)
            _uiState.update {
                it.copy(
                    stage = PaymentStage.FINDER_VERIFICATION,
                    isProcessing = false,
                    claimRecord = completedClaim ?: it.claimRecord,
                    finderVerificationMessage = if (completedClaim != null) {
                        "Code verified. Hand over the item and close the handoff."
                    } else {
                        "Invalid code. Ask the claimant to show the exact exchange code."
                    },
                    finderCodeError = if (completedClaim == null) {
                        "Code did not match the active claim."
                    } else {
                        null
                    },
                    statusMessage = if (completedClaim != null) {
                        "Listing marked Completed."
                    } else {
                        "Exchange code verification failed."
                    }
                )
            }
        }
    }

    private fun updateField(update: PaymentUiState.() -> PaymentUiState) {
        _uiState.update(update)
    }

    private fun validateForm(state: PaymentUiState): PaymentFieldErrors {
        val cardDigits = state.cardNumber.filter(Char::isDigit)
        val expiryValue = state.expiry.trim()
        val zipValue = state.zip.trim()
        val cvvValue = state.cvv.trim()

        return PaymentFieldErrors(
            cardNumber = when {
                state.cardNumber.isBlank() -> "Card number is required."
                !CARD_NUMBER_REGEX.matches(cardDigits) -> "Card number must be exactly 16 digits."
                else -> null
            },
            cardName = when {
                state.cardName.isBlank() -> "Card holder name is required."
                else -> null
            },
            expiry = when {
                expiryValue.isBlank() -> "Expiration date is required."
                !EXPIRY_REGEX.matches(expiryValue) -> "Use MM/YY format."
                isExpired(expiryValue) -> "Card is expired."
                else -> null
            },
            cvv = when {
                cvvValue.isBlank() -> "CVV is required."
                !CVV_REGEX.matches(cvvValue) -> "CVV must be 3 or 4 digits."
                else -> null
            },
            street = when {
                state.street.isBlank() -> "Street address is required."
                else -> null
            },
            city = when {
                state.city.isBlank() -> "City is required."
                else -> null
            },
            state = when {
                state.state.isBlank() -> "State is required."
                else -> null
            },
            zip = when {
                zipValue.isBlank() -> "ZIP code is required."
                !ZIP_REGEX.matches(zipValue) -> "ZIP code must be 5 digits."
                else -> null
            }
        )
    }

    private fun isExpired(expiry: String): Boolean {
        val match = EXPIRY_REGEX.matchEntire(expiry) ?: return true
        val month = match.groupValues[1].toInt()
        val year = 2000 + match.groupValues[2].toInt()
        val expiryMonth = YearMonth.of(year, month)
        return YearMonth.now().isAfter(expiryMonth)
    }

    private fun normalizeAnswer(value: String): String {
        return value
            .trim()
            .lowercase()
            .replace(Regex("[^a-z0-9 ]"), " ")
            .replace(Regex("\\s+"), " ")
    }

    private companion object {
        const val DEMO_LISTING_ID = "listing-001"
        const val DEMO_USER_ID = "user-001"
        const val REWARD_AMOUNT_CENTS = 1000
        val CARD_NUMBER_REGEX = Regex("^\\d{16}$")
        val EXPIRY_REGEX = Regex("^(0[1-9]|1[0-2])/(\\d{2})$")
        val CVV_REGEX = Regex("^\\d{3,4}$")
        val ZIP_REGEX = Regex("^\\d{5}$")
    }
}

class PaymentViewModelFactory(
    private val repository: ClaimPaymentRepository,
    private val exchangeCodeGenerator: ExchangeCodeGenerator
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PaymentViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PaymentViewModel(repository, exchangeCodeGenerator) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
