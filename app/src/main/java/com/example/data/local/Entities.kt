package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class InvoiceLineItem(
    val productId: Long = 0L,
    val name: String,
    val sku: String = "",
    val qty: Int,
    val unitPrice: Double,
    val gstPercent: Double = 18.0,
)

@Entity(tableName = "business_profile")
data class BusinessProfileEntity(
    @PrimaryKey val id: Int = 1,
    val ownerName: String = "Rajesh Sharma",
    val businessName: String = "Sharma Electronics & Kirana",
    val category: String = "Retail & General Store",
    val mobile: String = "+91 98765 43210",
    val email: String = "rajesh@sharmatraders.in",
    val gstRegistered: Boolean = true,
    val gstin: String = "27AABCS1429B1Z5",
    val upiId: String = "sharmatraders@okaxis",
    val address: String = "Shop 14, MG Road, Pune, Maharashtra 411001",
    val logoUri: String? = null,
    val invoicePrefix: String = "INV-2026-",
    val defaultGstPercent: Double = 18.0,
    val currency: String = "INR (₹)",
    val whatsappSupportUrl: String = "https://wa.me/919876543210?text=Hello%20Smart%20Business%20Toolkit%20Support",
)

@Entity(tableName = "customers")
data class CustomerEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    val phone: String,
    val email: String = "",
    val gstin: String = "",
    val address: String = "",
    val totalPurchases: Double = 0.0,
    val pendingBalance: Double = 0.0,
    val lastTransactionAt: Long = System.currentTimeMillis(),
    val createdAt: Long = System.currentTimeMillis(),
)

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    val sku: String,
    val category: String,
    val purchasePrice: Double,
    val sellingPrice: Double,
    val stockQty: Int,
    val lowStockThreshold: Int = 10,
    val gstPercent: Double = 18.0,
    val unit: String = "pcs",
    val updatedAt: Long = System.currentTimeMillis(),
)

@Entity(tableName = "invoices")
data class InvoiceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val invoiceNumber: String,
    val customerId: Long,
    val customerName: String,
    val customerPhone: String,
    val itemsJson: String,
    val subtotal: Double,
    val discountAmount: Double,
    val gstAmount: Double,
    val totalAmount: Double,
    val paidAmount: Double,
    val status: String, // PAID, PARTIAL, PENDING, OVERDUE
    val paymentMethod: String, // UPI, QR, CASH, BANK
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
)

@Entity(tableName = "expenses")
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val title: String,
    val category: String, // Rent, Electricity, Inventory, Salary, Transport, Marketing, Misc
    val amount: Double,
    val dateMillis: Long = System.currentTimeMillis(),
    val paymentMethod: String = "UPI",
    val notes: String = "",
)

@Entity(tableName = "payments")
data class PaymentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val customerId: Long,
    val customerName: String,
    val invoiceId: Long? = null,
    val invoiceNumber: String = "",
    val amount: Double,
    val method: String, // UPI, QR, CASH, BANK
    val transactionId: String,
    val dateMillis: Long = System.currentTimeMillis(),
    val notes: String = "",
)

@Entity(tableName = "subscription_status")
data class SubscriptionEntity(
    @PrimaryKey val id: Int = 1,
    val planName: String = "SMART BUSINESS TOOLKIT PRO",
    val priceInr: Int = 399,
    val status: String = "TRIAL", // TRIAL, ACTIVE, EXPIRED
    val trialDaysLeft: Int = 11,
    val expiryDateMillis: Long = System.currentTimeMillis() + 11L * 86_400_000L,
    val autoRenew: Boolean = true,
)

@Entity(tableName = "subscription_payments")
data class SubscriptionPaymentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val planName: String,
    val amountInr: Int,
    val transactionId: String,
    val status: String, // SUCCESS, PENDING
    val method: String,
    val paidAtMillis: Long = System.currentTimeMillis(),
)

@Entity(tableName = "admin_users")
data class AdminUserEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val ownerName: String,
    val businessName: String,
    val email: String,
    val mobile: String,
    val planStatus: String, // ACTIVE, TRIAL, EXPIRED
    val monthlyRevenueInr: Int,
    val joinedAtMillis: Long = System.currentTimeMillis(),
)

@Entity(tableName = "support_tickets")
data class SupportTicketEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val subject: String,
    val category: String,
    val message: String,
    val status: String = "OPEN", // OPEN, RESOLVED
    val createdAtMillis: Long = System.currentTimeMillis(),
)
