package com.example.mavfound

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerViewItems: RecyclerView
    private lateinit var lostItemAdapter: LostItemAdapter
    private lateinit var lostItemList: ArrayList<LostItem>
    private lateinit var etSearch: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        etSearch = findViewById(R.id.etSearch)

        val btnGoToVerification = findViewById<Button>(R.id.btnGoToVerification)
        btnGoToVerification.setOnClickListener {
            val intent = Intent(this, VerificationActivity::class.java)
            startActivity(intent)
        }

        recyclerViewItems = findViewById(R.id.recyclerViewItems)
        recyclerViewItems.layoutManager = LinearLayoutManager(this)
        recyclerViewItems.setHasFixedSize(true)

        lostItemList = arrayListOf(
            LostItem(
                "iPhone 14",
                "Electronics",
                "UTA Library",
                "Apr 10, 2026",
                "$20",
                "What color is the phone case?",
                "blue"
            ),
            LostItem(
                "Blue Backpack",
                "Bags",
                "UC Building",
                "Apr 11, 2026",
                "$15",
                "What brand is on the backpack?",
                "nike"
            ),
            LostItem(
                "Student ID Card",
                "Documents",
                "Pickard Hall",
                "Apr 12, 2026",
                "$10",
                "What is the first name on the ID?",
                "isneha"
            ),
            LostItem(
                "AirPods Case",
                "Electronics",
                "Central Library",
                "Apr 13, 2026",
                "$25",
                "What color is the AirPods case cover?",
                "pink"
            ),
            LostItem(
                "Water Bottle",
                "Personal Item",
                "ERB",
                "Apr 14, 2026",
                "$5",
                "What brand is the bottle?",
                "hydro flask"
            )
        )

        lostItemAdapter = LostItemAdapter(lostItemList)
        recyclerViewItems.adapter = lostItemAdapter

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterItems(s.toString())
            }
        })
    }

    private fun filterItems(query: String) {
        val filteredList = lostItemList.filter {
            it.title.contains(query, ignoreCase = true) ||
                    it.category.contains(query, ignoreCase = true) ||
                    it.location.contains(query, ignoreCase = true)
        }
        lostItemAdapter.updateList(filteredList)
    }
}