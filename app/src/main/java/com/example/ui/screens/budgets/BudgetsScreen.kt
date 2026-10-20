package com.example.ui.screens.budgets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import com.example.data.local.entity.BudgetEntity
import com.example.domain.CurrencyFormatter
import com.example.ui.screens.transactions.DropdownSelector
import com.example.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetsScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val budgets by viewModel.allBudgets.collectAsState()
    val summary by viewModel.monthlySummary.collectAsState()
    val categories by viewModel.allCategories.collectAsState()
    val members by viewModel.allMembers.collectAsState()

    var editingBudget by remember { mutableStateOf<BudgetEntity?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Budgets & Limits", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Budget")
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
                    text = "Establish monthly family, category, or member limits. Transparent budget tracking helps prevent deficits before they occur.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (budgets.isEmpty()) {
                item {
                    Text(
                        text = "No budgets configured. Tap + to set a family or category budget limit.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                }
            } else {
                items(budgets, key = { it.id }) { budget ->
                    val spentPaise = when (budget.budgetType) {
                        "FAMILY" -> summary?.totalExpensePaise ?: 0L
                        "CATEGORY" -> summary?.categorySummaries?.firstOrNull { it.categoryId == budget.categoryId }?.totalExpensePaise ?: 0L
                        "MEMBER" -> summary?.memberSummaries?.firstOrNull { it.memberId == budget.memberId }?.responsibleForPaise ?: 0L
                        else -> 0L
                    }
                    val limit = budget.amountPaise
                    val pct = if (limit > 0) (spentPaise.toFloat() / limit.toFloat()).coerceAtLeast(0f) else 0f
                    val isOver = spentPaise > limit

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
                                Column {
                                    Text(
                                        text = budget.budgetName,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "${budget.budgetType} Budget • ${budget.period}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Row {
                                    IconButton(onClick = { editingBudget = budget }) {
                                        Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit")
                                    }
                                    IconButton(onClick = { viewModel.deleteBudget(budget) {} }) {
                                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            LinearProgressIndicator(
                                progress = { pct.coerceIn(0f, 1f) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp),
                                color = if (isOver) Color(0xFFDC2626) else if (pct > 0.8f) Color(0xFFF59E0B) else Color(0xFF10B981),
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Spent: ${CurrencyFormatter.formatPaise(spentPaise)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Limit: ${CurrencyFormatter.formatPaise(limit)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            if (isOver) {
                                Text(
                                    text = "Over budget by ${CurrencyFormatter.formatPaise(spentPaise - limit)}!",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFDC2626),
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        if (showAddDialog || editingBudget != null) {
            BudgetEditDialog(
                budget = editingBudget,
                categories = categories,
                members = members,
                onSave = { b ->
                    viewModel.saveBudget(b) {
                        showAddDialog = false
                        editingBudget = null
                    }
                },
                onDismiss = {
                    showAddDialog = false
                    editingBudget = null
                }
            )
        }
    }
}

@Composable
fun BudgetEditDialog(
    budget: BudgetEntity?,
    categories: List<com.example.data.local.entity.CategoryEntity>,
    members: List<com.example.data.local.entity.FamilyMemberEntity>,
    onSave: (BudgetEntity) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(budget?.budgetName ?: "") }
    var type by remember { mutableStateOf(budget?.budgetType ?: "FAMILY") }
    var amountText by remember { mutableStateOf(budget?.amountPaise?.let { (it / 100).toString() } ?: "") }
    var selectedCategoryId by remember { mutableStateOf(budget?.categoryId ?: categories.firstOrNull()?.id ?: "") }
    var selectedMemberId by remember { mutableStateOf(budget?.memberId ?: members.firstOrNull()?.id ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (budget != null) "Edit Budget" else "Create Budget") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Budget Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Budget Scope", style = MaterialTheme.typography.labelSmall)
                val typeOptions = listOf("FAMILY" to "Family Overall", "CATEGORY" to "Category-Specific", "MEMBER" to "Member Personal")
                DropdownSelector(
                    items = typeOptions,
                    selectedId = type,
                    onSelect = { type = it }
                )

                if (type == "CATEGORY") {
                    Text("Select Category", style = MaterialTheme.typography.labelSmall)
                    DropdownSelector(
                        items = categories.map { it.id to it.name },
                        selectedId = selectedCategoryId,
                        onSelect = { selectedCategoryId = it }
                    )
                } else if (type == "MEMBER") {
                    Text("Select Member", style = MaterialTheme.typography.labelSmall)
                    DropdownSelector(
                        items = members.map { it.id to it.name },
                        selectedId = selectedMemberId,
                        onSelect = { selectedMemberId = it }
                    )
                }

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Monthly Limit (₹)") },
                    prefix = { Text("₹ ") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val paise = CurrencyFormatter.parseToPaise(amountText)
                    if (name.isNotBlank() && paise != null && paise > 0) {
                        val b = budget?.copy(
                            budgetName = name.trim(),
                            budgetType = type,
                            amountPaise = paise,
                            categoryId = if (type == "CATEGORY") selectedCategoryId else null,
                            memberId = if (type == "MEMBER") selectedMemberId else null
                        ) ?: BudgetEntity(
                            budgetName = name.trim(),
                            budgetType = type,
                            amountPaise = paise,
                            categoryId = if (type == "CATEGORY") selectedCategoryId else null,
                            memberId = if (type == "MEMBER") selectedMemberId else null
                        )
                        onSave(b)
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
