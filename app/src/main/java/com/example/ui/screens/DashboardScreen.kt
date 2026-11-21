package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AddBox
import androidx.compose.material.icons.rounded.CurrencyRupee
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material.icons.rounded.PendingActions
import androidx.compose.material.icons.rounded.PersonAdd
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.TrendingUp
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.InvoiceEntity
import com.example.navigation.AppRoutes
import com.example.ui.components.LoadingSkeletonCard
import com.example.ui.components.SaaSBarChart
import com.example.ui.components.SaaSKpiCard
import com.example.ui.components.SectionHeader
import com.example.ui.components.SmoothAreaLineChart
import com.example.ui.components.StatusPill
import com.example.ui.components.StatusTone
import com.example.ui.components.invoiceStatusTone
import com.example.ui.state.AppUiState
import com.example.ui.theme.LocalSaaSColors
import com.example.util.formatCompactInr
import com.example.util.formatDateTime
import com.example.util.formatInr
import com.example.util.tr

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DashboardScreen(
    state: AppUiState,
    onNavigate: (String) -> Unit,
    onNewInvoice: () -> Unit,
    onQuickAddCustomer: () -> Unit,
    onQuickAddExpense: () -> Unit,
    onQuickAddProduct: () -> Unit,
    onPreviewInvoice: (InvoiceEntity) -> Unit,
    onMarkInvoicePaid: (InvoiceEntity) -> Unit,
) {
    val semantic = LocalSaaSColors.current
    val lang = state.session.language
    var chartTab by remember { mutableIntStateOf(0) } // 0=Sales, 1=Expenses, 2=Profit

    if (state.isLoading) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            LoadingSkeletonCard()
            LoadingSkeletonCard()
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Quick Actions Strip
        item {
            SectionHeader(
                title = tr("Quick Actions", "त्वरित कार्य", lang),
                subtitle = tr("One-tap daily workflows", "दैनिक बिजनेस काम तुरंत करें", lang),
            )
            Spacer(modifier = Modifier.height(10.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                item {
                    QuickActionButton(
                        label = tr("Create Invoice", "नया बिल बनाएं", lang),
                        icon = Icons.Rounded.Add,
                        isPrimary = true,
                        testTag = "qa_create_invoice",
                        onClick = onNewInvoice,
                    )
                }
                item {
                    QuickActionButton(
                        label = tr("Add Customer", "ग्राहक जोड़ें", lang),
                        icon = Icons.Rounded.PersonAdd,
                        isPrimary = false,
                        testTag = "qa_add_customer",
                        onClick = onQuickAddCustomer,
                    )
                }
                item {
                    QuickActionButton(
                        label = tr("Add Expense", "खर्च लिखें", lang),
                        icon = Icons.Rounded.AccountBalanceWallet,
                        isPrimary = false,
                        testTag = "qa_add_expense",
                        onClick = onQuickAddExpense,
                    )
                }
                item {
                    QuickActionButton(
                        label = tr("Add Product", "प्रोडक्ट जोड़ें", lang),
                        icon = Icons.Rounded.AddBox,
                        isPrimary = false,
                        testTag = "qa_add_product",
                        onClick = onQuickAddProduct,
                    )
                }
            }
        }

        // 6 KPI Cards Grid
        item {
            FlowRow(
                maxItemsInEachRow = 3,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                SaaSKpiCard(
                    title = tr("Today's Sales", "आज की बिक्री", lang),
                    value = formatInr(state.metrics.todaysSales),
                    subtitle = "${state.invoices.count()} total bills",
                    icon = Icons.Rounded.CurrencyRupee,
                    accentColor = MaterialTheme.colorScheme.primary,
                    accentContainer = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.weight(1f).widthIn(min = 152.dp),
                    onClick = { onNavigate(AppRoutes.INVOICES) },
                )
                SaaSKpiCard(
                    title = tr("Monthly Sales", "मासिक बिक्री", lang),
                    value = formatCompactInr(state.metrics.monthlySales),
                    subtitle = "+18.4% vs last 30d",
                    icon = Icons.Rounded.TrendingUp,
                    accentColor = semantic.success,
                    accentContainer = semantic.successContainer,
                    badgeText = "30d",
                    modifier = Modifier.weight(1f).widthIn(min = 152.dp),
                    onClick = { onNavigate(AppRoutes.REPORTS) },
                )
                SaaSKpiCard(
                    title = tr("Expenses", "कुल खर्च", lang),
                    value = formatCompactInr(state.metrics.monthlyExpenses),
                    subtitle = "${state.expenses.size} entries",
                    icon = Icons.Rounded.AccountBalanceWallet,
                    accentColor = semantic.warning,
                    accentContainer = semantic.warningContainer,
                    modifier = Modifier.weight(1f).widthIn(min = 152.dp),
                    onClick = { onNavigate(AppRoutes.EXPENSES) },
                )
                SaaSKpiCard(
                    title = tr("Net Profit", "शुद्ध मुनाफा", lang),
                    value = formatCompactInr(state.metrics.netProfit),
                    subtitle = if (state.metrics.netProfit >= 0) "Healthy margin" else "Watch expenses",
                    icon = Icons.Rounded.TrendingUp,
                    accentColor = if (state.metrics.netProfit >= 0) semantic.success else semantic.danger,
                    accentContainer = if (state.metrics.netProfit >= 0) semantic.successContainer else semantic.dangerContainer,
                    modifier = Modifier.weight(1f).widthIn(min = 152.dp),
                    onClick = { onNavigate(AppRoutes.REPORTS) },
                )
                SaaSKpiCard(
                    title = tr("Pending Payments", "बकाया उधार", lang),
                    value = formatCompactInr(state.metrics.pendingPayments),
                    subtitle = "${state.customers.count { it.pendingBalance > 0 }} customers due",
                    icon = Icons.Rounded.PendingActions,
                    accentColor = semantic.danger,
                    accentContainer = semantic.dangerContainer,
                    modifier = Modifier.weight(1f).widthIn(min = 152.dp),
                    onClick = { onNavigate(AppRoutes.PAYMENTS) },
                )
                SaaSKpiCard(
                    title = tr("Total Customers", "कुल ग्राहक", lang),
                    value = "${state.metrics.totalCustomers}",
                    subtitle = "Active khata accounts",
                    icon = Icons.Rounded.Groups,
                    accentColor = semantic.info,
                    accentContainer = semantic.infoContainer,
                    modifier = Modifier.weight(1f).widthIn(min = 152.dp),
                    onClick = { onNavigate(AppRoutes.CUSTOMERS) },
                )
            }
        }

        // Interactive Charts Card (Sales / Expenses / Profit)
        item {
            Card(
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, semantic.cardBorder),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    SectionHeader(
                        title = tr("Business Performance", "बिजनेस ग्रोथ चार्ट", lang),
                        subtitle = "6-month trend (₹ in thousands)",
                        actionLabel = tr("Reports", "रिपोर्ट्स", lang),
                        onActionClick = { onNavigate(AppRoutes.REPORTS) },
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("Sales", "Expenses", "Profit").forEachIndexed { idx, label ->
                            FilterChip(
                                selected = chartTab == idx,
                                onClick = { chartTab = idx },
                                label = { Text(label) },
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    when (chartTab) {
                        0 -> SmoothAreaLineChart(
                            data = state.metrics.salesSeries,
                            lineColor = MaterialTheme.colorScheme.primary,
                        )
                        1 -> SaaSBarChart(
                            data = state.metrics.expenseSeries,
                            barColor = semantic.warning,
                            valuePrefix = "₹",
                        )
                        else -> SmoothAreaLineChart(
                            data = state.metrics.profitSeries,
                            lineColor = semantic.info,
                        )
                    }
                }
            }
        }

        // Recent Invoices
        item {
            Card(
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, semantic.cardBorder),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    SectionHeader(
                        title = tr("Recent Invoices", "हाल के बिल", lang),
                        actionLabel = "View All",
                        onActionClick = { onNavigate(AppRoutes.INVOICES) },
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    state.invoices.take(4).forEachIndexed { index, inv ->
                        if (index > 0) HorizontalDivider(color = semantic.cardBorder, modifier = Modifier.padding(vertical = 8.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onPreviewInvoice(inv) }
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Icon(Icons.Rounded.ReceiptLong, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(inv.customerName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                                    Text("${inv.invoiceNumber} • ${formatDateTime(inv.createdAt)}", style = MaterialTheme.typography.labelSmall, color = semantic.textMuted)
                                }
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(formatInr(inv.totalAmount), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(2.dp))
                                StatusPill(text = inv.status, tone = invoiceStatusTone(inv.status))
                            }
                        }
                    }
                }
            }
        }

        // Outstanding Payments & Low Stock Alerts
        item {
            val overdueInvoices = state.invoices.filter { it.status != "PAID" }
            val lowStockItems = state.products.filter { it.stockQty <= it.lowStockThreshold }

            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Card(
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, semantic.cardBorder),
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        SectionHeader(
                            title = tr("Outstanding Payments", "बकाया भुगतान", lang),
                            subtitle = "${overdueInvoices.size} pending bills",
                            actionLabel = "Collect",
                            onActionClick = { onNavigate(AppRoutes.PAYMENTS) },
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        if (overdueInvoices.isEmpty()) {
                            Text("All invoices settled!", style = MaterialTheme.typography.bodySmall, color = semantic.success)
                        } else {
                            overdueInvoices.take(3).forEach { inv ->
                                val due = (inv.totalAmount - inv.paidAmount).coerceAtLeast(0.0)
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(inv.customerName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                        Text("${inv.invoiceNumber} • Due ${formatInr(due)}", style = MaterialTheme.typography.labelSmall, color = semantic.danger)
                                    }
                                    OutlinedButton(
                                        onClick = { onMarkInvoicePaid(inv) },
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                    ) {
                                        Text("Mark Paid", style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                            }
                        }
                    }
                }

                Card(
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, semantic.cardBorder),
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        SectionHeader(
                            title = tr("Low Stock Alerts", "कम स्टॉक चेतावनी", lang),
                            subtitle = "${lowStockItems.size} items need restock",
                            actionLabel = "Inventory",
                            onActionClick = { onNavigate(AppRoutes.INVENTORY) },
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        if (lowStockItems.isEmpty()) {
                            Text("All inventory levels are healthy.", style = MaterialTheme.typography.bodySmall, color = semantic.success)
                        } else {
                            lowStockItems.take(4).forEach { prod ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                        Icon(
                                            imageVector = if (prod.stockQty == 0) Icons.Rounded.WarningAmber else Icons.Rounded.Inventory2,
                                            contentDescription = null,
                                            tint = if (prod.stockQty == 0) semantic.danger else semantic.warning,
                                            modifier = Modifier.size(18.dp),
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(prod.name, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                                            Text("SKU: ${prod.sku}", style = MaterialTheme.typography.labelSmall, color = semantic.textMuted)
                                        }
                                    }
                                    StatusPill(
                                        text = if (prod.stockQty == 0) "OUT OF STOCK" else "${prod.stockQty} left",
                                        tone = if (prod.stockQty == 0) StatusTone.DANGER else StatusTone.WARNING,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Recent Transactions
        item {
            Card(
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, semantic.cardBorder),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    SectionHeader(
                        title = tr("Recent Transactions", "ताज़ा पेमेंट लेन-देन", lang),
                        actionLabel = "Payments",
                        onActionClick = { onNavigate(AppRoutes.PAYMENTS) },
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    state.payments.take(4).forEachIndexed { idx, pay ->
                        if (idx > 0) HorizontalDivider(color = semantic.cardBorder, modifier = Modifier.padding(vertical = 6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(pay.customerName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                Text("${pay.method} • ${pay.transactionId}", style = MaterialTheme.typography.labelSmall, color = semantic.textMuted)
                            }
                            Text(
                                text = "+ ${formatInr(pay.amount)}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = semantic.success,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickActionButton(
    label: String,
    icon: ImageVector,
    isPrimary: Boolean,
    testTag: String,
    onClick: () -> Unit,
) {
    if (isPrimary) {
        Button(
            onClick = onClick,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.height(44.dp).testTag(testTag),
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(label, fontWeight = FontWeight.SemiBold)
        }
    } else {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, LocalSaaSColors.current.cardBorder),
            modifier = Modifier.height(44.dp).clickable(onClick = onClick).testTag(testTag),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(label, style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}
