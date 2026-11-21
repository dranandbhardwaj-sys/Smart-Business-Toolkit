package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.print.PrintManager
import androidx.core.content.FileProvider
import com.example.data.local.BusinessProfileEntity
import com.example.data.local.InvoiceEntity
import com.example.data.local.InvoiceLineItem
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

object ExportUtils {

    private fun ensureExportDir(context: Context): File {
        val dir = File(context.cacheDir, "exports")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    fun generateInvoicePdf(
        context: Context,
        profile: BusinessProfileEntity,
        invoice: InvoiceEntity,
        items: List<InvoiceLineItem>,
    ): File {
        val doc = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = doc.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        val headerBgPaint = Paint().apply { color = Color.parseColor("#059669") }
        val titlePaint = Paint().apply {
            color = Color.WHITE
            textSize = 20f
            typeface = Typeface.DEFAULT_BOLD
        }
        val subtitlePaint = Paint().apply {
            color = Color.parseColor("#ECFDF5")
            textSize = 11f
        }
        val darkPaint = Paint().apply {
            color = Color.parseColor("#0F172A")
            textSize = 12f
        }
        val boldPaint = Paint().apply {
            color = Color.parseColor("#0F172A")
            textSize = 12f
            typeface = Typeface.DEFAULT_BOLD
        }
        val mutedPaint = Paint().apply {
            color = Color.parseColor("#475569")
            textSize = 11f
        }
        val linePaint = Paint().apply {
            color = Color.parseColor("#CBD5E1")
            strokeWidth = 1f
        }
        val tableHeaderBg = Paint().apply { color = Color.parseColor("#F1F5F9") }

        // Header banner
        canvas.drawRect(0f, 0f, 595f, 108f, headerBgPaint)
        canvas.drawText(profile.businessName.ifBlank { "Smart Business Store" }, 32f, 42f, titlePaint)
        canvas.drawText(
            "GSTIN: ${profile.gstin.ifBlank { "Unregistered" }}  |  Ph: ${profile.mobile}",
            32f,
            64f,
            subtitlePaint
        )
        canvas.drawText(profile.address.ifBlank { "India" }, 32f, 82f, subtitlePaint)

        val rightTitle = Paint(titlePaint).apply { textAlign = Paint.Align.RIGHT }
        val rightSub = Paint(subtitlePaint).apply { textAlign = Paint.Align.RIGHT }
        canvas.drawText("TAX INVOICE", 563f, 42f, rightTitle)
        canvas.drawText(invoice.invoiceNumber, 563f, 64f, rightSub)
        canvas.drawText("Date: ${formatDate(invoice.createdAt)}", 563f, 82f, rightSub)

        // Bill To
        var y = 142f
        canvas.drawText("BILL TO:", 32f, y, mutedPaint)
        y += 18f
        canvas.drawText(invoice.customerName, 32f, y, boldPaint)
        y += 16f
        canvas.drawText("Phone: ${invoice.customerPhone}", 32f, y, mutedPaint)
        canvas.drawText("Status: ${invoice.status}", 420f, y, boldPaint)

        // Table Header
        y += 28f
        canvas.drawRect(32f, y, 563f, y + 26f, tableHeaderBg)
        canvas.drawText("Item", 42f, y + 17f, boldPaint)
        canvas.drawText("Qty", 290f, y + 17f, boldPaint)
        canvas.drawText("Rate", 360f, y + 17f, boldPaint)
        canvas.drawText("GST", 440f, y + 17f, boldPaint)
        canvas.drawText("Total", 505f, y + 17f, boldPaint)
        y += 38f

        items.forEach { item ->
            val lineBase = item.qty * item.unitPrice
            val lineTotal = lineBase + (lineBase * item.gstPercent / 100.0)
            canvas.drawText(item.name.take(32), 42f, y, darkPaint)
            canvas.drawText(item.qty.toString(), 295f, y, darkPaint)
            canvas.drawText(formatInr(item.unitPrice), 360f, y, darkPaint)
            canvas.drawText("${item.gstPercent.toInt()}%", 442f, y, darkPaint)
            canvas.drawText(formatInr(lineTotal), 505f, y, boldPaint)
            y += 22f
            canvas.drawLine(32f, y - 6f, 563f, y - 6f, linePaint)
        }

        // Totals
        y += 18f
        canvas.drawText("Subtotal:", 380f, y, mutedPaint)
        canvas.drawText(formatInr(invoice.subtotal), 505f, y, darkPaint)
        y += 18f
        canvas.drawText("Discount:", 380f, y, mutedPaint)
        canvas.drawText("- ${formatInr(invoice.discountAmount)}", 505f, y, darkPaint)
        y += 18f
        canvas.drawText("GST (CGST + SGST):", 380f, y, mutedPaint)
        canvas.drawText(formatInr(invoice.gstAmount), 505f, y, darkPaint)
        y += 22f
        canvas.drawLine(375f, y - 10f, 563f, y - 10f, linePaint)
        canvas.drawText("GRAND TOTAL:", 380f, y + 8f, boldPaint)
        canvas.drawText(formatInr(invoice.totalAmount), 505f, y + 8f, boldPaint)

        // UPI Footer
        y += 50f
        canvas.drawText("UPI ID: ${profile.upiId.ifBlank { "business@upi" }}", 32f, y, boldPaint)
        y += 16f
        canvas.drawText("Thank you for your business! Generated by Smart Business Toolkit.", 32f, y, mutedPaint)

        doc.finishPage(page)
        val outFile = File(ensureExportDir(context), "${invoice.invoiceNumber.replace("/", "-")}.pdf")
        FileOutputStream(outFile).use { doc.writeTo(it) }
        doc.close()
        return outFile
    }

    fun generateReportPdf(
        context: Context,
        title: String,
        periodLabel: String,
        rows: List<Pair<String, String>>,
    ): File {
        val doc = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = doc.startPage(pageInfo)
        val canvas = page.canvas

        val headerBg = Paint().apply { color = Color.parseColor("#059669") }
        val titlePaint = Paint().apply {
            color = Color.WHITE
            textSize = 20f
            typeface = Typeface.DEFAULT_BOLD
        }
        val subPaint = Paint().apply {
            color = Color.parseColor("#ECFDF5")
            textSize = 12f
        }
        val keyPaint = Paint().apply {
            color = Color.parseColor("#475569")
            textSize = 13f
        }
        val valPaint = Paint().apply {
            color = Color.parseColor("#0F172A")
            textSize = 13f
            typeface = Typeface.DEFAULT_BOLD
        }
        val linePaint = Paint().apply {
            color = Color.parseColor("#E2E8F0")
            strokeWidth = 1f
        }

        canvas.drawRect(0f, 0f, 595f, 96f, headerBg)
        canvas.drawText("Smart Business Toolkit - $title", 32f, 44f, titlePaint)
        canvas.drawText("Period: $periodLabel  |  Generated: ${formatDate(System.currentTimeMillis())}", 32f, 70f, subPaint)

        var y = 140f
        rows.forEach { (label, value) ->
            canvas.drawText(label, 36f, y, keyPaint)
            canvas.drawText(value, 360f, y, valPaint)
            y += 28f
            canvas.drawLine(36f, y - 10f, 559f, y - 10f, linePaint)
        }

        doc.finishPage(page)
        val outFile = File(ensureExportDir(context), "report_${title.lowercase().replace(" ", "_")}.pdf")
        FileOutputStream(outFile).use { doc.writeTo(it) }
        doc.close()
        return outFile
    }

    fun generateCsvFile(
        context: Context,
        fileName: String,
        headers: List<String>,
        rows: List<List<String>>,
    ): File {
        val outFile = File(ensureExportDir(context), fileName)
        val sb = StringBuilder()
        sb.appendLine(headers.joinToString(",") { "\"${it.replace("\"", "\"\"")}\"" })
        rows.forEach { row ->
            sb.appendLine(row.joinToString(",") { "\"${it.replace("\"", "\"\"")}\"" })
        }
        outFile.writeText(sb.toString())
        return outFile
    }

    fun shareFile(context: Context, file: File, mimeType: String, title: String) {
        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, title)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, title))
    }

    fun shareInvoiceOnWhatsApp(
        context: Context,
        profile: BusinessProfileEntity,
        invoice: InvoiceEntity,
        pdfFile: File,
    ) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", pdfFile)
        val message = buildString {
            appendLine("*${profile.businessName}*")
            appendLine("Invoice: *${invoice.invoiceNumber}*")
            appendLine("Customer: ${invoice.customerName}")
            appendLine("Amount: *${formatInr(invoice.totalAmount)}*")
            appendLine("Status: ${invoice.status}")
            if (profile.upiId.isNotBlank()) {
                appendLine("Pay via UPI: ${profile.upiId}")
            }
            append("Thank you for your business!")
        }
        val waIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            `package` = "com.whatsapp"
            putExtra(Intent.EXTRA_TEXT, message)
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        try {
            context.startActivity(waIntent)
        } catch (_: Exception) {
            val fallback = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_TEXT, message)
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(fallback, "Share Invoice"))
        }
    }

    fun printPdfFile(context: Context, file: File, jobName: String) {
        val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager ?: return
        val adapter = object : PrintDocumentAdapter() {
            override fun onLayout(
                oldAttributes: PrintAttributes?,
                newAttributes: PrintAttributes?,
                cancellationSignal: CancellationSignal?,
                callback: LayoutResultCallback?,
                extras: Bundle?,
            ) {
                if (cancellationSignal?.isCanceled == true) {
                    callback?.onLayoutCancelled()
                    return
                }
                val info = PrintDocumentInfo.Builder(file.name)
                    .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                    .setPageCount(1)
                    .build()
                callback?.onLayoutFinished(info, true)
            }

            override fun onWrite(
                pages: Array<out PageRange>?,
                destination: ParcelFileDescriptor?,
                cancellationSignal: CancellationSignal?,
                callback: WriteResultCallback?,
            ) {
                try {
                    FileInputStream(file).use { input ->
                        FileOutputStream(destination?.fileDescriptor).use { output ->
                            input.copyTo(output)
                        }
                    }
                    callback?.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
                } catch (e: Exception) {
                    callback?.onWriteFailed(e.message)
                }
            }
        }
        printManager.print(jobName, adapter, null)
    }

    fun openExternalUrl(context: Context, url: String) {
        val safeUrl = if (url.startsWith("http://") || url.startsWith("https://")) url else "https://$url"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(safeUrl))
        runCatching { context.startActivity(intent) }
    }
}
