package com.androidperformance.fps

import android.annotation.TargetApi
import android.app.Activity
import android.app.Application
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.FrameMetrics
import android.view.Window
import android.view.Window.OnFrameMetricsAvailableListener

class FrameMetricsListener : Application.ActivityLifecycleCallbacks {
    private var warningLevelMs = 0f
    private var errorLevelMs = 0f
    private var showWarning = false
    private var showError = false

    private val frameMetricsAvailableListenerMap: HashMap<String, OnFrameMetricsAvailableListener> =
        HashMap()

    override fun onActivityPaused(activity: Activity) {
        stopFrameMetrics(activity);
    }

    override fun onActivityStarted(activity: Activity) {
    }

    override fun onActivityDestroyed(activity: Activity) {
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
    }

    override fun onActivityStopped(activity: Activity) {
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
    }

    override fun onActivityResumed(activity: Activity) {
        startFrameMetrics(activity);
    }

    @TargetApi(Build.VERSION_CODES.N)
    fun startFrameMetrics(activity: Activity) {
        val activityName = activity.javaClass.simpleName
        val listener: OnFrameMetricsAvailableListener =
            object : OnFrameMetricsAvailableListener {
                private var allFrames = 0
                private var jankyFrames = 0
                override fun onFrameMetricsAvailable(
                    window: Window,
                    frameMetrics: FrameMetrics,
                    dropCountSinceLastInvocation: Int
                ) {
                    val frameMetricsCopy = FrameMetrics(frameMetrics)
                    allFrames++
                    val totalDurationMs =
                        (0.000001 * frameMetricsCopy.getMetric(FrameMetrics.TOTAL_DURATION)).toFloat()
                    if (totalDurationMs > warningLevelMs) {
                        jankyFrames++
                        var msg = String.format(
                            "Janky frame detected on %s with total duration: %.2fms\n",
                            activityName,
                            totalDurationMs
                        )
                        val inputMeasureDurationMs =
                            (0.000001 * frameMetricsCopy.getMetric(FrameMetrics.INPUT_HANDLING_DURATION)).toFloat()
                        val animationDurationMs =
                            (0.000001 * frameMetricsCopy.getMetric(FrameMetrics.ANIMATION_DURATION)).toFloat()
                        val layoutMeasureDurationMs =
                            (0.000001 * frameMetricsCopy.getMetric(FrameMetrics.LAYOUT_MEASURE_DURATION)).toFloat()
                        val drawDurationMs =
                            (0.000001 * frameMetricsCopy.getMetric(FrameMetrics.DRAW_DURATION)).toFloat()
                        val syncDurationMs =
                            (0.000001 * frameMetricsCopy.getMetric(FrameMetrics.SYNC_DURATION)).toFloat()
                        val gpuCommandMs =
                            (0.000001 * frameMetricsCopy.getMetric(FrameMetrics.COMMAND_ISSUE_DURATION)).toFloat()
                        val swapBufferDurationMs =
                            (0.000001 * frameMetricsCopy.getMetric(FrameMetrics.SWAP_BUFFERS_DURATION)).toFloat()
                        val perFrameTotalDurationMs =
                            (0.000001 * frameMetricsCopy.getMetric(FrameMetrics.TOTAL_DURATION)).toFloat()
                        val othersMs =
                            totalDurationMs - layoutMeasureDurationMs - drawDurationMs - gpuCommandMs
                        val jankyPercent =
                            jankyFrames.toFloat() / allFrames * 100
                        msg += String.format(
                            "Activity : %s " +
                                    " input: %.2fms " +
                                    " animation: %.2fms " +
                                    " Layout/measure: %.2fms " +
                                    " draw:%.2fms " +
                                    " sync :%.2fms " +
                                    " gpuCommand:%.2fms " +
                                    " swapBuffer :%.2fms " +
                                    " total:%.2fms " +
                                    " jankyFrames :%d " +
                                    " allFrames :%d\n ",
                            activityName,
                            inputMeasureDurationMs,
                            animationDurationMs,
                            layoutMeasureDurationMs,
                            drawDurationMs,
                            syncDurationMs,
                            gpuCommandMs,
                            swapBufferDurationMs,
                            perFrameTotalDurationMs,
                            jankyFrames,
                            allFrames
                        )
                        msg += "Janky frames: $jankyFrames/$allFrames($jankyPercent%)"
                        if (showWarning && totalDurationMs > errorLevelMs) {
                            Log.e("FrameMetrics", msg)
                        } else if (showError) {
                            Log.w("FrameMetrics", msg)
                        }
                    }
                }
            }
        activity.window.addOnFrameMetricsAvailableListener(listener, Handler())
        frameMetricsAvailableListenerMap[activityName] = listener
    }

    @TargetApi(Build.VERSION_CODES.N)
    fun stopFrameMetrics(activity: Activity) {
        val activityName = activity.javaClass.name
        val onFrameMetricsAvailableListener =
            frameMetricsAvailableListenerMap[activityName]
        if (onFrameMetricsAvailableListener != null) {
            activity.window
                .removeOnFrameMetricsAvailableListener(onFrameMetricsAvailableListener)
            frameMetricsAvailableListenerMap.remove(activityName)
        }
    }


    class Builder {
        private val DEFAULT_WARNING_LEVEL_MS = 17f
        private val DEFAULT_ERROR_LEVEL_MS = 34f
        private var warningLevelMs: Float = DEFAULT_WARNING_LEVEL_MS
        private var errorLevelMs: Float = DEFAULT_ERROR_LEVEL_MS
        private var showWarnings = true
        private var showErrors = true
        fun warningLevelMs(warningLevelMs: Float): Builder {
            this.warningLevelMs = warningLevelMs
            return this
        }

        fun errorLevelMs(errorLevelMs: Float): Builder {
            this.errorLevelMs = errorLevelMs
            return this
        }

        fun showWarnings(show: Boolean): Builder {
            showWarnings = show
            return this
        }

        fun showErrors(show: Boolean): Builder {
            showErrors = show
            return this
        }

        fun build(): FrameMetricsListener {
            val activityFrameMetrics = FrameMetricsListener()
            activityFrameMetrics.warningLevelMs = warningLevelMs
            activityFrameMetrics.errorLevelMs = errorLevelMs
            activityFrameMetrics.showError = showErrors
            activityFrameMetrics.showWarning = showWarnings
            return activityFrameMetrics
        }
    }

}