package com.example.lynxlauncher

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.util.Collections

// ItemDragListener remains the same
interface ItemDragListener {
    fun onItemDrag(viewHolder: RecyclerView.ViewHolder)
}

// Click listener for items in MainActivity's grid
interface ServiceItemClickListener {
    fun onServiceItemClick(serviceItem: ServiceItem)
}

class ServiceAdapter(
    val serviceItems: MutableList<ServiceItem>, // Now public for MainActivity to update
    private val dragListener: ItemDragListener,
    private val itemClickListener: ServiceItemClickListener // Add click listener
) : RecyclerView.Adapter<ServiceAdapter.ViewHolder>() {

    init {
        setHasStableIds(true) // Important for stable drag-and-drop and updates
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.grid_item_service, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = serviceItems[position]
        holder.serviceName.text = item.name
        holder.serviceIcon.setImageResource(item.iconResId) // Still using placeholder iconResId
        android.util.Log.d("ServiceAdapter", "Binding item: ${item.name} at position $position") // Logging

        holder.itemView.setOnLongClickListener {
            dragListener.onItemDrag(holder)
            true // Consume the long click
        }
        holder.itemView.setOnClickListener {
            itemClickListener.onServiceItemClick(item)
        }
    }

    override fun getItemCount(): Int {
        android.util.Log.d("ServiceAdapter", "getItemCount: ${serviceItems.size}") // Logging
        return serviceItems.size
    }

    // getItemId should return a stable, unique long.
    // String.hashCode() can have collisions but is often used for simplicity if true uniqueness
    // isn't strictly critical for RecyclerView's internal state beyond animations.
    // For drag and drop, the positions are more directly managed.
    // If ServiceItem.id is a UUID string, its hashCode() can be used.
    override fun getItemId(position: Int): Long = serviceItems[position].id.hashCode().toLong()


    fun onItemMove(fromPosition: Int, toPosition: Int) {
        if (fromPosition < toPosition) {
            for (i in fromPosition until toPosition) {
                Collections.swap(serviceItems, i, i + 1)
            }
        } else {
            for (i in fromPosition downTo toPosition + 1) {
                Collections.swap(serviceItems, i, i - 1)
            }
        }
        notifyItemMoved(fromPosition, toPosition)
        // Note: Persisting the new order should be handled in MainActivity after a move is complete.
    }

    fun updateServices(newServices: List<ServiceItem>) {
        serviceItems.clear()
        serviceItems.addAll(newServices)
        notifyDataSetChanged() // Or use DiffUtil for better performance
    }


    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val serviceIcon: ImageView = itemView.findViewById(R.id.imageViewServiceIcon)
        val serviceName: TextView = itemView.findViewById(R.id.textViewServiceName)
    }
}
