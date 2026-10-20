package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val categoryType: String = "EXPENSE", // "EXPENSE", "INCOME"
    val iconName: String? = null,
    val isSystemDefault: Boolean = false,
    val isActive: Boolean = true
)
