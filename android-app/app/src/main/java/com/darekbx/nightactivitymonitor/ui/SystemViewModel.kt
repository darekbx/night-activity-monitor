package com.darekbx.nightactivitymonitor.ui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.darekbx.nightactivitymonitor.ble.BluetoothService
import com.darekbx.nightactivitymonitor.repository.LocalRepository
import com.darekbx.nightactivitymonitor.system.BluetoothUtils
import kotlinx.coroutines.launch

sealed class DeviceUiState {
    object Idle : DeviceUiState()
    object ConnectionInProgress : DeviceUiState()
    object DeviceUiActive : DeviceUiState()
}

sealed class ServiceUiState {
    object Disconnected : ServiceUiState()
    object Connected : ServiceUiState()
}

class SystemViewModel(
    private val bluetoothUtils: BluetoothUtils,
    private val localRepository: LocalRepository
) : ViewModel() {

    private val _deviceUiState = mutableStateOf<DeviceUiState>(DeviceUiState.Idle)
    val deviceUiState: State<DeviceUiState>
        get() = _deviceUiState

    private val _serviceUiState = mutableStateOf<ServiceUiState>(ServiceUiState.Disconnected)
    val serviceUiState: State<ServiceUiState>
        get() = _serviceUiState

    init {
        _serviceUiState.value =
            if (BluetoothService.IS_SERVICE_ACTIVE) ServiceUiState.Connected
            else ServiceUiState.Disconnected

        if (BluetoothService.IS_SERVICE_ACTIVE) {
            _deviceUiState.value = DeviceUiState.DeviceUiActive
        }
    }

    val logs = localRepository.logFlow()

    fun updateDeviceUiState(state: DeviceUiState) {
        _deviceUiState.value = state
    }

    fun deleteAll() {
        viewModelScope.launch {
            localRepository.deleteAll()
        }
    }

    fun isBluetoothEnabled() = bluetoothUtils.isBluetoothEnabled()

    fun startService(context: Context) {
        if (BluetoothService.IS_SERVICE_ACTIVE) {
            return
        }

        _deviceUiState.value = DeviceUiState.ConnectionInProgress
        _serviceUiState.value = ServiceUiState.Connected
        context.startForegroundService(serviceIntent(context))
    }

    fun stopService(context: Context) {
        if (!BluetoothService.IS_SERVICE_ACTIVE) {
            return
        }

        _serviceUiState.value = ServiceUiState.Disconnected
        context.stopService(serviceIntent(context))
    }

    private fun serviceIntent(context: Context): Intent {
        return Intent(context, BluetoothService::class.java)
    }
}
