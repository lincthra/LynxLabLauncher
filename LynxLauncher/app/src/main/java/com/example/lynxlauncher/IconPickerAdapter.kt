package com.example.lynxlauncher

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class IconPickerAdapter(
    private val icons: List<Pair<Int, String>>,
    private val listener: (Int, String) -> Unit
) : RecyclerView.Adapter<IconPickerAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.grid_item_icon, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val (iconResId, iconIdentifier) = icons[position]
        holder.iconImage.setImageResource(iconResId)
        holder.iconName.text = iconIdentifier.replace("ic_service_", "").replace("_", " ").replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        holder.itemView.setOnClickListener {
            listener(iconResId, iconIdentifier)
        }
    }

    override fun getItemCount(): Int = icons.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val iconImage: ImageView = itemView.findViewById(R.id.imageViewGridIcon)
        val iconName: TextView = itemView.findViewById(R.id.textViewGridIconName)
    }
}
