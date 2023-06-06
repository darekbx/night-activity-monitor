@file:OptIn(ExperimentalTextApi::class)

package com.darekbx.nightactivitymonitor.ui

import android.content.Context
import androidx.compose.foundation.Canvas
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.darekbx.nightactivitymonitor.model.MinuteSpan

@Composable
fun MainScreen(viewModel: SystemViewModel) {
    val serviceState by viewModel.serviceUiState
    val deviceState by viewModel.deviceUiState
    val context = LocalContext.current
    val spans by viewModel.logs.collectAsState(initial = emptyList())
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .weight(1F)
                .fillMaxWidth()
                .background(Color.LightGray), contentAlignment = Alignment.Center
        ) {
            when (deviceState) {
                is DeviceUiState.Idle,
                is DeviceUiState.DeviceUiActive -> TabScreen(spans)

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
            ServiceControl(serviceState, viewModel, context) {
                showDeleteConfirmation = true
            }
        }
    }

    if (showDeleteConfirmation) {
        ConfirmationDialog(
            message = "Delete all?",
            confirmButtonText = "Yes",
            onDismiss = { showDeleteConfirmation = false }) {
            viewModel.deleteAll()
        }
    }
}

@Composable
fun TabScreen(items: List<MinuteSpan>) {
    var tabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("List", "Chart")
    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = tabIndex) {
            tabs.forEachIndexed { index, title ->
                Tab(text = { Text(title) },
                    selected = tabIndex == index,
                    onClick = { tabIndex = index }
                )
            }
        }
        when (tabIndex) {
            0 -> LogsView(items)
            1 -> ChartView(items)
        }
    }
}


@Composable
private fun LogsView(items: List<MinuteSpan>) {
    LazyColumn(
        modifier = Modifier
            .background(Color.White)
            .fillMaxSize()
            .padding(16.dp)
    ) {
        items(items) { span ->
            Text(text = "$span -> count: ${span.items.count()}")
        }
    }
}

@Composable
private fun ChartView(items: List<MinuteSpan>) {
        Chart(
            modifier = Modifier
                .padding(top = 192.dp, bottom = 192.dp)
                .background(Color.White)
                .fillMaxSize(),
            items
        )
}

@Composable
private fun Chart(
    modifier: Modifier = Modifier,
    records: List<MinuteSpan>
) {
    if (records.isEmpty() || records.all { it.items.isEmpty() }) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = "No data")
        }
        return
    }
    val textMeasure = rememberTextMeasurer()

    Canvas(modifier = modifier, onDraw = {

        val bottomPadding = 50.dp.toPx()
        val itemsToSkip = 1

        val width = size.width - 50
        val chunkWidth = width / (records.size - itemsToSkip)
        val maximum = records.maxOf { it.items.size }
        val minimum = records.minOf { it.items.size }
        val chunkHeightScale = (size.height - bottomPadding) / (maximum - minimum)

        var previousLevel = records.first().items.size
        var x = 0F

        translate(left = 25f, top = 0f) {

            // Minimum line
            drawLine(
                Color.Gray,
                Offset(0F, (maximum - minimum) * chunkHeightScale),
                Offset(size.width, (maximum - minimum) * chunkHeightScale)
            )

            for (record in records.drop(itemsToSkip)) {
                val firstPoint = Offset(x, (maximum - previousLevel) * chunkHeightScale)
                val bottomPoint = Offset(x, (maximum - minimum) * chunkHeightScale + 8)
                val secondPoint =
                    Offset(x + chunkWidth, (maximum - record.items.size) * chunkHeightScale)

                drawLine(Color(33, 107, 196), firstPoint, secondPoint, strokeWidth = 4F)
                drawLine(Color(99, 99, 99), bottomPoint, firstPoint, strokeWidth = 2F)

                drawText(
                    textMeasurer = textMeasure,
                    text = buildAnnotatedString {
                        withStyle(SpanStyle(fontSize = 10.sp)) {
                           append("${(record.from / 60).toString().padStart(2, '0')}H")
                        }
                    },
                    topLeft = bottomPoint.copy(x - 20F),
                )

                x += chunkWidth
                previousLevel = record.items.size
            }
        }
    })
}

@Composable
private fun ServiceControl(
    serviceState: ServiceUiState,
    viewModel: SystemViewModel,
    context: Context,
    onDeleteClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Button(onClick = { onDeleteClick() }) {
            Text(text = "Delete all")
        }
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


@Composable
fun ConfirmationDialog(
    message: String,
    confirmButtonText: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text(modifier = Modifier.padding(vertical = 8.dp), text = "Please confirm") },
        text = { Text(text = message) },
        confirmButton = {
            Button(onClick = {
                onConfirm()
                onDismiss()
            }) { Text(confirmButtonText) }
        },
        dismissButton = { Button(onClick = { onDismiss() }) { Text("Cancel") } }
    )
}