package com.example.mavfound.ui

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.mavfound.R
import com.example.mavfound.utils.ThemeManager
import com.google.android.material.button.MaterialButton

class OrderConfirmationActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeManager.applySavedTheme(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_confirmation)

        val itemTitle = intent.getStringExtra("EXTRA_ITEM_TITLE").orEmpty()
        val orderId = intent.getStringExtra("EXTRA_ORDER_ID").orEmpty()
        val amount = intent.getStringExtra("EXTRA_AMOUNT").orEmpty()
        val handoffCode = intent.getStringExtra("EXTRA_HANDOFF_CODE").orEmpty()

        findViewById<TextView>(R.id.tvConfirmationMessage).text =
            "Your payment for $itemTitle was accepted. The finder can now complete the handoff."
        findViewById<TextView>(R.id.tvConfirmationOrderId).text = "Confirmation ID: $orderId"
        findViewById<TextView>(R.id.tvConfirmationAmount).text = "Paid: $amount"
        findViewById<TextView>(R.id.tvConfirmationCode).text =
            "Handoff Code: $handoffCode"
        findViewById<TextView>(R.id.tvConfirmationHint).text =
            "Only give this code to the finder once they hand your item back to you."

        findViewById<MaterialButton>(R.id.btnConfirmationDone).setOnClickListener {
            val intent = Intent(this, DashboardActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            startActivity(intent)
            finish()
        }
    }
}
