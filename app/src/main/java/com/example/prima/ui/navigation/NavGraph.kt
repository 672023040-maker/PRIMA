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
    const val PRODUCT_CREATE = "product/create"
    const val PRODUCT_EDIT = "product/edit/{id}"
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
                userRole = authState.userRole,
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
                userRole = productViewModel.getUserRole(),
                onRefresh = { productViewModel.loadProducts() },
                onBack = { navController.popBackStack() },
                onAddProduct = { navController.navigate(Routes.PRODUCT_CREATE) },
                onEditProduct = { product ->
                    navController.navigate("product/edit/${product.id}")
                },
                onDeleteProduct = { product ->
                    productViewModel.deleteProduct(product.id)
                }
            )
        }

        composable(Routes.PRODUCT_CREATE) {
            val productViewModel: ProductViewModel = viewModel()

            ProductFormScreen(
                product = null,
                uiState = productViewModel.uiState.collectAsState().value,
                onSave = { name, price, category, description ->
                    productViewModel.createProduct(name, price, category, description)
                },
                onBack = { navController.popBackStack() },
                onClearMessages = { productViewModel.clearMessages() }
            )
        }

        composable(Routes.PRODUCT_EDIT) { backStackEntry ->
            val productId = backStackEntry.arguments?.getString("id")?.toIntOrNull()
            val productViewModel: ProductViewModel = viewModel()

            LaunchedEffect(Unit) {
                if (productViewModel.uiState.value.products.isEmpty()) {
                    productViewModel.loadProducts()
                }
            }

            val products = productViewModel.uiState.value.products
            val product = products.find { it.id == productId }

            ProductFormScreen(
                product = product,
                uiState = productViewModel.uiState.collectAsState().value,
                onSave = { name, price, category, description ->
                    if (productId != null) {
                        productViewModel.updateProduct(productId, name, price, category, description)
                    }
                },
                onBack = { navController.popBackStack() },
                onClearMessages = { productViewModel.clearMessages() }
            )
        }

        composable(Routes.TRANSACTION) {
            val productViewModel: ProductViewModel = viewModel()
            val transactionViewModel: TransactionViewModel = viewModel()
            val transactionUiState by transactionViewModel.uiState.collectAsState()

            LaunchedEffect(Unit) {
                productViewModel.loadProducts()
            }

            if (transactionUiState.showReceiptScreen && transactionUiState.lastCompletedTransaction != null) {
                ReceiptScreen(
                    transaction = transactionUiState.lastCompletedTransaction!!,
                    onDone = {
                        transactionViewModel.dismissReceiptScreen()
                    }
                )
            } else {
                TransactionScreen(
                    productUiState = productViewModel.uiState.collectAsState().value,
                    transactionUiState = transactionUiState,
                    onLoadProducts = { productViewModel.loadProducts() },
                    onAddToCart = { product -> transactionViewModel.addToCart(product) },
                    onUpdateQuantity = { id, qty -> transactionViewModel.updateQuantity(id, qty) },
                    onRemoveFromCart = { id -> transactionViewModel.removeFromCart(id) },
                    onSubmitOrder = { transactionViewModel.submitOrder() },
                    onBack = { navController.popBackStack() },
                    onClearMessages = { transactionViewModel.clearMessages() },
                    onSelectPaymentMethod = { method -> transactionViewModel.selectPaymentMethod(method) },
                    onUpdateAmountPaid = { amount -> transactionViewModel.updateAmountPaid(amount) },
                    onConfirmPayment = { transactionViewModel.completePayment() },
                    onDismissPaymentDialog = { transactionViewModel.dismissPaymentDialog() }
                )
            }
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
