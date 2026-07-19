package com.example.prima.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.prima.api.models.ReceiptData
import java.text.NumberFormat
import java.util.Locale

@Composable
fun ReceiptContent(
    receiptData: ReceiptData,
    modifier: Modifier = Modifier
) {
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("id", "ID")).apply {
        maximumFractionDigits = 0
    }

    Column(
        modifier = modifier
            .width(280.dp)
            .background(MaterialTheme.colorScheme.surface)
            .padding(12.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        Text(
            text = "=".repeat(32),
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "PRIMA POS",
            fontFamily = FontFamily.Monospace,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "Jl. Contoh No. 123",
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "Telp: 0812-3456-7890",
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "=".repeat(32),
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Transaction info
        ReceiptInfoLine(label = "Struk", value = receiptData.transactionCode)
        ReceiptInfoLine(label = "Kasir", value = receiptData.kasirName)
        ReceiptInfoLine(label = "Tanggal", value = receiptData.date)

        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "-".repeat(32),
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurface
        )

        // Items
        Spacer(modifier = Modifier.height(4.dp))
        for (item in receiptData.items) {
            Text(
                text = item.productName.take(22),
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.fillMaxWidth()
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "  ${item.quantity}x ${currencyFormat.format(item.price)}",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${currencyFormat.format(item.subtotal)}  ",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "-".repeat(32),
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurface
        )

        // Totals
        ReceiptInfoLine(label = "TOTAL", value = currencyFormat.format(receiptData.total))
        ReceiptInfoLine(label = "BAYAR", value = currencyFormat.format(receiptData.amountPaid))
        ReceiptInfoLine(label = "KEMBALIAN", value = currencyFormat.format(receiptData.changeAmount))

        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "-".repeat(32),
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
        ReceiptInfoLine(label = "Metode", value = receiptData.paymentMethodName)

        Spacer(modifier = Modifier.height(8.dp))

        // Footer
        Text(
            text = "Terima kasih!",
            fontFamily = FontFamily.Monospace,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "Atas kunjungan Anda",
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "=".repeat(32),
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun ReceiptInfoLine(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = value,
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
