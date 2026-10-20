package com.example.backup

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
import kotlinx.coroutines.flow.first
import org.json.JSONArray
import org.json.JSONObject
import java.security.MessageDigest

data class BackupPreview(
    val version: Int,
    val exportTimestamp: Long,
    val memberCount: Int,
    val accountCount: Int,
    val categoryCount: Int,
    val vehicleCount: Int,
    val transactionCount: Int,
    val transferCount: Int,
    val budgetCount: Int,
    val billCount: Int,
    val goalCount: Int
)

class BackupManager(private val database: AppDatabase) {

    /**
     * Generates a complete versioned JSON backup with SHA-256 integrity checksum.
     */
    suspend fun createBackupJson(): String {
        val members = database.familyMemberDao().getAllMembers().first()
        val accounts = database.financialAccountDao().getAllAccounts().first()
        val categories = database.categoryDao().getAllCategories().first()
        val vehicles = database.vehicleDao().getAllVehicles().first()
        val transactions = database.transactionDao().getAllTransactions().first()
        val transfers = database.internalTransferDao().getAllTransfers().first()
        val budgets = database.budgetDao().getAllBudgets().first()
        val bills = database.recurringBillDao().getAllBills().first()
        val goals = database.savingsGoalDao().getAllGoals().first()

        val payload = JSONObject().apply {
            put("members", JSONArray().apply {
                members.forEach { m ->
                    put(JSONObject().apply {
                        put("id", m.id)
                        put("name", m.name)
                        put("isActive", m.isActive)
                        put("createdAt", m.createdAt)
                        put("notes", m.notes ?: "")
                    })
                }
            })
            put("accounts", JSONArray().apply {
                accounts.forEach { a ->
                    put(JSONObject().apply {
                        put("id", a.id)
                        put("name", a.name)
                        put("accountType", a.accountType)
                        put("openingBalancePaise", a.openingBalancePaise)
                        put("openingDate", a.openingDate)
                        put("isActive", a.isActive)
                    })
                }
            })
            put("categories", JSONArray().apply {
                categories.forEach { c ->
                    put(JSONObject().apply {
                        put("id", c.id)
                        put("name", c.name)
                        put("categoryType", c.categoryType)
                        put("iconName", c.iconName ?: "")
                        put("isSystemDefault", c.isSystemDefault)
                        put("isActive", c.isActive)
                    })
                }
            })
            put("vehicles", JSONArray().apply {
                vehicles.forEach { v ->
                    put(JSONObject().apply {
                        put("id", v.id)
                        put("name", v.name)
                        put("vehicleType", v.vehicleType)
                        put("registrationLabel", v.registrationLabel ?: "")
                        put("isActive", v.isActive)
                        put("notes", v.notes ?: "")
                    })
                }
            })
            put("transactions", JSONArray().apply {
                transactions.forEach { t ->
                    put(JSONObject().apply {
                        put("id", t.id)
                        put("transactionType", t.transactionType)
                        put("amountPaise", t.amountPaise)
                        put("categoryId", t.categoryId)
                        put("transactionDate", t.transactionDate)
                        put("paidByMemberId", t.paidByMemberId)
                        put("responsibleMemberId", t.responsibleMemberId ?: "")
                        put("accountId", t.accountId)
                        put("paymentMethod", t.paymentMethod)
                        put("notes", t.notes ?: "")
                        put("vehicleId", t.vehicleId ?: "")
                        put("vehicleExpenseType", t.vehicleExpenseType ?: "")
                        put("recurringBillOccurrenceId", t.recurringBillOccurrenceId ?: "")
                        put("status", t.status)
                        put("createdAt", t.createdAt)
                        put("updatedAt", t.updatedAt)
                    })
                }
            })
            put("transfers", JSONArray().apply {
                transfers.forEach { tr ->
                    put(JSONObject().apply {
                        put("id", tr.id)
                        put("sourceAccountId", tr.sourceAccountId)
                        put("destinationAccountId", tr.destinationAccountId)
                        put("amountPaise", tr.amountPaise)
                        put("transferDate", tr.transferDate)
                        put("notes", tr.notes ?: "")
                        put("createdAt", tr.createdAt)
                    })
                }
            })
            put("budgets", JSONArray().apply {
                budgets.forEach { b ->
                    put(JSONObject().apply {
                        put("id", b.id)
                        put("budgetName", b.budgetName)
                        put("budgetType", b.budgetType)
                        put("memberId", b.memberId ?: "")
                        put("categoryId", b.categoryId ?: "")
                        put("amountPaise", b.amountPaise)
                        put("period", b.period)
                        put("isActive", b.isActive)
                    })
                }
            })
            put("bills", JSONArray().apply {
                bills.forEach { bl ->
                    put(JSONObject().apply {
                        put("id", bl.id)
                        put("name", bl.name)
                        put("expectedAmountPaise", bl.expectedAmountPaise)
                        put("frequency", bl.frequency)
                        put("nextDueDate", bl.nextDueDate)
                        put("categoryId", bl.categoryId)
                        put("responsibleMemberId", bl.responsibleMemberId ?: "")
                        put("isActive", bl.isActive)
                    })
                }
            })
            put("goals", JSONArray().apply {
                goals.forEach { g ->
                    put(JSONObject().apply {
                        put("id", g.id)
                        put("name", g.name)
                        put("targetAmountPaise", g.targetAmountPaise)
                        put("allocatedAmountPaise", g.allocatedAmountPaise)
                        put("targetDate", g.targetDate ?: 0L)
                        put("notes", g.notes ?: "")
                        put("status", g.status)
                    })
                }
            })
        }

        val payloadStr = payload.toString()
        val checksum = sha256(payloadStr)

        val root = JSONObject().apply {
            put("appName", "Orr Expency")
            put("version", 1)
            put("exportTimestamp", System.currentTimeMillis())
            put("checksum", checksum)
            put("payload", payload)
        }

        return root.toString(2)
    }

    /**
     * Inspects and validates a backup JSON without modifying database.
     */
    fun inspectBackup(jsonString: String): Result<BackupPreview> {
        return try {
            val root = JSONObject(jsonString)
            if (!root.has("appName") || root.getString("appName") != "Orr Expency") {
                return Result.failure(IllegalArgumentException("Invalid file: Not an Orr Expency backup"))
            }

            val version = root.getInt("version")
            val exportTime = root.getLong("exportTimestamp")
            val recordedChecksum = root.getString("checksum")
            val payload = root.getJSONObject("payload")

            val computedChecksum = sha256(payload.toString())
            if (computedChecksum != recordedChecksum) {
                return Result.failure(IllegalStateException("Backup file integrity check failed! Checksum mismatch."))
            }

            val preview = BackupPreview(
                version = version,
                exportTimestamp = exportTime,
                memberCount = payload.optJSONArray("members")?.length() ?: 0,
                accountCount = payload.optJSONArray("accounts")?.length() ?: 0,
                categoryCount = payload.optJSONArray("categories")?.length() ?: 0,
                vehicleCount = payload.optJSONArray("vehicles")?.length() ?: 0,
                transactionCount = payload.optJSONArray("transactions")?.length() ?: 0,
                transferCount = payload.optJSONArray("transfers")?.length() ?: 0,
                budgetCount = payload.optJSONArray("budgets")?.length() ?: 0,
                billCount = payload.optJSONArray("bills")?.length() ?: 0,
                goalCount = payload.optJSONArray("goals")?.length() ?: 0
            )

            Result.success(preview)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Atomically restores database from valid backup JSON.
     */
    suspend fun restoreBackup(jsonString: String): Result<Unit> {
        val inspection = inspectBackup(jsonString)
        if (inspection.isFailure) {
            return Result.failure(inspection.exceptionOrNull() ?: Exception("Corrupt backup"))
        }

        return try {
            val root = JSONObject(jsonString)
            val payload = root.getJSONObject("payload")

            database.withTransaction {
                // Clear existing tables
                database.transactionDao().deleteAll()
                database.internalTransferDao().deleteAll()
                database.budgetDao().deleteAll()
                database.savingsGoalDao().deleteAll()
                database.recurringBillDao().deleteAllOccurrences()
                database.recurringBillDao().deleteAllBills()
                database.financialAccountDao().deleteAll()
                database.vehicleDao().deleteAll()
                database.familyMemberDao().deleteAll()
                database.categoryDao().deleteAll()

                // Insert members
                val membersArr = payload.optJSONArray("members") ?: JSONArray()
                val members = (0 until membersArr.length()).map { i ->
                    val obj = membersArr.getJSONObject(i)
                    FamilyMemberEntity(
                        id = obj.getString("id"),
                        name = obj.getString("name"),
                        isActive = obj.optBoolean("isActive", true),
                        createdAt = obj.optLong("createdAt", System.currentTimeMillis()),
                        notes = obj.optString("notes").ifEmpty { null }
                    )
                }
                database.familyMemberDao().insertAll(members)

                // Insert accounts
                val accountsArr = payload.optJSONArray("accounts") ?: JSONArray()
                val accounts = (0 until accountsArr.length()).map { i ->
                    val obj = accountsArr.getJSONObject(i)
                    FinancialAccountEntity(
                        id = obj.getString("id"),
                        name = obj.getString("name"),
                        accountType = obj.getString("accountType"),
                        openingBalancePaise = obj.optLong("openingBalancePaise", 0L),
                        openingDate = obj.optLong("openingDate", System.currentTimeMillis()),
                        isActive = obj.optBoolean("isActive", true)
                    )
                }
                database.financialAccountDao().insertAll(accounts)

                // Insert categories
                val categoriesArr = payload.optJSONArray("categories") ?: JSONArray()
                val categories = (0 until categoriesArr.length()).map { i ->
                    val obj = categoriesArr.getJSONObject(i)
                    CategoryEntity(
                        id = obj.getString("id"),
                        name = obj.getString("name"),
                        categoryType = obj.optString("categoryType", "EXPENSE"),
                        iconName = obj.optString("iconName").ifEmpty { null },
                        isSystemDefault = obj.optBoolean("isSystemDefault", false),
                        isActive = obj.optBoolean("isActive", true)
                    )
                }
                database.categoryDao().insertAll(categories)

                // Insert vehicles
                val vehiclesArr = payload.optJSONArray("vehicles") ?: JSONArray()
                val vehicles = (0 until vehiclesArr.length()).map { i ->
                    val obj = vehiclesArr.getJSONObject(i)
                    VehicleEntity(
                        id = obj.getString("id"),
                        name = obj.getString("name"),
                        vehicleType = obj.optString("vehicleType", "BIKE"),
                        registrationLabel = obj.optString("registrationLabel").ifEmpty { null },
                        isActive = obj.optBoolean("isActive", true),
                        notes = obj.optString("notes").ifEmpty { null }
                    )
                }
                database.vehicleDao().insertAll(vehicles)

                // Insert transactions
                val txArr = payload.optJSONArray("transactions") ?: JSONArray()
                val transactions = (0 until txArr.length()).map { i ->
                    val obj = txArr.getJSONObject(i)
                    TransactionEntity(
                        id = obj.getString("id"),
                        transactionType = obj.getString("transactionType"),
                        amountPaise = obj.getLong("amountPaise"),
                        categoryId = obj.getString("categoryId"),
                        transactionDate = obj.getLong("transactionDate"),
                        paidByMemberId = obj.getString("paidByMemberId"),
                        responsibleMemberId = obj.optString("responsibleMemberId").ifEmpty { null },
                        accountId = obj.getString("accountId"),
                        paymentMethod = obj.optString("paymentMethod", "CASH"),
                        notes = obj.optString("notes").ifEmpty { null },
                        vehicleId = obj.optString("vehicleId").ifEmpty { null },
                        vehicleExpenseType = obj.optString("vehicleExpenseType").ifEmpty { null },
                        recurringBillOccurrenceId = obj.optString("recurringBillOccurrenceId").ifEmpty { null },
                        status = obj.optString("status", "CONFIRMED"),
                        createdAt = obj.optLong("createdAt", System.currentTimeMillis()),
                        updatedAt = obj.optLong("updatedAt", System.currentTimeMillis())
                    )
                }
                database.transactionDao().insertAll(transactions)

                // Insert transfers
                val trArr = payload.optJSONArray("transfers") ?: JSONArray()
                val transfers = (0 until trArr.length()).map { i ->
                    val obj = trArr.getJSONObject(i)
                    InternalTransferEntity(
                        id = obj.getString("id"),
                        sourceAccountId = obj.getString("sourceAccountId"),
                        destinationAccountId = obj.getString("destinationAccountId"),
                        amountPaise = obj.getLong("amountPaise"),
                        transferDate = obj.getLong("transferDate"),
                        notes = obj.optString("notes").ifEmpty { null },
                        createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                    )
                }
                database.internalTransferDao().insertAll(transfers)

                // Insert budgets
                val budgetsArr = payload.optJSONArray("budgets") ?: JSONArray()
                val budgets = (0 until budgetsArr.length()).map { i ->
                    val obj = budgetsArr.getJSONObject(i)
                    BudgetEntity(
                        id = obj.getString("id"),
                        budgetName = obj.getString("budgetName"),
                        budgetType = obj.getString("budgetType"),
                        memberId = obj.optString("memberId").ifEmpty { null },
                        categoryId = obj.optString("categoryId").ifEmpty { null },
                        amountPaise = obj.getLong("amountPaise"),
                        period = obj.optString("period", "MONTHLY"),
                        isActive = obj.optBoolean("isActive", true)
                    )
                }
                database.budgetDao().insertAll(budgets)

                // Insert bills
                val billsArr = payload.optJSONArray("bills") ?: JSONArray()
                val bills = (0 until billsArr.length()).map { i ->
                    val obj = billsArr.getJSONObject(i)
                    RecurringBillEntity(
                        id = obj.getString("id"),
                        name = obj.getString("name"),
                        expectedAmountPaise = obj.getLong("expectedAmountPaise"),
                        frequency = obj.optString("frequency", "MONTHLY"),
                        nextDueDate = obj.getLong("nextDueDate"),
                        categoryId = obj.getString("categoryId"),
                        responsibleMemberId = obj.optString("responsibleMemberId").ifEmpty { null },
                        isActive = obj.optBoolean("isActive", true)
                    )
                }
                database.recurringBillDao().insertAllBills(bills)

                // Insert goals
                val goalsArr = payload.optJSONArray("goals") ?: JSONArray()
                val goals = (0 until goalsArr.length()).map { i ->
                    val obj = goalsArr.getJSONObject(i)
                    SavingsGoalEntity(
                        id = obj.getString("id"),
                        name = obj.getString("name"),
                        targetAmountPaise = obj.getLong("targetAmountPaise"),
                        allocatedAmountPaise = obj.optLong("allocatedAmountPaise", 0L),
                        targetDate = obj.optLong("targetDate").let { if (it == 0L) null else it },
                        notes = obj.optString("notes").ifEmpty { null },
                        status = obj.optString("status", "IN_PROGRESS")
                    )
                }
                database.savingsGoalDao().insertAll(goals)
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun sha256(input: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(input.toByteArray(Charsets.UTF_8))
        return hash.joinToString("") { "%02x".format(it) }
    }
}
