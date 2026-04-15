package com.example.mavfound

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class LostItemAdapter(private var itemList: List<LostItem>) :
    RecyclerView.Adapter<LostItemAdapter.LostItemViewHolder>() {

    class LostItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvItemTitle: TextView = itemView.findViewById(R.id.tvItemTitle)
        val tvCategory: TextView = itemView.findViewById(R.id.tvCategory)
        val tvLocation: TextView = itemView.findViewById(R.id.tvLocation)
        val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        val tvReward: TextView = itemView.findViewById(R.id.tvReward)
        val btnClaimItem: Button = itemView.findViewById(R.id.btnClaimItem)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LostItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_lost, parent, false)
        return LostItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: LostItemViewHolder, position: Int) {
        val currentItem = itemList[position]

        holder.tvItemTitle.text = currentItem.title
        holder.tvCategory.text = "Category: ${currentItem.category}"
        holder.tvLocation.text = "Location: ${currentItem.location}"
        holder.tvDate.text = "Date: ${currentItem.date}"
        holder.tvReward.text = "Reward: ${currentItem.reward}"

        holder.btnClaimItem.setOnClickListener {
            val intent = Intent(holder.itemView.context, VerificationActivity::class.java)
            intent.putExtra("securityQuestion", currentItem.securityQuestion)
            intent.putExtra("correctAnswer", currentItem.correctAnswer)
            intent.putExtra("itemTitle", currentItem.title)
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    fun updateList(newList: List<LostItem>) {
        itemList = newList
        notifyDataSetChanged()
    }
}