package com.example.prima.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.prima.api.models.Product
import com.example.prima.viewmodel.CartItem
import com.example.prima.viewmodel.ProductUiState
import com.example.prima.viewmodel.TransactionUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionScreen(
    productUiState: ProductUiState,
    transactionUiState: TransactionUiState,
    onLoadProducts: () -> Unit,
    onAddToCart: (Product) -> Unit,
    onUpdateQuantity: (Int, Int) -> Unit,
    onRemoveFromCart: (Int) -> Unit,
    onSubmitOrder: () -> Unit,
    onBack: () -> Unit,
    onClearMessages: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    if (transactionUiState.successMessage != null) {
        AlertDialog(
            onDismissRequest = { onClearMessages() },
            title = { Text("Berhasil") },
            text = { Text(transactionUiState.successMessage) },
            confirmButton = {
                TextButton(onClick = onClearMessages) {
                    Text("OK")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Transaksi") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = com.example.prima.ui.theme.PrimaryDark,
                    titleContentColor = androidx.compose.ui.graphics.Color.White,
                    navigationIconContentColor = androidx.compose.ui.graphics.Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Produk") },
                    icon = { Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = null) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Text("Keranjang (${transactionUiState.cartItems.size})")
                    },
                    icon = { Icon(Icons.Default.ShoppingCart, contentDescription = null) }
                )
            }

            when (selectedTab) {
                0 -> ProductTab(
                    uiState = productUiState,
                    onRefresh = onLoadProducts,
                    onProductClick = onAddToCart,
                    isCreatingTransaction = transactionUiState.isCreatingTransaction
                )
                1 -> CartTab(
                    uiState = transactionUiState,
                    onUpdateQuantity = onUpdateQuantity,
                    onRemoveFromCart = onRemoveFromCart,
                    onSubmitOrder = onSubmitOrder
                )
            }
        }
    }
}

@Composable
private fun ProductTab(
    uiState: ProductUiState,
    onRefresh: () -> Unit,
    onProductClick: (Product) -> Unit,
    isCreatingTransaction: Boolean
) {
    Box(modifier = Modifier.fillMaxSize()) {
        when {
            uiState.isLoading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
            uiState.errorMessage != null -> {
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = uiState.errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = onRefresh) { Text("Coba Lagi") }
                }
            }
            uiState.products.isEmpty() -> {
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Belum ada produk")
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = onRefresh) { Text("Refresh") }
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.products) { product ->
                        ProductItem(
                            product = product,
                            onClick = { onProductClick(product) },
                            enabled = !isCreatingTransaction
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProductItem(
    product: Product,
    onClick: () -> Unit,
    enabled: Boolean
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        enabled = enabled
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Rp${String.format("%,.0f", product.price)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Icon(
                Icons.Default.AddCircle,
                contentDescription = "Tambah",
                tint = if (enabled) MaterialTheme.colorScheme.primary
                       else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
            )
        }
    }
}

@Composable
private fun CartTab(
    uiState: TransactionUiState,
    onUpdateQuantity: (Int, Int) -> Unit,
    onRemoveFromCart: (Int) -> Unit,
    onSubmitOrder: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        if (uiState.cartItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Keranjang masih kosong",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.cartItems, key = { it.product.id }) { item ->
                    CartItemCard(
                        item = item,
                        onIncrement = { onUpdateQuantity(item.product.id, item.quantity + 1) },
                        onDecrement = { onUpdateQuantity(item.product.id, item.quantity - 1) },
                        onRemove = { onRemoveFromCart(item.product.id) }
                    )
                }
            }

            HorizontalDivider()

            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Total",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Rp${String.format("%,.0f", uiState.cartItems.sumOf { it.product.price * it.quantity })}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onSubmitOrder,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    enabled = !uiState.isSubmitting
                ) {
                    if (uiState.isSubmitting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("Konfirmasi Pesanan", style = MaterialTheme.typography.labelLarge)
                    }
                }

                if (uiState.errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = uiState.errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

@Composable
private fun CartItemCard(
    item: CartItem,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    onRemove: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.product.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Rp${String.format("%,.0f", item.product.price)} x ${item.quantity}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Text(
                    text = "Rp${String.format("%,.0f", item.product.price * item.quantity)}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onDecrement) {
                    Icon(Icons.Default.Remove, contentDescription = "Kurangi")
                }
                Text(
                    text = "${item.quantity}",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                IconButton(onClick = onIncrement) {
                    Icon(Icons.Default.Add, contentDescription = "Tambah")
                }
                IconButton(onClick = onRemove) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Hapus",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}
