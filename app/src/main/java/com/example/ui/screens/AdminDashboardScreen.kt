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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CurrencyRupee
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.HourglassTop
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.unit.dp
import com.example.data.local.AdminUserEntity
import com.example.data.local.BusinessProfileEntity
import com.example.data.local.SubscriptionPaymentEntity
import com.example.ui.components.SaaSKpiCard
import com.example.ui.components.StatusPill
import com.example.ui.components.invoiceStatusTone
import com.example.ui.theme.LocalSaaSColors
import com.example.util.formatDate
import com.example.util.formatInr

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AdminDashboardScreen(
    adminUsers: List<AdminUserEntity>,
    subscriptionPayments: List<SubscriptionPaymentEntity>,
    profile: BusinessProfileEntity,
    onUpdateUserPlan: (AdminUserEntity, String) -> Unit,
    onUpdateWhatsappUrl: (String) -> Unit,
) {
    val semantic = LocalSaaSColors.current
    var adminTab by remember { mutableStateOf("Users") }
    var whatsappUrl by remember(profile.whatsappSupportUrl) { mutableStateOf(profile.whatsappSupportUrl) }

    val totalUsers = adminUsers.size
    val activeSubs = adminUsers.count { it.planStatus == "ACTIVE" }
    val trialAccounts = adminUsers.count { it.planStatus == "TRIAL" }
    val expiredUsers = adminUsers.count { it.planStatus == "EXPIRED" }
    val mrr = adminUsers.sumOf { it.monthlyRevenueInr }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Text("SaaS Admin Console", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text("Monitor merchants, ₹399/mo MRR, subscriptions & support config", style = MaterialTheme.typography.bodySmall, color = semantic.textMuted)
        }

        // 5 Admin KPI Cards
        item {
            FlowRow(
                maxItemsInEachRow = 3,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                SaaSKpiCard(
                    title = "Total Users",
                    value = "$totalUsers",
                    subtitle = "Registered SMBs",
                    icon = Icons.Rounded.Groups,
                    accentColor = semantic.info,
                    accentContainer = semantic.infoContainer,
                    modifier = Modifier.weight(1f).widthIn(min = 148.dp),
                )
                SaaSKpiCard(
                    title = "Active Subs",
                    value = "$activeSubs",
                    subtitle = "Paid ₹399/mo",
                    icon = Icons.Rounded.Verified,
                    accentColor = semantic.success,
                    accentContainer = semantic.successContainer,
                    modifier = Modifier.weight(1f).widthIn(min = 148.dp),
                )
                SaaSKpiCard(
                    title = "Trial Accounts",
                    value = "$trialAccounts",
                    subtitle = "14-day trial",
                    icon = Icons.Rounded.HourglassTop,
                    accentColor = semantic.warning,
                    accentContainer = semantic.warningContainer,
                    modifier = Modifier.weight(1f).widthIn(min = 148.dp),
                )
                SaaSKpiCard(
                    title = "Expired Users",
                    value = "$expiredUsers",
                    subtitle = "Needs renewal",
                    icon = Icons.Rounded.ErrorOutline,
                    accentColor = semantic.danger,
                    accentContainer = semantic.dangerContainer,
                    modifier = Modifier.weight(1f).widthIn(min = 148.dp),
                )
                SaaSKpiCard(
                    title = "Monthly Revenue",
                    value = formatInr(mrr.toDouble()),
                    subtitle = "MRR @ ₹399/user",
                    icon = Icons.Rounded.CurrencyRupee,
                    accentColor = MaterialTheme.colorScheme.primary,
                    accentContainer = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.weight(1f).widthIn(min = 148.dp),
                )
            }
        }

        // Section Switcher: Users / Subscriptions / Payments / Settings
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(listOf("Users", "Subscriptions", "Payments", "Settings")) { tab ->
                    FilterChip(
                        selected = adminTab == tab,
                        onClick = { adminTab = tab },
                        label = { Text(tab) },
                    )
                }
            }
        }

        when (adminTab) {
            "Users", "Subscriptions" -> {
                items(adminUsers, key = { it.id }) { user ->
                    Card(
                        shape = MaterialTheme.shapes.large,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, semantic.cardBorder),
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(user.businessName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                    Text("${user.ownerName} • ${user.email}", style = MaterialTheme.typography.bodySmall, color = semantic.textMuted)
                                    Text("${user.mobile} • Joined ${formatDate(user.joinedAtMillis)}", style = MaterialTheme.typography.labelSmall, color = semantic.textMuted)
                                }
                                StatusPill(text = user.planStatus, tone = invoiceStatusTone(user.planStatus))
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf("ACTIVE", "TRIAL", "EXPIRED").forEach { st ->
                                    if (user.planStatus != st) {
                                        OutlinedButton(
                                            onClick = { onUpdateUserPlan(user, st) },
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                        ) {
                                            Text("Set $st", style = MaterialTheme.typography.labelSmall)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            "Payments" -> {
                items(subscriptionPayments, key = { it.id }) { pay ->
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
                            Column {
                                Text(pay.planName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                Text("${pay.method} • ${pay.transactionId}", style = MaterialTheme.typography.labelSmall, color = semantic.textMuted)
                            }
                            Text("₹${pay.amountInr}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = semantic.success)
                        }
                    }
                }
            }
            else -> {
                item {
                    Card(
                        shape = MaterialTheme.shapes.large,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, semantic.cardBorder),
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("Admin Platform Settings", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("Configure the official WhatsApp Support link shown on Screen 15 (Support).", style = MaterialTheme.typography.bodySmall, color = semantic.textMuted)
                            OutlinedTextField(
                                value = whatsappUrl,
                                onValueChange = { whatsappUrl = it },
                                label = { Text("WhatsApp Support URL") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().testTag("admin_whatsapp_url_input"),
                            )
                            Button(
                                onClick = { onUpdateWhatsappUrl(whatsappUrl.trim()) },
                                modifier = Modifier.fillMaxWidth().height(48.dp).testTag("admin_save_whatsapp_btn"),
                            ) {
                                Text("Save Support Link")
                            }
                        }
                    }
                }
            }
        }
    }
}
