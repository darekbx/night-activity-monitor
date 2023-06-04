package com.darekbx.nightactivitymonitor.di

import android.app.NotificationManager
import android.bluetooth.BluetoothManager
import android.content.Context
import androidx.room.Room
import com.darekbx.nightactivitymonitor.repository.LocalRepository
import com.darekbx.nightactivitymonitor.repository.local.AppDatabase
import com.darekbx.nightactivitymonitor.repository.local.AppDatabase.Companion.DB_NAME
import com.darekbx.nightactivitymonitor.system.BluetoothUtils
import com.darekbx.nightactivitymonitor.system.NotificationUtil
import com.darekbx.nightactivitymonitor.ui.SystemViewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val mainModules = module {

    single { androidContext().getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager }
    single { androidContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager }
    single { BluetoothUtils(get()) }
    single { NotificationUtil(get(), get()) }
    single { Room.databaseBuilder(androidApplication(), AppDatabase::class.java, DB_NAME).build() }
    single { get<AppDatabase>().logDao() }
    single { LocalRepository(get()) }

    viewModel { SystemViewModel(get(), get()) }
}
