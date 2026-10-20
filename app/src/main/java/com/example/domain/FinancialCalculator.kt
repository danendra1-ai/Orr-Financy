package com.example.domain

import com.example.data.local.entity.BudgetEntity
import com.example.data.local.entity.FinancialAccountEntity
import com.example.data.local.entity.InternalTransferEntity
import com.example.data.local.entity.TransactionEntity
import com.example.data.local.entity.VehicleEntity
import java.util.Calendar

data class AccountBalance(
    val account: FinancialAccountEntity,
    val currentBalancePaise: Long
)

data class VehicleExpenseSummary(
    val vehicle: VehicleEntity,
    val petrolPaise: Long,
    val serviceRepairsPaise: Long,
    val insurancePaise: Long,
    val pucPaise: Long,
    val otherMaintenancePaise: Long,
    val totalMonthPaise: Long
)

data class MemberFinancialSummary(
    val memberId: String,
    val memberName: String,
    val paidByPaise: Long,
    val responsibleForPaise: Long,
    val earnedIncomePaise: Long,
    val budgetPaise: Long? = null,
    val budgetUtilizationPercentage: Double? = null
)

data class CategoryExpenseSummary(
    val categoryId: String,
    val categoryName: String,
    val totalExpensePaise: Long,
    val budgetLimitPaise: Long? = null,
    val utilizationPercentage: Double? = null
)

data class MonthlyFinancialSummary(
    val totalIncomePaise: Long,
    val totalExpensePaise: Long,
    val netCashFlowPaise: Long,
    val savingsRatePercentage: Double?,
    val availableBalancePaise: Long,
    val rationExpensePaise: Long,
    val memberSummaries: List<MemberFinancialSummary>,
    val vehicleSummaries: List<VehicleExpenseSummary>,
    val categorySummaries: List<CategoryExpenseSummary>,
    val familyBudgetLimitPaise: Long?,
    val familyBudgetUtilizationPercentage: Double?
)

object FinancialCalculator {

    /**
     * Calculates the exact current balance for each account:
     * Current balance = openingBalancePaise + confirmed credits - confirmed debits
     * Credits = confirmed income to account + transfers into account
     * Debits = confirmed expenses from account + transfers out of account
     */
    fun calculateAccountBalances(
        accounts: List<FinancialAccountEntity>,
        transactions: List<TransactionEntity>,
        transfers: List<InternalTransferEntity>
    ): List<AccountBalance> {
        return accounts.map { account ->
            val confirmedCredits = transactions
                .filter { it.accountId == account.id && it.transactionType == "INCOME" && it.status == "CONFIRMED" }
                .sumOf { it.amountPaise }

            val confirmedDebits = transactions
                .filter { it.accountId == account.id && it.transactionType == "EXPENSE" && it.status == "CONFIRMED" }
                .sumOf { it.amountPaise }

            val transferCredits = transfers
                .filter { it.destinationAccountId == account.id }
                .sumOf { it.amountPaise }

            val transferDebits = transfers
                .filter { it.sourceAccountId == account.id }
                .sumOf { it.amountPaise }

            val currentBalance = account.openingBalancePaise + (confirmedCredits + transferCredits) - (confirmedDebits + transferDebits)
            AccountBalance(account = account, currentBalancePaise = currentBalance)
        }
    }

    /**
     * Total available recorded balance across all tracked accounts.
     */
    fun calculateTotalAvailableBalance(accountBalances: List<AccountBalance>): Long {
        return accountBalances.filter { it.account.isActive }.sumOf { it.currentBalancePaise }
    }

    /**
     * Calculates monthly financial metrics for a specific month/year.
     */
    fun calculateMonthlySummary(
        month: Int, // 0-based, Calendar.JANUARY = 0
        year: Int,
        allTransactions: List<TransactionEntity>,
        allTransfers: List<InternalTransferEntity>,
        allAccounts: List<FinancialAccountEntity>,
        allMembers: List<com.example.data.local.entity.FamilyMemberEntity>,
        allCategories: List<com.example.data.local.entity.CategoryEntity>,
        allVehicles: List<VehicleEntity>,
        allBudgets: List<BudgetEntity>
    ): MonthlyFinancialSummary {
        val (startTime, endTime) = getMonthTimeRange(month, year)

        // Filter transactions strictly for this month
        val monthTransactions = allTransactions.filter {
            it.transactionDate in startTime..endTime && it.status == "CONFIRMED"
        }

        // Actual Income = Sum of confirmed external income
        val totalIncome = monthTransactions
            .filter { it.transactionType == "INCOME" }
            .sumOf { it.amountPaise }

        // Actual Expenses = Sum of confirmed external expenses (each counted exactly once)
        val totalExpense = monthTransactions
            .filter { it.transactionType == "EXPENSE" }
            .sumOf { it.amountPaise }

        // Monthly Net Cash Flow = Actual Income - Actual Expenses
        val netCashFlow = totalIncome - totalExpense

        // Savings Rate = (Income - Expense) / Income * 100
        val savingsRate = if (totalIncome > 0) {
            (netCashFlow.toDouble() / totalIncome.toDouble()) * 100.0
        } else {
            null
        }

        // Available balance as of now (overall account balances)
        val accountBalances = calculateAccountBalances(allAccounts, allTransactions, allTransfers)
        val availableBalance = calculateTotalAvailableBalance(accountBalances)

        // Ration expenses (Ration & Groceries category)
        val rationCatIds = allCategories
            .filter { it.name.contains("Ration", ignoreCase = true) || it.name.contains("Groceries", ignoreCase = true) }
            .map { it.id }
            .toSet()

        val rationExpense = monthTransactions
            .filter { it.transactionType == "EXPENSE" && it.categoryId in rationCatIds }
            .sumOf { it.amountPaise }

        // Member Summaries:
        // Expenses Paid By: who paid the money
        // Expenses Responsible For: who it was for (or family-wide if null)
        val memberSummaries = allMembers.map { member ->
            val paidBy = monthTransactions
                .filter { it.transactionType == "EXPENSE" && it.paidByMemberId == member.id }
                .sumOf { it.amountPaise }

            val responsibleFor = monthTransactions
                .filter { it.transactionType == "EXPENSE" && it.responsibleMemberId == member.id }
                .sumOf { it.amountPaise }

            val incomeEarned = monthTransactions
                .filter { it.transactionType == "INCOME" && it.paidByMemberId == member.id }
                .sumOf { it.amountPaise }

            val memberBudget = allBudgets.firstOrNull { it.budgetType == "MEMBER" && it.memberId == member.id && it.isActive }
            val memberBudgetPaise = memberBudget?.amountPaise
            val memberUtilization = if (memberBudgetPaise != null && memberBudgetPaise > 0) {
                (responsibleFor.toDouble() / memberBudgetPaise.toDouble()) * 100.0
            } else null

            MemberFinancialSummary(
                memberId = member.id,
                memberName = member.name,
                paidByPaise = paidBy,
                responsibleForPaise = responsibleFor,
                earnedIncomePaise = incomeEarned,
                budgetPaise = memberBudgetPaise,
                budgetUtilizationPercentage = memberUtilization
            )
        }

        // Vehicle Summaries
        val vehicleSummaries = allVehicles.map { vehicle ->
            val vehicleTx = monthTransactions.filter {
                it.transactionType == "EXPENSE" && it.vehicleId == vehicle.id
            }

            val petrol = vehicleTx.filter { it.vehicleExpenseType == "PETROL" || (it.notes?.contains("petrol", ignoreCase = true) == true) }.sumOf { it.amountPaise }
            val repairs = vehicleTx.filter { it.vehicleExpenseType == "SERVICE_REPAIRS" }.sumOf { it.amountPaise }
            val insurance = vehicleTx.filter { it.vehicleExpenseType == "INSURANCE" }.sumOf { it.amountPaise }
            val puc = vehicleTx.filter { it.vehicleExpenseType == "PUC" }.sumOf { it.amountPaise }
            val other = vehicleTx.filter { it.vehicleExpenseType == "OTHER" || (it.vehicleExpenseType == null && petrol == 0L) }.sumOf { it.amountPaise }
            val vehicleTotal = vehicleTx.sumOf { it.amountPaise }

            VehicleExpenseSummary(
                vehicle = vehicle,
                petrolPaise = petrol,
                serviceRepairsPaise = repairs,
                insurancePaise = insurance,
                pucPaise = puc,
                otherMaintenancePaise = other,
                totalMonthPaise = vehicleTotal
            )
        }

        // Category Summaries
        val categorySummaries = allCategories
            .filter { it.categoryType == "EXPENSE" }
            .map { category ->
                val catExpenses = monthTransactions
                    .filter { it.transactionType == "EXPENSE" && it.categoryId == category.id }
                    .sumOf { it.amountPaise }

                val catBudget = allBudgets.firstOrNull { it.budgetType == "CATEGORY" && it.categoryId == category.id && it.isActive }
                val limit = catBudget?.amountPaise
                val util = if (limit != null && limit > 0) {
                    (catExpenses.toDouble() / limit.toDouble()) * 100.0
                } else null

                CategoryExpenseSummary(
                    categoryId = category.id,
                    categoryName = category.name,
                    totalExpensePaise = catExpenses,
                    budgetLimitPaise = limit,
                    utilizationPercentage = util
                )
            }.sortedByDescending { it.totalExpensePaise }

        // Family Budget
        val familyBudget = allBudgets.firstOrNull { it.budgetType == "FAMILY" && it.isActive }
        val familyBudgetLimit = familyBudget?.amountPaise
        val familyBudgetUtil = if (familyBudgetLimit != null && familyBudgetLimit > 0) {
            (totalExpense.toDouble() / familyBudgetLimit.toDouble()) * 100.0
        } else null

        return MonthlyFinancialSummary(
            totalIncomePaise = totalIncome,
            totalExpensePaise = totalExpense,
            netCashFlowPaise = netCashFlow,
            savingsRatePercentage = savingsRate,
            availableBalancePaise = availableBalance,
            rationExpensePaise = rationExpense,
            memberSummaries = memberSummaries,
            vehicleSummaries = vehicleSummaries,
            categorySummaries = categorySummaries,
            familyBudgetLimitPaise = familyBudgetLimit,
            familyBudgetUtilizationPercentage = familyBudgetUtil
        )
    }

    /**
     * Returns UTC/local timestamp range [startMillis, endMillis] for given month & year.
     */
    fun getMonthTimeRange(month: Int, year: Int): Pair<Long, Long> {
        val cal = Calendar.getInstance()
        cal.set(Calendar.YEAR, year)
        cal.set(Calendar.MONTH, month)
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val startTime = cal.timeInMillis

        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        cal.set(Calendar.MILLISECOND, 999)
        val endTime = cal.timeInMillis

        return Pair(startTime, endTime)
    }
}
