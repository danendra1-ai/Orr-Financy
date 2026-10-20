package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.FinancialAccountEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FinancialAccountDao {
    @Query("SELECT * FROM financial_accounts WHERE isActive = 1 ORDER BY name ASC")
    fun getAllActiveAccounts(): Flow<List<FinancialAccountEntity>>

    @Query("SELECT * FROM financial_accounts ORDER BY name ASC")
    fun getAllAccounts(): Flow<List<FinancialAccountEntity>>

    @Query("SELECT * FROM financial_accounts WHERE id = :id LIMIT 1")
    suspend fun getAccountById(id: String): FinancialAccountEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(account: FinancialAccountEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(accounts: List<FinancialAccountEntity>)

    @Update
    suspend fun update(account: FinancialAccountEntity)

    @Query("UPDATE financial_accounts SET isActive = 0 WHERE id = :id")
    suspend fun deactivate(id: String)

    @Query("DELETE FROM financial_accounts")
    suspend fun deleteAll()
}
