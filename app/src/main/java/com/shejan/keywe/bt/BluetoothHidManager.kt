package com.shejan.keywe.bt

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothHidDevice
import android.bluetooth.BluetoothHidDeviceAppSdpSettings
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

enum class ConnectionStatus {
    DISCONNECTED,
    REGISTERING,
    REGISTERED,
    CONNECTING,
    CONNECTED,
    ERROR
}

@SuppressLint("MissingPermission")
class BluetoothHidManager(private val context: Context) {

    private val tag = "BluetoothHidManager"

    // --- Bluetooth Adapter ---
    private val bluetoothAdapter: BluetoothAdapter? = try {
        val mgr = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        mgr.adapter
    } catch (e: Exception) {
        null
    }

    // --- State Flows ---
    private val _connectionStatus = MutableStateFlow(ConnectionStatus.DISCONNECTED)
    val connectionStatus: StateFlow<ConnectionStatus> = _connectionStatus.asStateFlow()

    private val _connectedDeviceName = MutableStateFlow<String?>(null)
    val connectedDeviceName: StateFlow<String?> = _connectedDeviceName.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val _discoveredDevices = MutableStateFlow<List<BluetoothDevice>>(emptyList())
    val discoveredDevices: StateFlow<List<BluetoothDevice>> = _discoveredDevices.asStateFlow()

    private val _lastError = MutableStateFlow<String?>(null)
    val lastError: StateFlow<String?> = _lastError.asStateFlow()

    // --- Internal State ---
    private var hidDevice: BluetoothHidDevice? = null
    private var connectedDevice: BluetoothDevice? = null
    private var isAppRegistered = false
    private var isDiscoveryReceiverRegistered = false
    private var isBondReceiverRegistered = false
    private var connectionTimeoutFuture: ScheduledFuture<*>? = null

    // --- Dedicated Executors ---
    // Background scheduled executor for connection lifecycle and HID registration
    private val bgExecutor = Executors.newSingleThreadScheduledExecutor { r ->
        Thread({
            android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND)
            r.run()
        }, "keywe-bt-manager")
    }

    // Foreground-priority executor dedicated solely to sending HID reports
    private val reportExecutor = Executors.newSingleThreadExecutor { r ->
        Thread({
            android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_FOREGROUND)
            r.run()
        }, "keywe-bt-reports")
    }

    // --- SDP Settings ---
    private val sdpSettings = BluetoothHidDeviceAppSdpSettings(
        "Keywe Controller",
        "Tactile Keyboard & Mouse",
        "Keywe",
        BluetoothHidDevice.SUBCLASS1_KEYBOARD,
        HidReportDescriptor.COMBO_DESCRIPTOR
    )

    // =========================================================================
    // RECEIVERS
    // =========================================================================

    private val discoveryReceiver = object : BroadcastReceiver() {
        override fun onReceive(ctx: Context?, intent: Intent?) {
            val action = intent?.action ?: return
            val device: BluetoothDevice? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java)
            } else {
                @Suppress("DEPRECATION")
                intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
            }
            when (action) {
                BluetoothDevice.ACTION_FOUND -> {
                    if (device != null) {
                        val current = _discoveredDevices.value
                        if (current.none { it.address == device.address }) {
                            _discoveredDevices.value = current + device
                        }
                    }
                }
                BluetoothAdapter.ACTION_DISCOVERY_STARTED -> {
                    _isScanning.value = true
                    _discoveredDevices.value = emptyList()
                    Log.d(tag, "Bluetooth discovery started")
                }
                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    _isScanning.value = false
                    Log.d(tag, "Bluetooth discovery finished")
                }
                BluetoothDevice.ACTION_ACL_DISCONNECTED -> {
                    // PC turned off Bluetooth or walked out of range
                    if (device != null && connectedDevice?.address == device.address) {
                        connectionTimeoutFuture?.cancel(false)
                        connectedDevice = null
                        _connectedDeviceName.value = null
                        _connectionStatus.value = ConnectionStatus.REGISTERED
                        Log.d(tag, "ACL link dropped for ${device.name} — marking as disconnected")
                    }
                }
                BluetoothDevice.ACTION_ACL_CONNECTED -> {
                    // Remote side established the ACL link (e.g. Windows auto-connecting)
                    if (device != null && device.bondState == BluetoothDevice.BOND_BONDED) {
                        Log.d(tag, "ACL connected for bonded device ${device.name}")
                    }
                }
            }
        }
    }

    private val bondStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(ctx: Context?, intent: Intent?) {
            val action = intent?.action ?: return
            if (action == BluetoothAdapter.ACTION_STATE_CHANGED) {
                checkBluetoothCapabilities()
                return
            }
            if (action != BluetoothDevice.ACTION_BOND_STATE_CHANGED) return

            val device: BluetoothDevice? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java)
            } else {
                @Suppress("DEPRECATION")
                intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
            }
            val bondState = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.BOND_NONE)
            val prevBondState = intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, BluetoothDevice.BOND_NONE)

            if (device != null) {
                when (bondState) {
                    BluetoothDevice.BOND_BONDED -> {
                        Log.d(tag, "Bond established with ${device.name} — checking connection state")
                        bgExecutor.schedule({
                            val isAlreadyConnected = try {
                                hidDevice?.connectedDevices?.contains(device) == true
                            } catch (_: Exception) { false }

                            if (!isAlreadyConnected && connectedDevice?.address != device.address) {
                                connectDevice(device)
                            } else {
                                Log.d(tag, "Device ${device.name} already auto-connected upon bonding")
                            }
                        }, 1500, TimeUnit.MILLISECONDS)
                    }
                    BluetoothDevice.BOND_NONE -> {
                        if (prevBondState == BluetoothDevice.BOND_BONDING) {
                            _lastError.value = "Pairing refused or failed"
                            Log.w(tag, "Pairing with ${device.name} refused or failed")
                        }
                    }
                }
            }
        }
    }

    // =========================================================================
    // PROFILE SERVICE LISTENER
    // =========================================================================

    private val serviceListener = object : BluetoothProfile.ServiceListener {
        override fun onServiceConnected(profile: Int, proxy: BluetoothProfile) {
            if (profile != BluetoothProfile.HID_DEVICE) return
            Log.d(tag, "HID Device Profile proxy connected")
            val hid = proxy as BluetoothHidDevice
            hidDevice = hid
            try {
                // Use getDevicesMatchingConnectionStates for a real-time verified check
                // This avoids restoring a stale "connected" device from the previous session
                val trulyConnected = hid.getDevicesMatchingConnectionStates(
                    intArrayOf(BluetoothProfile.STATE_CONNECTED)
                )?.firstOrNull()
                if (trulyConnected != null) {
                    connectedDevice = trulyConnected
                    _connectedDeviceName.value = trulyConnected.name ?: trulyConnected.address
                    _connectionStatus.value = ConnectionStatus.CONNECTED
                    _lastError.value = null
                    Log.d(tag, "Verified active HID connection to ${trulyConnected.name}")
                } else {
                    // No verified live connection — clear any stale state
                    connectedDevice = null
                    _connectedDeviceName.value = null
                    Log.d(tag, "No verified live connection found on proxy attach")
                }
            } catch (e: Exception) {
                // If the check itself fails, clear the stale state to be safe
                connectedDevice = null
                _connectedDeviceName.value = null
                Log.w(tag, "Could not verify connection state on proxy attach: ${e.message}")
            }
            registerHidApp()
        }

        override fun onServiceDisconnected(profile: Int) {
            if (profile != BluetoothProfile.HID_DEVICE) return
            Log.d(tag, "HID Device Profile proxy disconnected — will rebind on BT toggle")
            hidDevice = null
            isAppRegistered = false
        }
    }

    // =========================================================================
    // HID DEVICE CALLBACK
    // =========================================================================

    private val hidCallback = object : BluetoothHidDevice.Callback() {
        override fun onAppStatusChanged(pluggedDevice: BluetoothDevice?, registered: Boolean) {
            Log.d(tag, "onAppStatusChanged: registered=$registered device=${pluggedDevice?.name}")
            isAppRegistered = registered
            if (registered) {
                _lastError.value = null
                val active = try {
                    hidDevice?.connectedDevices?.firstOrNull()
                } catch (e: Exception) { null }

                if (active != null) {
                    connectedDevice = active
                    _connectedDeviceName.value = active.name ?: active.address
                    _connectionStatus.value = ConnectionStatus.CONNECTED
                    Log.d(tag, "HID registered — already connected to ${active.name}")
                } else {
                    _connectionStatus.value = ConnectionStatus.REGISTERED
                    Log.d(tag, "HID registered — awaiting host connection")
                }
            } else {
                isAppRegistered = false
                if (bluetoothAdapter?.isEnabled == true) {
                    _connectionStatus.value = ConnectionStatus.REGISTERED
                    _lastError.value = null
                } else {
                    _connectionStatus.value = ConnectionStatus.ERROR
                    _lastError.value = "HID SDP Registration Failed"
                }
                Log.d(tag, "HID app un-registered")
            }
        }

        override fun onConnectionStateChanged(device: BluetoothDevice, state: Int) {
            Log.d(tag, "onConnectionStateChanged: state=$state device=${device.name}")
            when (state) {
                BluetoothProfile.STATE_CONNECTED -> {
                    connectionTimeoutFuture?.cancel(false)
                    connectedDevice = device
                    _connectedDeviceName.value = device.name ?: device.address
                    _connectionStatus.value = ConnectionStatus.CONNECTED
                    _lastError.value = null
                    Log.d(tag, "Connected to ${device.name}")
                }
                BluetoothProfile.STATE_CONNECTING -> {
                    _connectionStatus.value = ConnectionStatus.CONNECTING
                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    connectionTimeoutFuture?.cancel(false)
                    if (connectedDevice?.address == device.address) {
                        connectedDevice = null
                        _connectedDeviceName.value = null
                    }
                    _connectionStatus.value = ConnectionStatus.REGISTERED
                    Log.d(tag, "Disconnected from ${device.name}")
                }
            }
        }

        override fun onGetReport(device: BluetoothDevice, type: Byte, id: Byte, bufferSize: Int) {
            val dummy = when (id) {
                HidReportDescriptor.MOUSE_REPORT_ID -> byteArrayOf(0, 0, 0, 0)
                HidReportDescriptor.KEYBOARD_REPORT_ID -> byteArrayOf(0, 0, 0, 0, 0, 0, 0, 0)
                else -> ByteArray(bufferSize.coerceAtMost(8))
            }
            hidDevice?.replyReport(device, type, id, dummy)
        }

        override fun onSetReport(device: BluetoothDevice, type: Byte, id: Byte, data: ByteArray) {
            hidDevice?.reportError(device, BluetoothHidDevice.ERROR_RSP_SUCCESS)
        }

        override fun onSetProtocol(device: BluetoothDevice, protocol: Byte) {
            hidDevice?.reportError(device, BluetoothHidDevice.ERROR_RSP_SUCCESS)
        }

        override fun onVirtualCableUnplug(device: BluetoothDevice) {
            Log.d(tag, "Virtual cable unplugged from ${device.name}")
            connectedDevice = null
            _connectedDeviceName.value = null
            _connectionStatus.value = ConnectionStatus.REGISTERED
        }
    }

    // =========================================================================
    // CORE CONNECTION ENGINE
    // =========================================================================

    fun checkBluetoothCapabilities() {
        val adapter = bluetoothAdapter
        if (adapter == null) {
            _connectionStatus.value = ConnectionStatus.ERROR
            _lastError.value = "Bluetooth hardware not found"
            return
        }
        if (!adapter.isEnabled) {
            hidDevice = null
            isAppRegistered = false
            _connectionStatus.value = ConnectionStatus.DISCONNECTED
            _lastError.value = "Bluetooth is turned off"
            return
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val pm = android.content.pm.PackageManager.PERMISSION_GRANTED
            val ok = context.checkSelfPermission(android.Manifest.permission.BLUETOOTH_CONNECT) == pm &&
                     context.checkSelfPermission(android.Manifest.permission.BLUETOOTH_ADVERTISE) == pm &&
                     context.checkSelfPermission(android.Manifest.permission.BLUETOOTH_SCAN) == pm
            if (!ok) {
                _connectionStatus.value = ConnectionStatus.ERROR
                _lastError.value = "Bluetooth permissions required"
                return
            }
        }
        val hid = hidDevice
        when {
            hid == null -> initProfileProxy()
            !isAppRegistered -> registerHidApp()
            else -> {
                try {
                    // Verify live connection state — never trust stale cached state
                    val trulyConnected = hid.getDevicesMatchingConnectionStates(
                        intArrayOf(BluetoothProfile.STATE_CONNECTED)
                    )?.firstOrNull()
                    if (trulyConnected != null) {
                        // Already connected — update state without touching HID registration
                        connectedDevice = trulyConnected
                        _connectedDeviceName.value = trulyConnected.name ?: trulyConnected.address
                        _connectionStatus.value = ConnectionStatus.CONNECTED
                        _lastError.value = null
                        Log.d(tag, "checkBluetoothCapabilities: already connected to ${trulyConnected.name} — skipping re-registration")
                    } else {
                        connectedDevice = null
                        _connectedDeviceName.value = null
                        _connectionStatus.value = ConnectionStatus.REGISTERED
                    }
                } catch (e: Exception) {
                    Log.w(tag, "checkBtCapabilities sync error: ${e.message}")
                }
            }
        }
    }

    fun start() {
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
            _connectionStatus.value = ConnectionStatus.ERROR
            _lastError.value = "Bluetooth is turned off"
            return
        }
        _lastError.value = null
        registerDiscoveryReceiver()
        registerBondReceiver()

        // If already registered and connected, do NOT re-register — this causes the
        // Android Bluetooth stack to briefly drop the HID profile and triggers the
        // system "Can't connect to [device]" Toast notification on the host PC.
        val hid = hidDevice
        if (hid != null && isAppRegistered) {
            val trulyConnected = try {
                hid.getDevicesMatchingConnectionStates(intArrayOf(BluetoothProfile.STATE_CONNECTED))?.firstOrNull()
            } catch (_: Exception) { null }
            if (trulyConnected != null) {
                connectedDevice = trulyConnected
                _connectedDeviceName.value = trulyConnected.name ?: trulyConnected.address
                _connectionStatus.value = ConnectionStatus.CONNECTED
                Log.d(tag, "start(): already connected to ${trulyConnected.name} — skipping re-registration")
                return
            }
        }

        if (_connectionStatus.value == ConnectionStatus.ERROR) {
            _connectionStatus.value = ConnectionStatus.REGISTERING
        }
        checkBluetoothCapabilities()
    }

    private fun initProfileProxy() {
        _connectionStatus.value = ConnectionStatus.REGISTERING
        _lastError.value = null
        Log.d(tag, "Requesting HID Device Profile proxy…")
        try {
            val ok = bluetoothAdapter?.getProfileProxy(context, serviceListener, BluetoothProfile.HID_DEVICE) ?: false
            if (!ok) {
                _connectionStatus.value = ConnectionStatus.ERROR
                _lastError.value = "HID Device Profile not supported on this device"
                Log.e(tag, "getProfileProxy returned false")
            }
        } catch (e: Exception) {
            _connectionStatus.value = ConnectionStatus.ERROR
            _lastError.value = "HID service unavailable"
            Log.e(tag, "getProfileProxy threw: ${e.message}", e)
        }
    }

    private fun registerHidApp() {
        val hid = hidDevice ?: run {
            _connectionStatus.value = ConnectionStatus.ERROR
            _lastError.value = "HID Service Proxy Unavailable"
            return
        }

        // Guard: If already registered and a live connection exists, skip the
        // unregister → re-register cycle entirely. Calling unregisterApp() while
        // connected causes the OS to momentarily revoke the HID profile, which
        // triggers a "Can't connect to [device]" system Toast on the host PC.
        if (isAppRegistered) {
            val trulyConnected = try {
                hid.getDevicesMatchingConnectionStates(intArrayOf(BluetoothProfile.STATE_CONNECTED))?.firstOrNull()
            } catch (_: Exception) { null }
            if (trulyConnected != null) {
                Log.d(tag, "registerHidApp(): already registered and connected to ${trulyConnected.name} — skipping")
                return
            }
        }

        bgExecutor.execute {
            Log.d(tag, "Registering HID app…")
            try { hid.unregisterApp() } catch (_: Exception) {}
            try { Thread.sleep(300) } catch (_: InterruptedException) {}
            try {
                val registered = hid.registerApp(sdpSettings, null, null, bgExecutor, hidCallback)
                if (!registered) {
                    Log.e(tag, "hid.registerApp() returned false")
                    _connectionStatus.value = ConnectionStatus.ERROR
                    _lastError.value = "HID SDP Registration Failed"
                } else {
                    Log.d(tag, "HID app registration initiated successfully")
                }
            } catch (e: Exception) {
                Log.e(tag, "registerApp threw: ${e.message}", e)
                _connectionStatus.value = ConnectionStatus.ERROR
                _lastError.value = "HID registration error"
            }
        }
    }

    fun restartHidService() {
        Log.d(tag, "Restarting HID service…")
        val hid = hidDevice
        if (hid == null) { initProfileProxy(); return }
        _lastError.value = null
        bgExecutor.execute {
            try { hid.unregisterApp() } catch (_: Exception) {}
            try { Thread.sleep(300) } catch (_: InterruptedException) {}
            try { hid.registerApp(sdpSettings, null, null, bgExecutor, hidCallback) } catch (e: Exception) {
                Log.e(tag, "restartHidService registerApp threw: ${e.message}", e)
            }
        }
    }

    // =========================================================================
    // SCANNING
    // =========================================================================

    fun startScanning() {
        val adapter = bluetoothAdapter ?: return
        if (!adapter.isEnabled) return
        _discoveredDevices.value = emptyList()
        registerDiscoveryReceiver()
        try {
            if (adapter.isDiscovering) adapter.cancelDiscovery()
            val started = adapter.startDiscovery()
            if (started) { _isScanning.value = true }
            Log.d(tag, "startDiscovery() -> $started")
        } catch (e: Exception) {
            Log.e(tag, "startScanning error: ${e.message}", e)
        }
    }

    fun stopScanning() {
        try {
            if (bluetoothAdapter?.isDiscovering == true) bluetoothAdapter.cancelDiscovery()
        } catch (e: Exception) { Log.w(tag, "stopScanning error: ${e.message}") }
        _isScanning.value = false
    }

    // =========================================================================
    // PAIRING & CONNECTING
    // =========================================================================

    fun connectDevice(device: BluetoothDevice): Boolean {
        val hid = hidDevice ?: run {
            _connectionStatus.value = ConnectionStatus.ERROR
            _lastError.value = "HID Service not ready"
            return false
        }
        stopScanning()
        if (device.bondState != BluetoothDevice.BOND_BONDED) {
            Log.d(tag, "Initiating pairing with ${device.name}")
            _connectionStatus.value = ConnectionStatus.CONNECTING
            _lastError.value = null
            return try { device.createBond() } catch (e: Exception) { false }
        }
        _connectionStatus.value = ConnectionStatus.CONNECTING
        _lastError.value = null
        connectionTimeoutFuture?.cancel(false)

        bgExecutor.execute {
            try {
                val alreadyConnected = try {
                    hid.connectedDevices?.contains(device) == true || connectedDevice?.address == device.address
                } catch (_: Exception) { false }

                if (alreadyConnected) {
                    Log.d(tag, "Device ${device.name} is already connected — updating state")
                    connectedDevice = device
                    _connectedDeviceName.value = device.name ?: device.address
                    _connectionStatus.value = ConnectionStatus.CONNECTED
                    _lastError.value = null
                    return@execute
                }

                val ok = hid.connect(device)
                if (ok) {
                    scheduleConnectionTimeout(device)
                    Log.d(tag, "hid.connect() succeeded for ${device.name}")
                } else {
                    Log.w(tag, "hid.connect() returned false — retrying after 250ms")
                    try { Thread.sleep(250) } catch (_: InterruptedException) {}
                    val retry = hid.connect(device)
                    if (retry) {
                        scheduleConnectionTimeout(device)
                        Log.d(tag, "hid.connect() retry succeeded for ${device.name}")
                    } else {
                        _lastError.value = "Host rejected connection. Pair from PC Bluetooth settings."
                        _connectionStatus.value = ConnectionStatus.REGISTERED
                        Log.e(tag, "hid.connect() retry also failed for ${device.name}")
                    }
                }
            } catch (e: Exception) {
                _lastError.value = "Connection error: ${e.localizedMessage}"
                _connectionStatus.value = ConnectionStatus.REGISTERED
                Log.e(tag, "connectDevice bg error: ${e.message}", e)
            }
        }
        return true
    }

    private fun scheduleConnectionTimeout(device: BluetoothDevice) {
        connectionTimeoutFuture?.cancel(false)
        connectionTimeoutFuture = bgExecutor.schedule({
            if (_connectionStatus.value == ConnectionStatus.CONNECTING) {
                _lastError.value = "Connection timed out. Try again."
                _connectionStatus.value = ConnectionStatus.REGISTERED
                Log.w(tag, "Connection to ${device.name ?: device.address} timed out")
            }
        }, 10, TimeUnit.SECONDS)
    }

    fun disconnect() {
        connectionTimeoutFuture?.cancel(false)
        val dev = connectedDevice
        if (dev != null) {
            try { hidDevice?.disconnect(dev) } catch (e: Exception) { Log.w(tag, "disconnect error: ${e.message}") }
        }
        connectedDevice = null
        _connectedDeviceName.value = null
        _connectionStatus.value = ConnectionStatus.REGISTERED
    }

    // =========================================================================
    // HID REPORT SENDERS
    // =========================================================================

    fun sendMouseInput(buttons: Byte, dx: Byte, dy: Byte, wheel: Byte = 0) {
        val device = connectedDevice ?: return
        val hid = hidDevice ?: return
        val report = HidReportDescriptor.createMouseReport(buttons, dx, dy, wheel)
        reportExecutor.submit {
            try { hid.sendReport(device, HidReportDescriptor.MOUSE_REPORT_ID.toInt(), report) }
            catch (e: Exception) { Log.e(tag, "sendMouseInput error: ${e.message}") }
        }
    }

    fun sendKeyboardInput(modifiers: Byte, keys: ByteArray = byteArrayOf(0, 0, 0, 0, 0, 0)) {
        val device = connectedDevice ?: return
        val hid = hidDevice ?: return
        val report = HidReportDescriptor.createKeyboardReport(modifiers, keys)
        reportExecutor.submit {
            try { hid.sendReport(device, HidReportDescriptor.KEYBOARD_REPORT_ID.toInt(), report) }
            catch (e: Exception) { Log.e(tag, "sendKeyboardInput error: ${e.message}") }
        }
    }

    // =========================================================================
    // LIFECYCLE & CLEANUP
    // =========================================================================

    fun stop() {
        connectionTimeoutFuture?.cancel(false)
        stopScanning()
        unregisterDiscoveryReceiver()
        unregisterBondReceiver()
        try {
            hidDevice?.unregisterApp()
            bluetoothAdapter?.closeProfileProxy(BluetoothProfile.HID_DEVICE, hidDevice)
        } catch (e: Exception) { Log.e(tag, "stop cleanup error: ${e.message}") }
        hidDevice = null
        isAppRegistered = false
        connectedDevice = null
    }

    fun getPairedDevices(): List<BluetoothDevice> {
        return try { bluetoothAdapter?.bondedDevices?.toList() ?: emptyList() }
        catch (e: Exception) { emptyList() }
    }

    // =========================================================================
    // RECEIVER HELPERS
    // =========================================================================

    private fun registerDiscoveryReceiver() {
        if (isDiscoveryReceiverRegistered) return
        try {
            val filter = IntentFilter().apply {
                addAction(BluetoothDevice.ACTION_FOUND)
                addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
                addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
                // Critical: detect when PC drops the Bluetooth link (BT off, out of range)
                addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
                addAction(BluetoothDevice.ACTION_ACL_CONNECTED)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.registerReceiver(discoveryReceiver, filter, Context.RECEIVER_EXPORTED)
            } else {
                context.registerReceiver(discoveryReceiver, filter)
            }
            isDiscoveryReceiverRegistered = true
        } catch (e: Exception) { Log.e(tag, "Failed to register discovery receiver: ${e.message}") }
    }

    private fun unregisterDiscoveryReceiver() {
        if (!isDiscoveryReceiverRegistered) return
        try { context.unregisterReceiver(discoveryReceiver) } catch (e: Exception) { Log.w(tag, "Error unregistering discovery receiver: ${e.message}") }
        isDiscoveryReceiverRegistered = false
    }

    private fun registerBondReceiver() {
        if (isBondReceiverRegistered) return
        try {
            val filter = IntentFilter().apply {
                addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
                addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.registerReceiver(bondStateReceiver, filter, Context.RECEIVER_EXPORTED)
            } else {
                context.registerReceiver(bondStateReceiver, filter)
            }
            isBondReceiverRegistered = true
        } catch (e: Exception) { Log.e(tag, "Failed to register bond receiver: ${e.message}") }
    }

    private fun unregisterBondReceiver() {
        if (!isBondReceiverRegistered) return
        try { context.unregisterReceiver(bondStateReceiver) } catch (e: Exception) { Log.w(tag, "Error unregistering bond receiver: ${e.message}") }
        isBondReceiverRegistered = false
    }
}
