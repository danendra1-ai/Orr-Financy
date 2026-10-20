package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "family_members")
data class FamilyMemberEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val notes: String? = null
)
