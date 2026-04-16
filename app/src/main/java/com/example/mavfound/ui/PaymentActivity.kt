package com.example.mavfound.ui

import android.content.Context
import android.content.Intent
import android.graphics.drawable.AnimationDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.example.mavfound.R
import com.example.mavfound.database.DatabaseHelper
import com.example.mavfound.models.Listing
import com.example.mavfound.models.Order
import com.example.mavfound.utils.ThemeManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID
import java.text.SimpleDateFormat

class PaymentActivity : AppCompatActivity() {
    private lateinit var dbHelper: DatabaseHelper
    private var currentListing: Listing? = null
    private var currentUserId: Int = -1

    private lateinit var tilCardholderName: TextInputLayout
    private lateinit var tilBillingAddress: TextInputLayout
    private lateinit var tilCardNumber: TextInputLayout
    private lateinit var tilExpiryDate: TextInputLayout
    private lateinit var tilCvv: TextInputLayout

    private lateinit var etCardholderName: TextInputEditText
    private lateinit var etBillingAddress: TextInputEditText
    private lateinit var etCardNumber: TextInputEditText
    private lateinit var etExpiryDate: TextInputEditText
    private lateinit var etCvv: TextInputEditText

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeManager.applySavedTheme(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment)

        val rootLayout = findViewById<CoordinatorLayout>(R.id.paymentRootLayout)
        val animationDrawable = rootLayout.background as AnimationDrawable
        animationDrawable.setEnterFadeDuration(2000)
        animationDrawable.setExitFadeDuration(4000)
        animationDrawable.start()

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.paymentToolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = ""
        toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        dbHelper = DatabaseHelper(this)
        val listingId = intent.getIntExtra("LISTING_ID", -1)
        if (listingId != -1) {
            currentListing = dbHelper.getListingById(listingId)
        }

        currentUserId = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getInt("CURRENT_USER_ID", -1)

        tilCardholderName = findViewById(R.id.tilCardholderName)
        tilBillingAddress = findViewById(R.id.tilBillingAddress)
        tilCardNumber = findViewById(R.id.tilCardNumber)
        tilExpiryDate = findViewById(R.id.tilExpiryDate)
        tilCvv = findViewById(R.id.tilCvv)

        etCardholderName = findViewById(R.id.etCardholderName)
        etBillingAddress = findViewById(R.id.etBillingAddress)
        etCardNumber = findViewById(R.id.etCardNumber)
        etExpiryDate = findViewById(R.id.etExpiryDate)
        etCvv = findViewById(R.id.etCvv)

        val listing = currentListing
        if (listing == null || currentUserId == -1 || !isPaymentUnlocked(listingId, currentUserId)) {
            finish()
            return
        }

        findViewById<TextView>(R.id.tvPaymentPageSummary).text =
            if (listing.rewardAmount > 0) {
                "Enter your billing details to pay ${String.format("$%.2f", listing.rewardAmount)} for ${listing.title}."
            } else {
                "Enter your billing details to finish the claim flow for ${listing.title}."
            }

        installExpiryFormatter()
        findViewById<MaterialButton>(R.id.btnSubmitPayment).setOnClickListener { handlePaymentSubmission(listing) }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    private fun installExpiryFormatter() {
        etExpiryDate.addTextChangedListener(object : TextWatcher {
            private var isUpdating = false

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit

            override fun afterTextChanged(s: Editable?) {
                if (isUpdating) return
                val digits = s?.toString().orEmpty().filter { it.isDigit() }.take(4)
                val formatted = when {
                    digits.length <= 2 -> digits
                    else -> digits.substring(0, 2) + "/" + digits.substring(2)
                }
                if (formatted != s?.toString().orEmpty()) {
                    isUpdating = true
                    etExpiryDate.setText(formatted)
                    etExpiryDate.setSelection(formatted.length)
                    isUpdating = false
                }
            }
        })
    }

    private fun handlePaymentSubmission(listing: Listing) {
        clearPaymentErrors()

        val cardholderName = etCardholderName.text?.toString()?.trim().orEmpty()
        val billingAddress = etBillingAddress.text?.toString()?.trim().orEmpty()
        val cardNumber = etCardNumber.text?.toString()?.trim().orEmpty().filter { it.isDigit() }
        val expiryRaw = etExpiryDate.text?.toString()?.trim().orEmpty()
        val cvv = etCvv.text?.toString()?.trim().orEmpty()

        var hasError = false
        if (cardholderName.isEmpty()) {
            tilCardholderName.error = "Name on card is required"
            hasError = true
        }
        if (billingAddress.isEmpty()) {
            tilBillingAddress.error = "Billing address is required"
            hasError = true
        }
        if (cardNumber.length != 16) {
            tilCardNumber.error = "Card number must be 16 digits"
            hasError = true
        }
        if (!isValidExpiry(expiryRaw)) {
            tilExpiryDate.error = "Enter a valid future expiry date"
            hasError = true
        }
        if (!cvv.matches(Regex("^\\d{3,4}$"))) {
            tilCvv.error = "CVV must be 3 or 4 digits"
            hasError = true
        }
        if (hasError) return

        val orderId = "MAV-${UUID.randomUUID().toString().take(8).uppercase(Locale.US)}"
        val handoffCode = UUID.randomUUID().toString().replace("-", "").take(6).uppercase(Locale.US)
        val paymentDate = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
        val order = Order(
            orderId = orderId,
            listingId = listing.listingId,
            buyerId = currentUserId,
            listingTitle = listing.title,
            amount = listing.rewardAmount,
            paymentDate = paymentDate,
            status = "Pending",
            handoffCode = handoffCode
        )

        if (dbHelper.insertOrder(order) == -1L) {
            tilCardNumber.error = "Could not save payment. Please try again."
            return
        }

        dbHelper.updateListingStatus(listing.listingId, "Claimed")
        setPaymentUnlocked(listing.listingId, currentUserId, false)
        val amountLabel = if (listing.rewardAmount > 0) String.format("$%.2f", listing.rewardAmount) else "$0.00"
        val intent = Intent(this, OrderConfirmationActivity::class.java).apply {
            putExtra("EXTRA_ITEM_TITLE", listing.title)
            putExtra("EXTRA_ORDER_ID", orderId)
            putExtra("EXTRA_AMOUNT", amountLabel)
            putExtra("EXTRA_HANDOFF_CODE", handoffCode)
        }
        startActivity(intent)
        finish()
    }

    private fun clearPaymentErrors() {
        tilCardholderName.error = null
        tilBillingAddress.error = null
        tilCardNumber.error = null
        tilExpiryDate.error = null
        tilCvv.error = null
    }

    private fun isValidExpiry(expiry: String): Boolean {
        if (!expiry.matches(Regex("^(0[1-9]|1[0-2])/\\d{2}$"))) return false
        val parts = expiry.split("/")
        val month = parts[0].toInt()
        val year = 2000 + parts[1].toInt()

        val now = Calendar.getInstance()
        val expiryCalendar = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month - 1)
            set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }
        return !expiryCalendar.before(now)
    }

    private fun isPaymentUnlocked(listingId: Int, userId: Int): Boolean =
        getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean("payment_unlocked_${listingId}_$userId", false)

    private fun setPaymentUnlocked(listingId: Int, userId: Int, unlocked: Boolean) {
        getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean("payment_unlocked_${listingId}_$userId", unlocked)
            .apply()
    }

    companion object {
        private const val PREFS_NAME = "MavFoundPrefs"
    }
}
