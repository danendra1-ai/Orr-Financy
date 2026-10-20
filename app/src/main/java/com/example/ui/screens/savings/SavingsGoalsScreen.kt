package com.example.ui.screens.savings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
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
import com.example.data.local.entity.SavingsGoalEntity
import com.example.domain.CurrencyFormatter
import com.example.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavingsGoalsScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val goals by viewModel.allGoals.collectAsState()
    var editingGoal by remember { mutableStateOf<SavingsGoalEntity?>(null) }
    var allocatingGoal by remember { mutableStateOf<SavingsGoalEntity?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Savings Goals", fontWeight = FontWeight.Bold) },
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
                Icon(imageVector = Icons.Default.Add, contentDescription = "New Goal")
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
                    text = "Track household savings targets like Emergency Fund, Vehicle Repair, and Education. Virtual allocations track progress without double-counting account funds.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (goals.isEmpty()) {
                item {
                    Text(
                        text = "No savings goals created yet. Tap + to set up your family's Emergency Fund or savings targets.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                }
            } else {
                items(goals, key = { it.id }) { goal ->
                    val pct = if (goal.targetAmountPaise > 0) {
                        (goal.allocatedAmountPaise.toFloat() / goal.targetAmountPaise.toFloat()).coerceIn(0f, 1f)
                    } else 0f
                    val remaining = (goal.targetAmountPaise - goal.allocatedAmountPaise).coerceAtLeast(0L)

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
                                        imageVector = Icons.Default.Savings,
                                        contentDescription = null,
                                        tint = Color(0xFF10B981)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = goal.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Row {
                                    IconButton(onClick = { editingGoal = goal }) {
                                        Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit")
                                    }
                                    IconButton(onClick = { viewModel.deleteGoal(goal) {} }) {
                                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            LinearProgressIndicator(
                                progress = { pct },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp),
                                color = Color(0xFF10B981),
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "${CurrencyFormatter.formatPaise(goal.allocatedAmountPaise)} saved",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF059669)
                                )
                                Text(
                                    text = "Target: ${CurrencyFormatter.formatPaise(goal.targetAmountPaise)} (${String.format("%.1f", pct * 100)}%)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            if (remaining > 0) {
                                Text(
                                    text = "Remaining: ${CurrencyFormatter.formatPaise(remaining)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Button(
                                onClick = { allocatingGoal = goal },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Update Allocation")
                            }
                        }
                    }
                }
            }
        }

        // Add / Edit Goal Dialog
        if (showAddDialog || editingGoal != null) {
            var name by remember { mutableStateOf(editingGoal?.name ?: "") }
            var targetText by remember { mutableStateOf(editingGoal?.targetAmountPaise?.let { (it / 100).toString() } ?: "") }
            AlertDialog(
                onDismissRequest = {
                    showAddDialog = false
                    editingGoal = null
                },
                title = { Text(if (editingGoal != null) "Edit Savings Goal" else "Create Savings Goal") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Goal Name (e.g. Emergency Fund, Bike Repair)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = targetText,
                            onValueChange = { targetText = it },
                            label = { Text("Target Amount (₹)") },
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
                            val paise = CurrencyFormatter.parseToPaise(targetText)
                            if (name.isNotBlank() && paise != null && paise > 0) {
                                val g = editingGoal?.copy(name = name.trim(), targetAmountPaise = paise)
                                    ?: SavingsGoalEntity(name = name.trim(), targetAmountPaise = paise)
                                viewModel.saveGoal(g) {
                                    showAddDialog = false
                                    editingGoal = null
                                }
                            }
                        }
                    ) {
                        Text("Save")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddDialog = false; editingGoal = null }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Update Allocation Dialog
        if (allocatingGoal != null) {
            var allocText by remember { mutableStateOf((allocatingGoal!!.allocatedAmountPaise / 100).toString()) }
            AlertDialog(
                onDismissRequest = { allocatingGoal = null },
                title = { Text("Update Allocated Savings") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Set current allocated savings towards ${allocatingGoal!!.name}:",
                            style = MaterialTheme.typography.bodySmall
                        )
                        OutlinedTextField(
                            value = allocText,
                            onValueChange = { allocText = it },
                            label = { Text("Allocated Amount (₹)") },
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
                            val paise = CurrencyFormatter.parseToPaise(allocText) ?: 0L
                            viewModel.updateGoalAllocation(allocatingGoal!!, paise)
                            allocatingGoal = null
                        }
                    ) {
                        Text("Update")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { allocatingGoal = null }) { Text("Cancel") }
                }
            )
        }
    }
}
