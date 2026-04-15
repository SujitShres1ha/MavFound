package com.example.mavfound.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.mavfound.R
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
        val tvLoc: TextView = itemView.findViewById(R.id.tvItemLoc)
        val tvStatus: TextView = itemView.findViewById(R.id.tvItemStatus)
        val ivThumb: ImageView = itemView.findViewById(R.id.ivItemThumb)
        val btnDelete: MaterialButton = itemView.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_listing, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = listings[position]

        holder.tvTitle.text = item.title
        holder.tvLoc.text = item.location
        holder.tvStatus.text = item.status.uppercase()

        if (!item.imagePath.isNullOrEmpty()) {
            val file = File(item.imagePath)
            if (file.exists()) {
                Glide.with(holder.itemView.context)
                    .load(file)
                    .placeholder(R.drawable.ic_search)
                    .error(R.drawable.ic_search)
                    .centerCrop()
                    .into(holder.ivThumb)
            } else {
                // If not a file path, try as URI string
                Glide.with(holder.itemView.context)
                    .load(item.imagePath)
                    .placeholder(R.drawable.ic_search)
                    .error(R.drawable.ic_search)
                    .centerCrop()
                    .into(holder.ivThumb)
            }
        } else {
            holder.ivThumb.setImageResource(R.drawable.ic_search)
        }

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
