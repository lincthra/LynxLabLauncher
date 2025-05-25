package com.example.lynxlauncher

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.lynxlauncher.databinding.ActivityConfigBinding // Restored
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.atomic.AtomicLong

class ConfigActivity : AppCompatActivity(), ConfigActivity.IconSelectionListener {

    private lateinit var binding: ActivityConfigBinding // Restored
    private lateinit var configuredServiceAdapter: ConfiguredServiceAdapter
    private val displayedConfiguredServices = mutableListOf<ConfiguredServiceItem>() 
    private lateinit var serviceRepository: ServiceRepository
    private val localIdCounter = AtomicLong(0) 

    private var currentSelectedIconResId: Int = android.R.drawable.sym_def_app_icon
    private var currentSelectedIconIdentifier: String = "default_icon"

    // Listener for IconPickerDialogFragment
    interface IconSelectionListener {
        fun onIconSelected(iconResId: Int, iconIdentifier: String)
    }

    companion object {
        val availableIcons = listOf(
            Pair(R.drawable.ic_service_home_assistant, "ic_service_home_assistant"),
            Pair(R.drawable.ic_service_proxmox, "ic_service_proxmox"),
            Pair(R.drawable.ic_service_frigate, "ic_service_frigate"),
            Pair(R.drawable.ic_service_double_take, "ic_service_double_take"),
            Pair(R.drawable.ic_service_nas, "ic_service_nas"),
            Pair(R.drawable.ic_service_duplicati, "ic_service_duplicati"),
            Pair(R.drawable.ic_service_compreface, "ic_service_compreface"),
            Pair(R.drawable.ic_service_mqtt, "ic_service_mqtt"),
            Pair(android.R.drawable.sym_def_app_icon, "default_icon")
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityConfigBinding.inflate(layoutInflater) // Restored
        setContentView(binding.root) // Restored

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.title_activity_config)

        serviceRepository = ServiceRepository(applicationContext)

        setupRecyclerView()
        setupClickListeners()
        loadConfiguredServices()
    }

    private fun setupRecyclerView() {
        configuredServiceAdapter = ConfiguredServiceAdapter(
            displayedConfiguredServices,
            onRemoveClick = { item -> removeServiceItem(item) },
            onTestClick = { item -> testServiceItem(item) }
        )
        binding.recyclerViewConfiguredServices.layoutManager = LinearLayoutManager(this) // Use ViewBinding
        binding.recyclerViewConfiguredServices.adapter = configuredServiceAdapter // Use ViewBinding
    }

    private fun setupClickListeners() {
        binding.buttonAddService.setOnClickListener { // Use ViewBinding
            addService()
        }
        binding.buttonSelectIcon.setOnClickListener { // Use ViewBinding
            // Instead of cycling, show the IconPickerDialogFragment
            // val dialog = IconPickerDialogFragment()
            // Set ConfigActivity as the target fragment to receive callback
            // This method is deprecated, but often used for simple DialogFragment -> Activity communication.
            // For more complex scenarios, consider a Shared ViewModel or a Fragment Result API.
            // dialog.setTargetFragment(null, 0) // Not applicable for Activity, use listener interface
            // dialog.show(supportFragmentManager, "IconPickerDialogFragment")
            Toast.makeText(this, "Icon selection temporarily disabled", Toast.LENGTH_SHORT).show()
        }
    }

    // Implement the IconSelectionListener
    // This method will be called by IconPickerDialogFragment when an icon is selected
    override fun onIconSelected(iconResId: Int, iconIdentifier: String) { // Added override
        currentSelectedIconResId = iconResId
        currentSelectedIconIdentifier = iconIdentifier
        binding.imageViewSelectedIcon.setImageResource(currentSelectedIconResId)
        Toast.makeText(this, "Icon selected: ${iconIdentifier.replace("ic_service_", "")}", Toast.LENGTH_SHORT).show()
    }

    private fun loadConfiguredServices() {
        val servicesFromRepo = serviceRepository.getAllServices()
        displayedConfiguredServices.clear()
        servicesFromRepo.forEach { serviceItem ->
            val iconRes = if (serviceItem.iconIdentifier != null) {
                resources.getIdentifier(serviceItem.iconIdentifier, "drawable", packageName)
                    .takeIf { it != 0 } ?: android.R.drawable.sym_def_app_icon
            } else {
                serviceItem.iconResId.takeIf { it != 0 } ?: android.R.drawable.sym_def_app_icon
            }

            displayedConfiguredServices.add(
                ConfiguredServiceItem(
                    localId = localIdCounter.incrementAndGet(),
                    persistentId = serviceItem.id,
                    name = serviceItem.name,
                    url = serviceItem.url,
                    port = serviceItem.port,
                    iconResId = iconRes, 
                    iconIdentifier = serviceItem.iconIdentifier
                )
            )
        }
        configuredServiceAdapter.notifyDataSetChanged()
        
        if (servicesFromRepo.isEmpty()) { 
            val placeholderServices = listOf(
                ServiceItem(serviceRepository.generateNewId(),"Home Assistant", "homeassistant.local", 8123, R.drawable.ic_service_home_assistant, "ic_service_home_assistant"),
                ServiceItem(serviceRepository.generateNewId(),"NAS", "mynas.local", null, R.drawable.ic_service_nas, "ic_service_nas")
            )
            placeholderServices.forEach { serviceRepository.addService(it) }
            // Update displayedConfiguredServices directly instead of recursive call
            displayedConfiguredServices.clear()
            val updatedServicesFromRepo = serviceRepository.getAllServices()
            updatedServicesFromRepo.forEach { serviceItem ->
                val iconRes = if (serviceItem.iconIdentifier != null) {
                    resources.getIdentifier(serviceItem.iconIdentifier, "drawable", packageName)
                        .takeIf { it != 0 } ?: android.R.drawable.sym_def_app_icon
                } else {
                    serviceItem.iconResId.takeIf { it != 0 } ?: android.R.drawable.sym_def_app_icon
                }
                displayedConfiguredServices.add(
                    ConfiguredServiceItem(
                        localId = localIdCounter.incrementAndGet(),
                        persistentId = serviceItem.id,
                        name = serviceItem.name,
                        url = serviceItem.url,
                        port = serviceItem.port,
                        iconResId = iconRes,
                        iconIdentifier = serviceItem.iconIdentifier
                    )
                )
            }
            configuredServiceAdapter.notifyDataSetChanged()
        }
    }

    private fun addService() {
        val name = binding.editTextServiceName.text.toString().trim() // Use ViewBinding
        val urlString = binding.editTextServiceUrl.text.toString().trim() // Use ViewBinding
        val portString = binding.editTextServicePort.text.toString().trim() // Use ViewBinding for new port field

        if (name.isBlank() || urlString.isBlank()) {
            Toast.makeText(this, "Service name and URL cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }

        val explicitPort: Int? = if (portString.isNotEmpty()) {
            try {
                portString.toInt()
            } catch (e: NumberFormatException) {
                Toast.makeText(this, "Invalid port number", Toast.LENGTH_SHORT).show()
                return
            }
        } else {
            null
        }

        val (baseUrl, parsedUrlPort) = parseUrlAndPort(urlString, explicitPort)
        if (baseUrl.isEmpty()) {
            Toast.makeText(this, "Invalid URL format", Toast.LENGTH_SHORT).show()
            return
        }
        
        val finalPort = explicitPort ?: parsedUrlPort

        val persistentId = serviceRepository.generateNewId()
        val newServiceForRepo = ServiceItem(
            id = persistentId,
            name = name,
            url = baseUrl,
            port = finalPort, // Use the determined port
            iconResId = currentSelectedIconResId, 
            iconIdentifier = currentSelectedIconIdentifier
        )
        serviceRepository.addService(newServiceForRepo)

        val newConfiguredItem = ConfiguredServiceItem(
            localId = localIdCounter.incrementAndGet(),
            persistentId = persistentId,
            name = name,
            url = baseUrl,
            port = finalPort, // Use the determined port
            iconResId = currentSelectedIconResId,
            iconIdentifier = currentSelectedIconIdentifier
        )
        configuredServiceAdapter.addItem(newConfiguredItem)

        binding.editTextServiceName.text?.clear() // Use ViewBinding
        binding.editTextServiceUrl.text?.clear() // Use ViewBinding
        binding.editTextServicePort.text?.clear() // Clear the new port field
        currentSelectedIconResId = android.R.drawable.sym_def_app_icon 
        currentSelectedIconIdentifier = "default_icon"
        binding.imageViewSelectedIcon.setImageResource(currentSelectedIconResId) // Use ViewBinding

        Toast.makeText(this, "$name added", Toast.LENGTH_SHORT).show()
    }
    
    private fun parseUrlAndPort(urlString: String, explicitPort: Int?): Pair<String, Int?> {
        return try {
            // Normalize URL by adding scheme if missing, to allow URL parsing
            val fullUrl = if (!urlString.startsWith("http://", ignoreCase = true) && !urlString.startsWith("https://", ignoreCase = true)) {
                "http://$urlString" // Default to http for parsing if scheme is missing
            } else {
                urlString
            }
            
            val parsed = URL(fullUrl)
            val host = parsed.host
            
            // If an explicit port is provided, it takes precedence.
            // Otherwise, try to get it from the URL.
            val portFromUrl = if (parsed.port != -1) parsed.port else null
            
            // Return the host (which is the URL without scheme, port, path for our purpose)
            // and the port (explicit if provided, otherwise from URL, or null).
            Pair(host, explicitPort ?: portFromUrl)
        } catch (e: Exception) {
            // If URL parsing fails, return empty host and null port.
            // Consider if urlString itself should be returned as host if it doesn't contain scheme/port.
            // For now, aligning with previous behavior of returning empty on parse failure.
            Pair("", null) 
        }
    }

    private fun removeServiceItem(item: ConfiguredServiceItem) {
        serviceRepository.removeService(item.persistentId)
        configuredServiceAdapter.removeItem(item) 
        Toast.makeText(this, "${item.name} removed", Toast.LENGTH_SHORT).show()
    }

    private fun testServiceItem(item: ConfiguredServiceItem) {
        val scheme = if (item.port == 443 || item.url.startsWith("https")) "https" else "http"
        val portString = item.port?.let { if (it != 80 && it != 443) ":$it" else "" } ?: ""
        val testUrlString = "$scheme://${item.url}$portString"

        Toast.makeText(this, "Testing $testUrlString...", Toast.LENGTH_SHORT).show()

        lifecycleScope.launch {
            val isReachable = checkServiceReachable(testUrlString)
            if (isReachable) {
                Toast.makeText(applicationContext, "${item.name} is reachable!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(applicationContext, "Failed to reach ${item.name}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private suspend fun checkServiceReachable(urlString: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val url = URL(urlString)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "HEAD" 
            connection.connectTimeout = 3000 
            connection.readTimeout = 3000
            val responseCode = connection.responseCode
            return@withContext responseCode in 200..399 || responseCode == HttpURLConnection.HTTP_NOT_IMPLEMENTED || responseCode == HttpURLConnection.HTTP_BAD_METHOD
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext false
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
