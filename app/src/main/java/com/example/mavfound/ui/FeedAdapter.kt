package com.example.mavfound.ui

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mavfound.R
import com.example.mavfound.models.Listing

class FeedAdapter(private var itemList: List<Listing>) : RecyclerView.Adapter<FeedAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvItemTitle: TextView = itemView.findViewById(R.id.tvItemTitle)
        // FIX: Changed from tvLocation to tvItemLoc to match your XML
        val tvLocation: TextView = itemView.findViewById(R.id.tvItemLoc)
        val tvReward: TextView = itemView.findViewById(R.id.tvReward)
        val btnClaimItem: Button = itemView.findViewById(R.id.btnClaimItem)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_feed, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = itemList[position]
        val context = holder.itemView.context

        val sharedPrefs = context.getSharedPreferences("MavFoundPrefs", Context.MODE_PRIVATE)
        val currentUserId = sharedPrefs.getInt("CURRENT_USER_ID", -1)

        holder.tvItemTitle.text = currentItem.title
        holder.tvLocation.text = "${currentItem.location} • ${currentItem.dateTime}"

        if (currentItem.rewardAmount > 0) {
            holder.tvReward.text = String.format("Reward: $%.2f", currentItem.rewardAmount)
        } else {
            holder.tvReward.text = "No Reward Requested"
        }

        // Logical Check: Users cannot claim their own found items
        if (currentItem.listerId == currentUserId) {
            holder.btnClaimItem.isEnabled = false
            holder.btnClaimItem.text = "Your Posting"
            holder.btnClaimItem.alpha = 0.5f
        } else {
            holder.btnClaimItem.isEnabled = true
            holder.btnClaimItem.text = "Claim Item"
            holder.btnClaimItem.alpha = 1.0f

            holder.btnClaimItem.setOnClickListener {
                val intent = Intent(context, VerificationActivity::class.java)
                // Pass the unique Listing ID for the database handshake
                intent.putExtra("LISTING_ID", currentItem.listingId)
                intent.putExtra("itemTitle", currentItem.title)
                intent.putExtra("securityQuestion", currentItem.securityQuestion)
                context.startActivity(intent)
            }
        }
    }

    override fun getItemCount() = itemList.size

    fun updateList(newList: List<Listing>) {
        itemList = newList
        notifyDataSetChanged()
    }
}