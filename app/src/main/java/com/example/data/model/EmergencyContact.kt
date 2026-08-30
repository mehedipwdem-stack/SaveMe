package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "emergency_contacts")
data class EmergencyContact(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val phone: String,
    val relationship: String = "Family",
    val isPrimary: Boolean = false,
    val sendSms: Boolean = true,
    val autoCall: Boolean = false
)
