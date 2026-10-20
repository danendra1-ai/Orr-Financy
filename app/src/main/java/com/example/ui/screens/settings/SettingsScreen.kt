package com.example.ui.screens.settings

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.data.local.entity.FinancialAccountEntity
import com.example.domain.CurrencyFormatter
import com.example.ui.components.ConfirmDeleteDialog
import com.example.ui.viewmodel.MainViewModel
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    onNavigateToMembers: () -> Unit,
    onNavigateToVehicles: () -> Unit,
    onNavigateToBudgets: () -> Unit,
    onNavigateToBills: () -> Unit,
    onNavigateToSavings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val accounts by viewModel.allAccounts.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()

    var showPinDialog by remember { mutableStateOf(false) }
    var showBackupDialog by remember { mutableStateOf(false) }
    var showRestoreDialog by remember { mutableStateOf(false) }
    var showResetConfirm by remember { mutableStateOf(false) }
    var showAddAccountDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Settings & Configuration",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        // Navigation Shortcuts to Management Screens
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column {
                SettingsNavigationRow("Family Member Profiles", onNavigateToMembers)
                SettingsNavigationRow("Two-Wheeler Vehicles", onNavigateToVehicles)
                SettingsNavigationRow("Monthly Budgets & Limits", onNavigateToBudgets)
                SettingsNavigationRow("Savings Goals & Emergency Fund", onNavigateToSavings)
                SettingsNavigationRow("Recurring Bill Schedules", onNavigateToBills)
            }
        }

        // Security & App Lock
        Text(
            text = "Security & Privacy",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Card(
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
                        Text("App PIN Lock", fontWeight = FontWeight.SemiBold)
                        Text(
                            text = if (viewModel.securityManager.isPinEnabled) "PIN protection enabled" else "No PIN set",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = viewModel.securityManager.isPinEnabled,
                        onCheckedChange = { enable ->
                            if (enable) {
                                showPinDialog = true
                            } else {
                                viewModel.securityManager.disablePin()
                            }
                        },
                        modifier = Modifier.testTag("pin_switch")
                    )
                }

                if (viewModel.securityManager.isPinEnabled) {
                    Spacer(modifier = Modifier.height(10.dp))
                    TextButton(onClick = { showPinDialog = true }) {
                        Text("Change PIN")
                    }
                }
            }
        }

        // Theme Setting
        Text(
            text = "Appearance",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Theme Mode", fontWeight = FontWeight.SemiBold)
                    Text(
                        text = "Current: $themeMode",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(
                        onClick = { viewModel.setThemeMode("LIGHT") },
                        enabled = themeMode != "LIGHT"
                    ) { Text("Light") }
                    TextButton(
                        onClick = { viewModel.setThemeMode("DARK") },
                        enabled = themeMode != "DARK"
                    ) { Text("Dark") }
                    TextButton(
                        onClick = { viewModel.setThemeMode("SYSTEM") },
                        enabled = themeMode != "SYSTEM"
                    ) { Text("Auto") }
                }
            }
        }

        // Tracked Accounts
        Text(
            text = "Tracked Accounts",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                accounts.forEach { acc ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(acc.name, fontWeight = FontWeight.Medium)
                        Text(
                            text = "Opening: ${CurrencyFormatter.formatPaise(acc.openingBalancePaise)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = { showAddAccountDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Add Account / Wallet")
                }
            }
        }

        // Backup and Restore
        Text(
            text = "Backup & Data",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Export a full structured JSON backup with SHA-256 integrity verification. All data stays strictly on your device.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                val json = viewModel.backupManager.createBackupJson()
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("Orr Expency Backup", json))
                                Toast.makeText(context, "Backup copied to clipboard! Save it safely.", Toast.LENGTH_LONG).show()
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(imageVector = Icons.Default.Backup, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Export JSON")
                    }

                    Button(
                        onClick = { showRestoreDialog = true },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Restore JSON")
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                TextButton(
                    onClick = { showResetConfirm = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Reset Database to Default", color = MaterialTheme.colorScheme.error)
                }
            }
        }

        Spacer(modifier = Modifier.height(40.dp))
    }

    // Set PIN Dialog
    if (showPinDialog) {
        var pinInput by remember { mutableStateOf("") }
        var pinConfirm by remember { mutableStateOf("") }
        var pinError by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { showPinDialog = false },
            title = { Text("Set App PIN") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Enter a 4 to 6 digit security PIN for Orr Expency:")
                    OutlinedTextField(
                        value = pinInput,
                        onValueChange = { if (it.length <= 6) pinInput = it },
                        label = { Text("New PIN") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = pinConfirm,
                        onValueChange = { if (it.length <= 6) pinConfirm = it },
                        label = { Text("Confirm PIN") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (pinError != null) {
                        Text(pinError!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (pinInput.length < 4) {
                            pinError = "PIN must be at least 4 digits."
                        } else if (pinInput != pinConfirm) {
                            pinError = "PINs do not match."
                        } else {
                            viewModel.securityManager.setPin(pinInput)
                            showPinDialog = false
                            Toast.makeText(context, "App PIN enabled successfully.", Toast.LENGTH_SHORT).show()
                        }
                    }
                ) {
                    Text("Save PIN")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPinDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Restore Dialog
    if (showRestoreDialog) {
        var restoreText by remember { mutableStateOf("") }
        var restoreError by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { showRestoreDialog = false },
            title = { Text("Restore From JSON") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Paste your exported Orr Expency JSON backup below. Current records will be replaced atomically after checksum verification:")
                    OutlinedTextField(
                        value = restoreText,
                        onValueChange = { restoreText = it; restoreError = null },
                        label = { Text("Paste JSON Backup") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                    )
                    if (restoreError != null) {
                        Text(restoreError!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        coroutineScope.launch {
                            val res = viewModel.backupManager.restoreBackup(restoreText)
                            if (res.isSuccess) {
                                showRestoreDialog = false
                                Toast.makeText(context, "Backup restored successfully!", Toast.LENGTH_LONG).show()
                            } else {
                                restoreError = res.exceptionOrNull()?.message ?: "Invalid backup"
                            }
                        }
                    }
                ) {
                    Text("Verify & Restore")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRestoreDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Add Account Dialog
    if (showAddAccountDialog) {
        var accName by remember { mutableStateOf("") }
        var openingBalText by remember { mutableStateOf("0") }
        var accType by remember { mutableStateOf("BANK") }

        AlertDialog(
            onDismissRequest = { showAddAccountDialog = false },
            title = { Text("Add Tracked Account") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = accName,
                        onValueChange = { accName = it },
                        label = { Text("Account Name (e.g. HDFC Bank, Post Office)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = openingBalText,
                        onValueChange = { openingBalText = it },
                        label = { Text("Opening Balance (₹)") },
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
                        val paise = CurrencyFormatter.parseToPaise(openingBalText) ?: 0L
                        if (accName.isNotBlank()) {
                            viewModel.saveAccount(
                                FinancialAccountEntity(
                                    name = accName.trim(),
                                    accountType = accType,
                                    openingBalancePaise = paise
                                )
                            ) {
                                showAddAccountDialog = false
                            }
                        }
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddAccountDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Reset Confirm Dialog
    if (showResetConfirm) {
        ConfirmDeleteDialog(
            title = "Reset All Financial Records?",
            message = "This will erase all recorded transactions, accounts, and budgets, restoring initial empty setup. This action cannot be undone.",
            onConfirm = {
                viewModel.resetData {
                    showResetConfirm = false
                    Toast.makeText(context, "Data reset to default.", Toast.LENGTH_SHORT).show()
                }
            },
            onDismiss = { showResetConfirm = false }
        )
    }
}

@Composable
fun SettingsNavigationRow(title: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, fontWeight = FontWeight.Medium)
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
