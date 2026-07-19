package com.example.prima.api.models

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ReceiptData(
    val transactionCode: String,
    val kasirName: String,
    val date: String,
    val items: List<ReceiptItem>,
    val total: Double,
    val paymentMethodName: String,
    val amountPaid: Double,
    val changeAmount: Double
) {
    companion object {
        private const val RECEIPT_WIDTH = 32

        fun fromTransaction(transaction: Transaction): ReceiptData {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val outputFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            val dateStr = try {
                val date = inputFormat.parse(transaction.created_at)
                if (date != null) outputFormat.format(date) else transaction.created_at
            } catch (e: Exception) {
                transaction.created_at
            }

            val items = transaction.details?.map { detail ->
                ReceiptItem(
                    productName = detail.product_name ?: "Produk #${detail.product_id}",
                    quantity = detail.quantity,
                    price = detail.price,
                    subtotal = detail.subtotal
                )
            } ?: emptyList()

            return ReceiptData(
                transactionCode = transaction.transaction_code,
                kasirName = transaction.kasir_name ?: "Kasir",
                date = dateStr,
                items = items,
                total = transaction.total,
                paymentMethodName = transaction.payment_method_name ?: "-",
                amountPaid = transaction.amount_paid ?: 0.0,
                changeAmount = transaction.change_amount ?: 0.0
            )
        }
    }

    fun formatCurrency(amount: Double): String {
        val format = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        format.maximumFractionDigits = 0
        return format.format(amount)
    }

    fun toDisplayText(): String {
        val sb = StringBuilder()
        val line = "=".repeat(RECEIPT_WIDTH)
        val separator = "-".repeat(RECEIPT_WIDTH)

        sb.appendLine(line)
        sb.appendLine(centerText("PRIMA POS"))
        sb.appendLine(centerText("Jl. Contoh No. 123"))
        sb.appendLine(centerText("Telp: 0812-3456-7890"))
        sb.appendLine(line)
        sb.appendLine()
        sb.appendLine("Struk : $transactionCode")
        sb.appendLine("Kasir : $kasirName")
        sb.appendLine("Tanggal: $date")
        sb.appendLine(separator)
        sb.appendLine()

        for (item in items) {
            val name = item.productName.take(18)
            sb.appendLine("$name")
            sb.appendLine("  ${item.quantity}x ${formatCurrency(item.price)} = ${formatCurrency(item.subtotal)}")
        }

        sb.appendLine(separator)
        sb.appendLine(padRight("TOTAL", formatCurrency(total)))
        sb.appendLine(padRight("BAYAR", formatCurrency(amountPaid)))
        sb.appendLine(padRight("KEMBALIAN", formatCurrency(changeAmount)))
        sb.appendLine(separator)
        sb.appendLine("Metode: $paymentMethodName")
        sb.appendLine()
        sb.appendLine(centerText("Terima kasih!"))
        sb.appendLine(centerText("Atas kunjungan Anda"))
        sb.appendLine(line)

        return sb.toString()
    }

    private fun centerText(text: String): String {
        if (text.length >= RECEIPT_WIDTH) return text
        val padding = (RECEIPT_WIDTH - text.length) / 2
        return " ".repeat(padding) + text
    }

    private fun padRight(label: String, value: String): String {
        val spaces = RECEIPT_WIDTH - label.length - value.length
        return label + " ".repeat(maxOf(1, spaces)) + value
    }
}

data class ReceiptItem(
    val productName: String,
    val quantity: Int,
    val price: Double,
    val subtotal: Double
)
