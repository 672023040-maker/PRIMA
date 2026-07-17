package com.example.prima.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.prima.ui.screens.*
import com.example.prima.viewmodel.*

object Routes {
    const val LOGIN = "login"
    const val HOME = "home"
    const val PRODUCTS = "products"
    const val TRANSACTION = "transaction"
    const val REPORT = "report"
}

@Composable
fun NavGraph(
    navController: NavHostController,
    authViewModel: AuthViewModel = viewModel()
) {
    val authState by authViewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        authViewModel.events.collect { event ->
            when (event) {
                AuthEvent.LoginSuccess -> {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
                AuthEvent.LogoutSuccess -> {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            }
        }
    }

    val startDestination = if (authState.isLoggedIn) Routes.HOME else Routes.LOGIN

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Routes.LOGIN) {
            LoginScreen(
                uiState = authState,
                onLogin = { email, password ->
                    authViewModel.login(email, password)
                },
                onClearError = { authViewModel.clearError() }
            )
        }

        composable(Routes.HOME) {
            HomeScreen(
                userName = authState.userName,
                onNavigateToProducts = {
                    navController.navigate(Routes.PRODUCTS)
                },
                onNavigateToTransaction = {
                    navController.navigate(Routes.TRANSACTION)
                },
                onNavigateToReport = {
                    navController.navigate(Routes.REPORT)
                },
                onLogout = {
                    authViewModel.logout()
                }
            )
        }

        composable(Routes.PRODUCTS) {
            val productViewModel: ProductViewModel = viewModel()

            LaunchedEffect(Unit) {
                productViewModel.loadProducts()
            }

            ProductScreen(
                uiState = productViewModel.uiState.collectAsState().value,
                onRefresh = { productViewModel.loadProducts() },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.TRANSACTION) {
            val productViewModel: ProductViewModel = viewModel()
            val transactionViewModel: TransactionViewModel = viewModel()

            LaunchedEffect(Unit) {
                productViewModel.loadProducts()
            }

            TransactionScreen(
                productUiState = productViewModel.uiState.collectAsState().value,
                transactionUiState = transactionViewModel.uiState.collectAsState().value,
                onLoadProducts = { productViewModel.loadProducts() },
                onAddToCart = { product -> transactionViewModel.addToCart(product) },
                onUpdateQuantity = { id, qty -> transactionViewModel.updateQuantity(id, qty) },
                onRemoveFromCart = { id -> transactionViewModel.removeFromCart(id) },
                onSubmitOrder = { transactionViewModel.submitOrder() },
                onBack = { navController.popBackStack() },
                onClearMessages = { transactionViewModel.clearMessages() }
            )
        }

        composable(Routes.REPORT) {
            val reportViewModel: ReportViewModel = viewModel()

            LaunchedEffect(Unit) {
                reportViewModel.loadReports()
            }

            ReportScreen(
                uiState = reportViewModel.uiState.collectAsState().value,
                onRefresh = { reportViewModel.loadReports() },
                onBack = { navController.popBackStack() }
            )
        }
    }
}
