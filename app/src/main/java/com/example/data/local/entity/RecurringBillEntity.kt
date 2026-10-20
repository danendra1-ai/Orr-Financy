package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "recurring_bills")
data class RecurringBillEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String, // e.g. "Mobile Recharge", "Cooking Gas", "School Fees"
    val expectedAmountPaise: Long,
    val frequency: String = "MONTHLY", // "MONTHLY", "QUARTERLY", "YEARLY"
    val nextDueDate: Long,
    val categoryId: String,
    val responsibleMemberId: String? = null,
    val isActive: Boolean = true
)
