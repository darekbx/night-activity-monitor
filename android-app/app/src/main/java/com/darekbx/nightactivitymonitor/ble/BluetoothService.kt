package com.darekbx.nightactivitymonitor.ble

import android.annotation.SuppressLint
import android.app.Service
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.IBinder
import android.util.Log
import com.darekbx.nightactivitymonitor.repository.LocalRepository
import com.darekbx.nightactivitymonitor.system.NotificationUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import no.nordicsemi.android.ble.BleManager
import no.nordicsemi.android.ble.data.Data
import no.nordicsemi.android.support.v18.scanner.BluetoothLeScannerCompat
import no.nordicsemi.android.support.v18.scanner.ScanCallback
import no.nordicsemi.android.support.v18.scanner.ScanFilter
import no.nordicsemi.android.support.v18.scanner.ScanResult
import no.nordicsemi.android.support.v18.scanner.ScanSettings
import org.koin.android.ext.android.inject
import java.util.UUID

class BluetoothService: Service() {

    enum class DeviceStatus {
        CONNECTING,
        CONNECTED,
        NOTIFICATIONS_SET,
        DISCONNECTED,
        FAILED
    }

    companion object {
        const val DEVICE_NAME = "NightMonitor"
        const val TAG = "NightMonitor"

        var IS_SERVICE_ACTIVE = false
        val SERVICE_UUID: UUID = UUID.fromString("89409171-FE10-40B7-80A3-398A8C219855")//.lowercase())
        val NOTIFICATION_UUID:UUID = UUID.fromString("89409171-FE10-40AA-80A3-398A8C219855")//.lowercase())

            const val DEVICE_STATUS_ACTION = "deviceStatusAction"
            const val DEVICE_STATUS = "deviceStatus"
    }

    private val notificationUtil: NotificationUtil by inject()
    private var clientManager: ClientManager? = null

    private val localRepository: LocalRepository by inject()

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        IS_SERVICE_ACTIVE = true

        scanForDevices()

        val notification = notificationUtil.createNotification(
            "Current readings",
            "unknown"
        )
        startForeground(
            NotificationUtil.NOTIFICATION_ID,
            notification,
            ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE
        )
        Log.v(TAG, "Service created")
    }

    override fun onDestroy() {
        super.onDestroy()
        stopScanner()
        disableBleServices()
        IS_SERVICE_ACTIVE = false
    }

    private fun scanForDevices() {
        val scanner = BluetoothLeScannerCompat.getScanner()
        val settings: ScanSettings = ScanSettings.Builder()
            .setLegacy(false)
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .setReportDelay(5000)
            .setUseHardwareBatchingIfSupported(true)
            .build()
        val filters: MutableList<ScanFilter> = ArrayList()
        filters.add(ScanFilter.Builder().setDeviceName(DEVICE_NAME).build())
        scanner.startScan(filters, settings, scanCallback)
    }

    private val scanCallback = object: ScanCallback() {

        @SuppressLint("MissingPermission")
        override fun onBatchScanResults(results: MutableList<ScanResult>) {
            super.onBatchScanResults(results)
            Log.v(TAG, "Scan batch result: ${results}")
            results
                .firstOrNull { result ->
                    Log.v(TAG, "Check device ${result.device.name} == $DEVICE_NAME")
                    result.device.name == DEVICE_NAME
                }
                ?.let {
                    addDevice(it.device)
                    stopScanner()
                }
        }

        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)
            Log.v(TAG, "Scan result: ${result.device}")
            addDevice(result.device)
            stopScanner()
        }
    }

    private fun stopScanner() {
        val scanner = BluetoothLeScannerCompat.getScanner()
        scanner.stopScan(scanCallback)
    }

    private fun disableBleServices() {
        clientManager?.disconnect()
        clientManager?.close()
    }

    private fun addDevice(device: BluetoothDevice) {
        Log.v(TAG, "Connect device: ${device}")
        clientManager = ClientManager()
        clientManager!!.connect(device).useAutoConnect(true).enqueue()
    }

    private inner class ClientManager: BleManager(this) {

        override fun getGattCallback(): BleManagerGattCallback = GattCallback()

        private inner class GattCallback : BleManagerGattCallback() {

            private var myCharacteristic: BluetoothGattCharacteristic? = null

            override fun isRequiredServiceSupported(gatt: BluetoothGatt): Boolean {
                val service = gatt.getService(SERVICE_UUID)
                myCharacteristic =
                    service?.getCharacteristic(NOTIFICATION_UUID)
                val myCharacteristicProperties = myCharacteristic?.properties ?: 0
                return myCharacteristicProperties and BluetoothGattCharacteristic.PROPERTY_NOTIFY != 0
            }

            override fun initialize() {
                Log.v(TAG, "Initialize connection")
                setNotificationCallback(myCharacteristic).with { _, data ->
                    Log.v(TAG, "Notification data")
                    if (data.value != null) {
                        parseNotificationData(data)
                    }
                }

                notifyStatus(DeviceStatus.CONNECTING)
                beginAtomicRequestQueue()
                    .add(enableNotifications(myCharacteristic)
                        .fail { _: BluetoothDevice?, status: Int ->
                            Log.v(TAG, "Failed to enable notification")
                            notifyStatus(DeviceStatus.FAILED)
                            disconnect().enqueue()
                        }
                    )
                    .done {
                        Log.v(TAG, "Notifications connected")
                        notifyStatus(DeviceStatus.CONNECTED)
                        notifyStatus(DeviceStatus.NOTIFICATIONS_SET)
                    }
                    .enqueue()
            }

            override fun onServicesInvalidated() {
                myCharacteristic = null
            }
        }
    }

    private fun parseNotificationData(data: Data) {
        Log.v(TAG, "Received data: $data")

        val value = String(data.value!!, Charsets.UTF_8)
        notificationUtil.updateNotification("Movements detected: $value")

        CoroutineScope(Dispatchers.IO).launch {
            localRepository.notifyDetectedMovement()
        }
    }

    private fun notifyStatus(deviceStatus: DeviceStatus) {
        Log.v(TAG, "Notify device status: $deviceStatus")
        sendBroadcast(Intent(DEVICE_STATUS_ACTION).apply {
            when (deviceStatus) {
                DeviceStatus.NOTIFICATIONS_SET -> putExtra(DEVICE_STATUS, true)
                DeviceStatus.DISCONNECTED -> putExtra(DEVICE_STATUS, false)
                else -> { /* do nothing */ }
            }
        })
    }
}
