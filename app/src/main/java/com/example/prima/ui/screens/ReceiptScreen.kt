package com.example.prima.ui.screens

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Print
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.prima.api.models.ReceiptData
import com.example.prima.api.models.Transaction
import com.example.prima.data.bluetooth.BluetoothPrinterManager
import com.example.prima.ui.components.ReceiptContent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceiptScreen(
    transaction: Transaction,
    onDone: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val receiptData = remember(transaction) {
        ReceiptData.fromTransaction(transaction)
    }

    val printerManager = remember { BluetoothPrinterManager(context) }

    var pairedDevices by remember { mutableStateOf(emptyList<BluetoothDevice>()) }
    var selectedDevice by remember { mutableStateOf<BluetoothDevice?>(null) }
    var isPrinting by remember { mutableStateOf(false) }
    var hasBluetoothPermission by remember { mutableStateOf(false) }
    var showDeviceDropdown by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasBluetoothPermission = permissions.values.all { it }
        if (hasBluetoothPermission) {
            pairedDevices = printerManager.getPairedDevices()
            if (pairedDevices.isNotEmpty() && selectedDevice == null) {
                selectedDevice = pairedDevices.first()
            }
        }
    }

    LaunchedEffect(Unit) {
        val neededPermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            listOf(
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_SCAN
            )
        } else {
            emptyList()
        }

        val allGranted = neededPermissions.all { perm ->
            ContextCompat.checkSelfPermission(context, perm) == PackageManager.PERMISSION_GRANTED
        }

        if (allGranted) {
            hasBluetoothPermission = true
            pairedDevices = printerManager.getPairedDevices()
            if (pairedDevices.isNotEmpty() && selectedDevice == null) {
                selectedDevice = pairedDevices.first()
            }
        } else {
            permissionLauncher.launch(neededPermissions.toTypedArray())
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Struk Pembayaran") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Success banner
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Pembayaran Berhasil!",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Transaksi ${transaction.transaction_code}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Receipt preview
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                ReceiptContent(
                    receiptData = receiptData,
                    modifier = Modifier.padding(8.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Printer selection
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Bluetooth,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Printer Bluetooth",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (!hasBluetoothPermission) {
                        Text(
                            text = "Izin Bluetooth diperlukan untuk mencetak",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    } else if (pairedDevices.isEmpty()) {
                        Text(
                            text = "Tidak ada printer Bluetooth yang terpasang. Pasangkan printer dari pengaturan Bluetooth.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        ExposedDropdownMenuBox(
                            expanded = showDeviceDropdown,
                            onExpandedChange = { showDeviceDropdown = it }
                        ) {
                            OutlinedTextField(
                                value = selectedDevice?.name ?: "Pilih printer",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Printer") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showDeviceDropdown) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor()
                            )

                            ExposedDropdownMenu(
                                expanded = showDeviceDropdown,
                                onDismissRequest = { showDeviceDropdown = false }
                            ) {
                                pairedDevices.forEach { device ->
                                    DropdownMenuItem(
                                        text = { Text(device.name ?: device.address) },
                                        onClick = {
                                            selectedDevice = device
                                            showDeviceDropdown = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${pairedDevices.size} printer tersedia",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Print button
            Button(
                onClick = {
                    val device = selectedDevice ?: return@Button
                    isPrinting = true
                    scope.launch {
                        val result = withContext(Dispatchers.IO) {
                            printerManager.printReceipt(device, receiptData)
                        }
                        isPrinting = false
                        result.onSuccess {
                            snackbarHostState.showSnackbar("Struk berhasil dicetak!")
                        }.onFailure { error ->
                            snackbarHostState.showSnackbar("Gagal cetak: ${error.message}")
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = selectedDevice != null && !isPrinting && hasBluetoothPermission,
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Print, contentDescription = null)
                }
            ) {
                if (isPrinting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Mencetak...")
                } else {
                    Text("Cetak Struk")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Done button
            OutlinedButton(
                onClick = onDone,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Selesai")
            }
        }
    }
}
