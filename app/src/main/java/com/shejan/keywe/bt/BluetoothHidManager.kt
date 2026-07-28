package com.shejan.keywe.bt

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothHidDevice
import android.bluetooth.BluetoothHidDeviceAppQosSettings
import android.bluetooth.BluetoothHidDeviceAppSdpSettings
import android.bluetooth.BluetoothProfile
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Handler
import android.os.Looper
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
    private val mainHandler = Handler(Looper.getMainLooper())

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
                            performHidConnect(device)
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
                    _lastError.value = null
                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    if (connectedDevice?.address == device.address) {
                        connectedDevice = null
                        _connectedDeviceName.value = null
                        _connectionStatus.value = ConnectionStatus.REGISTERED
                    }
                }
            }
        }

        override fun onGetReport(device: BluetoothDevice, type: Byte, id: Byte, bufferSize: Int) {
            Log.d(tag, "onGetReport: device=${device.name}, type=$type, id=$id, bufferSize=$bufferSize")
            val dummyReport = when (id) {
                HidReportDescriptor.MOUSE_REPORT_ID -> byteArrayOf(0, 0, 0, 0)
                HidReportDescriptor.KEYBOARD_REPORT_ID -> byteArrayOf(0, 0, 0, 0, 0, 0, 0, 0)
                else -> ByteArray(bufferSize.coerceAtMost(8))
            }
            val ok = hidDevice?.replyReport(device, type, id, dummyReport) ?: false
            Log.d(tag, "replyReport result: $ok")
        }

        override fun onSetReport(device: BluetoothDevice, type: Byte, id: Byte, data: ByteArray) {
            Log.d(tag, "onSetReport: device=${device.name}, type=$type, id=$id, dataSize=${data.size}")
            val ok = hidDevice?.reportError(device, BluetoothHidDevice.ERROR_RSP_SUCCESS) ?: false
            Log.d(tag, "reportError result: $ok")
        }

        override fun onSetProtocol(device: BluetoothDevice, protocol: Byte) {
            Log.d(tag, "onSetProtocol: device=${device.name}, protocol=$protocol")
            val ok = hidDevice?.reportError(device, BluetoothHidDevice.ERROR_RSP_SUCCESS) ?: false
            Log.d(tag, "reportError for setProtocol result: $ok")
        }

        override fun onVirtualCableUnplug(device: BluetoothDevice) {
            Log.d(tag, "onVirtualCableUnplug for device=${device.name}")
            connectedDevice = null
            _connectedDeviceName.value = null
            _connectionStatus.value = ConnectionStatus.REGISTERED
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
     * Registers the SDP settings & QoS parameters with multi-stage fallback for all device ROMs.
     */
    private fun registerHidApp() {
        if (hidDevice == null) {
            _connectionStatus.value = ConnectionStatus.ERROR
            _lastError.value = "HID Service Proxy Unavailable"
            return
        }

        // Unregister any previous leftover app registration to ensure clean SDP state
        try {
            hidDevice?.unregisterApp()
        } catch (_: Exception) {}

        val comboSdp = BluetoothHidDeviceAppSdpSettings(
            "Keywe Controller",
            "Tactile Remote",
            "Keywe",
            0xC0.toByte(),
            HidReportDescriptor.COMBO_DESCRIPTOR
        )

        val qos = BluetoothHidDeviceAppQosSettings(
            BluetoothHidDeviceAppQosSettings.SERVICE_BEST_EFFORT,
            800, 900, 0, 0, 0
        )

        // Stage 1: Try Combo SDP with QoS
        var registered = try {
            hidDevice?.registerApp(comboSdp, qos, qos, executor, hidCallback) ?: false
        } catch (e: Exception) {
            Log.w(tag, "Stage 1 registerApp failed: ${e.message}")
            false
        }

        // Stage 2: Try Combo SDP with null QoS
        if (!registered) {
            Log.w(tag, "Retrying Stage 2 registerApp with null QoS...")
            registered = try {
                hidDevice?.registerApp(comboSdp, null, null, executor, hidCallback) ?: false
            } catch (e: Exception) {
                Log.w(tag, "Stage 2 registerApp failed: ${e.message}")
                false
            }
        }

        // Stage 3: Try Mouse Subclass (0x80) with null QoS
        if (!registered) {
            Log.w(tag, "Retrying Stage 3 registerApp with Subclass 0x80...")
            val mouseSdp = BluetoothHidDeviceAppSdpSettings(
                "Keywe Controller",
                "Tactile Remote",
                "Keywe",
                0x80.toByte(),
                HidReportDescriptor.COMBO_DESCRIPTOR
            )
            registered = try {
                hidDevice?.registerApp(mouseSdp, null, null, executor, hidCallback) ?: false
            } catch (e: Exception) {
                Log.w(tag, "Stage 3 registerApp failed: ${e.message}")
                false
            }
        }

        // Stage 4: Try Keyboard Subclass (0x40) with null QoS
        if (!registered) {
            Log.w(tag, "Retrying Stage 4 registerApp with Subclass 0x40...")
            val kbSdp = BluetoothHidDeviceAppSdpSettings(
                "Keywe Controller",
                "Tactile Remote",
                "Keywe",
                0x40.toByte(),
                HidReportDescriptor.COMBO_DESCRIPTOR
            )
            registered = try {
                hidDevice?.registerApp(kbSdp, null, null, executor, hidCallback) ?: false
            } catch (e: Exception) {
                Log.w(tag, "Stage 4 registerApp failed: ${e.message}")
                false
            }
        }

        if (!registered) {
            Log.e(tag, "All HID SDP Registration attempts failed.")
            _connectionStatus.value = ConnectionStatus.ERROR
            _lastError.value = "HID SDP Registration Failed"
        } else {
            Log.d(tag, "HID SDP Registration successfully initiated.")
        }
    }

    /**
     * Connects to a target paired host PC or initiates pairing if unbonded.
     */
    fun connectDevice(device: BluetoothDevice): Boolean {
        _lastError.value = null
        if (hidDevice == null) {
            Log.e(tag, "hidDevice is null. Cannot connect.")
            _connectionStatus.value = ConnectionStatus.ERROR
            _lastError.value = "HID Service not ready"
            return false
        }

        if (device.bondState == BluetoothDevice.BOND_BONDED) {
            _connectionStatus.value = ConnectionStatus.CONNECTING

            // If Bluetooth discovery was active, cancel discovery and delay connection to allow BT chip to settle
            val wasDiscovering = bluetoothAdapter?.isDiscovering == true
            stopScanning()

            if (wasDiscovering) {
                mainHandler.postDelayed({
                    performHidConnect(device)
                }, 350)
                return true
            } else {
                return performHidConnect(device)
            }
        } else {
            Log.d(tag, "Device not bonded, initiating createBond for ${device.name}")
            _connectionStatus.value = ConnectionStatus.CONNECTING
            return device.createBond()
        }
    }

    private fun performHidConnect(device: BluetoothDevice): Boolean {
        if (hidDevice == null) return false
        val success = hidDevice?.connect(device) ?: false
        Log.d(tag, "performHidConnect() returned $success for ${device.name}")
        if (!success) {
            _lastError.value = "PC rejected connection. Pair from Windows PC Bluetooth Settings."
            _connectionStatus.value = ConnectionStatus.REGISTERED
        }
        return success
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
