package com.example.domain

import com.example.data.local.entity.RecurringBillOccurrenceEntity

enum class HealthStatus {
    GREEN,   // Comfortable
    YELLOW,  // Caution
    RED      // Deficit / Critical Warning
}

data class FinancialHealthReport(
    val status: HealthStatus,
    val title: String,
    val reasons: List<String>,
    val overspentCategories: List<String>,
    val upcomingBillsImpact: List<String>,
    val practicalSuggestions: List<String>,
    val recommendedBudgetAdjustments: List<String>
)

object FinancialHealthAnalyzer {

    fun analyze(
        summary: MonthlyFinancialSummary,
        pendingBills: List<RecurringBillOccurrenceEntity>,
        targetSavingsRate: Double = 20.0,
        lowBalanceThresholdPaise: Long = 500000L // ₹5,000
    ): FinancialHealthReport {
        val reasons = mutableListOf<String>()
        val overspentCategories = mutableListOf<String>()
        val upcomingBillsImpact = mutableListOf<String>()
        val practicalSuggestions = mutableListOf<String>()
        val recommendedBudgetAdjustments = mutableListOf<String>()

        var isRed = false
        var isYellow = false

        // Check 1: Insufficient data check
        if (summary.totalIncomePaise == 0L && summary.totalExpensePaise == 0L) {
            return FinancialHealthReport(
                status = HealthStatus.YELLOW,
                title = "Insufficient Data",
                reasons = listOf("No confirmed income or expense records found for this month yet. Add your income and expenses to assess financial health."),
                overspentCategories = emptyList(),
                upcomingBillsImpact = emptyList(),
                practicalSuggestions = listOf("Log Papa's salary or other family income to begin monitoring cash flow."),
                recommendedBudgetAdjustments = emptyList()
            )
        }

        // Rule 1: Red if monthly net cash flow is negative
        if (summary.netCashFlowPaise < 0) {
            isRed = true
            val deficitAmount = CurrencyFormatter.formatPaise(-summary.netCashFlowPaise)
            reasons.add("Monthly expenses exceed total income by $deficitAmount (Deficit).")
            practicalSuggestions.add("Pause non-essential shopping and entertainment until income replenishes reserves.")
        }

        // Rule 2: Upcoming bills vs recorded available balance
        val totalPendingBillsPaise = pendingBills.sumOf { it.expectedAmountPaise }
        if (pendingBills.isNotEmpty()) {
            val billAmountFormatted = CurrencyFormatter.formatPaise(totalPendingBillsPaise)
            if (totalPendingBillsPaise > summary.availableBalancePaise) {
                isRed = true
                val shortfall = CurrencyFormatter.formatPaise(totalPendingBillsPaise - summary.availableBalancePaise)
                reasons.add("Upcoming scheduled bills ($billAmountFormatted) exceed available balance by $shortfall!")
                upcomingBillsImpact.add("Urgent: Fund shortage of $shortfall for scheduled recurring bills.")
                practicalSuggestions.add("Prioritize essential recurring payments (cooking gas/school fees) from current cash or bank funds.")
            } else {
                upcomingBillsImpact.add("Upcoming scheduled bills require $billAmountFormatted, which is covered by current available funds (${CurrencyFormatter.formatPaise(summary.availableBalancePaise)}).")
            }
        }

        // Rule 3: Available balance below low balance threshold
        if (summary.availableBalancePaise < lowBalanceThresholdPaise) {
            isYellow = true
            val thresholdStr = CurrencyFormatter.formatPaise(lowBalanceThresholdPaise)
            reasons.add("Recorded available cash/bank balance (${CurrencyFormatter.formatPaise(summary.availableBalancePaise)}) is below safety threshold ($thresholdStr).")
            practicalSuggestions.add("Maintain an emergency buffer in the bank account to avoid liquidity stress.")
        }

        // Rule 4: Family budget utilization
        if (summary.familyBudgetLimitPaise != null && summary.familyBudgetLimitPaise > 0) {
            val limit = summary.familyBudgetLimitPaise
            val spent = summary.totalExpensePaise
            if (spent > limit) {
                isYellow = true
                val overspend = CurrencyFormatter.formatPaise(spent - limit)
                reasons.add("Overall family budget exceeded by $overspend (${String.format("%.1f", summary.familyBudgetUtilizationPercentage)}% used).")
                recommendedBudgetAdjustments.add("Review overall monthly family budget cap or reallocate from flexible spending categories.")
            }
        }

        // Rule 5: Category-wise overspending
        summary.categorySummaries.forEach { cat ->
            if (cat.budgetLimitPaise != null && cat.budgetLimitPaise > 0 && cat.totalExpensePaise > cat.budgetLimitPaise) {
                isYellow = true
                val over = CurrencyFormatter.formatPaise(cat.totalExpensePaise - cat.budgetLimitPaise)
                overspentCategories.add("${cat.categoryName}: Exceeded by $over")
                reasons.add("${cat.categoryName} exceeded budget by $over.")
                recommendedBudgetAdjustments.add("Adjust ${cat.categoryName} budget or curb weekly allocations.")
            }
        }

        // Rule 6: Savings rate below target
        val actualRate = summary.savingsRatePercentage
        if (actualRate != null) {
            if (actualRate < targetSavingsRate && summary.netCashFlowPaise >= 0) {
                isYellow = true
                reasons.add("Savings rate of ${String.format("%.1f", actualRate)}% is below your household target of ${String.format("%.0f", targetSavingsRate)}%.")
                practicalSuggestions.add("Aim to save at least ${String.format("%.0f", targetSavingsRate)}% of monthly family income to build long-term safety.")
            }
        }

        // Determine Final Status
        val status = when {
            isRed -> HealthStatus.RED
            isYellow -> HealthStatus.YELLOW
            else -> HealthStatus.GREEN
        }

        val title = when (status) {
            HealthStatus.GREEN -> "Comfortable"
            HealthStatus.YELLOW -> "Caution (Budget Pressure)"
            HealthStatus.RED -> "Deficit / Critical Alert"
        }

        if (status == HealthStatus.GREEN) {
            reasons.add("Family cash flow is positive with surplus of ${CurrencyFormatter.formatPaise(summary.netCashFlowPaise)}.")
            if (actualRate != null) {
                reasons.add("Savings rate is healthy at ${String.format("%.1f", actualRate)}% (target: ${String.format("%.0f", targetSavingsRate)}%).")
            }
            reasons.add("All budgets and upcoming obligations are comfortably managed within available funds.")
            practicalSuggestions.add("Consider transferring part of the surplus (${CurrencyFormatter.formatPaise(summary.netCashFlowPaise)}) to dedicated savings goals.")
        }

        return FinancialHealthReport(
            status = status,
            title = title,
            reasons = reasons,
            overspentCategories = overspentCategories,
            upcomingBillsImpact = upcomingBillsImpact,
            practicalSuggestions = practicalSuggestions,
            recommendedBudgetAdjustments = recommendedBudgetAdjustments
        )
    }
}
