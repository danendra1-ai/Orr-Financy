package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "budgets")
data class BudgetEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val budgetName: String,
    val budgetType: String, // "FAMILY", "MEMBER", "CATEGORY"
    val memberId: String? = null,
    val categoryId: String? = null,
    val amountPaise: Long,
    val period: String = "MONTHLY",
    val isActive: Boolean = true
)
