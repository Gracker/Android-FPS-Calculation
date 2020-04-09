package com.androidperformance.fps

import android.app.Application

class FrameMetricsApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        registerActivityLifecycleCallbacks(
            FrameMetricsListener.Builder().showErrors(true)
                .showWarnings(true)
                .errorLevelMs(0f)
                .warningLevelMs(0f)
                .build()
        )
    }
}