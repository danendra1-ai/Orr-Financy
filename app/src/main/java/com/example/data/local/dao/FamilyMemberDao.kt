package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.FamilyMemberEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FamilyMemberDao {
    @Query("SELECT * FROM family_members WHERE isActive = 1 ORDER BY createdAt ASC")
    fun getAllActiveMembers(): Flow<List<FamilyMemberEntity>>

    @Query("SELECT * FROM family_members ORDER BY createdAt ASC")
    fun getAllMembers(): Flow<List<FamilyMemberEntity>>

    @Query("SELECT * FROM family_members WHERE id = :id LIMIT 1")
    suspend fun getMemberById(id: String): FamilyMemberEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(member: FamilyMemberEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(members: List<FamilyMemberEntity>)

    @Update
    suspend fun update(member: FamilyMemberEntity)

    @Query("UPDATE family_members SET isActive = 0 WHERE id = :id")
    suspend fun deactivate(id: String)

    @Query("DELETE FROM family_members")
    suspend fun deleteAll()
}
