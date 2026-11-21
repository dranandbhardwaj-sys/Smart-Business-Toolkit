package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface BusinessProfileDao {
    @Query("SELECT * FROM business_profile WHERE id = 1")
    fun observeProfile(): Flow<BusinessProfileEntity?>

    @Query("SELECT * FROM business_profile WHERE id = 1")
    suspend fun getProfileOnce(): BusinessProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertProfile(profile: BusinessProfileEntity)
}

@Dao
interface CustomerDao {
    @Query("SELECT * FROM customers ORDER BY lastTransactionAt DESC")
    fun observeCustomers(): Flow<List<CustomerEntity>>

    @Query("SELECT COUNT(*) FROM customers")
    suspend fun countCustomers(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomer(customer: CustomerEntity): Long

    @Update
    suspend fun updateCustomer(customer: CustomerEntity)

    @Delete
    suspend fun deleteCustomer(customer: CustomerEntity)
}

@Dao
interface ProductDao {
    @Query("SELECT * FROM products ORDER BY updatedAt DESC")
    fun observeProducts(): Flow<List<ProductEntity>>

    @Query("SELECT COUNT(*) FROM products")
    suspend fun countProducts(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductEntity): Long

    @Update
    suspend fun updateProduct(product: ProductEntity)

    @Delete
    suspend fun deleteProduct(product: ProductEntity)
}

@Dao
interface InvoiceDao {
    @Query("SELECT * FROM invoices ORDER BY createdAt DESC")
    fun observeInvoices(): Flow<List<InvoiceEntity>>

    @Query("SELECT COUNT(*) FROM invoices")
    suspend fun countInvoices(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvoice(invoice: InvoiceEntity): Long

    @Update
    suspend fun updateInvoice(invoice: InvoiceEntity)

    @Delete
    suspend fun deleteInvoice(invoice: InvoiceEntity)
}

@Dao
interface ExpenseDao {
    @Query("SELECT * FROM expenses ORDER BY dateMillis DESC")
    fun observeExpenses(): Flow<List<ExpenseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: ExpenseEntity): Long

    @Update
    suspend fun updateExpense(expense: ExpenseEntity)

    @Delete
    suspend fun deleteExpense(expense: ExpenseEntity)
}

@Dao
interface PaymentDao {
    @Query("SELECT * FROM payments ORDER BY dateMillis DESC")
    fun observePayments(): Flow<List<PaymentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: PaymentEntity): Long

    @Delete
    suspend fun deletePayment(payment: PaymentEntity)
}

@Dao
interface SubscriptionDao {
    @Query("SELECT * FROM subscription_status WHERE id = 1")
    fun observeSubscription(): Flow<SubscriptionEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSubscription(subscription: SubscriptionEntity)

    @Query("SELECT * FROM subscription_payments ORDER BY paidAtMillis DESC")
    fun observeSubscriptionPayments(): Flow<List<SubscriptionPaymentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubscriptionPayment(payment: SubscriptionPaymentEntity): Long
}

@Dao
interface AdminUserDao {
    @Query("SELECT * FROM admin_users ORDER BY joinedAtMillis DESC")
    fun observeAdminUsers(): Flow<List<AdminUserEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAdminUser(user: AdminUserEntity): Long

    @Update
    suspend fun updateAdminUser(user: AdminUserEntity)
}

@Dao
interface SupportTicketDao {
    @Query("SELECT * FROM support_tickets ORDER BY createdAtMillis DESC")
    fun observeTickets(): Flow<List<SupportTicketEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTicket(ticket: SupportTicketEntity): Long
}
