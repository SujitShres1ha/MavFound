package com.example.mavfound.ui

import android.content.Context
import android.content.Intent
import android.graphics.drawable.AnimationDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.TextView
import android.widget.Toast
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mavfound.R
import com.example.mavfound.database.DatabaseHelper
import com.example.mavfound.models.Listing
import com.example.mavfound.models.Order
import com.example.mavfound.models.User
import com.example.mavfound.utils.ThemeManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.android.material.textfield.TextInputEditText

class AdminDashboardActivity : AppCompatActivity() {
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var adapter: AdminDashboardAdapter
    private lateinit var tvSectionTitle: TextView
    private lateinit var tvSectionSubtitle: TextView
    private lateinit var tvEmptyState: TextView
    private var currentTab: String = "users"

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeManager.applySavedTheme(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_dashboard)

        val rootLayout = findViewById<CoordinatorLayout>(R.id.adminRootLayout)
        val animationDrawable = rootLayout.background as AnimationDrawable
        animationDrawable.setEnterFadeDuration(2000)
        animationDrawable.setExitFadeDuration(4000)
        animationDrawable.start()

        dbHelper = DatabaseHelper(this)
        adapter = AdminDashboardAdapter { item -> handleItemClick(item) }
        tvSectionTitle = findViewById(R.id.tvAdminSectionTitle)
        tvSectionSubtitle = findViewById(R.id.tvAdminSectionSubtitle)
        tvEmptyState = findViewById(R.id.tvAdminEmptyState)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerAdminItems)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        val btnUsers = findViewById<MaterialButton>(R.id.btnTabUsers)
        val btnListings = findViewById<MaterialButton>(R.id.btnTabListings)
        val btnClaimed = findViewById<MaterialButton>(R.id.btnTabClaimed)
        val btnCompleted = findViewById<MaterialButton>(R.id.btnTabCompleted)
        val btnLogout = findViewById<MaterialButton>(R.id.btnAdminLogout)

        val tabButtons = listOf(btnUsers, btnListings, btnClaimed, btnCompleted)

        fun select(button: MaterialButton) {
            tabButtons.forEach {
                it.setBackgroundColor(android.graphics.Color.TRANSPARENT)
                it.strokeWidth = 2
                it.strokeColor = android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#5C6E82"))
                it.setTextColor(android.graphics.Color.parseColor("#5C6E82"))
            }
            button.setBackgroundColor(android.graphics.Color.parseColor("#E7F0FF"))
            button.strokeWidth = 0
            button.setTextColor(android.graphics.Color.parseColor("#0F5BD8"))
        }

        btnUsers.setOnClickListener {
            select(btnUsers)
            currentTab = "users"
            showUsers()
        }
        btnListings.setOnClickListener {
            select(btnListings)
            currentTab = "listings"
            showListings()
        }
        btnClaimed.setOnClickListener {
            select(btnClaimed)
            currentTab = "claimed"
            showOrders("Pending", "Claimed Payments", "Payments completed, handoff still pending.")
        }
        btnCompleted.setOnClickListener {
            select(btnCompleted)
            currentTab = "completed"
            showOrders("Completed", "Completed Orders", "Orders already handed off and finished.")
        }

        btnLogout.setOnClickListener {
            val sharedPrefs = getSharedPreferences("MavFoundPrefs", Context.MODE_PRIVATE)
            sharedPrefs.edit().apply {
                remove("CURRENT_USER_ID")
                remove("CURRENT_USER_NAME")
                remove("IS_ADMIN")
                apply()
            }
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        btnUsers.performClick()
    }

    private fun showUsers() {
        tvSectionTitle.text = "Current Users"
        tvSectionSubtitle.text = "All registered accounts in the local database."
        val items = dbHelper.getAllUsers().map { user ->
            AdminDashboardItem(
                id = user.userId.toString(),
                title = user.name,
                subtitle = user.email,
                status = if (user.isAdmin) "Admin" else if (user.isActive) "Active" else "Inactive"
            )
        }
        renderItems(items, "No users found.")
    }

    private fun showListings() {
        tvSectionTitle.text = "Current Listings"
        tvSectionSubtitle.text = "Every listing with its latest lifecycle status."
        val items = dbHelper.getAllListings().map { listing ->
            AdminDashboardItem(
                id = listing.listingId.toString(),
                title = listing.title,
                subtitle = "${listing.location} • ${listing.dateTime}",
                status = listing.status
            )
        }
        renderItems(items, "No listings found.")
    }

    private fun showOrders(status: String, title: String, subtitle: String) {
        tvSectionTitle.text = title
        tvSectionSubtitle.text = subtitle
        val items = dbHelper.getOrdersByStatus(status).map { order ->
            AdminDashboardItem(
                id = order.orderId,
                title = order.listingTitle,
                subtitle = "${order.orderId} • ${String.format("$%.2f", order.amount)}",
                status = order.status
            )
        }
        renderItems(items, "No $title yet.")
    }

    private fun handleItemClick(item: AdminDashboardItem) {
        when (currentTab) {
            "users" -> dbHelper.getAllUsers().firstOrNull { it.userId.toString() == item.id }?.let { showEditUserDialog(it) }
            "listings" -> dbHelper.getAllListings().firstOrNull { it.listingId.toString() == item.id }?.let { showEditListingDialog(it) }
            "claimed", "completed" -> dbHelper.getOrderByOrderId(item.id)?.let { showEditOrderDialog(it) }
        }
    }

    private fun showEditUserDialog(user: User) {
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_admin_edit_user, null)
        val etName = view.findViewById<TextInputEditText>(R.id.etAdminUserName)
        val etEmail = view.findViewById<TextInputEditText>(R.id.etAdminUserEmail)
        val switchAdmin = view.findViewById<MaterialSwitch>(R.id.switchAdminUser)
        val switchActive = view.findViewById<MaterialSwitch>(R.id.switchActiveUser)

        etName.setText(user.name)
        etEmail.setText(user.email)
        switchAdmin.isChecked = user.isAdmin
        switchActive.isChecked = user.isActive

        MaterialAlertDialogBuilder(this)
            .setView(view)
            .setNegativeButton("Cancel", null)
            .setNeutralButton("Delete", null)
            .setPositiveButton("Save", null)
            .create()
            .also { dialog ->
                dialog.window?.setBackgroundDrawable(ColorDrawable(android.graphics.Color.TRANSPARENT))
                dialog.setOnShowListener {
                    dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                        val name = etName.text?.toString()?.trim().orEmpty()
                        val email = etEmail.text?.toString()?.trim().orEmpty()
                        if (name.isEmpty() || email.isEmpty()) {
                            Toast.makeText(this, "Name and email are required.", Toast.LENGTH_SHORT).show()
                            return@setOnClickListener
                        }
                        val updated = dbHelper.updateUserAdmin(
                            user.userId,
                            name,
                            email,
                            switchAdmin.isChecked,
                            switchActive.isChecked
                        )
                        if (!updated) {
                            Toast.makeText(this, "Could not update user. Email may already exist.", Toast.LENGTH_LONG).show()
                            return@setOnClickListener
                        }
                        dialog.dismiss()
                        showUsers()
                    }
                    dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_NEUTRAL).setOnClickListener {
                        dbHelper.deleteUser(user.userId)
                        dialog.dismiss()
                        showUsers()
                    }
                }
            }
            .show()
    }

    private fun showEditListingDialog(listing: Listing) {
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_admin_edit_listing, null)
        val etReward = view.findViewById<TextInputEditText>(R.id.etAdminListingReward)
        val etStatus = view.findViewById<TextInputEditText>(R.id.etAdminListingStatus)

        etReward.setText(listing.rewardAmount.toString())
        etStatus.setText(listing.status)

        MaterialAlertDialogBuilder(this)
            .setView(view)
            .setNegativeButton("Cancel", null)
            .setNeutralButton("Delete", null)
            .setPositiveButton("Save", null)
            .create()
            .also { dialog ->
                dialog.window?.setBackgroundDrawable(ColorDrawable(android.graphics.Color.TRANSPARENT))
                dialog.setOnShowListener {
                    dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                        val rewardText = etReward.text?.toString()?.trim().orEmpty()
                        val status = etStatus.text?.toString()?.trim().orEmpty()
                        val rewardAmount = rewardText.toDoubleOrNull()
                        if (rewardAmount == null || status.isEmpty()) {
                            Toast.makeText(this, "Valid reward and status are required.", Toast.LENGTH_SHORT).show()
                            return@setOnClickListener
                        }
                        dbHelper.updateListingAdmin(listing.listingId, rewardAmount, status)
                        dialog.dismiss()
                        showListings()
                    }
                    dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_NEUTRAL).setOnClickListener {
                        dbHelper.deleteListing(listing.listingId)
                        dialog.dismiss()
                        showListings()
                    }
                }
            }
            .show()
    }

    private fun showEditOrderDialog(order: Order) {
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_admin_edit_order, null)
        val etStatus = view.findViewById<TextInputEditText>(R.id.etAdminOrderStatus)
        etStatus.setText(order.status)

        MaterialAlertDialogBuilder(this)
            .setView(view)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Save", null)
            .create()
            .also { dialog ->
                dialog.window?.setBackgroundDrawable(ColorDrawable(android.graphics.Color.TRANSPARENT))
                dialog.setOnShowListener {
                    dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                        val status = etStatus.text?.toString()?.trim().orEmpty()
                        if (status.isEmpty()) {
                            Toast.makeText(this, "Status is required.", Toast.LENGTH_SHORT).show()
                            return@setOnClickListener
                        }
                        dbHelper.updateOrderStatus(order.orderId, status)
                        dialog.dismiss()
                        if (status.equals("Completed", ignoreCase = true)) showOrders("Completed", "Completed Orders", "Orders already handed off and finished.")
                        else showOrders("Pending", "Claimed Payments", "Payments completed, handoff still pending.")
                    }
                }
            }
            .show()
    }

    private fun renderItems(items: List<AdminDashboardItem>, emptyMessage: String) {
        adapter.update(items)
        tvEmptyState.text = if (items.isEmpty()) emptyMessage else ""
    }
}
