package com.darekbx.nightactivitymonitor.system

import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

@Composable
fun RequestPermission(
    permissions: Array<String>,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val permissionGranted = permissions.all { permission ->
        (ContextCompat.checkSelfPermission(
            context,
            permission
        ) == PackageManager.PERMISSION_GRANTED)
    }

    content()

    if (!permissionGranted) {
        val launcher = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { }
        SideEffect {
            launcher.launch(permissions)
        }
    }
}