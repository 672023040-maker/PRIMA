package com.example.prima

import android.app.Application
import android.content.SharedPreferences
import com.example.prima.api.models.*
import com.example.prima.viewmodel.AuthViewModel
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

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
    ): AuthViewModel {
        val mockPrefs = mockk<SharedPreferences>(relaxed = true) {
            every { getString("token", null) } returns prefsMap["token"] as? String
            every { getString("user_name", null) } returns prefsMap["user_name"] as? String
            every { getString("user_role", null) } returns prefsMap["user_role"] as? String
            every { getString("user_email", null) } returns prefsMap["user_email"] as? String
            every { getBoolean("is_logged_in", false) } returns (prefsMap["is_logged_in"] as? Boolean ?: false)
            every { edit() } returns mockk(relaxed = true)
        }

        val mockApp = mockk<Application>(relaxed = true) {
            every { getSharedPreferences(any(), any()) } returns mockPrefs
        }

        return AuthViewModel(mockApp)
    }

    @Test
    fun `initial state shows not logged in when no session`() = runTest {
        val viewModel = createViewModel()

        val state = viewModel.uiState.value
        assertFalse(state.isLoggedIn)
        assertNull(state.userRole)
        assertNull(state.errorMessage)
    }

    @Test
    fun `initial state shows logged in when session exists`() = runTest {
        val viewModel = createViewModel(
            mutableMapOf(
                "is_logged_in" to true,
                "user_role" to "kasir",
                "user_name" to "Kasir A"
            )
        )

        val state = viewModel.uiState.value
        assertTrue(state.isLoggedIn)
        assertEquals("kasir", state.userRole)
        assertEquals("Kasir A", state.userName)
    }

    @Test
    fun `login clears error when calling clearError`() = runTest {
        val viewModel = createViewModel()

        viewModel.clearError()

        val state = viewModel.uiState.value
        assertNull(state.errorMessage)
    }
}
