package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY transactionDate DESC, createdAt DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE transactionDate >= :startTime AND transactionDate <= :endTime ORDER BY transactionDate DESC, createdAt DESC")
    fun getTransactionsBetween(startTime: Long, endTime: Long): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE transactionDate >= :startTime AND transactionDate <= :endTime ORDER BY transactionDate DESC, createdAt DESC")
    suspend fun getTransactionsBetweenSync(startTime: Long, endTime: Long): List<TransactionEntity>

    @Query("SELECT * FROM transactions WHERE vehicleId = :vehicleId ORDER BY transactionDate DESC")
    fun getTransactionsForVehicle(vehicleId: String): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE paidByMemberId = :memberId OR responsibleMemberId = :memberId ORDER BY transactionDate DESC")
    fun getTransactionsForMember(memberId: String): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE accountId = :accountId ORDER BY transactionDate DESC")
    fun getTransactionsForAccount(accountId: String): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE id = :id LIMIT 1")
    suspend fun getTransactionById(id: String): TransactionEntity?

    @Query("""
        SELECT * FROM transactions 
        WHERE amountPaise = :amountPaise 
          AND categoryId = :categoryId 
          AND transactionDate BETWEEN :dateStart AND :dateEnd
        LIMIT 5
    """)
    suspend fun findPotentialDuplicates(
        amountPaise: Long,
        categoryId: String,
        dateStart: Long,
        dateEnd: Long
    ): List<TransactionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaction: TransactionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(transactions: List<TransactionEntity>)

    @Update
    suspend fun update(transaction: TransactionEntity)

    @Delete
    suspend fun delete(transaction: TransactionEntity)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM transactions")
    suspend fun deleteAll()
}
