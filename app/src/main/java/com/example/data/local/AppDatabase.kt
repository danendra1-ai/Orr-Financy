package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.BudgetDao
import com.example.data.local.dao.CategoryDao
import com.example.data.local.dao.FamilyMemberDao
import com.example.data.local.dao.FinancialAccountDao
import com.example.data.local.dao.InternalTransferDao
import com.example.data.local.dao.RecurringBillDao
import com.example.data.local.dao.SavingsGoalDao
import com.example.data.local.dao.TransactionDao
import com.example.data.local.dao.VehicleDao
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        FamilyMemberEntity::class,
        FinancialAccountEntity::class,
        CategoryEntity::class,
        TransactionEntity::class,
        InternalTransferEntity::class,
        BudgetEntity::class,
        VehicleEntity::class,
        RecurringBillEntity::class,
        RecurringBillOccurrenceEntity::class,
        SavingsGoalEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun familyMemberDao(): FamilyMemberDao
    abstract fun financialAccountDao(): FinancialAccountDao
    abstract fun categoryDao(): CategoryDao
    abstract fun transactionDao(): TransactionDao
    abstract fun internalTransferDao(): InternalTransferDao
    abstract fun budgetDao(): BudgetDao
    abstract fun vehicleDao(): VehicleDao
    abstract fun recurringBillDao(): RecurringBillDao
    abstract fun savingsGoalDao(): SavingsGoalDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "orr_expency_db"
                ).addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        CoroutineScope(Dispatchers.IO).launch {
                            seedDefaultData(getInstance(context))
                        }
                    }
                }).build()
                INSTANCE = instance
                instance
            }
        }

        suspend fun seedDefaultData(database: AppDatabase) {
            // Seed Default Family Members
            val members = listOf(
                FamilyMemberEntity(id = "member_papa", name = "Papa"),
                FamilyMemberEntity(id = "member_mummy", name = "Mummy"),
                FamilyMemberEntity(id = "member_me", name = "Me"),
                FamilyMemberEntity(id = "member_sister", name = "Sister")
            )
            database.familyMemberDao().insertAll(members)

            // Seed Default Categories
            val categories = listOf(
                CategoryEntity(id = "cat_ration", name = "Ration & Groceries", isSystemDefault = true),
                CategoryEntity(id = "cat_veg_fruits", name = "Vegetables & Fruits", isSystemDefault = true),
                CategoryEntity(id = "cat_milk_dairy", name = "Milk & Dairy", isSystemDefault = true),
                CategoryEntity(id = "cat_cooking_gas", name = "Cooking Gas", isSystemDefault = true),
                CategoryEntity(id = "cat_medical", name = "Medical & Medicines", isSystemDefault = true),
                CategoryEntity(id = "cat_education", name = "Education & College", isSystemDefault = true),
                CategoryEntity(id = "cat_mobile_net", name = "Mobile & Internet", isSystemDefault = true),
                CategoryEntity(id = "cat_clothing", name = "Clothing & Shopping", isSystemDefault = true),
                CategoryEntity(id = "cat_maintenance", name = "House Maintenance", isSystemDefault = true),
                CategoryEntity(id = "cat_travel", name = "Travel & Transport", isSystemDefault = true),
                CategoryEntity(id = "cat_entertainment", name = "Entertainment", isSystemDefault = true),
                CategoryEntity(id = "cat_emergency", name = "Emergency Expenses", isSystemDefault = true),
                CategoryEntity(id = "cat_vehicle", name = "Vehicle Expenses", isSystemDefault = true),
                CategoryEntity(id = "cat_other_exp", name = "Other Expenses", isSystemDefault = true),
                // Income categories
                CategoryEntity(id = "cat_inc_salary", name = "Salary / Business", categoryType = "INCOME", isSystemDefault = true),
                CategoryEntity(id = "cat_inc_other", name = "Other Income", categoryType = "INCOME", isSystemDefault = true)
            )
            database.categoryDao().insertAll(categories)

            // Seed Default Vehicles (Two Bikes)
            val vehicles = listOf(
                VehicleEntity(id = "vehicle_papa_bike", name = "Papa Bike", vehicleType = "BIKE"),
                VehicleEntity(id = "vehicle_family_bike", name = "Family Bike", vehicleType = "BIKE")
            )
            database.vehicleDao().insertAll(vehicles)

            // Seed Default Accounts if none exist
            val accounts = listOf(
                FinancialAccountEntity(id = "acc_cash", name = "Cash Wallet", accountType = "CASH", openingBalancePaise = 0L),
                FinancialAccountEntity(id = "acc_bank", name = "Primary Bank Account", accountType = "BANK", openingBalancePaise = 0L)
            )
            database.financialAccountDao().insertAll(accounts)
        }
    }
}
