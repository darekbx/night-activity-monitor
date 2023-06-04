package com.darekbx.nightactivitymonitor.system

import android.bluetooth.BluetoothManager

class BluetoothUtils constructor(private val bluetoothManager: BluetoothManager) {

    fun isBluetoothEnabled(): Boolean {
        val adapter = bluetoothManager.adapter
        return when {
            adapter == null -> false
            !adapter.isEnabled -> false
            else -> true
        }
    }
}
