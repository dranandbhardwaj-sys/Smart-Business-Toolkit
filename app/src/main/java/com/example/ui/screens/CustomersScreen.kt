package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.local.CustomerEntity
import com.example.data.local.InvoiceEntity
import com.example.data.local.PaymentEntity
import com.example.ui.components.ConfirmActionDialog
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.SearchInputBar
import com.example.ui.components.StatusPill
import com.example.ui.components.StatusTone
import com.example.ui.components.invoiceStatusTone
import com.example.ui.theme.LocalSaaSColors
import com.example.util.formatDate
import com.example.util.formatInr

@Composable
fun CustomersScreen(
    customers: List<CustomerEntity>,
    invoices: List<InvoiceEntity>,
    payments: List<PaymentEntity>,
    onSaveCustomer: (CustomerEntity) -> Unit,
    onDeleteCustomer: (CustomerEntity) -> Unit,
    onRecordPaymentForCustomer: (CustomerEntity, Double, String) -> Unit,
) {
    val semantic = LocalSaaSColors.current
    var query by remember { mutableStateOf("") }
    var editingCustomer by remember { mutableStateOf<CustomerEntity?>(null) }
    var showCreateForm by remember { mutableStateOf(false) }
    var detailCustomer by remember { mutableStateOf<CustomerEntity?>(null) }
    var deletingCustomer by remember { mutableStateOf<CustomerEntity?>(null) }

    val filtered = remember(customers, query) {
        customers.filter {
            query.isBlank() || it.name.contains(query, ignoreCase = true) || it.phone.contains(query, ignoreCase = true)
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text("Customers & Khata", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("${customers.size} accounts • Due ${formatInr(customers.sumOf { it.pendingBalance })}", style = MaterialTheme.typography.bodySmall, color = semantic.textMuted)
            }
            Button(
                onClick = { showCreateForm = true },
                modifier = Modifier.testTag("customers_add_btn"),
            ) {
                Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Add Customer")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        SearchInputBar(
            query = query,
            onQueryChange = { query = it },
            placeholder = "Search customer by name or phone...",
        )
        Spacer(modifier = Modifier.height(12.dp))

        if (filtered.isEmpty()) {
            EmptyStateCard(
                title = "No customers found",
                subtitle = "Add your retail or wholesale buyers to track purchases & pending udhar.",
                actionLabel = "Add First Customer",
                onAction = { showCreateForm = true },
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 80.dp),
            ) {
                items(filtered, key = { it.id }) { cust ->
                    Card(
                        shape = MaterialTheme.shapes.large,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, semantic.cardBorder),
                        modifier = Modifier.fillMaxWidth().clickable { detailCustomer = cust },
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(cust.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Rounded.Phone, contentDescription = null, modifier = Modifier.size(14.dp), tint = semantic.textMuted)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(cust.phone, style = MaterialTheme.typography.bodySmall, color = semantic.textMuted)
                                    }
                                }
                                StatusPill(
                                    text = if (cust.pendingBalance > 0) "Due ${formatInr(cust.pendingBalance)}" else "Settled",
                                    tone = if (cust.pendingBalance > 0) StatusTone.DANGER else StatusTone.SUCCESS,
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column {
                                    Text("Total Purchases: ${formatInr(cust.totalPurchases)}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                                    Text("Last txn: ${formatDate(cust.lastTransactionAt)}", style = MaterialTheme.typography.labelSmall, color = semantic.textMuted)
                                }
                                Row {
                                    IconButton(onClick = { editingCustomer = cust }) {
                                        Icon(Icons.Rounded.Edit, contentDescription = "Edit customer")
                                    }
                                    IconButton(onClick = { deletingCustomer = cust }) {
                                        Icon(Icons.Rounded.Delete, contentDescription = "Delete customer", tint = semantic.danger)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCreateForm || editingCustomer != null) {
        CustomerFormDialog(
            initial = editingCustomer,
            onSave = {
                onSaveCustomer(it)
                showCreateForm = false
                editingCustomer = null
            },
            onDismiss = {
                showCreateForm = false
                editingCustomer = null
            },
        )
    }

    detailCustomer?.let { cust ->
        CustomerDetailDialog(
            customer = customers.find { it.id == cust.id } ?: cust,
            invoices = invoices.filter { it.customerId == cust.id },
            payments = payments.filter { it.customerId == cust.id },
            onRecordPayment = { amount, method ->
                onRecordPaymentForCustomer(cust, amount, method)
            },
            onDismiss = { detailCustomer = null },
        )
    }

    deletingCustomer?.let { target ->
        ConfirmActionDialog(
            title = "Remove Customer?",
            message = "Remove ${target.name} from your customer directory?",
            confirmLabel = "Remove",
            onConfirm = {
                onDeleteCustomer(target)
                deletingCustomer = null
            },
            onDismiss = { deletingCustomer = null },
        )
    }
}

@Composable
fun CustomerFormDialog(
    initial: CustomerEntity?,
    onSave: (CustomerEntity) -> Unit,
    onDismiss: () -> Unit,
) {
    var name by remember { mutableStateOf(initial?.name.orEmpty()) }
    var phone by remember { mutableStateOf(initial?.phone.orEmpty()) }
    var email by remember { mutableStateOf(initial?.email.orEmpty()) }
    var gstin by remember { mutableStateOf(initial?.gstin.orEmpty()) }
    var address by remember { mutableStateOf(initial?.address.orEmpty()) }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "Add Customer" else "Edit Customer", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                OutlinedTextField(value = name, onValueChange = { name = it; error = null }, label = { Text("Customer / Business Name*") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = phone, onValueChange = { phone = it; error = null }, label = { Text("Phone Number*") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone), singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email (optional)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = gstin, onValueChange = { gstin = it.uppercase() }, label = { Text("GSTIN (optional)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("Address") }, modifier = Modifier.fillMaxWidth())
                if (error != null) Text(error.orEmpty(), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }
        },
        confirmButton = {
            Button(onClick = {
                if (name.isBlank() || phone.length < 7) {
                    error = "Enter customer name and phone number."
                } else {
                    onSave(
                        (initial ?: CustomerEntity(name = name, phone = phone)).copy(
                            name = name.trim(),
                            phone = phone.trim(),
                            email = email.trim(),
                            gstin = gstin.trim(),
                            address = address.trim(),
                        )
                    )
                }
            }) {
                Text("Save Customer")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}

@Composable
private fun CustomerDetailDialog(
    customer: CustomerEntity,
    invoices: List<InvoiceEntity>,
    payments: List<PaymentEntity>,
    onRecordPayment: (Double, String) -> Unit,
    onDismiss: () -> Unit,
) {
    val semantic = LocalSaaSColors.current
    var payAmountText by remember(customer.pendingBalance) { mutableStateOf(if (customer.pendingBalance > 0) customer.pendingBalance.toInt().toString() else "") }
    var showPayInput by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(0.94f).padding(vertical = 16.dp),
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surface,
        ) {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()).padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(customer.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text("${customer.phone} • ${customer.address.ifBlank { "India" }}", style = MaterialTheme.typography.bodySmall, color = semantic.textMuted)
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Rounded.Close, contentDescription = "Close customer detail")
                    }
                }

                // Summary Cards
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    Surface(
                        color = semantic.successContainer,
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier.weight(1f),
                    ) {
                        Column(Modifier.padding(12.dp)) {
                            Text("Total Purchases", style = MaterialTheme.typography.labelSmall, color = semantic.success)
                            Text(formatInr(customer.totalPurchases), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = semantic.success)
                        }
                    }
                    Surface(
                        color = if (customer.pendingBalance > 0) semantic.dangerContainer else semantic.subtleSurface,
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier.weight(1f),
                    ) {
                        Column(Modifier.padding(12.dp)) {
                            Text("Pending Amount", style = MaterialTheme.typography.labelSmall, color = if (customer.pendingBalance > 0) semantic.danger else semantic.textMuted)
                            Text(formatInr(customer.pendingBalance), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = if (customer.pendingBalance > 0) semantic.danger else semantic.textMuted)
                        }
                    }
                }

                if (customer.pendingBalance > 0) {
                    if (!showPayInput) {
                        Button(onClick = { showPayInput = true }, modifier = Modifier.fillMaxWidth()) {
                            Icon(Icons.Rounded.Payments, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Settle Pending Balance (UPI)")
                        }
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = payAmountText,
                                onValueChange = { payAmountText = it },
                                label = { Text("Amount (₹)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                            )
                            Button(onClick = {
                                val amt = payAmountText.toDoubleOrNull() ?: 0.0
                                if (amt > 0) {
                                    onRecordPayment(amt, "UPI")
                                    showPayInput = false
                                }
                            }) {
                                Text("Save")
                            }
                        }
                    }
                }

                HorizontalDivider(color = semantic.cardBorder)
                Text("Invoice History (${invoices.size})", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                if (invoices.isEmpty()) {
                    Text("No invoices billed yet.", style = MaterialTheme.typography.bodySmall, color = semantic.textMuted)
                } else {
                    invoices.forEach { inv ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column {
                                Text(inv.invoiceNumber, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                Text(formatDate(inv.createdAt), style = MaterialTheme.typography.labelSmall, color = semantic.textMuted)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(formatInr(inv.totalAmount), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                StatusPill(text = inv.status, tone = invoiceStatusTone(inv.status))
                            }
                        }
                    }
                }

                HorizontalDivider(color = semantic.cardBorder)
                Text("Ledger & Payments (${payments.size})", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                if (payments.isEmpty()) {
                    Text("No payments logged yet.", style = MaterialTheme.typography.bodySmall, color = semantic.textMuted)
                } else {
                    payments.forEach { pay ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Column {
                                Text("${pay.method} • ${pay.transactionId}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                                Text(formatDate(pay.dateMillis), style = MaterialTheme.typography.labelSmall, color = semantic.textMuted)
                            }
                            Text("+ ${formatInr(pay.amount)}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = semantic.success)
                        }
                    }
                }
            }
        }
    }
}
