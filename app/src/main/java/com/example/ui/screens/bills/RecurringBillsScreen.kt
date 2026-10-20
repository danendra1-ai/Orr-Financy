package com.example.ui.screens.bills

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.local.entity.RecurringBillEntity
import com.example.data.local.entity.RecurringBillOccurrenceEntity
import com.example.domain.CurrencyFormatter
import com.example.ui.screens.transactions.DropdownSelector
import com.example.ui.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecurringBillsScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bills by viewModel.allBills.collectAsState()
    val pendingOccurrences by viewModel.pendingBills.collectAsState()
    val accounts by viewModel.allAccounts.collectAsState()
    val members by viewModel.allMembers.collectAsState()
    val categories by viewModel.allCategories.collectAsState()

    var showAddBillDialog by remember { mutableStateOf(false) }
    var payingOccurrence by remember { mutableStateOf<Pair<RecurringBillOccurrenceEntity, RecurringBillEntity>?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Recurring Bills", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddBillDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Schedule")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Text(
                    text = "Manage schedules for mobile recharges, cooking gas cylinders, and school/college fees. Marking a bill as paid logs a single linked expense transaction.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (bills.isEmpty()) {
                item {
                    Text(
                        text = "No recurring bill schedules configured. Tap + to set up expected recurring payments.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                }
            } else {
                items(bills, key = { it.id }) { bill ->
                    val occurrence = pendingOccurrences.firstOrNull { it.recurringBillId == bill.id }
                    val dateStr = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(Date(bill.nextDueDate))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.NotificationsActive,
                                        contentDescription = null,
                                        tint = Color(0xFFD97706),
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = bill.name,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "${bill.frequency} • Due: $dateStr",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Text(
                                    text = CurrencyFormatter.formatPaise(bill.expectedAmountPaise),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                if (occurrence != null) {
                                    Button(
                                        onClick = { payingOccurrence = Pair(occurrence, bill) }
                                    ) {
                                        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Mark as Paid")
                                    }
                                } else {
                                    Text(
                                        text = "Paid for this cycle",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = Color(0xFF059669),
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(top = 8.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Add Recurring Bill Schedule Dialog
        if (showAddBillDialog) {
            AddBillDialog(
                categories = categories,
                members = members,
                onSave = { bill ->
                    viewModel.saveRecurringBill(bill) {
                        showAddBillDialog = false
                    }
                },
                onDismiss = { showAddBillDialog = false }
            )
        }

        // Pay Bill Dialog (creates linked transaction)
        if (payingOccurrence != null) {
            val (occ, b) = payingOccurrence!!
            var selectedAccId by remember { mutableStateOf(accounts.firstOrNull()?.id ?: "") }
            var selectedMemberId by remember { mutableStateOf(b.responsibleMemberId ?: members.firstOrNull()?.id ?: "") }
            var paymentMethod by remember { mutableStateOf("UPI") }

            AlertDialog(
                onDismissRequest = { payingOccurrence = null },
                title = { Text("Confirm Bill Payment") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "Record payment of ${CurrencyFormatter.formatPaise(occ.expectedAmountPaise)} for ${b.name}:",
                            style = MaterialTheme.typography.bodySmall
                        )

                        Text("Paid from Account", style = MaterialTheme.typography.labelSmall)
                        DropdownSelector(
                            items = accounts.map { it.id to it.name },
                            selectedId = selectedAccId,
                            onSelect = { selectedAccId = it }
                        )

                        Text("Paid by Member", style = MaterialTheme.typography.labelSmall)
                        DropdownSelector(
                            items = members.map { it.id to it.name },
                            selectedId = selectedMemberId,
                            onSelect = { selectedMemberId = it }
                        )

                        Text("Payment Method", style = MaterialTheme.typography.labelSmall)
                        val mOptions = listOf("UPI" to "UPI", "CASH" to "Cash", "BANK_TRANSFER" to "Bank Transfer", "CARD" to "Card")
                        DropdownSelector(
                            items = mOptions,
                            selectedId = paymentMethod,
                            onSelect = { paymentMethod = it }
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.payRecurringBill(
                                occurrence = occ,
                                bill = b,
                                accountId = selectedAccId,
                                paidByMemberId = selectedMemberId,
                                paymentMethod = paymentMethod
                            ) {
                                payingOccurrence = null
                            }
                        }
                    ) {
                        Text("Confirm & Record")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { payingOccurrence = null }) { Text("Cancel") }
                }
            )
        }
    }
}

@Composable
fun AddBillDialog(
    categories: List<com.example.data.local.entity.CategoryEntity>,
    members: List<com.example.data.local.entity.FamilyMemberEntity>,
    onSave: (RecurringBillEntity) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var frequency by remember { mutableStateOf("MONTHLY") }
    var selectedCategoryId by remember { mutableStateOf(categories.firstOrNull()?.id ?: "") }
    var selectedMemberId by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Recurring Bill") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Bill Name (e.g. Mobile Recharge, Cooking Gas)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Expected Amount (₹)") },
                    prefix = { Text("₹ ") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Category", style = MaterialTheme.typography.labelSmall)
                DropdownSelector(
                    items = categories.filter { it.categoryType == "EXPENSE" }.map { it.id to it.name },
                    selectedId = selectedCategoryId,
                    onSelect = { selectedCategoryId = it }
                )

                Text("Frequency", style = MaterialTheme.typography.labelSmall)
                val freqOptions = listOf("MONTHLY" to "Monthly", "QUARTERLY" to "Quarterly", "YEARLY" to "Yearly")
                DropdownSelector(
                    items = freqOptions,
                    selectedId = frequency,
                    onSelect = { frequency = it }
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val paise = CurrencyFormatter.parseToPaise(amountText)
                    if (name.isNotBlank() && paise != null && paise > 0) {
                        val bill = RecurringBillEntity(
                            name = name.trim(),
                            expectedAmountPaise = paise,
                            frequency = frequency,
                            nextDueDate = System.currentTimeMillis() + (7 * 24 * 60 * 60 * 1000L), // Due in 7 days
                            categoryId = selectedCategoryId,
                            responsibleMemberId = selectedMemberId
                        )
                        onSave(bill)
                    }
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
