package com.example.ui.screens.dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.TransactionEntity
import com.example.domain.CurrencyFormatter
import com.example.domain.HealthStatus
import com.example.ui.components.CurrencyText
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.FamilyMemberAvatar
import com.example.ui.components.HealthStatusBadge
import com.example.ui.components.HeroVaultCard
import com.example.ui.components.IncomeExpenseBarChart
import com.example.ui.components.MetricCard
import com.example.ui.components.MonthSelector
import com.example.ui.components.SpendingDonutChart
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.RoseExpense
import com.example.ui.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
    viewModel: MainViewModel,
    onNavigateToTransactions: () -> Unit,
    onNavigateToMembers: () -> Unit,
    onNavigateToVehicles: () -> Unit,
    onNavigateToBudgets: () -> Unit,
    onNavigateToBills: () -> Unit,
    onOpenAddTransaction: () -> Unit,
    modifier: Modifier = Modifier
) {
    val summary by viewModel.monthlySummary.collectAsState()
    val health by viewModel.healthReport.collectAsState()
    val pendingBills by viewModel.pendingBills.collectAsState()
    val allTransactions by viewModel.allTransactions.collectAsState()
    val accountBalances by viewModel.accountBalances.collectAsState()
    val monthName = viewModel.getMonthDisplayName()

    val cashBal = accountBalances.firstOrNull { it.account.accountType == "CASH" }?.currentBalancePaise ?: 0L
    val bankBal = accountBalances.filter { it.account.accountType == "BANK" }.sumOf { it.currentBalancePaise }
    val totalAvailable = summary?.availableBalancePaise ?: (cashBal + bankBal)

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onOpenAddTransaction,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape,
                modifier = Modifier
                    .shadow(6.dp, CircleShape)
                    .testTag("fab_add_transaction")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Transaction")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header with App Identity
            item {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Orr Expency",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Offline Family Finance • INR",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Local Vault",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            // Month Selector
            item {
                MonthSelector(
                    monthDisplayName = monthName,
                    onPrevMonth = { viewModel.changeMonth(-1) },
                    onNextMonth = { viewModel.changeMonth(1) }
                )
            }

            // Master Hero Vault Card
            item {
                HeroVaultCard(
                    availableBalancePaise = totalAvailable,
                    cashBalancePaise = cashBal,
                    bankBalancePaise = bankBal,
                    onAddIncome = onOpenAddTransaction,
                    onAddExpense = onOpenAddTransaction,
                    onTransfer = onOpenAddTransaction
                )
            }

            // Key Financial Metrics Grid
            item {
                val sum = summary
                val inc = sum?.totalIncomePaise ?: 0L
                val exp = sum?.totalExpensePaise ?: 0L
                val net = sum?.netCashFlowPaise ?: 0L
                val savingsRate = sum?.savingsRatePercentage

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        MetricCard(
                            title = "Total Income",
                            amountPaise = inc,
                            icon = Icons.Default.ArrowDownward,
                            iconColor = EmeraldSuccess,
                            modifier = Modifier.weight(1f)
                        )
                        MetricCard(
                            title = "Total Expenses",
                            amountPaise = exp,
                            icon = Icons.Default.ArrowUpward,
                            iconColor = RoseExpense,
                            modifier = Modifier.weight(1f),
                            isPositiveGood = false
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        MetricCard(
                            title = if (net >= 0) "Monthly Surplus" else "Monthly Deficit",
                            amountPaise = net,
                            icon = Icons.Default.Savings,
                            iconColor = if (net >= 0) EmeraldSuccess else RoseExpense,
                            subtitle = savingsRate?.let { "Savings Rate: ${String.format("%.1f", it)}%" } ?: "Savings Rate: N/A",
                            modifier = Modifier.weight(1f)
                        )
                        MetricCard(
                            title = "Ration & Grocery",
                            amountPaise = sum?.rationExpensePaise ?: 0L,
                            icon = Icons.Default.ShoppingCart,
                            iconColor = Color(0xFFF59E0B),
                            subtitle = "Household essentials",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Financial Health Diagnosis Card
            item {
                val h = health
                if (h != null) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(2.dp, RoundedCornerShape(22.dp))
                            .testTag("financial_health_card"),
                        shape = RoundedCornerShape(22.dp),
                        color = when (h.status) {
                            HealthStatus.GREEN -> Color(0xFFF0FDF4)
                            HealthStatus.YELLOW -> Color(0xFFFFFBEB)
                            HealthStatus.RED -> Color(0xFFFFF1F2)
                        },
                        border = BorderStroke(
                            1.dp,
                            when (h.status) {
                                HealthStatus.GREEN -> Color(0xFF86EFAC)
                                HealthStatus.YELLOW -> Color(0xFFFDE68A)
                                HealthStatus.RED -> Color(0xFFFECDD3)
                            }
                        )
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Financial Health Diagnosis",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0F172A)
                                )
                                HealthStatusBadge(status = h.status)
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            h.reasons.forEach { reason ->
                                Row(
                                    modifier = Modifier.padding(vertical = 3.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Text(
                                        text = "•",
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF475569),
                                        modifier = Modifier.padding(end = 8.dp)
                                    )
                                    Text(
                                        text = reason,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFF334155),
                                        lineHeight = 18.sp
                                    )
                                }
                            }

                            if (h.practicalSuggestions.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color.White.copy(alpha = 0.8f),
                                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "Recommendation: ${h.practicalSuggestions.first()}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFF0F766E),
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.padding(10.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Family Member Attribution Carousel / Row
            item {
                val sum = summary
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(2.dp, RoundedCornerShape(22.dp)),
                    shape = RoundedCornerShape(22.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f))
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Group,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Family Contributors",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            TextButton(onClick = onNavigateToMembers) {
                                Text("Profiles", fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        if (sum?.memberSummaries.isNullOrEmpty()) {
                            Text(
                                text = "No member details recorded.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            sum!!.memberSummaries.forEach { member ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        FamilyMemberAvatar(name = member.memberName)
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = member.memberName,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "Paid: ${CurrencyFormatter.formatPaise(member.paidByPaise)} • For: ${CurrencyFormatter.formatPaise(member.responsibleForPaise)}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    if (member.earnedIncomePaise > 0) {
                                        CurrencyText(
                                            amountPaise = member.earnedIncomePaise,
                                            fontSize = 13.sp,
                                            color = EmeraldSuccess,
                                            showSign = true,
                                            isIncome = true,
                                            showDecimals = false
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Charts Section
            item {
                val sum = summary
                if (sum != null) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        IncomeExpenseBarChart(
                            incomePaise = sum.totalIncomePaise,
                            expensePaise = sum.totalExpensePaise
                        )
                        SpendingDonutChart(categories = sum.categorySummaries)
                    }
                }
            }

            // Two-Wheeler Vehicle Costs
            item {
                val sum = summary
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(2.dp, RoundedCornerShape(22.dp)),
                    shape = RoundedCornerShape(22.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f))
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.DirectionsBike,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Two-Wheeler Costs",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            TextButton(onClick = onNavigateToVehicles) {
                                Text("Details", fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        if (sum?.vehicleSummaries.isNullOrEmpty()) {
                            Text(
                                text = "No vehicle expenses recorded this month.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            sum!!.vehicleSummaries.forEach { veh ->
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Text(
                                                text = veh.vehicle.name,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "Petrol: ${CurrencyFormatter.formatPaise(veh.petrolPaise)} | Service: ${CurrencyFormatter.formatPaise(veh.serviceRepairsPaise)}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        CurrencyText(
                                            amountPaise = veh.totalMonthPaise,
                                            fontSize = 15.sp,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Upcoming Recurring Bills Alert
            if (pendingBills.isNotEmpty()) {
                item {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(2.dp, RoundedCornerShape(22.dp)),
                        shape = RoundedCornerShape(22.dp),
                        color = Color(0xFFFEF3C7),
                        border = BorderStroke(1.dp, Color(0xFFFDE68A))
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.NotificationsActive,
                                        contentDescription = null,
                                        tint = Color(0xFFD97706)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Upcoming Recurring Bills",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF78350F)
                                    )
                                }
                                TextButton(onClick = onNavigateToBills) {
                                    Text("Pay Bills", color = Color(0xFF92400E), fontWeight = FontWeight.Bold)
                                }
                            }

                            pendingBills.take(2).forEach { bill ->
                                val dateStr = SimpleDateFormat("dd MMM", Locale.getDefault()).format(Date(bill.dueDate))
                                Text(
                                    text = "• Due $dateStr: ${CurrencyFormatter.formatPaise(bill.expectedAmountPaise)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF92400E),
                                    modifier = Modifier.padding(vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Recent Transactions List
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent Transactions",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    TextButton(onClick = onNavigateToTransactions) {
                        Text("View All", fontWeight = FontWeight.Bold)
                    }
                }
            }

            val recentTxs = allTransactions.take(5)
            if (recentTxs.isEmpty()) {
                item {
                    EmptyStateCard(
                        title = "No Transactions Recorded",
                        message = "Start logging family income and expenses using the + button below.",
                        actionLabel = "Add First Transaction",
                        onAction = onOpenAddTransaction
                    )
                }
            } else {
                items(recentTxs, key = { it.id }) { tx ->
                    BespokeTransactionRow(
                        transaction = tx,
                        onClick = onNavigateToTransactions
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(70.dp))
            }
        }
    }
}

@Composable
fun BespokeTransactionRow(
    transaction: TransactionEntity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isIncome = transaction.transactionType == "INCOME"
    val dateStr = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(transaction.transactionDate))

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 3.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(
                            if (isIncome) Color(0xFFD1FAE5) else Color(0xFFFFE4E6)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isIncome) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                        contentDescription = null,
                        tint = if (isIncome) EmeraldSuccess else RoseExpense,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = transaction.notes?.ifEmpty { if (isIncome) "Income" else "Expense" } ?: if (isIncome) "Income" else "Expense",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                    Text(
                        text = "$dateStr • ${transaction.paymentMethod}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            CurrencyText(
                amountPaise = transaction.amountPaise,
                fontSize = 15.sp,
                showSign = true,
                isIncome = isIncome
            )
        }
    }
}
