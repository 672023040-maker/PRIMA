package com.example.prima.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.prima.ui.theme.Nunito

@Composable
fun HomeScreen(
    userName: String?,
    userRole: String?,
    onNavigateToProducts: () -> Unit,
    onNavigateToTransaction: () -> Unit,
    onNavigateToReport: () -> Unit,
    onLogout: () -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Konfirmasi Logout", fontFamily = Nunito) },
            text = { Text("Apakah Anda yakin ingin logout?", fontFamily = Nunito) },
            confirmButton = {
                TextButton(onClick = {
                    showDialog = false
                    onLogout()
                }) {
                    Text("Ya", fontFamily = Nunito)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Tidak", fontFamily = Nunito)
                }
            }
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF90A3DE))
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "PRIMA",
                    fontFamily = Nunito,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                IconButton(onClick = { showDialog = true }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Logout,
                        contentDescription = "Logout",
                        tint = Color.White
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFFAFAF8))
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "Halo, $userName",
                fontFamily = Nunito,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E293B)
            )

            Spacer(modifier = Modifier.height(28.dp))

            MenuItemRow(
                icon = Icons.AutoMirrored.Filled.MenuBook,
                label = "Katalog Produk",
                onClick = onNavigateToProducts
            )

            HorizontalDivider(
                modifier = Modifier.padding(start = 56.dp),
                color = Color(0xFFE2E8F0),
                thickness = 0.8.dp
            )

            MenuItemRow(
                icon = Icons.Default.ShoppingCart,
                label = "Buat Transaksi",
                onClick = onNavigateToTransaction
            )

            HorizontalDivider(
                modifier = Modifier.padding(start = 56.dp),
                color = Color(0xFFE2E8F0),
                thickness = 0.8.dp
            )

            MenuItemRow(
                icon = Icons.Default.Description,
                label = "Laporan Transaksi",
                onClick = onNavigateToReport
            )
        }
    }
}

@Composable
private fun MenuItemRow(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(Color(0xFF90A3DE).copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(22.dp),
                tint = Color(0xFF90A3DE)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = label,
            fontFamily = Nunito,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF1E293B),
            modifier = Modifier.weight(1f)
        )

        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            modifier = Modifier.size(22.dp),
            tint = Color(0xFFB0B8C9)
        )
    }
}
