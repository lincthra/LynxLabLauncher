package com.example.lynxlauncher

import androidx.annotation.DrawableRes

// Represents a service item during configuration in ConfigActivity
data class ConfiguredServiceItem(
    val localId: Long, // Unique ID for local list management in ConfigActivity
    var persistentId: String, // ID that will be stored (e.g., UUID)
    var name: String,
    var url: String, // Base URL
    var port: Int?,
    @DrawableRes var iconResId: Int, // Placeholder for icon preview in ConfigActivity
    var iconIdentifier: String? = null // For custom icon lookup later
)
