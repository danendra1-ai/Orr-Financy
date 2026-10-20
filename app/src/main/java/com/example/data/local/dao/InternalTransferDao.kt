package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.InternalTransferEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface InternalTransferDao {
    @Query("SELECT * FROM internal_transfers ORDER BY transferDate DESC, createdAt DESC")
    fun getAllTransfers(): Flow<List<InternalTransferEntity>>

    @Query("SELECT * FROM internal_transfers WHERE transferDate >= :startTime AND transferDate <= :endTime ORDER BY transferDate DESC")
    fun getTransfersBetween(startTime: Long, endTime: Long): Flow<List<InternalTransferEntity>>

    @Query("SELECT * FROM internal_transfers WHERE sourceAccountId = :accountId OR destinationAccountId = :accountId ORDER BY transferDate DESC")
    fun getTransfersForAccount(accountId: String): Flow<List<InternalTransferEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transfer: InternalTransferEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(transfers: List<InternalTransferEntity>)

    @Update
    suspend fun update(transfer: InternalTransferEntity)

    @Delete
    suspend fun delete(transfer: InternalTransferEntity)

    @Query("DELETE FROM internal_transfers WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM internal_transfers")
    suspend fun deleteAll()
}
