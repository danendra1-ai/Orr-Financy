package com.example

import com.example.data.local.entity.BudgetEntity
import com.example.data.local.entity.CategoryEntity
import com.example.data.local.entity.FamilyMemberEntity
import com.example.data.local.entity.FinancialAccountEntity
import com.example.data.local.entity.InternalTransferEntity
import com.example.data.local.entity.RecurringBillOccurrenceEntity
import com.example.data.local.entity.TransactionEntity
import com.example.data.local.entity.VehicleEntity
import com.example.domain.CurrencyFormatter
import com.example.domain.FinancialCalculator
import com.example.domain.FinancialHealthAnalyzer
import com.example.domain.HealthStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class OrrExpencyFinancialTest {

    private val cal = Calendar.getInstance().apply {
        set(Calendar.MONTH, Calendar.OCTOBER)
        set(Calendar.YEAR, 2026)
        set(Calendar.DAY_OF_MONTH, 15)
    }
    private val testDate = cal.timeInMillis

    private val memberPapa = FamilyMemberEntity(id = "mem_papa", name = "Papa")
    private val memberMummy = FamilyMemberEntity(id = "mem_mummy", name = "Mummy")
    private val memberMe = FamilyMemberEntity(id = "mem_me", name = "Me")
    private val memberSister = FamilyMemberEntity(id = "mem_sister", name = "Sister")
    private val testMembers = listOf(memberPapa, memberMummy, memberMe, memberSister)

    private val catSalary = CategoryEntity(id = "c_sal", name = "Salary", categoryType = "INCOME")
    private val catRation = CategoryEntity(id = "c_ration", name = "Ration & Groceries", categoryType = "EXPENSE")
    private val catPetrol = CategoryEntity(id = "c_petrol", name = "Vehicle Petrol", categoryType = "EXPENSE")
    private val testCategories = listOf(catSalary, catRation, catPetrol)

    private val bike1 = VehicleEntity(id = "v_bike1", name = "Papa Bike")
    private val bike2 = VehicleEntity(id = "v_bike2", name = "Family Bike")
    private val testVehicles = listOf(bike1, bike2)

    /**
     * Test A: Monthly surplus (from prompt Section 23)
     * Given:
     * - Opening balance: ₹10,000 (1000000 paise)
     * - Confirmed monthly income: ₹30,000 (3000000 paise)
     * - Confirmed monthly expenses: ₹20,000 (2000000 paise)
     * Expected:
     * - Actual income: ₹30,000
     * - Actual expenses: ₹20,000
     * - Monthly net surplus: ₹10,000
     * - Savings rate: 33.33% approx
     * - Closing available balance: ₹20,000
     */
    @Test
    fun testMonthlySurplus() {
        val account = FinancialAccountEntity(id = "acc1", name = "Bank", accountType = "BANK", openingBalancePaise = 1000000L)
        val incomeTx = TransactionEntity(
            transactionType = "INCOME",
            amountPaise = 3000000L,
            categoryId = catSalary.id,
            transactionDate = testDate,
            paidByMemberId = memberPapa.id,
            accountId = account.id
        )
        val expenseTx = TransactionEntity(
            transactionType = "EXPENSE",
            amountPaise = 2000000L,
            categoryId = catRation.id,
            transactionDate = testDate,
            paidByMemberId = memberPapa.id,
            accountId = account.id
        )

        val summary = FinancialCalculator.calculateMonthlySummary(
            month = Calendar.OCTOBER,
            year = 2026,
            allTransactions = listOf(incomeTx, expenseTx),
            allTransfers = emptyList(),
            allAccounts = listOf(account),
            allMembers = testMembers,
            allCategories = testCategories,
            allVehicles = testVehicles,
            allBudgets = emptyList()
        )

        assertEquals(3000000L, summary.totalIncomePaise)
        assertEquals(2000000L, summary.totalExpensePaise)
        assertEquals(1000000L, summary.netCashFlowPaise)
        assertNotNull(summary.savingsRatePercentage)
        assertEquals(33.33, summary.savingsRatePercentage!!, 0.1)

        val balances = FinancialCalculator.calculateAccountBalances(listOf(account), listOf(incomeTx, expenseTx), emptyList())
        val closingBal = FinancialCalculator.calculateTotalAvailableBalance(balances)
        assertEquals(2000000L, closingBal) // 10k opening + 30k inc - 20k exp = 20k
    }

    /**
     * Test B: Monthly deficit (from prompt Section 23)
     * Given:
     * - Opening balance: ₹5,000
     * - Confirmed monthly income: ₹20,000
     * - Confirmed monthly expenses: ₹25,000
     * Expected:
     * - Net cash flow: -₹5,000
     * - Closing balance: ₹0
     * - Financial health: Red (deficit)
     */
    @Test
    fun testMonthlyDeficit() {
        val account = FinancialAccountEntity(id = "acc1", name = "Bank", accountType = "BANK", openingBalancePaise = 500000L)
        val incomeTx = TransactionEntity(
            transactionType = "INCOME",
            amountPaise = 2000000L,
            categoryId = catSalary.id,
            transactionDate = testDate,
            paidByMemberId = memberPapa.id,
            accountId = account.id
        )
        val expenseTx = TransactionEntity(
            transactionType = "EXPENSE",
            amountPaise = 2500000L,
            categoryId = catRation.id,
            transactionDate = testDate,
            paidByMemberId = memberPapa.id,
            accountId = account.id
        )

        val summary = FinancialCalculator.calculateMonthlySummary(
            month = Calendar.OCTOBER,
            year = 2026,
            allTransactions = listOf(incomeTx, expenseTx),
            allTransfers = emptyList(),
            allAccounts = listOf(account),
            allMembers = testMembers,
            allCategories = testCategories,
            allVehicles = testVehicles,
            allBudgets = emptyList()
        )

        assertEquals(-500000L, summary.netCashFlowPaise)

        val balances = FinancialCalculator.calculateAccountBalances(listOf(account), listOf(incomeTx, expenseTx), emptyList())
        val closingBal = FinancialCalculator.calculateTotalAvailableBalance(balances)
        assertEquals(0L, closingBal)

        val health = FinancialHealthAnalyzer.analyze(summary, emptyList())
        assertEquals(HealthStatus.RED, health.status)
    }

    /**
     * Test C: Internal transfer (from prompt Section 23)
     * Given:
     * - Bank account: ₹10,000
     * - Cash wallet: ₹1,000
     * - Transfer ₹2,000 from bank to cash
     * Expected:
     * - Bank: ₹8,000
     * - Cash: ₹3,000
     * - Combined balance remains ₹11,000
     * - No income or expense recorded
     */
    @Test
    fun testInternalTransfer() {
        val bank = FinancialAccountEntity(id = "acc_bank", name = "Bank", accountType = "BANK", openingBalancePaise = 1000000L)
        val cash = FinancialAccountEntity(id = "acc_cash", name = "Cash", accountType = "CASH", openingBalancePaise = 100000L)
        val accounts = listOf(bank, cash)

        val transfer = InternalTransferEntity(
            sourceAccountId = bank.id,
            destinationAccountId = cash.id,
            amountPaise = 200000L,
            transferDate = testDate
        )

        val balances = FinancialCalculator.calculateAccountBalances(accounts, emptyList(), listOf(transfer))
        val bankBal = balances.first { it.account.id == bank.id }.currentBalancePaise
        val cashBal = balances.first { it.account.id == cash.id }.currentBalancePaise
        val combined = FinancialCalculator.calculateTotalAvailableBalance(balances)

        assertEquals(800000L, bankBal) // ₹8,000
        assertEquals(300000L, cashBal) // ₹3,000
        assertEquals(1100000L, combined) // ₹11,000 unchanged

        val summary = FinancialCalculator.calculateMonthlySummary(
            month = Calendar.OCTOBER,
            year = 2026,
            allTransactions = emptyList(),
            allTransfers = listOf(transfer),
            allAccounts = accounts,
            allMembers = testMembers,
            allCategories = testCategories,
            allVehicles = testVehicles,
            allBudgets = emptyList()
        )
        assertEquals(0L, summary.totalIncomePaise)
        assertEquals(0L, summary.totalExpensePaise)
    }

    /**
     * Test D: One expense, multiple views (from prompt Section 23)
     * Given:
     * - A ₹1,000 petrol transaction linked to Vehicle 1 paid by Papa
     * Expected:
     * - Family expense increases by ₹1,000
     * - Vehicle 1 expense increases by ₹1,000
     * - Papa's paid-by total increases by ₹1,000
     * - No duplicated expense appears in family grand total
     */
    @Test
    fun testOneExpenseMultipleViews() {
        val account = FinancialAccountEntity(id = "acc1", name = "Bank", accountType = "BANK", openingBalancePaise = 500000L)
        val tx = TransactionEntity(
            transactionType = "EXPENSE",
            amountPaise = 100000L, // ₹1,000
            categoryId = catPetrol.id,
            transactionDate = testDate,
            paidByMemberId = memberPapa.id,
            responsibleMemberId = null, // Family wide
            accountId = account.id,
            vehicleId = bike1.id,
            vehicleExpenseType = "PETROL"
        )

        val summary = FinancialCalculator.calculateMonthlySummary(
            month = Calendar.OCTOBER,
            year = 2026,
            allTransactions = listOf(tx),
            allTransfers = emptyList(),
            allAccounts = listOf(account),
            allMembers = testMembers,
            allCategories = testCategories,
            allVehicles = testVehicles,
            allBudgets = emptyList()
        )

        // Family grand total exactly ₹1,000 (NOT duplicated)
        assertEquals(100000L, summary.totalExpensePaise)

        // Vehicle 1 total is ₹1,000
        val v1Summary = summary.vehicleSummaries.first { it.vehicle.id == bike1.id }
        assertEquals(100000L, v1Summary.totalMonthPaise)
        assertEquals(100000L, v1Summary.petrolPaise)

        // Papa paid-by total is ₹1,000
        val papaSummary = summary.memberSummaries.first { it.memberId == memberPapa.id }
        assertEquals(100000L, papaSummary.paidByPaise)
    }

    /**
     * Test E: Currency Formatter Indian Numbering & Parsing
     */
    @Test
    fun testCurrencyFormatter() {
        assertEquals("₹1,00,000.00", CurrencyFormatter.formatPaise(10000000L))
        assertEquals("₹25,500.50", CurrencyFormatter.formatPaise(2550050L))
        assertEquals("₹500.00", CurrencyFormatter.formatPaise(50000L))
        assertEquals("-₹5,000.00", CurrencyFormatter.formatPaise(-500000L))

        assertEquals(10000000L, CurrencyFormatter.parseToPaise("1,00,000"))
        assertEquals(150050L, CurrencyFormatter.parseToPaise("1500.50"))
        assertEquals(50000L, CurrencyFormatter.parseToPaise("₹500"))
    }
}
