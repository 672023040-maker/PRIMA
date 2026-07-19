package com.example.prima

import com.example.prima.api.models.*
import org.junit.Assert.*
import org.junit.Test

class ReceiptDataTest {

    private fun createTransaction(): Transaction {
        return Transaction(
            id = 1,
            transaction_code = "TRX-12345-ABCDE",
            kasir_id = 1,
            kasir_name = "Kasir A",
            payment_method_id = 1,
            payment_method_name = "Cash",
            total = 43000.0,
            amount_paid = 50000.0,
            change_amount = 7000.0,
            notes = null,
            status = "completed",
            created_at = "2025-07-19T10:30:00",
            details = listOf(
                TransactionDetail(1, 1, 1, "Kopi Hitam", 2, 15000.0, 30000.0),
                TransactionDetail(2, 1, 2, "Nasi Goreng", 1, 13000.0, 13000.0)
            )
        )
    }

    @Test
    fun `fromTransaction creates receipt data correctly`() {
        val transaction = createTransaction()
        val receipt = ReceiptData.fromTransaction(transaction)

        assertEquals("TRX-12345-ABCDE", receipt.transactionCode)
        assertEquals("Kasir A", receipt.kasirName)
        assertEquals(2, receipt.items.size)
        assertEquals(43000.0, receipt.total, 0.01)
        assertEquals(50000.0, receipt.amountPaid, 0.01)
        assertEquals(7000.0, receipt.changeAmount, 0.01)
        assertEquals("Cash", receipt.paymentMethodName)
    }

    @Test
    fun `fromTransaction handles null details`() {
        val transaction = createTransaction().copy(details = null)
        val receipt = ReceiptData.fromTransaction(transaction)

        assertTrue(receipt.items.isEmpty())
    }

    @Test
    fun `fromTransaction handles missing kasir name`() {
        val transaction = createTransaction().copy(kasir_name = null)
        val receipt = ReceiptData.fromTransaction(transaction)

        assertEquals("Kasir", receipt.kasirName)
    }

    @Test
    fun `fromTransaction handles missing payment method name`() {
        val transaction = createTransaction().copy(payment_method_name = null)
        val receipt = ReceiptData.fromTransaction(transaction)

        assertEquals("-", receipt.paymentMethodName)
    }

    @Test
    fun `fromTransaction parses date correctly`() {
        val transaction = createTransaction().copy(created_at = "2025-07-19T10:30:00")
        val receipt = ReceiptData.fromTransaction(transaction)

        assertTrue(receipt.date.contains("19/07/2025"))
    }

    @Test
    fun `toDisplayText contains required sections`() {
        val receipt = ReceiptData.fromTransaction(createTransaction())
        val text = receipt.toDisplayText()

        assertTrue(text.contains("PRIMA POS"))
        assertTrue(text.contains("TRX-12345-ABCDE"))
        assertTrue(text.contains("Kasir A"))
        assertTrue(text.contains("Kopi Hitam"))
        assertTrue(text.contains("Nasi Goreng"))
        assertTrue(text.contains("TOTAL"))
        assertTrue(text.contains("BAYAR"))
        assertTrue(text.contains("KEMBALIAN"))
        assertTrue(text.contains("Cash"))
        assertTrue(text.contains("Terima kasih"))
    }

    @Test
    fun `toDisplayText has correct line width`() {
        val receipt = ReceiptData.fromTransaction(createTransaction())
        val text = receipt.toDisplayText()
        val lines = text.lines()

        for (line in lines) {
            if (line.isNotBlank()) {
                assertTrue(
                    "Line exceeds 32 chars: '${line}' (${line.length})",
                    line.length <= 32
                )
            }
        }
    }

    @Test
    fun `fromTransaction creates receipt items correctly`() {
        val transaction = createTransaction()
        val receipt = ReceiptData.fromTransaction(transaction)

        assertEquals("Kopi Hitam", receipt.items[0].productName)
        assertEquals(2, receipt.items[0].quantity)
        assertEquals(15000.0, receipt.items[0].price, 0.01)
        assertEquals(30000.0, receipt.items[0].subtotal, 0.01)

        assertEquals("Nasi Goreng", receipt.items[1].productName)
        assertEquals(1, receipt.items[1].quantity)
    }
}
