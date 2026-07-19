package com.example.prima

import com.example.prima.api.ApiService
import com.example.prima.api.models.*
import com.example.prima.data.repository.TransactionRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import retrofit2.Response

class TransactionRepositoryTest {

    private lateinit var api: ApiService
    private lateinit var repository: TransactionRepository

    @Before
    fun setup() {
        api = mockk()
        repository = TransactionRepository(api)
    }

    @Test
    fun `getProducts returns success`() = runTest {
        val products = listOf(
            Product(1, "Kopi", 15000.0, 1, "Minuman", "Kopi hitam", null, 50, true)
        )
        val response = ProductResponse(200, "OK", products)
        coEvery { api.getProducts(any()) } returns Response.success(response)

        val result = repository.getProducts("token123")

        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()?.data?.size)
        assertEquals("Kopi", result.getOrNull()?.data?.first()?.name)
    }

    @Test
    fun `getProducts returns failure on error`() = runTest {
        coEvery { api.getProducts(any()) } returns Response.error(500, mockk(relaxed = true))

        val result = repository.getProducts("token123")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("500") == true)
    }

    @Test
    fun `getProducts returns failure on exception`() = runTest {
        coEvery { api.getProducts(any()) } throws Exception("Network error")

        val result = repository.getProducts("token123")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("Network error") == true)
    }

    @Test
    fun `createTransaction returns success`() = runTest {
        val transaction = Transaction(1, "TRX-001", 1, "Kasir A", null, null, 0.0, null, null, null, "active", "2025-01-01")
        val response = TransactionResponse(201, "OK", transaction)
        coEvery { api.createTransaction(any(), any()) } returns Response.success(response)

        val result = repository.createTransaction("token123", 1)

        assertTrue(result.isSuccess)
        assertEquals("TRX-001", result.getOrNull()?.data?.transaction_code)
    }

    @Test
    fun `addDetail returns success`() = runTest {
        val detail = TransactionDetail(1, 1, 1, "Kopi", 2, 15000.0, 30000.0)
        val response = TransactionDetailResponse(201, "OK", detail)
        coEvery { api.addTransactionDetail(any(), any(), any()) } returns Response.success(response)

        val result = repository.addDetail("token123", 1, 1, 2)

        assertTrue(result.isSuccess)
        assertEquals(2, result.getOrNull()?.data?.quantity)
    }

    @Test
    fun `completeTransaction returns success`() = runTest {
        val transaction = Transaction(1, "TRX-001", 1, "Kasir A", 1, "Cash", 30000.0, 50000.0, 20000.0, null, "completed", "2025-01-01")
        val response = CompleteTransactionResponse(200, "OK", transaction)
        coEvery { api.completeTransaction(any(), any(), any()) } returns Response.success(response)

        val result = repository.completeTransaction("token123", 1, 1, 50000.0)

        assertTrue(result.isSuccess)
        assertEquals("completed", result.getOrNull()?.data?.status)
    }

    @Test
    fun `completeTransaction returns failure when body null`() = runTest {
        coEvery { api.completeTransaction(any(), any(), any()) } returns Response.success(null)

        val result = repository.completeTransaction("token123", 1, 1, 50000.0)

        assertTrue(result.isFailure)
    }

    @Test
    fun `getCategories returns success`() = runTest {
        val categories = listOf(Category(1, "Minuman", "Minuman dingin"))
        val response = CategoryResponse(200, "OK", categories)
        coEvery { api.getCategories(any()) } returns Response.success(response)

        val result = repository.getCategories("token123")

        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()?.data?.size)
    }

    @Test
    fun `getPaymentMethods returns success`() = runTest {
        val methods = listOf(PaymentMethod(1, "Cash", "Tunai", true))
        val response = PaymentMethodResponse(200, "OK", methods)
        coEvery { api.getPaymentMethods(any()) } returns Response.success(response)

        val result = repository.getPaymentMethods("token123")

        assertTrue(result.isSuccess)
        assertEquals("Cash", result.getOrNull()?.data?.first()?.name)
    }

    @Test
    fun `deleteProduct returns success`() = runTest {
        val response = ApiResponse(200, "Produk berhasil dihapus")
        coEvery { api.deleteProduct(any(), any()) } returns Response.success(response)

        val result = repository.deleteProduct("token123", 1)

        assertTrue(result.isSuccess)
        assertEquals("Produk berhasil dihapus", result.getOrNull()?.message)
    }

    @Test
    fun `getTransactions returns success`() = runTest {
        val transactions = listOf(
            Transaction(1, "TRX-001", 1, "Kasir A", null, null, 50000.0, null, null, null, "completed", "2025-01-01")
        )
        val response = TransactionListResponse(200, "OK", transactions)
        coEvery { api.getTransactions(any()) } returns Response.success(response)

        val result = repository.getTransactions("token123")

        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()?.data?.size)
    }
}
