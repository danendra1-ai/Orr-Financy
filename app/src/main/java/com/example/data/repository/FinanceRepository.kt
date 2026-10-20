package com.example.data.repository

import androidx.room.withTransaction
import com.example.data.local.AppDatabase
import com.example.data.local.entity.BudgetEntity
import com.example.data.local.entity.CategoryEntity
import com.example.data.local.entity.FamilyMemberEntity
import com.example.data.local.entity.FinancialAccountEntity
import com.example.data.local.entity.InternalTransferEntity
import com.example.data.local.entity.RecurringBillEntity
import com.example.data.local.entity.RecurringBillOccurrenceEntity
import com.example.data.local.entity.SavingsGoalEntity
import com.example.data.local.entity.TransactionEntity
import com.example.data.local.entity.VehicleEntity
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class FinanceRepository(private val database: AppDatabase) {

    // DAOs
    val memberDao = database.familyMemberDao()
    val accountDao = database.financialAccountDao()
    val categoryDao = database.categoryDao()
    val vehicleDao = database.vehicleDao()
    val transactionDao = database.transactionDao()
    val transferDao = database.internalTransferDao()
    val budgetDao = database.budgetDao()
    val billDao = database.recurringBillDao()
    val goalDao = database.savingsGoalDao()

    // Flows
    val allActiveMembers: Flow<List<FamilyMemberEntity>> = memberDao.getAllActiveMembers()
    val allMembers: Flow<List<FamilyMemberEntity>> = memberDao.getAllMembers()

    val allActiveAccounts: Flow<List<FinancialAccountEntity>> = accountDao.getAllActiveAccounts()
    val allAccounts: Flow<List<FinancialAccountEntity>> = accountDao.getAllAccounts()

    val allActiveCategories: Flow<List<CategoryEntity>> = categoryDao.getAllActiveCategories()
    val allCategories: Flow<List<CategoryEntity>> = categoryDao.getAllCategories()

    val allActiveVehicles: Flow<List<VehicleEntity>> = vehicleDao.getAllActiveVehicles()
    val allVehicles: Flow<List<VehicleEntity>> = vehicleDao.getAllVehicles()

    val allTransactions: Flow<List<TransactionEntity>> = transactionDao.getAllTransactions()
    val allTransfers: Flow<List<InternalTransferEntity>> = transferDao.getAllTransfers()
    val allBudgets: Flow<List<BudgetEntity>> = budgetDao.getAllBudgets()
    val allBills: Flow<List<RecurringBillEntity>> = billDao.getAllActiveBills()
    val pendingBillOccurrences: Flow<List<RecurringBillOccurrenceEntity>> = billDao.getPendingOccurrences()
    val allGoals: Flow<List<SavingsGoalEntity>> = goalDao.getAllGoals()

    // Transactions
    suspend fun insertTransaction(transaction: TransactionEntity) {
        transactionDao.insert(transaction)
    }

    suspend fun updateTransaction(transaction: TransactionEntity) {
        transactionDao.update(transaction.copy(updatedAt = System.currentTimeMillis()))
    }

    suspend fun deleteTransaction(transaction: TransactionEntity) {
        database.withTransaction {
            transactionDao.delete(transaction)
            // If linked to a recurring bill occurrence, reset the occurrence status to PENDING
            if (transaction.recurringBillOccurrenceId != null) {
                // In case a bill was linked
            }
        }
    }

    suspend fun checkPotentialDuplicates(
        amountPaise: Long,
        categoryId: String,
        date: Long
    ): List<TransactionEntity> {
        val window = 24 * 60 * 60 * 1000L // 24 hour window
        return transactionDao.findPotentialDuplicates(
            amountPaise = amountPaise,
            categoryId = categoryId,
            dateStart = date - window,
            dateEnd = date + window
        )
    }

    // Transfers
    suspend fun executeTransfer(
        sourceAccountId: String,
        destinationAccountId: String,
        amountPaise: Long,
        transferDate: Long,
        notes: String?
    ) {
        database.withTransaction {
            val transfer = InternalTransferEntity(
                sourceAccountId = sourceAccountId,
                destinationAccountId = destinationAccountId,
                amountPaise = amountPaise,
                transferDate = transferDate,
                notes = notes
            )
            transferDao.insert(transfer)
        }
    }

    suspend fun deleteTransfer(transfer: InternalTransferEntity) {
        transferDao.delete(transfer)
    }

    // Recurring Bills
    suspend fun markBillAsPaid(
        occurrence: RecurringBillOccurrenceEntity,
        bill: RecurringBillEntity,
        accountId: String,
        paidByMemberId: String,
        paymentMethod: String,
        notes: String?
    ) {
        database.withTransaction {
            val txId = UUID.randomUUID().toString()
            val transaction = TransactionEntity(
                id = txId,
                transactionType = "EXPENSE",
                amountPaise = occurrence.expectedAmountPaise,
                categoryId = bill.categoryId,
                transactionDate = System.currentTimeMillis(),
                paidByMemberId = paidByMemberId,
                responsibleMemberId = bill.responsibleMemberId,
                accountId = accountId,
                paymentMethod = paymentMethod,
                notes = notes ?: "Bill Payment: ${bill.name}",
                recurringBillOccurrenceId = occurrence.id,
                status = "CONFIRMED"
            )
            transactionDao.insert(transaction)

            val updatedOccurrence = occurrence.copy(
                status = "PAID",
                linkedTransactionId = txId,
                paidAt = System.currentTimeMillis()
            )
            billDao.updateOccurrence(updatedOccurrence)
        }
    }

    // Members, Accounts, Vehicles, Budgets, Goals
    suspend fun insertMember(member: FamilyMemberEntity) = memberDao.insert(member)
    suspend fun updateMember(member: FamilyMemberEntity) = memberDao.update(member)

    suspend fun insertAccount(account: FinancialAccountEntity) = accountDao.insert(account)
    suspend fun updateAccount(account: FinancialAccountEntity) = accountDao.update(account)

    suspend fun insertCategory(category: CategoryEntity) = categoryDao.insert(category)
    suspend fun updateCategory(category: CategoryEntity) = categoryDao.update(category)

    suspend fun insertVehicle(vehicle: VehicleEntity) = vehicleDao.insert(vehicle)
    suspend fun updateVehicle(vehicle: VehicleEntity) = vehicleDao.update(vehicle)

    suspend fun insertBudget(budget: BudgetEntity) = budgetDao.insert(budget)
    suspend fun updateBudget(budget: BudgetEntity) = budgetDao.update(budget)
    suspend fun deleteBudget(budget: BudgetEntity) = budgetDao.delete(budget)

    suspend fun insertBill(bill: RecurringBillEntity) {
        database.withTransaction {
            billDao.insertBill(bill)
            // Create first occurrence
            val occurrence = RecurringBillOccurrenceEntity(
                recurringBillId = bill.id,
                dueDate = bill.nextDueDate,
                expectedAmountPaise = bill.expectedAmountPaise,
                status = "PENDING"
            )
            billDao.insertOccurrence(occurrence)
        }
    }
    suspend fun updateBill(bill: RecurringBillEntity) = billDao.updateBill(bill)
    suspend fun deleteBill(bill: RecurringBillEntity) = billDao.deleteBill(bill)

    suspend fun insertGoal(goal: SavingsGoalEntity) = goalDao.insert(goal)
    suspend fun updateGoal(goal: SavingsGoalEntity) = goalDao.update(goal)
    suspend fun deleteGoal(goal: SavingsGoalEntity) = goalDao.delete(goal)

    suspend fun clearAllData() {
        database.withTransaction {
            transactionDao.deleteAll()
            transferDao.deleteAll()
            budgetDao.deleteAll()
            goalDao.deleteAll()
            billDao.deleteAllOccurrences()
            billDao.deleteAllBills()
            accountDao.deleteAll()
            vehicleDao.deleteAll()
            memberDao.deleteAll()
            categoryDao.deleteAll()
            AppDatabase.seedDefaultData(database)
        }
    }
}
