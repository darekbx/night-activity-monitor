package com.darekbx.nightactivitymonitor

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.darekbx.nightactivitymonitor.ble.BluetoothService
import com.darekbx.nightactivitymonitor.ble.BluetoothService.Companion.DEVICE_STATUS_ACTION
import com.darekbx.nightactivitymonitor.system.RequestPermission
import com.darekbx.nightactivitymonitor.ui.DeviceUiState
import com.darekbx.nightactivitymonitor.ui.MainScreen
import com.darekbx.nightactivitymonitor.ui.SystemViewModel
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {

    val viewModel: SystemViewModel by inject()

    private val deviceStatusReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == DEVICE_STATUS_ACTION) {
                val isActive = intent.getBooleanExtra(BluetoothService.DEVICE_STATUS, false)
                viewModel.updateDeviceUiState(
                    if (isActive) DeviceUiState.DeviceUiActive
                    else DeviceUiState.ConnectionInProgress
                )
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Black
                ) {
                    RequestPermission(requiredPermissions) {
                        if (viewModel.isBluetoothEnabled()) {
                            MainScreen(viewModel)
                        } else {
                            BluetoothDisabled()
                        }
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        registerReceiver(deviceStatusReceiver, IntentFilter(DEVICE_STATUS_ACTION))
    }

    override fun onStop() {
        super.onStop()
        try {
            unregisterReceiver(deviceStatusReceiver)
        } catch (e: Exception) { }
    }

    private val requiredPermissions by lazy {
        arrayOf(
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.FOREGROUND_SERVICE
        )
    }
}

@Preview
@Composable
fun BluetoothDisabled() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = "Please enable Bluetooth!",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color(210, 40, 20, 190)
        )
    }
}
