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
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.WorkspacePremium
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.unit.dp
import com.example.data.local.SubscriptionEntity
import com.example.data.local.SubscriptionPaymentEntity
import com.example.ui.components.StatusPill
import com.example.ui.components.invoiceStatusTone
import com.example.ui.theme.LocalSaaSColors
import com.example.ui.theme.MonoFontFamily
import com.example.util.formatDate

@Composable
fun SubscriptionScreen(
    subscription: SubscriptionEntity,
    paymentHistory: List<SubscriptionPaymentEntity>,
    onRenewSubscription: (String) -> Unit,
) {
    val semantic = LocalSaaSColors.current
    var showCheckoutDialog by remember { mutableStateOf(false) }
    var selectedPayMethod by remember { mutableStateOf("UPI AutoPay") }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Text("Subscription & Billing", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text("Simple ₹399/month SaaS plan for Indian businesses", style = MaterialTheme.typography.bodySmall, color = semantic.textMuted)
        }

        // Current Plan Hero Card
        item {
            Card(
                shape = MaterialTheme.shapes.extraLarge,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)),
                border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.WorkspacePremium, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(8.dp))
                            Text("CURRENT PLAN", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                        StatusPill(text = subscription.status, tone = invoiceStatusTone(subscription.status))
                    }

                    Text(subscription.planName, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text("₹${subscription.priceInr}", style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Text("/month", style = MaterialTheme.typography.titleMedium, color = semantic.textMuted, modifier = Modifier.padding(bottom = 4.dp))
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Column {
                            Text("Trial Status", style = MaterialTheme.typography.labelSmall, color = semantic.textMuted)
                            Text(
                                text = if (subscription.status == "TRIAL") "${subscription.trialDaysLeft} days remaining" else "Active Pro Member",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Expiry / Renewal Date", style = MaterialTheme.typography.labelSmall, color = semantic.textMuted)
                            Text(formatDate(subscription.expiryDateMillis), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))

                    Text("Included Pro Features", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    listOf(
                        "Unlimited GST & Non-GST Invoices",
                        "Unlimited Customer Records & Khata",
                        "Expense Tracking & Category Charts",
                        "UPI, BharatQR, Cash & Bank Records",
                        "Inventory & Low-Stock Management",
                        "Sales, Profit & GST Reports (PDF/CSV)",
                    ).forEach { feat ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(feat, style = MaterialTheme.typography.bodySmall)
                        }
                    }

                    Spacer(Modifier.height(4.dp))
                    Button(
                        onClick = { showCheckoutDialog = true },
                        modifier = Modifier.fillMaxWidth().height(50.dp).testTag("renew_subscription_btn"),
                    ) {
                        Text(
                            text = if (subscription.status == "ACTIVE") "Renew Subscription (+30 Days • ₹399)" else "Activate Pro Plan (₹399/month)",
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
        }

        // Payment History
        item {
            Text("Subscription Payment History", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }

        items(paymentHistory, key = { it.id }) { pay ->
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
                        Text(pay.planName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Text("${pay.method} • ${pay.transactionId}", style = MaterialTheme.typography.labelSmall, fontFamily = MonoFontFamily, color = semantic.textMuted)
                        Text(formatDate(pay.paidAtMillis), style = MaterialTheme.typography.labelSmall, color = semantic.textMuted)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("₹${pay.amountInr}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        StatusPill(text = pay.status, tone = invoiceStatusTone(pay.status))
                    }
                }
            }
        }
    }

    if (showCheckoutDialog) {
        AlertDialog(
            onDismissRequest = { showCheckoutDialog = false },
            title = { Text("Renew Smart Business Pro", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Total Payable: ₹399 / month (30 days validity)", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                    Text("Select Payment Method", style = MaterialTheme.typography.labelMedium, color = semantic.textMuted)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("UPI AutoPay", "BharatQR", "Debit/Credit Card").forEach { m ->
                            FilterChip(selected = selectedPayMethod == m, onClick = { selectedPayMethod = m }, label = { Text(m) })
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    onRenewSubscription(selectedPayMethod)
                    showCheckoutDialog = false
                }) {
                    Text("Pay ₹399 Now")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showCheckoutDialog = false }) { Text("Cancel") }
            },
        )
    }
}
