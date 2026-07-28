package com.shejan.keywe

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import com.shejan.keywe.bt.BluetoothHidManager
import com.shejan.keywe.bt.ConnectionStatus
import com.shejan.keywe.ui.components.*
import com.shejan.keywe.ui.keyboard.TactileKeyboard
import com.shejan.keywe.ui.theme.*

enum class ControlMode {
    TOUCHPAD,
    KEYBOARD,
    SPLIT
}

class MainActivity : ComponentActivity() {

    private lateinit var hidManager: BluetoothHidManager

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.entries.all { it.value }
        if (allGranted) {
            hidManager.start()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        hidManager = BluetoothHidManager(this)

        checkAndRequestPermissions()

        setContent {
            KeyweTheme {
                val connectionStatus by hidManager.connectionStatus.collectAsState()
                val connectedDeviceName by hidManager.connectedDeviceName.collectAsState()
                val isScanning by hidManager.isScanning.collectAsState()
                val discoveredDevices by hidManager.discoveredDevices.collectAsState()
                val lastError by hidManager.lastError.collectAsState()

                var currentMode by remember { mutableStateOf(ControlMode.SPLIT) }
                var showDeviceDialog by remember { mutableStateOf(false) }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = PitchBlack
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .systemBarsPadding()
                    ) {
                        // Header Status Bar
                        val (statusColor, statusText) = when (connectionStatus) {
                            ConnectionStatus.CONNECTED -> MatrixGreen to (connectedDeviceName ?: "CONNECTED")
                            ConnectionStatus.CONNECTING -> AmberWarning to "CONNECTING..."
                            ConnectionStatus.REGISTERED -> SignalRed to "PAIRED / READY"
                            ConnectionStatus.REGISTERING -> AmberWarning to "STARTING..."
                            ConnectionStatus.DISCONNECTED -> SignalRed to "DISCONNECTED"
                            ConnectionStatus.ERROR -> SignalRed to "BT ERROR"
                        }

                        DotMatrixHeader(
                            title = "KEYWE",
                            subtitle = if (lastError != null) "ERR: $lastError" else "BLUETOOTH HID PERIPHERAL",
                            statusColor = statusColor,
                            statusText = statusText,
                            modifier = Modifier.clickable { showDeviceDialog = true }
                        )

                        // Mode Selector Bar
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            TactileButton(
                                text = "TOUCHPAD",
                                active = currentMode == ControlMode.TOUCHPAD,
                                onClick = { currentMode = ControlMode.TOUCHPAD },
                                modifier = Modifier.weight(1f)
                            )
                            TactileButton(
                                text = "SPLIT VIEW",
                                active = currentMode == ControlMode.SPLIT,
                                onClick = { currentMode = ControlMode.SPLIT },
                                modifier = Modifier.weight(1f)
                            )
                            TactileButton(
                                text = "KEYBOARD",
                                active = currentMode == ControlMode.KEYBOARD,
                                onClick = { currentMode = ControlMode.KEYBOARD },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Active View Area
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .padding(horizontal = 16.dp)
                        ) {
                            when (currentMode) {
                                ControlMode.TOUCHPAD -> {
                                    com.shejan.keywe.ui.touchpad.TouchpadSurface(
                                        onMouseInput = { buttons, dx, dy, wheel ->
                                            hidManager.sendMouseInput(buttons, dx, dy, wheel)
                                        },
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }

                                ControlMode.KEYBOARD -> {
                                    TactileKeyboard(
                                        onSendKey = { modifiers, keycode ->
                                            val keys = if (keycode != 0.toByte()) byteArrayOf(keycode) else byteArrayOf()
                                            hidManager.sendKeyboardInput(modifiers, keys)
                                        },
                                        keyHeight = 44.dp,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }

                                ControlMode.SPLIT -> {
                                    Column(
                                        modifier = Modifier.fillMaxSize(),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        com.shejan.keywe.ui.touchpad.TouchpadSurface(
                                            onMouseInput = { buttons, dx, dy, wheel ->
                                                hidManager.sendMouseInput(buttons, dx, dy, wheel)
                                            },
                                            modifier = Modifier.weight(1f)
                                        )

                                        TactileKeyboard(
                                            onSendKey = { modifiers, keycode ->
                                                val keys = if (keycode != 0.toByte()) byteArrayOf(keycode) else byteArrayOf()
                                                hidManager.sendKeyboardInput(modifiers, keys)
                                            },
                                            keyHeight = 32.dp,
                                            modifier = Modifier.wrapContentHeight()
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // Device Scanner & Selection Dialog Modal
                    if (showDeviceDialog) {
                        DeviceManagerDialog(
                            pairedDevices = hidManager.getPairedDevices(),
                            discoveredDevices = discoveredDevices,
                            isConnected = connectionStatus == ConnectionStatus.CONNECTED,
                            connectedDeviceName = connectedDeviceName,
                            isScanning = isScanning,
                            lastError = lastError,
                            onStartScan = { hidManager.startScanning() },
                            onMakeDiscoverable = {
                                val discoverableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE).apply {
                                    putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 120)
                                }
                                startActivity(discoverableIntent)
                            },
                            onSelectDevice = { device ->
                                hidManager.connectDevice(device)
                                showDeviceDialog = false
                            },
                            onDisconnect = {
                                hidManager.disconnect()
                            },
                            onDismiss = {
                                hidManager.stopScanning()
                                showDeviceDialog = false
                            }
                        )
                    }
                }
            }
        }
    }

    private fun checkAndRequestPermissions() {
        val permissions = mutableListOf<String>()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissions.add(Manifest.permission.BLUETOOTH_CONNECT)
            permissions.add(Manifest.permission.BLUETOOTH_ADVERTISE)
            permissions.add(Manifest.permission.BLUETOOTH_SCAN)
        } else {
            permissions.add(Manifest.permission.BLUETOOTH)
            permissions.add(Manifest.permission.BLUETOOTH_ADMIN)
            permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        val missing = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (missing.isNotEmpty()) {
            permissionLauncher.launch(missing.toTypedArray())
        } else {
            hidManager.start()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        hidManager.stop()
    }
}

@SuppressLint("MissingPermission")
@Composable
fun DeviceManagerDialog(
    pairedDevices: List<BluetoothDevice>,
    discoveredDevices: List<BluetoothDevice>,
    isConnected: Boolean,
    connectedDeviceName: String?,
    isScanning: Boolean,
    lastError: String?,
    onStartScan: () -> Unit,
    onMakeDiscoverable: () -> Unit,
    onSelectDevice: (BluetoothDevice) -> Unit,
    onDisconnect: () -> Unit,
    onDismiss: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Paired, 1 = Nearby Discovered

    val infiniteTransition = rememberInfiniteTransition(label = "scan_pulse")
    val scanPulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(12.dp))
                .background(CharcoalDark)
                .border(1.dp, GraphiteBorder, RoundedCornerShape(12.dp))
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Dialog Header Title
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "BLUETOOTH DEVICE MANAGER",
                        style = DotMatrixTypography.titleMedium,
                        color = MonochromeWhite,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Tab Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    TactileButton(
                        text = "PAIRED (${pairedDevices.size})",
                        active = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        modifier = Modifier.weight(1f)
                    )
                    TactileButton(
                        text = if (isScanning) "SCANNING..." else "NEARBY (${discoveredDevices.size})",
                        active = selectedTab == 1,
                        onClick = {
                            selectedTab = 1
                            if (!isScanning) onStartScan()
                        },
                        modifier = Modifier.weight(1.2f)
                    )
                }

                if (isConnected) {
                    // Active Connection Banner
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(PitchBlack)
                            .border(1.dp, MatrixGreen, RoundedCornerShape(6.dp))
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "ACTIVE CONNECTION",
                                style = DotMatrixTypography.labelSmall.copy(fontSize = 10.sp),
                                color = MatrixGreen
                            )
                            Text(
                                text = (connectedDeviceName ?: "CONNECTED PC").uppercase(),
                                style = DotMatrixTypography.bodyMedium.copy(fontSize = 13.sp),
                                color = MonochromeWhite,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        TactileButton(
                            text = "DISCONNECT",
                            onClick = onDisconnect,
                            accentColor = SignalRed,
                            active = true
                        )
                    }
                }

                if (lastError != null && !isConnected) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(PitchBlack)
                            .border(1.dp, SignalRed, RoundedCornerShape(6.dp))
                            .padding(8.dp)
                    ) {
                        Text(
                            text = "NOTE: $lastError",
                            style = DotMatrixTypography.labelSmall.copy(fontSize = 10.sp),
                            color = SignalRed
                        )
                    }
                }

                if (selectedTab == 0) {
                    // PAIRED DEVICES LIST
                    if (pairedDevices.isEmpty()) {
                        Text(
                            text = "No paired devices found.\nTap 'NEARBY' tab to scan for your PC.",
                            style = DotMatrixTypography.bodyMedium,
                            color = MonochromeMuted
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.heightIn(max = 200.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            items(pairedDevices) { device ->
                                val deviceName = try {
                                    device.name ?: device.address
                                } catch (_: SecurityException) {
                                    device.address
                                }

                                DeviceItemCard(
                                    name = deviceName,
                                    address = device.address,
                                    isBonded = true,
                                    onClick = { onSelectDevice(device) }
                                )
                            }
                        }
                    }
                } else {
                    // DISCOVERED NEARBY DEVICES LIST
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isScanning) "SCANNING NEARBY..." else "SEARCH COMPLETED",
                            style = DotMatrixTypography.labelSmall.copy(fontSize = 10.sp),
                            color = if (isScanning) AmberWarning else MonochromeMuted,
                            modifier = Modifier.alpha(if (isScanning) scanPulseAlpha else 1.0f)
                        )

                        if (!isScanning) {
                            Text(
                                text = "[ RESCAN ]",
                                style = DotMatrixTypography.labelSmall.copy(fontSize = 10.sp),
                                color = MonochromeWhite,
                                modifier = Modifier.clickable { onStartScan() }
                            )
                        }
                    }

                    if (discoveredDevices.isEmpty()) {
                        Text(
                            text = if (isScanning) "Searching for nearby PCs..." else "No new devices found.\nEnsure Bluetooth is ON & discoverable on your PC.",
                            style = DotMatrixTypography.bodyMedium,
                            color = MonochromeMuted,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.heightIn(max = 200.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            items(discoveredDevices) { device ->
                                val deviceName = try {
                                    device.name ?: "UNKNOWN DEVICE"
                                } catch (_: SecurityException) {
                                    "UNKNOWN DEVICE"
                                }

                                val isDeviceBonded = try {
                                    device.bondState == BluetoothDevice.BOND_BONDED
                                } catch (_: SecurityException) {
                                    false
                                }

                                DeviceItemCard(
                                    name = deviceName,
                                    address = device.address,
                                    isBonded = isDeviceBonded,
                                    onClick = { onSelectDevice(device) }
                                )
                            }
                        }
                    }
                }

                // Help Box for Pairing Gotcha
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(PitchBlack)
                        .border(1.dp, GraphiteBorder, RoundedCornerShape(6.dp))
                        .padding(8.dp)
                ) {
                    Text(
                        text = "TIP: If PC was paired before opening Keywe, remove/unpair on PC, then tap 'MAKE PHONE DISCOVERABLE' & pair from PC.",
                        style = DotMatrixTypography.labelSmall.copy(fontSize = 9.5.sp),
                        color = MonochromeMuted
                    )
                }

                // Action Buttons Row (Make Discoverable & Close)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TactileButton(
                        text = "MAKE DISCOVERABLE",
                        onClick = onMakeDiscoverable,
                        modifier = Modifier.weight(1.4f)
                    )
                    TactileButton(
                        text = "CLOSE",
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@SuppressLint("MissingPermission")
@Composable
fun DeviceItemCard(
    name: String,
    address: String,
    isBonded: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(PitchBlack)
            .border(1.dp, GraphiteBorder, RoundedCornerShape(6.dp))
            .clickable { onClick() }
            .padding(10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = name.uppercase(),
                style = DotMatrixTypography.bodyMedium.copy(fontSize = 12.sp, fontWeight = FontWeight.SemiBold),
                color = MonochromeWhite,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = address,
                style = DotMatrixTypography.labelSmall.copy(fontSize = 9.sp),
                color = MonochromeMuted,
                maxLines = 1
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = if (isBonded) "CONNECT" else "PAIR & CONNECT",
                style = DotMatrixTypography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                color = if (isBonded) MatrixGreen else SignalRed
            )
            StatusIndicatorDot(color = if (isBonded) MatrixGreen else AmberWarning)
        }
    }
}