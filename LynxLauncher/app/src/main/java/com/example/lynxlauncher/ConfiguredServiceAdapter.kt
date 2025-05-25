package com.example.lynxlauncher

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ConfiguredServiceAdapter(
    private val configuredServices: MutableList<ConfiguredServiceItem>,
    private val onRemoveClick: (ConfiguredServiceItem) -> Unit,
    private val onTestClick: (ConfiguredServiceItem) -> Unit
) : RecyclerView.Adapter<ConfiguredServiceAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_configured_service, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = configuredServices[position]
        holder.serviceName.text = item.name
        holder.serviceUrl.text = item.url // Consider showing URL and Port separately or combined
        holder.serviceIcon.setImageResource(item.iconResId) // Placeholder icon logic

        // Display the port
        if (item.port != null) {
            holder.servicePort.text = "Port: ${item.port}"
            holder.servicePort.visibility = View.VISIBLE
        } else {
            holder.servicePort.visibility = View.GONE
        }

        holder.removeButton.setOnClickListener { onRemoveClick(item) }
        holder.testButton.setOnClickListener { onTestClick(item) }
    }

    override fun getItemCount(): Int = configuredServices.size

    fun addItem(item: ConfiguredServiceItem) {
        configuredServices.add(item)
        notifyItemInserted(configuredServices.size - 1)
    }

    fun removeItem(item: ConfiguredServiceItem) {
        val position = configuredServices.indexOf(item)
        if (position >= 0) {
            configuredServices.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val serviceIcon: ImageView = itemView.findViewById(R.id.imageViewConfiguredServiceIcon)
        val serviceName: TextView = itemView.findViewById(R.id.textViewConfiguredServiceName)
        val serviceUrl: TextView = itemView.findViewById(R.id.textViewConfiguredServiceUrl)
        val servicePort: TextView = itemView.findViewById(R.id.textViewConfiguredServicePort) // Added
        val removeButton: Button = itemView.findViewById(R.id.buttonRemoveService)
        val testButton: Button = itemView.findViewById(R.id.buttonTestService)
    }
}
