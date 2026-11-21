package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.Savings
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.example.data.local.ProductEntity
import com.example.ui.components.ConfirmActionDialog
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.SaaSKpiCard
import com.example.ui.components.SearchInputBar
import com.example.ui.components.StatusPill
import com.example.ui.components.StatusTone
import com.example.ui.theme.LocalSaaSColors
import com.example.ui.theme.MonoFontFamily
import com.example.util.formatCompactInr
import com.example.util.formatInr

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun InventoryScreen(
    products: List<ProductEntity>,
    onSaveProduct: (ProductEntity) -> Unit,
    onAdjustStock: (ProductEntity, Int) -> Unit,
    onDeleteProduct: (ProductEntity) -> Unit,
) {
    val semantic = LocalSaaSColors.current
    var query by remember { mutableStateOf("") }
    var stockFilter by remember { mutableStateOf("ALL") }
    var showAddForm by remember { mutableStateOf(false) }
    var editingProduct by remember { mutableStateOf<ProductEntity?>(null) }
    var deletingProduct by remember { mutableStateOf<ProductEntity?>(null) }

    val totalValue = products.sumOf { it.stockQty * it.purchasePrice }
    val lowStock = products.count { it.stockQty in 1..it.lowStockThreshold }
    val outOfStock = products.count { it.stockQty <= 0 }

    val filtered = remember(products, query, stockFilter) {
        products.filter { prod ->
            val matchQuery = query.isBlank() ||
                prod.name.contains(query, ignoreCase = true) ||
                prod.sku.contains(query, ignoreCase = true)
            val matchStock = when (stockFilter) {
                "LOW STOCK" -> prod.stockQty in 1..prod.lowStockThreshold
                "OUT OF STOCK" -> prod.stockQty <= 0
                "IN STOCK" -> prod.stockQty > prod.lowStockThreshold
                else -> true
            }
            matchQuery && matchStock
        }
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
                    Text("Inventory & Stock", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text("Manage SKUs, prices & live stock levels", style = MaterialTheme.typography.bodySmall, color = semantic.textMuted)
                }
                Button(
                    onClick = { showAddForm = true },
                    modifier = Modifier.testTag("inventory_add_btn"),
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Add Product")
                }
            }
        }

        // 4 KPI Cards
        item {
            FlowRow(
                maxItemsInEachRow = 2,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                SaaSKpiCard(
                    title = "Total Products",
                    value = "${products.size}",
                    subtitle = "${products.sumOf { it.stockQty }} units total",
                    icon = Icons.Rounded.Inventory2,
                    accentColor = MaterialTheme.colorScheme.primary,
                    accentContainer = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.weight(1f).widthIn(min = 148.dp),
                )
                SaaSKpiCard(
                    title = "Total Stock Value",
                    value = formatCompactInr(totalValue),
                    subtitle = "At purchase cost",
                    icon = Icons.Rounded.Savings,
                    accentColor = semantic.info,
                    accentContainer = semantic.infoContainer,
                    modifier = Modifier.weight(1f).widthIn(min = 148.dp),
                )
                SaaSKpiCard(
                    title = "Low Stock",
                    value = "$lowStock",
                    subtitle = "Reorder soon",
                    icon = Icons.Rounded.WarningAmber,
                    accentColor = semantic.warning,
                    accentContainer = semantic.warningContainer,
                    modifier = Modifier.weight(1f).widthIn(min = 148.dp),
                )
                SaaSKpiCard(
                    title = "Out of Stock",
                    value = "$outOfStock",
                    subtitle = "Restock urgent",
                    icon = Icons.Rounded.ErrorOutline,
                    accentColor = semantic.danger,
                    accentContainer = semantic.dangerContainer,
                    modifier = Modifier.weight(1f).widthIn(min = 148.dp),
                )
            }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SearchInputBar(
                    query = query,
                    onQueryChange = { query = it },
                    placeholder = "Search product name or SKU...",
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(listOf("ALL", "IN STOCK", "LOW STOCK", "OUT OF STOCK")) { filter ->
                        FilterChip(
                            selected = stockFilter == filter,
                            onClick = { stockFilter = filter },
                            label = { Text(filter) },
                        )
                    }
                }
            }
        }

        if (filtered.isEmpty()) {
            item {
                EmptyStateCard(
                    title = "No products match",
                    subtitle = "Add your items with purchase/selling price and GST%.",
                    actionLabel = "Add Product",
                    onAction = { showAddForm = true },
                )
            }
        } else {
            items(filtered, key = { it.id }) { prod ->
                val statusLabel = when {
                    prod.stockQty <= 0 -> "OUT OF STOCK"
                    prod.stockQty <= prod.lowStockThreshold -> "LOW STOCK"
                    else -> "IN STOCK"
                }
                val tone = when {
                    prod.stockQty <= 0 -> StatusTone.DANGER
                    prod.stockQty <= prod.lowStockThreshold -> StatusTone.WARNING
                    else -> StatusTone.SUCCESS
                }

                Card(
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, semantic.cardBorder),
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(prod.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text("SKU: ${prod.sku} • ${prod.category} • GST ${prod.gstPercent.toInt()}%", style = MaterialTheme.typography.labelSmall, fontFamily = MonoFontFamily, color = semantic.textMuted)
                            }
                            StatusPill(text = statusLabel, tone = tone)
                        }

                        Spacer(Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Column {
                                Text("Purchase", style = MaterialTheme.typography.labelSmall, color = semantic.textMuted)
                                Text(formatInr(prod.purchasePrice), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                            }
                            Column {
                                Text("Selling", style = MaterialTheme.typography.labelSmall, color = semantic.textMuted)
                                Text(formatInr(prod.sellingPrice), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Stock Qty", style = MaterialTheme.typography.labelSmall, color = semantic.textMuted)
                                Text("${prod.stockQty} ${prod.unit}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(Modifier.height(10.dp))
                        HorizontalDivider(color = semantic.cardBorder)
                        Spacer(Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(
                                    onClick = { onAdjustStock(prod, -1) },
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                ) {
                                    Icon(Icons.Rounded.Remove, contentDescription = "Stock Out", modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Stock Out", style = MaterialTheme.typography.labelSmall)
                                }
                                Button(
                                    onClick = { onAdjustStock(prod, 5) },
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                ) {
                                    Icon(Icons.Rounded.Add, contentDescription = "Stock In", modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("+5 In", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                            Row {
                                IconButton(onClick = { editingProduct = prod }) {
                                    Icon(Icons.Rounded.Edit, contentDescription = "Edit product")
                                }
                                IconButton(onClick = { deletingProduct = prod }) {
                                    Icon(Icons.Rounded.Delete, contentDescription = "Delete product", tint = semantic.danger)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddForm || editingProduct != null) {
        ProductFormDialog(
            initial = editingProduct,
            onSave = {
                onSaveProduct(it)
                showAddForm = false
                editingProduct = null
            },
            onDismiss = {
                showAddForm = false
                editingProduct = null
            },
        )
    }

    deletingProduct?.let { target ->
        ConfirmActionDialog(
            title = "Delete Product?",
            message = "Delete '${target.name}' (${target.sku}) from inventory?",
            confirmLabel = "Delete",
            onConfirm = {
                onDeleteProduct(target)
                deletingProduct = null
            },
            onDismiss = { deletingProduct = null },
        )
    }
}

@Composable
fun ProductFormDialog(
    initial: ProductEntity?,
    onSave: (ProductEntity) -> Unit,
    onDismiss: () -> Unit,
) {
    var name by remember { mutableStateOf(initial?.name.orEmpty()) }
    var sku by remember { mutableStateOf(initial?.sku.orEmpty()) }
    var category by remember { mutableStateOf(initial?.category ?: "General") }
    var purchaseText by remember { mutableStateOf(initial?.purchasePrice?.toInt()?.toString().orEmpty()) }
    var sellingText by remember { mutableStateOf(initial?.sellingPrice?.toInt()?.toString().orEmpty()) }
    var qtyText by remember { mutableStateOf(initial?.stockQty?.toString() ?: "10") }
    var gstText by remember { mutableStateOf(initial?.gstPercent?.toInt()?.toString() ?: "18") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "Add Product" else "Edit Product", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                OutlinedTextField(value = name, onValueChange = { name = it; error = null }, label = { Text("Product Name*") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = sku, onValueChange = { sku = it.uppercase() }, label = { Text("SKU Code") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text("Category") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = purchaseText, onValueChange = { purchaseText = it }, label = { Text("Buy Price ₹") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true, modifier = Modifier.weight(1f))
                    OutlinedTextField(value = sellingText, onValueChange = { sellingText = it; error = null }, label = { Text("Sell Price ₹*") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true, modifier = Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = qtyText, onValueChange = { qtyText = it }, label = { Text("Stock Qty") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true, modifier = Modifier.weight(1f))
                    OutlinedTextField(value = gstText, onValueChange = { gstText = it }, label = { Text("GST %") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true, modifier = Modifier.weight(1f))
                }
                if (error != null) Text(error.orEmpty(), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }
        },
        confirmButton = {
            Button(onClick = {
                val sell = sellingText.toDoubleOrNull() ?: 0.0
                if (name.isBlank() || sell <= 0) {
                    error = "Enter product name and selling price."
                } else {
                    val buy = purchaseText.toDoubleOrNull() ?: (sell * 0.75)
                    val qty = qtyText.toIntOrNull() ?: 0
                    val gst = gstText.toDoubleOrNull() ?: 18.0
                    onSave(
                        (initial ?: ProductEntity(
                            name = name,
                            sku = sku.ifBlank { "SKU-${System.currentTimeMillis().toString().takeLast(4)}" },
                            category = category,
                            purchasePrice = buy,
                            sellingPrice = sell,
                            stockQty = qty,
                        )).copy(
                            name = name.trim(),
                            sku = sku.ifBlank { "SKU-${System.currentTimeMillis().toString().takeLast(4)}" },
                            category = category.trim().ifBlank { "General" },
                            purchasePrice = buy,
                            sellingPrice = sell,
                            stockQty = qty,
                            gstPercent = gst,
                        )
                    )
                }
            }) { Text("Save Product") }
        },
        dismissButton = { OutlinedButton(onClick = onDismiss) { Text("Cancel") } },
    )
}
