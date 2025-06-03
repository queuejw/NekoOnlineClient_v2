package ru.neko.online.client.components.app

import android.app.Application
import ru.neko.online.client.components.error.CriticalErrorDetector

class NekoOnlineApplication: Application() {

    private lateinit var criticalErrorDetector: CriticalErrorDetector

    override fun onCreate() {
        criticalErrorDetector = CriticalErrorDetector(applicationContext)
        Thread.setDefaultUncaughtExceptionHandler(criticalErrorDetector)
        super.onCreate()
    }
}