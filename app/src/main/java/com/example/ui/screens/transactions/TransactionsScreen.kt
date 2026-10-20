package com.example.ui.screens.transactions

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
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SyncAlt
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.ui.components.CurrencyText
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.FamilyMemberAvatar
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.RoseExpense
import com.example.ui.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsScreen(
    viewModel: MainViewModel,
    onOpenAdd: () -> Unit,
    modifier: Modifier = Modifier
) {
    val allTransactions by viewModel.allTransactions.collectAsState()
    val allTransfers by viewModel.allTransfers.collectAsState()
    val members by viewModel.allMembers.collectAsState()
    val categories by viewModel.allCategories.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedTypeFilter by remember { mutableStateOf<String?>("ALL") }
    var selectedMemberId by remember { mutableStateOf<String?>(null) }
    var editingTransaction by remember { mutableStateOf<TransactionEntity?>(null) }

    // Filter logic
    val filteredTransactions = remember(allTransactions, searchQuery, selectedTypeFilter, selectedMemberId) {
        allTransactions.filter { tx ->
            val matchesType = when (selectedTypeFilter) {
                "EXPENSE" -> tx.transactionType == "EXPENSE"
                "INCOME" -> tx.transactionType == "INCOME"
                else -> true
            }
            val matchesMember = if (selectedMemberId == null) true
            else tx.paidByMemberId == selectedMemberId || tx.responsibleMemberId == selectedMemberId

            val matchesSearch = if (searchQuery.isEmpty()) true
            else {
                (tx.notes?.contains(searchQuery, ignoreCase = true) == true) ||
                (tx.amountPaise.toString().contains(searchQuery))
            }

            matchesType && matchesMember && matchesSearch
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onOpenAdd,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape,
                modifier = Modifier
                    .shadow(6.dp, CircleShape)
                    .testTag("fab_add_tx_list")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Transaction")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // Search Bar with crisp fintech styling
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search by notes, amount, category...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("search_input"),
                shape = RoundedCornerShape(18.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Filter Chips Carousel
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    FilterChip(
                        selected = selectedTypeFilter == "ALL",
                        onClick = { selectedTypeFilter = "ALL" },
                        label = { Text("All Records") },
                        shape = RoundedCornerShape(12.dp)
                    )
                }
                item {
                    FilterChip(
                        selected = selectedTypeFilter == "EXPENSE",
                        onClick = { selectedTypeFilter = "EXPENSE" },
                        label = { Text("Expenses") },
                        shape = RoundedCornerShape(12.dp)
                    )
                }
                item {
                    FilterChip(
                        selected = selectedTypeFilter == "INCOME",
                        onClick = { selectedTypeFilter = "INCOME" },
                        label = { Text("Income") },
                        shape = RoundedCornerShape(12.dp)
                    )
                }
                item {
                    FilterChip(
                        selected = selectedTypeFilter == "TRANSFER",
                        onClick = { selectedTypeFilter = "TRANSFER" },
                        label = { Text("Transfers") },
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                items(members) { m ->
                    FilterChip(
                        selected = selectedMemberId == m.id,
                        onClick = {
                            selectedMemberId = if (selectedMemberId == m.id) null else m.id
                        },
                        label = { Text(m.name) },
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (selectedTypeFilter == "TRANSFER") {
                // Transfers List
                if (allTransfers.isEmpty()) {
                    EmptyStateCard(
                        title = "No Internal Transfers",
                        message = "Internal transfers move funds between cash and bank without altering net household income.",
                        actionLabel = "Record Transfer",
                        onAction = onOpenAdd
                    )
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(allTransfers, key = { it.id }) { tr ->
                            Surface(
                                shape = RoundedCornerShape(18.dp),
                                color = MaterialTheme.colorScheme.surface,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(44.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFFEFF6FF)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.SyncAlt,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(22.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = tr.notes ?: "Account Transfer",
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold
                                            )
                                            val dateStr = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(tr.transferDate))
                                            Text(
                                                text = dateStr,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                    CurrencyText(
                                        amountPaise = tr.amountPaise,
                                        fontSize = 16.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                // Transactions List
                if (filteredTransactions.isEmpty()) {
                    EmptyStateCard(
                        title = "No Matching Transactions",
                        message = "No transactions found matching your filters. Try adjusting your search query.",
                        actionLabel = "New Transaction",
                        onAction = onOpenAdd
                    )
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(filteredTransactions, key = { it.id }) { tx ->
                            val memberName = members.firstOrNull { it.id == tx.paidByMemberId }?.name ?: "Family"
                            val catName = categories.firstOrNull { it.id == tx.categoryId }?.name ?: "General"
                            TransactionCardItem(
                                transaction = tx,
                                memberName = memberName,
                                categoryName = catName,
                                onEdit = { editingTransaction = tx }
                            )
                        }
                        item {
                            Spacer(modifier = Modifier.height(75.dp))
                        }
                    }
                }
            }
        }

        if (editingTransaction != null) {
            AddEditTransactionSheet(
                viewModel = viewModel,
                existingTransaction = editingTransaction,
                onDismiss = { editingTransaction = null }
            )
        }
    }
}

@Composable
fun TransactionCardItem(
    transaction: TransactionEntity,
    memberName: String,
    categoryName: String,
    onEdit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isIncome = transaction.transactionType == "INCOME"
    val dateStr = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(transaction.transactionDate))

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onEdit),
        shape = RoundedCornerShape(18.dp),
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
                        .size(46.dp)
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
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = transaction.notes?.ifEmpty { categoryName } ?: categoryName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "$categoryName • Paid by $memberName",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "$dateStr • ${transaction.paymentMethod}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }

            CurrencyText(
                amountPaise = transaction.amountPaise,
                fontSize = 16.sp,
                showSign = true,
                isIncome = isIncome
            )
        }
    }
}
