package com.example.mavfound.ui

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mavfound.R
import com.example.mavfound.models.Listing

class FeedAdapter(private var itemList: List<Listing>) : RecyclerView.Adapter<FeedAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvItemTitle: TextView = itemView.findViewById(R.id.tvItemTitle)
        val tvLocation: TextView = itemView.findViewById(R.id.tvItemLoc)
        val tvReward: TextView = itemView.findViewById(R.id.tvReward)
        val tvStatus: TextView = itemView.findViewById(R.id.tvItemStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_feed, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = itemList[position]
        val context = holder.itemView.context

        holder.tvItemTitle.text = currentItem.title
        holder.tvLocation.text = "${currentItem.location} • ${currentItem.dateTime}"
        holder.tvReward.text = if (currentItem.rewardAmount > 0) {
            String.format("Reward: $%.2f", currentItem.rewardAmount)
        } else {
            "No Reward Requested"
        }
        holder.tvStatus.text = currentItem.status
        holder.tvStatus.setTextColor(
            if (currentItem.status.equals("Claimed", ignoreCase = true)) {
                android.graphics.Color.parseColor("#F59E0B")
            } else {
                android.graphics.Color.parseColor("#0064B1")
            }
        )

        holder.itemView.setOnClickListener {
            val intent = Intent(context, ListingDetailActivity::class.java)
            intent.putExtra("LISTING_ID", currentItem.listingId)
            context.startActivity(intent)
        }
    }

    override fun getItemCount() = itemList.size

    fun updateList(newList: List<Listing>) {
        itemList = newList
        notifyDataSetChanged()
    }
}
