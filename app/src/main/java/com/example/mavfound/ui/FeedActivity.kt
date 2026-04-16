package com.example.mavfound.ui

import android.graphics.drawable.AnimationDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.animation.AnimationUtils
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mavfound.R
import com.example.mavfound.database.DatabaseHelper
import com.example.mavfound.models.Listing
import com.google.android.material.button.MaterialButton

class FeedActivity : AppCompatActivity() {

    private lateinit var recyclerViewItems: RecyclerView
    private lateinit var feedAdapter: FeedAdapter
    private lateinit var dbHelper: DatabaseHelper
    private var allListings: List<Listing> = listOf()

    private lateinit var etSearch: EditText
    private var currentCategoryFilter = "" // Empty means "All"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feed)

        // Animated Background
        val rootLayout = findViewById<CoordinatorLayout>(R.id.feedRootLayout)
        val animationDrawable = rootLayout.background as AnimationDrawable
        animationDrawable.setEnterFadeDuration(2000)
        animationDrawable.setExitFadeDuration(4000)
        animationDrawable.start()

        dbHelper = DatabaseHelper(this)

        etSearch = findViewById(R.id.etSearch)
        recyclerViewItems = findViewById(R.id.recyclerViewItems)
        recyclerViewItems.layoutManager = LinearLayoutManager(this)

        // FETCH REAL DATA FROM DATABASE
        allListings = dbHelper.getAllAvailableListings()

        feedAdapter = FeedAdapter(allListings)
        recyclerViewItems.adapter = feedAdapter

        // Initial Entry Animation
        animateRecyclerView()

        setupSwitchingButtons()

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                applyFilters()
            }
        })
    }

    private fun setupSwitchingButtons() {
        val btnAll = findViewById<MaterialButton>(R.id.btnFilterAll)
        val btnTech = findViewById<MaterialButton>(R.id.btnFilterTech)
        val btnKeys = findViewById<MaterialButton>(R.id.btnFilterKeys)
        val btnBags = findViewById<MaterialButton>(R.id.btnFilterBags)

        val buttons = listOf(btnAll, btnTech, btnKeys, btnBags)

        val clickListener = android.view.View.OnClickListener { view ->
            val clickedButton = view as MaterialButton

            // 1. Reset all buttons to an "Unselected" (Outlined) style
            buttons.forEach {
                it.setBackgroundColor(android.graphics.Color.TRANSPARENT)
                it.strokeWidth = 3
                it.strokeColor = android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#CBD5E1")) // Light Gray
                it.setTextColor(android.graphics.Color.parseColor("#64748B")) // Slate Gray Text
            }

            // 2. Set the clicked button to a "Selected" (Tonal) style
            clickedButton.setBackgroundColor(android.graphics.Color.parseColor("#E6F0F9")) // Soft MavFound Blue
            clickedButton.strokeWidth = 0
            clickedButton.setTextColor(android.graphics.Color.parseColor("#0064B1")) // Primary Blue Text

            // 3. Update the filter logic
            currentCategoryFilter = when (view.id) {
                R.id.btnFilterTech -> "phone|laptop|earbuds|airpods|charger|tech"
                R.id.btnFilterKeys -> "key|id|wallet|card"
                R.id.btnFilterBags -> "bag|backpack|purse"
                else -> "" // All
            }

            applyFilters()
            animateRecyclerView() // Re-animate the list for interactivity!
        }

        // Attach listeners to all buttons
        buttons.forEach { it.setOnClickListener(clickListener) }

        // Simulate a click on "All Items" so it highlights by default when the page opens
        btnAll.performClick()
    }

    private fun applyFilters() {
        val query = etSearch.text.toString()

        val filteredList = allListings.filter { item ->
            val matchesSearch = item.title.contains(query, ignoreCase = true) ||
                    item.description.contains(query, ignoreCase = true) ||
                    item.location.contains(query, ignoreCase = true)

            val matchesCategory = if (currentCategoryFilter.isEmpty()) {
                true
            } else {
                // If the title or description contains ANY of the category keywords
                val keywords = currentCategoryFilter.split("|")
                keywords.any { keyword ->
                    item.title.contains(keyword, true) || item.description.contains(keyword, true)
                }
            }

            matchesSearch && matchesCategory
        }
        feedAdapter.updateList(filteredList)
    }

    private fun animateRecyclerView() {
        val context = recyclerViewItems.context
        val controller = AnimationUtils.loadLayoutAnimation(context, R.anim.layout_fall_down)
        recyclerViewItems.layoutAnimation = controller
        recyclerViewItems.adapter?.notifyDataSetChanged()
        recyclerViewItems.scheduleLayoutAnimation()
    }
}