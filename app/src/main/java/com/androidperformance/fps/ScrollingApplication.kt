package com.androidperformance.fps

import android.app.Application

class ScrollingApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        registerActivityLifecycleCallbacks(FrameMetricsListener.Builder().build())
    }
}