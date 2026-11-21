package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

@Database(
    entities = [
        BusinessProfileEntity::class,
        CustomerEntity::class,
        ProductEntity::class,
        InvoiceEntity::class,
        ExpenseEntity::class,
        PaymentEntity::class,
        SubscriptionEntity::class,
        SubscriptionPaymentEntity::class,
        AdminUserEntity::class,
        SupportTicketEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun businessProfileDao(): BusinessProfileDao
    abstract fun customerDao(): CustomerDao
    abstract fun productDao(): ProductDao
    abstract fun invoiceDao(): InvoiceDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun paymentDao(): PaymentDao
    abstract fun subscriptionDao(): SubscriptionDao
    abstract fun adminUserDao(): AdminUserDao
    abstract fun supportTicketDao(): SupportTicketDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val moshi: Moshi = Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()

        private val lineItemsListType = Types.newParameterizedType(
            List::class.java,
            InvoiceLineItem::class.java
        )
        private val lineItemsAdapter = moshi.adapter<List<InvoiceLineItem>>(lineItemsListType)

        fun encodeLineItems(items: List<InvoiceLineItem>): String = lineItemsAdapter.toJson(items)

        fun decodeLineItems(json: String): List<InvoiceLineItem> =
            runCatching { lineItemsAdapter.fromJson(json) }.getOrNull().orEmpty()

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "smart_business_toolkit.db"
                ).build().also { INSTANCE = it }
            }
        }

        suspend fun seedInitialDataIfEmpty(db: AppDatabase) {
            if (db.businessProfileDao().getProfileOnce() != null) return

            val now = System.currentTimeMillis()
            val day = 86_400_000L

            db.businessProfileDao().upsertProfile(BusinessProfileEntity())
            db.subscriptionDao().upsertSubscription(SubscriptionEntity())

            // Customers
            val c1 = db.customerDao().insertCustomer(
                CustomerEntity(
                    name = "Ananya Traders",
                    phone = "+91 98201 44512",
                    email = "accounts@ananyatraders.in",
                    gstin = "27AAACA8891F1Z2",
                    address = "FC Road, Pune",
                    totalPurchases = 48500.0,
                    pendingBalance = 6400.0,
                    lastTransactionAt = now - 3 * 3_600_000L
                )
            )
            val c2 = db.customerDao().insertCustomer(
                CustomerEntity(
                    name = "Verma Hardware & Electricals",
                    phone = "+91 97650 11234",
                    email = "verma.hw@gmail.com",
                    address = "Kothrud, Pune",
                    totalPurchases = 32900.0,
                    pendingBalance = 0.0,
                    lastTransactionAt = now - day
                )
            )
            val c3 = db.customerDao().insertCustomer(
                CustomerEntity(
                    name = "Patel Kirana Mart",
                    phone = "+91 99220 88761",
                    address = "Baner High Street, Pune",
                    totalPurchases = 21450.0,
                    pendingBalance = 4250.0,
                    lastTransactionAt = now - 2 * day
                )
            )
            val c4 = db.customerDao().insertCustomer(
                CustomerEntity(
                    name = "Mehta Interiors",
                    phone = "+91 98111 76230",
                    gstin = "27AABCM9123H1Z8",
                    address = "Viman Nagar, Pune",
                    totalPurchases = 64200.0,
                    pendingBalance = 11800.0,
                    lastTransactionAt = now - 4 * day
                )
            )
            val c5 = db.customerDao().insertCustomer(
                CustomerEntity(
                    name = "Sneha Boutique",
                    phone = "+91 98904 55319",
                    address = "Aundh, Pune",
                    totalPurchases = 14800.0,
                    pendingBalance = 0.0,
                    lastTransactionAt = now - 6 * day
                )
            )

            // Products
            val p1 = db.productDao().insertProduct(
                ProductEntity(
                    name = "LED Smart Bulb 12W Pack",
                    sku = "EL-LED-12W",
                    category = "Lighting",
                    purchasePrice = 320.0,
                    sellingPrice = 499.0,
                    stockQty = 48,
                    lowStockThreshold = 12,
                    gstPercent = 18.0,
                    unit = "box",
                    updatedAt = now - 3600_000L
                )
            )
            val p2 = db.productDao().insertProduct(
                ProductEntity(
                    name = "Copper Extension Board 6A",
                    sku = "EL-EXT-06A",
                    category = "Electricals",
                    purchasePrice = 410.0,
                    sellingPrice = 650.0,
                    stockQty = 7,
                    lowStockThreshold = 10,
                    gstPercent = 18.0,
                    unit = "pcs",
                    updatedAt = now - 2 * 3600_000L
                )
            )
            val p3 = db.productDao().insertProduct(
                ProductEntity(
                    name = "Havells Ceiling Fan 1200mm",
                    sku = "AP-FAN-120",
                    category = "Appliances",
                    purchasePrice = 1850.0,
                    sellingPrice = 2490.0,
                    stockQty = 16,
                    lowStockThreshold = 6,
                    gstPercent = 18.0,
                    unit = "pcs",
                    updatedAt = now - day
                )
            )
            val p4 = db.productDao().insertProduct(
                ProductEntity(
                    name = "Modular 8-Way Switch Plate",
                    sku = "EL-SWP-08M",
                    category = "Switches",
                    purchasePrice = 190.0,
                    sellingPrice = 310.0,
                    stockQty = 4,
                    lowStockThreshold = 15,
                    gstPercent = 18.0,
                    unit = "pcs",
                    updatedAt = now - 2 * day
                )
            )
            val p5 = db.productDao().insertProduct(
                ProductEntity(
                    name = "Wi-Fi Smart Plug 16A",
                    sku = "SM-PLG-16A",
                    category = "Smart Home",
                    purchasePrice = 680.0,
                    sellingPrice = 999.0,
                    stockQty = 0,
                    lowStockThreshold = 8,
                    gstPercent = 18.0,
                    unit = "pcs",
                    updatedAt = now - 3 * day
                )
            )
            val p6 = db.productDao().insertProduct(
                ProductEntity(
                    name = "Premium Cotton Packaging Tape",
                    sku = "PK-TAP-050",
                    category = "Packaging",
                    purchasePrice = 45.0,
                    sellingPrice = 85.0,
                    stockQty = 92,
                    lowStockThreshold = 20,
                    gstPercent = 12.0,
                    unit = "roll",
                    updatedAt = now - 4 * day
                )
            )

            // Invoices
            val inv1Items = listOf(
                InvoiceLineItem(p1, "LED Smart Bulb 12W Pack", "EL-LED-12W", 10, 499.0, 18.0),
                InvoiceLineItem(p3, "Havells Ceiling Fan 1200mm", "AP-FAN-120", 2, 2490.0, 18.0),
            )
            val inv1Sub = 9970.0
            val inv1Gst = 1794.6
            val inv1Total = 11764.6
            val inv1Id = db.invoiceDao().insertInvoice(
                InvoiceEntity(
                    invoiceNumber = "INV-2026-1042",
                    customerId = c1,
                    customerName = "Ananya Traders",
                    customerPhone = "+91 98201 44512",
                    itemsJson = encodeLineItems(inv1Items),
                    subtotal = inv1Sub,
                    discountAmount = 0.0,
                    gstAmount = inv1Gst,
                    totalAmount = inv1Total,
                    paidAmount = 5364.6,
                    status = "PARTIAL",
                    paymentMethod = "UPI",
                    notes = "Balance due within 7 days",
                    createdAt = now - 3 * 3_600_000L
                )
            )

            val inv2Items = listOf(
                InvoiceLineItem(p3, "Havells Ceiling Fan 1200mm", "AP-FAN-120", 4, 2490.0, 18.0),
                InvoiceLineItem(p2, "Copper Extension Board 6A", "EL-EXT-06A", 5, 650.0, 18.0),
            )
            val inv2Id = db.invoiceDao().insertInvoice(
                InvoiceEntity(
                    invoiceNumber = "INV-2026-1041",
                    customerId = c2,
                    customerName = "Verma Hardware & Electricals",
                    customerPhone = "+91 97650 11234",
                    itemsJson = encodeLineItems(inv2Items),
                    subtotal = 13210.0,
                    discountAmount = 210.0,
                    gstAmount = 2340.0,
                    totalAmount = 15340.0,
                    paidAmount = 15340.0,
                    status = "PAID",
                    paymentMethod = "QR",
                    notes = "Paid via BharatQR at counter",
                    createdAt = now - day
                )
            )

            val inv3Items = listOf(
                InvoiceLineItem(p4, "Modular 8-Way Switch Plate", "EL-SWP-08M", 12, 310.0, 18.0),
            )
            db.invoiceDao().insertInvoice(
                InvoiceEntity(
                    invoiceNumber = "INV-2026-1040",
                    customerId = c3,
                    customerName = "Patel Kirana Mart",
                    customerPhone = "+91 99220 88761",
                    itemsJson = encodeLineItems(inv3Items),
                    subtotal = 3720.0,
                    discountAmount = 118.0,
                    gstAmount = 648.0,
                    totalAmount = 4250.0,
                    paidAmount = 0.0,
                    status = "PENDING",
                    paymentMethod = "UPI",
                    notes = "Credit billing — Friday collection",
                    createdAt = now - 2 * day
                )
            )

            val inv4Items = listOf(
                InvoiceLineItem(p3, "Havells Ceiling Fan 1200mm", "AP-FAN-120", 4, 2490.0, 18.0),
            )
            db.invoiceDao().insertInvoice(
                InvoiceEntity(
                    invoiceNumber = "INV-2026-1039",
                    customerId = c4,
                    customerName = "Mehta Interiors",
                    customerPhone = "+91 98111 76230",
                    itemsJson = encodeLineItems(inv4Items),
                    subtotal = 9960.0,
                    discountAmount = 0.0,
                    gstAmount = 1840.0,
                    totalAmount = 11800.0,
                    paidAmount = 0.0,
                    status = "OVERDUE",
                    paymentMethod = "BANK",
                    notes = "Site delivery batch #2",
                    createdAt = now - 9 * day
                )
            )

            val inv5Items = listOf(
                InvoiceLineItem(p6, "Premium Cotton Packaging Tape", "PK-TAP-050", 40, 85.0, 12.0),
                InvoiceLineItem(p1, "LED Smart Bulb 12W Pack", "EL-LED-12W", 6, 499.0, 18.0),
            )
            db.invoiceDao().insertInvoice(
                InvoiceEntity(
                    invoiceNumber = "INV-2026-1038",
                    customerId = c5,
                    customerName = "Sneha Boutique",
                    customerPhone = "+91 98904 55319",
                    itemsJson = encodeLineItems(inv5Items),
                    subtotal = 6394.0,
                    discountAmount = 0.0,
                    gstAmount = 946.0,
                    totalAmount = 7340.0,
                    paidAmount = 7340.0,
                    status = "PAID",
                    paymentMethod = "CASH",
                    createdAt = now - 6 * day
                )
            )

            // Expenses
            db.expenseDao().insertExpense(
                ExpenseEntity(
                    title = "Shop Electricity & Inverter Bill",
                    category = "Electricity",
                    amount = 2450.0,
                    dateMillis = now - 2 * 3_600_000L,
                    paymentMethod = "UPI",
                    notes = "MSEDCL monthly meter"
                )
            )
            db.expenseDao().insertExpense(
                ExpenseEntity(
                    title = "Shop Monthly Rent",
                    category = "Rent",
                    amount = 18000.0,
                    dateMillis = now - 3 * day,
                    paymentMethod = "BANK",
                    notes = "November shop lease"
                )
            )
            db.expenseDao().insertExpense(
                ExpenseEntity(
                    title = "Tempo Freight & Delivery",
                    category = "Transport",
                    amount = 1350.0,
                    dateMillis = now - 4 * day,
                    paymentMethod = "CASH",
                    notes = "Wholesale stock pickup"
                )
            )
            db.expenseDao().insertExpense(
                ExpenseEntity(
                    title = "Helper Staff Salary",
                    category = "Salary",
                    amount = 11500.0,
                    dateMillis = now - 7 * day,
                    paymentMethod = "UPI",
                    notes = "Ramesh monthly payout"
                )
            )
            db.expenseDao().insertExpense(
                ExpenseEntity(
                    title = "WhatsApp Festival Catalog Print",
                    category = "Marketing",
                    amount = 850.0,
                    dateMillis = now - 11 * day,
                    paymentMethod = "QR",
                    notes = "Diwali offer flyers"
                )
            )

            // Payments
            db.paymentDao().insertPayment(
                PaymentEntity(
                    customerId = c1,
                    customerName = "Ananya Traders",
                    invoiceId = inv1Id,
                    invoiceNumber = "INV-2026-1042",
                    amount = 5364.6,
                    method = "UPI",
                    transactionId = "UPI43219876012",
                    dateMillis = now - 3 * 3_600_000L,
                    notes = "Advance via GPay"
                )
            )
            db.paymentDao().insertPayment(
                PaymentEntity(
                    customerId = c2,
                    customerName = "Verma Hardware & Electricals",
                    invoiceId = inv2Id,
                    invoiceNumber = "INV-2026-1041",
                    amount = 15340.0,
                    method = "QR",
                    transactionId = "BQR99102348711",
                    dateMillis = now - day,
                    notes = "Full QR settlement"
                )
            )
            db.paymentDao().insertPayment(
                PaymentEntity(
                    customerId = c5,
                    customerName = "Sneha Boutique",
                    invoiceNumber = "INV-2026-1038",
                    amount = 7340.0,
                    method = "CASH",
                    transactionId = "CASH-1038",
                    dateMillis = now - 6 * day,
                    notes = "Counter cash"
                )
            )
            db.paymentDao().insertPayment(
                PaymentEntity(
                    customerId = c4,
                    customerName = "Mehta Interiors",
                    invoiceNumber = "INV-2026-1031",
                    amount = 24000.0,
                    method = "BANK",
                    transactionId = "NEFT004819283",
                    dateMillis = now - 12 * day,
                    notes = "HDFC NEFT transfer"
                )
            )

            // Subscription payments
            db.subscriptionDao().insertSubscriptionPayment(
                SubscriptionPaymentEntity(
                    planName = "SMART BUSINESS TOOLKIT PRO (Trial Activation)",
                    amountInr = 0,
                    transactionId = "TRIAL-FREE-14D",
                    status = "SUCCESS",
                    method = "14-Day Free Trial",
                    paidAtMillis = now - 3 * day
                )
            )

            // Admin users (SaaS platform view)
            db.adminUserDao().insertAdminUser(
                AdminUserEntity(
                    ownerName = "Rajesh Sharma",
                    businessName = "Sharma Electronics & Kirana",
                    email = "rajesh@sharmatraders.in",
                    mobile = "+91 98765 43210",
                    planStatus = "TRIAL",
                    monthlyRevenueInr = 0,
                    joinedAtMillis = now - 3 * day
                )
            )
            db.adminUserDao().insertAdminUser(
                AdminUserEntity(
                    ownerName = "Priya Deshmukh",
                    businessName = "Deshmukh Fashion Studio",
                    email = "priya@deshmukhstudio.in",
                    mobile = "+91 98234 77120",
                    planStatus = "ACTIVE",
                    monthlyRevenueInr = 399,
                    joinedAtMillis = now - 18 * day
                )
            )
            db.adminUserDao().insertAdminUser(
                AdminUserEntity(
                    ownerName = "Arvind Gupta",
                    businessName = "Gupta Medical & General",
                    email = "arvind@guptamed.in",
                    mobile = "+91 99100 32145",
                    planStatus = "ACTIVE",
                    monthlyRevenueInr = 399,
                    joinedAtMillis = now - 35 * day
                )
            )
            db.adminUserDao().insertAdminUser(
                AdminUserEntity(
                    ownerName = "Kavita Nair",
                    businessName = "Nair Freelance Consulting",
                    email = "kavita@nairconsult.in",
                    mobile = "+91 98451 66789",
                    planStatus = "ACTIVE",
                    monthlyRevenueInr = 399,
                    joinedAtMillis = now - 42 * day
                )
            )
            db.adminUserDao().insertAdminUser(
                AdminUserEntity(
                    ownerName = "Suresh Yadav",
                    businessName = "Yadav Dairy & Sweets",
                    email = "suresh@yadavsweets.in",
                    mobile = "+91 97112 88901",
                    planStatus = "EXPIRED",
                    monthlyRevenueInr = 0,
                    joinedAtMillis = now - 54 * day
                )
            )

            // Support tickets
            db.supportTicketDao().insertTicket(
                SupportTicketEntity(
                    subject = "How to add custom GSTIN footer on thermal bill?",
                    category = "Billing & GST",
                    message = "Need guidance on printing 80mm thermal receipt with GST breakup.",
                    status = "RESOLVED",
                    createdAtMillis = now - 2 * day
                )
            )
        }
    }
}
