package com.shejan.keywe.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.shejan.keywe.MainActivity
import com.shejan.keywe.R
import com.shejan.keywe.bt.BluetoothHidManager
import com.shejan.keywe.bt.ConnectionStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class KeyweHidService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private lateinit var hidManager: BluetoothHidManager

    companion object {
        const val CHANNEL_ID = "keywe_status_channel"
        const val NOTIFICATION_ID = 1001

        const val ACTION_START = "com.shejan.keywe.action.START_SERVICE"
        const val ACTION_STOP = "com.shejan.keywe.action.STOP_SERVICE"
        const val ACTION_DISCONNECT = "com.shejan.keywe.action.DISCONNECT"

        fun start(context: Context) {
            val intent = Intent(context, KeyweHidService::class.java).apply {
                action = ACTION_START
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, KeyweHidService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        hidManager = BluetoothHidManager.getInstance(applicationContext)
        createNotificationChannel()
        startForegroundWithNotification(buildNotification("Ready / Standby", isConnected = false))
        observeConnectionState()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_DISCONNECT -> {
                hidManager.disconnect()
            }
            ACTION_STOP -> {
                hidManager.stop()
                stopForegroundCompat()
                stopSelf()
                return START_NOT_STICKY
            }
            ACTION_START -> {
                hidManager.start()
            }
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        hidManager.stop()
        stopForegroundCompat()
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        hidManager.stop()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Keywe Connection Status",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows live connection status for Keywe Bluetooth controller"
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(statusText: String, isConnected: Boolean): Notification {
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingOpenApp = PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val disconnectIntent = Intent(this, KeyweHidService::class.java).apply {
            action = ACTION_DISCONNECT
        }
        val pendingDisconnect = PendingIntent.getService(
            this,
            1,
            disconnectIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.keywe_icon)
            .setContentTitle("Keywe Bluetooth Peripheral")
            .setContentText(statusText)
            .setContentIntent(pendingOpenApp)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)

        if (isConnected) {
            builder.addAction(0, "DISCONNECT", pendingDisconnect)
        }

        return builder.build()
    }

    private fun startForegroundWithNotification(notification: Notification) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun updateNotification(statusText: String, isConnected: Boolean) {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        manager?.notify(NOTIFICATION_ID, buildNotification(statusText, isConnected))
    }

    private fun observeConnectionState() {
        serviceScope.launch {
            combine(
                hidManager.connectionStatus,
                hidManager.connectedDeviceName
            ) { status, deviceName ->
                when (status) {
                    ConnectionStatus.CONNECTED -> "Connected: ${deviceName ?: "PC"}" to true
                    ConnectionStatus.CONNECTING -> "Connecting..." to false
                    ConnectionStatus.REGISTERED -> "Ready / Standby" to false
                    ConnectionStatus.REGISTERING -> "Initializing Bluetooth HID..." to false
                    ConnectionStatus.DISCONNECTED -> "Disconnected" to false
                    ConnectionStatus.ERROR -> "Bluetooth Error" to false
                }
            }.collect { (statusText, isConnected) ->
                updateNotification(statusText, isConnected)
            }
        }
    }

    private fun stopForegroundCompat() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            @Suppress("DEPRECATION")
            stopForeground(true)
        }
    }
}
