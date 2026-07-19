package com.example.prima.data.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.os.Build
import com.dantsu.mt3200 escposprinter.connection.BluetoothConnection
import com.dantsu.mt3200 escposprinter.EscPosPrinter
import com.dantsu.mt3200 escposprinter.connection.ConnectionResult
import com.dantsu.mt3200 escposprinter.exceptions.EscPosBarcodeException
import com.dantsu.mt3200 escposprinter.exceptions.EscPosConnectionException
import com.dantsu.mt3200 escposprinter.exceptions.EscPosEncodingException
import com.dantsu.mt3200 escposprinter.textparser.IEscPosTextParser
import com.example.prima.api.models.ReceiptData

class BluetoothPrinterManager(private val context: Context) {

    @SuppressLint("MissingPermission")
    fun getPairedDevices(): List<BluetoothDevice> {
        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
            ?: return emptyList()
        val adapter = bluetoothManager.adapter ?: return emptyList()

        if (!adapter.isEnabled) return emptyList()

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            adapter.bondedDevices?.toList() ?: emptyList()
        } else {
            @Suppress("DEPRECATION")
            adapter.bondedDevices?.toList() ?: emptyList()
        }
    }

    @SuppressLint("MissingPermission")
    fun isBluetoothEnabled(): Boolean {
        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
            ?: return false
        val adapter = bluetoothManager.adapter ?: return false
        return adapter.isEnabled
    }

    fun printReceipt(device: BluetoothDevice, receiptData: ReceiptData): Result<Unit> {
        return try {
            val connection = BluetoothConnection(device)
            val printer = EscPosPrinter(connection, 203, 48f, 32)

            val text = receiptData.toDisplayText()
            val parsedText = text.replace("\n", "\n")

            printer.printFormattedTextAndFeed(parsedText, 5)

            connection.disconnect()
            Result.success(Unit)
        } catch (e: EscPosConnectionException) {
            Result.failure(Exception("Gagal koneksi ke printer: ${e.message}"))
        } catch (e: EscPosBarcodeException) {
            Result.failure(Exception("Error cetak barcode: ${e.message}"))
        } catch (e: EscPosEncodingException) {
            Result.failure(Exception("Error encoding: ${e.message}"))
        } catch (e: Exception) {
            Result.failure(Exception("Gagal cetak struk: ${e.message}"))
        }
    }
}
