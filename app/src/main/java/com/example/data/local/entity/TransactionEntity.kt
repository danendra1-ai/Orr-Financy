package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "transactions",
    indices = [
        Index("transactionDate"),
        Index("categoryId"),
        Index("paidByMemberId"),
        Index("responsibleMemberId"),
        Index("accountId"),
        Index("vehicleId")
    ]
)
data class TransactionEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val transactionType: String, // "INCOME" or "EXPENSE"
    val amountPaise: Long,
    val categoryId: String,
    val transactionDate: Long,
    val paidByMemberId: String,
    val responsibleMemberId: String? = null, // null means Family-wide attribution
    val accountId: String,
    val paymentMethod: String = "CASH", // "CASH", "UPI", "BANK_TRANSFER", "CARD"
    val notes: String? = null,
    val vehicleId: String? = null,
    val vehicleExpenseType: String? = null, // "PETROL", "SERVICE_REPAIRS", "INSURANCE", "PUC", "OTHER"
    val recurringBillOccurrenceId: String? = null,
    val status: String = "CONFIRMED", // "CONFIRMED", "EXPECTED"
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
