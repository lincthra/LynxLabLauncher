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

class ConfigActivity : AppCompatActivity() {

    private lateinit var binding: ActivityConfigBinding // Restored
    private lateinit var configuredServiceAdapter: ConfiguredServiceAdapter
    private val displayedConfiguredServices = mutableListOf<ConfiguredServiceItem>() 
    private lateinit var serviceRepository: ServiceRepository
    private val localIdCounter = AtomicLong(0) 

    private var currentSelectedIconResId: Int = android.R.drawable.sym_def_app_icon
    private var currentSelectedIconIdentifier: String = "default_icon" 

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
            val icons = listOf(
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
            val currentIndex = icons.indexOfFirst { it.first == currentSelectedIconResId }
            val nextIndex = if (currentIndex == -1 || currentIndex == icons.size - 1) 0 else currentIndex + 1
            
            currentSelectedIconResId = icons[nextIndex].first
            currentSelectedIconIdentifier = icons[nextIndex].second
            
            binding.imageViewSelectedIcon.setImageResource(currentSelectedIconResId) // Use ViewBinding
            Toast.makeText(this, "Icon: ${currentSelectedIconIdentifier.replace("ic_service_", "")}", Toast.LENGTH_SHORT).show()
        }
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
            loadConfiguredServices() 
        }
    }

    private fun addService() {
        val name = binding.editTextServiceName.text.toString().trim() // Use ViewBinding
        val urlString = binding.editTextServiceUrl.text.toString().trim() // Use ViewBinding

        if (name.isBlank() || urlString.isBlank()) {
            Toast.makeText(this, "Service name and URL cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }

        val (baseUrl, port) = parseUrlAndPort(urlString)
        if (baseUrl.isEmpty()) {
            Toast.makeText(this, "Invalid URL format", Toast.LENGTH_SHORT).show()
            return
        }

        val persistentId = serviceRepository.generateNewId()
        val newServiceForRepo = ServiceItem(
            id = persistentId,
            name = name,
            url = baseUrl,
            port = port,
            iconResId = currentSelectedIconResId, 
            iconIdentifier = currentSelectedIconIdentifier
        )
        serviceRepository.addService(newServiceForRepo)

        val newConfiguredItem = ConfiguredServiceItem(
            localId = localIdCounter.incrementAndGet(),
            persistentId = persistentId,
            name = name,
            url = baseUrl,
            port = port,
            iconResId = currentSelectedIconResId,
            iconIdentifier = currentSelectedIconIdentifier
        )
        configuredServiceAdapter.addItem(newConfiguredItem)

        binding.editTextServiceName.text?.clear() // Use ViewBinding
        binding.editTextServiceUrl.text?.clear() // Use ViewBinding
        currentSelectedIconResId = android.R.drawable.sym_def_app_icon 
        currentSelectedIconIdentifier = "default_icon"
        binding.imageViewSelectedIcon.setImageResource(currentSelectedIconResId) // Use ViewBinding

        Toast.makeText(this, "$name added", Toast.LENGTH_SHORT).show()
    }
    
    private fun parseUrlAndPort(urlString: String): Pair<String, Int?> {
        return try {
            val fullUrl = if (!urlString.startsWith("http://") && !urlString.startsWith("https://")) {
                "http://$urlString" 
            } else {
                urlString
            }
            val parsed = URL(fullUrl)
            val host = parsed.host
            val port = if (parsed.port != -1) parsed.port else null 
            Pair(host, port)
        } catch (e: Exception) {
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
