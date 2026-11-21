package com.example.data.remote.supabase

import com.example.data.local.BusinessProfileEntity
import com.example.data.local.CustomerEntity
import com.example.data.local.ExpenseEntity
import com.example.data.local.InvoiceEntity
import com.example.data.local.PaymentEntity
import com.example.data.local.ProductEntity

/**
 * Supabase Backend Readiness Layer.
 *
 * Modular contract prepared for drop-in `supabase-kt` (GoTrue + Postgrest + Storage) integration.
 * Replace [LocalNoOpSupabaseAdapter] with a live Postgrest/Auth implementation once project
 * SUPABASE_URL & SUPABASE_ANON_KEY are configured in Secrets.
 */
object SupabaseSchema {
    const val TABLE_PROFILES = "business_profiles"
    const val TABLE_CUSTOMERS = "customers"
    const val TABLE_PRODUCTS = "products"
    const val TABLE_INVOICES = "invoices"
    const val TABLE_EXPENSES = "expenses"
    const val TABLE_PAYMENTS = "payments"
    const val TABLE_SUBSCRIPTIONS = "subscriptions"
    const val BUCKET_LOGOS = "business-logos"
}

data class SupabaseSyncStatus(
    val isConfigured: Boolean = false,
    val providerName: String = "Supabase (Local-First Ready)",
    val lastSyncedAtMillis: Long? = null,
    val pendingQueueCount: Int = 0,
)

data class SupabaseInvoiceDto(
    val invoice_number: String,
    val customer_id: Long,
    val customer_name: String,
    val subtotal: Double,
    val discount_amount: Double,
    val gst_amount: Double,
    val total_amount: Double,
    val paid_amount: Double,
    val status: String,
    val payment_method: String,
    val created_at: Long,
)

fun InvoiceEntity.toSupabaseDto() = SupabaseInvoiceDto(
    invoice_number = invoiceNumber,
    customer_id = customerId,
    customer_name = customerName,
    subtotal = subtotal,
    discount_amount = discountAmount,
    gst_amount = gstAmount,
    total_amount = totalAmount,
    paid_amount = paidAmount,
    status = status,
    payment_method = paymentMethod,
    created_at = createdAt,
)

interface SupabaseSyncAdapter {
    suspend fun pushProfile(profile: BusinessProfileEntity): Result<Unit>
    suspend fun pushCustomer(customer: CustomerEntity): Result<Unit>
    suspend fun pushProduct(product: ProductEntity): Result<Unit>
    suspend fun pushInvoice(invoice: InvoiceEntity): Result<Unit>
    suspend fun pushExpense(expense: ExpenseEntity): Result<Unit>
    suspend fun pushPayment(payment: PaymentEntity): Result<Unit>
    fun currentStatus(): SupabaseSyncStatus
}

class LocalNoOpSupabaseAdapter : SupabaseSyncAdapter {
    private var lastAttemptMillis: Long? = null

    override suspend fun pushProfile(profile: BusinessProfileEntity): Result<Unit> {
        lastAttemptMillis = System.currentTimeMillis()
        return Result.success(Unit)
    }

    override suspend fun pushCustomer(customer: CustomerEntity): Result<Unit> {
        lastAttemptMillis = System.currentTimeMillis()
        return Result.success(Unit)
    }

    override suspend fun pushProduct(product: ProductEntity): Result<Unit> {
        lastAttemptMillis = System.currentTimeMillis()
        return Result.success(Unit)
    }

    override suspend fun pushInvoice(invoice: InvoiceEntity): Result<Unit> {
        lastAttemptMillis = System.currentTimeMillis()
        return Result.success(Unit)
    }

    override suspend fun pushExpense(expense: ExpenseEntity): Result<Unit> {
        lastAttemptMillis = System.currentTimeMillis()
        return Result.success(Unit)
    }

    override suspend fun pushPayment(payment: PaymentEntity): Result<Unit> {
        lastAttemptMillis = System.currentTimeMillis()
        return Result.success(Unit)
    }

    override fun currentStatus(): SupabaseSyncStatus = SupabaseSyncStatus(
        isConfigured = false,
        providerName = "Supabase Ready (Local SQLite Active)",
        lastSyncedAtMillis = lastAttemptMillis,
        pendingQueueCount = 0,
    )
}
