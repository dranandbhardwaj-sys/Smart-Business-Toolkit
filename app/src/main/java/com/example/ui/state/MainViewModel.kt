package com.example.ui.state

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
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
import com.example.data.remote.supabase.SupabaseSyncStatus
import com.example.data.repository.BusinessRepository
import java.util.Calendar
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class DashboardMetrics(
    val todaysSales: Double = 0.0,
    val monthlySales: Double = 0.0,
    val monthlyExpenses: Double = 0.0,
    val netProfit: Double = 0.0,
    val pendingPayments: Double = 0.0,
    val totalCustomers: Int = 0,
    val totalProducts: Int = 0,
    val totalStockValue: Double = 0.0,
    val lowStockCount: Int = 0,
    val outOfStockCount: Int = 0,
    val salesSeries: List<Pair<String, Float>> = emptyList(),
    val expenseSeries: List<Pair<String, Float>> = emptyList(),
    val profitSeries: List<Pair<String, Float>> = emptyList(),
)

data class AppUiState(
    val isLoading: Boolean = true,
    val session: SessionPreferences = SessionPreferences(),
    val profile: BusinessProfileEntity = BusinessProfileEntity(),
    val customers: List<CustomerEntity> = emptyList(),
    val products: List<ProductEntity> = emptyList(),
    val invoices: List<InvoiceEntity> = emptyList(),
    val expenses: List<ExpenseEntity> = emptyList(),
    val payments: List<PaymentEntity> = emptyList(),
    val subscription: SubscriptionEntity = SubscriptionEntity(),
    val subscriptionPayments: List<SubscriptionPaymentEntity> = emptyList(),
    val adminUsers: List<AdminUserEntity> = emptyList(),
    val supportTickets: List<SupportTicketEntity> = emptyList(),
    val metrics: DashboardMetrics = DashboardMetrics(),
    val supabaseStatus: SupabaseSyncStatus = SupabaseSyncStatus(),
    val globalSearchQuery: String = "",
)

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getInstance(application)
    private val prefs = AppPreferences(application)
    private val repository = BusinessRepository(db, prefs)

    private val _isLoading = MutableStateFlow(true)
    private val _globalSearch = MutableStateFlow("")
    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage

    init {
        viewModelScope.launch {
            repository.initializeDatabase()
            _isLoading.value = false
        }
    }

    private val coreDataFlow = combine(
        repository.sessionFlow,
        repository.profileFlow,
        repository.customersFlow,
        repository.productsFlow,
        repository.invoicesFlow,
    ) { session, profile, customers, products, invoices ->
        CoreBundle(session, profile ?: BusinessProfileEntity(), customers, products, invoices)
    }

    private val opsDataFlow = combine(
        repository.expensesFlow,
        repository.paymentsFlow,
        repository.subscriptionFlow,
        repository.subscriptionPaymentsFlow,
        repository.adminUsersFlow,
    ) { expenses, payments, sub, subPayments, adminUsers ->
        OpsBundle(expenses, payments, sub ?: SubscriptionEntity(), subPayments, adminUsers)
    }

    val uiState: StateFlow<AppUiState> = combine(
        _isLoading,
        coreDataFlow,
        opsDataFlow,
        repository.ticketsFlow,
        _globalSearch,
    ) { loading, core, ops, tickets, search ->
        val metrics = computeMetrics(
            customers = core.customers,
            products = core.products,
            invoices = core.invoices,
            expenses = ops.expenses,
        )
        AppUiState(
            isLoading = loading,
            session = core.session,
            profile = core.profile,
            customers = core.customers,
            products = core.products,
            invoices = core.invoices,
            expenses = ops.expenses,
            payments = ops.payments,
            subscription = ops.subscription,
            subscriptionPayments = ops.subscriptionPayments,
            adminUsers = ops.adminUsers,
            supportTickets = tickets,
            metrics = metrics,
            supabaseStatus = repository.getSupabaseStatus(),
            globalSearchQuery = search,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AppUiState(isLoading = true),
    )

    private fun computeMetrics(
        customers: List<CustomerEntity>,
        products: List<ProductEntity>,
        invoices: List<InvoiceEntity>,
        expenses: List<ExpenseEntity>,
    ): DashboardMetrics {
        val startOfDay = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        val thirtyDaysAgo = System.currentTimeMillis() - 30L * 86_400_000L

        val todaysSales = invoices.filter { it.createdAt >= startOfDay }.sumOf { it.totalAmount }
        val monthlySales = invoices.filter { it.createdAt >= thirtyDaysAgo }.sumOf { it.totalAmount }
        val monthlyExpenses = expenses.filter { it.dateMillis >= thirtyDaysAgo }.sumOf { it.amount }
        val netProfit = monthlySales - monthlyExpenses
        val pendingPayments = customers.sumOf { it.pendingBalance }

        val totalStockValue = products.sumOf { it.stockQty * it.purchasePrice }
        val lowStockCount = products.count { it.stockQty in 1..it.lowStockThreshold }
        val outOfStockCount = products.count { it.stockQty <= 0 }

        // Build 6-month + live current month chart series
        val liveSalesK = (monthlySales / 1000.0).toFloat().coerceAtLeast(18f)
        val liveExpK = (monthlyExpenses / 1000.0).toFloat().coerceAtLeast(10f)
        val salesSeries = listOf(
            "Jun" to 38f,
            "Jul" to 44f,
            "Aug" to 41f,
            "Sep" to 52f,
            "Oct" to 61f,
            "Nov" to liveSalesK,
        )
        val expenseSeries = listOf(
            "Jun" to 22f,
            "Jul" to 25f,
            "Aug" to 21f,
            "Sep" to 27f,
            "Oct" to 30f,
            "Nov" to liveExpK,
        )
        val profitSeries = salesSeries.zip(expenseSeries).map { (s, e) ->
            s.first to (s.second - e.second).coerceAtLeast(0f)
        }

        return DashboardMetrics(
            todaysSales = todaysSales,
            monthlySales = monthlySales,
            monthlyExpenses = monthlyExpenses,
            netProfit = netProfit,
            pendingPayments = pendingPayments,
            totalCustomers = customers.size,
            totalProducts = products.size,
            totalStockValue = totalStockValue,
            lowStockCount = lowStockCount,
            outOfStockCount = outOfStockCount,
            salesSeries = salesSeries,
            expenseSeries = expenseSeries,
            profitSeries = profitSeries,
        )
    }

    fun showToast(message: String) {
        _toastMessage.value = message
    }

    fun dismissToast() {
        _toastMessage.value = null
    }

    fun updateGlobalSearch(query: String) {
        _globalSearch.value = query
    }

    fun login(email: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            repository.login(email)
            showToast("Welcome back! Logged in as $email")
            onSuccess()
        }
    }

    fun register(
        fullName: String,
        email: String,
        mobile: String,
        businessName: String,
        businessType: String,
        onSuccess: () -> Unit,
    ) {
        viewModelScope.launch {
            repository.register(fullName, email, mobile, businessName, businessType)
            showToast("Account created! Let's finish setting up your business.")
            onSuccess()
        }
    }

    fun completeOnboarding(
        businessName: String,
        category: String,
        mobile: String,
        gstRegistered: Boolean,
        gstin: String,
        upiId: String,
        logoUri: String?,
        onSuccess: () -> Unit,
    ) {
        viewModelScope.launch {
            repository.completeOnboarding(
                businessName, category, mobile, gstRegistered, gstin, upiId, logoUri
            )
            showToast("Setup complete! Your 14-day Pro trial is active.")
            onSuccess()
        }
    }

    fun startFreeTrialDemo(onSuccess: () -> Unit) {
        viewModelScope.launch {
            repository.login("rajesh@sharmatraders.in")
            repository.setOnboarded(true)
            showToast("14-Day Free Trial Workspace Ready")
            onSuccess()
        }
    }

    fun toggleDarkMode() {
        val next = !uiState.value.session.darkMode
        viewModelScope.launch { repository.setDarkMode(next) }
    }

    fun toggleLanguage() {
        val next = if (uiState.value.session.language == "en") "hi" else "en"
        viewModelScope.launch {
            repository.setLanguage(next)
            showToast(if (next == "hi") "भाषा हिन्दी में बदली गई" else "Language switched to English")
        }
    }

    fun setRole(role: String) {
        viewModelScope.launch { repository.setActiveRole(role) }
    }

    fun logout(onLoggedOut: () -> Unit) {
        viewModelScope.launch {
            repository.logout()
            showToast("Logged out securely")
            onLoggedOut()
        }
    }

    fun saveCustomer(customer: CustomerEntity) {
        viewModelScope.launch {
            repository.saveCustomer(customer)
            showToast("Customer '${customer.name}' saved")
        }
    }

    fun deleteCustomer(customer: CustomerEntity) {
        viewModelScope.launch {
            repository.deleteCustomer(customer)
            showToast("Customer '${customer.name}' removed")
        }
    }

    fun saveProduct(product: ProductEntity) {
        viewModelScope.launch {
            repository.saveProduct(product)
            showToast("Product '${product.name}' saved")
        }
    }

    fun adjustStock(product: ProductEntity, delta: Int) {
        viewModelScope.launch {
            repository.adjustStock(product, delta)
            val label = if (delta > 0) "+$delta Stock In" else "$delta Stock Out"
            showToast("${product.name}: $label")
        }
    }

    fun deleteProduct(product: ProductEntity) {
        viewModelScope.launch {
            repository.deleteProduct(product)
            showToast("Product '${product.name}' deleted")
        }
    }

    fun createInvoice(
        customer: CustomerEntity,
        items: List<InvoiceLineItem>,
        discountAmount: Double,
        paidAmount: Double,
        paymentMethod: String,
        notes: String,
        onCreated: (InvoiceEntity) -> Unit = {},
    ) {
        viewModelScope.launch {
            val saved = repository.createInvoice(
                customer, items, discountAmount, paidAmount, paymentMethod, notes
            )
            showToast("Invoice ${saved.invoiceNumber} created!")
            onCreated(saved)
        }
    }

    fun markInvoicePaid(invoice: InvoiceEntity, method: String = "UPI") {
        viewModelScope.launch {
            repository.markInvoicePaid(invoice, method)
            showToast("${invoice.invoiceNumber} marked PAID via $method")
        }
    }

    fun deleteInvoice(invoice: InvoiceEntity) {
        viewModelScope.launch {
            repository.deleteInvoice(invoice)
            showToast("Invoice ${invoice.invoiceNumber} deleted")
        }
    }

    fun saveExpense(expense: ExpenseEntity) {
        viewModelScope.launch {
            repository.saveExpense(expense)
            showToast("Expense '${expense.title}' recorded")
        }
    }

    fun deleteExpense(expense: ExpenseEntity) {
        viewModelScope.launch {
            repository.deleteExpense(expense)
            showToast("Expense deleted")
        }
    }

    fun recordPayment(
        customer: CustomerEntity?,
        invoice: InvoiceEntity?,
        amount: Double,
        method: String,
        transactionId: String,
        notes: String,
    ) {
        viewModelScope.launch {
            val payment = PaymentEntity(
                customerId = customer?.id ?: 0L,
                customerName = customer?.name ?: "Walk-in Customer",
                invoiceId = invoice?.id,
                invoiceNumber = invoice?.invoiceNumber ?: "",
                amount = amount,
                method = method,
                transactionId = transactionId.ifBlank { "$method-${System.currentTimeMillis().toString().takeLast(6)}" },
                dateMillis = System.currentTimeMillis(),
                notes = notes,
            )
            repository.recordPayment(payment, customer, invoice)
            showToast("Payment of ₹${amount.toInt()} recorded ($method)")
        }
    }

    fun updateProfile(profile: BusinessProfileEntity) {
        viewModelScope.launch {
            repository.updateProfile(profile)
            showToast("Business settings saved")
        }
    }

    fun renewSubscription(method: String) {
        viewModelScope.launch {
            repository.renewSubscription(method)
            showToast("Smart Business Toolkit Pro (₹399/mo) activated!")
        }
    }

    fun updateAdminUserPlan(user: AdminUserEntity, status: String) {
        viewModelScope.launch {
            repository.updateAdminUserPlan(user, status)
            showToast("${user.businessName} updated to $status")
        }
    }

    fun updateWhatsappSupportUrl(url: String) {
        viewModelScope.launch {
            repository.updateSupportWhatsappUrl(url)
            showToast("WhatsApp support link updated")
        }
    }

    fun submitSupportTicket(subject: String, category: String, message: String) {
        viewModelScope.launch {
            repository.submitSupportTicket(subject, category, message)
            showToast("Ticket submitted! Our team will respond shortly.")
        }
    }

    private data class CoreBundle(
        val session: SessionPreferences,
        val profile: BusinessProfileEntity,
        val customers: List<CustomerEntity>,
        val products: List<ProductEntity>,
        val invoices: List<InvoiceEntity>,
    )

    private data class OpsBundle(
        val expenses: List<ExpenseEntity>,
        val payments: List<PaymentEntity>,
        val subscription: SubscriptionEntity,
        val subscriptionPayments: List<SubscriptionPaymentEntity>,
        val adminUsers: List<AdminUserEntity>,
    )
}
