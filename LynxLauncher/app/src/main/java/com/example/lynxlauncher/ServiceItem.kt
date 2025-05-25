package com.example.lynxlauncher

import androidx.annotation.DrawableRes

// Represents a service displayed on the MainActivity grid
data class ServiceItem(
    val id: String, // Unique ID, can be based on URL+port or a generated UUID
    var name: String,
    var url: String, // Base URL, e.g., "myservice.local" or "192.168.1.100"
    var port: Int?,   // Optional port number
    @DrawableRes var iconResId: Int, // Placeholder for now, will be replaced by custom icon logic
    var iconIdentifier: String? = null // For custom icon lookup later
)
