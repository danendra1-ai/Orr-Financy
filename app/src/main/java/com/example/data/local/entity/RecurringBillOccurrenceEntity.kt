package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "recurring_bill_occurrences",
    indices = [
        Index("recurringBillId"),
        Index("dueDate"),
        Index("status")
    ]
)
data class RecurringBillOccurrenceEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val recurringBillId: String,
    val dueDate: Long,
    val expectedAmountPaise: Long,
    val status: String = "PENDING", // "PENDING", "PAID", "OVERDUE"
    val linkedTransactionId: String? = null,
    val paidAt: Long? = null
)
