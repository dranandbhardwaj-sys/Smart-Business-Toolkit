package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.QrCode2
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.local.BusinessProfileEntity
import com.example.data.local.CustomerEntity
import com.example.data.local.InvoiceEntity
import com.example.data.local.PaymentEntity
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.MiniUpiQrCode
import com.example.ui.components.StatusPill
import com.example.ui.components.StatusTone
import com.example.ui.theme.LocalSaaSColors
import com.example.ui.theme.MonoFontFamily
import com.example.util.formatDateTime
import com.example.util.formatInr

@Composable
fun PaymentsScreen(
    payments: List<PaymentEntity>,
    customers: List<CustomerEntity>,
    invoices: List<InvoiceEntity>,
    profile: BusinessProfileEntity,
    onRecordPayment: (
        customer: CustomerEntity?,
        invoice: InvoiceEntity?,
        amount: Double,
        method: String,
        transactionId: String,
        notes: String,
    ) -> Unit,
) {
    val semantic = LocalSaaSColors.current
    var selectedMethod by remember { mutableStateOf("ALL") }
    var showAddDialog by remember { mutableStateOf(false) }

    val filtered = remember(payments, selectedMethod) {
        if (selectedMethod == "ALL") payments else payments.filter { it.method == selectedMethod }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text("Payments & UPI Records", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text("Collected: ${formatInr(payments.sumOf { it.amount })}", style = MaterialTheme.typography.bodySmall, color = semantic.success)
                }
                Button(
                    onClick = { showAddDialog = true },
                    modifier = Modifier.testTag("payments_add_btn"),
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Add Payment")
                }
            }
        }

        // Merchant UPI & QR Collector Card
        item {
            Card(
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    MiniUpiQrCode(modifier = Modifier.size(64.dp))
                    Spacer(Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.QrCode2, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Merchant UPI & BharatQR", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(profile.upiId, style = MaterialTheme.typography.bodyMedium, fontFamily = MonoFontFamily, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Text("Show QR at counter and log UTR/Transaction IDs below.", style = MaterialTheme.typography.bodySmall, color = semantic.textMuted)
                    }
                }
            }
        }

        // Category Filter Chips
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(listOf("ALL", "UPI", "QR", "CASH", "BANK")) { method ->
                    val count = if (method == "ALL") payments.size else payments.count { it.method == method }
                    FilterChip(
                        selected = selectedMethod == method,
                        onClick = { selectedMethod = method },
                        label = { Text("$method ($count)") },
                    )
                }
            }
        }

        if (filtered.isEmpty()) {
            item {
                EmptyStateCard(
                    title = "No $selectedMethod payments yet",
                    subtitle = "Record incoming UPI, QR, Cash, or Bank settlements.",
                    actionLabel = "Add Payment Record",
                    onAction = { showAddDialog = true },
                )
            }
        } else {
            items(filtered, key = { it.id }) { pay ->
                Card(
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, semantic.cardBorder),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(pay.customerName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                StatusPill(text = pay.method, tone = StatusTone.INFO)
                            }
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "Txn: ${pay.transactionId}" + if (pay.invoiceNumber.isNotBlank()) " • ${pay.invoiceNumber}" else "",
                                style = MaterialTheme.typography.labelSmall,
                                fontFamily = MonoFontFamily,
                                color = semantic.textMuted,
                            )
                            Text(formatDateTime(pay.dateMillis), style = MaterialTheme.typography.labelSmall, color = semantic.textMuted)
                            if (pay.notes.isNotBlank()) {
                                Text(pay.notes, style = MaterialTheme.typography.bodySmall, color = semantic.textMuted)
                            }
                        }
                        Text(
                            text = "+ ${formatInr(pay.amount)}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = semantic.success,
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddPaymentDialog(
            customers = customers,
            invoices = invoices,
            onSave = { cust, inv, amt, method, txnId, notes ->
                onRecordPayment(cust, inv, amt, method, txnId, notes)
                showAddDialog = false
            },
            onDismiss = { showAddDialog = false },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddPaymentDialog(
    customers: List<CustomerEntity>,
    invoices: List<InvoiceEntity>,
    onSave: (CustomerEntity?, InvoiceEntity?, Double, String, String, String) -> Unit,
    onDismiss: () -> Unit,
) {
    var selectedCustomer by remember { mutableStateOf(customers.firstOrNull()) }
    var custExpanded by remember { mutableStateOf(false) }
    var selectedInvoice by remember { mutableStateOf<InvoiceEntity?>(null) }
    var invExpanded by remember { mutableStateOf(false) }
    var amountText by remember { mutableStateOf("") }
    var method by remember { mutableStateOf("UPI") }
    var txnId by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Payment Record", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                ExposedDropdownMenuBox(expanded = custExpanded, onExpandedChange = { custExpanded = !custExpanded }) {
                    OutlinedTextField(
                        value = selectedCustomer?.name ?: "Walk-in Customer",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Customer") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = custExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                    )
                    ExposedDropdownMenu(expanded = custExpanded, onDismissRequest = { custExpanded = false }) {
                        customers.forEach { c ->
                            DropdownMenuItem(text = { Text(c.name) }, onClick = { selectedCustomer = c; custExpanded = false })
                        }
                    }
                }

                ExposedDropdownMenuBox(expanded = invExpanded, onExpandedChange = { invExpanded = !invExpanded }) {
                    OutlinedTextField(
                        value = selectedInvoice?.invoiceNumber ?: "General Credit / No Invoice",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Link Invoice (Optional)") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = invExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                    )
                    ExposedDropdownMenu(expanded = invExpanded, onDismissRequest = { invExpanded = false }) {
                        DropdownMenuItem(text = { Text("None") }, onClick = { selectedInvoice = null; invExpanded = false })
                        invoices.filter { it.status != "PAID" }.forEach { inv ->
                            DropdownMenuItem(
                                text = { Text("${inv.invoiceNumber} (${formatInr(inv.totalAmount - inv.paidAmount)} due)") },
                                onClick = {
                                    selectedInvoice = inv
                                    if (amountText.isBlank()) amountText = (inv.totalAmount - inv.paidAmount).toInt().toString()
                                    invExpanded = false
                                },
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it; error = null },
                    label = { Text("Amount Received (₹)*") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Text("Payment Category", style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("UPI", "QR", "CASH", "BANK").forEach { m ->
                        FilterChip(selected = method == m, onClick = { method = m }, label = { Text(m) })
                    }
                }
                OutlinedTextField(value = txnId, onValueChange = { txnId = it }, label = { Text("Transaction / UTR ID") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Notes") }, modifier = Modifier.fillMaxWidth())
                if (error != null) Text(error.orEmpty(), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }
        },
        confirmButton = {
            Button(onClick = {
                val amt = amountText.toDoubleOrNull() ?: 0.0
                if (amt <= 0) {
                    error = "Enter a valid positive amount."
                } else {
                    onSave(selectedCustomer, selectedInvoice, amt, method, txnId.trim(), notes.trim())
                }
            }) { Text("Record Payment") }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}
