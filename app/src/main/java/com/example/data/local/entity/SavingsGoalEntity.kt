package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "savings_goals")
data class SavingsGoalEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val targetAmountPaise: Long,
    val allocatedAmountPaise: Long = 0L,
    val targetDate: Long? = null,
    val notes: String? = null,
    val status: String = "IN_PROGRESS" // "IN_PROGRESS", "COMPLETED", "ON_HOLD"
)
