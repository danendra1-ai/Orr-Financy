package com.example.ui.screens.transactions

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.local.entity.TransactionEntity
import com.example.domain.CurrencyFormatter
import com.example.ui.components.ConfirmDeleteDialog
import com.example.ui.viewmodel.MainViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditTransactionSheet(
    viewModel: MainViewModel,
    existingTransaction: TransactionEntity? = null,
    onDismiss: () -> Unit
) {
    val members by viewModel.allMembers.collectAsState()
    val accounts by viewModel.allAccounts.collectAsState()
    val categories by viewModel.allCategories.collectAsState()
    val vehicles by viewModel.allVehicles.collectAsState()

    var selectedTab by remember {
        mutableStateOf(
            when (existingTransaction?.transactionType) {
                "INCOME" -> 1
                else -> 0 // Expense default
            }
        )
    }

    // Common fields
    var amountText by remember {
        mutableStateOf(
            existingTransaction?.amountPaise?.let { (it / 100).toString() } ?: ""
        )
    }
    var notes by remember { mutableStateOf(existingTransaction?.notes ?: "") }
    var selectedAccountId by remember {
        mutableStateOf(
            existingTransaction?.accountId ?: accounts.firstOrNull()?.id ?: ""
        )
    }
    var selectedPaymentMethod by remember {
        mutableStateOf(existingTransaction?.paymentMethod ?: "UPI")
    }

    // Expense / Income specific
    var selectedCategoryId by remember {
        mutableStateOf(
            existingTransaction?.categoryId ?: categories.firstOrNull { it.categoryType == if (selectedTab == 1) "INCOME" else "EXPENSE" }?.id ?: ""
        )
    }
    var selectedPaidByMemberId by remember {
        mutableStateOf(
            existingTransaction?.paidByMemberId ?: members.firstOrNull()?.id ?: ""
        )
    }
    var selectedResponsibleMemberId by remember {
        mutableStateOf(existingTransaction?.responsibleMemberId) // null = Family-wide
    }
    var selectedVehicleId by remember {
        mutableStateOf(existingTransaction?.vehicleId)
    }
    var selectedVehicleExpenseType by remember {
        mutableStateOf(existingTransaction?.vehicleExpenseType ?: "PETROL")
    }

    // Transfer specific
    var transferSourceAccountId by remember {
        mutableStateOf(accounts.firstOrNull()?.id ?: "")
    }
    var transferDestAccountId by remember {
        mutableStateOf(accounts.getOrNull(1)?.id ?: "")
    }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var duplicateWarning by remember { mutableStateOf<String?>(null) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Check for potential duplicates when amount changes
    LaunchedEffect(amountText, selectedCategoryId) {
        val paise = CurrencyFormatter.parseToPaise(amountText)
        if (paise != null && paise > 0 && selectedCategoryId.isNotEmpty() && existingTransaction == null) {
            val dupes = viewModel.repository.checkPotentialDuplicates(
                amountPaise = paise,
                categoryId = selectedCategoryId,
                date = System.currentTimeMillis()
            )
            if (dupes.isNotEmpty()) {
                duplicateWarning = "A transaction with same amount (${CurrencyFormatter.formatPaise(paise)}) was recorded today. Tap Save again if this is intentional."
            } else {
                duplicateWarning = null
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (existingTransaction != null) "Edit Transaction" else "Add New Entry",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                }
            }

            if (existingTransaction == null) {
                Spacer(modifier = Modifier.height(10.dp))
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0; errorMessage = null },
                        text = { Text("Expense", fontWeight = FontWeight.SemiBold) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1; errorMessage = null },
                        text = { Text("Income", fontWeight = FontWeight.SemiBold) }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2; errorMessage = null },
                        text = { Text("Transfer", fontWeight = FontWeight.SemiBold) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Amount Field
            OutlinedTextField(
                value = amountText,
                onValueChange = { amountText = it; errorMessage = null },
                label = { Text("Amount in INR (₹)") },
                prefix = { Text("₹ ", fontWeight = FontWeight.Bold) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("amount_input")
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (selectedTab == 2) {
                // Internal Transfer Form
                Text("From Account", style = MaterialTheme.typography.labelMedium)
                DropdownSelector(
                    items = accounts.map { it.id to it.name },
                    selectedId = transferSourceAccountId,
                    onSelect = { transferSourceAccountId = it }
                )

                Spacer(modifier = Modifier.height(12.dp))
                Text("To Account", style = MaterialTheme.typography.labelMedium)
                DropdownSelector(
                    items = accounts.map { it.id to it.name },
                    selectedId = transferDestAccountId,
                    onSelect = { transferDestAccountId = it }
                )
            } else {
                // Category Selector
                val availableCategories = categories.filter {
                    it.categoryType == if (selectedTab == 1) "INCOME" else "EXPENSE"
                }
                Text("Category", style = MaterialTheme.typography.labelMedium)
                DropdownSelector(
                    items = availableCategories.map { it.id to it.name },
                    selectedId = selectedCategoryId.ifEmpty { availableCategories.firstOrNull()?.id ?: "" },
                    onSelect = { selectedCategoryId = it }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Paid By Member
                Text(
                    if (selectedTab == 1) "Earned / Received By" else "Paid By Member",
                    style = MaterialTheme.typography.labelMedium
                )
                DropdownSelector(
                    items = members.map { it.id to it.name },
                    selectedId = selectedPaidByMemberId.ifEmpty { members.firstOrNull()?.id ?: "" },
                    onSelect = { selectedPaidByMemberId = it }
                )

                if (selectedTab == 0) {
                    Spacer(modifier = Modifier.height(12.dp))
                    // Responsible Member / For Whom
                    Text("For Whom (Attribution)", style = MaterialTheme.typography.labelMedium)
                    val memberOptions = listOf("" to "Household / Family-Wide") + members.map { it.id to it.name }
                    DropdownSelector(
                        items = memberOptions,
                        selectedId = selectedResponsibleMemberId ?: "",
                        onSelect = { selectedResponsibleMemberId = it.ifEmpty { null } }
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    // Vehicle Reference (Optional)
                    Text("Linked Vehicle (Optional)", style = MaterialTheme.typography.labelMedium)
                    val vehicleOptions = listOf("" to "None") + vehicles.map { it.id to it.name }
                    DropdownSelector(
                        items = vehicleOptions,
                        selectedId = selectedVehicleId ?: "",
                        onSelect = { selectedVehicleId = it.ifEmpty { null } }
                    )

                    if (!selectedVehicleId.isNullOrEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("Vehicle Expense Type", style = MaterialTheme.typography.labelMedium)
                        val typeOptions = listOf(
                            "PETROL" to "Petrol",
                            "SERVICE_REPAIRS" to "Service & Repairs",
                            "INSURANCE" to "Insurance",
                            "PUC" to "PUC",
                            "OTHER" to "Other Maintenance"
                        )
                        DropdownSelector(
                            items = typeOptions,
                            selectedId = selectedVehicleExpenseType,
                            onSelect = { selectedVehicleExpenseType = it }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                // Account
                Text("Account / Wallet", style = MaterialTheme.typography.labelMedium)
                DropdownSelector(
                    items = accounts.map { it.id to it.name },
                    selectedId = selectedAccountId.ifEmpty { accounts.firstOrNull()?.id ?: "" },
                    onSelect = { selectedAccountId = it }
                )

                Spacer(modifier = Modifier.height(12.dp))
                // Payment Method
                Text("Payment Method", style = MaterialTheme.typography.labelMedium)
                val methodOptions = listOf("UPI" to "UPI", "CASH" to "Cash", "BANK_TRANSFER" to "Bank Transfer", "CARD" to "Debit / Credit Card")
                DropdownSelector(
                    items = methodOptions,
                    selectedId = selectedPaymentMethod,
                    onSelect = { selectedPaymentMethod = it }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes (Optional, e.g. Monthly Ration, Papa Bike Petrol)") },
                modifier = Modifier.fillMaxWidth()
            )

            // Warning or Error Box
            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            if (duplicateWarning != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF3C7)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = Color(0xFFD97706)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = duplicateWarning!!,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF92400E)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Save Button
            Button(
                onClick = {
                    val paise = CurrencyFormatter.parseToPaise(amountText)
                    if (paise == null || paise <= 0) {
                        errorMessage = "Please enter a valid amount greater than zero."
                        return@Button
                    }

                    if (selectedTab == 2) {
                        // Transfer
                        viewModel.executeTransfer(
                            sourceAccountId = transferSourceAccountId,
                            destinationAccountId = transferDestAccountId,
                            amountPaise = paise,
                            transferDate = System.currentTimeMillis(),
                            notes = notes.ifEmpty { "Internal Transfer" },
                            onSuccess = onDismiss,
                            onError = { errorMessage = it }
                        )
                    } else {
                        // Income or Expense
                        val txType = if (selectedTab == 1) "INCOME" else "EXPENSE"
                        val tx = existingTransaction?.copy(
                            amountPaise = paise,
                            categoryId = selectedCategoryId,
                            paidByMemberId = selectedPaidByMemberId,
                            responsibleMemberId = selectedResponsibleMemberId,
                            accountId = selectedAccountId,
                            paymentMethod = selectedPaymentMethod,
                            notes = notes,
                            vehicleId = selectedVehicleId,
                            vehicleExpenseType = if (!selectedVehicleId.isNullOrEmpty()) selectedVehicleExpenseType else null
                        ) ?: TransactionEntity(
                            transactionType = txType,
                            amountPaise = paise,
                            categoryId = selectedCategoryId,
                            transactionDate = System.currentTimeMillis(),
                            paidByMemberId = selectedPaidByMemberId,
                            responsibleMemberId = selectedResponsibleMemberId,
                            accountId = selectedAccountId,
                            paymentMethod = selectedPaymentMethod,
                            notes = notes,
                            vehicleId = selectedVehicleId,
                            vehicleExpenseType = if (!selectedVehicleId.isNullOrEmpty()) selectedVehicleExpenseType else null
                        )

                        if (existingTransaction != null) {
                            viewModel.updateTransaction(tx, onDismiss)
                        } else {
                            viewModel.addTransaction(tx, onDismiss, { errorMessage = it })
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("save_transaction_btn")
            ) {
                Text(if (existingTransaction != null) "Update Transaction" else "Save Entry", fontWeight = FontWeight.Bold)
            }

            if (existingTransaction != null) {
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedButton(
                    onClick = { showDeleteConfirm = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Delete Transaction")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        if (showDeleteConfirm && existingTransaction != null) {
            ConfirmDeleteDialog(
                title = "Delete Transaction?",
                message = "Are you sure you want to delete this ${CurrencyFormatter.formatPaise(existingTransaction.amountPaise)} record? Financial balances will be reversed.",
                onConfirm = {
                    showDeleteConfirm = false
                    viewModel.deleteTransaction(existingTransaction, onDismiss)
                },
                onDismiss = { showDeleteConfirm = false }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownSelector(
    items: List<Pair<String, String>>,
    selectedId: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedLabel = items.firstOrNull { it.first == selectedId }?.second ?: "Select..."

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = selectedLabel,
            onValueChange = {},
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            items.forEach { (id, label) ->
                DropdownMenuItem(
                    text = { Text(label) },
                    onClick = {
                        onSelect(id)
                        expanded = false
                    }
                )
            }
        }
    }
}
