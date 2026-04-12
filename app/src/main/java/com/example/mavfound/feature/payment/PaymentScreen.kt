package com.example.mavfound.feature.payment

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.mavfound.data.model.ListingStatus

@Composable
fun PaymentScreen(
    viewModel: PaymentViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    PaymentScreen(
        uiState = uiState,
        onEvent = viewModel::onEvent
    )
}

@Composable
private fun PaymentScreen(
    uiState: PaymentUiState,
    onEvent: (PaymentEvent) -> Unit
) {
    when (uiState.stage) {
        PaymentStage.FINDER_FORM -> FinderListingFormScreen(uiState = uiState, onEvent = onEvent)
        PaymentStage.LISTING -> ListingScreen(uiState = uiState, onEvent = onEvent)
        PaymentStage.SECURITY_QUESTIONS -> SecurityQuestionsScreen(uiState = uiState, onEvent = onEvent)
        PaymentStage.FORM -> PaymentFormScreen(uiState = uiState, onEvent = onEvent)
        PaymentStage.PROCESSING -> PaymentProcessingScreen(uiState = uiState)
        PaymentStage.EXCHANGE_CODE -> ExchangeCodeScreen(uiState = uiState, onEvent = onEvent)
        PaymentStage.FINDER_VERIFICATION -> FinderVerificationScreen(uiState = uiState, onEvent = onEvent)
    }
}

@Composable
private fun FinderListingFormScreen(
    uiState: PaymentUiState,
    onEvent: (PaymentEvent) -> Unit
) {
    val bg = Brush.verticalGradient(
        listOf(
            MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f),
            MaterialTheme.colorScheme.surface,
            MaterialTheme.colorScheme.surfaceContainerHighest
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bg)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("Finder Listing Form", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text("Create the found-item listing that claimants will see.", style = MaterialTheme.typography.bodyLarge)

        Card(shape = RoundedCornerShape(24.dp), modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .border(
                            width = 2.dp,
                            color = MaterialTheme.colorScheme.outline,
                            shape = RoundedCornerShape(18.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("UPLOAD", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.ExtraBold)
                        Text("Image placeholder", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Text(
                    text = "Image upload is mocked for now. Replace this box with a real picker later.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = uiState.productTitle,
                    onValueChange = { onEvent(PaymentEvent.ProductTitleChanged(it)) },
                    label = { Text("Product Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = uiState.listingDescription,
                    onValueChange = { onEvent(PaymentEvent.ListingDescriptionChanged(it)) },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = uiState.listingLocation,
                    onValueChange = { onEvent(PaymentEvent.ListingLocationChanged(it)) },
                    label = { Text("Found Location") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = uiState.listingDate,
                    onValueChange = { onEvent(PaymentEvent.ListingDateChanged(it)) },
                    label = { Text("Found Date") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = uiState.listingTime,
                    onValueChange = { onEvent(PaymentEvent.ListingTimeChanged(it)) },
                    label = { Text("Found Time") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = uiState.rewardAmountLabel,
                    onValueChange = { onEvent(PaymentEvent.RewardAmountChanged(it)) },
                    label = { Text("Reward Amount") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Card(shape = RoundedCornerShape(24.dp), modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Claim Verification Questions", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = uiState.securityQuestionOne,
                    onValueChange = { onEvent(PaymentEvent.SecurityQuestionOneChanged(it)) },
                    label = { Text("Question 1") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = uiState.securityQuestionTwo,
                    onValueChange = { onEvent(PaymentEvent.SecurityQuestionTwoChanged(it)) },
                    label = { Text("Question 2") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = uiState.securityQuestionThree,
                    onValueChange = { onEvent(PaymentEvent.SecurityQuestionThreeChanged(it)) },
                    label = { Text("Question 3") },
                    modifier = Modifier.fillMaxWidth()
                )
                uiState.finderListingError?.let {
                    Text(it, color = MaterialTheme.colorScheme.error)
                }
                uiState.statusMessage?.let {
                    Text(it, color = MaterialTheme.colorScheme.primary)
                }
                Button(
                    onClick = { onEvent(PaymentEvent.SubmitFinderListing) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Create Listing Preview")
                }
            }
        }
    }
}

@Composable
private fun ListingScreen(
    uiState: PaymentUiState,
    onEvent: (PaymentEvent) -> Unit
) {
    val bg = Brush.verticalGradient(
        listOf(
            MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
            MaterialTheme.colorScheme.surface,
            MaterialTheme.colorScheme.surfaceContainerHighest
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bg)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("Found Item Listing", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text("A claimant who recognizes this item can verify details and continue to the reward payment.", style = MaterialTheme.typography.bodyLarge)

        Card(shape = RoundedCornerShape(24.dp), modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .border(
                            width = 2.dp,
                            color = MaterialTheme.colorScheme.outline,
                            shape = RoundedCornerShape(18.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "IMAGE",
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(uiState.productTitle, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text(uiState.listingDescription, style = MaterialTheme.typography.bodyLarge)
                Text("Reward to claim: ${uiState.rewardAmountLabel}", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            }
        }

        Card(shape = RoundedCornerShape(24.dp), modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Home, contentDescription = null)
                    Text("Found at: ${uiState.listingLocation}", style = MaterialTheme.typography.bodyLarge)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Schedule, contentDescription = null)
                    Text("Date: ${uiState.listingDate} at ${uiState.listingTime}", style = MaterialTheme.typography.bodyLarge)
                }
            }
        }

        Card(shape = RoundedCornerShape(24.dp), modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Claim process", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("1. Confirm the item using finder-only questions.")
                Text("2. If your answers are correct, continue to reward payment.")
                Text("3. Receive an exchange code for handoff.")
            }
        }

        Button(
            onClick = { onEvent(PaymentEvent.BackToFinderForm) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Back To Finder Form")
        }

        Button(
            onClick = { onEvent(PaymentEvent.StartClaim) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Claim This Product")
        }
    }
}

@Composable
private fun SecurityQuestionsScreen(
    uiState: PaymentUiState,
    onEvent: (PaymentEvent) -> Unit
) {
    val bg = Brush.verticalGradient(
        listOf(
            MaterialTheme.colorScheme.surfaceContainerHighest,
            MaterialTheme.colorScheme.surface
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bg)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Ownership Verification", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text("Answer the finder's questions correctly to unlock payment.", style = MaterialTheme.typography.bodyLarge)

        Card(shape = RoundedCornerShape(24.dp), modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(uiState.securityQuestionOne, style = MaterialTheme.typography.titleMedium)
                OutlinedTextField(
                    value = uiState.securityAnswerOne,
                    onValueChange = { onEvent(PaymentEvent.SecurityAnswerOneChanged(it)) },
                    label = { Text("Answer 1") },
                    modifier = Modifier.fillMaxWidth()
                )

                Text(uiState.securityQuestionTwo, style = MaterialTheme.typography.titleMedium)
                OutlinedTextField(
                    value = uiState.securityAnswerTwo,
                    onValueChange = { onEvent(PaymentEvent.SecurityAnswerTwoChanged(it)) },
                    label = { Text("Answer 2") },
                    modifier = Modifier.fillMaxWidth()
                )

                Text(uiState.securityQuestionThree, style = MaterialTheme.typography.titleMedium)
                OutlinedTextField(
                    value = uiState.securityAnswerThree,
                    onValueChange = { onEvent(PaymentEvent.SecurityAnswerThreeChanged(it)) },
                    label = { Text("Answer 3") },
                    modifier = Modifier.fillMaxWidth()
                )

                uiState.securityQuestionError?.let {
                    Text(it, color = MaterialTheme.colorScheme.error)
                }
                uiState.statusMessage?.let {
                    Text(it, color = MaterialTheme.colorScheme.primary)
                }

                Button(
                    onClick = { onEvent(PaymentEvent.SubmitClaimAnswers) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Verify Answers And Continue")
                }
            }
        }
    }
}

@Composable
private fun PaymentFormScreen(
    uiState: PaymentUiState,
    onEvent: (PaymentEvent) -> Unit
) {
    val bg = Brush.verticalGradient(
        listOf(
            MaterialTheme.colorScheme.surfaceContainerHighest,
            MaterialTheme.colorScheme.surface
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bg)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Reward Checkout", style = MaterialTheme.typography.headlineMedium)
        Text("Secure payment to claim ${uiState.productTitle}", style = MaterialTheme.typography.bodyMedium)

        Card(
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Card Details", style = MaterialTheme.typography.titleMedium)

                OutlinedTextField(
                    value = uiState.cardNumber,
                    onValueChange = { onEvent(PaymentEvent.CardNumberChanged(it)) },
                    label = { Text("Card Number") },
                    leadingIcon = { Icon(Icons.Default.CreditCard, null) },
                    isError = uiState.fieldErrors.cardNumber != null,
                    modifier = Modifier.fillMaxWidth()
                )
                uiState.fieldErrors.cardNumber?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                OutlinedTextField(
                    value = uiState.cardName,
                    onValueChange = { onEvent(PaymentEvent.CardNameChanged(it)) },
                    label = { Text("Card Holder Name") },
                    leadingIcon = { Icon(Icons.Default.Person, null) },
                    isError = uiState.fieldErrors.cardName != null,
                    modifier = Modifier.fillMaxWidth()
                )
                uiState.fieldErrors.cardName?.let { Text(it, color = MaterialTheme.colorScheme.error) }

                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = uiState.expiry,
                        onValueChange = { onEvent(PaymentEvent.ExpiryChanged(it)) },
                        label = { Text("MM/YY") },
                        isError = uiState.fieldErrors.expiry != null,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(Modifier.width(10.dp))
                    OutlinedTextField(
                        value = uiState.cvv,
                        onValueChange = { onEvent(PaymentEvent.CvvChanged(it)) },
                        label = { Text("CVV") },
                        leadingIcon = { Icon(Icons.Default.Lock, null) },
                        isError = uiState.fieldErrors.cvv != null,
                        modifier = Modifier.weight(1f)
                    )
                }
                uiState.fieldErrors.expiry?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                uiState.fieldErrors.cvv?.let { Text(it, color = MaterialTheme.colorScheme.error) }

                Spacer(modifier = Modifier.height(6.dp))
                Text("Billing Address", style = MaterialTheme.typography.titleMedium)

                OutlinedTextField(
                    value = uiState.street,
                    onValueChange = { onEvent(PaymentEvent.StreetChanged(it)) },
                    label = { Text("Street Address") },
                    leadingIcon = { Icon(Icons.Default.Home, null) },
                    isError = uiState.fieldErrors.street != null,
                    modifier = Modifier.fillMaxWidth()
                )
                uiState.fieldErrors.street?.let { Text(it, color = MaterialTheme.colorScheme.error) }

                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = uiState.city,
                        onValueChange = { onEvent(PaymentEvent.CityChanged(it)) },
                        label = { Text("City") },
                        isError = uiState.fieldErrors.city != null,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(Modifier.width(10.dp))
                    OutlinedTextField(
                        value = uiState.state,
                        onValueChange = { onEvent(PaymentEvent.StateChanged(it)) },
                        label = { Text("State") },
                        isError = uiState.fieldErrors.state != null,
                        modifier = Modifier.weight(1f)
                    )
                }
                uiState.fieldErrors.city?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                uiState.fieldErrors.state?.let { Text(it, color = MaterialTheme.colorScheme.error) }

                OutlinedTextField(
                    value = uiState.zip,
                    onValueChange = { onEvent(PaymentEvent.ZipChanged(it)) },
                    label = { Text("ZIP Code") },
                    isError = uiState.fieldErrors.zip != null,
                    modifier = Modifier.fillMaxWidth()
                )
                uiState.fieldErrors.zip?.let { Text(it, color = MaterialTheme.colorScheme.error) }

                Spacer(modifier = Modifier.height(6.dp))
                Button(
                    onClick = { onEvent(PaymentEvent.SubmitPayment) },
                    enabled = !uiState.isProcessing,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Pay $10.00")
                }

                if (uiState.isProcessing) {
                    CircularProgressIndicator()
                }

                uiState.statusMessage?.let { message ->
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun PaymentProcessingScreen(
    uiState: PaymentUiState
) {
    val bg = Brush.verticalGradient(
        listOf(
            MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
            MaterialTheme.colorScheme.surface,
            MaterialTheme.colorScheme.secondary.copy(alpha = 0.08f)
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bg)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(28.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CircularProgressIndicator()
                Text(
                    text = "Processing Payment",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = uiState.statusMessage ?: "Please wait while we secure your claim.",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Do not close the app during this step.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun ExchangeCodeScreen(
    uiState: PaymentUiState,
    onEvent: (PaymentEvent) -> Unit
) {
    val bg = Brush.verticalGradient(
        listOf(
            MaterialTheme.colorScheme.primary.copy(alpha = 0.16f),
            MaterialTheme.colorScheme.surface,
            MaterialTheme.colorScheme.surfaceContainerHighest
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bg)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .height(72.dp)
                .width(72.dp)
        )
        Text(
            text = "Payment Confirmed",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Show this exchange code when you meet to receive the item.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
        Text(
            text = "Share this code with the finder. The finder must verify it before handing over the item.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Card(
            shape = RoundedCornerShape(30.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primary
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 28.dp, horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Exchange Code",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                )
                Text(
                    text = uiState.exchangeCode ?: "------",
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }

        Card(
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "What happens next",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text("1. Claimant shows this exchange code to the finder.")
                Text("2. Finder enters the code on their verification screen.")
                Text("3. If the code matches, the listing is marked completed.")
            }
        }

        uiState.claimRecord?.let { claim ->
            Card(
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Claim Status: ${claim.status.name.lowercase().replaceFirstChar(Char::uppercase)}",
                        style = MaterialTheme.typography.titleMedium
                    )
                    uiState.paymentRecord?.let { payment ->
                        Text("Payment ending in ${payment.paymentMethodLastFour}")
                    }
                    uiState.statusMessage?.let { Text(it) }
                    if (claim.status == ListingStatus.COMPLETED) {
                        Text("Exchange-code verification is complete.")
                    }
                }
            }
        }

        if (uiState.claimRecord?.status != ListingStatus.COMPLETED) {
            Button(
                onClick = { onEvent(PaymentEvent.OpenFinderVerification) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Go To Finder Verification Page")
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant,
                        shape = RoundedCornerShape(18.dp)
                    )
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Item handoff completed.",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

@Composable
private fun FinderVerificationScreen(
    uiState: PaymentUiState,
    onEvent: (PaymentEvent) -> Unit
) {
    val bg = Brush.verticalGradient(
        listOf(
            MaterialTheme.colorScheme.secondary.copy(alpha = 0.14f),
            MaterialTheme.colorScheme.surface,
            MaterialTheme.colorScheme.surfaceContainerHighest
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bg)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            imageVector = Icons.Default.QrCode2,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .height(56.dp)
                .width(56.dp)
        )
        Text(
            text = "Finder Verification",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Use this screen on the finder side. Enter the claimant's exchange code before handing over the item.",
            style = MaterialTheme.typography.bodyLarge
        )

        Card(
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = uiState.finderEnteredCode,
                    onValueChange = { onEvent(PaymentEvent.FinderCodeChanged(it)) },
                    label = { Text("Exchange Code") },
                    isError = uiState.finderCodeError != null,
                    modifier = Modifier.fillMaxWidth()
                )
                uiState.finderCodeError?.let { Text(it, color = MaterialTheme.colorScheme.error) }

                Button(
                    onClick = { onEvent(PaymentEvent.VerifyFinderCode) },
                    enabled = !uiState.isProcessing,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Verify Code And Hand Over Item")
                }

                Button(
                    onClick = { onEvent(PaymentEvent.ReturnToExchangeCode) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Back To Claimant Code Page")
                }
            }
        }

        uiState.finderVerificationMessage?.let { message ->
            Card(
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = message,
                    modifier = Modifier.padding(18.dp),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }

        if (uiState.claimRecord?.status == ListingStatus.COMPLETED) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant,
                        shape = RoundedCornerShape(18.dp)
                    )
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Verification complete. Listing status is Completed.",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
