package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Assessment
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.ui.unit.dp
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.example.ui.navigation.Screen
import com.example.ui.screens.bills.RecurringBillsScreen
import com.example.ui.screens.budgets.BudgetsScreen
import com.example.ui.screens.dashboard.DashboardScreen
import com.example.ui.screens.members.MembersScreen
import com.example.ui.screens.onboarding.OnboardingScreen
import com.example.ui.screens.reports.ReportsScreen
import com.example.ui.screens.savings.SavingsGoalsScreen
import com.example.ui.screens.security.LockScreen
import com.example.ui.screens.settings.SettingsScreen
import com.example.ui.screens.transactions.AddEditTransactionSheet
import com.example.ui.screens.transactions.TransactionsScreen
import com.example.ui.screens.vehicles.VehiclesScreen
import com.example.ui.theme.OrrExpencyTheme
import com.example.ui.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val themeMode by viewModel.themeMode.collectAsState()
            val isLocked by viewModel.isAppLocked.collectAsState()
            val isOnboardingCompleted by viewModel.isOnboardingCompleted.collectAsState()

            OrrExpencyTheme(themeMode = themeMode) {
                when {
                    !isOnboardingCompleted -> {
                        OnboardingScreen(
                            viewModel = viewModel,
                            onFinished = { /* Onboarding completed state updates automatically */ }
                        )
                    }
                    isLocked -> {
                        LockScreen(viewModel = viewModel)
                    }
                    else -> {
                        MainAppContent(viewModel = viewModel)
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.checkAutoLock()
    }
}

@Composable
fun MainAppContent(viewModel: MainViewModel) {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Home) }
    var showAddSheet by remember { mutableStateOf(false) }

    // Intercept back button when inside secondary sub-screens
    if (currentScreen !in listOf(Screen.Home, Screen.Transactions, Screen.Reports, Screen.Settings)) {
        BackHandler {
            currentScreen = Screen.Home
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (currentScreen in listOf(Screen.Home, Screen.Transactions, Screen.Reports, Screen.Settings)) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 4.dp,
                    windowInsets = WindowInsets.navigationBars
                ) {
                    NavigationBarItem(
                        selected = currentScreen == Screen.Home,
                        onClick = { currentScreen = Screen.Home },
                        icon = {
                            Icon(
                                imageVector = if (currentScreen == Screen.Home) Icons.Filled.Home else Icons.Outlined.Home,
                                contentDescription = "Home"
                            )
                        },
                        label = {
                            Text(
                                "Home",
                                fontWeight = if (currentScreen == Screen.Home) androidx.compose.ui.text.font.FontWeight.Bold else androidx.compose.ui.text.font.FontWeight.Normal
                            )
                        },
                        modifier = Modifier.testTag("nav_home")
                    )
                    NavigationBarItem(
                        selected = currentScreen == Screen.Transactions,
                        onClick = { currentScreen = Screen.Transactions },
                        icon = {
                            Icon(
                                imageVector = if (currentScreen == Screen.Transactions) Icons.Filled.ReceiptLong else Icons.Outlined.ReceiptLong,
                                contentDescription = "Transactions"
                            )
                        },
                        label = {
                            Text(
                                "Transactions",
                                fontWeight = if (currentScreen == Screen.Transactions) androidx.compose.ui.text.font.FontWeight.Bold else androidx.compose.ui.text.font.FontWeight.Normal
                            )
                        },
                        modifier = Modifier.testTag("nav_transactions")
                    )
                    NavigationBarItem(
                        selected = currentScreen == Screen.Reports,
                        onClick = { currentScreen = Screen.Reports },
                        icon = {
                            Icon(
                                imageVector = if (currentScreen == Screen.Reports) Icons.Filled.Assessment else Icons.Outlined.Assessment,
                                contentDescription = "Reports"
                            )
                        },
                        label = {
                            Text(
                                "Reports",
                                fontWeight = if (currentScreen == Screen.Reports) androidx.compose.ui.text.font.FontWeight.Bold else androidx.compose.ui.text.font.FontWeight.Normal
                            )
                        },
                        modifier = Modifier.testTag("nav_reports")
                    )
                    NavigationBarItem(
                        selected = currentScreen == Screen.Settings,
                        onClick = { currentScreen = Screen.Settings },
                        icon = {
                            Icon(
                                imageVector = if (currentScreen == Screen.Settings) Icons.Filled.Settings else Icons.Outlined.Settings,
                                contentDescription = "Settings"
                            )
                        },
                        label = {
                            Text(
                                "Settings",
                                fontWeight = if (currentScreen == Screen.Settings) androidx.compose.ui.text.font.FontWeight.Bold else androidx.compose.ui.text.font.FontWeight.Normal
                            )
                        },
                        modifier = Modifier.testTag("nav_settings")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (currentScreen) {
                Screen.Home -> {
                    DashboardScreen(
                        viewModel = viewModel,
                        onNavigateToTransactions = { currentScreen = Screen.Transactions },
                        onNavigateToMembers = { currentScreen = Screen.Members },
                        onNavigateToVehicles = { currentScreen = Screen.Vehicles },
                        onNavigateToBudgets = { currentScreen = Screen.Budgets },
                        onNavigateToBills = { currentScreen = Screen.RecurringBills },
                        onOpenAddTransaction = { showAddSheet = true }
                    )
                }
                Screen.Transactions -> {
                    TransactionsScreen(
                        viewModel = viewModel,
                        onOpenAdd = { showAddSheet = true }
                    )
                }
                Screen.Reports -> {
                    ReportsScreen(viewModel = viewModel)
                }
                Screen.Settings -> {
                    SettingsScreen(
                        viewModel = viewModel,
                        onNavigateToMembers = { currentScreen = Screen.Members },
                        onNavigateToVehicles = { currentScreen = Screen.Vehicles },
                        onNavigateToBudgets = { currentScreen = Screen.Budgets },
                        onNavigateToBills = { currentScreen = Screen.RecurringBills },
                        onNavigateToSavings = { currentScreen = Screen.SavingsGoals }
                    )
                }
                Screen.Members -> {
                    MembersScreen(
                        viewModel = viewModel,
                        onBack = { currentScreen = Screen.Home }
                    )
                }
                Screen.Vehicles -> {
                    VehiclesScreen(
                        viewModel = viewModel,
                        onBack = { currentScreen = Screen.Home },
                        onAddVehicleExpense = { vehicleId ->
                            showAddSheet = true
                        }
                    )
                }
                Screen.Budgets -> {
                    BudgetsScreen(
                        viewModel = viewModel,
                        onBack = { currentScreen = Screen.Home }
                    )
                }
                Screen.SavingsGoals -> {
                    SavingsGoalsScreen(
                        viewModel = viewModel,
                        onBack = { currentScreen = Screen.Home }
                    )
                }
                Screen.RecurringBills -> {
                    RecurringBillsScreen(
                        viewModel = viewModel,
                        onBack = { currentScreen = Screen.Home }
                    )
                }
                else -> {
                    DashboardScreen(
                        viewModel = viewModel,
                        onNavigateToTransactions = { currentScreen = Screen.Transactions },
                        onNavigateToMembers = { currentScreen = Screen.Members },
                        onNavigateToVehicles = { currentScreen = Screen.Vehicles },
                        onNavigateToBudgets = { currentScreen = Screen.Budgets },
                        onNavigateToBills = { currentScreen = Screen.RecurringBills },
                        onOpenAddTransaction = { showAddSheet = true }
                    )
                }
            }
        }

        if (showAddSheet) {
            AddEditTransactionSheet(
                viewModel = viewModel,
                onDismiss = { showAddSheet = false }
            )
        }
    }
}
