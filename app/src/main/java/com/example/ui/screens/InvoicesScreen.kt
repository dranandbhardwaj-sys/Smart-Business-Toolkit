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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Print
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.local.AppDatabase
import com.example.data.local.BusinessProfileEntity
import com.example.data.local.CustomerEntity
import com.example.data.local.InvoiceEntity
import com.example.data.local.InvoiceLineItem
import com.example.data.local.ProductEntity
import com.example.ui.components.ConfirmActionDialog
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.SearchInputBar
import com.example.ui.components.StatusPill
import com.example.ui.components.invoiceStatusTone
import com.example.ui.theme.LocalSaaSColors
import com.example.ui.theme.MonoFontFamily
import com.example.util.ExportUtils
import com.example.util.formatDate
import com.example.util.formatInr

@Composable
fun InvoicesScreen(
    invoices: List<InvoiceEntity>,
    profile: BusinessProfileEntity,
    onCreateNew: () -> Unit,
    onPreviewInvoice: (InvoiceEntity) -> Unit,
    onMarkPaid: (InvoiceEntity) -> Unit,
    onDeleteInvoice: (InvoiceEntity) -> Unit,
    onActionFeedback: (String) -> Unit,
) {
    val context = LocalContext.current
    val semantic = LocalSaaSColors.current
    var query by remember { mutableStateOf("") }
    var statusFilter by remember { mutableStateOf("ALL") }
    var pendingDelete by remember { mutableStateOf<InvoiceEntity?>(null) }

    val filtered = remember(invoices, query, statusFilter) {
        invoices.filter { inv ->
            val matchFilter = statusFilter == "ALL" || inv.status.equals(statusFilter, ignoreCase = true)
            val matchQuery = query.isBlank() ||
                inv.invoiceNumber.contains(query, ignoreCase = true) ||
                inv.customerName.contains(query, ignoreCase = true)
            matchFilter && matchQuery
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text("Invoices & GST Billing", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("${invoices.size} total invoices", style = MaterialTheme.typography.bodySmall, color = semantic.textMuted)
            }
            Button(
                onClick = onCreateNew,
                modifier = Modifier.testTag("invoices_new_btn"),
            ) {
                Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("New Invoice")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        SearchInputBar(
            query = query,
            onQueryChange = { query = it },
            placeholder = "Search invoice # or customer...",
            testTag = "invoices_search_input",
        )
        Spacer(modifier = Modifier.height(10.dp))

        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(listOf("ALL", "PAID", "PARTIAL", "PENDING", "OVERDUE")) { status ->
                FilterChip(
                    selected = statusFilter == status,
                    onClick = { statusFilter = status },
                    label = { Text(status) },
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (filtered.isEmpty()) {
            EmptyStateCard(
                title = "No matching invoices",
                subtitle = "Create a GST or non-GST bill and share it directly on WhatsApp.",
                actionLabel = "Create First Invoice",
                onAction = onCreateNew,
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 80.dp),
            ) {
                items(filtered, key = { it.id }) { inv ->
                    val lineItems = remember(inv.itemsJson) { AppDatabase.decodeLineItems(inv.itemsJson) }
                    Card(
                        shape = MaterialTheme.shapes.large,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, semantic.cardBorder),
                        modifier = Modifier.fillMaxWidth().clickable { onPreviewInvoice(inv) },
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(inv.invoiceNumber, style = MaterialTheme.typography.labelLarge, fontFamily = MonoFontFamily, color = MaterialTheme.colorScheme.primary)
                                    Text(inv.customerName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    Text("${formatDate(inv.createdAt)} • ${inv.paymentMethod} • ${lineItems.size} items", style = MaterialTheme.typography.bodySmall, color = semantic.textMuted)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(formatInr(inv.totalAmount), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    StatusPill(text = inv.status, tone = invoiceStatusTone(inv.status))
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            HorizontalDivider(color = semantic.cardBorder)
                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Row {
                                    IconButton(onClick = { onPreviewInvoice(inv) }) {
                                        Icon(Icons.Rounded.Visibility, contentDescription = "Preview invoice", tint = MaterialTheme.colorScheme.primary)
                                    }
                                    IconButton(onClick = {
                                        val pdf = ExportUtils.generateInvoicePdf(context, profile, inv, lineItems)
                                        ExportUtils.shareFile(context, pdf, "application/pdf", "PDF ${inv.invoiceNumber}")
                                        onActionFeedback("PDF exported for ${inv.invoiceNumber}")
                                    }) {
                                        Icon(Icons.Rounded.Download, contentDescription = "Download PDF")
                                    }
                                    IconButton(onClick = {
                                        val pdf = ExportUtils.generateInvoicePdf(context, profile, inv, lineItems)
                                        ExportUtils.printPdfFile(context, pdf, inv.invoiceNumber)
                                    }) {
                                        Icon(Icons.Rounded.Print, contentDescription = "Print invoice")
                                    }
                                    IconButton(onClick = {
                                        val pdf = ExportUtils.generateInvoicePdf(context, profile, inv, lineItems)
                                        ExportUtils.shareInvoiceOnWhatsApp(context, profile, inv, pdf)
                                    }) {
                                        Icon(Icons.Rounded.Share, contentDescription = "Share on WhatsApp", tint = semantic.success)
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (inv.status != "PAID") {
                                        TextButton(onClick = { onMarkPaid(inv) }) {
                                            Text("Mark Paid")
                                        }
                                    }
                                    IconButton(onClick = { pendingDelete = inv }) {
                                        Icon(Icons.Rounded.Delete, contentDescription = "Delete invoice", tint = semantic.danger)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    pendingDelete?.let { target ->
        ConfirmActionDialog(
            title = "Delete Invoice?",
            message = "Are you sure you want to delete ${target.invoiceNumber}?",
            confirmLabel = "Delete",
            onConfirm = {
                onDeleteInvoice(target)
                pendingDelete = null
            },
            onDismiss = { pendingDelete = null },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewInvoiceScreen(
    customers: List<CustomerEntity>,
    products: List<ProductEntity>,
    profile: BusinessProfileEntity,
    onBack: () -> Unit,
    onQuickSaveCustomer: (CustomerEntity) -> Unit,
    onQuickSaveProduct: (ProductEntity) -> Unit,
    onSaveInvoice: (
        customer: CustomerEntity,
        items: List<InvoiceLineItem>,
        discountAmount: Double,
        paidAmount: Double,
        paymentMethod: String,
        notes: String,
    ) -> Unit,
) {
    val semantic = LocalSaaSColors.current
    var selectedCustomer by remember(customers) { mutableStateOf(customers.firstOrNull()) }
    var customerExpanded by remember { mutableStateOf(false) }

    val lineItems = remember { mutableStateListOf<InvoiceLineItem>() }
    var showAddCustomerDialog by remember { mutableStateOf(false) }
    var showAddProductDialog by remember { mutableStateOf(false) }

    var discountText by remember { mutableStateOf("0") }
    var paymentStatus by remember { mutableStateOf("PAID") }
    var paymentMethod by remember { mutableStateOf("UPI") }
    var paidText by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    val subtotal = lineItems.sumOf { it.qty * it.unitPrice }
    val discount = discountText.toDoubleOrNull()?.coerceAtLeast(0.0) ?: 0.0
    val taxable = (subtotal - discount).coerceAtLeast(0.0)
    val gstTotal = if (profile.gstRegistered) {
        val rawSub = subtotal.coerceAtLeast(1.0)
        lineItems.sumOf { line ->
            val share = (line.qty * line.unitPrice) / rawSub
            (taxable * share) * (line.gstPercent / 100.0)
        }
    } else 0.0
    val grandTotal = taxable + gstTotal

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = stringResource(R.string.cd_back))
            }
            Column {
                Text("Create New Invoice", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("GST-ready Indian bill with instant PDF & WhatsApp share", style = MaterialTheme.typography.bodySmall, color = semantic.textMuted)
            }
        }

        // Customer Selector
        Card(
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, semantic.cardBorder),
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("1. Select Customer", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    TextButton(onClick = { showAddCustomerDialog = true }) {
                        Text("+ Add Customer")
                    }
                }
                ExposedDropdownMenuBox(
                    expanded = customerExpanded,
                    onExpandedChange = { customerExpanded = !customerExpanded },
                ) {
                    OutlinedTextField(
                        value = selectedCustomer?.let { "${it.name} (${it.phone})" } ?: "Select customer",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = customerExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                    )
                    ExposedDropdownMenu(
                        expanded = customerExpanded,
                        onDismissRequest = { customerExpanded = false },
                    ) {
                        customers.forEach { cust ->
                            DropdownMenuItem(
                                text = { Text("${cust.name} • ${cust.phone}") },
                                onClick = {
                                    selectedCustomer = cust
                                    customerExpanded = false
                                },
                            )
                        }
                    }
                }
            }
        }

        // Product Picker + Added Items
        Card(
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, semantic.cardBorder),
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("2. Add Products / Services", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    TextButton(onClick = { showAddProductDialog = true }) {
                        Text("+ New Product")
                    }
                }

                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(products) { prod ->
                        Surface(
                            shape = MaterialTheme.shapes.medium,
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)),
                            modifier = Modifier.clickable {
                                val existingIdx = lineItems.indexOfFirst { it.productId == prod.id }
                                if (existingIdx >= 0) {
                                    val cur = lineItems[existingIdx]
                                    lineItems[existingIdx] = cur.copy(qty = cur.qty + 1)
                                } else {
                                    lineItems.add(
                                        InvoiceLineItem(
                                            productId = prod.id,
                                            name = prod.name,
                                            sku = prod.sku,
                                            qty = 1,
                                            unitPrice = prod.sellingPrice,
                                            gstPercent = if (profile.gstRegistered) prod.gstPercent else 0.0,
                                        )
                                    )
                                }
                                errorMsg = null
                            },
                        ) {
                            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                                Text(prod.name, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                                Text("${formatInr(prod.sellingPrice)} • Stock ${prod.stockQty}", style = MaterialTheme.typography.labelSmall, color = semantic.textMuted)
                            }
                        }
                    }
                }

                if (lineItems.isEmpty()) {
                    Text("Tap a product chip above to add line items.", style = MaterialTheme.typography.bodySmall, color = semantic.textMuted)
                } else {
                    lineItems.forEachIndexed { idx, line ->
                        Surface(
                            shape = MaterialTheme.shapes.medium,
                            color = MaterialTheme.colorScheme.background,
                            border = BorderStroke(1.dp, semantic.cardBorder),
                        ) {
                            Column(modifier = Modifier.fillMaxWidth().padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text(line.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                                    IconButton(onClick = { lineItems.removeAt(idx) }) {
                                        Icon(Icons.Rounded.Delete, contentDescription = "Remove item", tint = semantic.danger, modifier = Modifier.size(18.dp))
                                    }
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        IconButton(onClick = {
                                            if (line.qty > 1) lineItems[idx] = line.copy(qty = line.qty - 1)
                                        }) {
                                            Icon(Icons.Rounded.Remove, contentDescription = "Decrease qty")
                                        }
                                        Text("${line.qty}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                        IconButton(onClick = {
                                            lineItems[idx] = line.copy(qty = line.qty + 1)
                                        }) {
                                            Icon(Icons.Rounded.Add, contentDescription = "Increase qty")
                                        }
                                    }
                                    Text(
                                        text = "${formatInr(line.unitPrice)} x ${line.qty} (+${line.gstPercent.toInt()}% GST)",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = semantic.textMuted,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Totals & Payment Status
        Card(
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, semantic.cardBorder),
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("3. Discount, GST & Payment", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = discountText,
                    onValueChange = { discountText = it },
                    label = { Text("Discount Amount (₹)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )

                Text("Payment Status", style = MaterialTheme.typography.labelMedium, color = semantic.textMuted)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("PAID", "PARTIAL", "PENDING").forEach { st ->
                        FilterChip(
                            selected = paymentStatus == st,
                            onClick = { paymentStatus = st },
                            label = { Text(st) },
                        )
                    }
                }

                if (paymentStatus == "PARTIAL") {
                    OutlinedTextField(
                        value = paidText,
                        onValueChange = { paidText = it },
                        label = { Text("Paid Amount (₹)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                Text("Payment Mode", style = MaterialTheme.typography.labelMedium, color = semantic.textMuted)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("UPI", "QR", "CASH", "BANK").forEach { mode ->
                        FilterChip(
                            selected = paymentMethod == mode,
                            onClick = { paymentMethod = mode },
                            label = { Text(mode) },
                        )
                    }
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )

                HorizontalDivider(color = semantic.cardBorder)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Subtotal", style = MaterialTheme.typography.bodySmall, color = semantic.textMuted)
                    Text(formatInr(subtotal), style = MaterialTheme.typography.bodySmall)
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("GST Tax", style = MaterialTheme.typography.bodySmall, color = semantic.textMuted)
                    Text(formatInr(gstTotal), style = MaterialTheme.typography.bodySmall)
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Grand Total", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(formatInr(grandTotal), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }

                if (errorMsg != null) {
                    Text(errorMsg.orEmpty(), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }

                Button(
                    onClick = {
                        val cust = selectedCustomer
                        if (cust == null) {
                            errorMsg = "Please select or add a customer."
                            return@Button
                        }
                        if (lineItems.isEmpty()) {
                            errorMsg = "Add at least one product to the invoice."
                            return@Button
                        }
                        val paid = when (paymentStatus) {
                            "PAID" -> grandTotal
                            "PARTIAL" -> paidText.toDoubleOrNull() ?: (grandTotal / 2.0)
                            else -> 0.0
                        }
                        onSaveInvoice(cust, lineItems.toList(), discount, paid, paymentMethod, notes)
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp).testTag("save_invoice_btn"),
                ) {
                    Text("Save & Preview Invoice", fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    if (showAddCustomerDialog) {
        CustomerFormDialog(
            initial = null,
            onSave = {
                onQuickSaveCustomer(it)
                showAddCustomerDialog = false
            },
            onDismiss = { showAddCustomerDialog = false },
        )
    }

    if (showAddProductDialog) {
        ProductFormDialog(
            initial = null,
            onSave = {
                onQuickSaveProduct(it)
                showAddProductDialog = false
            },
            onDismiss = { showAddProductDialog = false },
        )
    }
}
