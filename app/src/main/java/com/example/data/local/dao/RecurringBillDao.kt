package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.RecurringBillEntity
import com.example.data.local.entity.RecurringBillOccurrenceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RecurringBillDao {
    @Query("SELECT * FROM recurring_bills WHERE isActive = 1 ORDER BY nextDueDate ASC")
    fun getAllActiveBills(): Flow<List<RecurringBillEntity>>

    @Query("SELECT * FROM recurring_bills ORDER BY nextDueDate ASC")
    fun getAllBills(): Flow<List<RecurringBillEntity>>

    @Query("SELECT * FROM recurring_bills WHERE id = :id LIMIT 1")
    suspend fun getBillById(id: String): RecurringBillEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBill(bill: RecurringBillEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllBills(bills: List<RecurringBillEntity>)

    @Update
    suspend fun updateBill(bill: RecurringBillEntity)

    @Delete
    suspend fun deleteBill(bill: RecurringBillEntity)

    // Occurrences
    @Query("SELECT * FROM recurring_bill_occurrences WHERE recurringBillId = :billId ORDER BY dueDate DESC")
    fun getOccurrencesForBill(billId: String): Flow<List<RecurringBillOccurrenceEntity>>

    @Query("SELECT * FROM recurring_bill_occurrences WHERE status = 'PENDING' OR status = 'OVERDUE' ORDER BY dueDate ASC")
    fun getPendingOccurrences(): Flow<List<RecurringBillOccurrenceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOccurrence(occurrence: RecurringBillOccurrenceEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllOccurrences(occurrences: List<RecurringBillOccurrenceEntity>)

    @Update
    suspend fun updateOccurrence(occurrence: RecurringBillOccurrenceEntity)

    @Query("DELETE FROM recurring_bills")
    suspend fun deleteAllBills()

    @Query("DELETE FROM recurring_bill_occurrences")
    suspend fun deleteAllOccurrences()
}
