package com.vectorpeaks.edulink

import android.app.Application
import com.vectorpeaks.edulink.network.RetrofitClient
import timber.log.Timber

/**
 * Main Application class. Acts as the global entry point for the app process.
 * Used to initialize singletons and background-safe components.
 */
class EduLinkApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        Timber.plant(Timber.DebugTree())

        // secure initialization: it's goint to initialize always
        // even when system is going to init only background firebase service
        RetrofitClient.initialize(this)
    }
}