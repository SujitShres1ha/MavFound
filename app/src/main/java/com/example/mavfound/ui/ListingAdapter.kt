package com.example.mavfound.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mavfound.R
import com.example.mavfound.database.DatabaseHelper
import com.example.mavfound.models.Listing
import com.google.android.material.button.MaterialButton
import java.io.File

class ListingAdapter(
    val listings: MutableList<Listing>,
    private val onDetailClick: (Listing) -> Unit,
    private val onDeleteClick: (Listing) -> Unit
) : RecyclerView.Adapter<ListingAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle: TextView = itemView.findViewById(R.id.tvItemTitle)
        val tvLocation: TextView = itemView.findViewById(R.id.tvItemLoc)
        val tvStatus: TextView = itemView.findViewById(R.id.tvItemStatus)
        val ivThumb: ImageView = itemView.findViewById(R.id.ivItemThumb)
        val btnDelete: MaterialButton = itemView.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_listing, parent, false)
        return ViewHolder(view)
    }

    // In main/java/com/example/mavfound/ui/ListingAdapter.kt

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = listings[position]
        val dbHelper = DatabaseHelper(holder.itemView.context)

        // Set basic item data
        holder.tvTitle.text = item.title
        holder.tvLocation.text = item.location

        // NEW HANDSHAKE LOGIC: Check for pending claims
        val claimCount = dbHelper.getClaimCountForListing(item.listingId)

        if (item.status.equals("Claimed", ignoreCase = true)) {
            holder.tvStatus.text = "CLAIMED"
            holder.tvStatus.setTextColor(android.graphics.Color.parseColor("#F59E0B"))
        } else if (claimCount > 0) {
            // Highlight that people are waiting for verification
            holder.tvStatus.text = "$claimCount PENDING CLAIMS"
            holder.tvStatus.setTextColor(android.graphics.Color.parseColor("#F59E0B")) // Warning Orange
        } else {
            holder.tvStatus.text = item.status
            holder.tvStatus.setTextColor(android.graphics.Color.parseColor("#0064B1")) // Default Blue
        }

        // Set up click listeners for details and deletion
        holder.itemView.setOnClickListener { onDetailClick(item) }
        holder.btnDelete.setOnClickListener { onDeleteClick(item) }
    }

    override fun getItemCount(): Int = listings.size

    fun updateData(newList: List<Listing>) {
        listings.clear()
        listings.addAll(newList)
        notifyDataSetChanged()
    }
}
