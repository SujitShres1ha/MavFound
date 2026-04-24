package com.example.mavfound.ui

import android.content.Context
import android.content.Intent
import android.graphics.drawable.AnimationDrawable
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mavfound.R
import com.example.mavfound.database.DatabaseHelper
import com.example.mavfound.utils.ThemeManager

class OrdersActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeManager.applySavedTheme(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_orders)

        val rootLayout = findViewById<CoordinatorLayout>(R.id.ordersRootLayout)
        val animationDrawable = rootLayout.background as AnimationDrawable
        animationDrawable.setEnterFadeDuration(2000)
        animationDrawable.setExitFadeDuration(4000)
        animationDrawable.start()

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.ordersToolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = ""
        toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        val currentUserId = getSharedPreferences("MavFoundPrefs", Context.MODE_PRIVATE)
            .getInt("CURRENT_USER_ID", -1)
        val dbHelper = DatabaseHelper(this)
        val orders = if (currentUserId != -1) dbHelper.getOrdersForUser(currentUserId) else emptyList()

        findViewById<TextView>(R.id.tvOrdersEmpty).text =
            if (orders.isEmpty()) "No paid orders yet." else ""

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewOrders)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = OrdersAdapter(orders) { order ->
            startActivity(
                Intent(this, OrderDetailActivity::class.java)
                    .putExtra("EXTRA_ORDER_ID", order.orderId)
            )
        }
    }
}
