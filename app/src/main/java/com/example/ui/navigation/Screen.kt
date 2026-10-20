package com.example.ui.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Transactions : Screen("transactions")
    object Reports : Screen("reports")
    object Settings : Screen("settings")

    // Secondary / sub screens
    object Members : Screen("members")
    object Vehicles : Screen("vehicles")
    object Budgets : Screen("budgets")
    object SavingsGoals : Screen("savings_goals")
    object RecurringBills : Screen("recurring_bills")
    object BackupRestore : Screen("backup_restore")
    object SecuritySettings : Screen("security_settings")
    object Onboarding : Screen("onboarding")
}
