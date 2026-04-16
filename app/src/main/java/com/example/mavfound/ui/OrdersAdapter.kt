package com.example.mavfound.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mavfound.R
import com.example.mavfound.models.Order

class OrdersAdapter(
    private val orders: List<Order>,
    private val onClick: (Order) -> Unit
) : RecyclerView.Adapter<OrdersAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle: TextView = itemView.findViewById(R.id.tvOrderTitle)
        val tvStatus: TextView = itemView.findViewById(R.id.tvOrderStatus)
        val tvMeta: TextView = itemView.findViewById(R.id.tvOrderMeta)
        val tvCode: TextView = itemView.findViewById(R.id.tvOrderCode)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_order, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val order = orders[position]
        holder.tvTitle.text = order.listingTitle
        holder.tvStatus.text = order.status
        holder.tvMeta.text = "${String.format("$%.2f", order.amount)} • ${order.paymentDate}"
        holder.tvCode.text = "Code: ${order.handoffCode}"
        holder.itemView.setOnClickListener { onClick(order) }
    }

    override fun getItemCount(): Int = orders.size
}
