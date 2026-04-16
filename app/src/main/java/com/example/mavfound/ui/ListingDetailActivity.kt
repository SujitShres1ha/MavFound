package com.example.mavfound.ui

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.AnimationDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.example.mavfound.R
import com.example.mavfound.database.DatabaseHelper
import com.example.mavfound.models.Listing
import com.example.mavfound.models.Order
import com.example.mavfound.utils.ThemeManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.util.Locale

class ListingDetailActivity : AppCompatActivity() {
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var claimCard: com.google.android.material.card.MaterialCardView
    private lateinit var claimStatus: TextView
    private lateinit var paymentCard: com.google.android.material.card.MaterialCardView
    private lateinit var paymentSummary: TextView
    private lateinit var btnClaimProduct: MaterialButton
    private lateinit var btnOpenPayment: MaterialButton
    private lateinit var listerOrdersCard: com.google.android.material.card.MaterialCardView
    private lateinit var tvListerOrdersEmpty: TextView
    private lateinit var ordersContainer: LinearLayout

    private var currentListing: Listing? = null
    private var currentUserId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeManager.applySavedTheme(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_listing_detail)

        val rootLayout = findViewById<CoordinatorLayout>(R.id.detailRootLayout)
        val animationDrawable = rootLayout.background as AnimationDrawable
        animationDrawable.setEnterFadeDuration(2000)
        animationDrawable.setExitFadeDuration(4000)
        animationDrawable.start()

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = ""

        dbHelper = DatabaseHelper(this)
        claimCard = findViewById(R.id.claimCard)
        claimStatus = findViewById(R.id.tvClaimStatus)
        paymentCard = findViewById(R.id.paymentCard)
        paymentSummary = findViewById(R.id.tvPaymentSummary)
        btnClaimProduct = findViewById(R.id.btnClaimProduct)
        btnOpenPayment = findViewById(R.id.btnOpenPayment)
        listerOrdersCard = findViewById(R.id.listerOrdersCard)
        tvListerOrdersEmpty = findViewById(R.id.tvListerOrdersEmpty)
        ordersContainer = findViewById(R.id.ordersContainer)

        currentUserId = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getInt("CURRENT_USER_ID", -1)

        val listingId = intent.getIntExtra("LISTING_ID", -1)
        if (listingId != -1) {
            currentListing = dbHelper.getListingById(listingId)
            bindListing(currentListing)
        }

        btnOpenPayment.setOnClickListener {
            val listing = currentListing ?: return@setOnClickListener
            startActivity(Intent(this, PaymentActivity::class.java).putExtra("LISTING_ID", listing.listingId))
        }

        configureRoleSpecificUi()
    }

    override fun onResume() {
        super.onResume()
        currentListing = currentListing?.listingId?.let { dbHelper.getListingById(it) }
        bindListing(currentListing)
        configureRoleSpecificUi()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    private fun bindListing(item: Listing?) {
        item ?: return
        findViewById<TextView>(R.id.tvDetailTitle).text = item.title
        findViewById<TextView>(R.id.tvDetailDesc).text = item.description
        findViewById<TextView>(R.id.tvDetailLocation).text = "Found at: ${item.location}"
        findViewById<TextView>(R.id.tvDetailReward).text =
            if (item.rewardAmount > 0) String.format("Reward: $%.2f", item.rewardAmount) else "No Reward Requested"
        paymentSummary.text =
            if (item.rewardAmount > 0) {
                "Your answer is verified. Continue to the payment page to complete the ${String.format("$%.2f", item.rewardAmount)} reward."
            } else {
                "Your answer is verified. Continue to payment to finish the claim confirmation."
            }

        val ivHero = findViewById<ImageView>(R.id.ivDetailHero)
        if (!item.imagePath.isNullOrEmpty()) {
            Glide.with(this).load(item.imagePath).into(ivHero)
        }
    }

    private fun configureRoleSpecificUi() {
        val listing = currentListing ?: return
        if (currentUserId == -1) {
            claimCard.isVisible = true
            listerOrdersCard.isVisible = false
            paymentCard.isVisible = false
            btnClaimProduct.isEnabled = false
            claimStatus.text = "Login to start the claim flow."
            return
        }

        if (listing.listerId == currentUserId) {
            claimCard.isVisible = false
            paymentCard.isVisible = false
            listerOrdersCard.isVisible = true
            renderListerOrders(listing)
        } else {
            claimCard.isVisible = true
            listerOrdersCard.isVisible = false
            setupClaimEntry(listing)
        }
    }

    private fun renderListerOrders(listing: Listing) {
        ordersContainer.removeAllViews()
        val orders = dbHelper.getOrdersForListing(listing.listingId)
        tvListerOrdersEmpty.isVisible = orders.isEmpty()

        orders.forEach { order ->
            val row = LayoutInflater.from(this).inflate(R.layout.item_lister_order, ordersContainer, false)
            val buyerName = dbHelper.getUserNameById(order.buyerId) ?: "Claimant"

            row.findViewById<TextView>(R.id.tvListerOrderBuyer).text = buyerName
            row.findViewById<TextView>(R.id.tvListerOrderMeta).text =
                "Order ${order.orderId} • ${String.format("$%.2f", order.amount)} • ${order.paymentDate}"

            val statusView = row.findViewById<TextView>(R.id.tvListerOrderStatus)
            statusView.text = order.status.uppercase(Locale.US)
            statusView.setTextColor(
                if (order.status.equals("Completed", ignoreCase = true)) Color.parseColor("#0064B1")
                else Color.parseColor("#F59E0B")
            )

            val verifyButton = row.findViewById<MaterialButton>(R.id.btnVerifyHandoff)
            if (order.status.equals("Completed", ignoreCase = true)) {
                verifyButton.text = "Completed"
                verifyButton.isEnabled = false
                verifyButton.alpha = 0.6f
            } else {
                verifyButton.setOnClickListener { showHandoffDialog(order) }
            }

            ordersContainer.addView(row)
        }
    }

    private fun showHandoffDialog(order: Order) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_verify_handoff, null)
        val tilCode = dialogView.findViewById<TextInputLayout>(R.id.tilHandoffCode)
        val etCode = dialogView.findViewById<TextInputEditText>(R.id.etHandoffCode)

        MaterialAlertDialogBuilder(this)
            .setView(dialogView)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Confirm", null)
            .create()
            .also { dialog ->
                dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                dialog.setOnShowListener {
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                        tilCode.error = null
                        val entered = etCode.text?.toString()?.trim().orEmpty().uppercase(Locale.US)

                        if (entered.isEmpty()) {
                            tilCode.error = "Enter the code shown by the claimant"
                            return@setOnClickListener
                        }

                        if (entered != order.handoffCode.uppercase(Locale.US)) {
                            tilCode.error = "Code does not match"
                            return@setOnClickListener
                        }

                        dbHelper.updateOrderStatus(order.orderId, "Completed")
                        dbHelper.updateListingStatus(order.listingId, "Delivered")
                        Toast.makeText(this, "Order completed and listing marked claimed.", Toast.LENGTH_LONG).show()
                        dialog.dismiss()
                        currentListing = dbHelper.getListingById(order.listingId)
                        bindListing(currentListing)
                        currentListing?.let { renderListerOrders(it) }
                    }
                }
            }
            .show()
    }

    private fun setupClaimEntry(listing: Listing) {
        if (isPaymentUnlocked(listing.listingId, currentUserId)) {
            unlockPaymentUi()
            return
        }

        renderClaimStatus(listing)
        btnClaimProduct.setOnClickListener { showClaimDialog(listing) }
    }

    private fun showClaimDialog(listing: Listing) {
        val lockoutUntil = getLockoutUntil(listing.listingId, currentUserId)
        if (lockoutUntil > System.currentTimeMillis()) {
            renderClaimStatus(listing)
            Toast.makeText(this, "Try again in ${formatRemaining(lockoutUntil)}.", Toast.LENGTH_LONG).show()
            return
        }

        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_claim_verification, null)
        val tvQuestion = dialogView.findViewById<TextView>(R.id.tvClaimQuestion)
        val tvAttempts = dialogView.findViewById<TextView>(R.id.tvClaimAttempts)
        val tvFeedback = dialogView.findViewById<TextView>(R.id.tvClaimFeedback)
        val tilAnswer = dialogView.findViewById<TextInputLayout>(R.id.tilClaimAnswer)
        val etAnswer = dialogView.findViewById<TextInputEditText>(R.id.etClaimAnswer)

        tvQuestion.text = listing.securityQuestion
        tvAttempts.text = "Attempts remaining: ${MAX_ATTEMPTS - getFailedAttempts(listing.listingId, currentUserId)}"

        MaterialAlertDialogBuilder(this)
            .setView(dialogView)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Verify", null)
            .create()
            .also { dialog ->
                dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                dialog.setOnShowListener {
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                        tilAnswer.error = null
                        tvFeedback.text = ""
                        val submitted = etAnswer.text?.toString()?.trim().orEmpty()

                        if (submitted.isEmpty()) {
                            tilAnswer.error = "Answer is required"
                            return@setOnClickListener
                        }

                        if (submitted.equals(listing.securityAnswer.trim(), ignoreCase = true)) {
                            clearAttemptState(listing.listingId, currentUserId)
                            setPaymentUnlocked(listing.listingId, currentUserId, true)
                            unlockPaymentUi()
                            dbHelper.insertClaim(listing.listingId, currentUserId, submitted)
                            dialog.dismiss()
                            return@setOnClickListener
                        }

                        val failedAttempts = incrementFailedAttempts(listing.listingId, currentUserId)
                        if (failedAttempts >= MAX_ATTEMPTS) {
                            val lockoutUntilMillis = System.currentTimeMillis() + LOCKOUT_DURATION_MS
                            setLockoutUntil(listing.listingId, currentUserId, lockoutUntilMillis)
                            resetFailedAttempts(listing.listingId, currentUserId)
                            dialog.dismiss()
                            renderClaimStatus(listing)
                            Toast.makeText(this, "Too many attempts. Try again in ${formatRemaining(lockoutUntilMillis)}.", Toast.LENGTH_LONG).show()
                        } else {
                            val remaining = MAX_ATTEMPTS - failedAttempts
                            tvAttempts.text = "Attempts remaining: $remaining"
                            tvFeedback.text = "That answer is incorrect. Try again."
                            renderClaimStatus(listing)
                        }
                    }
                }
            }
            .show()
    }

    private fun unlockPaymentUi() {
        paymentCard.isVisible = true
        btnClaimProduct.text = "Answer Verified"
        btnClaimProduct.isEnabled = false
        btnClaimProduct.alpha = 0.7f
        claimStatus.text = "Verified successfully. Continue to the payment page."
    }

    private fun renderClaimStatus(listing: Listing) {
        val lockoutUntil = getLockoutUntil(listing.listingId, currentUserId)
        if (lockoutUntil > System.currentTimeMillis()) {
            paymentCard.isVisible = false
            claimStatus.text = "Too many wrong answers. Try again in ${formatRemaining(lockoutUntil)}."
            btnClaimProduct.isEnabled = false
            btnClaimProduct.alpha = 0.5f
            return
        }

        paymentCard.isVisible = false
        val remaining = MAX_ATTEMPTS - getFailedAttempts(listing.listingId, currentUserId)
        claimStatus.text = "Answer the finder’s question. You have $remaining attempt${if (remaining == 1) "" else "s"} left before a 10 minute timeout."
        btnClaimProduct.isEnabled = true
        btnClaimProduct.alpha = 1.0f
        btnClaimProduct.text = "Claim This Product"
    }

    private fun getPrefs() = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private fun getFailedAttempts(listingId: Int, userId: Int): Int = getPrefs().getInt(attemptKey(listingId, userId), 0)
    private fun incrementFailedAttempts(listingId: Int, userId: Int): Int {
        val updated = getFailedAttempts(listingId, userId) + 1
        getPrefs().edit().putInt(attemptKey(listingId, userId), updated).apply()
        return updated
    }
    private fun resetFailedAttempts(listingId: Int, userId: Int) {
        getPrefs().edit().putInt(attemptKey(listingId, userId), 0).apply()
    }
    private fun getLockoutUntil(listingId: Int, userId: Int): Long = getPrefs().getLong(lockoutKey(listingId, userId), 0L)
    private fun setLockoutUntil(listingId: Int, userId: Int, until: Long) {
        getPrefs().edit().putLong(lockoutKey(listingId, userId), until).apply()
    }
    private fun setPaymentUnlocked(listingId: Int, userId: Int, unlocked: Boolean) {
        getPrefs().edit().putBoolean(paymentUnlockedKey(listingId, userId), unlocked).apply()
    }
    private fun isPaymentUnlocked(listingId: Int, userId: Int): Boolean =
        getPrefs().getBoolean(paymentUnlockedKey(listingId, userId), false)
    private fun clearAttemptState(listingId: Int, userId: Int) {
        getPrefs().edit().putInt(attemptKey(listingId, userId), 0).putLong(lockoutKey(listingId, userId), 0L).apply()
    }
    private fun formatRemaining(lockoutUntil: Long): String {
        val millisLeft = (lockoutUntil - System.currentTimeMillis()).coerceAtLeast(0L)
        val totalSeconds = millisLeft / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format(Locale.US, "%d:%02d", minutes, seconds)
    }
    private fun attemptKey(listingId: Int, userId: Int) = "claim_attempts_${listingId}_$userId"
    private fun lockoutKey(listingId: Int, userId: Int) = "claim_lockout_${listingId}_$userId"
    private fun paymentUnlockedKey(listingId: Int, userId: Int) = "payment_unlocked_${listingId}_$userId"

    companion object {
        private const val PREFS_NAME = "MavFoundPrefs"
        private const val MAX_ATTEMPTS = 3
        private const val LOCKOUT_DURATION_MS = 10 * 60 * 1000L
    }
}
