package com.example.common

import android.app.ActivityManager
import android.content.Context
import android.os.SystemClock
import android.util.Log
import java.util.Timer
import java.util.TimerTask
import kotlin.math.max
import kotlin.math.min


class Measurer(context: Context) {
    private var fpsTimer: Timer? = null
    private var framesPerSecond: Int = 0
    private var frameProcessedInOneSecondInterval: Int = 0

    private var frameLatencyMs = 0L
    private var frameStartMs = 0L
    private var frameMaxLatencyMs = 0L
    private var frameMinLatencyMs = Long.MAX_VALUE

    private var detectorLatencyMs = 0L
    private var detectorStartMs = 0L
    private var detectorMaxLatencyMs = 0L
    private var detectorMinLatencyMs = Long.MAX_VALUE

    private var numRuns = 0
    private var totalFrameLatencyMs = 0L
    private var totalDetectorLatencyMs = 0L

    private var activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager

    fun startTimer() {
        fpsTimer = Timer()
        fpsTimer?.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                framesPerSecond = frameProcessedInOneSecondInterval
                frameProcessedInOneSecondInterval = 0
            }
        }, 0L, 1000L)
    }

    fun onFrameStart() {
        frameStartMs = SystemClock.elapsedRealtime()
    }

    fun onDetectStart() {
        detectorStartMs = SystemClock.elapsedRealtime()
    }

    fun onProcessed() {
        val endMs = SystemClock.elapsedRealtime()
        frameLatencyMs = endMs - frameStartMs
        detectorLatencyMs = endMs - detectorStartMs

        frameMaxLatencyMs = max(frameMaxLatencyMs, frameLatencyMs)
        frameMinLatencyMs = min(frameMinLatencyMs, frameLatencyMs)

        detectorMaxLatencyMs = max(detectorMaxLatencyMs, detectorLatencyMs)
        detectorMinLatencyMs = min(detectorMinLatencyMs, detectorLatencyMs)

        numRuns++
        totalFrameLatencyMs += frameLatencyMs
        totalDetectorLatencyMs += detectorLatencyMs

        if (numRuns < 0L || totalFrameLatencyMs < 0L || totalDetectorLatencyMs < 0L) {
            numRuns = 0
            totalFrameLatencyMs = 0L
            totalDetectorLatencyMs = 0L
        }

        frameProcessedInOneSecondInterval++

        printLog()
    }

    fun stopTimer() {
        numRuns = 0
        totalFrameLatencyMs = 0L
        totalDetectorLatencyMs = 0L
        fpsTimer?.cancel()
        fpsTimer = null
    }

    private fun printLog() {
        if (frameProcessedInOneSecondInterval == 1) {
            val mi = ActivityManager.MemoryInfo()
            activityManager.getMemoryInfo(mi)
            val availableMegs = mi.availMem / 0x100000L

            buildString {
                appendLine("Memory available          : ${availableMegs}MB")
                appendLine("FramesPerSecond           : ${framesPerSecond}")
                appendLine("Frame latency (Current)   : ${frameLatencyMs}ms")
                appendLine("Frame latency (Average)   : ${totalFrameLatencyMs / numRuns}ms")
                appendLine("Frame latency (Max)       : ${frameMaxLatencyMs}ms")
                appendLine("Frame latency (Min)       : ${frameMinLatencyMs}ms")
                appendLine("Detector latency (Current): ${detectorLatencyMs}ms")
                appendLine("Detector latency (Average): ${totalDetectorLatencyMs / numRuns}ms")
                appendLine("Detector latency (Max)    : ${detectorMaxLatencyMs}ms")
                appendLine("Detector latency (Min)    : ${detectorMinLatencyMs}ms")
            }.let {
                Log.i(TAG, it)
            }
        }
    }

    companion object {
        private const val TAG = "Measurer"
    }
}
