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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.PictureAsPdf
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.components.CategoryDonutChart
import com.example.ui.components.SaaSBarChart
import com.example.ui.components.SmoothAreaLineChart
import com.example.ui.state.AppUiState
import com.example.ui.theme.LocalSaaSColors
import com.example.util.ExportUtils
import com.example.util.formatInr

private val ReportTabs = listOf(
    "Sales Report",
    "Profit Report",
    "Expense Report",
    "Inventory Report",
    "Customer Report",
    "Payment Report",
)

private val PeriodFilters = listOf("Today", "7 Days", "30 Days", "Month", "Year", "Custom")

@Composable
fun ReportsScreen(
    state: AppUiState,
    onExportFeedback: (String) -> Unit,
) {
    val context = LocalContext.current
    val semantic = LocalSaaSColors.current
    var selectedReport by remember { mutableStateOf(ReportTabs.first()) }
    var selectedPeriod by remember { mutableStateOf("30 Days") }
    var showCustomDialog by remember { mutableStateOf(false) }
    var customRangeLabel by remember { mutableStateOf("01 Nov - 21 Nov 2026") }

    val cutoffMillis = remember(selectedPeriod) {
        val now = System.currentTimeMillis()
        when (selectedPeriod) {
            "Today" -> now - 86_400_000L
            "7 Days" -> now - 7L * 86_400_000L
            "30 Days", "Month" -> now - 30L * 86_400_000L
            "Year" -> now - 365L * 86_400_000L
            else -> 0L
        }
    }

    val periodLabel = if (selectedPeriod == "Custom") customRangeLabel else selectedPeriod
    val filteredInvoices = state.invoices.filter { it.createdAt >= cutoffMillis }
    val filteredExpenses = state.expenses.filter { it.dateMillis >= cutoffMillis }
    val filteredPayments = state.payments.filter { it.dateMillis >= cutoffMillis }

    val summaryRows: List<Pair<String, String>> = remember(selectedReport, state, cutoffMillis) {
        when (selectedReport) {
            "Sales Report" -> listOf(
                "Total Invoices" to "${filteredInvoices.size}",
                "Gross Sales" to formatInr(filteredInvoices.sumOf { it.totalAmount }),
                "Total GST Collected" to formatInr(filteredInvoices.sumOf { it.gstAmount }),
                "Paid Amount" to formatInr(filteredInvoices.sumOf { it.paidAmount }),
                "Pending Receivables" to formatInr(filteredInvoices.sumOf { (it.totalAmount - it.paidAmount).coerceAtLeast(0.0) }),
            )
            "Profit Report" -> {
                val rev = filteredInvoices.sumOf { it.totalAmount }
                val exp = filteredExpenses.sumOf { it.amount }
                val profit = rev - exp
                listOf(
                    "Total Revenue" to formatInr(rev),
                    "Total Expenses" to formatInr(exp),
                    "Net Profit" to formatInr(profit),
                    "Net Margin" to "${if (rev > 0) ((profit / rev) * 100).toInt() else 0}%",
                )
            }
            "Expense Report" -> {
                val grouped = filteredExpenses.groupBy { it.category }.map { (k, v) ->
                    k to formatInr(v.sumOf { it.amount })
                }
                listOf("Total Expenses" to formatInr(filteredExpenses.sumOf { it.amount })) + grouped
            }
            "Inventory Report" -> listOf(
                "Total SKUs" to "${state.products.size}",
                "Total Units in Stock" to "${state.products.sumOf { it.stockQty }}",
                "Stock Cost Value" to formatInr(state.metrics.totalStockValue),
                "Low Stock Items" to "${state.metrics.lowStockCount}",
                "Out of Stock Items" to "${state.metrics.outOfStockCount}",
            )
            "Customer Report" -> listOf(
                "Total Customers" to "${state.customers.size}",
                "Lifetime Customer Sales" to formatInr(state.customers.sumOf { it.totalPurchases }),
                "Total Udhar / Pending" to formatInr(state.customers.sumOf { it.pendingBalance }),
                "Top Buyer" to (state.customers.maxByOrNull { it.totalPurchases }?.name ?: "-"),
            )
            else -> {
                val byMethod = filteredPayments.groupBy { it.method }.map { (m, l) ->
                    "$m Settlements" to formatInr(l.sumOf { it.amount })
                }
                listOf("Total Collected" to formatInr(filteredPayments.sumOf { it.amount })) + byMethod
            }
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Text("Business Reports & Exports", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text("Filter by period and export CA-ready CSV or PDF statements", style = MaterialTheme.typography.bodySmall, color = semantic.textMuted)
        }

        // Report Selector Tabs
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(ReportTabs) { tab ->
                    FilterChip(
                        selected = selectedReport == tab,
                        onClick = { selectedReport = tab },
                        label = { Text(tab) },
                    )
                }
            }
        }

        // Period Filter Chips
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(PeriodFilters) { period ->
                    FilterChip(
                        selected = selectedPeriod == period,
                        onClick = {
                            if (period == "Custom") showCustomDialog = true
                            selectedPeriod = period
                        },
                        label = { Text(period) },
                    )
                }
            }
        }

        // Export Actions
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                OutlinedButton(
                    onClick = {
                        val csv = ExportUtils.generateCsvFile(
                            context = context,
                            fileName = "${selectedReport.lowercase().replace(" ", "_")}.csv",
                            headers = listOf("Metric", "Value", "Period"),
                            rows = summaryRows.map { listOf(it.first, it.second, periodLabel) },
                        )
                        ExportUtils.shareFile(context, csv, "text/csv", "Export $selectedReport CSV")
                        onExportFeedback("CSV exported: ${csv.name}")
                    },
                    modifier = Modifier.weight(1f).testTag("report_export_csv_btn"),
                ) {
                    Icon(Icons.Rounded.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Export CSV")
                }
                Button(
                    onClick = {
                        val pdf = ExportUtils.generateReportPdf(context, selectedReport, periodLabel, summaryRows)
                        ExportUtils.shareFile(context, pdf, "application/pdf", "Export $selectedReport PDF")
                        onExportFeedback("PDF exported: ${pdf.name}")
                    },
                    modifier = Modifier.weight(1f).testTag("report_export_pdf_btn"),
                ) {
                    Icon(Icons.Rounded.PictureAsPdf, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Export PDF")
                }
            }
        }

        // Visualization Card
        item {
            Card(
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, semantic.cardBorder),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("$selectedReport ($periodLabel)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(12.dp))
                    when (selectedReport) {
                        "Sales Report", "Profit Report" -> SmoothAreaLineChart(
                            data = if (selectedReport == "Sales Report") state.metrics.salesSeries else state.metrics.profitSeries,
                            lineColor = MaterialTheme.colorScheme.primary,
                        )
                        "Expense Report" -> SaaSBarChart(
                            data = state.metrics.expenseSeries,
                            barColor = semantic.warning,
                        )
                        else -> CategoryDonutChart(
                            slices = listOf(
                                Triple("UPI", filteredPayments.filter { it.method == "UPI" }.sumOf { it.amount }.toFloat().coerceAtLeast(1000f), MaterialTheme.colorScheme.primary),
                                Triple("QR", filteredPayments.filter { it.method == "QR" }.sumOf { it.amount }.toFloat().coerceAtLeast(1000f), semantic.info),
                                Triple("Cash", filteredPayments.filter { it.method == "CASH" }.sumOf { it.amount }.toFloat().coerceAtLeast(1000f), semantic.warning),
                                Triple("Bank", filteredPayments.filter { it.method == "BANK" }.sumOf { it.amount }.toFloat().coerceAtLeast(1000f), semantic.success),
                            ),
                            centerLabel = "Mix",
                        )
                    }
                }
            }
        }

        // Summary Breakdown Table Card
        item {
            Card(
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, semantic.cardBorder),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Report Summary", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(10.dp))
                    summaryRows.forEachIndexed { idx, (k, v) ->
                        if (idx > 0) HorizontalDivider(color = semantic.cardBorder, modifier = Modifier.padding(vertical = 8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(k, style = MaterialTheme.typography.bodyMedium, color = semantic.textMuted)
                            Text(v, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    if (showCustomDialog) {
        AlertDialog(
            onDismissRequest = { showCustomDialog = false },
            title = { Text("Custom Date Range", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = customRangeLabel,
                    onValueChange = { customRangeLabel = it },
                    label = { Text("Date Range Label") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            },
            confirmButton = {
                Button(onClick = { showCustomDialog = false }) { Text("Apply") }
            },
            dismissButton = {
                OutlinedButton(onClick = { showCustomDialog = false }) { Text("Cancel") }
            },
        )
    }
}
