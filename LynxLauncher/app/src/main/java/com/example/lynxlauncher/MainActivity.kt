package com.example.lynxlauncher

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.lynxlauncher.databinding.ActivityMainBinding // Restored ViewBinding

class MainActivity : AppCompatActivity(), ItemDragListener, ServiceItemClickListener {

    private lateinit var binding: ActivityMainBinding // Restored ViewBinding
    // private lateinit var recyclerViewServices: RecyclerView // Removed for ViewBinding
    private lateinit var serviceAdapter: ServiceAdapter
    private lateinit var itemTouchHelper: ItemTouchHelper
    private val displayedServiceItems = mutableListOf<ServiceItem>() 
    private lateinit var serviceRepository: ServiceRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater) // Restored ViewBinding
        setContentView(binding.root) // Restored ViewBinding
        // recyclerViewServices = findViewById(R.id.recyclerViewServices) // Removed

        serviceRepository = ServiceRepository(applicationContext)
        setupRecyclerView()
    }

    override fun onResume() {
        super.onResume()
        loadServicesAndUpdateAdapter()
    }

    private fun loadServicesAndUpdateAdapter() {
        val servicesFromRepo = serviceRepository.getAllServices()
        android.util.Log.d("MainActivity", "Services from repo: $servicesFromRepo") // Logging
        displayedServiceItems.clear()
        
        servicesFromRepo.forEach { serviceItem ->
            val iconRes = if (serviceItem.iconIdentifier != null) {
                resources.getIdentifier(serviceItem.iconIdentifier, "drawable", packageName)
                    .takeIf { it != 0 } ?: android.R.drawable.sym_def_app_icon 
            } else {
                serviceItem.iconResId.takeIf { it != 0 } ?: android.R.drawable.sym_def_app_icon 
            }
            displayedServiceItems.add(serviceItem.copy(iconResId = iconRes))
        }
        android.util.Log.d("MainActivity", "Displayed service items: $displayedServiceItems") // Logging

        if (::serviceAdapter.isInitialized) {
            serviceAdapter.updateServices(displayedServiceItems)
            android.util.Log.d("MainActivity", "Adapter updated with items.") // Logging
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_configure -> {
                startActivity(Intent(this, ConfigActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setupRecyclerView() {
        val spanCount = 3 
        binding.recyclerViewServices.layoutManager = GridLayoutManager(this, spanCount) // Use ViewBinding
        
        serviceAdapter = ServiceAdapter(displayedServiceItems, this, this)
        binding.recyclerViewServices.adapter = serviceAdapter // Use ViewBinding

        val callback = object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN or ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT,
            0 
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                val fromPosition = viewHolder.adapterPosition
                val toPosition = target.adapterPosition
                serviceAdapter.onItemMove(fromPosition, toPosition)
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                // Not used
            }

            override fun isLongPressDragEnabled(): Boolean {
                return false 
            }

            override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
                super.onSelectedChanged(viewHolder, actionState)
                if (actionState == ItemTouchHelper.ACTION_STATE_DRAG) {
                    viewHolder?.itemView?.alpha = 0.7f
                }
            }

            override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
                super.clearView(recyclerView, viewHolder)
                viewHolder.itemView.alpha = 1.0f
                serviceRepository.updateAllServices(serviceAdapter.serviceItems)
                Toast.makeText(this@MainActivity, "Order saved", Toast.LENGTH_SHORT).show()
            }
        }
        itemTouchHelper = ItemTouchHelper(callback)
        itemTouchHelper.attachToRecyclerView(binding.recyclerViewServices) // Use ViewBinding
    }

    override fun onItemDrag(viewHolder: RecyclerView.ViewHolder) {
        itemTouchHelper.startDrag(viewHolder)
    }

    override fun onServiceItemClick(serviceItem: ServiceItem) {
        val scheme = if (serviceItem.port == 443 || serviceItem.url.startsWith("https://")) "https" 
                     else if (serviceItem.url.startsWith("http://")) "http"
                     else "http" 

        val portString = serviceItem.port?.let {
            // Only add port to string if it's not a default port for the scheme
            if (!((scheme == "http" && it == 80) || (scheme == "https" && it == 443))) ":$it" else ""
        } ?: ""

        // Normalize the base URL: remove any existing scheme or port, as we'll add them based on serviceItem.port
        var host = serviceItem.url
        if (host.contains("://")) {
            host = host.substringAfter("://")
        }
        // Remove any port and path from the host string for clean reconstruction
        host = host.split("/").first().split(":").first()
        
        // Path and query handling: Re-append if they existed
        var pathAndQuery = ""
        if (serviceItem.url.contains("/")) {
            pathAndQuery = serviceItem.url.substring(serviceItem.url.indexOf("/"))
        }


        // Construct the full URL using the determined scheme, host (from serviceItem.url, cleaned),
        // and port (from serviceItem.port via portString).
        val fullUrl = "$scheme://$host$portString$pathAndQuery"
        
        android.util.Log.d("MainActivity", "Constructed URL: $fullUrl from item: $serviceItem")


        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(fullUrl))
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "Could not open URL: $fullUrl", Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }
}
