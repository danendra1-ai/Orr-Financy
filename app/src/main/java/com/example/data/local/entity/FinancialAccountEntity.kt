package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "financial_accounts")
data class FinancialAccountEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val accountType: String, // "CASH", "BANK", "OTHER"
    val openingBalancePaise: Long = 0L,
    val openingDate: Long = System.currentTimeMillis(),
    val isActive: Boolean = true
)
