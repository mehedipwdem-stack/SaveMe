package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "community_alerts")
data class CommunityAlert(
    @PrimaryKey
    val id: String,
    val victimName: String,
    val phoneMasked: String,
    val locationName: String,
    val latitude: Double,
    val longitude: Double,
    val batteryLevel: Int,
    val timestamp: Long,
    val status: String = "ACTIVE_DISTRESS", // ACTIVE_DISTRESS, RESPONDING, SAFE
    val emergencyType: String = "GENERAL_DANGER",
    val distanceMeters: Int = 350,
    val responderCount: Int = 0,
    val isUserTriggered: Boolean = false,
    val customMessage: String = "I am in danger! Need immediate assistance."
)
