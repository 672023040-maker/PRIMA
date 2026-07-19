package com.example.prima.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.prima.api.models.Product
import com.example.prima.ui.theme.Nunito
import com.example.prima.ui.theme.PrimaryDark
import com.example.prima.viewmodel.ProductUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductScreen(
    uiState: ProductUiState,
    userRole: String?,
    onRefresh: () -> Unit,
    onBack: () -> Unit,
    onAddProduct: () -> Unit,
    onEditProduct: (Product) -> Unit,
    onDeleteProduct: (Product) -> Unit
) {
    val isAdmin = userRole == "admin" || userRole == "owner"
    var showDeleteDialog by remember { mutableStateOf<Product?>(null) }

    if (showDeleteDialog != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Hapus Produk", fontFamily = Nunito) },
            text = { Text("Yakin ingin menghapus \"${showDeleteDialog!!.name}\"?", fontFamily = Nunito) },
            confirmButton = {
                TextButton(onClick = {
                    onDeleteProduct(showDeleteDialog!!)
                    showDeleteDialog = null
                }) {
                    Text("Hapus", color = MaterialTheme.colorScheme.error, fontFamily = Nunito)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) {
                    Text("Batal", fontFamily = Nunito)
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Katalog Produk", fontFamily = Nunito) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PrimaryDark,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            if (isAdmin) {
                FloatingActionButton(
                    onClick = onAddProduct,
                    containerColor = PrimaryDark
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Tambah Produk", tint = Color.White)
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
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
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = onRefresh) {
                            Text("Coba Lagi")
                        }
                    }
                }
                uiState.products.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Belum ada produk",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = onRefresh) {
                            Text("Refresh")
                        }
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(uiState.products) { product ->
                            ProductCard(
                                product = product,
                                isAdmin = isAdmin,
                                onEdit = { onEditProduct(product) },
                                onDelete = { showDeleteDialog = product }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProductCard(
    product: Product,
    isAdmin: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = product.name,
                    fontFamily = Nunito,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                product.category_name?.let {
                    Text(
                        text = it,
                        fontFamily = Nunito,
                        fontSize = 13.sp,
                        color = Color(0xFF64748B)
                    )
                }
                product.description?.let {
                    Text(
                        text = it,
                        fontFamily = Nunito,
                        fontSize = 12.sp,
                        color = Color(0xFF94A3B8),
                        maxLines = 2
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Rp${String.format("%,.0f", product.price)}",
                    fontFamily = Nunito,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryDark
                )
            }

            if (isAdmin) {
                Row {
                    IconButton(onClick = onEdit) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = PrimaryDark
                        )
                    }
                    IconButton(onClick = onDelete) {
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
}
