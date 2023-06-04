package com.darekbx.nightactivitymonitor

import android.app.Application
import com.darekbx.nightactivitymonitor.di.mainModules
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class MainApplication: Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@MainApplication)
            modules(mainModules)
        }
    }
}
