package com.example.prima

import android.app.Application
import android.content.SharedPreferences
import com.example.prima.api.models.Product
import com.example.prima.data.SessionManager
import com.example.prima.data.repository.TransactionRepository
import com.example.prima.viewmodel.CartItem
import com.example.prima.viewmodel.TransactionViewModel
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TransactionViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    private fun createViewModel(
        prefsMap: MutableMap<String, Any?> = mutableMapOf()
    ): TransactionViewModel {
        val mockPrefs = mockk<SharedPreferences>(relaxed = true) {
            every { getString("token", null) } returns prefsMap["token"] as? String
            every { getInt("user_id", 0) } returns (prefsMap["user_id"] as? Int ?: 0)
            every { edit() } returns mockk(relaxed = true)
        }

        val mockApp = mockk<Application>(relaxed = true) {
            every { getSharedPreferences(any(), any()) } returns mockPrefs
        }

        return TransactionViewModel(mockApp)
    }

    private fun createProduct(id: Int, name: String, price: Double): Product {
        return Product(id, name, price, 1, "Kategori", "Deskripsi", null, 50, true)
    }

    @Test
    fun `initial state has empty cart`() = runTest {
        val viewModel = createViewModel()

        val state = viewModel.uiState.value
        assertTrue(state.cartItems.isEmpty())
        assertEquals(0.0, viewModel.getTotal(), 0.01)
    }

    @Test
    fun `addToCart adds new item`() = runTest {
        val viewModel = createViewModel()
        val product = createProduct(1, "Kopi", 15000.0)

        viewModel.addToCart(product)

        val state = viewModel.uiState.value
        assertEquals(1, state.cartItems.size)
        assertEquals("Kopi", state.cartItems[0].product.name)
        assertEquals(1, state.cartItems[0].quantity)
    }

    @Test
    fun `addToCart increases quantity for existing item`() = runTest {
        val viewModel = createViewModel()
        val product = createProduct(1, "Kopi", 15000.0)

        viewModel.addToCart(product)
        viewModel.addToCart(product)

        val state = viewModel.uiState.value
        assertEquals(1, state.cartItems.size)
        assertEquals(2, state.cartItems[0].quantity)
    }

    @Test
    fun `addToCart adds different items separately`() = runTest {
        val viewModel = createViewModel()
        val product1 = createProduct(1, "Kopi", 15000.0)
        val product2 = createProduct(2, "Teh", 10000.0)

        viewModel.addToCart(product1)
        viewModel.addToCart(product2)

        val state = viewModel.uiState.value
        assertEquals(2, state.cartItems.size)
    }

    @Test
    fun `removeFromCart removes item`() = runTest {
        val viewModel = createViewModel()
        viewModel.addToCart(createProduct(1, "Kopi", 15000.0))
        viewModel.addToCart(createProduct(2, "Teh", 10000.0))

        viewModel.removeFromCart(1)

        val state = viewModel.uiState.value
        assertEquals(1, state.cartItems.size)
        assertEquals("Teh", state.cartItems[0].product.name)
    }

    @Test
    fun `updateQuantity changes quantity`() = runTest {
        val viewModel = createViewModel()
        viewModel.addToCart(createProduct(1, "Kopi", 15000.0))

        viewModel.updateQuantity(1, 5)

        val state = viewModel.uiState.value
        assertEquals(5, state.cartItems[0].quantity)
    }

    @Test
    fun `updateQuantity removes item when quantity is 0`() = runTest {
        val viewModel = createViewModel()
        viewModel.addToCart(createProduct(1, "Kopi", 15000.0))

        viewModel.updateQuantity(1, 0)

        val state = viewModel.uiState.value
        assertTrue(state.cartItems.isEmpty())
    }

    @Test
    fun `updateQuantity removes item when quantity is negative`() = runTest {
        val viewModel = createViewModel()
        viewModel.addToCart(createProduct(1, "Kopi", 15000.0))

        viewModel.updateQuantity(1, -1)

        val state = viewModel.uiState.value
        assertTrue(state.cartItems.isEmpty())
    }

    @Test
    fun `getTotal calculates correctly`() = runTest {
        val viewModel = createViewModel()
        viewModel.addToCart(createProduct(1, "Kopi", 15000.0))
        viewModel.addToCart(createProduct(2, "Teh", 10000.0))

        val product = createProduct(1, "Kopi", 15000.0)
        viewModel.addToCart(product)

        assertEquals(40000.0, viewModel.getTotal(), 0.01)
    }

    @Test
    fun `dismissPaymentDialog sets showPaymentDialog to false`() = runTest {
        val viewModel = createViewModel()

        viewModel.dismissPaymentDialog()

        assertFalse(viewModel.uiState.value.showPaymentDialog)
    }

    @Test
    fun `clearMessages clears error and success messages`() = runTest {
        val viewModel = createViewModel()

        viewModel.clearMessages()

        val state = viewModel.uiState.value
        assertNull(state.errorMessage)
        assertNull(state.successMessage)
    }

    @Test
    fun `submitOrder shows error when cart is empty`() = runTest {
        val viewModel = createViewModel()
        viewModel.submitOrder()

        val state = viewModel.uiState.value
        assertNotNull(state.errorMessage)
    }

    @Test
    fun `completePayment shows error when no transaction`() = runTest {
        val viewModel = createViewModel()
        viewModel.completePayment()

        val state = viewModel.uiState.value
        assertNotNull(state.errorMessage)
    }

    @Test
    fun `completePayment shows error when no payment method selected`() = runTest {
        val viewModel = createViewModel()

        viewModel.updateAmountPaid("50000")
        viewModel.completePayment()

        val state = viewModel.uiState.value
        assertNotNull(state.errorMessage)
    }

    @Test
    fun `completePayment shows error when amount is invalid`() = runTest {
        val viewModel = createViewModel()

        viewModel.updateAmountPaid("abc")
        viewModel.completePayment()

        val state = viewModel.uiState.value
        assertNotNull(state.errorMessage)
    }

    @Test
    fun `selectPaymentMethod updates state`() = runTest {
        val viewModel = createViewModel()
        val method = com.example.prima.api.models.PaymentMethod(1, "Cash", "Tunai", true)

        viewModel.selectPaymentMethod(method)

        assertEquals("Cash", viewModel.uiState.value.selectedPaymentMethod?.name)
    }

    @Test
    fun `updateAmountPaid updates state`() = runTest {
        val viewModel = createViewModel()

        viewModel.updateAmountPaid("50000")

        assertEquals("50000", viewModel.uiState.value.amountPaid)
    }

    @Test
    fun `initial state has no receipt screen`() = runTest {
        val viewModel = createViewModel()

        val state = viewModel.uiState.value
        assertFalse(state.showReceiptScreen)
        assertNull(state.lastCompletedTransaction)
    }

    @Test
    fun `dismissReceiptScreen clears receipt state`() = runTest {
        val viewModel = createViewModel()

        viewModel.dismissReceiptScreen()

        val state = viewModel.uiState.value
        assertFalse(state.showReceiptScreen)
        assertNull(state.lastCompletedTransaction)
    }
}
