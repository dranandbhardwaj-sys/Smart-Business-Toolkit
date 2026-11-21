package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Chat
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.HelpCenter
import androidx.compose.material.icons.rounded.MailOutline
import androidx.compose.material.icons.rounded.SupportAgent
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.SupportTicketEntity
import com.example.ui.components.StatusPill
import com.example.ui.components.invoiceStatusTone
import com.example.ui.theme.LocalSaaSColors
import com.example.util.ExportUtils
import com.example.util.formatDateTime

@Composable
fun SupportScreen(
    whatsappSupportUrl: String,
    tickets: List<SupportTicketEntity>,
    onSubmitFeedback: (subject: String, category: String, message: String) -> Unit,
) {
    val context = LocalContext.current
    val semantic = LocalSaaSColors.current
    var expandedFaq by remember { mutableIntStateOf(0) }
    var subject by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Billing & GST") }
    var message by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    val faqs = listOf(
        "How do I share an invoice PDF on WhatsApp?" to "Open Invoices, tap any bill to preview, and tap Share on WhatsApp. A formatted Indian GST PDF is generated automatically.",
        "How do I update my GSTIN or UPI QR ID?" to "Go to Settings from the menu, update your GSTIN or UPI ID, and tap Save Business Settings.",
        "Can I track partial payments and customer udhar?" to "Yes! Choose PARTIAL when creating an invoice or record payments in the Payments tab to automatically reduce customer pending balance.",
        "How is my ₹399/month subscription renewed?" to "Visit Pro Plan from the sidebar or header badge and pay via UPI AutoPay, BharatQR, or Card.",
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Text("Help Center & Support", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text("Fast WhatsApp assistance & feedback for Smart Business Toolkit", style = MaterialTheme.typography.bodySmall, color = semantic.textMuted)
        }

        // Quick WhatsApp Support Banner (Admin-Configurable URL)
        item {
            Card(
                shape = MaterialTheme.shapes.extraLarge,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.SupportAgent, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text("Dedicated Merchant Support", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("Mon–Sat • 9:00 AM to 8:00 PM IST", style = MaterialTheme.typography.labelSmall, color = semantic.textMuted)
                        }
                    }
                    Button(
                        onClick = { ExportUtils.openExternalUrl(context, whatsappSupportUrl) },
                        modifier = Modifier.fillMaxWidth().height(48.dp).testTag("support_whatsapp_btn"),
                    ) {
                        Icon(Icons.Rounded.Chat, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Chat on WhatsApp Support", fontWeight = FontWeight.Bold)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.MailOutline, contentDescription = null, tint = semantic.textMuted, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("support@smartbusinesstoolkit.in", style = MaterialTheme.typography.bodySmall, color = semantic.textMuted)
                    }
                }
            }
        }

        // FAQ Accordion
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.HelpCenter, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("Frequently Asked Questions", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
        }

        faqs.forEachIndexed { idx, (q, a) ->
            item {
                val isOpen = expandedFaq == idx
                Card(
                    shape = MaterialTheme.shapes.medium,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, semantic.cardBorder),
                    modifier = Modifier.fillMaxWidth().clickable { expandedFaq = if (isOpen) -1 else idx },
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(q, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                            Icon(if (isOpen) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore, contentDescription = null)
                        }
                        AnimatedVisibility(visible = isOpen) {
                            Text(a, style = MaterialTheme.typography.bodySmall, color = semantic.textMuted, modifier = Modifier.padding(top = 8.dp))
                        }
                    }
                }
            }
        }

        // Contact / Feedback Form
        item {
            Card(
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, semantic.cardBorder),
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Send Feedback or Raise Ticket", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(listOf("Billing & GST", "UPI Payments", "Inventory", "Feature Request")) { cat ->
                            FilterChip(selected = category == cat, onClick = { category = cat }, label = { Text(cat) })
                        }
                    }
                    OutlinedTextField(
                        value = subject,
                        onValueChange = { subject = it; errorMsg = null },
                        label = { Text("Subject*") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("support_subject_input"),
                    )
                    OutlinedTextField(
                        value = message,
                        onValueChange = { message = it; errorMsg = null },
                        label = { Text("Describe your question or feedback*") },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth().testTag("support_message_input"),
                    )
                    if (errorMsg != null) {
                        Text(errorMsg.orEmpty(), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                    Button(
                        onClick = {
                            if (subject.isBlank() || message.isBlank()) {
                                errorMsg = "Please fill both subject and message."
                            } else {
                                onSubmitFeedback(subject.trim(), category, message.trim())
                                subject = ""
                                message = ""
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp).testTag("support_submit_btn"),
                    ) {
                        Text("Submit Ticket", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        if (tickets.isNotEmpty()) {
            item {
                Text("Recent Support Tickets", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            items(tickets, key = { it.id }) { ticket ->
                Card(
                    shape = MaterialTheme.shapes.medium,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, semantic.cardBorder),
                ) {
                    Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(ticket.subject, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                            StatusPill(text = ticket.status, tone = invoiceStatusTone(ticket.status))
                        }
                        Spacer(Modifier.height(4.dp))
                        Text("${ticket.category} • ${formatDateTime(ticket.createdAtMillis)}", style = MaterialTheme.typography.labelSmall, color = semantic.textMuted)
                        Spacer(Modifier.height(4.dp))
                        Text(ticket.message, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}
