package com.shejan.keywe.bt

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothHidDevice
import android.bluetooth.BluetoothHidDeviceAppSdpSettings
import android.bluetooth.BluetoothProfile
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.Executors

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

    private val bluetoothAdapter: BluetoothAdapter? by lazy {
        BluetoothAdapter.getDefaultAdapter()
    }

    private var hidDevice: BluetoothHidDevice? = null
    private var connectedDevice: BluetoothDevice? = null

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

    private val executor = Executors.newSingleThreadExecutor()

    private val discoveryReceiver = object : BroadcastReceiver() {
        override fun onReceive(ctx: Context?, intent: Intent?) {
            val device: BluetoothDevice? = intent?.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
            when (intent?.action) {
                BluetoothDevice.ACTION_FOUND -> {
                    if (device != null) {
                        val currentList = _discoveredDevices.value.toMutableList()
                        if (currentList.none { it.address == device.address }) {
                            currentList.add(device)
                            _discoveredDevices.value = currentList
                            Log.d(tag, "Discovered device: ${device.name} [${device.address}]")
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
                BluetoothDevice.ACTION_BOND_STATE_CHANGED -> {
                    val bondState = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.BOND_NONE)
                    Log.d(tag, "Bond state changed: device=${device?.name}, bondState=$bondState")
                    if (bondState == BluetoothDevice.BOND_BONDED && device != null) {
                        connectDevice(device)
                    }
                }
                BluetoothDevice.ACTION_ACL_CONNECTED -> {
                    Log.d(tag, "ACL Connected: device=${device?.name}")
                    if (device != null && device.bondState == BluetoothDevice.BOND_BONDED) {
                        if (connectedDevice == null && hidDevice != null) {
                            Log.d(tag, "Attempting HID connect following ACL_CONNECTED for ${device.name}")
                            hidDevice?.connect(device)
                        }
                    }
                }
                BluetoothDevice.ACTION_ACL_DISCONNECTED -> {
                    Log.d(tag, "ACL Disconnected: device=${device?.name}")
                    if (device != null && connectedDevice?.address == device.address) {
                        connectedDevice = null
                        _connectedDeviceName.value = null
                        _connectionStatus.value = ConnectionStatus.REGISTERED
                    }
                }
            }
        }
    }

    private val serviceListener = object : BluetoothProfile.ServiceListener {
        override fun onServiceConnected(profile: Int, proxy: BluetoothProfile) {
            if (profile == BluetoothProfile.HID_DEVICE) {
                Log.d(tag, "Bluetooth HID Profile Proxy connected")
                hidDevice = proxy as BluetoothHidDevice
                registerHidApp()
            }
        }

        override fun onServiceDisconnected(profile: Int) {
            if (profile == BluetoothProfile.HID_DEVICE) {
                Log.d(tag, "Bluetooth HID Profile Proxy disconnected")
                hidDevice = null
                _connectionStatus.value = ConnectionStatus.DISCONNECTED
                _connectedDeviceName.value = null
            }
        }
    }

    private val hidCallback = object : BluetoothHidDevice.Callback() {
        override fun onAppStatusChanged(pluggedDevice: BluetoothDevice?, registered: Boolean) {
            Log.d(tag, "onAppStatusChanged: registered=$registered, device=${pluggedDevice?.name}")
            if (registered) {
                _connectionStatus.value = ConnectionStatus.REGISTERED
                _lastError.value = null

                if (pluggedDevice != null) {
                    connectedDevice = pluggedDevice
                    _connectedDeviceName.value = pluggedDevice.name ?: pluggedDevice.address
                    _connectionStatus.value = ConnectionStatus.CONNECTED
                } else {
                    val connectedList = hidDevice?.getDevicesMatchingConnectionStates(
                        intArrayOf(BluetoothProfile.STATE_CONNECTED)
                    )
                    if (!connectedList.isNullOrEmpty()) {
                        connectedDevice = connectedList[0]
                        _connectedDeviceName.value = connectedDevice?.name ?: connectedDevice?.address
                        _connectionStatus.value = ConnectionStatus.CONNECTED
                    }
                }
            } else {
                _connectionStatus.value = ConnectionStatus.DISCONNECTED
                _lastError.value = "HID SDP Registration Failed"
            }
        }

        override fun onConnectionStateChanged(device: BluetoothDevice, state: Int) {
            Log.d(tag, "onConnectionStateChanged: state=$state for device=${device.name}")
            when (state) {
                BluetoothProfile.STATE_CONNECTED -> {
                    connectedDevice = device
                    _connectedDeviceName.value = device.name ?: device.address
                    _connectionStatus.value = ConnectionStatus.CONNECTED
                    _lastError.value = null
                }
                BluetoothProfile.STATE_CONNECTING -> {
                    _connectionStatus.value = ConnectionStatus.CONNECTING
                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    if (connectedDevice?.address == device.address || connectedDevice == null) {
                        connectedDevice = null
                        _connectedDeviceName.value = null
                        _connectionStatus.value = ConnectionStatus.REGISTERED
                    }
                }
            }
        }
    }

    /**
     * Initializes the Bluetooth HID Service & registers receivers.
     */
    fun start() {
        if (bluetoothAdapter == null || !bluetoothAdapter!!.isEnabled) {
            Log.e(tag, "Bluetooth is not enabled or available")
            _connectionStatus.value = ConnectionStatus.ERROR
            _lastError.value = "Bluetooth is turned off"
            return
        }

        val filter = IntentFilter().apply {
            addAction(BluetoothDevice.ACTION_FOUND)
            addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
            addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
            addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
            addAction(BluetoothDevice.ACTION_ACL_CONNECTED)
            addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
        }
        try {
            context.registerReceiver(discoveryReceiver, filter)
        } catch (e: Exception) {
            Log.e(tag, "Failed to register discovery receiver: ${e.message}")
        }

        _connectionStatus.value = ConnectionStatus.REGISTERING
        bluetoothAdapter?.getProfileProxy(context, serviceListener, BluetoothProfile.HID_DEVICE)
    }

    /**
     * Starts scanning for nearby Bluetooth devices.
     */
    fun startScanning() {
        if (bluetoothAdapter == null || !bluetoothAdapter!!.isEnabled) return
        if (bluetoothAdapter!!.isDiscovering) {
            bluetoothAdapter!!.cancelDiscovery()
        }
        _discoveredDevices.value = emptyList()
        val started = bluetoothAdapter!!.startDiscovery()
        Log.d(tag, "startDiscovery result: $started")
    }

    /**
     * Stops scanning for nearby devices.
     */
    fun stopScanning() {
        if (bluetoothAdapter != null && bluetoothAdapter!!.isDiscovering) {
            bluetoothAdapter!!.cancelDiscovery()
        }
        _isScanning.value = false
    }

    /**
     * Registers the SDP settings for standard combo Keyboard & Mouse peripheral.
     */
    private fun registerHidApp() {
        val sdpSettings = BluetoothHidDeviceAppSdpSettings(
            "Keywe Controller",
            "Tactile Remote",
            "Keywe",
            0xC0.toByte(),
            HidReportDescriptor.COMBO_DESCRIPTOR
        )

        val registered = hidDevice?.registerApp(
            sdpSettings,
            null,
            null,
            executor,
            hidCallback
        ) ?: false

        if (!registered) {
            Log.e(tag, "Failed to register HID App SDP settings")
            _connectionStatus.value = ConnectionStatus.ERROR
            _lastError.value = "Could not register HID Profile"
        }
    }

    /**
     * Connects to a target paired host PC or initiates pairing if unbonded.
     */
    fun connectDevice(device: BluetoothDevice): Boolean {
        stopScanning()
        if (hidDevice == null) {
            Log.e(tag, "hidDevice is null. Cannot connect.")
            _connectionStatus.value = ConnectionStatus.ERROR
            _lastError.value = "HID Service not ready"
            return false
        }

        if (device.bondState == BluetoothDevice.BOND_BONDED) {
            _connectionStatus.value = ConnectionStatus.CONNECTING
            val success = hidDevice?.connect(device) ?: false
            Log.d(tag, "hidDevice.connect() returned $success for ${device.name}")
            if (!success) {
                _lastError.value = "PC rejected incoming connection. Tap 'Connect' from Windows PC Bluetooth Settings."
                _connectionStatus.value = ConnectionStatus.REGISTERED
            }
            return success
        } else {
            Log.d(tag, "Device not bonded, initiating createBond for ${device.name}")
            _connectionStatus.value = ConnectionStatus.CONNECTING
            return device.createBond()
        }
    }

    /**
     * Disconnects from current PC host.
     */
    fun disconnect() {
        connectedDevice?.let { hidDevice?.disconnect(it) }
        connectedDevice = null
        _connectedDeviceName.value = null
        _connectionStatus.value = ConnectionStatus.REGISTERED
    }

    /**
     * Stops and unregisters the HID Service & receivers.
     */
    fun stop() {
        try {
            stopScanning()
            context.unregisterReceiver(discoveryReceiver)
        } catch (e: Exception) {
            Log.e(tag, "Error unregistering receiver: ${e.message}")
        }

        try {
            hidDevice?.unregisterApp()
            bluetoothAdapter?.closeProfileProxy(BluetoothProfile.HID_DEVICE, hidDevice)
        } catch (e: Exception) {
            Log.e(tag, "Error stopping HID Manager: ${e.message}")
        }
    }

    /**
     * Sends relative mouse movement, button states, and scroll.
     */
    fun sendMouseInput(buttons: Byte, dx: Byte, dy: Byte, wheel: Byte = 0) {
        val device = connectedDevice ?: return
        val report = HidReportDescriptor.createMouseReport(buttons, dx, dy, wheel)
        hidDevice?.sendReport(device, HidReportDescriptor.MOUSE_REPORT_ID.toInt(), report)
    }

    /**
     * Sends keyboard modifier state and active keycodes.
     */
    fun sendKeyboardInput(modifiers: Byte, keys: ByteArray = byteArrayOf(0, 0, 0, 0, 0, 0)) {
        val device = connectedDevice ?: return
        val report = HidReportDescriptor.createKeyboardReport(modifiers, keys)
        hidDevice?.sendReport(device, HidReportDescriptor.KEYBOARD_REPORT_ID.toInt(), report)
    }

    /**
     * Returns list of currently paired devices (e.g. PC).
     */
    fun getPairedDevices(): List<BluetoothDevice> {
        return bluetoothAdapter?.bondedDevices?.toList() ?: emptyList()
    }
}
