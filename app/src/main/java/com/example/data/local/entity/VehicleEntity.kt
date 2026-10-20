package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "vehicles")
data class VehicleEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String, // e.g. "Papa Bike", "Family Bike"
    val vehicleType: String = "BIKE", // "BIKE", "SCOOTER", "OTHER"
    val registrationLabel: String? = null,
    val isActive: Boolean = true,
    val notes: String? = null
)
