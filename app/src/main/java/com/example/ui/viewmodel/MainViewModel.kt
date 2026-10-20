package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.backup.BackupManager
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
import com.example.data.repository.FinanceRepository
import com.example.domain.CurrencyFormatter
import com.example.domain.FinancialCalculator
import com.example.domain.FinancialHealthAnalyzer
import com.example.domain.FinancialHealthReport
import com.example.domain.MonthlyFinancialSummary
import com.example.reports.PdfReportGenerator
import com.example.security.SecurityManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getInstance(application)
    val repository = FinanceRepository(database)
    val securityManager = SecurityManager(application)
    val backupManager = BackupManager(database)
    val pdfGenerator = PdfReportGenerator(application)

    private val prefs = application.getSharedPreferences("orr_expency_app_prefs", Application.MODE_PRIVATE)

    // Month & Year selector state
    private val _selectedMonth = MutableStateFlow(Calendar.getInstance().get(Calendar.MONTH))
    val selectedMonth: StateFlow<Int> = _selectedMonth.asStateFlow()

    private val _selectedYear = MutableStateFlow(Calendar.getInstance().get(Calendar.YEAR))
    val selectedYear: StateFlow<Int> = _selectedYear.asStateFlow()

    // App Lock State
    private val _isAppLocked = MutableStateFlow(securityManager.isPinEnabled)
    val isAppLocked: StateFlow<Boolean> = _isAppLocked.asStateFlow()

    // First Launch Onboarding state
    private val _isOnboardingCompleted = MutableStateFlow(prefs.getBoolean("onboarding_completed", false))
    val isOnboardingCompleted: StateFlow<Boolean> = _isOnboardingCompleted.asStateFlow()

    // Theme Mode ("SYSTEM", "LIGHT", "DARK")
    private val _themeMode = MutableStateFlow(prefs.getString("theme_mode", "SYSTEM") ?: "SYSTEM")
    val themeMode: StateFlow<String> = _themeMode.asStateFlow()

    // Core Data Flows
    val allMembers = repository.allActiveMembers.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allAccounts = repository.allActiveAccounts.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allCategories = repository.allActiveCategories.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allVehicles = repository.allActiveVehicles.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allTransactions = repository.allTransactions.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allTransfers = repository.allTransfers.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allBudgets = repository.allBudgets.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allBills = repository.allBills.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val pendingBills = repository.pendingBillOccurrences.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allGoals = repository.allGoals.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Reactive Monthly Summary
    private val timeAndFinancialFlow = combine(
        selectedMonth,
        selectedYear,
        allTransactions,
        allTransfers,
        allAccounts
    ) { m, y, tx, tr, acc ->
        Triple(Pair(m, y), Pair(tx, tr), acc)
    }

    private val masterEntitiesFlow = combine(
        allMembers,
        allCategories,
        allVehicles,
        allBudgets
    ) { mem, cat, veh, bud ->
        Pair(Pair(mem, cat), Pair(veh, bud))
    }

    // Reactive Monthly Summary
    val monthlySummary: StateFlow<MonthlyFinancialSummary?> = combine(
        timeAndFinancialFlow,
        masterEntitiesFlow
    ) { td, md ->
        val month = td.first.first
        val year = td.first.second
        val transactions = td.second.first
        val transfers = td.second.second
        val accounts = td.third
        val members = md.first.first
        val categories = md.first.second
        val vehicles = md.second.first
        val budgets = md.second.second

        if (members.isEmpty() && categories.isEmpty()) {
            null
        } else {
            FinancialCalculator.calculateMonthlySummary(
                month = month,
                year = year,
                allTransactions = transactions,
                allTransfers = transfers,
                allAccounts = accounts,
                allMembers = members,
                allCategories = categories,
                allVehicles = vehicles,
                allBudgets = budgets
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Reactive Health Report
    val healthReport: StateFlow<FinancialHealthReport?> = combine(
        monthlySummary,
        pendingBills
    ) { summary, bills ->
        if (summary == null) null
        else FinancialHealthAnalyzer.analyze(summary, bills)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Account Balances
    val accountBalances = combine(
        allAccounts,
        allTransactions,
        allTransfers
    ) { accounts, txs, transfers ->
        FinancialCalculator.calculateAccountBalances(accounts, txs, transfers)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Month Navigation
    fun changeMonth(delta: Int) {
        val cal = Calendar.getInstance()
        cal.set(Calendar.YEAR, _selectedYear.value)
        cal.set(Calendar.MONTH, _selectedMonth.value)
        cal.add(Calendar.MONTH, delta)
        _selectedMonth.value = cal.get(Calendar.MONTH)
        _selectedYear.value = cal.get(Calendar.YEAR)
    }

    fun setMonthAndYear(month: Int, year: Int) {
        _selectedMonth.value = month
        _selectedYear.value = year
    }

    fun getMonthDisplayName(): String {
        val cal = Calendar.getInstance()
        cal.set(Calendar.MONTH, _selectedMonth.value)
        cal.set(Calendar.YEAR, _selectedYear.value)
        return SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(cal.time)
    }

    // Security & App Lock
    fun unlockApp(pin: String): Boolean {
        val ok = securityManager.verifyPin(pin)
        if (ok) {
            _isAppLocked.value = false
            securityManager.recordUserActivity()
        }
        return ok
    }

    fun unlockWithBiometric() {
        _isAppLocked.value = false
        securityManager.recordUserActivity()
    }

    fun lockApp() {
        if (securityManager.isPinEnabled) {
            _isAppLocked.value = true
        }
    }

    fun checkAutoLock() {
        if (securityManager.shouldLockOnResume()) {
            _isAppLocked.value = true
        }
    }

    fun setThemeMode(mode: String) {
        _themeMode.value = mode
        prefs.edit().putString("theme_mode", mode).apply()
    }

    // Transaction Management
    fun addTransaction(
        transaction: TransactionEntity,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (transaction.amountPaise <= 0) {
            onError("Amount must be greater than zero.")
            return
        }
        viewModelScope.launch {
            try {
                repository.insertTransaction(transaction)
                onSuccess()
            } catch (e: Exception) {
                onError(e.localizedMessage ?: "Failed to save transaction.")
            }
        }
    }

    fun updateTransaction(transaction: TransactionEntity, onComplete: () -> Unit) {
        viewModelScope.launch {
            repository.updateTransaction(transaction)
            onComplete()
        }
    }

    fun deleteTransaction(transaction: TransactionEntity, onComplete: () -> Unit) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction)
            onComplete()
        }
    }

    // Transfers
    fun executeTransfer(
        sourceAccountId: String,
        destinationAccountId: String,
        amountPaise: Long,
        transferDate: Long,
        notes: String?,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (sourceAccountId == destinationAccountId) {
            onError("Source and destination accounts must be different.")
            return
        }
        if (amountPaise <= 0) {
            onError("Transfer amount must be greater than zero.")
            return
        }
        viewModelScope.launch {
            try {
                repository.executeTransfer(
                    sourceAccountId, destinationAccountId, amountPaise, transferDate, notes
                )
                onSuccess()
            } catch (e: Exception) {
                onError(e.localizedMessage ?: "Failed to execute transfer.")
            }
        }
    }

    fun deleteTransfer(transfer: InternalTransferEntity, onComplete: () -> Unit) {
        viewModelScope.launch {
            repository.deleteTransfer(transfer)
            onComplete()
        }
    }

    // Recurring Bills
    fun payRecurringBill(
        occurrence: RecurringBillOccurrenceEntity,
        bill: RecurringBillEntity,
        accountId: String,
        paidByMemberId: String,
        paymentMethod: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            repository.markBillAsPaid(
                occurrence = occurrence,
                bill = bill,
                accountId = accountId,
                paidByMemberId = paidByMemberId,
                paymentMethod = paymentMethod,
                notes = "Scheduled bill payment for ${bill.name}"
            )
            onSuccess()
        }
    }

    // Budgets & Goals
    fun saveBudget(budget: BudgetEntity, onComplete: () -> Unit) {
        viewModelScope.launch {
            repository.insertBudget(budget)
            onComplete()
        }
    }

    fun deleteBudget(budget: BudgetEntity, onComplete: () -> Unit) {
        viewModelScope.launch {
            repository.deleteBudget(budget)
            onComplete()
        }
    }

    fun saveGoal(goal: SavingsGoalEntity, onComplete: () -> Unit) {
        viewModelScope.launch {
            repository.insertGoal(goal)
            onComplete()
        }
    }

    fun deleteGoal(goal: SavingsGoalEntity, onComplete: () -> Unit) {
        viewModelScope.launch {
            repository.deleteGoal(goal)
            onComplete()
        }
    }

    fun updateGoalAllocation(goal: SavingsGoalEntity, newAllocatedPaise: Long) {
        viewModelScope.launch {
            val status = if (newAllocatedPaise >= goal.targetAmountPaise) "COMPLETED" else "IN_PROGRESS"
            repository.updateGoal(goal.copy(allocatedAmountPaise = newAllocatedPaise, status = status))
        }
    }

    // Member & Vehicle Management
    fun saveMember(member: FamilyMemberEntity, onComplete: () -> Unit) {
        viewModelScope.launch {
            repository.insertMember(member)
            onComplete()
        }
    }

    fun saveVehicle(vehicle: VehicleEntity, onComplete: () -> Unit) {
        viewModelScope.launch {
            repository.insertVehicle(vehicle)
            onComplete()
        }
    }

    fun saveAccount(account: FinancialAccountEntity, onComplete: () -> Unit) {
        viewModelScope.launch {
            repository.insertAccount(account)
            onComplete()
        }
    }

    fun saveRecurringBill(bill: RecurringBillEntity, onComplete: () -> Unit) {
        viewModelScope.launch {
            repository.insertBill(bill)
            onComplete()
        }
    }

    fun deleteRecurringBill(bill: RecurringBillEntity, onComplete: () -> Unit) {
        viewModelScope.launch {
            repository.deleteBill(bill)
            onComplete()
        }
    }

    // Onboarding Completion
    fun completeOnboarding(
        cashBalancePaise: Long,
        bankBalancePaise: Long,
        papaIncomePaise: Long?,
        familyBudgetPaise: Long?,
        papaBikeName: String,
        familyBikeName: String,
        pin: String?
    ) {
        viewModelScope.launch {
            // Update Accounts
            val cashAcc = FinancialAccountEntity(id = "acc_cash", name = "Cash Wallet", accountType = "CASH", openingBalancePaise = cashBalancePaise)
            val bankAcc = FinancialAccountEntity(id = "acc_bank", name = "Primary Bank Account", accountType = "BANK", openingBalancePaise = bankBalancePaise)
            repository.insertAccount(cashAcc)
            repository.insertAccount(bankAcc)

            // Update Vehicles
            repository.insertVehicle(VehicleEntity(id = "vehicle_papa_bike", name = papaBikeName.ifEmpty { "Papa Bike" }, vehicleType = "BIKE"))
            repository.insertVehicle(VehicleEntity(id = "vehicle_family_bike", name = familyBikeName.ifEmpty { "Family Bike" }, vehicleType = "BIKE"))

            // Optional Papa regular income entry
            if (papaIncomePaise != null && papaIncomePaise > 0) {
                val incomeTx = TransactionEntity(
                    transactionType = "INCOME",
                    amountPaise = papaIncomePaise,
                    categoryId = "cat_inc_salary",
                    transactionDate = System.currentTimeMillis(),
                    paidByMemberId = "member_papa",
                    responsibleMemberId = null,
                    accountId = "acc_bank",
                    paymentMethod = "BANK_TRANSFER",
                    notes = "Monthly Salary / Business Income",
                    status = "CONFIRMED"
                )
                repository.insertTransaction(incomeTx)
            }

            // Optional Family Budget
            if (familyBudgetPaise != null && familyBudgetPaise > 0) {
                repository.insertBudget(
                    BudgetEntity(
                        budgetName = "Monthly Family Budget",
                        budgetType = "FAMILY",
                        amountPaise = familyBudgetPaise
                    )
                )
            }

            // Setup PIN if provided
            if (!pin.isNullOrEmpty()) {
                securityManager.setPin(pin)
            }

            prefs.edit().putBoolean("onboarding_completed", true).apply()
            _isOnboardingCompleted.value = true
        }
    }

    // PDF Report Export
    fun generatePdfReport(): File? {
        val summary = monthlySummary.value ?: return null
        val health = healthReport.value ?: return null
        return try {
            pdfGenerator.generateMonthlyReport(
                monthName = SimpleDateFormat("MMMM", Locale.getDefault()).format(
                    Calendar.getInstance().apply { set(Calendar.MONTH, _selectedMonth.value) }.time
                ),
                year = _selectedYear.value,
                summary = summary,
                healthReport = health,
                pendingBills = pendingBills.value,
                goals = allGoals.value
            )
        } catch (e: Exception) {
            null
        }
    }

    // Reset Data
    fun resetData(onComplete: () -> Unit) {
        viewModelScope.launch {
            repository.clearAllData()
            onComplete()
        }
    }
}
