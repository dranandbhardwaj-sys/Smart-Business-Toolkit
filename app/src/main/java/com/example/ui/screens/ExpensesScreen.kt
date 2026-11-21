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
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Today
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
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
import com.example.data.local.ExpenseEntity
import com.example.ui.components.ConfirmActionDialog
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.SaaSBarChart
import com.example.ui.components.SaaSKpiCard
import com.example.ui.components.StatusPill
import com.example.ui.components.StatusTone
import com.example.ui.theme.LocalSaaSColors
import com.example.util.formatCompactInr
import com.example.util.formatDate
import com.example.util.formatInr
import java.util.Calendar

private val ExpenseCategories = listOf("All", "Rent", "Electricity", "Salary", "Transport", "Inventory", "Marketing", "Misc")

@Composable
fun ExpensesScreen(
    expenses: List<ExpenseEntity>,
    onSaveExpense: (ExpenseEntity) -> Unit,
    onDeleteExpense: (ExpenseEntity) -> Unit,
) {
    val semantic = LocalSaaSColors.current
    var selectedCategory by remember { mutableStateOf("All") }
    var editingExpense by remember { mutableStateOf<ExpenseEntity?>(null) }
    var showAddForm by remember { mutableStateOf(false) }
    var deletingExpense by remember { mutableStateOf<ExpenseEntity?>(null) }

    val startOfDay = remember {
        Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0)
        }.timeInMillis
    }
    val thirtyDaysAgo = remember { System.currentTimeMillis() - 30L * 86_400_000L }

    val todayExpenses = expenses.filter { it.dateMillis >= startOfDay }.sumOf { it.amount }
    val monthlyExpenses = expenses.filter { it.dateMillis >= thirtyDaysAgo }.sumOf { it.amount }
    val totalExpenses = expenses.sumOf { it.amount }

    val filtered = remember(expenses, selectedCategory) {
        if (selectedCategory == "All") expenses else expenses.filter { it.category == selectedCategory }
    }

    val chartData = remember(expenses) {
        val grouped = expenses.groupBy { it.category }.map { (cat, list) ->
            cat.take(5) to list.sumOf { it.amount }.toFloat()
        }
        grouped.ifEmpty { listOf("None" to 0f) }
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
                    Text("Expense Management", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text("Track shop rent, electricity, staff & daily spends", style = MaterialTheme.typography.bodySmall, color = semantic.textMuted)
                }
                Button(
                    onClick = { showAddForm = true },
                    modifier = Modifier.testTag("expenses_add_btn"),
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Add Expense")
                }
            }
        }

        // 3 KPI Cards
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                SaaSKpiCard(
                    title = "Today",
                    value = formatCompactInr(todayExpenses),
                    subtitle = "Daily outgo",
                    icon = Icons.Rounded.Today,
                    accentColor = semantic.warning,
                    accentContainer = semantic.warningContainer,
                    modifier = Modifier.weight(1f),
                )
                SaaSKpiCard(
                    title = "Monthly",
                    value = formatCompactInr(monthlyExpenses),
                    subtitle = "Last 30 days",
                    icon = Icons.Rounded.CalendarMonth,
                    accentColor = semantic.danger,
                    accentContainer = semantic.dangerContainer,
                    modifier = Modifier.weight(1f),
                )
                SaaSKpiCard(
                    title = "Total",
                    value = formatCompactInr(totalExpenses),
                    subtitle = "${expenses.size} items",
                    icon = Icons.Rounded.AccountBalanceWallet,
                    accentColor = MaterialTheme.colorScheme.primary,
                    accentContainer = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.weight(1f),
                )
            }
        }

        // Monthly Expense Chart
        item {
            Card(
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, semantic.cardBorder),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Expense Breakdown by Category", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(10.dp))
                    SaaSBarChart(data = chartData, barColor = semantic.warning)
                }
            }
        }

        // Category Filter
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(ExpenseCategories) { cat ->
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = { selectedCategory = cat },
                        label = { Text(cat) },
                    )
                }
            }
        }

        if (filtered.isEmpty()) {
            item {
                EmptyStateCard(
                    title = "No expenses in $selectedCategory",
                    subtitle = "Tap Add Expense to log rent, bills, salary, or transport.",
                    actionLabel = "Add Expense",
                    onAction = { showAddForm = true },
                )
            }
        } else {
            items(filtered, key = { it.id }) { exp ->
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
                                Text(exp.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                StatusPill(text = exp.category, tone = StatusTone.WARNING)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("${formatDate(exp.dateMillis)} • ${exp.paymentMethod}", style = MaterialTheme.typography.labelSmall, color = semantic.textMuted)
                            if (exp.notes.isNotBlank()) {
                                Text(exp.notes, style = MaterialTheme.typography.bodySmall, color = semantic.textMuted)
                            }
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(formatInr(exp.amount), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = semantic.danger)
                            Row {
                                IconButton(onClick = { editingExpense = exp }) {
                                    Icon(Icons.Rounded.Edit, contentDescription = "Edit expense")
                                }
                                IconButton(onClick = { deletingExpense = exp }) {
                                    Icon(Icons.Rounded.Delete, contentDescription = "Delete expense", tint = semantic.danger)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddForm || editingExpense != null) {
        ExpenseFormDialog(
            initial = editingExpense,
            onSave = {
                onSaveExpense(it)
                showAddForm = false
                editingExpense = null
            },
            onDismiss = {
                showAddForm = false
                editingExpense = null
            },
        )
    }

    deletingExpense?.let { target ->
        ConfirmActionDialog(
            title = "Delete Expense?",
            message = "Delete '${target.title}' (${formatInr(target.amount)})?",
            confirmLabel = "Delete",
            onConfirm = {
                onDeleteExpense(target)
                deletingExpense = null
            },
            onDismiss = { deletingExpense = null },
        )
    }
}

@Composable
fun ExpenseFormDialog(
    initial: ExpenseEntity?,
    onSave: (ExpenseEntity) -> Unit,
    onDismiss: () -> Unit,
) {
    var title by remember { mutableStateOf(initial?.title.orEmpty()) }
    var category by remember { mutableStateOf(initial?.category ?: "Rent") }
    var amountText by remember { mutableStateOf(initial?.amount?.toInt()?.toString().orEmpty()) }
    var method by remember { mutableStateOf(initial?.paymentMethod ?: "UPI") }
    var notes by remember { mutableStateOf(initial?.notes.orEmpty()) }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "Add Expense" else "Edit Expense", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                OutlinedTextField(value = title, onValueChange = { title = it; error = null }, label = { Text("Expense Title*") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = amountText, onValueChange = { amountText = it; error = null }, label = { Text("Amount (₹)*") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true, modifier = Modifier.fillMaxWidth())
                Text("Category", style = MaterialTheme.typography.labelMedium)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(ExpenseCategories.drop(1)) { cat ->
                        FilterChip(selected = category == cat, onClick = { category = cat }, label = { Text(cat) })
                    }
                }
                Text("Payment Method", style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("UPI", "QR", "CASH", "BANK").forEach { m ->
                        FilterChip(selected = method == m, onClick = { method = m }, label = { Text(m) })
                    }
                }
                OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Notes") }, modifier = Modifier.fillMaxWidth())
                if (error != null) Text(error.orEmpty(), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }
        },
        confirmButton = {
            Button(onClick = {
                val amt = amountText.toDoubleOrNull() ?: 0.0
                if (title.isBlank() || amt <= 0) {
                    error = "Enter a valid title and positive amount."
                } else {
                    onSave(
                        (initial ?: ExpenseEntity(title = title, category = category, amount = amt)).copy(
                            title = title.trim(),
                            category = category,
                            amount = amt,
                            paymentMethod = method,
                            notes = notes.trim(),
                        )
                    )
                }
            }) {
                Text("Save Expense")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}
