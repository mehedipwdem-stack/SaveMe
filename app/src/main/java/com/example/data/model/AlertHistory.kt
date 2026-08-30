package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alert_history")
data class AlertHistory(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val triggerSource: String, // "POWER_BUTTON_3X", "SOS_BUTTON", "VOICE_TRIGGER", "SAFETY_CHECK_EXPIRED"
    val latitude: Double,
    val longitude: Double,
    val locationAddress: String,
    val batteryPercentage: Int,
    val contactsNotified: Int,
    val smsStatus: String, // "DELIVERED", "SENT", "FAILED"
    val communityBroadcasted: Boolean,
    val isResolved: Boolean = true
)
