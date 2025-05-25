package com.example.lynxlauncher

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.UUID

class ServiceRepository(context: Context) {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("LynxLauncherPrefs", Context.MODE_PRIVATE)
    private val gson = Gson()
    private val servicesKey = "services"

    fun getAllServices(): MutableList<ServiceItem> {
        val json = sharedPreferences.getString(servicesKey, null)
        return if (json != null) {
            val type = object : TypeToken<MutableList<ServiceItem>>() {}.type
            try {
                gson.fromJson(json, type) ?: mutableListOf() // Return empty list if fromJson returns null
            } catch (e: com.google.gson.JsonSyntaxException) {
                android.util.Log.e("ServiceRepository", "Error parsing services JSON", e)
                mutableListOf() // Return empty list on parsing error
            }
        } else {
            mutableListOf()
        }
    }

    fun addService(serviceItem: ServiceItem) {
        val services = getAllServices()
        // Ensure unique ID if not already set (though ConfigActivity should handle this)
        val itemToAdd = if (services.any { it.id == serviceItem.id }) {
            serviceItem.copy(id = UUID.randomUUID().toString()) // Re-generate if collision, though unlikely with UUIDs
        } else {
            serviceItem
        }
        services.add(itemToAdd)
        saveServices(services)
    }

    fun removeService(serviceId: String) {
        val services = getAllServices()
        services.removeAll { it.id == serviceId }
        saveServices(services)
    }
    
    fun updateService(updatedServiceItem: ServiceItem) {
        val services = getAllServices()
        val index = services.indexOfFirst { it.id == updatedServiceItem.id }
        if (index != -1) {
            services[index] = updatedServiceItem
            saveServices(services)
        }
    }

    fun updateAllServices(services: List<ServiceItem>) {
        // Directly saves the provided list, overwriting the previous one.
        // Useful for operations like reordering.
        saveServices(services)
    }

    private fun saveServices(services: List<ServiceItem>) {
        val json = gson.toJson(services)
        sharedPreferences.edit().putString(servicesKey, json).apply()
    }

    // Generates a new unique ID for a service
    fun generateNewId(): String {
        return UUID.randomUUID().toString()
    }
}
