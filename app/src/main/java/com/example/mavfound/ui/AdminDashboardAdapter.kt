package com.example.mavfound.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mavfound.R

data class AdminDashboardItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val status: String
)

class AdminDashboardAdapter(
    private val onClick: (AdminDashboardItem) -> Unit
) : RecyclerView.Adapter<AdminDashboardAdapter.ViewHolder>() {
    private val items = mutableListOf<AdminDashboardItem>()

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.tvAdminItemTitle)
        val subtitle: TextView = itemView.findViewById(R.id.tvAdminItemSubtitle)
        val status: TextView = itemView.findViewById(R.id.tvAdminItemStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_admin_dashboard, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.title.text = item.title
        holder.subtitle.text = item.subtitle
        holder.status.text = item.status.uppercase()
        holder.status.setTextColor(
            if (item.status.equals("Completed", ignoreCase = true)) {
                android.graphics.Color.parseColor("#0F5BD8")
            } else {
                android.graphics.Color.parseColor("#F59E0B")
            }
        )
        holder.itemView.setOnClickListener { onClick(item) }
    }

    override fun getItemCount(): Int = items.size

    fun update(newItems: List<AdminDashboardItem>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }
}
