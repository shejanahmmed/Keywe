package com.shejan.keywe

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
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
import androidx.compose.ui.platform.LocalConfiguration
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
import com.shejan.keywe.ui.keyboard.SystemKeyboardSurface
import com.shejan.keywe.ui.keyboard.TactileKeyboard
import com.shejan.keywe.ui.theme.*

enum class ControlMode {
    TOUCHPAD,
    KEYBOARD,
    SPLIT
}

enum class KeyboardType {
    TACTILE,
    SYSTEM
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
                var selectedKeyboardType by remember { mutableStateOf(KeyboardType.TACTILE) }
                var currentTheme by remember { mutableStateOf(AppThemePreset.MONOCHROME_DARK) }
                var sensitivity by remember { mutableFloatStateOf(1.2f) }
                var hapticsEnabled by remember { mutableStateOf(true) }
                var showDeviceDialog by remember { mutableStateOf(false) }
                var showKeyboardTypeDialog by remember { mutableStateOf(false) }
                var showSettingsDialog by remember { mutableStateOf(false) }

                val configuration = LocalConfiguration.current
                val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

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
                            ConnectionStatus.REGISTERED -> currentTheme.accentColor to "PAIRED / READY"
                            ConnectionStatus.REGISTERING -> AmberWarning to "STARTING..."
                            ConnectionStatus.DISCONNECTED -> currentTheme.accentColor to "DISCONNECTED"
                            ConnectionStatus.ERROR -> SignalRed to "BT ERROR"
                        }

                        if (isLandscape) {
                            // Compact Landscape Top Bar (Header + Controls in one row)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 2.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                DotMatrixHeader(
                                    title = "KEYWE",
                                    subtitle = if (lastError != null) "ERR: $lastError" else "BLUETOOTH HID",
                                    statusColor = statusColor,
                                    statusText = statusText,
                                    onStatusClick = { showDeviceDialog = true },
                                    onOpenSettings = { showSettingsDialog = true },
                                    modifier = Modifier.weight(1f, fill = false)
                                )

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    TactileButton(
                                        text = "TOUCHPAD",
                                        active = currentMode == ControlMode.TOUCHPAD,
                                        accentColor = currentTheme.accentColor,
                                        onClick = { currentMode = ControlMode.TOUCHPAD }
                                    )
                                    TactileButton(
                                        text = "SPLIT VIEW",
                                        active = currentMode == ControlMode.SPLIT,
                                        accentColor = currentTheme.accentColor,
                                        onClick = { currentMode = ControlMode.SPLIT }
                                    )
                                    TactileButton(
                                        text = if (selectedKeyboardType == KeyboardType.SYSTEM) "KEYBOARD (IME)" else "KEYBOARD",
                                        active = currentMode == ControlMode.KEYBOARD,
                                        accentColor = currentTheme.accentColor,
                                        onClick = { currentMode = ControlMode.KEYBOARD },
                                        onLongClick = { showKeyboardTypeDialog = true }
                                    )
                                }
                            }
                        } else {
                            // Standard Portrait Layout (Stacked Header & Controls)
                            DotMatrixHeader(
                                title = "KEYWE",
                                subtitle = if (lastError != null) "ERR: $lastError" else "BLUETOOTH HID PERIPHERAL",
                                statusColor = statusColor,
                                statusText = statusText,
                                onStatusClick = { showDeviceDialog = true },
                                onOpenSettings = { showSettingsDialog = true }
                            )

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                TactileButton(
                                    text = "TOUCHPAD",
                                    active = currentMode == ControlMode.TOUCHPAD,
                                    accentColor = currentTheme.accentColor,
                                    onClick = { currentMode = ControlMode.TOUCHPAD },
                                    modifier = Modifier.weight(1f)
                                )
                                TactileButton(
                                    text = "SPLIT VIEW",
                                    active = currentMode == ControlMode.SPLIT,
                                    accentColor = currentTheme.accentColor,
                                    onClick = { currentMode = ControlMode.SPLIT },
                                    modifier = Modifier.weight(1f)
                                )
                                TactileButton(
                                    text = if (selectedKeyboardType == KeyboardType.SYSTEM) "KEYBOARD (IME)" else "KEYBOARD",
                                    active = currentMode == ControlMode.KEYBOARD,
                                    accentColor = currentTheme.accentColor,
                                    onClick = { currentMode = ControlMode.KEYBOARD },
                                    onLongClick = { showKeyboardTypeDialog = true },
                                    modifier = Modifier.weight(1.2f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(if (isLandscape) 4.dp else 8.dp))

                        // Active View Area
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .padding(horizontal = if (isLandscape) 10.dp else 16.dp)
                        ) {
                            when (currentMode) {
                                ControlMode.TOUCHPAD -> {
                                    com.shejan.keywe.ui.touchpad.TouchpadSurface(
                                        onMouseInput = { buttons, dx, dy, wheel ->
                                            hidManager.sendMouseInput(buttons, dx, dy, wheel)
                                        },
                                        sensitivity = sensitivity,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }

                                ControlMode.KEYBOARD -> {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = if (selectedKeyboardType == KeyboardType.TACTILE) Alignment.BottomCenter else Alignment.TopCenter
                                    ) {
                                        if (selectedKeyboardType == KeyboardType.TACTILE) {
                                            TactileKeyboard(
                                                onSendKey = { modifiers, keycode ->
                                                    val keys = if (keycode != 0.toByte()) byteArrayOf(keycode) else byteArrayOf()
                                                    hidManager.sendKeyboardInput(modifiers, keys)
                                                },
                                                keyHeight = if (isLandscape) 34.dp else 44.dp,
                                                modifier = Modifier.wrapContentHeight()
                                            )
                                        } else {
                                            SystemKeyboardSurface(
                                                onSendKey = { modifiers, keycode ->
                                                    val keys = if (keycode != 0.toByte()) byteArrayOf(keycode) else byteArrayOf()
                                                    hidManager.sendKeyboardInput(modifiers, keys)
                                                },
                                                modifier = Modifier.wrapContentHeight()
                                            )
                                        }
                                    }
                                }

                                ControlMode.SPLIT -> {
                                    if (isLandscape) {
                                        // Landscape Side-by-Side Split View
                                        Row(
                                            modifier = Modifier.fillMaxSize(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            com.shejan.keywe.ui.touchpad.TouchpadSurface(
                                                onMouseInput = { buttons, dx, dy, wheel ->
                                                    hidManager.sendMouseInput(buttons, dx, dy, wheel)
                                                },
                                                sensitivity = sensitivity,
                                                modifier = Modifier.weight(0.42f)
                                            )

                                            Box(
                                                modifier = Modifier
                                                    .weight(0.58f)
                                                    .fillMaxHeight(),
                                                contentAlignment = Alignment.BottomCenter
                                            ) {
                                                if (selectedKeyboardType == KeyboardType.TACTILE) {
                                                    TactileKeyboard(
                                                        onSendKey = { modifiers, keycode ->
                                                            val keys = if (keycode != 0.toByte()) byteArrayOf(keycode) else byteArrayOf()
                                                            hidManager.sendKeyboardInput(modifiers, keys)
                                                        },
                                                        keyHeight = 30.dp,
                                                        modifier = Modifier.wrapContentHeight()
                                                    )
                                                } else {
                                                    SystemKeyboardSurface(
                                                        onSendKey = { modifiers, keycode ->
                                                            val keys = if (keycode != 0.toByte()) byteArrayOf(keycode) else byteArrayOf()
                                                            hidManager.sendKeyboardInput(modifiers, keys)
                                                        },
                                                        modifier = Modifier.wrapContentHeight()
                                                    )
                                                }
                                            }
                                        }
                                    } else {
                                        // Portrait Vertical Stacked Split View
                                        Column(
                                            modifier = Modifier.fillMaxSize(),
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            com.shejan.keywe.ui.touchpad.TouchpadSurface(
                                                onMouseInput = { buttons, dx, dy, wheel ->
                                                    hidManager.sendMouseInput(buttons, dx, dy, wheel)
                                                },
                                                sensitivity = sensitivity,
                                                modifier = Modifier.weight(1f)
                                            )

                                            if (selectedKeyboardType == KeyboardType.TACTILE) {
                                                TactileKeyboard(
                                                    onSendKey = { modifiers, keycode ->
                                                        val keys = if (keycode != 0.toByte()) byteArrayOf(keycode) else byteArrayOf()
                                                        hidManager.sendKeyboardInput(modifiers, keys)
                                                    },
                                                    keyHeight = 32.dp,
                                                    modifier = Modifier.wrapContentHeight()
                                                )
                                            } else {
                                                SystemKeyboardSurface(
                                                    onSendKey = { modifiers, keycode ->
                                                        val keys = if (keycode != 0.toByte()) byteArrayOf(keycode) else byteArrayOf()
                                                        hidManager.sendKeyboardInput(modifiers, keys)
                                                    },
                                                    modifier = Modifier.wrapContentHeight()
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(if (isLandscape) 4.dp else 12.dp))
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

                    // Keyboard Engine Type Selector Dialog
                    if (showKeyboardTypeDialog) {
                        KeyboardTypeSelectorDialog(
                            selectedType = selectedKeyboardType,
                            onSelectType = { selectedKeyboardType = it },
                            onDismiss = { showKeyboardTypeDialog = false }
                        )
                    }

                    // Settings Dialog Modal (Theme, Preferences/Modify, About)
                    if (showSettingsDialog) {
                        com.shejan.keywe.ui.components.SettingsDialog(
                            currentTheme = currentTheme,
                            onSelectTheme = { currentTheme = it },
                            sensitivity = sensitivity,
                            onSensitivityChange = { sensitivity = it },
                            hapticsEnabled = hapticsEnabled,
                            onHapticsToggle = { hapticsEnabled = it },
                            onDismiss = { showSettingsDialog = false }
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

@Composable
fun KeyboardTypeSelectorDialog(
    selectedType: KeyboardType,
    onSelectType: (KeyboardType) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(12.dp))
                .background(CharcoalDark)
                .border(1.dp, GraphiteBorder, RoundedCornerShape(12.dp))
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "SELECT KEYBOARD ENGINE",
                    style = DotMatrixTypography.titleMedium,
                    color = MonochromeWhite
                )

                Text(
                    text = "Choose how you want to input text to your PC:",
                    style = DotMatrixTypography.bodyMedium,
                    color = MonochromeMuted
                )

                // Option 1: Tactile Keyboard
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (selectedType == KeyboardType.TACTILE) PitchBlack else CharcoalDark)
                        .border(
                            1.dp,
                            if (selectedType == KeyboardType.TACTILE) SignalRed else GraphiteBorder,
                            RoundedCornerShape(8.dp)
                        )
                        .clickable {
                            onSelectType(KeyboardType.TACTILE)
                            onDismiss()
                        }
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "TACTILE KEYBOARD (BUILT-IN)",
                            style = DotMatrixTypography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = MonochromeWhite
                        )
                        Text(
                            text = "Retro mechanical keycaps with Win, Alt, Ctrl, Shift & hotkeys.",
                            style = DotMatrixTypography.labelSmall.copy(fontSize = 10.sp),
                            color = MonochromeMuted
                        )
                    }
                    if (selectedType == KeyboardType.TACTILE) {
                        StatusIndicatorDot(color = SignalRed)
                    }
                }

                // Option 2: System Keyboard (Phone IME)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (selectedType == KeyboardType.SYSTEM) PitchBlack else CharcoalDark)
                        .border(
                            1.dp,
                            if (selectedType == KeyboardType.SYSTEM) MatrixGreen else GraphiteBorder,
                            RoundedCornerShape(8.dp)
                        )
                        .clickable {
                            onSelectType(KeyboardType.SYSTEM)
                            onDismiss()
                        }
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "SYSTEM KEYBOARD (PHONE IME)",
                            style = DotMatrixTypography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = MonochromeWhite
                        )
                        Text(
                            text = "Use your phone's default soft keyboard (Swipe, Voice typing, IME).",
                            style = DotMatrixTypography.labelSmall.copy(fontSize = 10.sp),
                            color = MonochromeMuted
                        )
                    }
                    if (selectedType == KeyboardType.SYSTEM) {
                        StatusIndicatorDot(color = MatrixGreen)
                    }
                }

                TactileButton(
                    text = "CLOSE",
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
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
    var selectedTab by remember { mutableIntStateOf(0) }

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