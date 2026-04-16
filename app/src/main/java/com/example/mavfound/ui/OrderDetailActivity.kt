package com.example.mavfound.ui

import android.graphics.drawable.AnimationDrawable
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.example.mavfound.R
import com.example.mavfound.database.DatabaseHelper
import com.example.mavfound.utils.ThemeManager

class OrderDetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeManager.applySavedTheme(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_detail)

        val rootLayout = findViewById<CoordinatorLayout>(R.id.orderDetailRootLayout)
        val animationDrawable = rootLayout.background as AnimationDrawable
        animationDrawable.setEnterFadeDuration(2000)
        animationDrawable.setExitFadeDuration(4000)
        animationDrawable.start()

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.orderDetailToolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = ""
        toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        val orderId = intent.getStringExtra("EXTRA_ORDER_ID").orEmpty()
        val order = DatabaseHelper(this).getOrderByOrderId(orderId)

        findViewById<TextView>(R.id.tvOrderDetailTitle).text = order?.listingTitle ?: "Order"
        findViewById<TextView>(R.id.tvOrderDetailStatus).text = "Status: ${order?.status ?: "Pending"}"
        findViewById<TextView>(R.id.tvOrderDetailAmount).text =
            "Amount Paid: ${String.format("$%.2f", order?.amount ?: 0.0)}"
        findViewById<TextView>(R.id.tvOrderDetailOrderId).text = "Order ID: ${order?.orderId.orEmpty()}"
        findViewById<TextView>(R.id.tvOrderDetailDate).text = "Paid On: ${order?.paymentDate.orEmpty()}"
        findViewById<TextView>(R.id.tvOrderDetailCode).text = "Handoff Code: ${order?.handoffCode.orEmpty()}"
        findViewById<TextView>(R.id.tvOrderDetailHint).text =
            "Only give this finder code once the finder returns your item in hand. Until verification and delivery are complete, the order stays pending."
    }
}
