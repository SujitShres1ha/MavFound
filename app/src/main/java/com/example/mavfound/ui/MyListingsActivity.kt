package com.example.mavfound.ui

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mavfound.R
import com.example.mavfound.database.DatabaseHelper
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton

class MyListingsActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var adapter: ListingAdapter
    private var currentUserId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_listings)

        // 1. Initialize Toolbar - Required for header title & back button
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "My Postings"

        dbHelper = DatabaseHelper(this)

        val prefs = getSharedPreferences("MavFoundPrefs", Context.MODE_PRIVATE)
        currentUserId = prefs.getInt("CURRENT_USER_ID", -1)

        val rv = findViewById<RecyclerView>(R.id.rvMyListings)
        rv.layoutManager = LinearLayoutManager(this)

        val fab = findViewById<ExtendedFloatingActionButton>(R.id.fabPostItem)
        fab.setOnClickListener {
            startActivity(Intent(this, PostItemActivity::class.java))
        }

        if (currentUserId != -1) {
            val list = dbHelper.getMyListings(currentUserId)

            adapter = ListingAdapter(
                listings = list.toMutableList(),
                onDetailClick = {
                    val intent = Intent(this, ListingDetailActivity::class.java)
                    intent.putExtra("LISTING_ID", it.listingId)
                    startActivity(intent)
                },
                onDeleteClick = {
                    dbHelper.deleteListing(it.listingId)
                    refresh()
                }
            )

            rv.adapter = adapter

            // 2. FIX: Use layout_fall_down (the LayoutAnimation) instead of fall_down
            try {
                rv.layoutAnimation = AnimationUtils.loadLayoutAnimation(this, R.anim.layout_fall_down)
                rv.scheduleLayoutAnimation()
            } catch (e: Exception) {
                e.printStackTrace()
            }

            setupSwipeDelete(rv)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    private fun refresh() {
        val updated = dbHelper.getMyListings(currentUserId)
        if (::adapter.isInitialized) {
            adapter.updateData(updated)
        }
    }

    private fun setupSwipeDelete(rv: RecyclerView) {
        val helper = ItemTouchHelper(object :
            ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ) = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val pos = viewHolder.adapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    val item = adapter.listings[pos]
                    dbHelper.deleteListing(item.listingId)
                    refresh()
                    Toast.makeText(this@MyListingsActivity, "Posting removed", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onChildDraw(
                c: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {
                val paint = Paint()
                paint.color = Color.parseColor("#D32F2F")

                val itemView = viewHolder.itemView
                c.drawRect(
                    itemView.right + dX,
                    itemView.top.toFloat(),
                    itemView.right.toFloat(),
                    itemView.bottom.toFloat(),
                    paint
                )

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            }
        })

        helper.attachToRecyclerView(rv)
    }
}