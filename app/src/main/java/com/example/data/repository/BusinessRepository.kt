package com.example.data.repository

import com.example.data.local.AdminUserEntity
import com.example.data.local.AppDatabase
import com.example.data.local.AppPreferences
import com.example.data.local.BusinessProfileEntity
import com.example.data.local.CustomerEntity
import com.example.data.local.ExpenseEntity
import com.example.data.local.InvoiceEntity
import com.example.data.local.InvoiceLineItem
import com.example.data.local.PaymentEntity
import com.example.data.local.ProductEntity
import com.example.data.local.SessionPreferences
import com.example.data.local.SubscriptionEntity
import com.example.data.local.SubscriptionPaymentEntity
import com.example.data.local.SupportTicketEntity
import com.example.data.remote.supabase.LocalNoOpSupabaseAdapter
import com.example.data.remote.supabase.SupabaseSyncAdapter
import com.example.data.remote.supabase.SupabaseSyncStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class BusinessRepository(
    private val db: AppDatabase,
    private val prefs: AppPreferences,
    private val supabaseAdapter: SupabaseSyncAdapter = LocalNoOpSupabaseAdapter(),
) {
    val sessionFlow: Flow<SessionPreferences> = prefs.sessionFlow
    val profileFlow: Flow<BusinessProfileEntity?> = db.businessProfileDao().observeProfile()
    val customersFlow: Flow<List<CustomerEntity>> = db.customerDao().observeCustomers()
    val productsFlow: Flow<List<ProductEntity>> = db.productDao().observeProducts()
    val invoicesFlow: Flow<List<InvoiceEntity>> = db.invoiceDao().observeInvoices()
    val expensesFlow: Flow<List<ExpenseEntity>> = db.expenseDao().observeExpenses()
    val paymentsFlow: Flow<List<PaymentEntity>> = db.paymentDao().observePayments()
    val subscriptionFlow: Flow<SubscriptionEntity?> = db.subscriptionDao().observeSubscription()
    val subscriptionPaymentsFlow: Flow<List<SubscriptionPaymentEntity>> =
        db.subscriptionDao().observeSubscriptionPayments()
    val adminUsersFlow: Flow<List<AdminUserEntity>> = db.adminUserDao().observeAdminUsers()
    val ticketsFlow: Flow<List<SupportTicketEntity>> = db.supportTicketDao().observeTickets()

    suspend fun initializeDatabase() {
        AppDatabase.seedInitialDataIfEmpty(db)
    }

    fun getSupabaseStatus(): SupabaseSyncStatus = supabaseAdapter.currentStatus()

    // Auth & Session
    suspend fun login(email: String) {
        prefs.setLoggedIn(true, email)
    }

    suspend fun register(
        fullName: String,
        email: String,
        mobile: String,
        businessName: String,
        businessType: String,
    ) {
        val current = db.businessProfileDao().getProfileOnce() ?: BusinessProfileEntity()
        val updated = current.copy(
            ownerName = fullName.trim(),
            email = email.trim(),
            mobile = mobile.trim(),
            businessName = businessName.trim(),
            category = businessType.trim(),
        )
        db.businessProfileDao().upsertProfile(updated)
        supabaseAdapter.pushProfile(updated)
        prefs.setLoggedIn(true, email.trim())
        prefs.setOnboarded(false)
    }

    suspend fun completeOnboarding(
        businessName: String,
        category: String,
        mobile: String,
        gstRegistered: Boolean,
        gstin: String,
        upiId: String,
        logoUri: String?,
    ) {
        val current = db.businessProfileDao().getProfileOnce() ?: BusinessProfileEntity()
        val updated = current.copy(
            businessName = businessName,
            category = category,
            mobile = mobile,
            gstRegistered = gstRegistered,
            gstin = if (gstRegistered) gstin else "",
            upiId = upiId,
            logoUri = logoUri,
        )
        db.businessProfileDao().upsertProfile(updated)
        supabaseAdapter.pushProfile(updated)
        prefs.setOnboarded(true)
    }

    suspend fun updateProfile(profile: BusinessProfileEntity) {
        db.businessProfileDao().upsertProfile(profile)
        supabaseAdapter.pushProfile(profile)
    }

    suspend fun setDarkMode(enabled: Boolean) = prefs.setDarkMode(enabled)
    suspend fun setOnboarded(onboarded: Boolean) = prefs.setOnboarded(onboarded)
    suspend fun setLanguage(language: String) = prefs.setLanguage(language)
    suspend fun setActiveRole(role: String) = prefs.setActiveRole(role)
    suspend fun logout() = prefs.clearSession()

    // Customers
    suspend fun saveCustomer(customer: CustomerEntity): Long {
        val id = db.customerDao().insertCustomer(customer)
        val saved = if (customer.id == 0L) customer.copy(id = id) else customer
        supabaseAdapter.pushCustomer(saved)
        return saved.id
    }

    suspend fun deleteCustomer(customer: CustomerEntity) {
        db.customerDao().deleteCustomer(customer)
    }

    // Products / Inventory
    suspend fun saveProduct(product: ProductEntity): Long {
        val id = db.productDao().insertProduct(product.copy(updatedAt = System.currentTimeMillis()))
        val saved = if (product.id == 0L) product.copy(id = id) else product
        supabaseAdapter.pushProduct(saved)
        return saved.id
    }

    suspend fun adjustStock(product: ProductEntity, delta: Int) {
        val nextQty = (product.stockQty + delta).coerceAtLeast(0)
        val updated = product.copy(stockQty = nextQty, updatedAt = System.currentTimeMillis())
        db.productDao().updateProduct(updated)
        supabaseAdapter.pushProduct(updated)
    }

    suspend fun deleteProduct(product: ProductEntity) {
        db.productDao().deleteProduct(product)
    }

    // Invoices
    suspend fun createInvoice(
        customer: CustomerEntity,
        items: List<InvoiceLineItem>,
        discountAmount: Double,
        paidAmount: Double,
        paymentMethod: String,
        notes: String,
    ): InvoiceEntity {
        val profile = db.businessProfileDao().getProfileOnce() ?: BusinessProfileEntity()
        val count = db.invoiceDao().countInvoices()
        val invoiceNumber = "${profile.invoicePrefix}${1040 + count + 1}"

        val subtotal = items.sumOf { it.qty * it.unitPrice }
        val taxable = (subtotal - discountAmount).coerceAtLeast(0.0)
        val rawSubtotal = subtotal.coerceAtLeast(1.0)
        val gstAmount = items.sumOf { item ->
            val share = (item.qty * item.unitPrice) / rawSubtotal
            (taxable * share) * (item.gstPercent / 100.0)
        }
        val total = taxable + gstAmount
        val clampedPaid = paidAmount.coerceIn(0.0, total)
        val status = when {
            clampedPaid >= total - 0.5 -> "PAID"
            clampedPaid > 0.0 -> "PARTIAL"
            else -> "PENDING"
        }

        val entity = InvoiceEntity(
            invoiceNumber = invoiceNumber,
            customerId = customer.id,
            customerName = customer.name,
            customerPhone = customer.phone,
            itemsJson = AppDatabase.encodeLineItems(items),
            subtotal = subtotal,
            discountAmount = discountAmount,
            gstAmount = gstAmount,
            totalAmount = total,
            paidAmount = clampedPaid,
            status = status,
            paymentMethod = paymentMethod,
            notes = notes,
            createdAt = System.currentTimeMillis(),
        )
        val id = db.invoiceDao().insertInvoice(entity)
        val saved = entity.copy(id = id)
        supabaseAdapter.pushInvoice(saved)

        // Update customer totals
        val pendingDelta = (total - clampedPaid).coerceAtLeast(0.0)
        val updatedCustomer = customer.copy(
            totalPurchases = customer.totalPurchases + total,
            pendingBalance = customer.pendingBalance + pendingDelta,
            lastTransactionAt = System.currentTimeMillis(),
        )
        db.customerDao().updateCustomer(updatedCustomer)

        // Deduct stock for catalog products
        val allProducts = productsFlow.first()
        items.forEach { line ->
            val matched = allProducts.find { it.id == line.productId }
            if (matched != null) {
                adjustStock(matched, -line.qty)
            }
        }

        // Record payment if paidAmount > 0
        if (clampedPaid > 0.0) {
            val txnId = "${paymentMethod}${System.currentTimeMillis().toString().takeLast(8)}"
            val payment = PaymentEntity(
                customerId = customer.id,
                customerName = customer.name,
                invoiceId = id,
                invoiceNumber = invoiceNumber,
                amount = clampedPaid,
                method = paymentMethod,
                transactionId = txnId,
                dateMillis = System.currentTimeMillis(),
                notes = "Invoice counter payment",
            )
            db.paymentDao().insertPayment(payment)
            supabaseAdapter.pushPayment(payment)
        }

        return saved
    }

    suspend fun markInvoicePaid(invoice: InvoiceEntity, method: String = "UPI") {
        val remaining = (invoice.totalAmount - invoice.paidAmount).coerceAtLeast(0.0)
        val updated = invoice.copy(
            paidAmount = invoice.totalAmount,
            status = "PAID",
            paymentMethod = method
        )
        db.invoiceDao().updateInvoice(updated)

        val customers = customersFlow.first()
        customers.find { it.id == invoice.customerId }?.let { cust ->
            db.customerDao().updateCustomer(
                cust.copy(
                    pendingBalance = (cust.pendingBalance - remaining).coerceAtLeast(0.0),
                    lastTransactionAt = System.currentTimeMillis(),
                )
            )
        }

        if (remaining > 0.0) {
            val payment = PaymentEntity(
                customerId = invoice.customerId,
                customerName = invoice.customerName,
                invoiceId = invoice.id,
                invoiceNumber = invoice.invoiceNumber,
                amount = remaining,
                method = method,
                transactionId = "${method}${System.currentTimeMillis().toString().takeLast(8)}",
                dateMillis = System.currentTimeMillis(),
                notes = "Invoice balance settled",
            )
            db.paymentDao().insertPayment(payment)
        }
    }

    suspend fun deleteInvoice(invoice: InvoiceEntity) {
        db.invoiceDao().deleteInvoice(invoice)
    }

    // Expenses
    suspend fun saveExpense(expense: ExpenseEntity): Long {
        val id = db.expenseDao().insertExpense(expense)
        supabaseAdapter.pushExpense(if (expense.id == 0L) expense.copy(id = id) else expense)
        return id
    }

    suspend fun deleteExpense(expense: ExpenseEntity) {
        db.expenseDao().deleteExpense(expense)
    }

    // Payments
    suspend fun recordPayment(payment: PaymentEntity, customer: CustomerEntity?, invoice: InvoiceEntity?) {
        val id = db.paymentDao().insertPayment(payment)
        supabaseAdapter.pushPayment(payment.copy(id = id))

        customer?.let { cust ->
            db.customerDao().updateCustomer(
                cust.copy(
                    pendingBalance = (cust.pendingBalance - payment.amount).coerceAtLeast(0.0),
                    lastTransactionAt = payment.dateMillis,
                )
            )
        }
        invoice?.let { inv ->
            val newPaid = (inv.paidAmount + payment.amount).coerceAtMost(inv.totalAmount)
            val newStatus = if (newPaid >= inv.totalAmount - 0.5) "PAID" else "PARTIAL"
            db.invoiceDao().updateInvoice(inv.copy(paidAmount = newPaid, status = newStatus))
        }
    }

    // Subscription
    suspend fun renewSubscription(method: String) {
        val now = System.currentTimeMillis()
        val nextExpiry = now + 30L * 86_400_000L
        db.subscriptionDao().upsertSubscription(
            SubscriptionEntity(
                id = 1,
                planName = "SMART BUSINESS TOOLKIT PRO",
                priceInr = 399,
                status = "ACTIVE",
                trialDaysLeft = 0,
                expiryDateMillis = nextExpiry,
                autoRenew = true,
            )
        )
        db.subscriptionDao().insertSubscriptionPayment(
            SubscriptionPaymentEntity(
                planName = "SMART BUSINESS TOOLKIT PRO (Monthly)",
                amountInr = 399,
                transactionId = "SBT-${now.toString().takeLast(7)}",
                status = "SUCCESS",
                method = method,
                paidAtMillis = now,
            )
        )
    }

    // Admin
    suspend fun updateAdminUserPlan(user: AdminUserEntity, newStatus: String) {
        val revenue = if (newStatus == "ACTIVE") 399 else 0
        db.adminUserDao().updateAdminUser(
            user.copy(planStatus = newStatus, monthlyRevenueInr = revenue)
        )
    }

    suspend fun updateSupportWhatsappUrl(url: String) {
        val profile = db.businessProfileDao().getProfileOnce() ?: BusinessProfileEntity()
        db.businessProfileDao().upsertProfile(profile.copy(whatsappSupportUrl = url))
    }

    // Support
    suspend fun submitSupportTicket(subject: String, category: String, message: String) {
        db.supportTicketDao().insertTicket(
            SupportTicketEntity(
                subject = subject,
                category = category,
                message = message,
                status = "OPEN",
                createdAtMillis = System.currentTimeMillis(),
            )
        )
    }
}
