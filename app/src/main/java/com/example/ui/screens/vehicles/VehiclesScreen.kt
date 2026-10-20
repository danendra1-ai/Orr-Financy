package com.example.ui.screens.vehicles

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.entity.VehicleEntity
import com.example.domain.CurrencyFormatter
import com.example.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VehiclesScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit,
    onAddVehicleExpense: (vehicleId: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val vehicles by viewModel.allVehicles.collectAsState()
    val summary by viewModel.monthlySummary.collectAsState()
    val monthName = viewModel.getMonthDisplayName()

    var editingVehicle by remember { mutableStateOf<VehicleEntity?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Two-Wheeler Expenses", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "Track separate costs for the two family bikes. All vehicle expenses are categorized views of existing transactions and counted exactly once in total family expenses.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            items(vehicles, key = { it.id }) { vehicle ->
                val vSummary = summary?.vehicleSummaries?.firstOrNull { it.vehicle.id == vehicle.id }
                val totalCost = vSummary?.totalMonthPaise ?: 0L

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DirectionsBike,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = vehicle.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "$monthName Total: ${CurrencyFormatter.formatPaise(totalCost)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }

                            IconButton(onClick = { editingVehicle = vehicle }) {
                                Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit Vehicle")
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Breakdown Items
                        VehicleCostRow("Petrol", vSummary?.petrolPaise ?: 0L)
                        VehicleCostRow("Service & Repairs", vSummary?.serviceRepairsPaise ?: 0L)
                        VehicleCostRow("Insurance", vSummary?.insurancePaise ?: 0L)
                        VehicleCostRow("PUC & Pollution", vSummary?.pucPaise ?: 0L)
                        VehicleCostRow("Other Maintenance", vSummary?.otherMaintenancePaise ?: 0L)

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = { onAddVehicleExpense(vehicle.id) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(imageVector = Icons.Default.LocalGasStation, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Log Expense for ${vehicle.name}")
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(30.dp))
            }
        }

        if (editingVehicle != null) {
            var vName by remember { mutableStateOf(editingVehicle!!.name) }
            AlertDialog(
                onDismissRequest = { editingVehicle = null },
                title = { Text("Edit Vehicle") },
                text = {
                    OutlinedTextField(
                        value = vName,
                        onValueChange = { vName = it },
                        label = { Text("Vehicle Name (e.g. Papa Bike, Family Bike)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            if (vName.isNotBlank()) {
                                viewModel.saveVehicle(editingVehicle!!.copy(name = vName.trim())) {
                                    editingVehicle = null
                                }
                            }
                        }
                    ) {
                        Text("Save")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { editingVehicle = null }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
fun VehicleCostRow(label: String, amountPaise: Long) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = CurrencyFormatter.formatPaise(amountPaise),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold
        )
    }
}
