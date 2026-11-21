package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.local.AppPreferences
import com.example.data.local.InvoiceLineItem
import com.example.data.repository.BusinessRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    private lateinit var context: Context
    private lateinit var db: AppDatabase
    private lateinit var repository: BusinessRepository

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = BusinessRepository(db, AppPreferences(context))
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun `app name matches Smart Business`() {
        val appName = context.getString(R.string.app_name)
        assertEquals("Smart Business", appName)
    }

    @Test
    fun `seed database creates initial Indian SMB records and invoice deducts stock`() = runBlocking {
        AppDatabase.seedInitialDataIfEmpty(db)

        val customers = repository.customersFlow.first()
        val products = repository.productsFlow.first()
        assertTrue(customers.isNotEmpty())
        assertTrue(products.isNotEmpty())

        val customer = customers.first()
        val product = products.first()
        val initialQty = product.stockQty

        val created = repository.createInvoice(
            customer = customer,
            items = listOf(
                InvoiceLineItem(
                    productId = product.id,
                    name = product.name,
                    sku = product.sku,
                    qty = 2,
                    unitPrice = product.sellingPrice,
                    gstPercent = product.gstPercent,
                )
            ),
            discountAmount = 0.0,
            paidAmount = 0.0,
            paymentMethod = "UPI",
            notes = "Test GST invoice",
        )

        assertEquals("PENDING", created.status)
        val updatedProducts = repository.productsFlow.first()
        val updatedProduct = updatedProducts.first { it.id == product.id }
        assertEquals(initialQty - 2, updatedProduct.stockQty)
    }
}
