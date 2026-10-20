package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "internal_transfers",
    indices = [
        Index("sourceAccountId"),
        Index("destinationAccountId"),
        Index("transferDate")
    ]
)
data class InternalTransferEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val sourceAccountId: String,
    val destinationAccountId: String,
    val amountPaise: Long,
    val transferDate: Long,
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
