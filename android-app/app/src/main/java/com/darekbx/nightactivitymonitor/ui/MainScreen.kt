@file:OptIn(ExperimentalFoundationApi::class)

package com.darekbx.nightactivitymonitor.ui

import android.content.Context
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.darekbx.nightactivitymonitor.model.MinuteSpan

@Composable
fun MainScreen(viewModel: SystemViewModel) {
    val serviceState by viewModel.serviceUiState
    val deviceState by viewModel.deviceUiState
    val context = LocalContext.current
    val spans by viewModel.logs.collectAsState(initial = emptyList())

    Column(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .weight(1F)
                .fillMaxWidth()
                .background(Color.LightGray), contentAlignment = Alignment.Center
        ) {
            when (deviceState) {
                is DeviceUiState.Idle -> LogsView(spans) { viewModel.deleteAll() }
                is DeviceUiState.DeviceUiActive -> LogsView(spans) { viewModel.deleteAll() }
                is DeviceUiState.ConnectionInProgress -> CircularProgressIndicator(Modifier.size(48.dp))
            }
        }
        Column(
            modifier = Modifier
                .weight(0.1F)
                .fillMaxWidth()
        ) {
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Color.DarkGray)
            )
            ServiceControl(serviceState, viewModel, context)
        }
    }
}

@Composable
private fun LogsView(items: List<MinuteSpan>, deleteAll: () -> Unit = { }) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        stickyHeader {
            Button(onClick = { deleteAll() }) {
                Text(text = "Delete all")
            }
        }
        items(items) { span ->
            Text(text = "$span -> count: ${span.items.count()}")
        }
    }
}

@Composable
private fun ServiceControl(
    serviceState: ServiceUiState,
    viewModel: SystemViewModel,
    context: Context
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (serviceState == ServiceUiState.Connected) {
            Text(text = "Service is running", color = Color(-10044566))
            Button(onClick = { viewModel.stopService(context) }) {
                Text(text = "Stop service")
            }
        } else {
            Text(text = "Service is idle", color = Color(-2039584))
            Button(onClick = { viewModel.startService(context) }) {
                Text(text = "Start service")
            }
        }
    }
}