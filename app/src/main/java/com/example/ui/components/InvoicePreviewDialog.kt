package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Print
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.local.AppDatabase
import com.example.data.local.BusinessProfileEntity
import com.example.data.local.InvoiceEntity
import com.example.ui.theme.LocalSaaSColors
import com.example.ui.theme.MonoFontFamily
import com.example.util.ExportUtils
import com.example.util.formatDate
import com.example.util.formatInr

@Composable
fun IndianInvoicePreviewDialog(
    invoice: InvoiceEntity,
    profile: BusinessProfileEntity,
    onDismiss: () -> Unit,
    onActionFeedback: (String) -> Unit,
) {
    val context = LocalContext.current
    val items = AppDatabase.decodeLineItems(invoice.itemsJson)
    val semantic = LocalSaaSColors.current

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .padding(vertical = 16.dp),
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 4.dp,
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(18.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "GST Tax Invoice Preview",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Rounded.Close, contentDescription = "Close preview")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Authentic Indian Tax Invoice Sheet
                Surface(
                    shape = MaterialTheme.shapes.large,
                    border = BorderStroke(1.dp, semantic.cardBorder),
                    color = MaterialTheme.colorScheme.background,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column {
                        // Top Emerald Header
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.primary)
                                .padding(16.dp),
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = profile.businessName,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                    )
                                    if (profile.gstRegistered && profile.gstin.isNotBlank()) {
                                        Text(
                                            text = "GSTIN: ${profile.gstin}",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontFamily = MonoFontFamily,
                                            color = Color.White.copy(alpha = 0.9f),
                                        )
                                    }
                                    Text(
                                        text = profile.address,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White.copy(alpha = 0.85f),
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "TAX INVOICE",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                    )
                                    Text(
                                        text = invoice.invoiceNumber,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontFamily = MonoFontFamily,
                                        color = Color.White,
                                    )
                                    Text(
                                        text = formatDate(invoice.createdAt),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White.copy(alpha = 0.85f),
                                    )
                                }
                            }
                        }

                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column {
                                    Text("BILL TO", style = MaterialTheme.typography.labelSmall, color = semantic.textMuted)
                                    Text(invoice.customerName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                    Text(invoice.customerPhone, style = MaterialTheme.typography.bodySmall, color = semantic.textMuted)
                                }
                                StatusPill(text = invoice.status, tone = invoiceStatusTone(invoice.status))
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider(color = semantic.cardBorder)
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(modifier = Modifier.fillMaxWidth()) {
                                Text("Item", Modifier.weight(1.8f), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                Text("Qty", Modifier.weight(0.5f), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, textAlign = TextAlign.End)
                                Text("GST", Modifier.weight(0.6f), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, textAlign = TextAlign.End)
                                Text("Amount", Modifier.weight(1f), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, textAlign = TextAlign.End)
                            }
                            Spacer(modifier = Modifier.height(6.dp))

                            items.forEach { item ->
                                val lineAmount = item.qty * item.unitPrice * (1 + item.gstPercent / 100.0)
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Column(Modifier.weight(1.8f)) {
                                        Text(item.name, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                                        Text("${formatInr(item.unitPrice)} / unit", style = MaterialTheme.typography.labelSmall, color = semantic.textMuted)
                                    }
                                    Text("${item.qty}", Modifier.weight(0.5f), style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.End)
                                    Text("${item.gstPercent.toInt()}%", Modifier.weight(0.6f), style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.End)
                                    Text(formatInr(lineAmount), Modifier.weight(1f), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.End)
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            HorizontalDivider(color = semantic.cardBorder)
                            Spacer(modifier = Modifier.height(8.dp))

                            InvoiceSummaryRow("Subtotal", formatInr(invoice.subtotal))
                            InvoiceSummaryRow("Discount", "- ${formatInr(invoice.discountAmount)}")
                            InvoiceSummaryRow("CGST + SGST", formatInr(invoice.gstAmount))
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text("Grand Total", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text(
                                    text = formatInr(invoice.totalAmount),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            // UPI QR Payment Footer
                            Surface(
                                color = MaterialTheme.colorScheme.surface,
                                shape = MaterialTheme.shapes.medium,
                                border = BorderStroke(1.dp, semantic.cardBorder),
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    MiniUpiQrCode(modifier = Modifier.size(52.dp))
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text("Pay via UPI / BharatQR", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                                        Text(profile.upiId, style = MaterialTheme.typography.bodySmall, fontFamily = MonoFontFamily, color = MaterialTheme.colorScheme.primary)
                                        Text("Paid: ${formatInr(invoice.paidAmount)}", style = MaterialTheme.typography.labelSmall, color = semantic.textMuted)
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Real Action Buttons: PDF, Print, WhatsApp
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    OutlinedButton(
                        onClick = {
                            val pdf = ExportUtils.generateInvoicePdf(context, profile, invoice, items)
                            ExportUtils.shareFile(context, pdf, "application/pdf", "Download / Share ${invoice.invoiceNumber}")
                            onActionFeedback("PDF generated: ${pdf.name}")
                        },
                        modifier = Modifier.weight(1f).testTag("invoice_download_pdf_btn"),
                    ) {
                        Icon(Icons.Rounded.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("PDF", maxLines = 1)
                    }
                    OutlinedButton(
                        onClick = {
                            val pdf = ExportUtils.generateInvoicePdf(context, profile, invoice, items)
                            ExportUtils.printPdfFile(context, pdf, invoice.invoiceNumber)
                            onActionFeedback("Sent ${invoice.invoiceNumber} to Print Manager")
                        },
                        modifier = Modifier.weight(1f).testTag("invoice_print_btn"),
                    ) {
                        Icon(Icons.Rounded.Print, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Print", maxLines = 1)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        val pdf = ExportUtils.generateInvoicePdf(context, profile, invoice, items)
                        ExportUtils.shareInvoiceOnWhatsApp(context, profile, invoice, pdf)
                        onActionFeedback("Sharing ${invoice.invoiceNumber} via WhatsApp")
                    },
                    modifier = Modifier.fillMaxWidth().testTag("invoice_whatsapp_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                ) {
                    Icon(Icons.Rounded.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Share on WhatsApp")
                }
            }
        }
    }
}

@Composable
private fun InvoiceSummaryRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = LocalSaaSColors.current.textMuted)
        Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun MiniUpiQrCode(modifier: Modifier = Modifier) {
    val fg = MaterialTheme.colorScheme.onSurface
    val bg = MaterialTheme.colorScheme.surface
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(6.dp),
        border = BorderStroke(1.dp, LocalSaaSColors.current.cardBorder),
        color = bg,
    ) {
        Canvas(modifier = Modifier.padding(4.dp)) {
            val grid = 7
            val cell = size.width / grid
            val pattern = listOf(
                listOf(1, 1, 1, 0, 1, 1, 1),
                listOf(1, 0, 1, 1, 1, 0, 1),
                listOf(1, 1, 1, 0, 1, 1, 1),
                listOf(0, 1, 0, 1, 0, 1, 0),
                listOf(1, 1, 1, 1, 0, 1, 1),
                listOf(1, 0, 1, 0, 1, 0, 1),
                listOf(1, 1, 1, 0, 1, 1, 1),
            )
            pattern.forEachIndexed { r, row ->
                row.forEachIndexed { c, bit ->
                    if (bit == 1) {
                        drawRect(
                            color = fg,
                            topLeft = Offset(c * cell, r * cell),
                            size = Size(cell * 0.9f, cell * 0.9f),
                        )
                    }
                }
            }
        }
    }
}
